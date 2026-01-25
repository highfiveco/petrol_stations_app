package co.highfive.petrolstation.invoices;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDetailDto;
import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.entities.InvoiceDetailEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceListEntity;

public class InvoicesListMappers {

    public static List<InvoiceListEntity> toInvoiceListEntities(
            List<InvoiceDto> invoices,
            Integer queryAccountId,
            Gson gson
    ) {
        List<InvoiceListEntity> out = new ArrayList<>();
        if (invoices == null) return out;

        long now = System.currentTimeMillis();

        for (InvoiceDto inv : invoices) {
            if (inv == null) continue;

            InvoiceListEntity e = new InvoiceListEntity();
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

    public static List<InvoiceDetailEntity> toDetailEntities(List<InvoiceDto> invoices, Gson gson) {
        List<InvoiceDetailEntity> out = new ArrayList<>();
        if (invoices == null) return out;

        for (InvoiceDto inv : invoices) {
            if (inv == null || inv.details == null) continue;

            for (InvoiceDetailDto d : inv.details) {
                if (d == null) continue;

                InvoiceDetailEntity e = new InvoiceDetailEntity();
                e.id = d.id;
                e.invoiceId = (d.invoice_id != null) ? d.invoice_id : inv.id;
                e.itemId = d.item_id;
                e.updateCostPrice = d.update_cost_price;
                e.count = d.count;
                e.price = d.price;
//                e.itemJson = d.item != null ? gson.toJson(d.item) : null;

                out.add(e);
            }
        }
        return out;
    }
}
