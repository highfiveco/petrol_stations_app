package co.highfive.petrolstation.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.SelectSearchCustomerAdapter;
import co.highfive.petrolstation.databinding.DialogSelectCustomerBinding;
import co.highfive.petrolstation.fuelsale.dto.FuelCustomerDto;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ApiError;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.highfive.petrolstation.data.local.AppDatabase;
import co.highfive.petrolstation.data.local.DatabaseProvider;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import android.os.Handler;
import android.os.Looper;
public class SelectCustomerDialog extends DialogFragment {

    public interface Listener {
        void onSelected(@NonNull FuelCustomerDto customer);
    }

    private DialogSelectCustomerBinding binding;
    private BaseActivity baseActivity;
    private Listener listener;

    // persist state
    private String lastSearch = "";
    private ArrayList<FuelCustomerDto> lastResults = new ArrayList<>();
    private int selectedCustomerId = 0;

    private SelectSearchCustomerAdapter adapter;
    private AppDatabase db;
    private ExecutorService dbExecutor;
    private Handler mainHandler;

    public SelectCustomerDialog() {}

    public static SelectCustomerDialog newInstance(String lastSearch,
                                                   ArrayList<FuelCustomerDto> lastResults,
                                                   int selectedCustomerId) {
        SelectCustomerDialog d = new SelectCustomerDialog();
        d.lastSearch = lastSearch != null ? lastSearch : "";
        d.lastResults = lastResults != null ? lastResults : new ArrayList<>();
        d.selectedCustomerId = selectedCustomerId;
        return d;
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            baseActivity = (BaseActivity) context;

            // ✅ Room + threading
            db = DatabaseProvider.get(context);
            dbExecutor = Executors.newSingleThreadExecutor();
            mainHandler = new Handler(Looper.getMainLooper());
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DialogSelectCustomerBinding.inflate(inflater, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initRecycler();
        initClicks();
        restoreStateUi();

        return binding.getRoot();
    }

    private void initRecycler() {
        adapter = new SelectSearchCustomerAdapter(c -> {
            selectedCustomerId = c.id; // single select
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        adapter.setItems(lastResults);
        adapter.setSelectedId(selectedCustomerId);
    }

    private void initClicks() {
        binding.close.setOnClickListener(v -> dismissAllowingStateLoss());
        binding.closeBtn.setOnClickListener(v -> dismissAllowingStateLoss());

        binding.btnSearch.setOnClickListener(v -> doSearch());

        binding.confirm.setOnClickListener(v -> {
            FuelCustomerDto sel = adapter.getSelected();
            if (sel == null) {
                toastLocal(getString(R.string.general_error));
                return;
            }
            if (listener != null) listener.onSelected(sel);
            dismissAllowingStateLoss();
        });

        binding.etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    baseActivity.hideSoftKeyboard();
                    doSearch();
                    return true;
                }
                return false;
            }
        });
    }

    private void restoreStateUi() {
        binding.etSearch.setText(lastSearch);
        binding.etSearch.setSelection(binding.etSearch.getText() != null ? binding.etSearch.getText().length() : 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (dbExecutor != null) {
            try { dbExecutor.shutdown(); } catch (Exception ignored) {}
        }
    }

    private void doSearch() {
        if (baseActivity == null) return;

        String q = safeTrim(binding.etSearch.getText());
        lastSearch = q;

        if (q.isEmpty()) {
            toastLocal("اكتب اسم للبحث");
            return;
        }

        // ✅ OFFLINE
        if (!baseActivity.connectionAvailable) {
            doSearchOffline(q);
            return;
        }

        // ✅ ONLINE (نفس كودك)
        baseActivity.showProgressHUD();

        Type type = new TypeToken<BaseResponse<List<FuelCustomerDto>>>(){}.getType();
        ApiClient.ApiParams params = new ApiClient.ApiParams().add("search", q);

        baseActivity.apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.FUEL_PRICE_CUSTOMER_SEARCH,
                params,
                null,
                type,
                0,
                new ApiCallback<List<FuelCustomerDto>>() {
                    @Override
                    public void onSuccess(List<FuelCustomerDto> data, String msg, String rawJson) {
                        baseActivity.hideProgressHUD();

                        lastResults.clear();
                        if (data != null) lastResults.addAll(data);

                        adapter.setItems(lastResults);
                        adapter.setSelectedId(selectedCustomerId);
                    }

                    @Override
                    public void onError(ApiError error) {
                        baseActivity.hideProgressHUD();
                        toastLocal(error != null ? error.message : getString(R.string.general_error));
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        baseActivity.hideProgressHUD();
                        baseActivity.logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        baseActivity.hideProgressHUD();
                        toastLocal(getString(R.string.no_internet));
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        baseActivity.hideProgressHUD();
                        toastLocal(getString(R.string.general_error));
                    }
                }
        );
    }


    public String getLastSearch() { return lastSearch; }
    public ArrayList<FuelCustomerDto> getLastResults() { return lastResults; }
    public int getSelectedCustomerId() { return selectedCustomerId; }

    private void toastLocal(String msg) {
        try { if (baseActivity != null) baseActivity.toast(msg); } catch (Exception ignored) {}
    }

    private static String safeTrim(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

    @NonNull
    private ArrayList<FuelCustomerDto> mapCustomersEntitiesToDto(@NonNull List<CustomerEntity> rows) {
        ArrayList<FuelCustomerDto> out = new ArrayList<>();
        for (CustomerEntity e : rows) {
            if (e == null) continue;

            FuelCustomerDto c = new FuelCustomerDto();
            c.id = e.id;
            c.name = e.name;
//            c.mobile = e.mobile;
            c.account_id = e.accountId; // مهم عشان الفيويل سيل
            out.add(c);
        }
        return out;
    }
    private void doSearchOffline(@NonNull String q) {
        if (db == null || dbExecutor == null || mainHandler == null) {
            toastLocal(getString(R.string.general_error));
            return;
        }

        baseActivity.showProgressHUD();

        dbExecutor.execute(() -> {
            List<CustomerEntity> mainRows = new ArrayList<>();
            List<co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity> offlineRows = new ArrayList<>();

            try {
                mainRows = db.customerDao().search(q);
            } catch (Exception e) {
                baseActivity.errorLogger("CustomerOfflineSearch_main", safeMsg(e));
            }

            try {
                // ✅ لازم تكون موجودة في OfflineCustomerDao
                offlineRows = db.offlineCustomerDao().search(q);
            } catch (Exception e) {
                baseActivity.errorLogger("CustomerOfflineSearch_offline", safeMsg(e));
            }

            ArrayList<FuelCustomerDto> mapped = new ArrayList<>();
            mapped.addAll(mapCustomersEntitiesToDto(mainRows));
            mapped.addAll(mapOfflineCustomersToDto(offlineRows)); // ✅ NEW

            mainHandler.post(() -> {
                baseActivity.hideProgressHUD();

                lastResults.clear();
                lastResults.addAll(mapped);

                adapter.setItems(lastResults);
                adapter.setSelectedId(selectedCustomerId);

                if (lastResults.isEmpty()) toastLocal("لا توجد نتائج (Offline)");
            });
        });
    }

    private String safeMsg(Exception e) {
        return (e == null || e.getMessage() == null) ? "null" : e.getMessage();
    }

    @NonNull
    private ArrayList<FuelCustomerDto> mapOfflineCustomersToDto(
            @NonNull List<co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity> rows
    ) {
        ArrayList<FuelCustomerDto> out = new ArrayList<>();
        for (co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity e : rows) {
            if (e == null) continue;

            FuelCustomerDto c = new FuelCustomerDto();

            long localId = e.localId;

            // ✅ Negative id للـ POS/DB key
            int fakeId = (localId > Integer.MAX_VALUE) ? -1 : (int) (-localId);
            c.id = fakeId;

            c.name = e.name;
            c.mobile = e.mobile; // إذا بدك
            c.account_id = 0;    // إذا ما عندك account للأوفلاين

            // ✅ flags
            c.is_offline = true;
            c.local_id = localId;

            out.add(c);
        }
        return out;
    }




}
