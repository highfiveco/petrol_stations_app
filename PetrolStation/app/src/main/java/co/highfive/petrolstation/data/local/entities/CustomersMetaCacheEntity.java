package co.highfive.petrolstation.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customers_meta_cache")
public class CustomersMetaCacheEntity {

    @PrimaryKey
    public int id = 1;

    public Integer sms;
    public Integer viewLog;
    public Integer viewFinancialMove;
    public Integer updateCustomers;
    public Integer addCustomers;
    public Integer viewReminders;
    public Integer addReminders;
    public Integer deleteReminders;
    public Integer updateMobile;
    public Integer viewCustomerVehicles;
    public Integer addCustomerVehicles;
    public Integer editCustomerVehicles;

    public String settingJson; // نخزن SettingDto كـ JSON
    public long updatedAt;
}
