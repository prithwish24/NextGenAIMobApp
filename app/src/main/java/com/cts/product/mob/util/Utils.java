package com.cts.product.mob.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//import com.nextgen.carrental.app.constants.GlobalConstants;

/**
 * Single utility class for all common operations
 * @author Prithwish
 */

public class Utils {
    public static final String TAG = Utils.class.getName();
    public static final String SHORT_TIME_AMPM = "h:mm a";
    public static final String LONG_DATE_TIME = "EEE MMM d, yyyy - h:mm a";
    public static final String RESP_DATE_FMT = "yyyy-MM-dd";
    public static final String RESP_TIME_FMT = "hh:mm:ss";
    public static final String YAHOO_WEATHER_DATE = "dd MMM yyyy";


    public static String fmtTime(Date value) {
        return fmtTime(value, SHORT_TIME_AMPM);
    }

    public static String fmtTime(Date value, String fmt) {
        if (value == null) return "";
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            return sdf.format(value);
        } catch (Exception pe) {
            Log.w(TAG, pe.getMessage(), pe);
        }
        return "";
    }

    public static Date fmtDateTime(String value, String originFmt) {
        final SimpleDateFormat sdf = new SimpleDateFormat(originFmt);
        try {
            return sdf.parse(value);
        } catch (ParseException e) {
            Log.w(TAG, e.getMessage(), e);
        }
        return null;
    }

    /*public static String getServiceURL(@NonNull Context context, @NonNull final GlobalConstants.Services svc) {
        final String environment = getSettingsValue(context, "app_environment", "dev");
        final String serviceDomain = getSettingsValue(context, "app_service_url", "http://18.188.102.146");
        final StringBuilder sb = new StringBuilder(serviceDomain);

        // Prod URL can have domain name or ip address with
        // or without TCP port number.
        if ("prod".equalsIgnoreCase(environment)) {
            // -- http://84.15.64.12:8080/ngapi/profile/login/
            // -- http://www.somedomain.com/ngapi/profile/login/
            if (sb.charAt(sb.length()-1) != '/')
                sb.append('/');

            sb.append("ngapi/")
                    .append(svc.getApp())
                    .append(svc.getName())
                    .append('/');

            Log.d(TAG, "Service URL: "+sb.toString());

        } else if ("dev".equalsIgnoreCase(environment)) {
            // -- http://84.15.64.12:8001/login/
            // -- http://84.15.64.12:8002/zipcode/
            if (sb.charAt(sb.length()-1) == '/')
                sb.deleteCharAt(sb.length()-1);

            sb.append(':')
                    .append(svc.getPort())
                    .append(svc.getName());

            Log.d(TAG, "Service URL: "+sb.toString());
        }

        sb.trimToSize();
        return sb.toString();
    }*/

    public static String getSettingsValue(final Context context, final String key, String...defValue) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if ((defValue == null) || (defValue.length <= 0)) {
            return preferences.getString(key, "");
        }
        return preferences.getString(key, defValue[0]);
    }

    public static boolean isPreferenceSwitchedOn(final Context context, final String key, boolean...defValue) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if ((defValue == null) || (defValue.length <= 0)) {
            return preferences.getBoolean(key, false);
        }
        return preferences.getBoolean(key, defValue[0]);
    }


}
