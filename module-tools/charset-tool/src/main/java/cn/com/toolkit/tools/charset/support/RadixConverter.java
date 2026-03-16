package cn.com.toolkit.tools.charset.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * 通用进制转换工具类（支持小数）
 * 支持2-36进制之间的互相转换
 */
public class RadixConverter {

    // 字符集，用于36进制以内的表示
    private static final String DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 通用进制转换（支持小数）
     * @param number 源数字字符串（可包含小数点）
     * @param fromRadix 源进制
     * @param toRadix 目标进制
     * @param precision 小数精度（保留几位）
     */
    public static String convert(String number, int fromRadix, int toRadix, int precision) {
        // 检查进制范围
        if (fromRadix < 2 || fromRadix > 36 || toRadix < 2 || toRadix > 36) {
            throw new IllegalArgumentException("进制必须在2-36之间");
        }

        // 分离整数和小数部分
        String[] parts = number.split("\\.");
        String integerPart = parts[0];
        String fractionalPart = parts.length > 1 ? parts[1] : "";

        // 1. 将源进制转换为十进制（使用BigDecimal保证精度）
        BigDecimal decimalValue = toDecimal(integerPart, fractionalPart, fromRadix);

        // 2. 将十进制转换为目标进制
        return fromDecimal(decimalValue, toRadix, precision);
    }

    /**
     * 任意进制 → 十进制
     */
    private static BigDecimal toDecimal(String integerPart, String fractionalPart, int radix) {
        // 转换整数部分
        BigInteger integerValue = BigInteger.ZERO;
        for (int i = 0; i < integerPart.length(); i++) {
            char c = integerPart.charAt(i);
            int digitValue = DIGITS.indexOf(Character.toUpperCase(c));
            if (digitValue == -1 || digitValue >= radix) {
                throw new IllegalArgumentException("无效的数字: " + c);
            }
            integerValue = integerValue.multiply(BigInteger.valueOf(radix))
                    .add(BigInteger.valueOf(digitValue));
        }

        // 转换小数部分
        BigDecimal fractionalValue = BigDecimal.ZERO;
        if (fractionalPart != null && !fractionalPart.isEmpty()) {
            for (int i = 0; i < fractionalPart.length(); i++) {
                char c = fractionalPart.charAt(i);
                int digitValue = DIGITS.indexOf(Character.toUpperCase(c));
                if (digitValue == -1 || digitValue >= radix) {
                    throw new IllegalArgumentException("无效的数字: " + c);
                }

                // 小数位：digitValue * radix^(-(i+1))
                BigDecimal divisor = BigDecimal.valueOf(radix).pow(i + 1);
                BigDecimal term = BigDecimal.valueOf(digitValue).divide(divisor, 50, RoundingMode.HALF_UP);
                fractionalValue = fractionalValue.add(term);
            }
        }

        // 合并整数和小数部分
        return new BigDecimal(integerValue).add(fractionalValue);
    }

    /**
     * 十进制 → 任意进制
     */
    private static String fromDecimal(BigDecimal decimal, int radix, int precision) {
        // 分离整数和小数部分
        BigInteger integerPart = decimal.toBigInteger();
        BigDecimal fractionalPart = decimal.subtract(new BigDecimal(integerPart));

        // 转换整数部分
        String integerResult;
        if (integerPart.equals(BigInteger.ZERO)) {
            integerResult = "0";
        } else {
            StringBuilder sb = new StringBuilder();
            BigInteger value = integerPart;
            BigInteger radixBI = BigInteger.valueOf(radix);

            while (value.compareTo(BigInteger.ZERO) > 0) {
                BigInteger[] divRem = value.divideAndRemainder(radixBI);
                int remainder = divRem[1].intValue();
                sb.insert(0, DIGITS.charAt(remainder));
                value = divRem[0];
            }
            integerResult = sb.toString();
        }

        // 转换小数部分
        if (fractionalPart.compareTo(BigDecimal.ZERO) == 0 || precision <= 0) {
            return integerResult;
        }

        StringBuilder fractionalResult = new StringBuilder();
        BigDecimal fractional = fractionalPart;
        BigDecimal radixBD = BigDecimal.valueOf(radix);

        for (int i = 0; i < precision; i++) {
            if (fractional.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }

            // 小数部分乘以基数
            fractional = fractional.multiply(radixBD);

            // 取整数部分作为当前位
            BigInteger digit = fractional.toBigInteger();
            fractionalResult.append(DIGITS.charAt(digit.intValue()));

            // 保留新的小数部分
            fractional = fractional.subtract(new BigDecimal(digit));
        }

        return integerResult + "." + fractionalResult;
    }

    /**
     * 简便方法：不指定精度时使用默认精度
     */
    public static String convert(String number, int fromRadix, int toRadix) {
        return convert(number, fromRadix, toRadix, 10);
    }
}
