package org.dotspace.oofp.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PoiUtil {
	public static void excelToCsv(String excelFilePath, String csvFilePath) {
		excelToCsv(excelFilePath, csvFilePath, ",");
	}
	 /**
     * 撠xcel銵冽頧�sv�撘�
     * @param excelFilePath xls, xlsx頝臬��
     * @param csvFilePath csv頝臬��
     * @param separatedSymbol ������
     */
	public static void excelToCsv(String excelFilePath, String csvFilePath, String separatedSymbol){
        String buffer = "";
        Workbook wb = null;
        Sheet sheet = null;
        Row row = null;
        Row rowHead = null;
//        List<Map<String,String>> list = null;
        String cellData = null;
        String filePath = excelFilePath;
 
        wb = readExcel(filePath);
        if(wb != null){
        	for (int i = 0; i < wb.getNumberOfSheets(); i++) {
        		sheet = wb.getSheetAt(i);
                // 璅�蜇��
                rowHead = sheet.getRow(i);
                if (rowHead == null) {
                    continue;
                }
                //蝮賢�colNum
//                int colNum = rowHead.getPhysicalNumberOfCells();
//                String[] keyArray = new String[colNum];
//                Map<String, Object> map = new LinkedHashMap<>();
                
                //�靘�銵其葉����
//                ArrayList<Map<String, String>> list = new ArrayList<Map<String,String>>();
                //���洵銝��heet
                sheet = wb.getSheetAt(i);
                //����憭扯�
                int rownum = sheet.getPhysicalNumberOfRows();
                //���洵銝�銵�
                row = sheet.getRow(0);
                //����憭批�
                int colnum = row.getPhysicalNumberOfCells();
                for (int n = 0; n<rownum; n++) {
                    row = sheet.getRow(n);
                    for (int m = 0; m < colnum; m++) {
     
                        cellData =  getCellFormatValue(row.getCell(m)).toString();
     
                        buffer += cellData;
                    }
                    buffer = buffer.substring(0, buffer.lastIndexOf(",")).toString();
                    buffer += "\n";
     
                }
     
                String savePath = csvFilePath;
                File saveCSV = new File(savePath);               
                try {                	
                	FileOutputStream fos = new FileOutputStream(saveCSV);
                    fos.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                	
                    if(!saveCSV.exists())
                        saveCSV.createNewFile();
                    BufferedWriter writer = new BufferedWriter(osw);
                    writer.write(buffer);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }	
        }
    }
 
    //霈���xcel
    @SuppressWarnings("resource")
	public static Workbook readExcel(String filePath){
        Workbook wb = null;
        if(filePath==null){
            return null;
        }
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if(".xls".equals(extString)){
                return wb = new HSSFWorkbook(is);
            }else if(".xlsx".equals(extString)){
                return wb = new XSSFWorkbook(is);
            }else{
                return wb = null;
            }
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wb;
    }
 
    public static Object getCellFormatValue(Cell cell){
        Object cellValue = null;
        if(cell!=null){
            // ��cell憿��
            switch(cell.getCellType()){
                case NUMERIC:{
                	String cellva = getValue(cell);
                    cellValue = ("\"\t" + cellva + "\"").replaceAll("\n", " ") + ",";
                    break;
                }
                case FORMULA:{
                    // ��cell������撘�
                    if(DateUtil.isCellDateFormatted(cell)){
                        // 頧����撘YYY-mm-dd
                        cellValue = String.valueOf(cell.getDateCellValue()).replaceAll("\n", " ") + ",";
                    }else{
                        // �摮�
                        cellValue = String.valueOf(cell.getNumericCellValue()).replaceAll("\n", " ") + ",";
                    }
                    break;
                }
                case STRING:{
                    cellValue = String.valueOf("\"\t" + cell.getRichStringCellValue() + "\"").replaceAll("\n", " ") + ",";
                    break;
                }
                default:
                    cellValue = ""+ ",";
            }
        }else{
            cellValue = ""+ ",";
        }
        return cellValue;
    }
 
    /**
     * 甇斗瘜����sv��摮���振銝��暺�
     * 憒���閬���矽�甇斗瘜�
     * 
     */
	public static String getValue(Cell hssfCell) {
        if (hssfCell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(hssfCell.getBooleanCellValue());
        } else if (hssfCell.getCellType() == CellType.NUMERIC) {
            Object inputValue = null;
            Long longVal = Math.round(hssfCell.getNumericCellValue());
            Double doubleVal = hssfCell.getNumericCellValue();
            if(Double.parseDouble(longVal + ".0") == doubleVal){   // ��������雿�.0
                inputValue = longVal;
            }
            else{
                inputValue = doubleVal;
            }
            DecimalFormat df = new DecimalFormat("##0.00");    // �甇方��撠暺��嚗�撌梢�瘙���
            return String.valueOf(df.format(inputValue));      // 餈�tring憿��
        } else {
            // 餈��泵銝脤�����
            return String.valueOf(hssfCell.getStringCellValue());
        }
    }
}

