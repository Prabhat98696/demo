package com.agron.wc.integration.util;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.*;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.*;
import com.lcs.wc.flextype.datatypes.FlexUOM;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.load.LoadCommon;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.placeholder.Placeholder;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.util.*;
import com.ptc.core.meta.common.DataTypesUtility;
import com.ptc.core.meta.common.FloatingPoint;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.text.*;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import wt.fc.WTObject;
import wt.session.SessionHelper;
import wt.util.*;

/**
 * @author Mallikarjuna Savi
 *
 */
public class AgronCSVGenerator extends TableGenerator {

	private static final String fileEncoding = LoadCommon.getFileEncoding();
	public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset",
			"UTF-8");
	private static final boolean DEBUG = LCSProperties.getBoolean("com.lcs.wc.client.web.CSVExcelGenerator.verbose");
	private static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.util.erp");
	String reportName;
	static String downloadLocation = "";
	static String tempDownloadLocation;
	private ClientContext context;
	private static final String WORK_KEY = LCSProperties.get("com.agron.wc.interface.attributes.product.workKey");
	private static final String ADDEMPTY_ROW = LCSProperties.get("com.agron.wc.interface.addEmptyRow", "true");
	
	public static final String ADIDAS_ARTICLE_FLEXTYPE_PATH = LCSProperties.get("com.agron.wc.interface.adidasArticleOverrride.FlexTypePath");
	public static final String BUS_OBJECT_NAME = LCSProperties.get("com.agron.wc.interface.busObj.objectName").trim();
	public static final String MOA_ADIDAS_ARTICLE_OBJ_KEY = LCSProperties.get("com.agron.wc.interface.moa.AdidasArticleNumberObj").trim();
	public static final String MOA_ADIDAS_ARTICLE_NUMBER_KEY = LCSProperties.get("com.agron.wc.interface.moa.AdidasArticleNumber").trim();
	public static final String MOA_UPC_NUMBER_KEY = LCSProperties.get("com.agron.wc.interface.moa.upcNumber").trim();
	
	public CSVTableHeaderGenerator cthg;
	private boolean addEmptyRow;

	static {
		tempDownloadLocation = "";
		try {
			tempDownloadLocation = FormatHelper
					.formatOSFolderLocation(LCSProperties.get("com.lcs.wc.client.web.CSVGenerator.exportLocation"));
			WTProperties properties = WTProperties.getLocalProperties();
			String wt_home = properties.getProperty("wt.home");
			downloadLocation = (new StringBuilder()).append(wt_home).append(tempDownloadLocation).toString();
		} catch (Exception exception) {
		}
	}

	public String drawTable(Collection<?> data, Collection<?> columns, ClientContext context, String reportName,
			boolean printHeaders) {
		this.context = context;
		this.reportName = reportName;
		return drawTable(data, columns, printHeaders, false);
	}
	
	public String drawTable(Collection<?> data, Collection<?> columns, ClientContext context, String reportName,
			boolean printHeaders, boolean addEmptyRow) {
		this.context = context;
		this.reportName = reportName;
		this.addEmptyRow = addEmptyRow;
		return drawTable(data, columns, printHeaders, addEmptyRow);
	}

	public String drawTable(Collection data, Collection columns, boolean printHeaders,  boolean addEmptyRow) {
		setTableId();
		this.data = data;
		this.columns = columns;
		StringBuffer buffer = new StringBuffer();
		String tid = getTableId();
		buffer.append(drawHeader());
		if (FormatHelper.hasContent(rowIdIndex) && this.columns != null && this.columns.size() > 0) {
			for (Iterator<?> i = this.columns.iterator(); i.hasNext(); ((TableColumn) i.next())
					.setRowIdIndex(rowIdIndex))
				;
		}
		if (printHeaders)
			buffer.append(drawContentColumnHeaders((new StringBuilder()).append("T").append(tid).toString(), false));
		if (addEmptyRow)
			buffer.append(drawContentColumnHeaders((new StringBuilder()).append("T").append(tid).toString(), true));
		if (DEBUG)
			LOGGER.info("---before drawContentData()");
		buffer.append(drawContentData());
		if (DEBUG)
			LOGGER.info("---after drawContentData()");
		return printFile(buffer.toString());
	}

	public String drawHeader() {
		if (cthg != null)
			return cthg.createHeader();
		else
			return "";
	}

	public String drawContentColumnHeaders(String tid, boolean addEmptyCell) {
		StringBuffer buffer = new StringBuffer();
		if (columns != null) {
			Iterator<?> i = columns.iterator();
			for (int columnIndex = 0; i.hasNext(); columnIndex++) {
				TableColumn column = (TableColumn) i.next();
				if (column.isDisplayed())
					if (addEmptyCell)
						buffer.append((new StringBuilder()).append("").append(",").toString());
					else
						buffer.append((new StringBuilder()).append(column.getHeaderLabel()).append(",").toString());
			}

		} else if (data.size() > 0) {
			Object o = data.iterator().next();
			if (o instanceof FlexObject) {
				FlexObject flex = (FlexObject) o;
				String key;
				for (Iterator<?> e = flex.keySet().iterator(); e.hasNext(); buffer
						.append((new StringBuilder()).append(key).append(",").toString()))
					key = (String) e.next();

			}
		}
		buffer.append("\n");
		return buffer.toString();
	}

	public String drawContentData() {
		StringBuffer buffer = new StringBuffer();
		String group = "";
		if (getGroupByColumns() == null || getGroupByColumns().isEmpty()) {
			if (groups == null) {
				groups = deriveGroupsList();
				groups.add("");
			}
			Collection<?> groupRows;
			for (Iterator allGroups = groups.iterator(); allGroups.hasNext(); buffer.append(drawRows(groupRows))) {
				group = (String) allGroups.next();
				groupRows = getGroupRows(group);
				if (groupRows.size() > 0 && (groups.size() > 1
						|| groups.size() == 1 && !"".equals(groups.iterator().next().toString()))) {
					String groupLabel = group;
					if (groupColumn != null) {
						groupLabel = groupColumn.getLocalizedData(group);
						groupLabel = FormatHelper.applyFormat(groupLabel, groupColumn.getFormat());
					}
					if (!FormatHelper.hasContent(groupLabel))
						groupLabel = WTMessage.getLocalizedMessage("com.lcs.wc.resource.MainRB", "emptyLowerCase_LBL",
								RB.objA);
					if (columns != null)
						buffer.append((new StringBuilder()).append(groupLabel).append("\n").toString());
				}
				groupFamily.add(group);
			}

		} else {
			buffer.append(drawMultiGroupByTable());
		}
		return buffer.toString();
	}

	public String drawMultiGroupByTable() {
		StringBuffer buffer = new StringBuffer();
		populateGroupByGroups();
		if (groupByGroups != null)
			buffer.append(drawMultiGroupByTable(new Vector(getGroupByColumns()), data, 0));
		return buffer.toString();
	}

	public String drawMultiGroupByTable(Vector tcgroups, Collection rowData, int level) {
		StringBuffer buffer = new StringBuffer();
		if (rowData.size() == 0)
			return buffer.toString();
		TableColumn column = (TableColumn) tcgroups.elementAt(level);
		currentColumn = column;
		String index = column.getTableIndex();
		Vector groupVals = (Vector) groupByGroups.get(index);
		if ("CURRENCY_FORMAT".equals(column.getFormat()))
			groupVals = new Vector(SortHelper.sortCurrencies(groupVals, false));
		else if ("FLOAT_FORMAT".equals(column.getFormat()) || "DOUBLE_FORMAT".equals(column.getFormat()))
			groupVals = new Vector(SortHelper.sortStringsAsDoubles(groupVals, false));
		else if ("INT_FORMAT".equals(column.getFormat()) || "LONG_FORMAT".equals(column.getFormat()))
			groupVals = new Vector(SortHelper.sortStringsAsIntegers(groupVals, false));
		else if (column.getList() != null && !"moaList".equals(column.getAttributeType())
				&& !"composite".equals(column.getAttributeType()))
			try {
				Iterator sortedKeyIter = column.getList().getOrderedKeys(null, false).iterator();
				HashSet newGroupVals = new LinkedHashSet();
				do {
					if (!sortedKeyIter.hasNext())
						break;
					String key = (String) sortedKeyIter.next();
					if (FormatHelper.hasContent(column.getAttValListDisplay()))
						key = column.getList().get(key, column.getAttValListDisplay());
					else
						key = column.getList().getValue(key, ClientContext.getContext().getLocale());
					if (groupVals.contains(key))
						newGroupVals.add(key);
				} while (true);
				if (groupVals.contains("") && !newGroupVals.contains(""))
					newGroupVals.add("");
				groupVals = new Vector();
				groupVals.addAll(newGroupVals);
			} catch (Exception e) {
				e.printStackTrace();
			}
		else
			Collections.sort(groupVals);
		String val;
		for (Iterator it = groupVals.iterator(); it.hasNext(); groupFamily.remove(val)) {
			Collection tempData = new Vector(rowData);
			val = (String) it.next();
			groupFamily.add(val);
			tempData = getGroupRows(tempData, column, val);
			if (columns != null && tempData.size() > 0 && column.isShowGroupByHeader())
				if (!FormatHelper.hasContent(val)) {
					buffer.append((new StringBuilder())
							.append(getIndents(level)).append(WTMessage
									.getLocalizedMessage("com.lcs.wc.resource.MainRB", "emptyLowerCase_LBL", RB.objA))
							.append("\n").toString());
				} else {
					String groupDisplay = column.getLocalizedData(val, true);
					buffer.append((new StringBuilder()).append(getIndents(level)).append(groupDisplay).append("\n")
							.toString());
				}
			if (level == tcgroups.size() - 1)
				buffer.append(drawRows(tempData));
			else
				buffer.append(drawMultiGroupByTable(tcgroups, tempData, level + 1));
		}

		return buffer.toString();
	}

	protected String getIndents(int indents) {
		String indent = "    ";
		String totalIndent = "";
		for (int i = 0; i < indents; i++)
			totalIndent = (new StringBuilder()).append(totalIndent).append(indent).toString();

		return totalIndent;
	}

	public String drawRows(Collection groupRows) {
		StringBuffer buffer = new StringBuffer();
		Iterator rows = groupRows.iterator();
		Iterator columnsIt = null;
		TableColumn column = null;
		boolean firstRow = true;
		while (rows.hasNext()) {
			Object obj = rows.next();
			if (obj instanceof TableSectionHeader) {
				TableSectionHeader header = (TableSectionHeader) obj;
				buffer.append(drawSectionHeader(header.getLabel()));
			} else {
				TableData td = (TableData) obj;
				if (columns != null) {
					columnsIt = columns.iterator();
					do {
						if (!columnsIt.hasNext())
							break;
						column = (TableColumn) columnsIt.next();
						if (column.isShowCriteriaOverride())
							column = column.getOverrideColumn(td);
						if (column.isByGroup() && !firstRow)
							td.setData(column.getTableIndex(), "");
						if (column.isDisplayed() && column.showCell(td))
							buffer.append((new StringBuilder()).append(formatForCSV(column.drawCSVCell(td))).append(",")
									.toString());
						else if (column.isDisplayed())
							buffer.append(",");
					} while (true);
				} else if (td instanceof FlexObject) {
					FlexObject flex = (FlexObject) td;
					String key;
					for (Iterator keys = flex.keySet().iterator(); keys.hasNext(); buffer
							.append((new StringBuilder()).append(flex.getString(key)).append(",").toString()))
						key = (String) keys.next();

				}
				firstRow = false;
				buffer.append("\n");
			}
		}
		return buffer.toString();
	}

	public String drawDiscreteGrandTotal(TableColumn column) {
		StringBuffer buffer = new StringBuffer();
		String display = column.getDiscreteLabel();
		if (!FormatHelper.hasContent(display))
			display = (new StringBuilder()).append("Total for ").append(column.getHeaderLabel()).append(": ")
			.toString();
		int total = getDiscreteTotalSetCount(column);
		buffer.append((new StringBuilder()).append(display).append(total).append("\n").toString());
		return buffer.toString();
	}

	public String drawDiscreteTotalRows(String group, TableColumn column) {
		StringBuffer buffer = new StringBuffer();
		Iterator subColumns = (new Vector(columns)).iterator();
		do {
			if (!subColumns.hasNext())
				break;
			TableColumn sColumn = (TableColumn) subColumns.next();
			if (sColumn.isDiscreteCount()) {
				String display = sColumn.getDiscreteLabel();
				if (!FormatHelper.hasContent(display))
					display = (new StringBuilder()).append("Total for ").append(sColumn.getHeaderLabel()).append(": ")
					.toString();
				int total = getDiscreteSetCount(sColumn, group);
				Iterator subColumns2 = (new Vector(columns)).iterator();
				Boolean reachedColumn = Boolean.valueOf(false);
				do {
					if (!subColumns2.hasNext())
						break;
					TableColumn drawColumn = (TableColumn) subColumns2.next();
					if (drawColumn.isDisplayed())
						if (drawColumn.equals(column)) {
							buffer.append((new StringBuilder()).append(display).append(total).append(",").toString());
							reachedColumn = Boolean.valueOf(true);
						} else {
							buffer.append(",");
						}
				} while (true);
				buffer.append("\n");
			}
		} while (true);
		return buffer.toString();
	}

	public String drawDiscreteTotal(String group) {
		StringBuffer buffer = new StringBuffer();
		Iterator columnsIt = null;
		TableColumn column = null;
		columnsIt = columns.iterator();
		String displayGroup = "";
		do {
			if (!columnsIt.hasNext())
				break;
			column = (TableColumn) columnsIt.next();
			if (column.isDisplayed()) {
				if (FormatHelper.hasContent(group))
					displayGroup = group;
				if (groupByColumns.contains(column) && column.isShowGroupSubTotal()
						&& FormatHelper.hasContent(displayGroup)
						&& displayGroup.equals(getSubTotalDisplay(column, group)))
					buffer.append(drawDiscreteTotalRows(group, column));
			}
		} while (true);
		return buffer.toString();
	}

	public String drawTotal(String group, boolean isSubTotal) {
		StringBuffer buffer = new StringBuffer();
		Iterator columnsIt = null;
		TableColumn column = null;
		if (!isSubTotal && drawTotalLabel) {
			buffer.append(grandTotalLabel);
			buffer.append("\n");
		}
		columnsIt = columns.iterator();
		do {
			if (!columnsIt.hasNext())
				break;
			column = (TableColumn) columnsIt.next();
			if (column.isDisplayed())
				if (groupByColumns.contains(column)) {
					Vector colGroups = new Vector((Collection) groupByGroups.get(column.getTableIndex()));
					int gIndex = colGroups.indexOf(group);
					if (isSubTotal && column.isShowGroupSubTotal() && FormatHelper.hasContent(group) && gIndex > -1)
						buffer.append(
								(new StringBuilder()).append(getSubTotalDisplay(column, group)).append(",").toString());
					else
						buffer.append(",");
				} else if (column.isTotal()) {
					if (isSubTotal)
						buffer.append((new StringBuilder()).append("\"").append(getSubTotalDisplay(column, group))
								.append("\"").append(",").toString());
					else
						buffer.append((new StringBuilder()).append("\"").append(getTotalDisplay(column)).append("\"")
								.append(",").toString());
				} else {
					buffer.append(",");
				}
		} while (true);
		buffer.append("\n");
		if (isShowDiscreteRows())
			if (isSubTotal) {
				buffer.append(drawDiscreteTotal(group));
			} else {
				Iterator columnCount = columns.iterator();
				do {
					if (!columnCount.hasNext())
						break;
					TableColumn t = (TableColumn) columnCount.next();
					if (t.isDiscreteCount())
						buffer.append(drawDiscreteGrandTotal(t));
				} while (true);
			}
		return buffer.toString();
	}

	protected String drawSectionHeader(String label) {
		StringBuffer buffer = new StringBuffer();
		buffer.append((new StringBuilder()).append(FormatHelper.format(label)).append("\n").toString());
		return buffer.toString();
	}

	private String printFile(String output) {
		String fileName = generateFileName();
		LOGGER.info("fileName=============================" + fileName);
		String fileOutName = FormatHelper.formatRemoveProblemFileNameChars(fileName);
		String url = "";
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
					(new StringBuilder()).append(downloadLocation).append(fileOutName).toString()));
			out.write(new byte[] { -17, -69, -65 });
			out.write(output.getBytes(fileEncoding));
			out.flush();
			out.close();
			fileOutName = URLEncoder.encode(fileOutName, defaultCharsetEncoding);
			String filepath = FormatHelper.formatFilePathForUrlUse(FileLocation.CsvExcelDownloadLocation);
			url = (new StringBuilder()).append(filepath).append(fileOutName).toString();
		} catch (Exception ex) {
			LOGGER.error((new StringBuilder("Error : ")).append(ex.fillInStackTrace()));
		}
		return url;
	}

	private String generateFileName() {
		SimpleDateFormat format = new SimpleDateFormat("MMddyyyy_hhmm");
		String time = format.format(new Date());
		String userName = "-";
		try {
			userName = context.getUser().getName().toString();
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		String fname = (new StringBuilder()).append(reportName).toString();
		return (new StringBuilder()).append(fname).append(".csv").toString();
	}

	public static String formatForCSV(String text) {
		if (text == null || "".equals(text)) {
			text = "";
		} else {
			text = text.replaceAll("\"", "\"\"");
			if (text.indexOf(",") > -1)
				text = (new StringBuilder()).append("\"").append(text).append("\"").toString();
			text = text.replace('\n', ' ');
			text = text.replace('\r', ' ');
		}
		return text;
	}

	protected void addToTotals(TableData td, TableColumn col) {
		if (groupByColumns.contains(col)) {
			String group;
			Hashtable grpST;
			for (Iterator family = groupFamily.iterator(); family.hasNext(); groupSubTotals.put(group, grpST)) {
				group = (String) family.next();
				grpST = (Hashtable) groupSubTotals.get(group);
				if (grpST == null)
					grpST = new Hashtable();
				String storedVal = (String) grpST.get(col);
				String dataVal = col.drawCSVCell(td);
				if (!FormatHelper.hasContent(storedVal))
					grpST.put(col, dataVal);
			}

			return;
		}
		if (col.getTotalMathFunctions() != null) {
			Collection mathFunctions = col.getTotalMathFunctions();
			Iterator it = mathFunctions.iterator();
			TableColumn column1 = null;
			double d1 = 0.0D;
			TableColumn column2 = null;
			double d2 = 0.0D;
			Hashtable operation;
			String s1;
			for (; it.hasNext(); s1 = (String) operation.get("MATHFUNCTION")) {
				d1 = 0.0D;
				d2 = 0.0D;
				column1 = null;
				column2 = null;
				operation = (Hashtable) it.next();
				column1 = (TableColumn) operation.get("MATHCOLUMN1");
				if (operation.get("MATHCOLUMN2") instanceof TableColumn) {
					column2 = (TableColumn) operation.get("MATHCOLUMN2");
					continue;
				}
				if (operation.get("MATHCOLUMN2") instanceof Double)
					d2 = ((Double) operation.get("MATHCOLUMN2")).doubleValue();
			}

			return;
		} else {
			String s = td.getData(col.getTableIndex());
			double d = FormatHelper.parseDouble(s);
			return;
		}
	}

	public CSVTableHeaderGenerator getCSVTableHeaderGenerator() {
		return cthg;
	}

	public void setCSVTableHeaderGenerator(CSVTableHeaderGenerator cthg) {
		this.cthg = cthg;
	}

	public static String getData(WTObject obj, String key, boolean needValue2) throws WTException {
		String value = "";
		FlexType type;
		FlexTypeAttribute att;
		FlexTyped typed;
		type = null;
		att = null;
		String attType = "";
		Object attValue = null;
		String format = null;
		if (obj instanceof FlexTyped) {
			typed = (FlexTyped) obj;
			type = typed.getFlexType();
			if (type != null) {
				try {
					att = type.getAttribute(key);
				} catch (FlexTypeException e) {
					LOGGER.debug((new StringBuilder("Attribute not Found with the key : ")).append(key).toString());
					return value;
				}
				try {
					attType = att.getAttVariableType();

					if (ACLHelper.hasViewAccess(att)) {
						attValue = getAttValue(obj, key);
					

						if (attType.equals("choice") || attType.equals("driven")) {
							if (attValue != null)
								if (!needValue2) {
									value = type.getAttribute(key).getAttValueList().getValue((String) attValue,
											Locale.getDefault());

								} else
									value = getValue2(typed, key, attValue.toString());
						} else if (attType.equals("derivedString") || attType.equals("text")
								|| attType.equals("textArea")) {
							if (attValue != null)
								value = (String) attValue;
						} else if (attType.equals("float") || attType.equals("currency") || attType.equals("integer"))

						{
							if (attType.equals("integer")) {

								if (attValue != null) {

									String str3 = String.valueOf(attValue);
									value = str3;

								} else {
									value = "0";
								}
							}
							
											
							else if (attValue != null) {
							
								Double d = (Double) attValue;
					
								int precision = att.getAttDecimalFigures();
								
									
								if (precision != 0) {

									value = FormatHelper.formatWithPrecision(d, precision);
									/*LOGGER.info("Key  :"+key+"Double Value  :"+d+"precision :"+ precision+"with Precision on double :"+value);
									/*  Comment old code below beacuse Java code DecimalFormat has found bug
									 * String str = "";
									StringBuilder build = new StringBuilder("");
									for (int i = 0; i < precision; i++)
										build = build.append("#");
										str = build.toString();
									DecimalFormat twoDForm = new DecimalFormat(
											(new StringBuilder("#.")).append(str).toString());																	
									value = (new StringBuilder()).append(twoDForm.format(d)).toString();								
											
								*/
									} else {
									
									value = (new StringBuilder()).append(d.intValue()).toString();
									
																		
								}
							} else {
								value = "0";
							}
						} else if (attType.equals("sequence")) {
							if (attValue != null) {
								Long l = (Long) attValue;
								Double d = l.doubleValue();
								int i = (int) Math.round(d.doubleValue());
								value = String.valueOf(i);
							}
						} else if (attType.equals("date")) {
							if (attValue != null) {
								WrappedTimestamp time = (WrappedTimestamp) attValue;
								format = "dd/MM/yyyy";
								DateFormat formatter = new SimpleDateFormat(format);
								TimeZone timeZone = null;
								timeZone = TimeZone.getTimeZone("GMT");
								formatter.setTimeZone(timeZone);
								value = formatter.format(time);
							}
						} else if (attType.equals("moaList") || attType.equals("moaEntry")) {
							if (attValue != null) {

								String stringValue = (String) attValue;
								value = MOAHelper.parseOutDelimsLocalized(stringValue, ",", att.getAttValueList(),
										null);
							}
						} else if (attType.equals("composite")) {
							String token = null;
							String compositeKey = null;
							String percentValue = null;
							String keyValue = "";
							if (attValue != null) {
								String stringValue = (String) attValue;
								for (StringTokenizer compositeST = new StringTokenizer(stringValue,
										"|~*~|"); compositeST.hasMoreElements();) {
									token = compositeST.nextToken();
									if (token != null && token.indexOf(' ') != -1) {
										percentValue = token.substring(0, token.indexOf(' '));
										compositeKey = token.substring(token.indexOf(' ') + 1, token.length());
										keyValue = type.getAttribute(key).getAttValueList().getValue(compositeKey,
												ClientContext.getContext().getLocale());
										if (compositeST.hasMoreElements())
											value = (new StringBuilder(String.valueOf(value))).append(percentValue)
											.append(" ").append(keyValue).append(",").toString();
										else
											value = (new StringBuilder(String.valueOf(value))).append(percentValue)
											.append(" ").append(keyValue).toString();
									}
								}

							}
						} else if (attType.equals("object_ref") || attType.equals("object_ref_list")) {
							if (attValue != null)
								if (attValue instanceof LCSSupplier) {
									LCSSupplier val = (LCSSupplier) attValue;
									value = val.getName();
								} else if (attValue instanceof LCSColor) {
									LCSColor val = (LCSColor) attValue;
									value = val.getName();
								} else if (attValue instanceof LCSSeason) {
									LCSSeason val = (LCSSeason) attValue;
									value = val.getName();
								} else if (attValue instanceof LCSProduct) {
									LCSProduct val = (LCSProduct) attValue;
									value = val.getName();
								} else if (attValue instanceof LCSSourcingConfig) {
									LCSSourcingConfig val = (LCSSourcingConfig) attValue;
									value = val.getName();
								} else if (attValue instanceof FlexSpecification) {
									FlexSpecification val = (FlexSpecification) attValue;
									value = val.getName();
								} else if (attValue instanceof LCSLifecycleManaged) {
									LCSLifecycleManaged val = (LCSLifecycleManaged) attValue;
									value = val.getName();
								} else if (attValue instanceof LCSCountry) {
									LCSCountry val = (LCSCountry) attValue;
									value = val.getName();
								}
						} else if (attType.equals("userList") && attValue != null) {
							FlexObject flexobj = (FlexObject) attValue;
							if ("developer".equals(key) || "productManager".equals(key) || "patternMaker".equals(key))
								value = flexobj.getString("EMAIL");
							else
								value = flexobj.getString("FULLNAME");
						} else if (attType.equals("boolean")) {

							if (attValue != null)
								value = attValue.toString();

						} else if (attType.equals("uom")) {
							
											
						
							attValue=att.getDisplayValue(typed);
	
							
							if (attValue != null) {
								
								value = attValue.toString();
								}
								
																
							}
							
						
					} else {
						value = "** Restricted Access";
					}
				} catch (WTException wtException) {
					LOGGER.error((new StringBuilder("Error : ")).append(wtException.getLocalizedMessage()));
				}
			}
		}

		if (!FormatHelper.hasContent(value) && WORK_KEY.equals(key))
			value = "0";
		else if(FormatHelper.hasContent(value) && WORK_KEY.equals(key) && value.startsWith("-"))
			value = value.substring(1);
		
		return value.toUpperCase();

	}

	public static String getDateData(WTObject obj, String key, boolean needValue2) throws WTException {
		String value = "";
		FlexTypeAttribute att;
		FlexType type = null;
		att = null;
		String attType = "";
		Object attValue = null;
		String format = null;
		if (obj instanceof FlexTyped) {
			FlexTyped typed = (FlexTyped) obj;
			type = typed.getFlexType();
			if (type != null) {
				{
					try {
						att = type.getAttribute(key);
					} catch (FlexTypeException e) {
						LOGGER.debug((new StringBuilder("Attribute not Found with the key : ")).append(key).toString());
						return value;
					}
					try {
						attType = att.getAttVariableType();
						if (ACLHelper.hasViewAccess(att)) {
							attValue = getAttValue(obj, key);
							if (attType.equals("date")) {
								if (attValue != null) {
									WrappedTimestamp time = (WrappedTimestamp) attValue;
									format = "MM/dd/yyyy";
									DateFormat formatter = new SimpleDateFormat(format);
									TimeZone timeZone = null;
									timeZone = TimeZone.getTimeZone("GMT");
									formatter.setTimeZone(timeZone);
									value = formatter.format(time);
								}
							} else {
								value = "** Restricted Access";
							}
						}
					} catch (WTException wtException) {
						LOGGER.error((new StringBuilder("Error : ")).append(wtException.getLocalizedMessage()));
					}

				}

			}
		}
		if (!FormatHelper.hasContent(value) && WORK_KEY.equals(key))
			value = "0";
		return value.toUpperCase();

	}

	public static Object getAttValue(WTObject object, String key) {
		Object attValue = null;
		try {
			if (object instanceof LCSSupplier) {
				LCSSupplier val = (LCSSupplier) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSColor) {
				LCSColor val = (LCSColor) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSSeason) {
				LCSSeason val = (LCSSeason) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSSeasonProductLink) {
				LCSSeasonProductLink val = (LCSSeasonProductLink) object;
				if ("SKU".equals(val.getSeasonLinkType()))
					attValue = val.getLogicalValue(key);
				else
					attValue = val.getValue(key);
			} else if (object instanceof LCSProduct) {
				LCSProduct val = (LCSProduct) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSSourcingConfig) {
				LCSSourcingConfig val = (LCSSourcingConfig) object;
				attValue = val.getValue(key);
			} else if (object instanceof FlexSpecification) {
				FlexSpecification val = (FlexSpecification) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSLifecycleManaged) {
				LCSLifecycleManaged val = (LCSLifecycleManaged) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSCountry) {
				LCSCountry val = (LCSCountry) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSMOAObject) {
				LCSMOAObject val = (LCSMOAObject) object;
				attValue = val.getValue(key);
			} else if (object instanceof Placeholder) {
				Placeholder val = (Placeholder) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSSKU) {
				LCSSKU val = (LCSSKU) object;
				attValue = val.getValue(key);
			}
		} catch (Exception e) {
			LOGGER.error((new StringBuilder("Error : ")).append(e.getLocalizedMessage()));
		}
		return attValue;
	}

	
	public static HashMap<String,String> getMapOfAdidasArticleUPC() {
		 LOGGER.info("MEthod getMapOfAdidasArticleUPC>>> STRT");
		 HashMap<String,String> adidasArticleMoaRowMap=new HashMap<String,String>();
		try {
			
			LOGGER.info("ADIDAS_ARTICLE_FLEXTYPE_PATH>>> "+ADIDAS_ARTICLE_FLEXTYPE_PATH);
			FlexType BSObjectType = FlexTypeCache.getFlexTypeFromPath(ADIDAS_ARTICLE_FLEXTYPE_PATH);  
			LOGGER.info("BSObjectType>>> "+BSObjectType);
			LookupTableHelper tableHelper = new LookupTableHelper();

			LCSMOATable moaTable = tableHelper.getLookupTable(BSObjectType,  BUS_OBJECT_NAME, MOA_ADIDAS_ARTICLE_OBJ_KEY);
			Collection moaRows= moaTable.getRows();
			
			if (moaRows !=null && moaRows.size()>0) {
				Iterator moaIterator = moaRows.iterator();
				while (moaIterator.hasNext()) {
					String agrUPC="";
					String agrAdidasArticleNumber="";

					FlexObject adidasArticelMoaData = (FlexObject) moaIterator.next();
					agrUPC=adidasArticelMoaData.getData(MOA_UPC_NUMBER_KEY) ;
					agrAdidasArticleNumber=adidasArticelMoaData.getData(MOA_ADIDAS_ARTICLE_NUMBER_KEY) ;
					if(agrUPC !=null && agrAdidasArticleNumber.trim() !=""){
						adidasArticleMoaRowMap.put(agrUPC, agrAdidasArticleNumber);
					}
				}	
			}

		} catch (WTException e) {
			e.printStackTrace();
		}
		LOGGER.info(": getMapOfAdidasArticleUPC>>> "+adidasArticleMoaRowMap);
		LOGGER.info("MEthod getMapOfAdidasArticleUPC>>> END");
		return adidasArticleMoaRowMap;
	}
	
	public static String getValue2(FlexTyped typed, String key, String value) {
		String attValue = null;
		Map tempMap = null;
		Map value2Map = null;
		try {
			FlexTypeAttribute att = typed.getFlexType().getAttribute(key);
			Map listMap = (HashMap) att.getAttValueList().toLocalizedMapAll(ClientContext.getContext().getLocale());
			Set listCollection = listMap.keySet();
			Iterator listIter = listCollection.iterator();
			value2Map = new HashMap();
			while (listIter.hasNext()) {
				key = (String) listIter.next();
				tempMap = (Map) listMap.get(key);
				if (tempMap.size() > 0)
					value2Map.put(key, tempMap.get("Value2"));
			}
			attValue = (String) value2Map.get(value);
		} catch (Exception e) {
			LOGGER.error((new StringBuilder("Error : ")).append(e.getLocalizedMessage()));
		}
		return attValue;
	}

}
