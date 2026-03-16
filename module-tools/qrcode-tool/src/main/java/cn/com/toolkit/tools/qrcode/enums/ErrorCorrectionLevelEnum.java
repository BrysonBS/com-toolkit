package cn.com.toolkit.tools.qrcode.enums;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCorrectionLevelEnum {
    L( "~7%",ErrorCorrectionLevel.L),
    M( "~15%",ErrorCorrectionLevel.M),
    Q( "~25%",ErrorCorrectionLevel.Q),
    H( "~30%",ErrorCorrectionLevel.H),
    ;
    private final String displayName;
    private final ErrorCorrectionLevel value;


    @Override
    public String toString() {
        return this.displayName;
    }
}
