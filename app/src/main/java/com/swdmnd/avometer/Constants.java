package com.swdmnd.avometer;

import java.util.Locale;

/**
 * Created by Arief on 9/3/2015.
 * A class holding constants that is used or will be used in SOFCApp
 */
public class Constants {
    public static final int MESSAGE_READ = 1;

    public static final  int REQUEST_ENABLE_BT = 10;

    public static final int LOG_USER_INPUT = 20;
    public static final int LOG_SYSTEM = 21;
    public static final int LOG_CLEAR = 22;
    //27 is RESERVED
    public static final int LOG_OTHERS = 29;

    public static final int INPUT_LOG = 30;
    public static final int INPUT_COMMAND = 27; //RESERVED

    public static final int MAC_ADDRESS_LENGTH = 17;

    public static final int DRAWER_POSITION_MAIN_MENU = 40;
    public static final int DRAWER_POSITION_SETTING_MENU = 41;
    public static final int DRAWER_POSITION_HOME_MENU = 42;

    public static final int STATUS_SUCCESS = 90;
    public static final int STATUS_FAILED = 91;

    public static final int KEY_BLUETOOTH_ICON = 101;

    public static final long MINIMUM_REALTIME_INTERVAL = 100;
    public static final byte[] ASK_REALTIME_DATA = "1".getBytes();
    public static final byte[] ASK_ALL_RECORDS = "2".getBytes();

    public static final String STATUS_ARGS_STRING = "status_args_string";

    public static Locale APP_LOCALE = new Locale("id");
}
