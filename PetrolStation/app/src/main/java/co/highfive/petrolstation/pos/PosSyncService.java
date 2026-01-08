package co.highfive.petrolstation.pos;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.pos.dto.PosAddJsonInvoiceDto;
import co.highfive.petrolstation.pos.dto.PosAddJsonResponse;

public class PosSyncService {

    private final ApiClient apiClient;
    private final Gson gson;

    public PosSyncService(ApiClient apiClient, Gson gson) {
        this.apiClient = apiClient;
        this.gson = gson;
    }

    public void addJsonPosInvoices(
            List<PosAddJsonInvoiceDto> invoices,
            ApiCallback<PosAddJsonResponse> callback
    ) {
        PosJsonRequests.BuiltForm form = PosJsonRequests.buildPosAddJsonForm(invoices, gson);

        Type type = new TypeToken<PosAddJsonResponse>() {}.getType();

        apiClient.requestRawLists(
                Constant.REQUEST_POST,
                Endpoints.POS_ADD_JSON,
                form.keys,
                form.values,
                null,
                type,
                0,
                callback
        );
    }
}
