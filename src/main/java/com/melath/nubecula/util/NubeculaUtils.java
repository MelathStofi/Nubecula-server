package com.melath.nubecula.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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


    public static <T> Collector<T, ?, T> toSingleton(RuntimeException e) {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw e;
                    }
                    return list.get(0);
                }
        );
    }

}
