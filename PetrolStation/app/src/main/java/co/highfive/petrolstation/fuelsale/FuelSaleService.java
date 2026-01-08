package co.highfive.petrolstation.fuelsale;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.fuelsale.dto.FuelPriceAddJsonResponse;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceAddRequest;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceOfflineInvoiceRequestDto;
import co.highfive.petrolstation.network.ApiCallback;
import co.highfive.petrolstation.network.ApiClient;
import co.highfive.petrolstation.network.ParamsList;
import co.highfive.petrolstation.fuelsale.dto.FuelPriceAddResponse;
import co.highfive.petrolstation.hazemhamadaqa.Http.Constant;
import co.highfive.petrolstation.network.Endpoints;

public class FuelSaleService {

    private final ApiClient apiClient;
    private final Gson gson;

    public FuelSaleService(ApiClient apiClient, Gson gson) {
        this.apiClient = apiClient;
        this.gson = gson;
    }

    public void addFuelInvoice(FuelPriceAddRequest req, ApiCallback<FuelPriceAddResponse> callback) {

        ParamsList p = new ParamsList();

        // arrays
        p.addRepeated("item_id[]", req.itemIds);
        p.addRepeated("price[]", req.prices);
        p.addRepeated("count[]", req.counts);

        // normal fields
        p.add("account_id", req.accountId);
        p.add("customer_vehicle_id", req.customerVehicleId);
        p.add("pump_id", req.pumpId);
        p.add("campaign_id", req.campaignId);
        p.add("notes", req.notes);

        // payment_methods JSON string
        if (req.paymentMethods != null) {
            p.add("payment_methods", gson.toJson(req.paymentMethods));
        }

        Type type = new TypeToken<FuelPriceAddResponse>() {}.getType();

        apiClient.requestRawLists(
                Constant.REQUEST_POST,
                Endpoints.FUEL_PRICE_ADD,
                p.keys,
                p.values,
                null,
                type,
                0,
                callback
        );
    }

    public void addFuelInvoicesJson(
            List<FuelPriceOfflineInvoiceRequestDto> invoices,
            ApiCallback<FuelPriceAddJsonResponse> callback
    ) {

        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        keys.add("data");
        values.add(gson.toJson(invoices));

        Type type = new TypeToken<FuelPriceAddJsonResponse>() {}.getType();

        apiClient.requestRawLists(
                Constant.REQUEST_POST,
                Endpoints.FUEL_PRICE_ADD_JSON,
                keys,
                values,
                null,
                type,
                0,
                callback
        );
    }
}
