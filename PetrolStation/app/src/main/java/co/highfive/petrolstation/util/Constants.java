package co.highfive.petrolstation.util;

public interface Constants {
  // REQUEST CODES
  int REQUEST_CHECK_SETTINGS = 101;
  int REQUEST_ONBOARDING = 102;
  int REQUEST_LOCATION = 103;
  int REQUEST_WRITE_EXTERNAL = 104;
  int REQUEST_SET_ALARM = 105;
  int REQUEST_TNC = 106;

  int FRIDAY_ALARM_ID = 1135;
  int Favorite_ALARM_ID = 1136;
  int ALARM_ID = 1010;
  int FajerListALARM_ID = 1192;
  int PASSIVE_LOCATION_ID = 1011;
  int PRE_SUHOOR_ALARM_ID = 1012;
  int PRE_IFTAR_ALARM_ID = 1013;

  int PRE_FAJER_ALARM_ID = 1014;
  int PRE_DUHOR_ALARM_ID = 1015;
  int PRE_ASER_ALARM_ID = 1016;
  int PRE_MAGRIB_ALARM_ID = 1017;
  int PRE_ISHAA_ALARM_ID = 1018;

  long ONE_MINUTE = 60000;
  long FIVE_MINUTES = ONE_MINUTE * 5;

  //EXTRAS
  String EXTRA_ALARM_INDEX = "alarm_index";
  String EXTRA_LAST_LOCATION = "last_location";
  String EXTRA_PRAYER_NAME = "prayer_name";
  String EXTRA_PRAYER_TIME = "prayer_time";
  String EXTRA_PRE_ALARM_FLAG = "pre_alarm_flag";

  String CONTENT_FRAGMENT = "content_fragment";
  String TIMES_FRAGMENT = "times_fragment";
  String CONFIG_FRAGMENT = "config_fragment";
  String LOCATION_FRAGMENT = "location_fragment";

  String PRAYER_DAY = "PRAYER_DAY";
  String PRAYER_MONTH = "PRAYER_MONTH";
  String PRAYER_YEAR = "PRAYER_YEAR";
  String Is_Today = "Is_Today";

  int NOTIFICATION_ID = 2010;
  int FavoriteNOTIFICATION_ID = 2018;
}
