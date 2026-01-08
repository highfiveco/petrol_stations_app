package co.highfive.petrolstation.auth;


import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import co.highfive.petrolstation.auth.dto.LoginData;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class AuthService {

    private final ApiClient apiClient;

    public AuthService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void login(
            String code,
            String username,
            String password,
            String platform,
            String fcmToken,
            ApiCallback<LoginData> callback
    ) {
        Map<String, String> params = ApiClient.mapOf(
                "code", code,
                "username", username,
                "password", password,
                "platform", platform,
                "fcm_token", fcmToken
        );

        Type type = new TypeToken<BaseResponse<LoginData>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_POST,
                Endpoints.AUTH_LOGIN,
                params,
                null,
                type,
                0,
                callback
        );
    }
}
