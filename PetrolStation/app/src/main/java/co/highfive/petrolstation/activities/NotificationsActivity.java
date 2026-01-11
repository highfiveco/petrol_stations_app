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

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private final ArrayList<Notification> notifications = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notifications);
        setupUI(binding.mainLayout);
        initViews();
        loadPage(1, true);
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

        Type type = new TypeToken<BaseResponse<co.highfive.petrolstation.notifications.dto.NotificationsResponseDto>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.GETNOTIFICATIONS,
                params,
                null,
                type,
                0,
                new ApiCallback<co.highfive.petrolstation.notifications.dto.NotificationsResponseDto>() {
                    @Override
                    public void onSuccess(co.highfive.petrolstation.notifications.dto.NotificationsResponseDto dto,
                                          String message,
                                          String rawJson) {

                        hideProgressHUD();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        adapter.setLoading(false);
                        isLoading = false;

                        if (page == 1) notifications.clear();

                        List<Notification> list = (dto != null) ? dto.getData() : null;

                        if (list != null && !list.isEmpty()) {
                            notifications.addAll(list);
                            adapter.setItems(notifications);
                            currentPage = page;
                        }

                        // Pagination (source of truth)
                        co.highfive.petrolstation.notifications.dto.Pagination p =
                                (dto != null) ? dto.getPagination() : null;

                        if (p != null) {
                            hasMore = p.getCurrent_page() < p.getLast_page();
                        } else {
                            // fallback (if API didn't send pagination for any reason)
                            hasMore = list != null && !list.isEmpty();
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
