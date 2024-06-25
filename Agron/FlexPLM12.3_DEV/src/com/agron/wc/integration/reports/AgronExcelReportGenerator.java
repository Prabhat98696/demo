package com.agron.wc.integration.reports;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFDrawing;
import org.apache.poi.xssf.streaming.SXSSFPicture;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookType;

import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.document.ImageUtil;
import com.lcs.wc.util.FormatHelper;

import wt.util.WTException;

public class AgronExcelReportGenerator {
	
	private static final Logger LOGGER = LogManager.getLogger(AgronExcelReportGenerator.class);
	protected static int MAX_ROWS_PER_SHEET=1000000;
	static HashMap<String,Integer> wbImageToPictureIndexMap=new HashMap<>();
	
	
	public static String generateExcel(String  filePath,Collection rows,Collection<TableColumn> tableColumns) throws IOException, WTException{
		LOGGER.info("generateExcelReport STARTED:::file>>"+filePath+"::Total Rrecords>>"+rows.size()+"::Columns>>"+tableColumns.size());
		try(SXSSFWorkbook wb=setupWorkBook() ){
			wbImageToPictureIndexMap=new HashMap<>();
			SXSSFSheet sheet=setUpSheet(wb, tableColumns);
			sheet=addDataToSheet(wb,sheet,tableColumns,rows);
			writeWorkBookToFile(wb,filePath);
		}
		LOGGER.info("generateExcelReport COMPLETED>>>");
		return filePath;

	}
	
	public static SXSSFWorkbook setupWorkBook() throws IOException {
		LOGGER.info("setupWorkBook START>>>>>>>");//XSSFWorkbookType.XLSX
        XSSFWorkbook wb_template = new XSSFWorkbook(XSSFWorkbookType.XLSX);
        SXSSFWorkbook wb = new SXSSFWorkbook(wb_template); 
        wb.setCompressTempFiles(true);
    	LOGGER.info("setupWorkBook END>>>>>>>");
        return wb;
	}
	
	public static void  writeWorkBookToFile( SXSSFWorkbook wb,String fileName ) throws IOException {
		LOGGER.info("writeWorkBookToFile START>>>>>>>"+fileName);
		
		 try (FileOutputStream out = new FileOutputStream(fileName)) {
			wb.write(out);
		 }
		LOGGER.info("writeWorkBookToFile END>>>>>>>");
	}
	
	public static SXSSFSheet setUpSheet( SXSSFWorkbook wb,Collection columns) throws WTException, IOException{
		LOGGER.info("setUpSheet START>>>>>>>"+wb);
		SXSSFSheet sheet=wb.createSheet();
	  	sheet.setRandomAccessWindowSize(100);
	  	//sheet.trackAllColumnsForAutoSizing();
		setUpHeaderColumns(sheet,columns);
		LOGGER.info("setUpSheet END>>>>>>>::sheet::"+sheet.getSheetName());
	    return sheet;
	}
	
		
	public static SXSSFSheet addDataToSheet(SXSSFWorkbook wb,SXSSFSheet sh,Collection<TableColumn> columns,Collection<Map> rows) throws IOException, WTException{

		if(wb==null) {
			sh=setUpSheet(wb, columns);
		}
		LOGGER.info("addDataToSheet START::: Rows:"+rows.size()+"::Columns>>"+columns.size()+"::sheet:"+sh.getSheetName());
		try {
			if(rows.size()>0 && columns.size()>0){
				int rownum = 1;
				for (Map<String, Object> rowMap : rows) {
					if(rownum%10000==0) {
						LOGGER.info("RowNumber>>"+rownum);
					}
					if(sh.getPhysicalNumberOfRows()>MAX_ROWS_PER_SHEET) {
						autoSizeColumns(sh,columns.size());
						sh=setUpSheet(wb, columns);
					}
					Row row = sh.createRow(sh.getLastRowNum()+1);
					//row.setHeight((short) 800);
					int cellnum = 0;
					for (TableColumn column : columns) {
						Cell cell = row.createCell(cellnum++); 
						String columnValue = "";
						String tableIndex=column.getTableIndex();
						if(rowMap.containsKey(tableIndex) && rowMap.get(tableIndex) !=null){
							columnValue= String.valueOf(rowMap.get(tableIndex)).trim();
							if("SUPPLIERNAME".equalsIgnoreCase(tableIndex) && "PLACEHOLDER".equalsIgnoreCase(columnValue)) {
								columnValue="";
							}
						}
						if(column.isImage()) {
							if(addImage(wb,sh,columnValue,cellnum,sh.getLastRowNum())) {
								row.setHeight((short)800);
							}
						}else {
							if("NUMERIC".equalsIgnoreCase(column.getAttributeType()) && FormatHelper.hasContent(columnValue) ) {
							try {
								if(columnValue.contains(".")) {
									Double doubleColumnValue= Double.parseDouble(columnValue);
									cell.setCellValue(doubleColumnValue);
								}else {
									Long longColumnValue= Long.parseLong(columnValue);
									cell.setCellValue(longColumnValue);
								}
							}catch(Exception e) {
								cell.setCellValue(columnValue== null ? "" : columnValue);
							}
							
							}else {
								columnValue=htmlDecode(columnValue);
								cell.setCellValue(columnValue== null ? "" : columnValue); 
							}
							
						}
					}
					rownum++;
				}
				//autoSizeColumns(sh,columns.size());
			}

		}catch(Exception e) {
			LOGGER.error("Error in excel generation >>>"+e.getMessage(),e);
			e.printStackTrace();
		}

		LOGGER.info("addDataToSheet END::: Rows:"+rows.size()+"::Columns>>"+columns.size()+"::sheet:"+sh.getSheetName());
	  return sh;

	}
	
    
	public static void setUpHeaderColumns(SXSSFSheet sheet,Collection<TableColumn> columns) throws WTException, IOException{
		LOGGER.info("setUpHeaderColumns START>>>");
		if(sheet !=null && columns.size()>0) {
			Row row = sheet.createRow(0);
			int cellnum = 0;
			for (TableColumn column : columns) {
				Cell cell = row.createCell(cellnum);
				if("NUMERIC".equalsIgnoreCase(column.getAttributeType())) {
					cell.setCellType(CellType.NUMERIC);
				}
				cell.setCellValue(column.getHeaderLabel());
				cellnum++;
			}

		}
		LOGGER.info("setUpHeaderColumns END>>>");
	}
	
	
	
	public static boolean addImage(SXSSFWorkbook workbook,SXSSFSheet sheet ,String imageURL,int colIndex,int rowIndex) {
		boolean pictureAdded=false;
		 int pictureIdx=0;
		 try {

			 Optional<File> image_file = ImageUtil.getFileFromURL(imageURL);
			 if (image_file.isPresent()) {
				 String imagePath = ((File)image_file.get()).getAbsolutePath();
				 if(wbImageToPictureIndexMap != null && wbImageToPictureIndexMap.containsKey(imagePath)) {
					 pictureIdx=wbImageToPictureIndexMap.get(imagePath);
				 }else {
					 InputStream image = new FileInputStream(imagePath);
					 byte[] bytes = IOUtils.toByteArray(image);
					 pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
					 image.close();  
					 wbImageToPictureIndexMap.put(imagePath, pictureIdx);
				 }

				 SXSSFDrawing drawing = (SXSSFDrawing) sheet.createDrawingPatriarch();
				 XSSFClientAnchor anchor = new XSSFClientAnchor();
				 anchor.setCol1(colIndex-1); //Column B
				 anchor.setRow1(rowIndex); //Row 3
				 anchor.setCol2(colIndex); //Column C
				 anchor.setRow2(rowIndex+1); //Row 4
				 SXSSFPicture picture = drawing.createPicture(anchor, pictureIdx);
				 pictureAdded=true;

			 }


		 } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       return pictureAdded;
	}
	

	
	
	
	  public static void main(String[] args) throws Throwable {
	    	Instant start= Instant.now();
	    	String REPORT_COLUMNS="column1,image,n1,n2";
	    	
	        Collection<TableColumn> tableColumns=new ArrayList();
	         List<String> columns=new ArrayList();
	    	columns=new ArrayList(Arrays.asList(REPORT_COLUMNS.toUpperCase().split(",")));
	    	columns.forEach(column->{
				String tableColumArray[]=column.split(":");
			    String columnKey=tableColumArray[0].trim().toUpperCase();
			    String columnDispalyValue=columnKey;
				if(tableColumArray.length>1 && FormatHelper.hasContent(tableColumArray[1]) ){
					columnDispalyValue=tableColumArray[1];
				}
				TableColumn tColumn=new TableColumn(columnKey,columnDispalyValue);
				if(columnKey.contains("IMAGE")) {
					tColumn.setImage(true);
				}
				tableColumns.add(tColumn);
				
			});
	  	  Map row=new HashMap();
		  Collection rows=new ArrayList();
		  int i=0;
		  while(i<2) {
			  i++;
			   row=new HashMap();
				for (TableColumn column : tableColumns) {
					  row.put(column.getTableIndex(),column.getTableIndex() );
	        	}
				rows.add(row);
			   System.out.println(i+"::"+row);
	
		  }
	
		SimpleDateFormat sdfDestination = new SimpleDateFormat("mmddyyyyhhmmss");
		 String fileId= sdfDestination.format(new Date());
		 generateExcel(fileId+".xlsx",rows,tableColumns);
	    System.out.println("out::"+Duration.between(start, Instant.now()).toMinutes());
	}
	   
		  
	  public static String htmlDecode(String s) {
		  if(FormatHelper.hasContent(s) && FormatHelper.isHtmlEncoded(s)) {
			  s = s.replace("&nbsp;", " ");
			  s = s.replace("&quot;", "\"");
			  s = s.replace("&apos;", "'");
			  s = s.replace("&#39;", "'");
			  s = s.replace("&lt;", "<");
			  s = s.replace("&gt;", ">");
			  s = s.replace("&amp;", "&");	
		  }
		  return s;
	  }  
	  
	  public static void autoSizeColumns(SXSSFSheet sheet,int columns) {
		  LOGGER.info("autoSizeColumns START>>>");
		  for(int i=0;i<columns;i++) {
			  sheet.autoSizeColumn(i);  
		  }
		  LOGGER.info("autoSizeColumns END>>>");
	  }
	
	  
}
