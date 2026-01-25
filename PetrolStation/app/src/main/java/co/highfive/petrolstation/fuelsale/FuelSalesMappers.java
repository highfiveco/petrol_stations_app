package co.highfive.petrolstation.fuelsale;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.customers.dto.InvoiceDetailDto;
import co.highfive.petrolstation.data.local.entities.FuelSaleEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceDetailEntity;

public class FuelSalesMappers {

    public static List<FuelSaleEntity> toFuelSaleEntities(List<InvoiceDto> invoices, Gson gson) {
        List<FuelSaleEntity> out = new ArrayList<>();
        if (invoices == null) return out;

        for (InvoiceDto inv : invoices) {
            if (inv == null) continue;

            FuelSaleEntity e = new FuelSaleEntity();
            e.id = inv.id;
            e.date = inv.date;
            e.statement = inv.statement;
            e.payAmount = inv.pay_amount;
            e.accountId = inv.account_id;
            e.storeId = inv.store_id;
            e.discount = inv.discount;
            e.total = inv.total;
            e.invoiceNo = inv.invoice_no;
            e.notes = inv.notes;
            e.campaignId = inv.campaign_id;
            e.pumpId = inv.pump_id;
            e.customerVehicleId = inv.customer_vehicle_id;

            // snapshots
            e.accountJson = inv.account != null ? gson.toJson(inv.account) : null;
//            e.pumpJson = inv.pump != null ? gson.toJson(inv.pump) : null;
//            e.campaignJson = inv.campaign != null ? gson.toJson(inv.campaign) : null;
//            e.customerVehicleJson = inv.customer_vehicle != null ? gson.toJson(inv.customer_vehicle) : null;

            e.updatedAt = System.currentTimeMillis();

            // فلتر مفيد للشاشة (اختياري)
            e.queryAccountId = inv.account_id;

            out.add(e);
        }
        return out;
    }

    public static List<InvoiceDetailEntity> toDetailEntities(List<InvoiceDto> invoices, Gson gson) {
        List<InvoiceDetailEntity> out = new ArrayList<>();
        if (invoices == null) return out;

        for (InvoiceDto inv : invoices) {
            if (inv == null || inv.details == null) continue;

            for (InvoiceDetailDto d : inv.details) {
                if (d == null) continue;

                InvoiceDetailEntity e = new InvoiceDetailEntity();
                e.id = d.id;
                e.invoiceId = d.invoice_id;
                e.itemId = d.item_id;
                e.count = d.count;
                e.price = d.price;
                e.updateCostPrice = d.update_cost_price;

//                e.itemJson = d.item != null ? gson.toJson(d.item) : null;

                out.add(e);
            }
        }
        return out;
    }
}
