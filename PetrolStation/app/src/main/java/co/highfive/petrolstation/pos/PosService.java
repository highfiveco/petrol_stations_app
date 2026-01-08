package co.highfive.petrolstation.pos;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.Endpoints;
import co.highfive.petrolstation.pos.dto.PosAddItemDto;
import co.highfive.petrolstation.pos.dto.PosAddResponse;
import co.highfive.petrolstation.pos.dto.PosPaymentMethodDto;

public class PosService {

    private final ApiClient apiClient;
    private final Gson gson;

    public PosService(ApiClient apiClient, Gson gson) {
        this.apiClient = apiClient;
        this.gson = gson;
    }

    public void addPosInvoice(
            int accountId,
            List<PosAddItemDto> items,
            List<PosPaymentMethodDto> paymentMethods,
            String notes,
            ApiCallback<PosAddResponse> callback
    ) {

        PosRequests.BuiltForm form = PosRequests.buildPosAddForm(
                accountId,
                items,
                paymentMethods,
                notes,
                gson
        );

        Type type = new TypeToken<PosAddResponse>() {}.getType();

        apiClient.requestRawLists(
                Constant.REQUEST_POST,
                Endpoints.POS_ADD,
                form.keys,
                form.values,
                null,
                type,
                0,
                callback
        );
    }
}
