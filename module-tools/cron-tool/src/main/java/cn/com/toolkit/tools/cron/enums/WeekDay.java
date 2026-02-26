package cn.com.toolkit.tools.cron.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WeekDay {
    SUNDAY(1, "日"),
    MONDAY(2, "一"),
    TUESDAY(3, "二"),
    WEDNESDAY(4, "三"),
    THURSDAY(5, "四"),
    FRIDAY(6, "五"),
    SATURDAY(7, "六");

    private final int value;
    private final String display;
    public static WeekDay valueOf(int value) {
        for (WeekDay day : values()) {
            if (day.value == value) return day;
        }
        throw new IllegalArgumentException("无效的星期值: " + value);
    }

    @Override
    public String toString() {
        return display;
    }
}
