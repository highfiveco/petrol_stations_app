package co.highfive.petrolstation.fuelsale;

import com.google.gson.Gson;
import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.entities.FuelSaleEntity;

public class FuelSalesSingleMapper {

    public static FuelSaleEntity toEntity(InvoiceDto inv, Gson gson) {
        if (inv == null) return null;

        FuelSaleEntity e = new FuelSaleEntity();
        e.id = inv.id;

        e.date = inv.date;
        e.payAmount = inv.pay_amount;
        e.statement = inv.statement;

        e.accountId = inv.account_id;
        e.storeId = inv.store_id;

        e.discount = inv.discount;
        e.total = inv.total;

        e.invoiceNo = inv.invoice_no;
        e.notes = inv.notes;

        e.campaignId = inv.campaign_id;
        e.pumpId = inv.pump_id;
        e.customerVehicleId = inv.customer_vehicle_id;

        e.accountJson = inv.account != null ? gson.toJson(inv.account) : null;
//        e.pumpJson = inv.pump != null ? gson.toJson(inv.pump) : null;
//        e.customerVehicleJson = inv.customer_vehicle != null ? gson.toJson(inv.customer_vehicle) : null;

        return e;
    }
}
