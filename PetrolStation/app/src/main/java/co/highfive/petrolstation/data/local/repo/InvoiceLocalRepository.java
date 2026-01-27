package co.highfive.petrolstation.data.local.repo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.dao.InvoiceDao;
import co.highfive.petrolstation.data.local.dao.OfflineInvoiceDao;
import co.highfive.petrolstation.data.local.dao.UnifiedInvoiceRow;
import co.highfive.petrolstation.data.local.entities.OfflineFuelInvoiceEntity;
import co.highfive.petrolstation.data.local.entities.OfflineInvoiceEntity;
import co.highfive.petrolstation.fuelsale.dto.FuelCustomerDto;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceAddRequest;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.pos.dto.PosOfflineSaleRequest;
import co.highfive.petrolstation.data.local.dao.OfflineFuelInvoiceDao;

public class InvoiceLocalRepository {

    public static final int SYNC_PENDING = 0;
    public static final int SYNC_SENT    = 1;
    public static final int SYNC_FAILED  = 2;

    private final InvoiceDao invoiceDao;
    private final OfflineInvoiceDao offlineDao;
//    private final OfflineFuelInvoiceDao offlineFuelInvoiceDao;
    private final Gson gson;
    private final ApiClient apiClient;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    public InvoiceLocalRepository(@NonNull Context ctx,
                                  @NonNull AppDatabase db,
                                  @NonNull Gson gson,
                                  @NonNull ApiClient apiClient) {
        this.invoiceDao = db.invoiceDao();
        this.offlineDao = db.offlineInvoiceDao();
//        this.offlineFuelInvoiceDao = db.offlineFuelInvoiceDao();
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


    private String buildOfflineFuelInvoiceRequestJson(
            @NonNull OfflineFuelInvoiceEntity e,
            @NonNull FuelPriceAddRequest req,
            @NonNull FuelCustomerDto customer,
            @Nullable CustomerVehicleDto vehicle
    ) {
        try {
            Map<String, Object> root = new HashMap<>();

            root.put("local_id", e.localId);
            root.put("statement", e.statement);
            root.put("total", e.total);
            root.put("invoice_no_placeholder", e.invoiceNoPlaceholder);

            // References
            Map<String, Object> refs = new HashMap<>();
            refs.put("customer_online_id", e.customerOnlineId);
            refs.put("customer_local_id", e.customerLocalId);

            refs.put("vehicle_online_id", e.vehicleOnlineId);
            refs.put("vehicle_local_id", e.vehicleLocalId);

            refs.put("pump_id", e.pumpId);
            refs.put("campaign_id", e.campaignId);
            root.put("refs", refs);

            // Raw payload (FuelPriceAddRequest as built by UI)
            root.put("payload", req);

            // ✅ Customer snapshot (POS-like)
            Map<String, Object> customerSnap = new HashMap<>();
            customerSnap.put("local_id", e.customerLocalId != null ? e.customerLocalId : null);
            customerSnap.put("name", customer.name != null ? customer.name : "");
            customerSnap.put("mobile", customer.mobile != null ? customer.mobile : "");
            root.put("customer_snapshot", customerSnap);

            // ✅ Vehicle snapshot (full enough to re-send)
            if (vehicle != null) {
                Map<String, Object> vehicleSnap = new HashMap<>();
                vehicleSnap.put("local_id", e.vehicleLocalId != null ? e.vehicleLocalId : null);

                vehicleSnap.put("vehicle_number", vehicle.vehicle_number != null ? vehicle.vehicle_number : "");
                vehicleSnap.put("vehicle_type", vehicle.vehicle_type != null ? vehicle.vehicle_type : 0);
                vehicleSnap.put("vehicle_color", vehicle.vehicle_color != null ? vehicle.vehicle_color : 0);

                vehicleSnap.put("model", vehicle.model != null ? vehicle.model : "");
                vehicleSnap.put("license_expiry_date", vehicle.license_expiry_date != null ? vehicle.license_expiry_date : "");
                vehicleSnap.put("notes", vehicle.notes != null ? vehicle.notes : "");

                // Optional (nice to have)
                vehicleSnap.put("vehicle_type_name", vehicle.vehicle_type_name != null ? vehicle.vehicle_type_name : "");
                vehicleSnap.put("vehicle_color_name", vehicle.vehicle_color_name != null ? vehicle.vehicle_color_name : "");

                root.put("vehicle_snapshot", vehicleSnap);
            }

            return gson.toJson(root);
        } catch (Exception ignored) {
            return null;
        }
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
        if (req == null || req.customerId == 0 || req.customerId <= 0) {
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
        if (r.customerId != 0) p.add("customer_id", r.customerId);

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
        e.customerName =  req.customerName ;
        e.customerMobile =  req.customerMobile ;

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

    public long saveFuelSaleOffline(
            @NonNull FuelPriceAddRequest req,
            @NonNull FuelCustomerDto customer,
            @Nullable CustomerVehicleDto vehicle,
            @NonNull String statement,
            double total,
            @NonNull String invoiceNoPlaceholder
    ) {

//        long now = System.currentTimeMillis();

//        OfflineFuelInvoiceEntity e = new OfflineFuelInvoiceEntity();
//        e.statement = statement;
//        e.total = total;
//        e.invoiceNoPlaceholder = invoiceNoPlaceholder;
//
//        // pump/campaign (safe)
//        e.pumpId = req.pumpId != null ? req.pumpId : null;
//        e.campaignId = req.campaignId != null ? req.campaignId : null;
//
//        // ===== Customer reference (works for online/offline)
//        // Online customer: id > 0
//        if (customer.id > 0) {
//            e.customerOnlineId = customer.id;
//            e.customerLocalId = null;
//        } else if (customer.is_offline && customer.local_id > 0 && customer.id < 0) {
//            e.customerOnlineId = null;
//            e.customerLocalId = customer.local_id;
//        } else {
//            // لا تحفظ فاتورة بدون customer valid
//            throw new IllegalStateException("Invalid customer for offline invoice");
//        }
//
//        // ===== Vehicle reference (optional; works for online/offline)
//        if (vehicle != null) {
//            if (vehicle.id > 0) {
//                e.vehicleOnlineId = vehicle.id;
//                e.vehicleLocalId = null;
//            } else if (vehicle.id < 0) {
//                // عندك vehicle offline معمول id = -localId
//                // إذا عندك localId حقيقي في DTO ضيفه، وإلا استخرجه من id
//                long vehicleLocalId = 0;
//                try {
//                    // الأفضل: تضيف vehicle.local_id مثل customer
//                    // لكن إن لم يوجد: استنتجه من id السالب
//                    vehicleLocalId = (long) (-vehicle.id);
//                } catch (Exception ignored) {}
//
//                if (vehicleLocalId > 0) {
//                    e.vehicleOnlineId = null;
//                    e.vehicleLocalId = vehicleLocalId;
//                }
//            }
//        }
//
//        e.syncStatus = 0;
//        e.syncError = null;
//        e.createdAtTs = now;
//        e.updatedAtTs = now;

        // insert first to get localId
//        long localId = offlineFuelInvoiceDao.insert(e);
//        e.localId = localId;
//        e.requestJson = buildOfflineFuelInvoiceRequestJson(e, req, customer, vehicle);
//        offlineFuelInvoiceDao.update(e);

//        return localId;

        OfflineInvoiceEntity e = new OfflineInvoiceEntity();
        e.onlineId = null;

        // Required
//        e.customerId =  req.customerId ;
        if (customer.id > 0) {
            e.customerId = customer.id;
        } else if (customer.is_offline && customer.local_id > 0 && customer.id < 0) {
            e.customerId =(int) customer.local_id;
        } else {
            // لا تحفظ فاتورة بدون customer valid
            throw new IllegalStateException("Invalid customer for offline invoice");
        }


        e.customerName =  customer.name ;
        e.customerMobile =  customer.mobile ;

        // Optional links
        e.accountId = req.accountId;
        if (vehicle != null) {
            e.vehicleColor = vehicle.vehicle_color;
            e.vehicleNumber = vehicle.vehicle_number;
            e.vehicleType = vehicle.vehicle_type;
            e.vehicleModel = vehicle.model;
            e.vehicleNotes = vehicle.notes;
            e.licenseExpiryDate= vehicle.license_expiry_date;

            if (vehicle.id > 0) {
                e.customerVehicleId = vehicle.id;
            } else if (vehicle.id < 0) {
                // عندك vehicle offline معمول id = -localId
                // إذا عندك localId حقيقي في DTO ضيفه، وإلا استخرجه من id
                long vehicleLocalId = 0;
                try {
                    // الأفضل: تضيف vehicle.local_id مثل customer
                    // لكن إن لم يوجد: استنتجه من id السالب
                    vehicleLocalId = (long) (-vehicle.id);
                } catch (Exception ignored) {}

                if (vehicleLocalId > 0) {
                    e.customerVehicleId = (int)vehicleLocalId;
                }
            }
        }

//        e.customerVehicleId = req.customerVehicleId;
        e.pumpId = req.pumpId;
        e.campaignId = req.campaignId;

        // POS/Fuel sale flag
        e.isFuelSale = 1;

        // Snapshot for UI
        e.statement = statement;
        e.total =   total  ;
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

//        Log.e("e",""+e.toString());

        return offlineDao.insert(e);
//        return 0;
    }




// ========= Sync (NEW JSON endpoints) =========

    public void syncPendingInvoicesJson(int limit, @NonNull SyncListener listener) {

        List<OfflineInvoiceEntity> pending = offlineDao.getPending(limit);
        if (pending == null || pending.isEmpty()) {
            listener.onDone(0, 0, 0);
            return;
        }

        syncNextJson(0, pending, listener, 0, 0, 0);
    }

    private void syncNextJson(int index,
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
            syncNextJson(index + 1, list, listener, success, failed, skipped + 1);
            return;
        }

        // Mark SENT (in-progress)
        row.syncStatus = SYNC_SENT;
        row.syncError = null;
        row.updatedAtTs = System.currentTimeMillis();
        offlineDao.update(row);

        // Decide endpoint
        final String endpoint = (row.isFuelSale == 1)
                ? "/api/fuel-price/add-json"
                : "/api/pos/add-json";

        // Build "data" = [ { ... } ]
        String dataArrayJson = null;

        try {
            if (row.isFuelSale == 1) {
                FuelPriceAddRequest fuelReq = gson.fromJson(row.requestJson, FuelPriceAddRequest.class);
                if (fuelReq == null) throw new IllegalStateException("Fuel request null");
                dataArrayJson = buildFuelDataArrayJson(row, fuelReq);
            } else {
                PosOfflineSaleRequest posReq = gson.fromJson(row.requestJson, PosOfflineSaleRequest.class);
                if (posReq == null) throw new IllegalStateException("POS request null");
                dataArrayJson = buildPosDataArrayJson(row, posReq);
            }
        } catch (Exception e) {
            row.syncStatus = SYNC_FAILED;
            row.syncError = "Invalid request payload";
            row.updatedAtTs = System.currentTimeMillis();
            offlineDao.update(row);

            syncNextJson(index + 1, list, listener, success, failed + 1, skipped);
            return;
        }

        if (dataArrayJson == null || dataArrayJson.trim().isEmpty()) {
            row.syncStatus = SYNC_FAILED;
            row.syncError = "Empty data payload";
            row.updatedAtTs = System.currentTimeMillis();
            offlineDao.update(row);

            syncNextJson(index + 1, list, listener, success, failed + 1, skipped);
            return;
        }

        ApiClient.ApiParams params = new ApiClient.ApiParams();
        params.add("data", dataArrayJson);

        Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

        apiClient.request(
                co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_POST,
                endpoint,
                params,
                null,
                type,
                0,
                new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String msg, String rawJson) {
                        // ✅ success => delete local row
                        offlineDao.deleteByLocalId(row.localId);
                        syncNextJson(index + 1, list, listener, success + 1, failed, skipped);
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        row.syncStatus = SYNC_FAILED;
                        row.syncError = error != null ? error.message : "API error";
                        row.updatedAtTs = System.currentTimeMillis();
                        offlineDao.update(row);

                        syncNextJson(index + 1, list, listener, success, failed + 1, skipped);
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        row.syncStatus = SYNC_FAILED;
                        row.syncError = "Unauthorized";
                        row.updatedAtTs = System.currentTimeMillis();
                        offlineDao.update(row);

                        syncNextJson(index + 1, list, listener, success, failed + 1, skipped);
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        // رجّعها pending (وقف)
                        row.syncStatus = SYNC_PENDING;
                        row.syncError = "Network: " + reason;
                        row.updatedAtTs = System.currentTimeMillis();
                        offlineDao.update(row);

                        listener.onDone(success, failed, skipped);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        row.syncStatus = SYNC_FAILED;
                        row.syncError = "Parse error";
                        row.updatedAtTs = System.currentTimeMillis();
                        offlineDao.update(row);

                        syncNextJson(index + 1, list, listener, success, failed + 1, skipped);
                    }
                }
        );
    }

    private String todayDate() {
        try {
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            return f.format(new java.util.Date());
        } catch (Exception e) {
            return "2024-01-01";
        }
    }

    /**
     * POS JSON: account_id optional + offline_customer object
     */
    private String buildPosDataArrayJson(@NonNull OfflineInvoiceEntity row,
                                         @NonNull PosOfflineSaleRequest r) {

        java.util.Map<String, Object> obj = new java.util.HashMap<>();

        obj.put("date", todayDate());
        obj.put("discount", 0); // إذا عندك discount في UI ضيفه للـ req
        obj.put("notes", r.notes != null ? r.notes : "");

        // ✅ customer handling (Option A)
        boolean isOfflineCustomer = (r.offlineCustomerLocalId > 0) || (r.customerId < 0);

        if (isOfflineCustomer) {
            long localId = (r.offlineCustomerLocalId > 0)
                    ? r.offlineCustomerLocalId
                    : Math.abs((long) r.customerId);

            java.util.Map<String, Object> oc = new java.util.HashMap<>();
            oc.put("local_id", localId);
            oc.put("name", r.customerName != null ? r.customerName : "");
            oc.put("mobile", r.customerMobile != null ? r.customerMobile : "");
            obj.put("offline_customer", oc);

            // account_id اختياري — الأفضل ما نبعته
            // obj.put("account_id", null); // لو السيرفر بقبل null
        } else {
            // Online customer => account_id مطلوب عندك عادة
            if ( r.accountId > 0) obj.put("account_id", r.accountId);
            obj.put("customer_id", r.customerId);
        }

        // items
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        if (r.items != null) {
            for (co.highfive.petrolstation.pos.dto.PosDraftItemDto it : r.items) {
                if (it == null) continue;
                int itemId = it.itemId;
                double price = it.price;
                int count = it.qty;

                if (itemId <= 0) continue;
                if (count <= 0) count = 1;

                java.util.Map<String, Object> x = new java.util.HashMap<>();
                x.put("item_id", itemId);
                x.put("price", price);
                x.put("count", count);
                items.add(x);
            }
        }
        obj.put("items", items);

        // payment_methods
        obj.put("payment_methods", (r.paymentMethods != null) ? r.paymentMethods : new java.util.ArrayList<>());

        java.util.List<java.util.Map<String, Object>> arr = new java.util.ArrayList<>();
        arr.add(obj);
        return gson.toJson(arr);
    }

    /**
     * Fuel JSON: account_id optional + offline_customer object
     * ملاحظة: FuelPriceAddRequest عندك مبني على arrays (itemIds/prices/counts)
     */
    private String buildFuelDataArrayJson(@NonNull OfflineInvoiceEntity row,
                                          @NonNull FuelPriceAddRequest r) {

        java.util.Map<String, Object> obj = new java.util.HashMap<>();

        obj.put("date", todayDate());
        obj.put("discount", 0); // إذا عندك discount في fuelReq موجود استخدمه
        obj.put("notes", r.notes != null ? r.notes : "");

        if (r.customerVehicleId != null) obj.put("customer_vehicle_id", r.customerVehicleId);
        if (r.pumpId != null) obj.put("pump_id", r.pumpId);
        if (r.campaignId != null) obj.put("campaign_id", r.campaignId);

        // ✅ customer handling (Option A)
        // FuelPriceAddRequest عندك customerId Integer (ممكن يكون سالب لو أوفلاين)
        boolean isOfflineCustomer = (r.customerId != 0 && r.customerId < 0);

        if (isOfflineCustomer) {
            long localId = Math.abs((long) r.customerId);

            // بما أنه FuelPriceAddRequest ما فيه name/mobile غالبًا،
            // نستخدم snapshot من OfflineInvoiceEntity لو متوفر عندك (إن لم يكن، لازم تضيفها هناك عند الحفظ)
            java.util.Map<String, Object> oc = new java.util.HashMap<>();
            oc.put("local_id", localId);
            // إذا عندك customerName/mobile مخزنين بالـ OfflineInvoiceEntity ضيفهم هنا
            oc.put("name", "");   // ✅ الأفضل: خزّنهم في fuel offline save مثل POS
            oc.put("mobile", ""); // ✅
            obj.put("offline_customer", oc);

            // account_id اختياري — لا تبعته
        } else {
            if (r.accountId != null) obj.put("account_id", r.accountId);
            if (r.customerId != 0) obj.put("customer_id", r.customerId);
        }

        // items[] من arrays
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        int n = 0;
        if (r.itemIds != null) n = r.itemIds.size();

        for (int i = 0; i < n; i++) {
            Integer itemId = r.itemIds.get(i);
            Double price = (r.prices != null && r.prices.size() > i) ? r.prices.get(i) : 0.0;
            Double cntD  = (r.counts != null && r.counts.size() > i) ? r.counts.get(i) : 1.0;

            int iid = (itemId != null) ? itemId : 0;
            double pr = (price != null) ? price : 0.0;
            double cnt = (cntD != null) ? cntD : 1.0;

            if (iid <= 0) continue;
            if (cnt <= 0) cnt = 1;

            java.util.Map<String, Object> x = new java.util.HashMap<>();
            x.put("item_id", iid);
            x.put("price", pr);
            x.put("count", cnt);
            items.add(x);
        }
        obj.put("items", items);

        obj.put("payment_methods", (r.paymentMethods != null) ? r.paymentMethods : new java.util.ArrayList<>());

        if(row.vehicleNumber != null){
            java.util.Map<String, Object> vehicle = new java.util.HashMap<>();
            vehicle.put("local_id", row.customerVehicleId);
            // إذا عندك customerName/mobile مخزنين بالـ OfflineInvoiceEntity ضيفهم هنا
            vehicle.put("vehicle_number", ""+row.vehicleNumber);   // ✅ الأفضل: خزّنهم في fuel offline save مثل POS
            vehicle.put("vehicle_type", ""+row.vehicleType);   // ✅ الأفضل: خزّنهم في fuel offline save مثل POS
            vehicle.put("vehicle_color", ""+row.vehicleColor);   // ✅ الأفضل: خزّنهم في fuel offline save مثل POS
            vehicle.put("model", ""+row.vehicleModel);   // ✅ الأفضل: خزّنهم في fuel offline save مثل POS
            vehicle.put("license_expiry_date", ""+row.licenseExpiryDate);   // ✅ الأفضل: خزّنهم في fuel offline save مثل POS
            vehicle.put("notes", ""+row.vehicleNotes);   // ✅ الأفضل: خزّنهم في fuel offline save مثل POS

            obj.put("offline_vehicle", vehicle);
        }
        java.util.List<java.util.Map<String, Object>> arr = new java.util.ArrayList<>();
        arr.add(obj);
        return gson.toJson(arr);
    }

    public interface SingleSyncListener {
        void onSuccess();
        void onFailed(@NonNull String errorMsg);
        void onNetwork(@NonNull String reason);
    }


    public void syncOneInvoiceJson(long offlineLocalId, @NonNull SingleSyncListener listener) {

        dbExecutor.execute(() -> {

            OfflineInvoiceEntity row = offlineDao.getByLocalId(offlineLocalId);
            if (row == null) {
                mainHandler.post(() -> listener.onFailed("Invoice not found"));
                return;
            }

            if (row.requestJson == null || row.requestJson.trim().isEmpty()) {
                row.syncStatus = SYNC_FAILED;
                row.syncError = "Empty request payload";
                row.updatedAtTs = System.currentTimeMillis();
                offlineDao.update(row);

                mainHandler.post(() -> listener.onFailed("Empty request payload"));
                return;
            }

            // mark in-progress
            row.syncStatus = SYNC_SENT;
            row.syncError = null;
            row.updatedAtTs = System.currentTimeMillis();
            offlineDao.update(row);

            final String endpoint = (row.isFuelSale == 1)
                    ? "/api/fuel-price/add-json"
                    : "/api/pos/add-json";

            String dataArrayJson;
            try {
                if (row.isFuelSale == 1) {
                    FuelPriceAddRequest fuelReq = gson.fromJson(row.requestJson, FuelPriceAddRequest.class);
                    if (fuelReq == null) throw new IllegalStateException("Fuel req null");
                    dataArrayJson = buildFuelDataArrayJson(row, fuelReq);
                } else {
                    PosOfflineSaleRequest posReq = gson.fromJson(row.requestJson, PosOfflineSaleRequest.class);
                    if (posReq == null) throw new IllegalStateException("POS req null");
                    dataArrayJson = buildPosDataArrayJson(row, posReq);
                }
            } catch (Exception e) {
                row.syncStatus = SYNC_FAILED;
                row.syncError = "Invalid request payload";
                row.updatedAtTs = System.currentTimeMillis();
                offlineDao.update(row);

                mainHandler.post(() -> listener.onFailed("Invalid request payload"));
                return;
            }

            if (dataArrayJson == null || dataArrayJson.trim().isEmpty()) {
                row.syncStatus = SYNC_FAILED;
                row.syncError = "Empty data payload";
                row.updatedAtTs = System.currentTimeMillis();
                offlineDao.update(row);

                mainHandler.post(() -> listener.onFailed("Empty data payload"));
                return;
            }

            ApiClient.ApiParams params = new ApiClient.ApiParams();
            params.add("data", dataArrayJson);

            Type type = new TypeToken<BaseResponse<Object>>() {}.getType();

            apiClient.request(
                    co.highfive.petrolstation.hazemhamadaqa.Http.Constant.REQUEST_POST,
                    endpoint,
                    params,
                    null,
                    type,
                    0,
                    new ApiCallback<Object>() {

                        @Override
                        public void onSuccess(Object data, String msg, String rawJson) {

                            SyncOneResult res = parseSyncOneResponse(rawJson);

                            dbExecutor.execute(() -> {
                                if (res.ok) {
                                    // ✅ نجحت: احذف الصف من pending (أفضل UX)
                                    offlineDao.deleteByLocalId(row.localId);

                                    mainHandler.post(listener::onSuccess);
                                } else {
                                    row.syncStatus = SYNC_FAILED;
                                    row.syncError = res.errorMessage;
                                    row.updatedAtTs = System.currentTimeMillis();
                                    offlineDao.update(row);

                                    mainHandler.post(() -> listener.onFailed(res.errorMessage));
                                }
                            });
                        }

                        @Override
                        public void onError(co.highfive.petrolstation.network.ApiError error) {
                            String em = (error != null && error.message != null) ? error.message : "API error";

                            dbExecutor.execute(() -> {
                                row.syncStatus = SYNC_FAILED;
                                row.syncError = em;
                                row.updatedAtTs = System.currentTimeMillis();
                                offlineDao.update(row);

                                mainHandler.post(() -> listener.onFailed(em));
                            });
                        }

                        @Override
                        public void onUnauthorized(String rawJson) {
                            dbExecutor.execute(() -> {
                                row.syncStatus = SYNC_FAILED;
                                row.syncError = "Unauthorized";
                                row.updatedAtTs = System.currentTimeMillis();
                                offlineDao.update(row);

                                mainHandler.post(() -> listener.onFailed("Unauthorized"));
                            });
                        }

                        @Override
                        public void onNetworkError(String reason) {
                            dbExecutor.execute(() -> {
                                row.syncStatus = SYNC_PENDING;
                                row.syncError = "Network: " + reason;
                                row.updatedAtTs = System.currentTimeMillis();
                                offlineDao.update(row);

                                mainHandler.post(() -> listener.onNetwork(reason));
                            });
                        }

                        @Override
                        public void onParseError(String rawJson, Exception e) {
                            dbExecutor.execute(() -> {
                                row.syncStatus = SYNC_FAILED;
                                row.syncError = "Parse error";
                                row.updatedAtTs = System.currentTimeMillis();
                                offlineDao.update(row);

                                mainHandler.post(() -> listener.onFailed("Parse error"));
                            });
                        }
                    }
            );
        });
    }

    private SyncOneResult parseSyncOneResponse(@NonNull String rawJson) {
        SyncOneResult r = new SyncOneResult();
        r.ok = false;
        r.errorMessage = "Unknown error";

        try {
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(rawJson).getAsJsonObject();

            boolean status = root.has("status") && root.get("status").getAsBoolean();
            String message = root.has("message") ? root.get("message").getAsString() : "";

            if (!status) {
                // أحياناً السيرفر يرجع status=false
                r.ok = false;
                r.errorMessage = !message.trim().isEmpty() ? message : "Request failed";
                return r;
            }

            if (!root.has("data") || root.get("data").isJsonNull()) {
                // status=true بس ما في data
                r.ok = true;
                r.errorMessage = "";
                return r;
            }

            com.google.gson.JsonObject data = root.getAsJsonObject("data");

            int successCount = data.has("success_count") && !data.get("success_count").isJsonNull()
                    ? data.get("success_count").getAsInt() : 0;

            int failCount = data.has("fail_count") && !data.get("fail_count").isJsonNull()
                    ? data.get("fail_count").getAsInt() : 0;

            // ✅ إذا نجح واحد على الأقل
            if (successCount > 0 && failCount == 0) {
                r.ok = true;
                r.errorMessage = "";
                return r;
            }

            // ✅ إذا فشل، خذ errors[0].errors[]
            StringBuilder sb = new StringBuilder();

            if (data.has("errors") && data.get("errors").isJsonArray()) {
                com.google.gson.JsonArray errs = data.getAsJsonArray("errors");
                if (errs.size() > 0) {

                    // في حال batch: بدك خطأ index=0 لفاتورتنا (لو بنبعت واحدة فهي دايمًا 0)
                    com.google.gson.JsonObject e0 = errs.get(0).getAsJsonObject();

                    if (e0.has("errors") && e0.get("errors").isJsonArray()) {
                        com.google.gson.JsonArray arr = e0.getAsJsonArray("errors");
                        for (int i = 0; i < arr.size(); i++) {
                            String one = arr.get(i).getAsString();
                            if (one == null) continue;
                            if (sb.length() > 0) sb.append(" | ");
                            sb.append(one.trim());
                        }
                    }
                }
            }

            String errMsg = sb.length() > 0 ? sb.toString() : (!message.trim().isEmpty() ? message : "Failed");
            r.ok = false;
            r.errorMessage = errMsg;
            return r;

        } catch (Exception ex) {
            r.ok = false;
            r.errorMessage = "Parse error";
            return r;
        }
    }

    private static class SyncOneResult {
        boolean ok;
        String errorMessage;
    }

}
