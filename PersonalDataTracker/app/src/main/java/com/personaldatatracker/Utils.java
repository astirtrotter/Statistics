package com.personaldatatracker;

import android.content.Context;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static class Param {
        public final static int YEAR = 0;
        public final static int MONTH = 1;
        public final static int DAY = 2;
    };

    public static int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static String convertDateToString(Date date, String format) {
        if (date == null)
            return "";

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String convertDateToString(Date date) {
        return convertDateToString(date, "yyyy-MM-dd");
    }

    public static Date convertStringToDate(String string) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getParamFromDate(Date date, int param) {

        String dateString = convertDateToString(date);
        String paramStrings[] = dateString.split("-");

        return Integer.parseInt(paramStrings[param]);
    }

    public static String convertValueToString(float value, String unit) {
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setPositiveSuffix(" " + unit);
        return decimalFormat.format(value);
//        return String.format("%.0f", value) + " " + unit;
    }

}
