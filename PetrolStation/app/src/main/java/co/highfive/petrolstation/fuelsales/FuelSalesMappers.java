package co.highfive.petrolstation.fuelsales;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.entities.FuelSaleEntity;

public class FuelSalesMappers {

    public static List<FuelSaleEntity> toEntities(List<InvoiceDto> invoices, Integer queryAccountId, Gson gson) {
        List<FuelSaleEntity> out = new ArrayList<>();
        if (invoices == null) return out;

        long now = System.currentTimeMillis();

        for (InvoiceDto inv : invoices) {
            if (inv == null) continue;

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
//            e.pumpJson = inv.pump != null ? gson.toJson(inv.pump) : null;
//            e.customerVehicleJson = inv.customer_vehicle != null ? gson.toJson(inv.customer_vehicle) : null;

            e.queryAccountId = queryAccountId;
            e.updatedAt = now;

            out.add(e);
        }

        return out;
    }
}
