package co.highfive.petrolstation.network;

public class Endpoints {

    // Setting
    public static final String APP_LOGS = "/api/app-logs";
    public static final String END_SYNCHRONIZATION = "/api/end-synchronization";
    public static final String GET_CATEGORIES = "/api/get-categories";
    public static final String GET_COMPANY_SETTING = "/api/getCompanySetting";
    public static final String GET_SETTING = "/api/getSetting";
    public static final String START_SYNCHRONIZATION = "/api/start-synchronization";

    // Auth
    public static final String AUTH_LOGIN = "/api/auth/login";
    public static final String AUTH_LOGOUT = "/api/auth/logout";
    public static final String AUTH_QR = "/api/auth/qr";
    public static final String UPDATEFCMTOKEN = "/api/updateFcmtoken";
    public static final String UPDATEUSERSANAD = "/api/updateUserSanad";

    // Customers
    public static final String CUSTOMERS = "/api/customers";
    public static final String CUSTOMERS_ADD = "/api/customers/add";
    public static final String CUSTOMERS_ADDINVOICE = "/api/customers/addInvoice";
    public static final String CUSTOMERS_ADDINVOICE_JSON = "/api/customers/addInvoice-json";
    public static final String CUSTOMERS_ADDREMINDERS = "/api/customers/addReminders";
    public static final String CUSTOMERS_ADD_JSON = "/api/customers/add-json";
    public static final String CUSTOMERS_DELETE = "/api/customers/delete";
    public static final String CUSTOMERS_DELETEINVOICE = "/api/customers/deleteInvoice";
    public static final String CUSTOMERS_DELETEREMINDERS = "/api/customers/deleteReminders";
    public static final String CUSTOMERS_EDIT = "/api/customers/edit";
    public static final String CUSTOMERS_GETFUELSALEBYID = "/api/customers/getFuelSaleById";
    public static final String CUSTOMERS_GETFUELSALES = "/api/customers/getFuelSales";
    public static final String CUSTOMERS_GETINVOICE = "/api/customers/getInvoice";
    public static final String CUSTOMERS_GETINVOICES = "/api/customers/getInvoices";
    public static final String CUSTOMERS_GETREMINDERS = "/api/customers/getReminders";
    public static final String CUSTOMERS_LOG = "/api/customers/log";
    public static final String CUSTOMERS_SELECT = "/api/customers/select";
    public static final String CUSTOMERS_SENDONESMS = "/api/customers/sendOneSms";
    public static final String CUSTOMERS_UPDATE = "/api/customers/update";
    public static final String CUSTOMERS_UPDATE_MOBILE = "/api/customers/update-mobile";
    public static final String CUSTOMERS_UPDATE_MOBILE_JSON = "/api/customers/update-mobile-json";
    public static final String CUSTOMERS_VIEWFINANCIALMOVE = "/api/customers/viewFinancialMove";
    public static final String GETCUSTOMERSSETTING = "/api/getCustomersSetting";

    // Customer Vehicles
    public static final String CUSTOMER_VEHICLES = "/api/customer-vehicles";
    public static final String CUSTOMER_VEHICLES_ADD = "/api/customer-vehicles/add";
    public static final String CUSTOMER_VEHICLES_ADD_JSON = "/api/customer-vehicles/add-json";
    public static final String CUSTOMER_VEHICLES_DELETE = "/api/customer-vehicles/delete";
    public static final String CUSTOMER_VEHICLES_EDIT = "/api/customer-vehicles/edit";
    public static final String CUSTOMER_VEHICLES_SETTINGS = "/api/customer-vehicles/settings";
    public static final String CUSTOMER_VEHICLES_UPDATE = "/api/customer-vehicles/update";

    // Fuel Sales
    public static final String FUEL_PRICE_SETTINGS = "/api/fuel-price/settings";
    public static final String FUEL_PRICE_ADD = "/api/fuel-price/add";
    public static final String FUEL_PRICE_ADD_JSON = "/api/fuel-price/add-json";
    public static final String FUEL_PRICE_CUSTOMER_SEARCH = "/api/fuel-price/customer-search";
    public static final String FUEL_PRICE_GET_CUSTOMER_VEHICLES = "/api/fuel-price/get-customer-vehicles";

    // POS
    public static final String POS_SETTINGS = "/api/pos/settings";
    public static final String POS_INVOICES_OFFLINE = "/api/pos/invoices-offline";
    public static final String POS_ITEMS = "/api/pos/items";
    public static final String POS_ADD = "/api/pos/add";
    public static final String POS_ADD_JSON = "/api/pos/add-json";
    public static final String POS_CUSTOMER_SEARCH = "/api/pos/customer-search";

    // Fund
    public static final String FUND = "/api/fund";
    public static final String FUND_CLOSE = "/api/fund/close";

    // Financial
    public static final String FINANCIAL = "/api/financial";
    public static final String FINANCIAL_ACCOUNTS = "/api/financial/accounts";
    public static final String FINANCIAL_ACCOUNTS_SELECT = "/api/financial/accounts-select";
    public static final String FINANCIAL_ADDACCOUNT = "/api/financial/addAccount";
    public static final String FINANCIAL_ADDDISCOUNT = "/api/financial/addDiscount";
    public static final String FINANCIAL_ADDLOAD = "/api/financial/addLoad";
    public static final String FINANCIAL_ADDMOVE = "/api/financial/addMove";
    public static final String FINANCIAL_ADDRETURN = "/api/financial/addReturn";
    public static final String FINANCIAL_ADD_MOVES_JSON = "/api/financial/add-moves-json";
    public static final String FINANCIAL_BANKS = "/api/financial/banks";
    public static final String FINANCIAL_CURRENCY = "/api/financial/currency";
    public static final String FINANCIAL_DELETEMOVE = "/api/financial/deleteMove";
    public static final String FINANCIAL_getAccounts = "/api/financial/getAccounts";
    public static final String FINANCIAL_DELETEMOVELOAD = "/api/financial/deleteMoveLoad";
    public static final String FINANCIAL_EDITACCOUNT = "/api/financial/editAccount";
    public static final String FINANCIAL_PRINT_MOVE = "/api/financial/print-move";
    public static final String FINANCIAL_REPORT = "/api/financial/report";
    public static final String FINANCIAL_UPDATEACCOUNT = "/api/financial/updateAccount";

    // Notifications
    public static final String GETNOTIFICATIONS = "/api/getNotifications";
    public static final String FUND_TOTAL_PAYMENTS = "/api/fund/total-payments";


}
