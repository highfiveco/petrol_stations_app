package co.highfive.petrolstation.settings;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.settings.dto.CompanySettingData;

public class SettingsService {

    private final ApiClient apiClient;

    public SettingsService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * GET /api/getCompanySetting?user_sanad=...
     * يحتاج Authorization (ApiClient سيضيفه تلقائياً إذا logged in).
     */
    public void getCompanySetting(int userSanad, ApiCallback<CompanySettingData> callback) {

        Map<String, String> params = ApiClient.mapOf(
                "user_sanad", userSanad
        );

        Type type = new TypeToken<BaseResponse<CompanySettingData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,           // عندك String
                Endpoints.GET_COMPANY_SETTING,
                params,
                null,
                type,
                0,
                callback
        );
    }
}
