package co.highfive.petrolstation.pos;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers.dto.InvoiceDetailDto;
import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.entities.PosInvoiceDetailEntity;
import co.highfive.petrolstation.data.local.entities.PosInvoiceEntity;

public class PosInvoiceMappers {

    public static PosInvoiceEntity toInvoiceEntity(InvoiceDto inv, Gson gson) {
        PosInvoiceEntity e = new PosInvoiceEntity();
        e.id = inv.id;

        e.date = inv.date;
        e.statement = inv.statement;

        e.accountId = inv.account_id;
        e.payAmount = inv.pay_amount;
        e.storeId = inv.store_id;
        e.discount = inv.discount != null ? inv.discount.doubleValue() : null;
        e.total = inv.total;

        e.invoiceNo = inv.invoice_no;
        e.notes = inv.notes;

        e.accountJson = inv.account != null ? gson.toJson(inv.account) : null;
//        e.pumpJson = inv.pump != null ? gson.toJson(inv.pump) : null;
//        e.campaignJson = inv.campaign != null ? gson.toJson(inv.campaign) : null;
//        e.customerVehicleJson = inv.customer_vehicle != null ? gson.toJson(inv.customer_vehicle) : null;

        e.updatedAt = System.currentTimeMillis();
        return e;
    }

    public static List<PosInvoiceDetailEntity> toDetailEntities(InvoiceDto inv, Gson gson) {
        List<PosInvoiceDetailEntity> out = new ArrayList<>();
        if (inv == null || inv.details == null) return out;

        for (InvoiceDetailDto d : inv.details) {
            if (d == null) continue;

            PosInvoiceDetailEntity e = new PosInvoiceDetailEntity();
            e.id = d.id;
            e.invoiceId = d.invoice_id;

            e.itemId = d.item_id;
            e.count = d.count;
            e.price = d.price;
            e.updateCostPrice = d.update_cost_price;

            e.itemJson = d.item != null ? gson.toJson(d.item) : null;

            out.add(e);
        }
        return out;
    }
}
