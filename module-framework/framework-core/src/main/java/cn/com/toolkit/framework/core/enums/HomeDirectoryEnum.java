package cn.com.toolkit.framework.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HomeDirectoryEnum {
    DOCUMENTS("Documents"),
    DOWNLOADS("Downloads"),
    PICTURES("Pictures"),
    MUSIC("Music"),
    VIDEOS("Videos"),
    DESKTOP("Desktop"),
    PUBLIC("Public"),

    ;
    private final String value;
}
