package co.highfive.petrolstation.data.local.dao;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import co.highfive.petrolstation.data.local.entities.InvoiceEntity;

public class InvoiceWithCustomerLite {

    @Embedded
    public InvoiceEntity invoice;

    @ColumnInfo(name = "customer_name")
    public String customerName;

    @ColumnInfo(name = "customer_mobile")
    public String customerMobile;
}
