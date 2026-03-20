package cn.com.toolkit.tools.excel.support;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.reactfx.util.Tuple2;
import org.reactfx.util.Tuple3;
import org.reactfx.util.Tuples;

import java.io.*;
import java.util.*;

public class ExcelCopySupport {
    private final Map<Tuple3<Integer,Integer,Short>, CellStyle> styleCacheMap = new HashMap<>();

    /**
     * 将excel中的每个sheet复制到单独的excel中
     * @param source 源文件
     * @param targetDir 目标目录
     * @param sheetNoSet 需转换的sheet
     */
    public void splitSheet(File source, File targetDir, Set<Integer> sheetNoSet){
        try(Workbook sourceWorkbook = WorkbookFactory.create(new FileInputStream(source))) {
            String name = source.getName();
            String baseName = name.substring(0, name.lastIndexOf("."));

            for (int i = 0; i < sourceWorkbook.getNumberOfSheets(); i++) {
                if(sheetNoSet != null && !sheetNoSet.contains(i)) continue;
                try(Workbook targetWorkbook = new XSSFWorkbook()){
                    copyWorkbookStyles(sourceWorkbook, targetWorkbook);
                    Sheet sourceSheet = sourceWorkbook.getSheetAt(i);
                    Sheet targetSheet = targetWorkbook.createSheet(sourceSheet.getSheetName());
                    // 复制工作表级别样式
                    copySheetStyles(sourceSheet, targetSheet);
                    // 复制合并单元格
                    copyMergedRegions(sourceSheet, targetSheet);
                    for (int m = 0; m <= sourceSheet.getLastRowNum(); m++) {
                        Row sourceRow = sourceSheet.getRow(m);
                        Row targetRow = targetSheet.createRow(m);
                        copyRowStyles(sourceRow, targetRow);
                        for (int n = 0; n < sourceRow.getLastCellNum(); n++) {
                            Cell currentCell = sourceRow.getCell(n);
                            Cell targetCell = targetRow.createCell(n);
                            if (currentCell == null) continue;
                            copyCellStyles(currentCell, targetCell);
                            copyCellValue(currentCell, targetCell);
                        }
                    }
                    File target = new File(targetDir, baseName + "_" + i + "_" + sourceSheet.getSheetName() + ".xlsx");
                    try (FileOutputStream fos = new FileOutputStream(target)) {
                        targetWorkbook.write(fos);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    /**
     * 按列拆分
     * @param source 源文件
     * @param targetDir 目标目录
     * @param sheetNoSet 需转换的sheet
     * @param colList 拆分的列
     */
    public void splitCol(File source, File targetDir, Set<Integer> sheetNoSet,List<Tuple2<String,String>> colList){
        try(Workbook sourceWorkbook = WorkbookFactory.create(new FileInputStream(source))) {
            String name = source.getName();
            String baseName = name.substring(0, name.lastIndexOf("."));
            for (int i = 0; i < sourceWorkbook.getNumberOfSheets(); i++) {
                List<Tuple3<Short,Short,Workbook>> outputList = new ArrayList<>();
                for (Tuple2<String, String> cols : colList) {
                    CellReference beginReference = new CellReference(cols.get1().toUpperCase() + 1);
                    Workbook workbook = new XSSFWorkbook();
                    outputList.add(Tuples.t(beginReference.getCol(),
                            cols.get2() == null ? null : new CellReference(cols.get2().toUpperCase() + 1).getCol()
                            ,workbook));
                }
                if(sheetNoSet != null && !sheetNoSet.contains(i)) continue;
                Sheet sourceSheet = sourceWorkbook.getSheetAt(i);
                for (int m = 0; m <= sourceSheet.getLastRowNum(); m++) {
                    Row sourceRow = sourceSheet.getRow(m);
                    for (int n = 0; n < sourceRow.getLastCellNum(); n++) {
                        Cell currentCell = sourceRow.getCell(n);
                        for (Tuple3<Short, Short, Workbook> output : outputList) {
                            if(n < output.get1()) continue;
                            if(output.get2() != null && n > output.get2()) continue;
                            Workbook targetWorkbook = output.get3();
                            Sheet targetSheet = targetWorkbook.getSheet(sourceSheet.getSheetName());
                            if(targetSheet == null) {
                                targetSheet = targetWorkbook.createSheet(sourceSheet.getSheetName());
                                copyWorkbookStyles(sourceWorkbook, targetWorkbook);
                                copySheetStyles(sourceSheet, targetSheet);
                                copyMergedRegions(sourceSheet, targetSheet);
                            }
                            Row targetRow = targetSheet.getRow(m);
                            if(targetRow == null) {
                                targetRow = targetSheet.createRow(m);
                                copyRowStyles(sourceRow, targetRow);
                            }
                            Cell targetCell = targetRow.createCell(n - output.get1());
                            if (currentCell == null) continue;
                            copyCellStyles(currentCell, targetCell);
                            copyCellValue(currentCell, targetCell);

                        }
                    }
                }
                for (Tuple3<Short, Short, Workbook> output : outputList) {
                    File target = new File(targetDir, baseName
                            + "_" + i + "_" + sourceSheet.getSheetName() + "_"
                            + output.get1() + (output.get2() == null ? "" : ( "_" + output.get2()))
                            + ".xlsx");
                    try (Workbook targetWorkbook = output.get3();
                         FileOutputStream fos = new FileOutputStream(target)) {
                        targetWorkbook.write(fos);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 按行分,按行平均分成avg份
     * @param source 源文件
     * @param targetDir 目标目录
     * @param sheetNoSet 需转换的sheet
     * @param avg 份数
     */
    public void splitAvgRow(File source, File targetDir, Set<Integer> sheetNoSet,int avg){
        if(avg <= 0) return;
        try(Workbook sourceWorkbook = WorkbookFactory.create(new FileInputStream(source))) {
            String name = source.getName();
            String baseName = name.substring(0, name.lastIndexOf("."));
            int[] nums = new int[sourceWorkbook.getNumberOfSheets()];
            for (int i = 0; i < sourceWorkbook.getNumberOfSheets(); i++) {
                Sheet sourceSheet = sourceWorkbook.getSheetAt(i);
                nums[i] = (sourceSheet.getLastRowNum() + avg) / avg;
            }
            splitNumRow(sourceWorkbook,baseName,targetDir,sheetNoSet,nums);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 按行分,每rowcount行为一份
     * @param source 源文件
     * @param targetDir 目标目录
     * @param sheetNoSet 需转换的sheet
     * @param num 行数
     */
    public void splitNumRow(File source, File targetDir, Set<Integer> sheetNoSet,int num){
        if(num <= 0) return;
        try(Workbook sourceWorkbook = WorkbookFactory.create(new FileInputStream(source))) {
            String name = source.getName();
            String baseName = name.substring(0, name.lastIndexOf("."));
            int[] nums = new int[sourceWorkbook.getNumberOfSheets()];
            Arrays.fill(nums, num);
            splitNumRow(sourceWorkbook,baseName,targetDir,sheetNoSet,nums);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 合并到一个Excel中的多个Sheet
     * @param sourceList 源文件列表
     * @param target 目标文件
     */
    public void mergeSheet(List<File> sourceList, File target){
        if(sourceList == null || sourceList.isEmpty()) return;
        try(Workbook targetWorkbook = new XSSFWorkbook()){
            for (File source : sourceList) {
                try(Workbook sourceWorkbook = WorkbookFactory.create(new FileInputStream(source))) {
                    copyWorkbookStyles(sourceWorkbook, targetWorkbook);
                    for (int i = 0; i < sourceWorkbook.getNumberOfSheets(); i++) {
                        Sheet sourceSheet = sourceWorkbook.getSheetAt(i);
                        String sheetName = sourceSheet.getSheetName();
                        String targetSheetName = sheetName;
                        int count = 0;
                        while(targetWorkbook.getSheet(targetSheetName) != null)
                            targetSheetName = sheetName + (++count);
                        Sheet targetSheet = targetWorkbook.createSheet(targetSheetName);
                        // 复制工作表级别样式
                        copySheetStyles(sourceSheet, targetSheet);
                        // 复制合并单元格
                        copyMergedRegions(sourceSheet, targetSheet);
                        for (int m = 0; m <= sourceSheet.getLastRowNum(); m++) {
                            Row sourceRow = sourceSheet.getRow(m);
                            Row targetRow = targetSheet.createRow(m);
                            copyRowStyles(sourceRow, targetRow);
                            for (int n = 0; n < sourceRow.getLastCellNum(); n++) {
                                Cell currentCell = sourceRow.getCell(n);
                                Cell targetCell = targetRow.createCell(n);
                                if (currentCell == null) continue;
                                copyCellStyles(currentCell, targetCell);
                                copyCellValue(currentCell, targetCell);
                            }
                        }
                    }
                }
            }
            try (FileOutputStream fos = new FileOutputStream(target)) {
                targetWorkbook.write(fos);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void splitNumRow(Workbook sourceWorkbook,String baseName, File targetDir, Set<Integer> sheetNoSet,int[] nums){
        int sheetNum = sourceWorkbook.getNumberOfSheets();
        boolean[] endFlag = new boolean[sheetNum];
        for(int t = 0; anyFalse(endFlag,sheetNoSet); t++){
            Workbook targetWorkbook = null;
            for (int i = 0; i < sheetNum && !endFlag[i]; i++) {
                if(sheetNoSet != null && !sheetNoSet.contains(i)) continue;
                Sheet sourceSheet = sourceWorkbook.getSheetAt(i);
                int countRowNum = sourceSheet.getLastRowNum();
                int beginRow = t * nums[i];
                Sheet targetSheet = null;
                for(int m = 0; m < nums[i]; m ++ ){
                    if(m + beginRow > countRowNum){
                        endFlag[i] = true;
                        break;
                    }
                    Row sourceRow = sourceSheet.getRow(beginRow + m);
                    if(sourceRow == null) continue;
                    if(targetWorkbook == null){
                        targetWorkbook = new XSSFWorkbook();
                        copyWorkbookStyles(sourceWorkbook, targetWorkbook);
                    }
                    if(targetSheet == null){
                        targetSheet = targetWorkbook.createSheet(sourceSheet.getSheetName());
                        // 复制工作表级别样式
                        copySheetStyles(sourceSheet, targetSheet);
                        // 复制合并单元格
                        copyMergedRegions(sourceSheet, targetSheet);
                    }
                    Row targetRow = targetSheet.createRow(m);
                    copyRowStyles(sourceRow, targetRow);
                    for (int n = 0; n < sourceRow.getLastCellNum(); n++) {
                        Cell currentCell = sourceRow.getCell(n);
                        Cell targetCell = targetRow.createCell(n);
                        if (currentCell == null) continue;
                        copyCellStyles(currentCell, targetCell);
                        copyCellValue(currentCell, targetCell);
                    }
                }
            }
            if(targetWorkbook != null){
                File target = new File(targetDir, baseName + "_" + t + ".xlsx");
                try (FileOutputStream fos = new FileOutputStream(target);
                     Workbook finalTargetWorkbook = targetWorkbook) {
                    finalTargetWorkbook.write(fos);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    /**
     * 复制工作簿所有样式
     */
    private void copyWorkbookStyles(Workbook source, Workbook target) {
        for (int i = 0; i < source.getNumCellStyles(); i++) {
            CellStyle sourceStyle = source.getCellStyleAt(i);
            if(sourceStyle == null) continue;
            Tuple3<Integer,Integer,Short> key = Tuples.t(source.hashCode(),target.hashCode(),sourceStyle.getIndex());
            if(styleCacheMap.containsKey(key)) continue;
            CellStyle targetStyle = target.createCellStyle();
            targetStyle.cloneStyleFrom(sourceStyle);
            styleCacheMap.put(key, targetStyle);
        }
    }
    /**
     * 复制工作表级别样式
     */
    private void copySheetStyles(Sheet source, Sheet target) {
        target.setAutobreaks(source.getAutobreaks());
        target.setDefaultColumnWidth(source.getDefaultColumnWidth());
        target.setDefaultRowHeight(source.getDefaultRowHeight());
        target.setDefaultRowHeightInPoints(source.getDefaultRowHeightInPoints());
        target.setDisplayZeros(source.isDisplayZeros());
        target.setDisplayFormulas(source.isDisplayFormulas());
        target.setDisplayGridlines(source.isDisplayGridlines());
        target.setDisplayGuts(source.isDisplayGridlines());
        target.setDisplayRowColHeadings(source.isDisplayRowColHeadings());
        target.setForceFormulaRecalculation(source.getForceFormulaRecalculation());
        target.setFitToPage(source.getFitToPage());
        target.setHorizontallyCenter(source.getHorizontallyCenter());
        target.setRightToLeft(source.isRightToLeft());
        target.setRowSumsRight(source.getRowSumsRight());
        target.setVerticallyCenter(source.getVerticallyCenter());
        //target.setSelected(source.isSelected());
    }
    /**
     * 复制行级别样式
     */
    private void copyRowStyles(Row source, Row target) {
        target.setHeight(source.getHeight());
        target.setHeightInPoints(source.getHeightInPoints());
        target.setZeroHeight(source.getZeroHeight());
        CellStyle sourceStyle = source.getRowStyle();
        if(sourceStyle == null) return;
        Tuple3<Integer,Integer,Short> key = Tuples.t(source.getSheet().getWorkbook().hashCode(),target.getSheet().getWorkbook().hashCode(),sourceStyle.getIndex());
        if(!styleCacheMap.containsKey(key)) return;
        CellStyle targetStyle = styleCacheMap.get(key);
        target.setRowStyle(targetStyle);
    }
    /**
     * 复制单元格样式
     */
    private void copyCellStyles(Cell source, Cell target) {
        CellStyle sourceStyle = source.getCellStyle();
        if(sourceStyle == null) return;
        Tuple3<Integer,Integer,Short> key = Tuples.t(source.getSheet().getWorkbook().hashCode(),target.getSheet().getWorkbook().hashCode(),sourceStyle.getIndex());
        if(!styleCacheMap.containsKey(key)) return;
        CellStyle targetStyle = styleCacheMap.get(key);
        target.setCellStyle(targetStyle);
    }
    /**
     * 复制合并单元格
     */
    private void copyMergedRegions(Sheet source, Sheet target) {
        for (int i = 0; i < source.getNumMergedRegions(); i++) {
            CellRangeAddress region = source.getMergedRegion(i);
            target.addMergedRegion(region);
        }
    }
    /**
     * 复制单元格值
     */
    private void copyCellValue(Cell source, Cell target) {
        switch (source.getCellType()) {
            case STRING:
                target.setCellValue(source.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(source)) target.setCellValue(source.getDateCellValue());
                else
                    target.setCellValue(source.getNumericCellValue());
                break;
            case BOOLEAN:
                target.setCellValue(source.getBooleanCellValue());
                break;
            case FORMULA:
                target.setCellFormula(source.getCellFormula());
                break;
            case BLANK:
                target.setBlank();
                break;
            case ERROR:
                target.setCellErrorValue(source.getErrorCellValue());
                break;
            default:
                break;
        }
    }
    private boolean anyFalse(boolean[] arr,Set<Integer> sheetNoSet){
        if(sheetNoSet == null) {
            for (boolean b : arr) if(!b) return true;
        }
        else {
            for (Integer i : sheetNoSet)
                if(i >= 0 && i < arr.length && !arr[i]) return true;
        }
        return false;
    }
}
