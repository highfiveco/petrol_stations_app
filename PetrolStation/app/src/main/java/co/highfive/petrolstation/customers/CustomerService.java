package co.highfive.petrolstation.customers;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.customers.dto.CustomerSelectDto;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.BaseResponse;
import co.highfive.petrolstation.network.Endpoints;

public class CustomerService {

    private final ApiClient apiClient;

    public CustomerService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void selectCustomers(String name, ApiCallback<java.util.List<CustomerSelectDto>> callback) {

        Map<String, String> params = ApiClient.mapOf(
                "name", name
        );

        Type type = new TypeToken<BaseResponse<java.util.List<CustomerSelectDto>>>() {}.getType();

        apiClient.request(
                Constant.REQUEST_GET,
                Endpoints.CUSTOMERS_SELECT,
                params,
                null,
                type,
                0,
                callback
        );
    }
}
