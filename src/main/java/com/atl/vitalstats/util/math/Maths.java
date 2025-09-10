package com.atl.vitalstats.util.math;

public class Maths {
    public static double truncate(double number, int places) {
        return Math.floor(number * Math.pow(10, places)) / Math.pow(10, places);
    }
}
