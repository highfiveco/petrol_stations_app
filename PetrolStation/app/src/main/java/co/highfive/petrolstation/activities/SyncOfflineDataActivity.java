package co.highfive.petrolstation.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.DatabaseProvider;
import co.highfive.petrolstation.data.local.dao.OfflineCustomerPhoneUpdateDao;
import co.highfive.petrolstation.data.local.dao.OfflineCustomerVehicleDao;
import co.highfive.petrolstation.data.local.dao.OfflineCustomerVehicleEditDao;
import co.highfive.petrolstation.data.local.dao.OfflineFinancialTransactionDao;
import co.highfive.petrolstation.data.local.dao.OfflineInvoiceDao;
import co.highfive.petrolstation.data.local.entities.OfflineCustomerPhoneUpdateEntity;
import co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEditEntity;
import co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity;
import co.highfive.petrolstation.data.local.entities.OfflineFinancialTransactionEntity;
import co.highfive.petrolstation.data.local.entities.OfflineInvoiceEntity;
import co.highfive.petrolstation.databinding.ActivityFuelSaleBinding;
import co.highfive.petrolstation.databinding.ActivitySyncOfflineDataBinding;
import co.highfive.petrolstation.fragments.ViewJsonDialog;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpRequest.RequestAsyncTask;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.AsyncResponse;
import co.highfive.petrolstation.hazemhamadaqa.Http.HttpResponse.model.ResponseObject;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.hazemhamadaqa.app.AppConfig;
import co.highfive.petrolstation.models.AppData;

public class SyncOfflineDataActivity extends BaseActivity {

    private ActivitySyncOfflineDataBinding binding;

    private AppDatabase db;
    private OfflineInvoiceDao offlineInvoiceDao;
    private OfflineCustomerVehicleDao offlineCustomerVehicleDao;
    private OfflineCustomerVehicleEditDao offlineCustomerVehicleEditDao;
    private OfflineCustomerPhoneUpdateDao offlineCustomerPhoneUpdateDao;
    private OfflineFinancialTransactionDao offlineFinancialTransactionDao;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private static final int PAGE_SIZE = 30;

    // invoice types
    private static final int TYPE_POS = 0;
    private static final int TYPE_FUEL_SALE = 1;

    // financial action types (Adjust if your backend uses different codes)
    private static final int ACTION_MOVE = 1;
    private static final int ACTION_REFUND = 2;
    private static final int ACTION_DISCOUNT = 3;

    // per-type sync flags
    private boolean syncingVehicles = false;
    private boolean syncingPos = false;
    private boolean syncingFuelSale = false;
    private boolean syncingPhoneUpdates = false;
    private boolean syncingMove = false;
    private boolean syncingRefund = false;
    private boolean syncingDiscount = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySyncOfflineDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_sync_offline_data);
        setupUI(binding.mainLayout);

        db = DatabaseProvider.get(this);
        offlineInvoiceDao = db.offlineInvoiceDao();
        offlineCustomerVehicleDao = db.offlineCustomerVehicleDao();
        offlineCustomerVehicleEditDao = db.offlineCustomerVehicleEditDao();
        offlineCustomerPhoneUpdateDao = db.offlineCustomerPhoneUpdateDao();
        offlineFinancialTransactionDao = db.offlineFinancialTransactionDao();

        binding.icHome.setOnClickListener(v -> moveToActivity(getApplicationContext(), MainActivity.class, null, false, true));
        binding.icBack.setOnClickListener(v -> finish());

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            refreshCounters();
        });

        // Buttons
        binding.syncAll.setOnClickListener(v -> syncAll());
        binding.viewJsonAll.setOnClickListener(v -> viewJsonAll());

        binding.syncPhoneUpdates.setOnClickListener(v -> syncPhoneUpdates());
        binding.viewJsonPhoneUpdates.setOnClickListener(v -> viewJsonPhoneUpdates());

        binding.syncCustomerVehicles.setOnClickListener(v -> syncCustomerVehicles());
        binding.viewJsonCustomerVehicles.setOnClickListener(v -> viewJsonCustomerVehicles());

        binding.syncPos.setOnClickListener(v -> syncPos());
        binding.viewJsonPos.setOnClickListener(v -> viewJsonPos());

        binding.syncFuelSale.setOnClickListener(v -> syncFuelSale());
        binding.viewJsonFuelSale.setOnClickListener(v -> viewJsonFuelSale());

        binding.syncAddMove.setOnClickListener(v -> syncFinancialByAction(ACTION_MOVE));
        binding.viewJsonAddMove.setOnClickListener(v -> viewJsonFinancialByAction(ACTION_MOVE));

        binding.syncAddLoad.setOnClickListener(v -> syncFinancialByAction(ACTION_REFUND));
        binding.viewJsonAddLoad.setOnClickListener(v -> viewJsonFinancialByAction(ACTION_REFUND));

        binding.syncAddDiscount.setOnClickListener(v -> syncFinancialByAction(ACTION_DISCOUNT));
        binding.viewJsonAddDiscount.setOnClickListener(v -> viewJsonFinancialByAction(ACTION_DISCOUNT));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCounters();
    }

    private void refreshCounters() {
        executor.execute(() -> {
            // showJson flag
            AppData appData = null;
            try {
                appData = getGson().fromJson(getSessionManager().getString(getSessionKeys().app_data), AppData.class);
            } catch (Exception ignored) {}

            final boolean showJson = appData != null && appData.getShow_json_app() == 1;

            int phoneCount = offlineCustomerPhoneUpdateDao.countPending();

            int vehiclesAddCount = offlineCustomerVehicleDao.countPending();
            int vehiclesEditCount = offlineCustomerVehicleEditDao.countPending();
            int vehiclesCount = vehiclesAddCount + vehiclesEditCount;

            int posCount = offlineInvoiceDao.countPendingByType(TYPE_POS);
            int fuelCount = offlineInvoiceDao.countPendingByType(TYPE_FUEL_SALE);

            int moveCount = offlineFinancialTransactionDao.countPendingByActionType(ACTION_MOVE);
            int refundCount = offlineFinancialTransactionDao.countPendingByActionType(ACTION_REFUND);
            int discountCount = offlineFinancialTransactionDao.countPendingByActionType(ACTION_DISCOUNT);

            final boolean noData =
                    phoneCount == 0 &&
                            vehiclesCount == 0 &&
                            posCount == 0 &&
                            fuelCount == 0 &&
                            moveCount == 0 &&
                            refundCount == 0 &&
                            discountCount == 0;

            int finalPhoneCount = phoneCount;
            int finalVehiclesCount = vehiclesCount;
            int finalPosCount = posCount;
            int finalFuelCount = fuelCount;
            int finalMoveCount = moveCount;
            int finalRefundCount = refundCount;
            int finalDiscountCount = discountCount;

            main.post(() -> {
                // JSON buttons visibility
                binding.viewJsonPhoneUpdates.setVisibility(showJson ? View.VISIBLE : View.GONE);
                binding.viewJsonCustomerVehicles.setVisibility(showJson ? View.VISIBLE : View.GONE);
                binding.viewJsonPos.setVisibility(showJson ? View.VISIBLE : View.GONE);
                binding.viewJsonFuelSale.setVisibility(showJson ? View.VISIBLE : View.GONE);
                binding.viewJsonAddMove.setVisibility(showJson ? View.VISIBLE : View.GONE);
                binding.viewJsonAddLoad.setVisibility(showJson ? View.VISIBLE : View.GONE);
                binding.viewJsonAddDiscount.setVisibility(showJson ? View.VISIBLE : View.GONE);
                binding.viewJsonAll.setVisibility(showJson ? View.VISIBLE : View.GONE);

                binding.syncPhoneUpdatesCounter.setText(finalPhoneCount + "/0");
                binding.syncCustomerVehiclesCounter.setText(finalVehiclesCount + "/0");
                binding.syncPosCounter.setText(finalPosCount + "/0");
                binding.syncFuelSaleCounter.setText(finalFuelCount + "/0");
                binding.syncAddMoveCounter.setText(finalMoveCount + "/0");
                binding.syncAddLoadCounter.setText(finalRefundCount + "/0");
                binding.syncAddDiscountCounter.setText(finalDiscountCount + "/0");

                binding.syncPhoneUpdatesLayout.setVisibility(finalPhoneCount == 0 ? View.GONE : View.VISIBLE);
                binding.syncCustomerVehiclesLayout.setVisibility(finalVehiclesCount == 0 ? View.GONE : View.VISIBLE);
                binding.syncPosLayout.setVisibility(finalPosCount == 0 ? View.GONE : View.VISIBLE);
                binding.syncFuelSaleLayout.setVisibility(finalFuelCount == 0 ? View.GONE : View.VISIBLE);
                binding.syncAddMoveLayout.setVisibility(finalMoveCount == 0 ? View.GONE : View.VISIBLE);
                binding.syncAddLoadLayout.setVisibility(finalRefundCount == 0 ? View.GONE : View.VISIBLE);
                binding.syncAddDiscountLayout.setVisibility(finalDiscountCount == 0 ? View.GONE : View.VISIBLE);

                binding.noDataToSync.setVisibility(noData ? View.VISIBLE : View.GONE);
                binding.syncAll.setVisibility(noData ? View.GONE : View.VISIBLE);
                binding.viewJsonAll.setVisibility(noData || !showJson ? View.GONE : View.VISIBLE);
            });
        });
    }

    // =============================
    // SYNC ALL (each type has its own sync_id)
    // =============================
    private void syncAll() {
        executor.execute(() -> {
            // prevent parallel massive runs
            if (syncingVehicles || syncingPos || syncingFuelSale || syncingPhoneUpdates || syncingMove || syncingRefund || syncingDiscount) {
                main.post(() -> toast(R.string.sync_in_progress));
                return;
            }

            // sequential per type (each creates its own sync_id)
            if (offlineCustomerPhoneUpdateDao.countPending() > 0) {
                syncPhoneUpdatesInternal();
            }
            if (offlineCustomerVehicleDao.countPending() + offlineCustomerVehicleEditDao.countPending() > 0) {
                syncCustomerVehiclesInternal();
            }
            if (offlineInvoiceDao.countPendingByType(TYPE_POS) > 0) {
                syncPosInternal();
            }
            if (offlineInvoiceDao.countPendingByType(TYPE_FUEL_SALE) > 0) {
                syncFuelSaleInternal();
            }
            if (offlineFinancialTransactionDao.countPendingByActionType(ACTION_MOVE) > 0) {
                syncFinancialByActionInternal(ACTION_MOVE);
            }
            if (offlineFinancialTransactionDao.countPendingByActionType(ACTION_REFUND) > 0) {
                syncFinancialByActionInternal(ACTION_REFUND);
            }
            if (offlineFinancialTransactionDao.countPendingByActionType(ACTION_DISCOUNT) > 0) {
                syncFinancialByActionInternal(ACTION_DISCOUNT);
            }

            main.post(this::refreshCounters);
        });
    }

    // =============================
    // PHONE UPDATES
    // =============================
    private void syncPhoneUpdates() {
        executor.execute(this::syncPhoneUpdatesInternal);
    }

    private void syncPhoneUpdatesInternal() {
        if (syncingPhoneUpdates) {
            main.post(() -> toast(R.string.sync_in_progress));
            return;
        }

        int total = offlineCustomerPhoneUpdateDao.countPending();
        if (total <= 0) {
            main.post(() -> toast(R.string.all_is_updated));
            return;
        }

        syncingPhoneUpdates = true;
        setLoadingPhone(true, false);

        startSynchronization(new SyncIdCallback() {
            @Override public void onSuccess(String syncId) {
                executePhoneUpdatesBatches(syncId, total);
            }
            @Override public void onFailed(String msg) {
                syncingPhoneUpdates = false;
                main.post(() -> {
                    setLoadingPhone(false, true);
                    if (msg != null) toast(msg); else toast(R.string.failed_to_start_sync);
                });
            }
        });
    }

    private void executePhoneUpdatesBatches(String syncId, int total) {
        executor.execute(() -> {
            int sent = 0;
            boolean allSuccess = true;

            while (true) {
                List<OfflineCustomerPhoneUpdateEntity> batch = offlineCustomerPhoneUpdateDao.getPending(PAGE_SIZE);
                if (batch == null || batch.isEmpty()) break;

                JSONArray data = buildJsonArrayFromRequestJson(batch, e -> e.requestJson);
                if (data == null) {
                    allSuccess = false;
                    markPhoneFailed(batch, "Invalid JSON");
                    break;
                }

                boolean ok = postJsonBatch(AppConfig.updateCustomerPhones, syncId, data);
                if (!ok) {
                    allSuccess = false;
                    markPhoneFailed(batch, "Request failed");
                    break;
                }

                markPhoneSent(batch);
                sent += batch.size();
                int finalSent = sent;
                main.post(() -> binding.syncPhoneUpdatesCounter.setText(total + " - " + finalSent));
            }

            endSynchronization(syncId);

            boolean finalAllSuccess = allSuccess;
            syncingPhoneUpdates = false;
            main.post(() -> {
                setLoadingPhone(false, true);
                setIconResult(binding.icSyncPhoneUpdates, finalAllSuccess);
                refreshCounters();
            });
        });
    }

    private void ui(Runnable r) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            r.run();
        } else {
            main.post(r);
        }
    }
    private void setLoadingPhone(boolean loading, boolean showIcon) {
        ui(() -> {
            binding.loaderSyncPhoneUpdates.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.icSyncPhoneUpdates.setVisibility(showIcon ? View.VISIBLE : View.GONE);
            if (loading) binding.icSyncPhoneUpdates.setVisibility(View.GONE);
        });
    }

    private void markPhoneSent(List<OfflineCustomerPhoneUpdateEntity> list) {
        List<Long> ids = new ArrayList<>();
        for (OfflineCustomerPhoneUpdateEntity e : list) ids.add(e.localId);
        offlineCustomerPhoneUpdateDao.markStatusByIds(ids, 1, null, System.currentTimeMillis());
    }

    private void markPhoneFailed(List<OfflineCustomerPhoneUpdateEntity> list, String err) {
        List<Long> ids = new ArrayList<>();
        for (OfflineCustomerPhoneUpdateEntity e : list) ids.add(e.localId);
        offlineCustomerPhoneUpdateDao.markStatusByIds(ids, 2, err, System.currentTimeMillis());
    }

    // =============================
    // CUSTOMER VEHICLES (ADD + EDIT) same endpoint & same sync_id
    // =============================
    private void syncCustomerVehicles() {
        executor.execute(this::syncCustomerVehiclesInternal);
    }

    private void syncCustomerVehiclesInternal() {
        if (syncingVehicles) {
            ui(() -> toast(R.string.sync_in_progress));
            return;
        }

        int total = offlineCustomerVehicleDao.countPending() + offlineCustomerVehicleEditDao.countPending();
        if (total <= 0) {
            ui(() -> toast(R.string.all_is_updated));
            return;
        }

        syncingVehicles = true;
        setLoadingVehicles(true, false);

        startSynchronization(new SyncIdCallback() {
            @Override public void onSuccess(String syncId) {
                executeVehiclesBatches(syncId, total);
            }
            @Override public void onFailed(String msg) {
                syncingVehicles = false;
                ui(() -> {
                    setLoadingVehicles(false, true);
                    if (msg != null) toast(msg); else toast(R.string.failed_to_start_sync);
                });
            }
        });
    }

    private void executeVehiclesBatches(String syncId, int total) {
        executor.execute(() -> {
            int sent = 0;
            boolean allSuccess = true;

            // 1) ADD vehicles
            while (true) {
                List<OfflineCustomerVehicleEntity> batch = offlineCustomerVehicleDao.getPending(PAGE_SIZE);
                if (batch == null || batch.isEmpty()) break;

                JSONArray data = buildJsonArrayFromRequestJson(batch, e -> e.requestJson);
                if (data == null) {
                    allSuccess = false;
                    markVehiclesAddFailed(batch, "Invalid JSON");
                    break;
                }

                boolean ok = postJsonBatch(AppConfig.addCustomerVehiclesJson, syncId, data);
                if (!ok) {
                    allSuccess = false;
                    markVehiclesAddFailed(batch, "Request failed");
                    break;
                }

                markVehiclesAddSent(batch);
                sent += batch.size();
                int finalSent = sent;
                ui(() -> binding.syncCustomerVehiclesCounter.setText(total + " - " + finalSent));
            }

            // 2) EDIT vehicles
            if (allSuccess) {
                while (true) {
                    List<OfflineCustomerVehicleEditEntity> batch = offlineCustomerVehicleEditDao.getPending(PAGE_SIZE);
                    if (batch == null || batch.isEmpty()) break;

                    JSONArray data = buildJsonArrayFromRequestJson(batch, e -> toRequestJsonFromEdit(e));
                    if (data == null) {
                        allSuccess = false;
                        markVehiclesEditFailed(batch, "Invalid JSON");
                        break;
                    }

                    boolean ok = postJsonBatch(AppConfig.addCustomerVehiclesJson, syncId, data);
                    if (!ok) {
                        allSuccess = false;
                        markVehiclesEditFailed(batch, "Request failed");
                        break;
                    }

                    markVehiclesEditSent(batch);
                    sent += batch.size();
                    int finalSent = sent;
                    ui(() -> binding.syncCustomerVehiclesCounter.setText(total + " - " + finalSent));
                }
            }

            endSynchronization(syncId);

            boolean finalAllSuccess = allSuccess;
            syncingVehicles = false;
            ui(() -> {
                setLoadingVehicles(false, true);
                setIconResult(binding.icSyncCustomerVehicles, finalAllSuccess);
                refreshCounters();
            });
        });
    }

    private void setLoadingVehicles(boolean loading, boolean showIcon) {
        ui(() -> {
            binding.loaderSyncCustomerVehicles.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.icSyncCustomerVehicles.setVisibility(showIcon ? View.VISIBLE : View.GONE);
            if (loading) binding.icSyncCustomerVehicles.setVisibility(View.GONE);
        });
    }

    private void markVehiclesAddSent(List<OfflineCustomerVehicleEntity> list) {
        List<Long> ids = new ArrayList<>();
        for (OfflineCustomerVehicleEntity e : list) ids.add(e.localId);
        offlineCustomerVehicleDao.markStatusByIds(ids, 1, null, System.currentTimeMillis());
    }

    private void markVehiclesAddFailed(List<OfflineCustomerVehicleEntity> list, String err) {
        List<Long> ids = new ArrayList<>();
        for (OfflineCustomerVehicleEntity e : list) ids.add(e.localId);
        offlineCustomerVehicleDao.markStatusByIds(ids, 2, err, System.currentTimeMillis());
    }

    private void markVehiclesEditSent(List<OfflineCustomerVehicleEditEntity> list) {
        List<Long> ids = new ArrayList<>();
        for (OfflineCustomerVehicleEditEntity e : list) ids.add(e.localId);
        offlineCustomerVehicleEditDao.markStatusByIds(ids, 1, null, System.currentTimeMillis());
    }

    private void markVehiclesEditFailed(List<OfflineCustomerVehicleEditEntity> list, String err) {
        List<Long> ids = new ArrayList<>();
        for (OfflineCustomerVehicleEditEntity e : list) ids.add(e.localId);
        offlineCustomerVehicleEditDao.markStatusByIds(ids, 2, err, System.currentTimeMillis());
    }

    // If edits don't have requestJson stored, build it from fields.
    // If you already store requestJson for edits, just return that instead.
    private String toRequestJsonFromEdit(OfflineCustomerVehicleEditEntity e) {
        try {
            JSONObject o = new JSONObject();

            // Scope
            if (e.customerId > 0) o.put("customer_id", e.customerId);
            if (e.offlineCustomerLocalId > 0) o.put("offline_customer_local_id", e.offlineCustomerLocalId);

            // Target
            if (e.targetOnlineVehicleId != null && e.targetOnlineVehicleId > 0) {
                o.put("vehicle_id", e.targetOnlineVehicleId);
            }
            if (e.targetLocalVehicleId != null && e.targetLocalVehicleId > 0) {
                o.put("vehicle_local_id", e.targetLocalVehicleId);
            }

            // Fields
            if (!TextUtils.isEmpty(e.vehicleNumber)) o.put("vehicle_number", e.vehicleNumber);
            if (e.vehicleType != null) o.put("vehicle_type", e.vehicleType);
            if (e.vehicleColor != null) o.put("vehicle_color", e.vehicleColor);
            if (!TextUtils.isEmpty(e.model)) o.put("model", e.model);
            if (!TextUtils.isEmpty(e.licenseExpiryDate)) o.put("license_expiry_date", e.licenseExpiryDate);
            if (!TextUtils.isEmpty(e.notes)) o.put("notes", e.notes);

            return o.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    // =============================
    // POS invoices
    // =============================
    private void syncPos() {
        executor.execute(this::syncPosInternal);
    }

    private void syncPosInternal() {
        if (syncingPos) {
            ui(() -> toast(R.string.sync_in_progress));
            return;
        }

        int total = offlineInvoiceDao.countPendingByType(TYPE_POS);
        if (total <= 0) {
            ui(() -> toast(R.string.all_is_updated));
            return;
        }

        syncingPos = true;
        setLoadingPos(true, false);

        startSynchronization(new SyncIdCallback() {
            @Override public void onSuccess(String syncId) {
                executeInvoiceBatches(TYPE_POS, AppConfig.addPosJson, syncId, total);
            }
            @Override public void onFailed(String msg) {
                syncingPos = false;
                ui(() -> {
                    setLoadingPos(false, true);
                    if (msg != null) toast(msg); else toast(R.string.failed_to_start_sync);
                });
            }
        });
    }

    private void setLoadingPos(boolean loading, boolean showIcon) {
        ui(() -> {
            binding.loaderSyncPos.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.icSyncPos.setVisibility(showIcon ? View.VISIBLE : View.GONE);
            if (loading) binding.icSyncPos.setVisibility(View.GONE);
        });
    }

    // =============================
    // Fuel Sale invoices
    // =============================
    private void syncFuelSale() {
        executor.execute(this::syncFuelSaleInternal);
    }

    private void syncFuelSaleInternal() {
        if (syncingFuelSale) {
            ui(() -> toast(R.string.sync_in_progress));
            return;
        }

        int total = offlineInvoiceDao.countPendingByType(TYPE_FUEL_SALE);
        if (total <= 0) {
            ui(() -> toast(R.string.all_is_updated));
            return;
        }

        syncingFuelSale = true;
        setLoadingFuel(true, false);

        startSynchronization(new SyncIdCallback() {
            @Override public void onSuccess(String syncId) {
                executeInvoiceBatches(TYPE_FUEL_SALE, AppConfig.addFuelPriceJson, syncId, total);
            }
            @Override public void onFailed(String msg) {
                syncingFuelSale = false;
                ui(() -> {
                    setLoadingFuel(false, true);
                    if (msg != null) toast(msg); else toast(R.string.failed_to_start_sync);
                });
            }
        });
    }

    private void setLoadingFuel(boolean loading, boolean showIcon) {
        ui(() -> {
            binding.loaderSyncFuelSale.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.icSyncFuelSale.setVisibility(showIcon ? View.VISIBLE : View.GONE);
            if (loading) binding.icSyncFuelSale.setVisibility(View.GONE);
        });
    }

    private void executeInvoiceBatches(int type, String endpoint, String syncId, int total) {
        executor.execute(() -> {
            int sent = 0;
            boolean allSuccess = true;

            while (true) {
                List<OfflineInvoiceEntity> batch = offlineInvoiceDao.getPendingByTypePendingOnly(type, PAGE_SIZE);
                if (batch == null || batch.isEmpty()) break;

                JSONArray data = buildJsonArrayFromRequestJson(batch, e -> e.requestJson);
                if (data == null) {
                    allSuccess = false;
                    markInvoicesFailed(batch, "Invalid JSON");
                    break;
                }

                boolean ok = postJsonBatch(endpoint, syncId, data);
                if (!ok) {
                    allSuccess = false;
                    markInvoicesFailed(batch, "Request failed");
                    break;
                }

                markInvoicesSent(batch);
                sent += batch.size();
                int finalSent = sent;

                if (type == TYPE_POS) {
                    ui(() -> binding.syncPosCounter.setText(total + " - " + finalSent));
                } else {
                    ui(() -> binding.syncFuelSaleCounter.setText(total + " - " + finalSent));
                }
            }

            endSynchronization(syncId);

            boolean finalAllSuccess = allSuccess;
            if (type == TYPE_POS) syncingPos = false; else syncingFuelSale = false;

            ui(() -> {
                if (type == TYPE_POS) {
                    setLoadingPos(false, true);
                    setIconResult(binding.icSyncPos, finalAllSuccess);
                } else {
                    setLoadingFuel(false, true);
                    setIconResult(binding.icSyncFuelSale, finalAllSuccess);
                }
                refreshCounters();
            });
        });
    }

    private void markInvoicesSent(List<OfflineInvoiceEntity> list) {
        List<Long> ids = new ArrayList<>();
        for (OfflineInvoiceEntity e : list) ids.add(e.localId);
        offlineInvoiceDao.markStatusByIds(ids, 1, null, System.currentTimeMillis());
    }

    private void markInvoicesFailed(List<OfflineInvoiceEntity> list, String err) {
        List<Long> ids = new ArrayList<>();
        for (OfflineInvoiceEntity e : list) ids.add(e.localId);
        offlineInvoiceDao.markStatusByIds(ids, 2, err, System.currentTimeMillis());
    }

    // =============================
    // FINANCIAL (move/refund/discount) => same endpoint add-moves-json
    // =============================
    private void syncFinancialByAction(int actionType) {
        executor.execute(() -> syncFinancialByActionInternal(actionType));
    }

    private void syncFinancialByActionInternal(int actionType) {
        if (isSyncingFinancial(actionType)) {
            ui(() -> toast(R.string.sync_in_progress));
            return;
        }

        int total = offlineFinancialTransactionDao.countPendingByActionType(actionType);
        if (total <= 0) {
            ui(() -> toast(R.string.all_is_updated));
            return;
        }

        setSyncingFinancial(actionType, true);
        setLoadingFinancial(actionType, true, false);

        startSynchronization(new SyncIdCallback() {
            @Override public void onSuccess(String syncId) {
                executeFinancialBatches(actionType, syncId, total);
            }
            @Override public void onFailed(String msg) {
                setSyncingFinancial(actionType, false);
                ui(() -> {
                    setLoadingFinancial(actionType, false, true);
                    if (msg != null) toast(msg); else toast(R.string.failed_to_start_sync);
                });
            }
        });
    }

    private void executeFinancialBatches(int actionType, String syncId, int total) {
        executor.execute(() -> {
            int sent = 0;
            boolean allSuccess = true;

            while (true) {
                List<OfflineFinancialTransactionEntity> batch = offlineFinancialTransactionDao.getPendingByActionType(actionType, PAGE_SIZE);
                if (batch == null || batch.isEmpty()) break;

                JSONArray data = buildJsonArrayFromRequestJson(batch, e -> e.requestJson);
                if (data == null) {
                    allSuccess = false;
                    markFinancialFailed(batch, "Invalid JSON");
                    break;
                }

                boolean ok = postJsonBatch(AppConfig.updateMoveTransactions, syncId, data);
                if (!ok) {
                    allSuccess = false;
                    markFinancialFailed(batch, "Request failed");
                    break;
                }

                markFinancialSent(batch);
                sent += batch.size();
                int finalSent = sent;

                ui(() -> getCounterViewByAction(actionType).setText(total + " - " + finalSent));
            }

            endSynchronization(syncId);

            boolean finalAllSuccess = allSuccess;
            setSyncingFinancial(actionType, false);

            ui(() -> {
                setLoadingFinancial(actionType, false, true);
                setIconResult(getIconViewByAction(actionType), finalAllSuccess);
                refreshCounters();
            });
        });
    }

    private void markFinancialSent(List<OfflineFinancialTransactionEntity> list) {
        List<Long> ids = new ArrayList<>();
        for (OfflineFinancialTransactionEntity e : list) ids.add(e.localId);
        offlineFinancialTransactionDao.markStatusByIds(ids, 1, null, System.currentTimeMillis());
    }

    private void markFinancialFailed(List<OfflineFinancialTransactionEntity> list, String err) {
        List<Long> ids = new ArrayList<>();
        for (OfflineFinancialTransactionEntity e : list) ids.add(e.localId);
        offlineFinancialTransactionDao.markStatusByIds(ids, 2, err, System.currentTimeMillis());
    }

    private boolean isSyncingFinancial(int actionType) {
        if (actionType == ACTION_MOVE) return syncingMove;
        if (actionType == ACTION_REFUND) return syncingRefund;
        return syncingDiscount;
    }

    private void setSyncingFinancial(int actionType, boolean v) {
        if (actionType == ACTION_MOVE) syncingMove = v;
        else if (actionType == ACTION_REFUND) syncingRefund = v;
        else syncingDiscount = v;
    }

    private void setLoadingFinancial(int actionType, boolean loading, boolean showIcon) {
        ui(() -> {
            if (actionType == ACTION_MOVE) {
                binding.loaderSyncAddMove.setVisibility(loading ? View.VISIBLE : View.GONE);
                binding.icSyncAddMove.setVisibility(showIcon ? View.VISIBLE : View.GONE);
                if (loading) binding.icSyncAddMove.setVisibility(View.GONE);
            } else if (actionType == ACTION_REFUND) {
                binding.loaderSyncAddLoad.setVisibility(loading ? View.VISIBLE : View.GONE);
                binding.icSyncAddLoad.setVisibility(showIcon ? View.VISIBLE : View.GONE);
                if (loading) binding.icSyncAddLoad.setVisibility(View.GONE);
            } else {
                binding.loaderSyncAddDiscount.setVisibility(loading ? View.VISIBLE : View.GONE);
                binding.icSyncAddDiscount.setVisibility(showIcon ? View.VISIBLE : View.GONE);
                if (loading) binding.icSyncAddDiscount.setVisibility(View.GONE);
            }
        });
    }

    private View getIconViewByAction(int actionType) {
        if (actionType == ACTION_MOVE) return binding.icSyncAddMove;
        if (actionType == ACTION_REFUND) return binding.icSyncAddLoad;
        return binding.icSyncAddDiscount;
    }

    private androidx.appcompat.widget.AppCompatTextView getCounterViewByAction(int actionType) {
        if (actionType == ACTION_MOVE) return binding.syncAddMoveCounter;
        if (actionType == ACTION_REFUND) return binding.syncAddLoadCounter;
        return binding.syncAddDiscountCounter;
    }

    // =============================
    // START/END SYNCHRONIZATION (server returns sync_id)
    // =============================
    private interface SyncIdCallback {
        void onSuccess(String syncId);
        void onFailed(String msg);
    }

    private void startSynchronization(final SyncIdCallback callback) {
        ArrayList<String> headerKeys = new ArrayList<>();
        ArrayList<String> headerValues = new ArrayList<>();
        headerKeys.add("Authorization");
        headerValues.add(getSessionManager().getString(getSessionKeys().token));

        try {
            new RequestAsyncTask(
                    0,
                    getApplicationContext(),
                    AppConfig.start_synchronization,
                    Constant.REQUEST_POST,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    headerKeys,
                    headerValues,
                    new AsyncResponse() {
                        @Override
                        public void processFinish(ResponseObject responseObject) {
                            try {
                                if (responseObject.getResponseCode() == 200) {
                                    JSONObject jsonObject = new JSONObject(responseObject.getResponseText());
                                    if (jsonObject.optBoolean("status")) {
                                        JSONObject data = jsonObject.getJSONObject("data");
                                        String syncId = data.getString("id");
                                        callback.onSuccess(syncId);
                                    } else {
                                        callback.onFailed(jsonObject.optString("message", null));
                                    }
                                } else if (responseObject.getResponseCode() == 401) {
                                    ui(() -> logout());
                                    callback.onFailed("Unauthorized");
                                } else {
                                    callback.onFailed(null);
                                }
                            } catch (Exception e) {
                                callback.onFailed(null);
                            }
                        }

                        @Override
                        public void processerror(String output) {
                            callback.onFailed(output);
                        }
                    }
            ).execute();
        } catch (UnsupportedEncodingException e) {
            callback.onFailed(e.getMessage());
        }
    }

    private void endSynchronization(String syncId) {
        if (TextUtils.isEmpty(syncId)) return;

        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        ArrayList<String> headerKeys = new ArrayList<>();
        ArrayList<String> headerValues = new ArrayList<>();

        keys.add("sync_id");
        values.add(syncId);

        headerKeys.add("Authorization");
        headerValues.add(getSessionManager().getString(getSessionKeys().token));

        try {
            new RequestAsyncTask(
                    0,
                    getApplicationContext(),
                    AppConfig.end_synchronization,
                    Constant.REQUEST_POST,
                    keys,
                    values,
                    headerKeys,
                    headerValues,
                    new AsyncResponse() {
                        @Override public void processFinish(ResponseObject responseObject) { }
                        @Override public void processerror(String output) { }
                    }
            ).execute();
        } catch (UnsupportedEncodingException ignored) { }
    }

    // =============================
    // HTTP helper: post {data: [..], sync_id: X}
    // =============================
    private boolean postJsonBatch(String endpoint, String syncId, JSONArray dataArray) {
        final Object lock = new Object();
        final boolean[] ok = {false};

        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> headerKeys = new ArrayList<>();
        ArrayList<String> headerValues = new ArrayList<>();

        keys.add("data");
        values.add(dataArray.toString());

        keys.add("sync_id");
        values.add(syncId);

        headerKeys.add("Authorization");
        headerValues.add(getSessionManager().getString(getSessionKeys().token));

        try {
            new RequestAsyncTask(
                    0,
                    getApplicationContext(),
                    endpoint,
                    Constant.REQUEST_POST,
                    keys,
                    values,
                    headerKeys,
                    headerValues,
                    new AsyncResponse() {
                        @Override
                        public void processFinish(ResponseObject responseObject) {
                            try {
                                if (responseObject.getResponseCode() == 200) {
                                    JSONObject jsonObject = new JSONObject(responseObject.getResponseText());
                                    ok[0] = jsonObject.optBoolean("status", false);
                                } else if (responseObject.getResponseCode() == 401) {
                                    ui(() -> logout());
                                    ok[0] = false;
                                }
                            } catch (Exception ignored) {
                                ok[0] = false;
                            }
                            synchronized (lock) { lock.notifyAll(); }
                        }

                        @Override
                        public void processerror(String output) {
                            ok[0] = false;
                            synchronized (lock) { lock.notifyAll(); }
                        }
                    }
            ).execute();

            synchronized (lock) {
                lock.wait(120000); // 120s timeout safety
            }
        } catch (Exception e) {
            return false;
        }

        return ok[0];
    }

    // =============================
    // JSON building helpers
    // =============================
    private interface JsonExtractor<T> {
        String extract(T item);
    }

    private <T> JSONArray buildJsonArrayFromRequestJson(List<T> list, JsonExtractor<T> extractor) {
        try {
            JSONArray arr = new JSONArray();
            for (T item : list) {
                String raw = extractor.extract(item);
                if (TextUtils.isEmpty(raw)) continue;

                // raw should be a JSON object
                JSONObject obj = new JSONObject(raw);
                arr.put(obj);
            }
            return arr;
        } catch (Exception e) {
            return null;
        }
    }

    private void setIconResult(View iconView, boolean success) {
        ui(() -> {
            try {
                if (success) iconView.setBackgroundResource(R.drawable.ic_true);
                else iconView.setBackgroundResource(R.drawable.ic_false);
            } catch (Exception ignored) {}
        });
    }

    // =============================
    // VIEW JSON (simple dialog)
    // =============================
    private void showJsonDialog(String title, String json) {
//        if (json == null) json = "[]";
//
//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setTitle(title)
//                .setMessage(json)
//                .setPositiveButton(android.R.string.ok, (d, w) -> d.dismiss())
//                .create();
//        dialog.show();

        try {
            ViewJsonDialog dialog = new ViewJsonDialog(this, json);
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), "ViewJsonDialog");
        } catch (Exception e) {
            toast(R.string.general_error);
        }
    }

    private void viewJsonAll() {
        executor.execute(() -> {
            JSONArray all = new JSONArray();

            // phone
            List<OfflineCustomerPhoneUpdateEntity> phones = offlineCustomerPhoneUpdateDao.getPending(1000);
            JSONArray phonesArr = buildJsonArrayFromRequestJson(phones, e -> e.requestJson);
            if (phonesArr != null) for (int i = 0; i < phonesArr.length(); i++) all.put(phonesArr.opt(i));

            // vehicles add
            List<OfflineCustomerVehicleEntity> vAdd = offlineCustomerVehicleDao.getPending(1000);
            JSONArray vAddArr = buildJsonArrayFromRequestJson(vAdd, e -> e.requestJson);
            if (vAddArr != null) for (int i = 0; i < vAddArr.length(); i++) all.put(vAddArr.opt(i));

            // vehicles edits
            List<OfflineCustomerVehicleEditEntity> vEdit = offlineCustomerVehicleEditDao.getPending(1000);
            JSONArray vEditArr = buildJsonArrayFromRequestJson(vEdit, e -> toRequestJsonFromEdit(e));
            if (vEditArr != null) for (int i = 0; i < vEditArr.length(); i++) all.put(vEditArr.opt(i));

            // POS + Fuel
            List<OfflineInvoiceEntity> pos = offlineInvoiceDao.getPendingByTypePendingOnly(TYPE_POS, 1000);
            JSONArray posArr = buildJsonArrayFromRequestJson(pos, e -> e.requestJson);
            if (posArr != null) for (int i = 0; i < posArr.length(); i++) all.put(posArr.opt(i));

            List<OfflineInvoiceEntity> fuel = offlineInvoiceDao.getPendingByTypePendingOnly(TYPE_FUEL_SALE, 1000);
            JSONArray fuelArr = buildJsonArrayFromRequestJson(fuel, e -> e.requestJson);
            if (fuelArr != null) for (int i = 0; i < fuelArr.length(); i++) all.put(fuelArr.opt(i));

            // financial all 3 types
            List<OfflineFinancialTransactionEntity> move = offlineFinancialTransactionDao.getPendingByActionType(ACTION_MOVE, 1000);
            JSONArray moveArr = buildJsonArrayFromRequestJson(move, e -> e.requestJson);
            if (moveArr != null) for (int i = 0; i < moveArr.length(); i++) all.put(moveArr.opt(i));

            List<OfflineFinancialTransactionEntity> refund = offlineFinancialTransactionDao.getPendingByActionType(ACTION_REFUND, 1000);
            JSONArray refundArr = buildJsonArrayFromRequestJson(refund, e -> e.requestJson);
            if (refundArr != null) for (int i = 0; i < refundArr.length(); i++) all.put(refundArr.opt(i));

            List<OfflineFinancialTransactionEntity> disc = offlineFinancialTransactionDao.getPendingByActionType(ACTION_DISCOUNT, 1000);
            JSONArray discArr = buildJsonArrayFromRequestJson(disc, e -> e.requestJson);
            if (discArr != null) for (int i = 0; i < discArr.length(); i++) all.put(discArr.opt(i));

            String out = all.toString();

            ui(() -> showJsonDialog(getString(R.string.view_json_all), out));
        });
    }

    private void viewJsonPhoneUpdates() {
        executor.execute(() -> {
            List<OfflineCustomerPhoneUpdateEntity> list = offlineCustomerPhoneUpdateDao.getPending(1000);
            JSONArray arr = buildJsonArrayFromRequestJson(list, e -> e.requestJson);
            ui(() -> showJsonDialog(getString(R.string.sync_phone_updates), arr != null ? arr.toString() : "[]"));
        });
    }

    private void viewJsonCustomerVehicles() {
        executor.execute(() -> {
            JSONArray all = new JSONArray();

            List<OfflineCustomerVehicleEntity> add = offlineCustomerVehicleDao.getPending(1000);
            JSONArray a = buildJsonArrayFromRequestJson(add, e -> e.requestJson);
            if (a != null) for (int i = 0; i < a.length(); i++) all.put(a.opt(i));

            List<OfflineCustomerVehicleEditEntity> edits = offlineCustomerVehicleEditDao.getPending(1000);
            JSONArray eArr = buildJsonArrayFromRequestJson(edits, e -> toRequestJsonFromEdit(e));
            if (eArr != null) for (int i = 0; i < eArr.length(); i++) all.put(eArr.opt(i));

            ui(() -> showJsonDialog(getString(R.string.sync_customer_vehicles), all.toString()));
        });
    }

    private void viewJsonPos() {
        executor.execute(() -> {
            List<OfflineInvoiceEntity> list = offlineInvoiceDao.getPendingByTypePendingOnly(TYPE_POS, 1000);
            JSONArray arr = buildJsonArrayFromRequestJson(list, e -> e.requestJson);
            ui(() -> showJsonDialog(getString(R.string.sync_pos), arr != null ? arr.toString() : "[]"));
        });
    }

    private void viewJsonFuelSale() {
        executor.execute(() -> {
            List<OfflineInvoiceEntity> list = offlineInvoiceDao.getPendingByTypePendingOnly(TYPE_FUEL_SALE, 1000);
            JSONArray arr = buildJsonArrayFromRequestJson(list, e -> e.requestJson);
            ui(() -> showJsonDialog(getString(R.string.sync_fuel_sale), arr != null ? arr.toString() : "[]"));
        });
    }

    private void viewJsonFinancialByAction(int actionType) {
        executor.execute(() -> {
            List<OfflineFinancialTransactionEntity> list = offlineFinancialTransactionDao.getPendingByActionType(actionType, 1000);
            JSONArray arr = buildJsonArrayFromRequestJson(list, e -> e.requestJson);
            String title = actionType == ACTION_MOVE ? getString(R.string.sync_add_move)
                    : actionType == ACTION_REFUND ? getString(R.string.sync_add_load)
                    : getString(R.string.sync_add_discount);
            ui(() -> showJsonDialog(title, arr != null ? arr.toString() : "[]"));
        });
    }
}
