package com.agron.wc.integration.reports.pdx;


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
import java.util.Vector;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.ss.usermodel.Header;
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

public class AgronPDXExcelReportGenerator {
	
	private static final Logger LOGGER = LogManager.getLogger(AgronPDXExcelReportGenerator.class);
	protected static int MAX_ROWS_PER_SHEET=1000000;
	static HashMap<String,Integer> wbImageToPictureIndexMap=new HashMap<>();
	
	
	public static String generateExcel(String  filePath,HashMap rows,Collection<TableColumn> tableColumns) throws IOException, WTException{
		LOGGER.info("generateExcelReport STARTED:::file>>"+filePath+"::Total Rrecords>>"+rows.size()+"::Columns>>"+tableColumns.size());
		System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet ----------generateExcel---------");
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
	  	//sheet.setRandomAccessWindowSize(10);
	  	sheet.trackAllColumnsForAutoSizing();
		Header header = sheet.getHeader();  
       // header.setCenter("PDX Report");  
        // header.setLeft  ("PDX Report");  
        // header.setRight ("Right align Header"); 
		//sheet.createRow(100);	
		  // Creating Row  
        setUpHeaderColumns(sheet,columns);
		sheet.trackAllColumnsForAutoSizing();
		LOGGER.info("setUpSheet END>>>>>>>::sheet::"+sheet.getSheetName());
	    return sheet;
	}
	
		
	public static SXSSFSheet addDataToSheet(SXSSFWorkbook wb,SXSSFSheet sh,Collection<TableColumn> columns,HashMap rows) throws IOException, WTException{
		System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -------------------");
		if(wb==null) {
			sh=setUpSheet(wb, columns);
		}
		LOGGER.info("addDataToSheet START::: Rows:"+rows.size()+"::Columns>>"+columns.size()+"::sheet:"+sh.getSheetName());
		try {
			System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----row--------------"+ rows.size());
			
			//int cellnum = 0;
			int k=1;
			
			int rownum = 0;
			if(rownum%10000==0) {
				LOGGER.info("RowNumber>>"+rownum);
			}
			if(sh.getPhysicalNumberOfRows()>MAX_ROWS_PER_SHEET) {
				autoSizeColumns(sh,columns.size());
				sh=setUpSheet(wb, columns);
			}
			if (rows.containsKey("PRODUCT")){
			Vector productData = (Vector)rows.get("PRODUCT");
			System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----productData-->"+ productData.size());
				if(productData.size()>0 && columns.size()>0){
					for (int i=0;i<productData.size();i++)
					{
						System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----productData-----i--------"+ i);
						//int rownum = 0;
						HashMap<String, String>  rowMap = (HashMap)productData.get(i);
						Row row = sh.createRow(sh.getLastRowNum()+1);
						int cellnum = 0;
						for (TableColumn column : columns) {
							Cell cell = row.createCell(cellnum++);
							String columnValue = "";
							String tableIndex=column.getTableIndex();
							if(rowMap.containsKey(tableIndex) && rowMap.get(tableIndex) !=null){	
								if (tableIndex.equals("NO"))
								{
									columnValue = String.valueOf(k);
									cell.setCellValue(columnValue== null ? "" : columnValue); 
									k=k+1;
								}else{
									columnValue= String.valueOf(rowMap.get(tableIndex)).trim();
									cell.setCellValue(columnValue== null ? "" : columnValue); 
								}
								
							}
								
						}
							
					}
				}
			}
			if (rows.containsKey("SKU")){
				Vector skuData = (Vector)rows.get("SKU");
				System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----skuData-->"+skuData.size());
				if(skuData.size()>0 && columns.size()>0){
					for (int i=0;i<skuData.size();i++)
					{
						System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----skuData-----i--------"+ i);
						//int rownum = 0;
						HashMap<String, String>  rowMap = (HashMap)skuData.get(i);
					/* 	if(rownum%10000==0) {
							LOGGER.info("RowNumber>>"+rownum);
						}
						if(sh.getPhysicalNumberOfRows()>MAX_ROWS_PER_SHEET) {
							autoSizeColumns(sh,columns.size());
							sh=setUpSheet(wb, columns);
						} */
						//System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----productData-----1--------");
						Row row = sh.createRow(sh.getLastRowNum()+1);
						int cellnum = 0;
						for (TableColumn column : columns) {
								System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----inside for-----begin--------");
							Cell cell = row.createCell(cellnum++);
							String columnValue = "";
							String tableIndex=column.getTableIndex();
							if(rowMap.containsKey(tableIndex) && rowMap.get(tableIndex) !=null){
								System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----inside for-----end----tableIndex----"+tableIndex);		
								if (tableIndex.equals("NO"))
								{
									columnValue = String.valueOf(k);
									cell.setCellValue(columnValue== null ? "" : columnValue); 
									System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----inside for-----end----k columnValue----"+columnValue);
									k=k+1;
								}else{
									columnValue= String.valueOf(rowMap.get(tableIndex)).trim();
									System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----inside for-----end----columnValue----"+columnValue);
									cell.setCellValue(columnValue== null ? "" : columnValue); 
								}
								
							}
								
						}
					}
				}
			}
			if (rows.containsKey("PSLINK")){
				Vector pslinkData = (Vector)rows.get("PSLINK");
				System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----pslinkData-->"+pslinkData.size());
				if(pslinkData.size()>0 && columns.size()>0){
					for (int i=0;i<pslinkData.size();i++)
					{
						System.out.println("In AgronPDXExcelReportGeneratoraddDataToSheet -----pslinkData-----i--------"+ i);
						HashMap<String, String>  rowMap = (HashMap)pslinkData.get(i);
						Row row = sh.createRow(sh.getLastRowNum()+1);
						int cellnum = 0;
						for (TableColumn column : columns) {
							Cell cell = row.createCell(cellnum++);
							String columnValue = "";
							String tableIndex=column.getTableIndex();
							if(rowMap.containsKey(tableIndex) && rowMap.get(tableIndex) !=null){	
								if (tableIndex.equals("NO"))
								{
									columnValue = String.valueOf(k);
									cell.setCellValue(columnValue== null ? "" : columnValue); 
									k=k+1;
								}else{
									columnValue= String.valueOf(rowMap.get(tableIndex)).trim();
									cell.setCellValue(columnValue== null ? "" : columnValue); 
								}
								
							}
								
						}
					}	
				}
			}
			autoSizeColumns(sh,columns.size());
			
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
			
	  	  HashMap row=new HashMap();
		  //Collection rows=new ArrayList();
		  Vector rows = new Vector ();
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
		 //generateExcel(fileId+".xlsx",rows,tableColumns);
		 generateExcel(fileId+".xlsx",row,tableColumns);
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
