package co.highfive.petrolstation.data.local.repo;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.dao.InvoiceDao;
import co.highfive.petrolstation.data.local.dao.OfflineInvoiceDao;
import co.highfive.petrolstation.data.local.dao.UnifiedInvoiceRow;
import co.highfive.petrolstation.data.local.entities.OfflineInvoiceEntity;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceAddRequest;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.pos.dto.PosOfflineSaleRequest;

public class InvoiceLocalRepository {

    public static final int SYNC_PENDING = 0;
    public static final int SYNC_SENT    = 1;
    public static final int SYNC_FAILED  = 2;

    private final InvoiceDao invoiceDao;
    private final OfflineInvoiceDao offlineDao;
    private final Gson gson;
    private final ApiClient apiClient;

    public InvoiceLocalRepository(@NonNull Context ctx,
                                  @NonNull AppDatabase db,
                                  @NonNull Gson gson,
                                  @NonNull ApiClient apiClient) {
        this.invoiceDao = db.invoiceDao();
        this.offlineDao = db.offlineInvoiceDao();
        this.gson = gson;
        this.apiClient = apiClient;
    }

    // ========= UI Feed =========

    public List<UnifiedInvoiceRow> getUnifiedInvoicesByCustomerPaged(int customerId, int isFuelSale, int limit, int offset) {
        return invoiceDao.getUnifiedInvoicesByCustomerPaged(customerId, isFuelSale, limit, offset);
    }

    public int countUnifiedByCustomer(int customerId, int isFuelSale) {
        return invoiceDao.countUnifiedByCustomer(customerId, isFuelSale);
    }

    // ========= Save Offline =========

    public long saveFuelSaleOffline(@NonNull FuelPriceAddRequest req,
                                    @NonNull String statement,
                                    Double total,
                                    String invoiceNoPlaceholder) {

        OfflineInvoiceEntity e = new OfflineInvoiceEntity();
        e.onlineId = null;

        e.customerId = req.customerId != null ? req.customerId : 0;
        e.accountId = req.accountId;

        e.customerVehicleId = req.customerVehicleId;
        e.pumpId = req.pumpId;
        e.campaignId = req.campaignId;

        e.isFuelSale = 1;

        e.statement = statement;
        e.total = total != null ? total : 0.0;
        e.invoiceNo = invoiceNoPlaceholder;
        e.notes = req.notes;

        e.requestJson = gson.toJson(req);

        e.syncStatus = SYNC_PENDING;
        e.syncError = null;

        long now = System.currentTimeMillis();
        e.createdAtTs = now;
        e.updatedAtTs = now;

        return offlineDao.insert(e);
    }

    // ========= Sync =========

    public void syncPendingFuelInvoices(int limit, @NonNull SyncListener listener) {

        List<OfflineInvoiceEntity> pending = offlineDao.getPending(limit);
        if (pending == null || pending.isEmpty()) {
            listener.onDone(0, 0, 0);
            return;
        }

        // Sync sequentially (one by one) لتجنب مشاكل الترتيب وتكرار الإرسال
        syncNext(0, pending, listener, 0, 0, 0);
    }

    private void syncNext(int index,
                          List<OfflineInvoiceEntity> list,
                          SyncListener listener,
                          int success,
                          int failed,
                          int skipped) {

        if (index >= list.size()) {
            listener.onDone(success, failed, skipped);
            return;
        }

        OfflineInvoiceEntity row = list.get(index);
        if (row == null || row.requestJson == null || row.requestJson.trim().isEmpty()) {
            syncNext(index + 1, list, listener, success, failed, skipped + 1);
            return;
        }

        FuelPriceAddRequest req = parseFuelRequest(row.requestJson);
        if (req == null || req.customerId == null || req.customerId <= 0) {
            row.syncStatus = SYNC_FAILED;
            row.syncError = "Invalid request payload (missing customerId)";
            row.updatedAtTs = System.currentTimeMillis();
            offlineDao.update(row);

            syncNext(index + 1, list, listener, success, failed + 1, skipped);
            return;
        }

        // Mark SENT (in-progress)
        row.syncStatus = SYNC_SENT;
        row.syncError = null;
        row.updatedAtTs = System.currentTimeMillis();
        offlineDao.update(row);

        ApiClient.ApiParams params = buildSaveFuelSaleParams(req);

        Type type = new TypeToken<BaseResponse<co.highfive.petrolstation.customers.dto.InvoiceDto>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_POST,
                co.highfive.petrolstation.network.Endpoints.FUEL_PRICE_ADD,
                params,
                null,
                type,
                0,
                new ApiCallback<co.highfive.petrolstation.customers.dto.InvoiceDto>() {
                    @Override
                    public void onSuccess(co.highfive.petrolstation.customers.dto.InvoiceDto data, String msg, String rawJson) {

                        // ✅ خذ online id من السيرفر
                        Integer onlineId = (data != null) ? data.id : null;
                        row.onlineId = onlineId;

                        // ✅ الأفضل: احذف الأوفلاين بعد نجاح الإرسال لتفادي التكرار في الـ UNION
                        offlineDao.deleteByLocalId(row.localId);

                        // (اختياري) هنا تعمل upsert للفاتورة في invoices لو عندك mapper جاهز من InvoiceDto -> InvoiceEntity
                        // invoiceDao.upsert(mapToInvoiceEntity(data));

                        syncNext(index + 1, list, listener, success + 1, failed, skipped);
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        row.syncStatus = SYNC_FAILED;
                        row.syncError = error != null ? error.message : "API error";
                        row.updatedAtTs = System.currentTimeMillis();
                        offlineDao.update(row);

                        syncNext(index + 1, list, listener, success, failed + 1, skipped);
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        row.syncStatus = SYNC_FAILED;
                        row.syncError = "Unauthorized";
                        row.updatedAtTs = System.currentTimeMillis();
                        offlineDao.update(row);

                        syncNext(index + 1, list, listener, success, failed + 1, skipped);
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        // رجّعها PENDING لأنه لسه النت سيء
                        row.syncStatus = SYNC_PENDING;
                        row.syncError = "Network: " + reason;
                        row.updatedAtTs = System.currentTimeMillis();
                        offlineDao.update(row);

                        // وقف السينك الآن (لأنه واضح النت مش ثابت)
                        listener.onDone(success, failed, skipped);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        row.syncStatus = SYNC_FAILED;
                        row.syncError = "Parse error";
                        row.updatedAtTs = System.currentTimeMillis();
                        offlineDao.update(row);

                        syncNext(index + 1, list, listener, success, failed + 1, skipped);
                    }
                }
        );
    }

    private FuelPriceAddRequest parseFuelRequest(String json) {
        try {
            return gson.fromJson(json, FuelPriceAddRequest.class);
        } catch (Exception e) {
            return null;
        }
    }

    private ApiClient.ApiParams buildSaveFuelSaleParams(@NonNull FuelPriceAddRequest r) {
        ApiClient.ApiParams p = new ApiClient.ApiParams();

        if (r.itemIds != null) for (Integer id : r.itemIds) p.add("item_id[]", id != null ? id : 0);
        if (r.prices  != null) for (Double  pr : r.prices)  p.add("price[]",   pr != null ? pr : 0);
        if (r.counts  != null) for (Double  c  : r.counts)  p.add("count[]",   c  != null ? c  : 0);

        if (r.accountId != null) p.add("account_id", r.accountId);

        if (r.customerVehicleId != null && r.customerVehicleId > 0) p.add("customer_vehicle_id", r.customerVehicleId);
        if (r.pumpId != null) p.add("pump_id", r.pumpId);
        if (r.campaignId != null) p.add("campaign_id", r.campaignId);
        if (r.notes != null) p.add("notes", r.notes);

        if (r.paymentMethods != null) p.add("payment_methods", gson.toJson(r.paymentMethods));
        else p.add("payment_methods", "[]");

        // ✅ customer_id لازم
        if (r.customerId != null) p.add("customer_id", r.customerId);

        return p;
    }

    public interface SyncListener {
        void onDone(int success, int failed, int skipped);
    }

    public long savePosSaleOffline(@NonNull PosOfflineSaleRequest req,
                                   @NonNull String statement,
                                   Double total,
                                   String invoiceNoPlaceholder) {

        OfflineInvoiceEntity e = new OfflineInvoiceEntity();
        e.onlineId = null;

        // Required
        e.customerId =  req.customerId ;

        // Optional links
        e.accountId = req.accountId;
//        e.customerVehicleId = req.customerVehicleId;
//        e.pumpId = req.pumpId;
//        e.campaignId = req.campaignId;

        // POS/Fuel sale flag
        e.isFuelSale = 0;

        // Snapshot for UI
        e.statement = statement;
        e.total = (total != null) ? total : 0.0;
        e.invoiceNo = invoiceNoPlaceholder;
        e.notes = req.notes;

        // Full request payload
        e.requestJson = gson.toJson(req);

        // Sync state
        e.syncStatus = SYNC_PENDING;
        e.syncError = null;

        long now = System.currentTimeMillis();
        e.createdAtTs = now;
        e.updatedAtTs = now;

        return offlineDao.insert(e);
    }



}
