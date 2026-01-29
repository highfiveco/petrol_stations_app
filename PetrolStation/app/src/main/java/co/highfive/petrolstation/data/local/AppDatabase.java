package co.highfive.petrolstation.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import co.highfive.petrolstation.data.local.dao.AccountDao;
import co.highfive.petrolstation.data.local.dao.CampaignDao;
import co.highfive.petrolstation.data.local.dao.CategoriesMetaDao;
import co.highfive.petrolstation.data.local.dao.CategoryDao;
import co.highfive.petrolstation.data.local.dao.CompanySettingCacheDao;
import co.highfive.petrolstation.data.local.dao.GetSettingCacheDao;
import co.highfive.petrolstation.data.local.dao.ItemDao;
import co.highfive.petrolstation.data.local.dao.CustomerDao;
import co.highfive.petrolstation.data.local.dao.CustomersMetaDao;

// Customers Setting
import co.highfive.petrolstation.data.local.dao.CustomersSettingCacheDao;
import co.highfive.petrolstation.data.local.dao.CustomerClassifyDao;
import co.highfive.petrolstation.data.local.dao.CustomerStatusDao;

// Customer Vehicles
import co.highfive.petrolstation.data.local.dao.CustomerVehicleDao;
import co.highfive.petrolstation.data.local.dao.ItemsCacheDao;
import co.highfive.petrolstation.data.local.dao.OfflineCustomerDao;
import co.highfive.petrolstation.data.local.dao.OfflineCustomerPhoneUpdateDao;
import co.highfive.petrolstation.data.local.dao.OfflineCustomerVehicleDao;
import co.highfive.petrolstation.data.local.dao.OfflineCustomerVehicleEditDao;
import co.highfive.petrolstation.data.local.dao.OfflineFinancialTransactionDao;
import co.highfive.petrolstation.data.local.dao.OfflineFuelInvoiceDao;
import co.highfive.petrolstation.data.local.dao.OfflineInvoiceDao;
import co.highfive.petrolstation.data.local.dao.PumpDao;
import co.highfive.petrolstation.data.local.dao.VehicleColorDao;
import co.highfive.petrolstation.data.local.dao.VehicleTypeDao;

// Fuel Settings
import co.highfive.petrolstation.data.local.dao.FuelPaymentTypeDao;
import co.highfive.petrolstation.data.local.dao.FuelPumpDao;
import co.highfive.petrolstation.data.local.dao.FuelSaleItemDao;
import co.highfive.petrolstation.data.local.dao.FuelCampaignDao;
import co.highfive.petrolstation.data.local.dao.FuelCampaignItemDao;
import co.highfive.petrolstation.data.local.dao.FuelPriceSettingsCacheDao;

// Fuel Sale (Invoices)
import co.highfive.petrolstation.data.local.dao.FuelSalesDao;
import co.highfive.petrolstation.data.local.dao.InvoiceDetailDao;
import co.highfive.petrolstation.data.local.dao.FuelPriceAddJsonSyncCacheDao;

// POS settings/items/add/add-json
import co.highfive.petrolstation.data.local.dao.PosCategoryDao;
import co.highfive.petrolstation.data.local.dao.PosPaymentTypeDao;
import co.highfive.petrolstation.data.local.dao.PosSettingsCacheDao;

import co.highfive.petrolstation.data.local.dao.PosItemDao;

import co.highfive.petrolstation.data.local.dao.PosInvoiceDao;
import co.highfive.petrolstation.data.local.dao.PosInvoiceDetailDao;

import co.highfive.petrolstation.data.local.dao.PosAddJsonSyncCacheDao;

import co.highfive.petrolstation.data.local.entities.CategoriesMetaCacheEntity;
import co.highfive.petrolstation.data.local.entities.CategoryEntity;
import co.highfive.petrolstation.data.local.entities.CompanySettingCacheEntity;
import co.highfive.petrolstation.data.local.entities.GetSettingCacheEntity;
import co.highfive.petrolstation.data.local.entities.ItemEntity;
import co.highfive.petrolstation.data.local.entities.CustomerEntity;
import co.highfive.petrolstation.data.local.entities.CustomersMetaCacheEntity;

// Customers Setting entities
import co.highfive.petrolstation.data.local.entities.CustomersSettingCacheEntity;
import co.highfive.petrolstation.data.local.entities.CustomerClassifyEntity;
import co.highfive.petrolstation.data.local.entities.CustomerStatusEntity;

// Customer Vehicles entities
import co.highfive.petrolstation.data.local.entities.CustomerVehicleEntity;
import co.highfive.petrolstation.data.local.entities.ItemsCacheEntity;
import co.highfive.petrolstation.data.local.entities.OfflineCustomerEntity;
import co.highfive.petrolstation.data.local.entities.OfflineCustomerPhoneUpdateEntity;
import co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEditEntity;
import co.highfive.petrolstation.data.local.entities.OfflineCustomerVehicleEntity;
import co.highfive.petrolstation.data.local.entities.OfflineFinancialTransactionEntity;
import co.highfive.petrolstation.data.local.entities.OfflineFuelInvoiceEntity;
import co.highfive.petrolstation.data.local.entities.OfflineInvoiceEntity;
import co.highfive.petrolstation.data.local.entities.PosItemCacheEntity;
import co.highfive.petrolstation.data.local.entities.VehicleColorEntity;
import co.highfive.petrolstation.data.local.entities.VehicleTypeEntity;

// Fuel Settings entities
import co.highfive.petrolstation.data.local.entities.FuelPaymentTypeEntity;
import co.highfive.petrolstation.data.local.entities.FuelPumpEntity;
import co.highfive.petrolstation.data.local.entities.FuelSaleItemEntity;
import co.highfive.petrolstation.data.local.entities.FuelCampaignEntity;
import co.highfive.petrolstation.data.local.entities.FuelCampaignItemEntity;
import co.highfive.petrolstation.data.local.entities.FuelPriceSettingsCacheEntity;

// Fuel Sale entities (Invoices + Details + Sync cache)
import co.highfive.petrolstation.data.local.entities.FuelSaleEntity;
import co.highfive.petrolstation.data.local.entities.InvoiceDetailEntity;
import co.highfive.petrolstation.data.local.entities.FuelPriceAddJsonSyncCacheEntity;

// POS entities
import co.highfive.petrolstation.data.local.entities.PosCategoryEntity;
import co.highfive.petrolstation.data.local.entities.PosPaymentTypeEntity;
import co.highfive.petrolstation.data.local.entities.PosSettingsCacheEntity;

import co.highfive.petrolstation.data.local.entities.PosItemEntity;

import co.highfive.petrolstation.data.local.entities.PosInvoiceEntity;
import co.highfive.petrolstation.data.local.entities.PosInvoiceDetailEntity;

import co.highfive.petrolstation.data.local.entities.PosAddJsonSyncCacheEntity;
import co.highfive.petrolstation.data.local.dao.InvoiceDao;
import co.highfive.petrolstation.data.local.entities.InvoiceEntity;

import co.highfive.petrolstation.data.local.dao.AccountDao;
import co.highfive.petrolstation.data.local.dao.PumpDao;
import co.highfive.petrolstation.data.local.dao.CampaignDao;

import co.highfive.petrolstation.data.local.entities.AccountEntity;
import co.highfive.petrolstation.data.local.entities.PumpEntity;
import co.highfive.petrolstation.data.local.entities.CampaignEntity;

@Database(
        entities = {
                InvoiceEntity.class,
                OfflineInvoiceEntity.class,
                OfflineFuelInvoiceEntity.class,
                // =========================
                // CompanySetting
                // =========================
                CompanySettingCacheEntity.class,

                // =========================
                // get-categories
                // =========================
                CategoryEntity.class,
                ItemEntity.class,
                CategoriesMetaCacheEntity.class,

                // =========================
                // getSetting
                // =========================
                GetSettingCacheEntity.class,

                // =========================
                // customers
                // =========================
                CustomerEntity.class,
                OfflineCustomerEntity.class,
                CustomersMetaCacheEntity.class,

                // =========================
                // api/getCustomersSetting
                // =========================
                CustomersSettingCacheEntity.class,
                CustomerStatusEntity.class,
                CustomerClassifyEntity.class,

                // =========================
                // customer-vehicles
                // =========================
                CustomerVehicleEntity.class,
                OfflineCustomerVehicleEntity.class,
                VehicleTypeEntity.class,
                VehicleColorEntity.class,

                // =========================
                // fuel-price/settings
                // =========================
                FuelPaymentTypeEntity.class,
                FuelPumpEntity.class,
                FuelSaleItemEntity.class,
                FuelCampaignEntity.class,
                FuelCampaignItemEntity.class,
                FuelPriceSettingsCacheEntity.class,

                // =========================
                // fuel sales + details + add-json sync cache
                // =========================
                FuelSaleEntity.class,
                InvoiceDetailEntity.class,
                FuelPriceAddJsonSyncCacheEntity.class,

                // =========================
                // POS settings
                // =========================
                PosCategoryEntity.class,
                PosPaymentTypeEntity.class,
                PosSettingsCacheEntity.class,

                // =========================
                // POS items
                // =========================
                PosItemEntity.class,
                ItemsCacheEntity.class,

                // =========================
                // POS add (invoice + details)
                // =========================
                PosInvoiceEntity.class,
                PosInvoiceDetailEntity.class,

                // =========================
                // POS add-json sync cache
                // =========================
                PosAddJsonSyncCacheEntity.class,
                AccountEntity.class,
                PumpEntity.class,
                CampaignEntity.class,
                PosItemCacheEntity.class,
                OfflineFinancialTransactionEntity.class,
                OfflineCustomerPhoneUpdateEntity.class,
                OfflineCustomerVehicleEditEntity.class
        },
        version = 26,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {


    // CompanySetting
    public abstract CompanySettingCacheDao companySettingCacheDao();

    // get-categories
    public abstract CategoryDao categoryDao();
    public abstract ItemDao itemDao();
    public abstract CategoriesMetaDao categoriesMetaDao();

    // getSetting
    public abstract GetSettingCacheDao getSettingCacheDao();

    // customers
    public abstract CustomerDao customerDao();
    public abstract CustomersMetaDao customersMetaDao();

    // api/getCustomersSetting
    public abstract CustomersSettingCacheDao customersSettingCacheDao();
    public abstract CustomerStatusDao customerStatusDao();
    public abstract CustomerClassifyDao customerClassifyDao();

    // customer-vehicles
    public abstract CustomerVehicleDao customerVehicleDao();
    public abstract VehicleTypeDao vehicleTypeDao();
    public abstract VehicleColorDao vehicleColorDao();

    // fuel-price/settings
    public abstract FuelPaymentTypeDao fuelPaymentTypeDao();
    public abstract FuelPumpDao fuelPumpDao();
    public abstract FuelSaleItemDao fuelSaleItemDao();
    public abstract FuelCampaignDao fuelCampaignDao();
    public abstract FuelCampaignItemDao fuelCampaignItemDao();
    public abstract FuelPriceSettingsCacheDao fuelPriceSettingsCacheDao();

    // fuel sale + details + add-json cache
    public abstract FuelSalesDao fuelSalesDao();
    public abstract InvoiceDetailDao invoiceDetailDao();
    public abstract FuelPriceAddJsonSyncCacheDao fuelPriceAddJsonSyncCacheDao();

    // POS settings
    public abstract PosCategoryDao posCategoryDao();
    public abstract PosPaymentTypeDao posPaymentTypeDao();
    public abstract PosSettingsCacheDao posSettingsCacheDao();

    // POS items
    public abstract PosItemDao posItemDao();

    // POS add
    public abstract PosInvoiceDao posInvoiceDao();
    public abstract PosInvoiceDetailDao posInvoiceDetailDao();

    // POS add-json sync cache
    public abstract PosAddJsonSyncCacheDao posAddJsonSyncCacheDao();

    public abstract InvoiceDao invoiceDao();
    public abstract OfflineInvoiceDao offlineInvoiceDao();
    public abstract OfflineFuelInvoiceDao offlineFuelInvoiceDao();
    public abstract AccountDao accountDao();
    public abstract PumpDao pumpDao();
    public abstract CampaignDao campaignDao();

    public abstract ItemsCacheDao itemsCacheDao();
    public abstract OfflineCustomerDao offlineCustomerDao();
    public abstract OfflineCustomerVehicleDao offlineCustomerVehicleDao();

    public abstract OfflineFinancialTransactionDao offlineFinancialTransactionDao();

    public abstract OfflineCustomerPhoneUpdateDao offlineCustomerPhoneUpdateDao();
    public abstract OfflineCustomerVehicleEditDao offlineCustomerVehicleEditDao();

}
