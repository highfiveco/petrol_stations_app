package co.highfive.petrolstation.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.highfive.petrolstation.R;
import co.highfive.petrolstation.adapters.NotificationAdapter;
import co.highfive.petrolstation.databinding.ActivityNotificationsBinding;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.hazemhamadaqa.activity.BaseActivity;
import co.highfive.petrolstation.models.Notification;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class NotificationsActivity extends BaseActivity {

    private ActivityNotificationsBinding binding;
    private NotificationAdapter adapter;
    private ApiClient apiClient;

    private int currentPage = 1;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private final ArrayList<Notification> notifications = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notifications);
        setupUI(binding.mainLayout);
        initApiClient();
        initViews();
        loadPage(1, true);
    }

    private void initApiClient() {
        apiClient = new ApiClient(
                getApplicationContext(),
                getGson(),
                new ApiClient.HeaderProvider() {
                    @Override public String getToken() {
                        return getSessionManager().getString(getSessionKeys().token);
                    }
                    @Override public String getLang() {
                        String lang = getSessionManager().getString(getSessionKeys().language_code);
                        return (lang == null || lang.trim().isEmpty()) ? "ar" : lang;
                    }
                    @Override public boolean isLoggedIn() {
                        return getSessionManager().getBoolean(getSessionKeys().isLogin);
                    }
                },
                () -> runOnUiThread(this::logout)
        );
    }

    private void initViews() {
        adapter = new NotificationAdapter(this);
        binding.recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerNotifications.setAdapter(adapter);

        binding.swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.blue);
        binding.swipeRefreshLayout.setOnRefreshListener(() -> loadPage(1, false));

        binding.icBack.setOnClickListener(v -> finish());
        binding.icHome.setOnClickListener(v ->
                moveToActivity(getApplicationContext(), MainActivity.class, null, false, true)
        );

        // Pagination listener
        binding.recyclerNotifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) return;
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visible = lm.getChildCount();
                int total = lm.getItemCount();
                int firstVisible = lm.findFirstVisibleItemPosition();

                if (!isLoading && hasMore && (visible + firstVisible) >= (total - 2)) {
                    loadPage(currentPage + 1, false);
                }
            }
        });
    }

    private void loadPage(int page, boolean showDialog) {
        if (isLoading) return;
        isLoading = true;

        if (showDialog) showProgressHUD();
        if (page > 1) adapter.setLoading(true);

        Map<String, String> params = ApiClient.mapOf(
                "page", String.valueOf(page)
        );

        Type type = new TypeToken<BaseResponse<List<Notification>>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.GETNOTIFICATIONS,
                params,
                null,
                type,
                0,
                new ApiCallback<List<Notification>>() {
                    @Override
                    public void onSuccess(List<Notification> data, String message, String rawJson) {
                        hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        adapter.setLoading(false);
                        isLoading = false;

                        if (page == 1) notifications.clear();
                        if (data != null && !data.isEmpty()) {
                            notifications.addAll(data);
                            adapter.setItems(notifications);
                            currentPage = page;
                            hasMore = data.size() >= pageSize;
                        } else {
                            hasMore = false;
                        }
                    }

                    @Override
                    public void onError(co.highfive.petrolstation.network.ApiError error) {
                        hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        adapter.setLoading(false);
                        isLoading = false;
                        toast(error.message);
                    }

                    @Override
                    public void onUnauthorized(String rawJson) {
                        hideProgressHUD();
                        logout();
                    }

                    @Override
                    public void onNetworkError(String reason) {
                        hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        adapter.setLoading(false);
                        isLoading = false;
                        toast(R.string.no_internet);
                    }

                    @Override
                    public void onParseError(String rawJson, Exception e) {
                        hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        adapter.setLoading(false);
                        isLoading = false;
                        toast(getString(R.string.general_error));
                    }
                }
        );
    }
}
