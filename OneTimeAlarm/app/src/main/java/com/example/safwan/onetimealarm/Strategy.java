package com.example.safwan.onetimealarm;

/**
 * Created by safwan on 25/12/2017.
 */

// contains misc algorithms

public class Strategy {

    Strategy(){};

    public static String repDays(int[] arr) {
        String tvDayStr = "";
        String colorOpenTag = "<font color=";
        String colorCloseTag = "</font>";
        String colorActive = "#045637>";
        String colorInactive = "#A9A9A9>";

        for(int i = 0; i < arr.length; i++) {
            tvDayStr += colorOpenTag;

            if(arr[i] == 1) {
                tvDayStr += colorActive;
            } else {
                tvDayStr += colorInactive;
            }

            tvDayStr += getStringDays(i) + "    "+ colorCloseTag + "    ";
        }
        System.out.println("TV day "+tvDayStr);
        return tvDayStr;
    }

    // helper
    private static  String getStringDays(int n) {
        switch(n) {
            case 0:

                return "S";
            case 1:

                return "M";
            case 2:

                return "T";
            case 3:

                return "W";
            case 4:

                return "T";
            case 5:

                return "F";
            case 6:

                return "S";
            default:

                return "";
        }
    }
}
