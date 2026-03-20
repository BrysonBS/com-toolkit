package cn.com.toolkit.framework.core.util;

import cn.com.toolkit.framework.core.enums.DatePatternEnum;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class ToolKitUtil {
    public static <T> String dateToString(T date, DatePatternEnum datePatternEnum) {
        if(date instanceof Date d){
            SimpleDateFormat sdf = new SimpleDateFormat(datePatternEnum.getPattern());
            return sdf.format(d);
        }
        else if(date instanceof LocalDate localDate){
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePatternEnum.getPattern());
            return dateTimeFormatter.format(localDate);
        }
        else if(date instanceof LocalDateTime localDateTime){
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePatternEnum.getPattern());
            return dateTimeFormatter.format(localDateTime);
        }
        throw new RuntimeException("非日期对象!");
    }
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    private static boolean isWin() {
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows");
    }
    private boolean isMac() {
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("mac");
    }
}
