package co.highfive.petrolstation.hazemhamadaqa.app;


/**
 * Created by Eng. Hazem Hamadaqa on 12/14/2016.
 */

public class AppConfig {


    public static String testUrl="https://google.com";
    public static String serverTimeZone ="UTC";

    public static String BASIC_URL="http://api.e-bill.site/";
//    public static String BASIC_URL="https://testh.e-bill.site/";

    public static String ImageUrl= BASIC_URL+ "upload/thumb/crop,256x256,/images/";
    public static String ProjectImageUrl= BASIC_URL+ "upload/thumb/crop,685x685,";

    public static String BASE_URL_API =BASIC_URL+"api/";
    public static String upload_file =BASIC_URL+"upload_file/";

    public static String app_logs=BASE_URL_API+"app-logs";

    ////////////auth/////////////////////////////////
    public static String login=BASE_URL_API+"auth/login";
    public static String loginQr=BASE_URL_API+"auth/qr";
    public static String logout=BASE_URL_API+"auth/logout";
    ////////////auth/////////////////////////////////

    public static String start_synchronization=BASE_URL_API+"start-synchronization";
    public static String end_synchronization=BASE_URL_API+"end-synchronization";
    public static String getSetting=BASE_URL_API+"getSetting";
    public static String updateUserSanad=BASE_URL_API+"updateUserSanad";
    public static String getCompanySetting=BASE_URL_API+"getCompanySetting";
    public static String fund=BASE_URL_API+"fund";

    public static String closeFund=fund+"/close";
    public static String getPosts=BASE_URL_API+"getPosts";
    public static String getNotifications=BASE_URL_API+"getNotifications";
    ////////////Setting/////////////////////////////////

    ////////////achievements/////////////////////////////////
    public static String achievements=BASE_URL_API+"achievements";
    ////////////achievements/////////////////////////////////

    ////////////customers/////////////////////////////////

    public static String getCustomersSetting=BASE_URL_API+"getCustomersSetting";
    public static String customers=BASE_URL_API+"customers";
    public static String customersOffline=BASE_URL_API+"customers-offline";
    public static String customerLog=customers+"/log";
    public static String viewFinancialMove=customers+"/viewFinancialMove";
    public static String getLastReading=customers+"/getLastReading";
    public static String getCustomerReadings=customers+"/getReadings";
    public static String getReminders=customers+"/getReminders";
    public static String customerEdit=customers+"/edit";
    public static String customerAdd=customers+"/add";
    public static String customerUpdate=customers+"/update";
    public static String update_mobile=customers+"/update-mobile";
    public static String updateCustomerPhones=customers+"/update-mobile-json";
    public static String updateLastReading=customers+"/updateLastReading";
    public static String updateLastReadingsJson=customers+"/updateLastReadingsJson";
    public static String deleteReminders=customers+"/deleteReminders";
    public static String addReminders=customers+"/addReminders";
    public static String getLastReadingMessage=customers+"/getLastReadingMessage";
    public static String sendOneSms=customers+"/sendOneSms";



    ////////////customers/////////////////////////////////

    // ////////maintenances/////////////////////////////////
    public static String maintenances=BASE_URL_API+"maintenances";
    public static String edit=maintenances+"/edit";
    public static String addMaintenance=maintenances+"/add";
    public static String updateMaintenance=maintenances+"/update";
    public static String endMaintenance=maintenances+"/end";
    public static String addReply=maintenances+"/addReply";
    public static String getReply=maintenances+"/getReply";
    ////////////maintenances/////////////////////////////////

    // ////////financial/////////////////////////////////

    public static String financial=BASE_URL_API+"financial";
    public static String addMove=financial+"/addMove";
    public static String addLoad=financial+"/addLoad";
    public static String addDiscount=financial+"/addDiscount";
    public static String deleteMove=financial+"/deleteMove";
    public static String printMove=financial+"/print-move";
    public static String getAccounts=financial+"/getAccounts";
    public static String addAccount=financial+"/addAccount";
    public static String updateAccount=financial+"/updateAccount";
    public static String addReturn=financial+"/addReturn";
    public static String updateMoveTransactions = financial+"/add-moves-json";

    ////////////financial/////////////////////////////////

    // ////////readings/////////////////////////////////
    public static String weeklyReadings=BASE_URL_API+"weeklyReadings";
    public static String readings=BASE_URL_API+"readings";
    public static String getMonthlyReadingsOffline=BASE_URL_API+"getMonthlyReadingsOffline";
    public static String weeklyReadingsOffline=BASE_URL_API+"weeklyReadingsOffline";
    public static String achievementsOffline=BASE_URL_API+"achievements-offline";
    public static String readingLog=readings+"/log";
    public static String addNewMonth=readings+"/addNewMonth";
    public static String addNewWeek=readings+"/addNewWeek";


    ////////////readings/////////////////////////////////


}