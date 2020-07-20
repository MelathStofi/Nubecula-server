package com.melath.nubecula.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NubeculaUtils {

    public static String getSizeString(long size) {
        if (size >= 1000L) {
            if (size >= 1000000L) {
                if (size >= 1000000000L) {
                    return BigDecimal.valueOf(size).divide(BigDecimal.valueOf(1000000000), 1, RoundingMode.HALF_UP) + "GB";
                }
                return BigDecimal.valueOf(size).divide(BigDecimal.valueOf(1000000), 1, RoundingMode.HALF_UP) + "MB";
            }
            return BigDecimal.valueOf(size).divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP) + "KB";
        }
        return size + "B";
    }

}
