package co.highfive.petrolstation.pos;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.pos.dto.PosInvoicesOfflineResponse;

public class PosInvoicesOfflineService {

    private final ApiClient apiClient;

    public PosInvoicesOfflineService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void getInvoicesOffline(ApiCallback<PosInvoicesOfflineResponse> callback) {

        Type type = new TypeToken<PosInvoicesOfflineResponse>() {}.getType();

        apiClient.requestRawLists(
                Constant.REQUEST_GET,
                Endpoints.POS_INVOICES_OFFLINE,
                new java.util.ArrayList<>(),   // keys empty
                new java.util.ArrayList<>(),   // values empty
                null,
                type,
                0,
                callback
        );
    }
}
