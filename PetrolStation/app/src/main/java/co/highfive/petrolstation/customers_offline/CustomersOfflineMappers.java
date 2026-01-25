package co.highfive.petrolstation.customers_offline;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import co.highfive.petrolstation.customers.dto.CustomerDto;
import co.highfive.petrolstation.customers.dto.CustomerVehicleDto;
import co.highfive.petrolstation.customers.dto.InvoiceDetailDto;
import co.highfive.petrolstation.customers.dto.InvoiceDto;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.CustomerVehicleEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceDetailEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceEntity;

public class CustomersOfflineMappers {

    public static List<CustomerEntity> toCustomerEntities(List<CustomerDto> customers) {
        List<CustomerEntity> out = new ArrayList<>();
        if (customers == null) return out;

        for (CustomerDto c : customers) {
            if (c == null) continue;

            CustomerEntity e = new CustomerEntity();
            e.id = c.id;
            e.typeCustomer = c.type_customer;
            e.customerClassify = c.customer_classify;
            e.name = c.name;
            e.accountId = c.account_id;
            e.mobile = c.mobile;
            e.status = c.status;
            e.customerStatus = c.customer_status;
            e.customerClassifyName = c.customer_classify_name;
            e.typeCustomerName = c.type_customer_name;
            e.balance = c.balance;
            e.campaignName = c.campaign_name;
            e.remainingAmount = c.remaining_amount;
            out.add(e);
        }

        return out;
    }

    public static List<CustomerVehicleEntity> toVehicleEntities(List<CustomerDto> customers) {
        List<CustomerVehicleEntity> out = new ArrayList<>();
        if (customers == null) return out;

        for (CustomerDto c : customers) {
            if (c == null || c.vehicles == null) continue;

            for (CustomerVehicleDto v : c.vehicles) {
                if (v == null) continue;

                CustomerVehicleEntity e = new CustomerVehicleEntity();
                e.id = v.id;
                e.customerId = (v.customer_id != null) ? v.customer_id : c.id;
                e.vehicleNumber = v.vehicle_number;
                e.vehicleType = v.vehicle_type;
                e.vehicleColor = v.vehicle_color;
                e.model = v.model;
                e.licenseExpiryDate = v.license_expiry_date;
                e.notes = v.notes;
                e.createdAt = v.created_at;
                e.vehicleTypeName = v.vehicle_type_name;
                e.vehicleColorName = v.vehicle_color_name;
                e.accountId = v.account_id;

                out.add(e);
            }
        }

        return out;
    }

    public static List<InvoiceEntity> toInvoiceEntities(List<CustomerDto> customers, Gson gson) {
        List<InvoiceEntity> out = new ArrayList<>();
        if (customers == null) return out;

        for (CustomerDto c : customers) {
            if (c == null) continue;

            // invoices (غير محروقات)
            if (c.invoices != null) {
                for (InvoiceDto inv : c.invoices) {
                    if (inv == null) continue;
                    out.add(mapInvoice(c.id, inv, gson));
                }
            }

            // fuel_invoices (محروقات)
            if (c.fuel_invoices != null) {
                for (InvoiceDto inv : c.fuel_invoices) {
                    if (inv == null) continue;
                    out.add(mapInvoice(c.id, inv, gson));
                }
            }
        }

        return out;
    }

    private static InvoiceEntity mapInvoice(int customerId, InvoiceDto inv, Gson gson) {
        InvoiceEntity e = new InvoiceEntity();
        e.id = inv.id;
        e.customerId = customerId;

        e.date = inv.date;
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

        e.isFuelSale = inv.is_fuel_sale;
        e.createdAt = inv.created_at;

//        e.accountJson = inv.account != null ? gson.toJson(inv.account) : null;
//        e.pumpJson = inv.pump != null ? gson.toJson(inv.pump) : null;
//        e.customerVehicleJson = inv.customer_vehicle != null ? gson.toJson(inv.customer_vehicle) : null;
        return e;
    }

    public static List<InvoiceDetailEntity> toInvoiceDetailEntities(List<CustomerDto> customers, Gson gson) {
        List<InvoiceDetailEntity> out = new ArrayList<>();
        if (customers == null) return out;

        for (CustomerDto c : customers) {
            if (c == null) continue;

            // process both invoice lists
            collectDetails(out, c.invoices, gson);
            collectDetails(out, c.fuel_invoices, gson);
        }

        return out;
    }

    private static void collectDetails(List<InvoiceDetailEntity> out, List<InvoiceDto> invoices, Gson gson) {
        if (invoices == null) return;

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
    }
}
