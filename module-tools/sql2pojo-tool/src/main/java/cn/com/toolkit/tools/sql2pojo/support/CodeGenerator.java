package cn.com.toolkit.tools.sql2pojo.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CodeGenerator {
    private static final String NEW_LINE = System.lineSeparator();
    private static final Map<String, String> IMPORT_MAPPING = new HashMap<>();
    private static final Map<String, String> TYPE_MAPPING = new HashMap<>();
    static {
        // JAVA类型导入
        IMPORT_MAPPING.put("BigDecimal", "java.math.BigDecimal");
        IMPORT_MAPPING.put("LocalDate", "import java.time.LocalDate");
        IMPORT_MAPPING.put("LocalTime", "java.time.LocalTime");
        IMPORT_MAPPING.put("LocalDateTime", "java.time.LocalDateTime");

        // SQL类型到Java类型的映射
        TYPE_MAPPING.put("VARCHAR", "String");
        TYPE_MAPPING.put("VARCHAR2", "String");
        TYPE_MAPPING.put("NVARCHAR", "String");
        TYPE_MAPPING.put("CHAR", "String");
        TYPE_MAPPING.put("TEXT", "String");
        TYPE_MAPPING.put("LONGTEXT", "String");
        TYPE_MAPPING.put("JSON", "String");
        TYPE_MAPPING.put("UUID", "String");
        TYPE_MAPPING.put("UNIQUEIDENTIFIER", "String");
        TYPE_MAPPING.put("INT", "Integer");
        TYPE_MAPPING.put("INTEGER", "Integer");
        TYPE_MAPPING.put("BIGINT", "Long");
        TYPE_MAPPING.put("SERIAL", "Long");
        TYPE_MAPPING.put("SMALLINT", "Short");
        TYPE_MAPPING.put("TINYINT", "Byte");
        TYPE_MAPPING.put("DECIMAL", "BigDecimal");
        TYPE_MAPPING.put("NUMERIC", "BigDecimal");
        TYPE_MAPPING.put("NUMBER", "BigDecimal");
        TYPE_MAPPING.put("DOUBLE", "Double");
        TYPE_MAPPING.put("REAL", "Double");
        TYPE_MAPPING.put("FLOAT", "Float");
        TYPE_MAPPING.put("DATE", "LocalDate");
        TYPE_MAPPING.put("TIME", "LocalTime");
        TYPE_MAPPING.put("TIMESTAMP", "LocalDateTime");
        TYPE_MAPPING.put("DATETIME", "LocalDateTime");
        TYPE_MAPPING.put("DATETIME2", "LocalDateTime");
        TYPE_MAPPING.put("BOOLEAN", "Boolean");
        TYPE_MAPPING.put("BOOL", "Boolean");
        TYPE_MAPPING.put("BIT", "Boolean");
        TYPE_MAPPING.put("BLOB", "byte[]");
        TYPE_MAPPING.put("BYTEA", "byte[]");
        TYPE_MAPPING.put("CLOB", "String");
    }
    public static String generatePojo(String packageName, String className, Map<String, String> columnMap) {
        StringBuilder code = new StringBuilder();
        // 添加包名
        if (packageName != null && !packageName.isEmpty()) {
            code.append("package ").append(packageName).append(";").append(NEW_LINE);
        }
        // 添加Lombok导入
        code.append("import lombok.Data;").append(NEW_LINE);
        // 添加必要的其他类型导入
        code.append(generateImports(columnMap));
        // @Data注解
        code.append("@Data").append(NEW_LINE);
        // 类定义
        code.append("public class ").append(className).append(" {").append(NEW_LINE);
        // 生成字段
        for (Map.Entry<String, String> entry : columnMap.entrySet()) {
            String fieldName = toCamelCase(entry.getKey());
            String javaType = getJavaType(entry.getValue());
            code.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n");
        }
        code.append("}").append(NEW_LINE);
        return code.toString();
    }
    public static String generateMapperXmlWithResultMap( String pojoClassName, Map<String, String> columnMap) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \n");
        xml.append("    \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n\n");
        xml.append("<mapper namespace=\"").append(pojoClassName).append("Mapper\">\n\n");

        // 生成 resultMap
        xml.append(generateResultMap(pojoClassName, columnMap));
        xml.append("\n\n");

        // 生成 Base_Column_List
        xml.append("    <sql id=\"Base_Column_List\">\n");
        xml.append("        ");
        int i = 0;
        for (String column : columnMap.keySet()) {
            if (i++ > 0) xml.append(", ");
            xml.append(column);
        }
        xml.append("\n    </sql>\n\n");

        xml.append("</mapper>");

        return xml.toString();
    }
    public static String generateResultMap(String pojoClassName, Map<String, String> columnMap) {
        StringBuilder resultMap = new StringBuilder();
        String idColumn = null;
        for (Map.Entry<String, String> entry : columnMap.entrySet()) {
            String column = entry.getKey();
            String jdbcType = convertToJdbcType(entry.getValue());
            String property = convertToPropertyName(column);
            if ((column.equalsIgnoreCase("id"))) {
                idColumn  = "    <id column=\"" + column + "\" property=\"" + property + "\"/>" + NEW_LINE;
            } else {
                resultMap.append("    <result column=\"").append(column)
                        .append("\" property=\"").append(property)
                        .append("\"/>")
                        .append(NEW_LINE);
            }
        }
        if(idColumn != null) resultMap.insert(0,idColumn);
        resultMap.insert(0,NEW_LINE).insert(0,"\">").insert(0,pojoClassName)
                .insert(0,"<resultMap id=\"BaseResultMap\" type=\"");
        resultMap.append("</resultMap>");

        return resultMap.toString();
    }
    private static String generateImports(Map<String, String> columnMap) {
        HashSet<String> importsSet = new HashSet<>();
        for (String sqlType : columnMap.values()) {
            String javaType = TYPE_MAPPING.get(sqlType.toUpperCase());
            if(javaType == null) continue;
            if(IMPORT_MAPPING.containsKey(javaType))
                importsSet.add(IMPORT_MAPPING.get(javaType) + ";" + NEW_LINE);
        }
        return String.join("", importsSet) + NEW_LINE;
    }
    private static String toCamelCase(String columnName) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;

        for (char c : columnName.toLowerCase().toCharArray()) {
            if (c == '_' || c == '-') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else result.append(c);
            }
        }
        return result.toString();
    }
    private static String getJavaType(String sqlType) {
        return TYPE_MAPPING.getOrDefault(sqlType.toUpperCase(), "Object");
    }

    private static String convertToPropertyName(String columnName) {
        StringBuilder result = new StringBuilder();
        boolean toUpperCase = false;
        for (char c : columnName.toCharArray()) {
            if (c == '_') {
                toUpperCase = true;
            } else {
                if (toUpperCase) {
                    result.append(Character.toUpperCase(c));
                    toUpperCase = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }
    private static String convertToJdbcType(String dbType) {
        if (dbType == null) return "VARCHAR";

        return switch (dbType.toUpperCase()) {
            case "VARCHAR", "VARCHAR2", "NVARCHAR", "JSON", "CHAR", "TEXT", "LONGTEXT" -> "VARCHAR";
            case "INT", "INTEGER" -> "INTEGER";
            case "BIGINT" -> "BIGINT";
            case "SMALLINT" -> "SMALLINT";
            case "TINYINT" -> "TINYINT";
            case "DECIMAL", "NUMERIC" -> "DECIMAL";
            case "FLOAT" -> "FLOAT";
            case "DOUBLE" -> "DOUBLE";
            case "DATE" -> "DATE";
            case "TIME" -> "TIME";
            case "TIMESTAMP", "DATETIME" -> "TIMESTAMP";
            case "BOOLEAN", "BIT" -> "BOOLEAN";
            case "BLOB" -> "BLOB";
            case "CLOB" -> "CLOB";
            default -> "OTHER";
        };
    }
}
