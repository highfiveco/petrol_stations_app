package co.highfive.petrolstation.hazemhamadaqa.app;

import co.highfive.petrolstation.network.ApiConfig;

public class AppConfig {

    public static String testUrl = "https://google.com";
    public static String serverTimeZone = "UTC";

    public static String ImageUrl = ApiConfig.BASE_URL + "/upload/thumb/crop,256x256,/images/";
    public static String ProjectImageUrl = ApiConfig.BASE_URL + "/upload/thumb/crop,685x685,";

    // ✅ مهم: بدون / في النهاية حتى ما يصير // لاحقًا
    public static String BASE_URL_API = ApiConfig.BASE_URL + "/api";

    public static String upload_file = ApiConfig.BASE_URL + "/upload_file/";
    public static String app_logs = BASE_URL_API + "/app-logs";

    // ✅ sync
    public static String start_synchronization = BASE_URL_API + "/start-synchronization";
    public static String end_synchronization   = BASE_URL_API + "/end-synchronization";

    // ========= customers =========
    public static String customers = BASE_URL_API + "/customers";

    // ✅ update phones
    public static String updateCustomerPhones = customers + "/update-mobile-json";

    // ========= customer vehicles =========
    public static String customerVehicles = BASE_URL_API + "/customer-vehicles";

    // ✅ add + edit same endpoint
    public static String addCustomerVehiclesJson = customerVehicles + "/add-json";

    // ========= POS =========
    public static String pos = BASE_URL_API + "/pos";
    public static String addPosJson = pos + "/add-json";

    // ========= Fuel Price / Fuel Sale =========
    public static String fuelPrice = BASE_URL_API + "/fuel-price";
    public static String addFuelPriceJson = fuelPrice + "/add-json";

    // ========= financial =========
    public static String financial = BASE_URL_API + "/financial";

    // ✅ add moves (includes actionType inside JSON)
    public static String updateMoveTransactions = financial + "/add-moves-json";
}
