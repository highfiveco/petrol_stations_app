package co.highfive.petrolstation.data.local.dao;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import co.highfive.petrolstation.data.local.entities.OfflineInvoiceEntity;

public class OfflineInvoiceWithCustomerLite {

    @Embedded
    public OfflineInvoiceEntity offline;

    @ColumnInfo(name = "c_name")
    public String customerName;

    @ColumnInfo(name = "c_mobile")
    public String customerMobile;
}
