<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.db.SearchResults,
                com.lcs.wc.db.FlexObject,
                com.lcs.wc.client.*,
                com.lcs.wc.client.web.*,
                com.lcs.wc.util.*,
                wt.util.*,
                com.lcs.wc.flextype.*,
                wt.indexsearch.*,
                com.lcs.wc.material.*,
                com.lcs.wc.report.*,
                com.ptc.rfa.rest.search.utility.SearchUtility,
                com.lcs.wc.sample.*,
				java.util.*,
				com.agron.wc.material.AgronMaterialQuery"
				


%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="flexg2" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator2" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>



<%!
    public static final String JSPNAME = "FindMaterialResults";
    public static final boolean DEBUG = true;

    public static final String getCellClass(boolean b){
       if(b){
            return "TABLEBODYDARK";
        } else {
            return "TABLEBODYLIGHT";
        }
    }
    public static final String MATERIAL_ROOT_TYPE = LCSProperties.get("com.lcs.wc.sample.LCSSample.Material.Root");
    public static final String COLOR_DEV_ROOT_TYPE = LCSProperties.get("com.lcs.wc.sample.LCSSample.Material.ColorDevelopement.Root");
    public static final String ADD_COLOR_AJAX_UTILITY = PageManager.getPageURL("BULK_CREATE_MATERIALCOLOR_PAGE", null);
    public static final String ADD_COLOR_DEVELOPMENT_AJAX_UTILITY = PageManager.getPageURL("BULK_CREATE_COLORDEVELOPMENTSAMPLE_PAGE", null);
    public static final String subURLFolder1 = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static final String MATERIAL_SUPPLIER_ROOT_TYPE = LCSProperties.get("com.lcs.wc.supplier.MaterialSupplierRootType", "Supplier");
    public static final int MATERIAL_QUERY_LIMIT = LCSProperties.get("com.lcs.wc.material.LCSMaterialQuery.queryLimit", 1000);
    public static final int CompareMaterials_MAXNUMBER = LCSProperties.get("com.lcs.wc.material.compareMaterials.maxNumber", 2);

    public FlexTypeAttribute getFlexAttribute(String thisKey, FiltersList filter) throws WTException{
            FlexTypeAttribute ftAttribute = null;

            if(thisKey.indexOf("_") > -1){
                thisKey = thisKey.substring(0, thisKey.lastIndexOf("_")) + "." + thisKey.substring(thisKey.lastIndexOf("_") + 1);
            }

            String typeOfKey = "";
            String attOfKey = "";
            if(thisKey.indexOf('.') > 0){
                typeOfKey = thisKey.substring(0, thisKey.indexOf("."));
                attOfKey = thisKey.substring(thisKey.indexOf(".")+1, thisKey.length());
                String typeOID = (String)filter.getTypes().get(typeOfKey);
                FlexType attType = FlexTypeCache.getFlexType(typeOID);
                if (attOfKey.indexOf(".") > 0) {
                    attOfKey = attOfKey.substring(0, attOfKey.indexOf("."));
                }
                if (attType != null){
                    ftAttribute = attType.getAttribute(attOfKey);
                }
            }


            return ftAttribute;
        }


    public Collection getAttColumnKeys(FlexType type, String prefix, String scope, String level) throws WTException{
        Collection attCollection = type.getAllAttributes(scope, level);
        Collection columns = new ArrayList();

        Iterator resultsAttsIter = attCollection.iterator();
        FlexTypeAttribute att = null;
        String attKey = "";

        while(resultsAttsIter.hasNext()){
            att = (FlexTypeAttribute) resultsAttsIter.next();

            if( !att.isAttEnabled() ||
            att.isAttHidden() ||
            !ACLHelper.hasViewAccess(att)){
                continue;
            }

            attKey = att.getAttKey();

            if(FormatHelper.hasContent(prefix)){
                attKey = prefix + attKey;
            }

            columns.add(attKey);
        }

        return columns;
    }

%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
appContext.clearAppContext(); //THis is added because the color chooser for multiple material-color creation always shows the palette if a season with palette was visited before
String imageLabel = WTMessage.getLocalizedMessage ( RB.IMAGE, "image_LBL", RB.objA ) ;
String nameLabel = WTMessage.getLocalizedMessage(RB.QUERYDEFINITION, "materialName", RB.objA);
nameLabel = nameLabel.substring(nameLabel.indexOf('\\') +1);
String allLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "all_LBL",RB.objA ) ;
String chooseLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "choose_LBL",RB.objA ) ;
String checkLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "all_LBL",RB.objA ) ;
String closeLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "close_LBL", RB.objA ) ;
String creatorLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "creator_COL", RB.objA ) ;
String modifierLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "modifier_COL", RB.objA ) ;
String createdOnLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "createdOn_COL", RB.objA ) ;
String modifiedOnLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "modifiedOn_COL", RB.objA ) ;
String lifecycleStateLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "lifeCycleState_LBL", RB.objA ) ;


String actionsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "actions_LBL", RB.objA ) ;
String addColorsLabel = WTMessage.getLocalizedMessage ( RB.MATERIALCOLOR, "addColorsLabel", RB.objA ) ; //"Add Colors";
String addSuppliersLabel = WTMessage.getLocalizedMessage ( RB.MATERIAL, "addSuppliers_LBL", RB.objA ) ; //"Add Colors";

String addColorDevelopmetSampleLabel = WTMessage.getLocalizedMessage ( RB.MATERIALCOLOR, "colorDevelopmentRequest_PG_TLE", RB.objA ) ;
String compareMaterialsLabel = WTMessage.getLocalizedMessage ( RB.MATERIAL, "compareMaterials_LBL", RB.objA ) ;

String includeSupplierLabel = WTMessage.getLocalizedMessage ( RB.MATERIAL, "includeSupplier_LBL",RB.objA );
String includeColorLabel = WTMessage.getLocalizedMessage ( RB.MATERIAL, "includeColor_LBL",RB.objA );
String includeSampleLabel = WTMessage.getLocalizedMessage ( RB.MATERIAL, "includeSample_LBL",RB.objA );
String refreshButton = WTMessage.getLocalizedMessage ( RB.MAIN, "refresh_LBL",RB.objA );
String selectedObjectsDoNotHaveAssociatedColorMessage = WTMessage.getLocalizedMessage ( RB.EXCEPTION, "selectedObjectsDoNotHaveAssociatedColor_MSG", RB.objA);
String sampleRequestLabel = WTMessage.getLocalizedMessage ( RB.SAMPLES, "sampleRequest_PG_HEAD", RB.objA);
String sampleLabel = WTMessage.getLocalizedMessage ( RB.SAMPLES, "sample_LBL", RB.objA);
String sampleTypeLabel = WTMessage.getLocalizedMessage ( RB.SAMPLES, "sampleType_LBL", RB.objA);
String materialColorLabel = WTMessage.getLocalizedMessage ( RB.MATERIALCOLOR, "materialColor_LBL", RB.objA);
String materialSupplierLabel = WTMessage.getLocalizedMessage ( RB.MATERIALSUPPLIER, "materialSupplier_LBL",RB.objA ) ;
String outOfQueryLimitExceptionMsg = WTMessage.getLocalizedMessage ( RB.EXCEPTION, "outOfQueryLimitException_MSG" , RB.objA );

String needToSelectMoreThanOneCompareMaterialsMSG = FormatHelper.encodeForJavascript(WTMessage.getLocalizedMessage ( RB.MATERIAL, "needToSelectMoreThanOneCompareMaterialsMSG", RB.objA) );
Object [] objs = {CompareMaterials_MAXNUMBER};
String excceedMaxNumberCompareMaterialsMSG = FormatHelper.encodeForJavascript(WTMessage.getLocalizedMessage ( RB.MATERIAL, "excceedMaxNumberCompareMaterialsMSG", objs) );

String errorQuickSearchMessage = "";
boolean cqsError = false;

    FlexType flexType = null;
    String type = request.getParameter("type");
    if(FormatHelper.hasContent(type)){
      flexType = FlexTypeCache.getFlexType(type);
    }
    String types = "";

%>

<%@ include file="/rfa/jsp/reports/ViewLookup.jspf" %>

<%

    boolean darkRow = false;

    String returnAction = request.getParameter("returnAction");
    boolean chooser = FormatHelper.parseBoolean(request.getParameter("chooser"));
    boolean multiple = FormatHelper.parseBoolean(request.getParameter("multiple"));
    boolean idOnly = FormatHelper.parseBoolean(request.getParameter("idOnly"));
    boolean detailRequest = FormatHelper.parseBoolean(request.getParameter("detailRequest"));
    String objectIdType = FormatHelper.format(request.getParameter("objectIdType"));
    String tableLayout = FormatHelper.format(request.getParameter("layout"));

    String includeSupplier = "false";
    String includeColor = "false";
    String includeSample = "false";

    String searchBySupplier = "false";
    String searchByColor = "false";
    String searchBySample = "false";

    if(FormatHelper.hasContent(request.getParameter("includeSupplier"))){
        includeSupplier = request.getParameter("includeSupplier");
    }

    ///Vendor search////
    if(lcsContext.isVendor){
        includeSupplier = "true";
    }


    if(FormatHelper.hasContent(request.getParameter("includeColor"))){
        includeColor = request.getParameter("includeColor");
    }

    if(FormatHelper.hasContent(request.getParameter("includeSample"))){
        includeSample = request.getParameter("includeSample");
    }


    if(FormatHelper.hasContent(request.getParameter("searchBySupplier"))){
        searchBySupplier = request.getParameter("searchBySupplier");
    }

    if(FormatHelper.hasContent(request.getParameter("searchByColor"))){
        searchByColor = request.getParameter("searchByColor");
    }

    if(FormatHelper.hasContent(request.getParameter("searchBySample"))){
        searchBySample = request.getParameter("searchBySample");
    }

    Map<String, Object> criteria = RequestHelper.hashRequest(request);
    if(FormatHelper.hasContent(request.getParameter("previousFilter"))){

      if(!Boolean.valueOf(includeSupplier)){
              criteria.put("searchBySupplier", "false");
              searchBySupplier = "false";
      }

      if(!Boolean.valueOf(includeColor)){
              criteria.put("searchByColor", "false");
              searchByColor = "false";
      }

      if(!Boolean.valueOf(includeSample)){
              criteria.put("searchBySample", "false");
              searchBySample = "false";
      }
    }

    if(filter!=null){
        Collection<String> filterAttributes = filter.getAttributes();
        if(filterAttributes != null && filterAttributes.size()>0){
            for (String att : filterAttributes){
                FlexTypeAttribute filterAtt = null;
                String searchIndex = "";
                String value = "";
                boolean isMOAAttr = false;
                //Get the searchIndex of the att. This is used to determine if value entered for a template filter is blank
                if(filter.isHardAttribute(att)){//hard and soft attributes require different APIs to retrieve the searchIndex
                    searchIndex = filter.getHardAttributeFilter(att);
                    if(FormatHelper.hasContent(searchIndex)){
                        String table = "";
                        String index = "";
                        String attType = "";
                        try{
                            StringTokenizer parser = new StringTokenizer(searchIndex, "/");
                            table = parser.nextToken();
                            index = parser.nextToken();
                            attType = parser.nextToken().toUpperCase();
                        }
                        catch(Exception e){
                            throw new WTException("Invald filterKey defintion in the QueryDefinitions.xml - " + searchIndex);
                        }

                        searchIndex = table + "_" + index;
                        searchIndex = searchIndex.toUpperCase();
                    }

                }else{
                    filterAtt = getFlexAttribute(att, filter);
                    isMOAAttr = SearchUtility.isMOAAttribute(filterAtt);
                    searchIndex = filterAtt.getSearchCriteriaIndex(filterAtt.getAttScope(), filterAtt.getAttObjectLevel());
                }

                if(FormatHelper.hasContent(searchIndex) && !criteria.containsKey(searchIndex)){
                    for(Map.Entry<String,Object> entry : criteria.entrySet()){
                        if(entry.getKey().startsWith(searchIndex) && FormatHelper.hasContent((String)entry.getValue())){
                            searchIndex = entry.getKey();
                            break;
                        }
                    }
                }

                //Set appropriate criteria for the query based on the filter type
                if(att.startsWith("Supplier.")){
                    value = (String) criteria.get(searchIndex);
                    if(!FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType()) || FormatHelper.hasContent(value)){//If is not template filter or has content, set to searchBy...
                        searchBySupplier = "true";
                        includeSupplier = "true";
                        criteria.put("searchBySupplier", "true");
                        criteria.put("includeSupplier", "true");
                    }
                }else if(att.startsWith("Material Color.") || att.startsWith("Color.")){
                    value = (String) criteria.get(searchIndex);
                    if(!FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType()) || FormatHelper.hasContent(value)){
                        searchByColor = "true";
                        includeColor = "true";
                        includeSupplier = "true";
                        criteria.put("searchByColor", "true");
                        criteria.put("includeColor", "true");
                        criteria.put("includeSupplier", "true");
                        if(Boolean.valueOf(includeSample)){
                          criteria.put("includeSample", "true");
                          criteria.put("searchBySample", "true");
                        }
                    }
                }else if(att.startsWith("Sample.")){
                    value = (String) criteria.get(searchIndex);
                    if(!FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType()) || FormatHelper.hasContent(value)){
                        searchBySample = "true";
                        includeSample = "true";
                        includeSupplier = "true";
                        criteria.put("searchBySample", "true");
                        criteria.put("includeSample", "true");
                        criteria.put("includeSupplier", "true");
                    }
                }else if(att.startsWith("Material.")){
                    value = (String) criteria.get(searchIndex);
                    if(!FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType()) || FormatHelper.hasContent(value) || isMOAAttr){
                        boolean addSupplierCriteria = false;
                        if(Boolean.valueOf(includeSample)){
                              criteria.put("includeSample", "true");
                              criteria.put("searchBySample", "true");
                        }
                        if(filterAtt != null){
                            if(MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE.equals(filterAtt.getAttScope())){
                                addSupplierCriteria = true;
                            }
                        }else{
                            if(att.startsWith("Material.materialsupplier")){
                                addSupplierCriteria = true;
                            }
                        }
                        if(addSupplierCriteria){
                            includeSupplier = "true";
                            searchBySupplier = "true";
                            criteria.put("searchBySupplier", "true");
                            criteria.put("includeSupplier", "true");
                        }
                    }
                }
            }
        }
    }

    String typesString = "";
    if(FormatHelper.hasContent(request.getParameter("typesString"))){
        typesString = request.getParameter("typesString");
    }

    FlexType materialColorRoot = FlexTypeCache.getFlexTypeRoot("Material Color");
    String materialColorId = FormatHelper.getObjectId(materialColorRoot);


    FlexType supplierRoot = FlexTypeCache.getFlexTypeFromPath(MATERIAL_SUPPLIER_ROOT_TYPE);
    FlexType materialSupplierRoot = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.material.LCSMaterialSupplier");
    FlexType supplierType = supplierRoot;
    String supplierTypeId = FormatHelper.getObjectId(supplierRoot);

    FlexType colorRoot = FlexTypeCache.getFlexTypeRoot("Color");
    FlexType colorType = colorRoot;
    String colorTypeId = FormatHelper.getObjectId(colorRoot);

    FlexType colorDevelopmentSampleType = FlexTypeCache.getFlexTypeFromPath(COLOR_DEV_ROOT_TYPE);


    /***************Not sure which root type to use*********/
    //FlexType sampleRoot = FlexTypeCache.getFlexTypeFromPath(COLOR_DEV_ROOT_TYPE);
    FlexType sampleRoot = FlexTypeCache.getFlexTypeFromPath(MATERIAL_ROOT_TYPE);
    FlexType sampleType = sampleRoot;
    String sampleTypeId = FormatHelper.getObjectId(sampleRoot);



    if(FormatHelper.hasContent(request.getParameter("colorType"))){
        colorTypeId = request.getParameter("colorType");
        colorType = FlexTypeCache.getFlexType(colorTypeId);
    }

    if(FormatHelper.hasContent(request.getParameter("supplierType"))){
        supplierTypeId = request.getParameter("supplierType");
        supplierType = FlexTypeCache.getFlexType(supplierTypeId);
    }

    if(FormatHelper.hasContent(request.getParameter("sampleType"))){
        sampleTypeId = request.getParameter("sampleType");
        sampleType = FlexTypeCache.getFlexType(sampleTypeId);
    }
    boolean hasAccessToSearchMatSuppliers = false;
    boolean hasAccessToSearchMatSamples = false;
    boolean hasAccessToSearchMatColors = false;

    hasAccessToSearchMatSuppliers = ACLHelper.hasViewAccess(supplierType) && ACLHelper.hasViewAccess(materialSupplierRoot);
    hasAccessToSearchMatColors = hasAccessToSearchMatSuppliers && ACLHelper.hasViewAccess(materialColorRoot) && ACLHelper.hasViewAccess(colorType);
    hasAccessToSearchMatSamples = hasAccessToSearchMatSuppliers && ACLHelper.hasViewAccess(sampleType);

    types = MOAHelper.addValue(types, "Material|" + FormatHelper.getObjectId(flexType));
    types = MOAHelper.addValue(types, "Material Color|" + FormatHelper.getObjectId(materialColorRoot));
    types = MOAHelper.addValue(types, "Color|" + colorTypeId);
    types = MOAHelper.addValue(types, "Supplier|" + supplierTypeId);
    types = MOAHelper.addValue(types, "Sample|" + sampleTypeId);


    String  SEARCH_CLASS_DISPLAY = flexType.getFullNameDisplay(true);

    String idColumn = "LCSMATERIAL.BRANCHIDITERATIONINFO";
    String bulkActivity = "FIND_ADVANCED_MATERIAL";
    String bulkAction = "SEARCH";

    boolean updateMode = "true".equals(request.getParameter("updateMode"));

    if(updateMode){
        FTSLHolder hldr = new FTSLHolder();
        ArrayList<String> keys = new ArrayList<String>();

        String nameCriteriaKey = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getSearchCriteriaIndex();
        keys.add(nameCriteriaKey);
        keys.add("typesString");

        hldr.add(type, MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE, null, keys);


        if(includeSupplier.equals("true")){
            keys.add("includeSupplier");
            if(searchBySupplier.equals("true")){
                keys.add("searchBySupplier");
                keys.add("supplierType");
                keys.add("LCSSUPPLIER_CREATESTAMPA2FromDateString");
                keys.add("LCSSUPPLIER_CREATESTAMPA2ToDateString");
                keys.add("LCSSUPPLIER_MODIFYSTAMPA2FromDateString");
                keys.add("LCSSUPPLIER_MODIFYSTAMPA2ToDateString");
                keys.add("LCSSUPPLIER_STATESTATE");
                keys.add("LCSMATERIALSUPPLIER_CREATESTAMPA2FromDateString");
                keys.add("LCSMATERIALSUPPLIER_CREATESTAMPA2ToDateString");
                keys.add("LCSMATERIALSUPPLIER_MODIFYSTAMPA2FromDateString");
                keys.add("LCSMATERIALSUPPLIER_MODIFYSTAMPA2ToDateString");
                keys.add("LCSMATERIALSUPPLIER_STATESTATE");
            }
            hldr.add(type, MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE, null, keys);

            keys = new ArrayList<String>();
            nameCriteriaKey = supplierType.getAttribute("name").getSearchCriteriaIndex();
            keys.add(nameCriteriaKey);

            hldr.add(supplierTypeId, null, null, keys);


        }
        if(includeColor.equals("true")){
            keys.add("includeColor");
            if(searchByColor.equals("true")){
                keys.add("searchByColor");
                keys.add("colorType");
                keys.add("LCSCOLOR_CREATESTAMPA2FromDateString");
                keys.add("LCSCOLOR_CREATESTAMPA2ToDateString");
                keys.add("LCSCOLOR_MODIFYSTAMPA2FromDateString");
                keys.add("LCSCOLOR_MODIFYSTAMPA2ToDateString");
                keys.add("LCSCOLOR_STATESTATE");
                keys.add("LCSMATERIALCOLOR_CREATESTAMPA2FromDateString");
                keys.add("LCSMATERIALCOLOR_CREATESTAMPA2ToDateString");
                keys.add("LCSMATERIALCOLOR_MODIFYSTAMPA2FromDateString");
                keys.add("LCSMATERIALCOLOR_MODIFYSTAMPA2ToDateString");
                keys.add("LCSMATERIALCOLOR_STATESTATE");
            }
            nameCriteriaKey = colorType.getAttribute("name").getSearchCriteriaIndex();
            keys.add(nameCriteriaKey);
            hldr.add(colorTypeId, null, null, keys);
            hldr.add(materialColorId, null, null, null);
        }

        if(includeSample.equals("true")){
            keys.add("includeSample");
            if(searchBySample.equals("true")){
                keys.add("searchBySample");
                keys.add("sampleType");
                keys.add("LCSSAMPLE_CREATESTAMPA2FromDateString");
                keys.add("LCSSAMPLE_CREATESTAMPA2ToDateString");
                keys.add("LCSSAMPLE_MODIFYSTAMPA2FromDateString");
                keys.add("LCSSAMPLE_MODIFYSTAMPA2ToDateString");
                keys.add("LCSSAMPLE_STATESTATE");
                keys.add("LCSSAMPLEREQUEST_CREATESTAMPA2FromDateString");
                keys.add("LCSSAMPLEREQUEST_CREATESTAMPA2ToDateString");
                keys.add("LCSSAMPLEREQUEST_MODIFYSTAMPA2FromDateString");
                keys.add("LCSSAMPLEREQUEST_MODIFYSTAMPA2ToDateString");
                keys.add("LCSSAMPLEREQUEST_STATESTATE");
            }
            nameCriteriaKey = sampleType.getAttribute("name").getSearchCriteriaIndex();
            keys.add(nameCriteriaKey);

            hldr.add(sampleTypeId, SampleRequestFlexTypeScopeDefinition.SAMPLE_SCOPE, null, keys);

            keys = new ArrayList<String>();
            nameCriteriaKey = sampleType.getAttribute("requestName").getSearchCriteriaIndex();

            keys.add(nameCriteriaKey);
            hldr.add(sampleTypeId, SampleRequestFlexTypeScopeDefinition.SAMPLEREQUEST_SCOPE, null, keys);
        }

        session.setAttribute("BU_FTSLHOLDER", hldr);
    }



    HashMap sampleRequestAttMap = new HashMap();

    HashMap columnMap = new HashMap();
    String nameColumnIndex = flexType.getAttribute("name").getSearchResultIndex();

    flexg.setScope(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE);
    flexg.setLevel(null);

    flexg.createTableColumns(flexType, columnMap, flexType.getAllAttributes(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE, null, false), updateMode, false, "Material.", null, true, null);

    TableColumn column = null;
   //Build potentially required "Hard attribute" columns
   columnMap.putAll(flexg.createHardColumns(flexType, "LCSMaterial", "Material"));


    if(updateMode){
        UpdateFileTableColumn fcolumn = new UpdateFileTableColumn();
        fcolumn.setHeaderLabel("");
        fcolumn.setDisplayed(true);
        fcolumn.setTableIndex("LCSMATERIAL.PRIMARYIMAGEURL");
        fcolumn.setColumnWidth("10%");
        fcolumn.setClassname("LCSMATERIAL");
        fcolumn.setWorkingIdIndex("LCSMATERIAL.IDA2A2");
        fcolumn.setFormElementName("primaryImageURL");
        fcolumn.setFormatHTML(false);
        fcolumn.setColumnWidth("10%");
        fcolumn.setWrapping(true);
        fcolumn.setImage(true);
        columnMap.put("Material.thumbnail", fcolumn);

    }else{
        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel("");
        column.setHeaderAlign("left");
        column.setLinkMethod("launchImageViewer");
        column.setLinkTableIndex("LCSMATERIAL.PRIMARYIMAGEURL");
        column.setTableIndex("LCSMATERIAL.PRIMARYIMAGEURL");
        column.setColumnWidth("1%");
        column.setLinkMethodPrefix("");
        column.setImage(true);
        column.setImageWidth(75);
        column.setHeaderLink("javascript:resort('" +nameColumnIndex + "')");
        column.setShowFullImage(FormatHelper.parseBoolean(request.getParameter("showThumbs")));
        columnMap.put("Material.thumbnail",column);
    }


    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(nameLabel);
    column.setHeaderAlign("left");
    column.setLinkMethod("viewObject");
    column.setUseQuickInfo(true);
    column.setLinkTableIndex("LCSMATERIAL.BRANCHIDITERATIONINFO");
    column.setUseQuickInfo(true);
    column.setTableIndex(nameColumnIndex);
    column.setLinkMethodPrefix("VR:com.lcs.wc.material.LCSMaterial:");
    column.setHeaderLink("javascript:resort('" +nameColumnIndex +"')");
    columnMap.put("Material.ptcmaterialName",column);



    if(includeSupplier.equals("true") || searchBySupplier.equals("true") ){


        column = new UserTableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(creatorLabel + " (" + materialSupplierLabel + ")");
        column.setHeaderAlign("left");
        column.setTableIndex("LCSMATERIALSUPPLIER.IDA3D2ITERATIONINFO");
        column.setHeaderLink("javascript:resort('MSCREATOR.name')");
        columnMap.put("Material.materialsuppliercreator", column);

        column = new UserTableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(modifierLabel + " (" + materialSupplierLabel + ")");
        column.setHeaderAlign("left");
        column.setTableIndex("LCSMATERIALSUPPLIER.IDA3B2ITERATIONINFO");
        column.setHeaderLink("javascript:resort('MSMODIFIER.name')");
        columnMap.put("Material.materialsuppliermodifier", column);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(createdOnLabel + " (" + materialSupplierLabel + ")");
        column.setHeaderAlign("left");
        column.setTableIndex("LCSMATERIALSUPPLIER.CREATESTAMPA2");
        column.setHeaderLink("javascript:resort('LCSMaterialSupplier.createStampA2')");
        column.setFormat(FormatHelper.GMT_TO_LOCAL_FORMAT);
        columnMap.put("Material.materialsuppliercreatedOn", column);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(modifiedOnLabel + " (" + materialSupplierLabel + ")");
        column.setHeaderAlign("left");
        column.setTableIndex("LCSMATERIALSUPPLIER.MODIFYSTAMPA2");
        column.setHeaderLink("javascript:resort('LCSMaterialSupplier.modifyStampA2')");
        column.setFormat(FormatHelper.GMT_TO_LOCAL_FORMAT);
        columnMap.put("Material.materialsuppliermodifiedOn", column);

        column = new LifecycleStateTableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(lifecycleStateLabel + " (" + materialSupplierLabel + ")");
        column.setHeaderAlign("left");
        column.setTableIndex("LCSMATERIALSUPPLIER.STATESTATE");
        column.setHeaderLink("javascript:resort('LCSMaterialSupplier.statestate')");
        column.setFormat(FormatHelper.CASE_FORMAT);
        columnMap.put("Material.materialsupplierstate", column);

        String supplierNameColumnIndex = supplierRoot.getAttribute("name").getSearchResultIndex();
        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(supplierRoot.getRootType().getFullNameDisplay());
        column.setHeaderAlign("left");
        column.setLinkMethod("viewObject");
        column.setUseQuickInfo(true);
        column.setLinkTableIndex("LCSSUPPLIER.BRANCHIDITERATIONINFO");
        column.setTableIndex(supplierNameColumnIndex);
        column.setUseQuickInfo(true);
        column.setLinkMethodPrefix("VR:com.lcs.wc.supplier.LCSSupplier:");
        column.setHeaderLink("javascript:resort('" + supplierNameColumnIndex +"')");
        columnMap.put("Supplier.name",column);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(imageLabel);
        column.setHeaderAlign("left");
        column.setLinkMethod("launchImageViewer");
        column.setLinkTableIndex("LCSSUPPLIER.PRIMARYIMAGEURL");
        column.setTableIndex("LCSSUPPLIER.PRIMARYIMAGEURL");
        column.setColumnWidth("1%");
        column.setImage(true);
        column.setShowFullImage(FormatHelper.parseBoolean(request.getParameter("showThumbs")));
        column.setImageWidth(75);
        columnMap.put("Supplier.primaryImageURL", column);

        flexg.setScope(com.lcs.wc.material.MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE);
        flexg.setLevel(null);



        //Make sure that attributes of material-supplier placeholder cannot be updated
        HashMap msColumnMap = new HashMap();
        flexg.createTableColumns(flexType, msColumnMap, flexType.getAllAttributes(com.lcs.wc.material.MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE, null, false), updateMode, false, "Material.", null, true, null);
        Iterator msColumnsIterator = msColumnMap.values().iterator();
        TableColumn msColumn;
        while(msColumnsIterator.hasNext()){
            msColumn = (TableColumn)msColumnsIterator.next();
            msColumn.setShowCriteria("0");
            msColumn.setShowCriteriaTarget("LCSMATERIALSUPPLIERMASTER.PLACEHOLDER");
            msColumn.setShowCriteriaNumericCompare(true);
        }
        columnMap.putAll(msColumnMap);

        flexg.setScope(null);

        flexg = new FlexTypeGenerator();
        flexg.setLevel(null);
        //Cannot update supplier objects
        flexg.createTableColumns(supplierType, columnMap, supplierType.getAllAttributes(null, null, false), false, false, "Supplier.", null, true, null);
    }

    if(includeColor.equals("true") || searchByColor.equals("true")){

        column = new UserTableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(creatorLabel + " (" + materialColorLabel + ")");
        column.setHeaderAlign("left");
        column.setTableIndex("LCSMATERIALCOLOR.IDA3A7");
        column.setHeaderLink("javascript:resort('MCCREATOR.name')");
        columnMap.put("Material Color.creator", column);

        column = new UserTableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(modifierLabel + " (" + materialColorLabel + ")");
        column.setHeaderAlign("left");
        column.setTableIndex("LCSMATERIALCOLOR.IDA3C8");
        column.setHeaderLink("javascript:resort('MCMODIFIER.name')");
        columnMap.put("Material Color.modifier", column);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(createdOnLabel + " (" + materialColorLabel + ")");
        column.setHeaderAlign("left");
        column.setTableIndex("LCSMATERIALCOLOR.CREATESTAMPA2");
        column.setHeaderLink("javascript:resort('LCSMaterialColor.createStampA2')");
        column.setFormat(FormatHelper.GMT_TO_LOCAL_FORMAT);
        columnMap.put("Material Color.createdOn", column);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(modifiedOnLabel + " (" + materialColorLabel + ")");
        column.setHeaderAlign("left");
        column.setTableIndex("LCSMATERIALCOLOR.MODIFYSTAMPA2");
        column.setHeaderLink("javascript:resort('LCSMaterialColor.modifyStampA2')");
        column.setFormat(FormatHelper.GMT_TO_LOCAL_FORMAT);
        columnMap.put("Material Color.modifiedOn", column);

        column = new LifecycleStateTableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(lifecycleStateLabel + " (" + materialColorLabel + ")");
        column.setHeaderAlign("left");
        column.setTableIndex("LCSMATERIALCOLOR.STATESTATE");
        column.setHeaderLink("javascript:resort('LCSMaterialColor.statestate')");
        column.setFormat(FormatHelper.CASE_FORMAT);
        columnMap.put("Material Color.state", column);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(colorRoot.getRootType().getFullNameDisplay());
        column.setHeaderAlign("left");
        column.setLinkMethod("viewObject");
        column.setLinkTableIndex("LCSCOLOR.IDA2A2");
        column.setTableIndex("LCSCOLOR.COLORNAME");
        column.setUseQuickInfo(true);
        column.setLinkMethodPrefix("OR:com.lcs.wc.color.LCSColor:");
        column.setUseQuickInfo(true);
        column.setHeaderLink("javascript:resort('" + "LCSColor.colorName" +"')");
        columnMap.put("Color.name", column);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel("");
        column.setHeaderAlign("left");
        column.setTableIndex("LCSCOLOR.thumbnail");
        //column.setColumnWidth("30");
        column.setBgColorIndex("LCSCOLOR.ColorHexidecimalValue");
        column.setImage(true);
        column.setImageWidth(75);
        column.setColumnWidth("5%");
        column.setShowFullImage(FormatHelper.parseBoolean(request.getParameter("showThumbs")));
        column.setLinkMethod("launchImageViewer");
        column.setLinkTableIndex("LCSCOLOR.thumbnail");
        columnMap.put("Color.thumbnail", column);

        flexg = new FlexTypeGenerator();
        flexg.createTableColumns(materialColorRoot, columnMap, materialColorRoot.getAllAttributes(null, null, false), updateMode, false, "Material Color.", null, true, null);

        flexg = new FlexTypeGenerator();
        flexg.setScope(null);
        flexg.setLevel(null);
        //Cannot update color objects
        flexg.createTableColumns(colorType, columnMap, colorType.getAllAttributes(null, null, false), false, false, "Color.", null, true, null);
    }



    if(includeSample.equals("true") || searchBySample.equals("true")){

        String sampleNameVariableName = "LCSSample." + sampleType.getAttribute("name").getColumnName();

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(sampleLabel);
        column.setHeaderAlign("left");
        column.setLinkMethod("viewObject");
        column.setLinkTableIndex("LCSSAMPLE.IDA2A2");
        column.setTableIndex(sampleNameVariableName);
        column.setUseQuickInfo(true);
        column.setLinkMethodPrefix("OR:com.lcs.wc.sample.LCSSample:");
        column.setHeaderLink("javascript:resort('" + sampleNameVariableName +"')");
        columnMap.put("Sample.name",column);


        String requestNameVariableName = "LCSSampleRequest." + sampleType.getAttribute("requestName").getColumnName();


        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(sampleRequestLabel);
        column.setHeaderAlign("left");
        column.setLinkMethod("viewObject");
        column.setUseQuickInfo(true);
        column.setLinkTableIndex("LCSSAMPLEREQUEST.IDA2A2");
        column.setUseQuickInfo(true);
        column.setTableIndex(requestNameVariableName);
        column.setLinkMethodPrefix("OR:com.lcs.wc.sample.LCSSampleRequest:");
        column.setHeaderLink("javascript:resort('" + requestNameVariableName +"')");
        columnMap.put("Sample.requestName",column);

    //  Vector groupByVector = new Vector();
    //  groupByVector.add(column);
    //  tg.setGroupByColumns(groupByVector);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(sampleTypeLabel);
        column.setHeaderAlign("left");
        column.setTableIndex("SAMPLEFLEXTYPE.TYPENAME");
        column.setHeaderLink("javascript:resort('SAMPLEFLEXTYPE.typeName')");
        columnMap.put("Sample.type",column);

        flexg = new FlexTypeGenerator();
        flexg.setScope(com.lcs.wc.sample.SampleRequestFlexTypeScopeDefinition.SAMPLEREQUEST_SCOPE);
        flexg.setLevel(null);
        flexg.createTableColumns(sampleType, columnMap, sampleType.getAllAttributes(SampleRequestFlexTypeScopeDefinition.SAMPLEREQUEST_SCOPE, null, false), updateMode, false, "Sample.", null, true, null);

        flexg.createTableColumns(sampleType, sampleRequestAttMap, sampleType.getAllAttributes(SampleRequestFlexTypeScopeDefinition.SAMPLEREQUEST_SCOPE, null, false), updateMode, false, "Sample.", null, true, null);

        flexg = new FlexTypeGenerator();
        flexg.setScope(com.lcs.wc.sample.SampleRequestFlexTypeScopeDefinition.SAMPLE_SCOPE);
        flexg.setLevel(null);
        flexg.createTableColumns(sampleType, columnMap, sampleType.getAllAttributes(SampleRequestFlexTypeScopeDefinition.SAMPLE_SCOPE, null, false), updateMode, false, "Sample.", null, true, null);


    }


    String prefix = "VR:com.lcs.wc.material.LCSMaterial:";
    if("master".equals(objectIdType)){
            prefix = "OR:com.lcs.wc.material.LCSMaterialMaster:";
            idColumn = "LCSMATERIAL.IDA3MASTERREFERENCE";
    }

    if(includeColor.equals("true")){
            prefix = "OR:com.lcs.wc.material.LCSMaterialColor:";
            idColumn = "LCSMATERIALCOLOR.IDA2A2";
    }else if(includeSupplier.equals("true")){
            prefix = "VR:com.lcs.wc.material.LCSMaterialSupplier:";
            idColumn = "LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO";
    }

    Collection<TableColumn> columnList = new ArrayList<TableColumn>();


    if(chooser){
        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel("");
        column.setHeaderAlign("left");

        column.setTableIndex(nameColumnIndex);

       // String prefix = "VR:com.lcs.wc.material.LCSMaterial:";
        if("master".equals(objectIdType)){
            prefix = "OR:com.lcs.wc.material.LCSMaterialMaster:";
            idColumn = "LCSMATERIAL.IDA3MASTERREFERENCE";
        }

        if(multiple){
            column.setHeaderLabel(flexg2.drawSelectAllCheckbox(allLabel));
            column.setFormat("UNFORMATTED_HTML");
            TableFormElement fe = new CheckBoxTableFormElement();
            fe.setValueIndex(idColumn);
            fe.setValuePrefix(prefix);
            fe.setName("selectedIds");
            column.setFormElement(fe);
        } else {

            column.setLinkMethod("opener.choose");
            column.setWrapping(false);
            if(detailRequest){
                column.setLinkTableIndex("LCSMATERIAL.BRANCHIDITERATIONINFO");
                column.setLinkMethod("chooseDetail");
                column.setLinkMethodPrefix("VR:com.lcs.wc.material.LCSMaterial:");
                column.setConstantDisplay(true);
                column.setConstantValue(chooseLabel);

            } else {
                column.setLinkTableIndex(idColumn);
                if(!idOnly){
                    column.setLinkMethodPrefix(prefix);
                }
                column.setLinkTableIndex2(nameColumnIndex);
                column.setConstantDisplay(true);
                column.setConstantValue(chooseLabel);
            }
        }

        column.setColumnWidth("1%");
        columnList.add(column);
    } else {
        if(!updateMode && !"thumbnails".equals(tableLayout) && !"filmstrip".equals(tableLayout)){

            column = new TableColumn();
            column.setDisplayed(true);
            column.setHeaderLabel(flexg2.drawSelectAllCheckbox(allLabel));
            column.setFormat("UNFORMATTED_HTML");
            column.setHeaderAlign("left");
            TableFormElement fe1 = new CheckBoxTableFormElement();
            fe1.setValueIndex(idColumn);
            fe1.setValuePrefix(prefix);
            fe1.setName("selectedIds");
            LinkedHashMap<String,String> alternativeIndexesPrefixes = new LinkedHashMap<String,String>();
            alternativeIndexesPrefixes.put("LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO", "VR:com.lcs.wc.material.LCSMaterialSupplier:" );
            ((CheckBoxTableFormElement)fe1).setAlternativeValueIndexesAndPrefixes(alternativeIndexesPrefixes);
            column.setFormElement(fe1);
            column.setHeaderLink("javascript:toggleAllItems()");
            columnList.add(column);


            if(includeSample.equals("true")){
                column = new ActionsTableColumn();
                column.setHeaderLabel("");
                column.setDisplayed(true);
                column.setTableIndex("LCSSAMPLE.IDA2A2");
                column.setLinkTableIndex("LCSSAMPLE.IDA2A2");
                column.setLinkMethodPrefix("OR:com.lcs.wc.sample.LCSSample:");
                column.setColumnWidth("7%");
                LinkedHashMap<String,String> alternativeIndexesPrefixes2 = new LinkedHashMap<String,String>();
                alternativeIndexesPrefixes2.put("LCSMATERIALCOLOR.IDA2A2", "OR:com.lcs.wc.material.LCSMaterialColor:" );
                alternativeIndexesPrefixes2.put("LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO", "VR:com.lcs.wc.material.LCSMaterialSupplier:" );
                alternativeIndexesPrefixes2.put("LCSMATERIAL.BRANCHIDITERATIONINFO", "VR:com.lcs.wc.material.LCSMaterial:" );
                ((ActionsTableColumn)column).setAlternativeLinkTableIndexesAndPrefixes(alternativeIndexesPrefixes2);
                columnList.add(column);

            }else if(includeColor.equals("true")){
                column = new ActionsTableColumn();
                column.setHeaderLabel("");
                column.setDisplayed(true);
                column.setTableIndex("LCSMATERIALCOLOR.IDA2A2");
                column.setLinkTableIndex("LCSMATERIALCOLOR.IDA2A2");
                column.setLinkMethodPrefix("OR:com.lcs.wc.material.LCSMaterialColor:");
                column.setColumnWidth("7%");

                LinkedHashMap<String,String> alternativeIndexesPrefixes2 = new LinkedHashMap<String,String>();
                alternativeIndexesPrefixes2.put("LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO", "VR:com.lcs.wc.material.LCSMaterialSupplier:" );
                alternativeIndexesPrefixes2.put("LCSMATERIAL.BRANCHIDITERATIONINFO", "VR:com.lcs.wc.material.LCSMaterial:" );
                ((ActionsTableColumn)column).setAlternativeLinkTableIndexesAndPrefixes(alternativeIndexesPrefixes2);
                columnList.add(column);

            }else if(includeSupplier.equals("true")){
                column = new ActionsTableColumn();
                column.setHeaderLabel("");
                column.setDisplayed(true);
                column.setTableIndex("LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO");
                column.setLinkTableIndex("LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO");
                column.setLinkMethodPrefix("VR:com.lcs.wc.material.LCSMaterialSupplier:");
                column.setColumnWidth("7%");
                LinkedHashMap<String,String> alternativeIndexesPrefixes2 = new LinkedHashMap<String,String>();
                alternativeIndexesPrefixes2.put("LCSMATERIAL.BRANCHIDITERATIONINFO", "VR:com.lcs.wc.material.LCSMaterial:" );
                ((ActionsTableColumn)column).setAlternativeLinkTableIndexesAndPrefixes(alternativeIndexesPrefixes2);
                columnList.add(column);

            }else{
                column = new ActionsTableColumn();
                column.setHeaderLabel("");
                column.setDisplayed(true);
                column.setLinkTableIndex("LCSMATERIAL.BRANCHIDITERATIONINFO");
                column.setTableIndex("LCSMATERIAL.BRANCHIDITERATIONINFO");
                column.setLinkMethodPrefix("VR:com.lcs.wc.material.LCSMaterial:");
                column.setColumnWidth("10%");
                columnList.add(column);
            }
        }else if(updateMode){
            //Added for SPR#2088974 to keep the column count the same for hide/show
            TableColumn acolumn = new TableColumn();
            acolumn.setDisplayed(true);
            acolumn.setHeaderLabel("");
            acolumn.setHeaderAlign("left");
            acolumn.setColumnWidth("1%");
            acolumn.setLinkTableIndex("LCSMATERIAL.BRANCHIDITERATIONINFO");
            acolumn.setLinkMethodPrefix("VR:com.lcs.wc.material.LCSMaterial:");
            columnList.add(acolumn);
            columnList.add(acolumn);

        }
    }

    if("thumbnails".equals(tableLayout) || "filmstrip".equals(tableLayout)) {
        DefaultTileCellRenderer cellRenderer = new DefaultTileCellRenderer();
        cellRenderer.setImageIndex("LCSMATERIAL.PRIMARYIMAGEURL");
        cellRenderer.setIdIndex(idColumn);
        cellRenderer.setNameIndex(flexType.getAttribute("name").getSearchResultIndex());
        cellRenderer.setIdPrefix("VR:com.lcs.wc.material.LCSMaterial:");

        if(includeSample.equals("true")){

            cellRenderer.setIdIndex("LCSSAMPLE.IDA2A2");
            cellRenderer.setIdPrefix("OR:com.lcs.wc.sample.LCSSample:");
            LinkedHashMap<String,String> alternativeIndexesPrefixes2 = new LinkedHashMap<String,String>();
            alternativeIndexesPrefixes2.put("LCSMATERIALCOLOR.IDA2A2", "OR:com.lcs.wc.material.LCSMaterialColor:" );
            alternativeIndexesPrefixes2.put("LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO", "VR:com.lcs.wc.material.LCSMaterialSupplier:" );
            alternativeIndexesPrefixes2.put("LCSMATERIAL.BRANCHIDITERATIONINFO", "VR:com.lcs.wc.material.LCSMaterial:" );               cellRenderer.setAlternativeLinkTableIndexesAndPrefixes(alternativeIndexesPrefixes2);

            if(includeColor.equals("true")){
                cellRenderer.setImageIndex2("LCSCOLOR.THUMBNAIL");
                cellRenderer.setBgColorIndex2("LCSCOLOR.ColorHexidecimalValue");
            }


        }else if(includeColor.equals("true")){
            cellRenderer.setIdIndex("LCSMATERIALCOLOR.IDA2A2");
            cellRenderer.setIdPrefix("OR:com.lcs.wc.material.LCSMaterialColor:");
            LinkedHashMap<String,String> alternativeIndexesPrefixes2 = new LinkedHashMap<String,String>();
            alternativeIndexesPrefixes2.put("LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO", "VR:com.lcs.wc.material.LCSMaterialSupplier:" );
            alternativeIndexesPrefixes2.put("LCSMATERIAL.BRANCHIDITERATIONINFO", "VR:com.lcs.wc.material.LCSMaterial:" );           cellRenderer.setAlternativeLinkTableIndexesAndPrefixes(alternativeIndexesPrefixes2);
            cellRenderer.setImageIndex2("LCSCOLOR.THUMBNAIL");
            cellRenderer.setBgColorIndex2("LCSCOLOR.ColorHexidecimalValue");

        }else if(includeSupplier.equals("true")){
            cellRenderer.setIdIndex("LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO");
            cellRenderer.setIdPrefix("VR:com.lcs.wc.material.LCSMaterialSupplier:");
            LinkedHashMap<String,String> alternativeIndexesPrefixes2 = new LinkedHashMap<String,String>();
            alternativeIndexesPrefixes2.put("LCSMATERIAL.BRANCHIDITERATIONINFO", "VR:com.lcs.wc.material.LCSMaterial:" );
            cellRenderer.setAlternativeLinkTableIndexesAndPrefixes(alternativeIndexesPrefixes2);

        }else{
            cellRenderer.setIdIndex("LCSMATERIAL.BRANCHIDITERATIONINFO");
            cellRenderer.setIdPrefix("VR:com.lcs.wc.material.LCSMaterial:");
        }
        request.setAttribute("tileCellRenderer", cellRenderer);


    }


    //Setup the attlist/columns
    //attList is the list of attribute keys...required for the query to get the attributes
    Collection attList = new ArrayList();
    //columns is the list of Columns in the order they should be displayed from the column map
    Collection columns = new ArrayList(columnList);
    TableDataModel tdm = null;


    Collection materialSupplierColumns = new ArrayList();
    if("thumbnails".equals(tableLayout) || "filmstrip".equals(tableLayout)){
        //columnMap.remove("Material.thumbnail");

    }

    flexg = new FlexTypeGenerator();


        if(includeSupplier.equals("true") || searchBySupplier.equals("true")){
            flexg.setScope(MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE);
            flexg.setLevel(null);
            flexg.setSingleLevel(false);
            materialSupplierColumns.addAll(getAttColumnKeys(flexType, "Material.", MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE, null));
        }

    if(FormatHelper.hasContent(viewId)){
        flexg.setScope(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE);

        if(!FormatHelper.parseBoolean(request.getParameter("showThumbs")) && columnMap.get("Material.thumbnail") != null){
        }
        tdm = new TableDataModel(columnMap, new ArrayList(), columns, viewId);

        attList = tdm.attList;

    }
    else{

        Collection searchColumns = new Vector();
        flexg.setScope(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE);
        flexg.setLevel(null);
        flexg.setSingleLevel(false);
        searchColumns = flexg.createSearchResultColumnKeys(flexType, "Material.");

        //columns.add(columnMap.get("Material.All"));
        columns.add(columnMap.get("Material.thumbnail"));
        columns.add(columnMap.get("Material.ptcmaterialName"));
        columns.add(columnMap.get("Material.typename"));


        if(includeSupplier.equals("true") || searchBySupplier.equals("true")){
            searchColumns.add("Supplier.name");
            flexg.setScope(null);
            flexg.setLevel(null);
            flexg.setSingleLevel(false);
            searchColumns.addAll(flexg.createSearchResultColumnKeys(supplierType, "Supplier."));

            flexg.setScope(MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE);
            flexg.setLevel(null);
            flexg.setSingleLevel(false);
            searchColumns.addAll(flexg.createSearchResultColumnKeys(flexType, "Material."));
        }

        if(includeColor.equals("true") || searchByColor.equals("true")){
            searchColumns.add("Color.name");
            searchColumns.add("Color.thumbnail");
            flexg.setScope(null);
            flexg.setLevel(null);
            flexg.setSingleLevel(false);
            searchColumns.addAll(flexg.createSearchResultColumnKeys(colorType, "Color."));
            searchColumns.addAll(flexg.createSearchResultColumnKeys(materialColorRoot, "Material Color."));
        }




        if(includeSample.equals("true") || searchBySample.equals("true")){
            searchColumns.add("Sample.requestName");

            flexg.setScope(SampleRequestFlexTypeScopeDefinition.SAMPLEREQUEST_SCOPE);
            flexg.setLevel(null);
            flexg.setSingleLevel(false);
            searchColumns.addAll(flexg.createSearchResultColumnKeys(sampleType, "Sample."));

            searchColumns.add("Sample.name");
            //searchColumns.add("Sample.type");
            flexg.setScope(SampleRequestFlexTypeScopeDefinition.SAMPLE_SCOPE);
            flexg.setLevel(null);
            flexg.setSingleLevel(false);
            searchColumns.addAll(flexg.createSearchResultColumnKeys(sampleType, "Sample."));
        }

        flexg.extractColumns(searchColumns, columnMap, attList, columns);

    }


    // BEGIN INDEX SEARCH - Set Up and Run the query via this plugin
    Collection oidList = null;
    String searchterm = null;
    if (FormatHelper.hasContent(request.getParameter("indexSearchKeyword"))) {
        searchterm = (request.getParameter("indexSearchKeyword")).trim();
    }
    String typeclass = null;
    if (FormatHelper.hasContent(request.getParameter("searchTypeClass"))) {
        typeclass =request.getParameter("searchTypeClass");
    }

    // If doing a library->Material search, and the keyword picklist has been set, then determine the
    // correct FlexType to use when setting up the flexType filter criteria
    FlexType tempFlexType = flexType;
    if (FormatHelper.hasContent(searchterm)) {
        // Initialize the FAST search type to the FlexType root associated with currently select object class
        if (typeclass.indexOf(".LCSColor") > -1 ) {
            flexType = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.color.LCSColor");
        } else if (typeclass.indexOf(".LCSMaterialSupplier") > -1) {
            flexType = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.material.LCSMaterial");
        } else if (typeclass.indexOf(".LCSSupplier") > -1) {
            flexType = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.supplier.LCSSupplier");
        } else if (typeclass.indexOf(".LCSSampleRequest") > -1) {
            flexType = FlexTypeCache.getFlexTypeFromPath(COLOR_DEV_ROOT_TYPE);
        } else if (typeclass.indexOf(".LCSSample") > -1) {
            flexType = FlexTypeCache.getFlexTypeFromPath(COLOR_DEV_ROOT_TYPE);
        } else if (typeclass.indexOf(".LCSMaterialColor") > -1) {
            flexType = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.material.LCSMaterialColor");
        } else if (typeclass.indexOf(".LCSMaterial") > -1) {
            flexType = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.material.LCSMaterial");
        }

        // If the user has used the type chooser to alter the type for any class of object, reset the FAST search type root
        if( typeclass.indexOf(".LCSMaterialSupplier") > -1 || typeclass.indexOf(".LCSMaterial") > -1 ){
            flexType = flexType;
        } else if( typeclass.indexOf(".LCSMaterialColor") > -1 || typeclass.indexOf(".LCSColor") > -1  ){
            flexType =  colorType;
        } else if( typeclass.indexOf(".LCSSupplier") > -1 ){
            flexType =  supplierType;
        } else if( typeclass.indexOf(".LCSSample") > -1  ){
            flexType =  sampleType;
        }
    }
%>
    <%@ include file="/rfa/jsp/indexsearch/IndexSearchLibraryPlugin.jspf" %>
<%
    // oidList should be populated now if user specified a keyword to search against, otherwise size=0
    // END INDEX SEARCH filter

    // Done with FAST Search, put the original flexType back....
    flexType = tempFlexType;


    //Run the FlexPLM query
   // LCSMaterialQuery query = new LCSMaterialQuery();
	AgronMaterialQuery query = new AgronMaterialQuery();
    SearchResults results = SearchResults.emptySearchResults();
    boolean outOfQueryLimit=false;
    if(null != tdm && null != tdm.groupByColumns){
        criteria.put("groupByColumns", tdm.getGroupByProperties());
    }
    try {
        if ( FormatHelper.hasContent(searchterm)) {
            if (oidList != null) {
                results = query.findMaterialsByCriteria(criteria, flexType, attList, filter, oidList, MATERIAL_QUERY_LIMIT);
            } // no else - no findby criteria needed if a keyword search was done and no hits occured
        } else {
            results = query.findMaterialsByCriteria(criteria, flexType, attList, filter, oidList, MATERIAL_QUERY_LIMIT);
        }
    } catch (OutOfQueryLimitException ooqe) {
        outOfQueryLimit=true;
    }catch(LCSException ex){
        cqsError =true;
        errorQuickSearchMessage = ex.getLocalizedMessage();
    }
    if(cqsError){%>
        <table>
            <tr>
                <td class="ERROR">
                    <%= FormatHelper.encodeAndFormatForHTMLContent(errorQuickSearchMessage) %>
                </td>
            </tr>
        </table>
    <%}
    boolean RTS = FormatHelper.parseBoolean(request.getParameter("RETURN_TO_SEARCH_FROM"));
    // If the result set exceeds the configured limit display a message to the user to provide additional criteria
    if (outOfQueryLimit) { %>

        <table>
            <tr>
                <td class="ERROR">
                    <%= outOfQueryLimitExceptionMsg %>
                </td>
            </tr>
        </table>

    <% } else if(results.getResultsFound() == 1 && !chooser && !RTS){
        // IF ONLY ONE MATERIAL IS FOUND, TAKE THE USER TO THE DETAILS PAGE
        FlexObject materialData = (FlexObject) results.getResults().elementAt(0);
        String oid = "";
        if(includeSample.equals("true") && FormatHelper.hasContent(materialData.getString("LCSSAMPLEREQUEST.IDA2A2"))){
            oid = "OR:com.lcs.wc.sample.LCSSampleRequest:" + materialData.getString("LCSSAMPLEREQUEST.IDA2A2");
        }else if(includeColor.equals("true") && FormatHelper.hasContent(materialData.getString("LCSMATERIALCOLOR.IDA2A2"))){
            oid = "OR:com.lcs.wc.material.LCSMaterialColor:" + materialData.getString("LCSMATERIALCOLOR.IDA2A2");
        }else if(includeSupplier.equals("true")){
            oid = "VR:com.lcs.wc.material.LCSMaterialSupplier:" + materialData.getString("LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO");
        }else{
            oid = "VR:com.lcs.wc.material.LCSMaterial:" + materialData.getString("LCSMATERIAL.BRANCHIDITERATIONINFO");
        }
        %>
        <script>
           viewObject('<%=oid%>');
        </script>
        <%

    }



    Hashtable repeatedDataColumnMap = new Hashtable();
    repeatedDataColumnMap.put("LCSMATERIAL", "LCSMATERIAL.IDA3MASTERREFERENCE");
    if(includeColor.equals("true")){
        repeatedDataColumnMap.put("FLEXTYPE", "LCSMATERIAL.IDA3MASTERREFERENCE");

    }else if(includeSupplier.equals("true")){
        repeatedDataColumnMap.put("FLEXTYPE", "LCSMATERIAL.IDA3MASTERREFERENCE");
    }

    repeatedDataColumnMap.put("LCSMATERIALSUPPLIER", "LCSMATERIALSUPPLIER.IDA2A2");
    repeatedDataColumnMap.put("LCSSUPPLIER", "LCSMATERIALSUPPLIER.IDA2A2");
    repeatedDataColumnMap.put("LCSSUPPLIERMASTER", "LCSMATERIALSUPPLIER.IDA2A2");
    repeatedDataColumnMap.put("LCSCOLOR", "LCSMATERIALCOLOR.IDA2A2");
    repeatedDataColumnMap.put("LCSMATERIALCOLOR", "LCSMATERIALCOLOR.IDA2A2");
    repeatedDataColumnMap.put("LCSSAMPLEREQUEST", "LCSSAMPLEREQUEST.IDA2A2");

    Iterator columnsIterator = columnMap.keySet().iterator();
    String key;
    String tableName;
    while(columnsIterator.hasNext()){
        key = (String)columnsIterator.next();
        column = (TableColumn)columnMap.get(key);
        if(column.getAttributeType()!=null && (column.getAttributeType().equals("object_ref") || column.getAttributeType().equals("object_ref_list"))){
            tableName = column.getTableIndex();
            tableName = tableName.substring(0, tableName.indexOf("."));
            if(key.startsWith("Material.")){
                if(materialSupplierColumns.contains(key)){
                    repeatedDataColumnMap.put(tableName, "LCSMATERIALSUPPLIER.IDA2A2");
                }else{
                    if(!repeatedDataColumnMap.containsKey(tableName))
                        repeatedDataColumnMap.put(tableName, "LCSMATERIAL.IDA3MASTERREFERENCE");
                }
            }else if(key.startsWith("Supplier.")){
                repeatedDataColumnMap.put(tableName, "LCSMATERIALSUPPLIER.IDA2A2");
            }else if(key.startsWith("Color.")){
                repeatedDataColumnMap.put(tableName, "LCSMATERIALCOLOR.IDA2A2");
            }else if(key.startsWith("Material Color.")){
                repeatedDataColumnMap.put(tableName, "LCSMATERIALCOLOR.IDA2A2");
            }
        }
    }

    columnsIterator = sampleRequestAttMap.keySet().iterator();
    while(columnsIterator.hasNext()){
        key = (String)columnsIterator.next();
        column = (TableColumn)columnMap.get(key);
        if(column.getAttributeType()!=null && (column.getAttributeType().equals("object_ref") || column.getAttributeType().equals("object_ref_list"))){
            tableName = column.getTableIndex();
            tableName = tableName.substring(0, tableName.indexOf("."));
            repeatedDataColumnMap.put(tableName, "LCSSAMPLEREQUEST.IDA2A2");

        }
    }


    tg.setRepeatedDataColumnMap(repeatedDataColumnMap);
    tg.setShowRepeatedData(false);

flexg.setScope(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE);
flexg.setLevel(null);
%>





<%@ include file="/rfa/jsp/main/SearchResultsTable.jspf" %>

      <td>
      <div id="additionalActionsRowSource">

         <table>
             <tr>
                  <% if (hasAccessToSearchMatSuppliers){
                  %>
                  <td class="LABEL"><%=includeSupplierLabel%>:</td>
                  <td><input id="includeSupplierCheckBox" name="includeSupplierCheckBox" type=checkbox onClick="respondToClick(this, document.MAINFORM.includeSupplier)"></td>
                  <%}
                  if (hasAccessToSearchMatColors){
                  %>
                  <td class="LABEL">&nbsp;&nbsp;&nbsp;<%=includeColorLabel%>:</td>
                  <td><input id="includeColorCheckBox" name="includeColorCheckBox" type=checkbox onClick="respondToClick(this, document.MAINFORM.includeColor)" ></td>
                  <%}
                  if (hasAccessToSearchMatSamples){
                  %>
                  <td class="LABEL">&nbsp;&nbsp;&nbsp;<%=includeSampleLabel%>:</td>
                  <td><input id="includeSampleCheckBox" name="includeSampleCheckBox" type=checkbox onClick="respondToClick(this, document.MAINFORM.includeSample)" >&nbsp;&nbsp;&nbsp;</td>
                  <%} %>
                    <td class="button" align="right" valign="top">
                        <a class="button" href="javascript:refreshSearchResult()"><%=refreshButton%></a>
                    </td>

                <%if(!updateMode){%>
                <td class="LABEL">
                &nbsp;&nbsp;&nbsp;<%= actionsLabel%>&nbsp;

                 <select onChange="evalList(this)">
                   <option>
                   <%if(ACLHelper.hasCreateAccess(flexType) && !lcsContext.isVendor){%>
                     <option value="javascript:addSuppliers(this.parentNode)"><%=addSuppliersLabel %>
                  <%}%>
                   <%if(ACLHelper.hasCreateAccess(materialColorRoot)){%>
                     <option value="javascript:addColors()"><%=addColorsLabel %>
                  <%}%>
                   <%if(includeColor.equals("true") && ACLHelper.hasCreateAccess(colorDevelopmentSampleType)){%>
                   <option value="javascript:addColorDevelopmentSamples()"><%=addColorDevelopmetSampleLabel %>
                   <%}%>
                                    <%if(includeColor.equals("false") && includeSample.equals("false")){%>
                           <option value="javascript:compareObjects()"><%=compareMaterialsLabel %>
                                   <%}%>
                 </select>
                 </td>
                <%}%>


             </tr>
          </table>
          </div>

      </td>

<input type="hidden" name="typesString"  value="<%=FormatHelper.encodeForHTMLContent(typesString)%>">
<input type="hidden" name="includeSupplier"  value="<%=FormatHelper.encodeForHTMLContent(includeSupplier)%>">
<input type="hidden" name="includeColor"  value="<%=FormatHelper.encodeForHTMLContent(includeColor)%>">
<input type="hidden" name="includeSample"  value="<%=FormatHelper.encodeForHTMLContent(includeSample)%>">
<input type="hidden" name="searchBySupplier"  value="<%=FormatHelper.encodeForHTMLContent(searchBySupplier)%>">
<input type="hidden" name="searchByColor"  value="<%=FormatHelper.encodeForHTMLContent(searchByColor)%>">
<input type="hidden" name="searchBySample"  value="<%=FormatHelper.encodeForHTMLContent(searchBySample)%>">
<input type="hidden" name="sampleType"  value="<%=FormatHelper.encodeForHTMLContent(sampleTypeId)%>">
<input type="hidden" name="colorType"  value="<%=FormatHelper.encodeForHTMLContent(colorTypeId)%>">
<input type="hidden" name="supplierType"  value="<%=FormatHelper.encodeForHTMLContent(supplierTypeId)%>">
<input type="hidden" name="colorIds"  value="">
<input type="hidden" name="materialSupplierIds"  value="">
<input type="hidden" name="materialColorIds"  value="">
<input type="hidden" name="materialIds"  value="">

<%if(filter!=null){%>
    <input type="hidden" value="<%=filter %>" name="previousFilter">
<%}else{%>
    <input type="hidden" value="" name="previousFilter">
<%}%>
<%if(searchBySupplier.equals("true")){
    FlexTypeAttribute supplierNameAtt = supplierType.getAttribute("name");
    flexg.setScope(null);

%>
    <input type="hidden" value="<%= FormatHelper.encodeForHTMLContent(request.getParameter(supplierNameAtt.getSearchCriteriaIndex())) %>" name="<%=supplierNameAtt.getSearchCriteriaIndex()%>">
    <%= flexg.generateSearchCriteriaPlaceholders(supplierType, request) %>
    <% flexg.setScope(MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE);%>
    <%= flexg.generateSearchCriteriaPlaceholders(flexType, request) %>

<%
    flexg.setScope(null);
}
%>

<%if(searchByColor.equals("true")){
    flexg.setScope(null);

    FlexTypeAttribute colorNameAtt = colorType.getAttribute("name");
%>
    <input type="hidden" value="<%= FormatHelper.encodeForHTMLContent(request.getParameter(colorNameAtt.getSearchCriteriaIndex())) %>" name="<%=colorNameAtt.getSearchCriteriaIndex()%>">
    <%= flexg.generateSearchCriteriaPlaceholders(colorType, request) %>

    <%= flexg.generateSearchCriteriaPlaceholders(materialColorRoot, request) %>
<%
}
%>

<%if(searchBySample.equals("true")){
    FlexTypeAttribute sampleNameAtt = sampleType.getAttribute("name");
    FlexTypeAttribute sampleRequestNameAtt = sampleType.getAttribute("requestName");

%>
    <input type="hidden" value="<%= FormatHelper.encodeForHTMLContent(request.getParameter(sampleNameAtt.getSearchCriteriaIndex())) %>" name="<%=sampleNameAtt.getSearchCriteriaIndex()%>">
    <%= flexg.generateSearchCriteriaPlaceholders(sampleType, request) %>

    <input type="hidden" value="<%= FormatHelper.encodeForHTMLContent(request.getParameter(sampleRequestNameAtt.getSearchCriteriaIndex())) %>" name="<%=sampleRequestNameAtt.getSearchCriteriaIndex()%>">
    <%
    flexg.setScope(com.lcs.wc.sample.SampleRequestFlexTypeScopeDefinition.SAMPLEREQUEST_SCOPE);
    %>
    <%= flexg.generateSearchCriteriaPlaceholders(sampleType, request) %>

<%  flexg.setScope(com.lcs.wc.sample.SampleRequestFlexTypeScopeDefinition.SAMPLE_SCOPE);
    %>
    <%= flexg.generateSearchCriteriaPlaceholders(sampleType, request) %>
<%
}
%>

<script>


var additionalActionsRowSource = document.getElementById("additionalActionsRowSource").innerHTML;
document.getElementById("additionalActionsRowSource").innerHTML="";

var additionalActionsRow = document.getElementById("additionalActionsRow");

additionalActionsRow.innerHTML = additionalActionsRowSource;


  <%if(includeSupplier.equals("true")){%>
    var targetCheckbox = document.getElementById("includeSupplierCheckBox");
    targetCheckbox.checked = true;
    <%if(searchBySupplier.equals("true") || lcsContext.isVendor){%>
      targetCheckbox.disabled = true;
    <%}%>
  <%}%>

  <%if(includeColor.equals("true")){%>
    var targetCheckbox = document.getElementById("includeColorCheckBox");
    targetCheckbox.checked = true;
    <%if(searchByColor.equals("true")){%>
      targetCheckbox.disabled = true;
      targetCheckbox = document.getElementById("includeSupplierCheckBox");
      targetCheckbox.disabled = true;
    <%}%>
  <%}%>

  <%if(includeSample.equals("true")){%>
    var targetCheckbox = document.getElementById("includeSampleCheckBox");
    targetCheckbox.checked = true;
    <%if(searchBySample.equals("true")){%>
      targetCheckbox.disabled = true;
      targetCheckbox = document.getElementById("includeSupplierCheckBox");
      targetCheckbox.disabled = true;
      <%if(FormatHelper.getObjectId(colorDevelopmentSampleType).equals(sampleTypeId)){%>
          targetCheckbox = document.getElementById("includeColorCheckBox");
          targetCheckbox.disabled = true;
      <%}%>

    <%}%>
  <%}%>


</script>

<script>

    function respondToClick(inputCheckbox, input){
        if(inputCheckbox.checked){
            input.value = "true";
        }else if(!inputCheckbox.checked){
            input.value = "false";
        }
        if(inputCheckbox.name=="includeColorCheckBox" && inputCheckbox.checked){
            var targetCheckbox = document.getElementById("includeSupplierCheckBox");
            targetCheckbox.checked = true;
            document.MAINFORM.includeSupplier.value = "true";

        }else if(inputCheckbox.name=="includeSupplierCheckBox" && !inputCheckbox.checked){
            document.MAINFORM.includeSupplier.value = "false";
            var targetCheckbox = document.getElementById("includeColorCheckBox");
            if (targetCheckbox ){
                targetCheckbox.checked = false;
            }
            document.MAINFORM.includeColor.value = "false";
            targetCheckbox = document.getElementById("includeSampleCheckBox");
            if ( targetCheckbox ){
                targetCheckbox.checked = false;
            }
            document.MAINFORM.includeSample.value = "false";

        }else if(inputCheckbox.name=="includeColorCheckBox" && !inputCheckbox.checked){
            document.MAINFORM.includeColor.value = "false";
            //targetCheckbox = document.getElementById("includeSampleCheckBox");
            //targetCheckbox.checked = false;
            //document.MAINFORM.includeSample.value = "false";

        }else if(inputCheckbox.name=="includeSampleCheckBox" && inputCheckbox.checked){
            document.MAINFORM.includeSample.value = "true";
            var targetCheckbox = document.getElementById("includeSupplierCheckBox");
            targetCheckbox.checked = true;
            document.MAINFORM.includeSupplier.value = "true";
        /**not sure what to do with this yet

            targetCheckbox = document.getElementById("includeColorCheckBox");
            targetCheckbox.checked = true;
            document.MAINFORM.includeColor.value = "true";
        ***/

        }
        document.MAINFORM.activity.value = document.REQUEST.activity;
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = document.SEARCH_RESULTS.flexTypeObjectId;
        if(hasContent(document.REQUEST.oid)){
        document.MAINFORM.oid.value = document.REQUEST.oid;
        }
        submitForm();
    }


function refreshSearchResult(){
        document.MAINFORM.activity.value = document.REQUEST.activity;
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = document.SEARCH_RESULTS.flexTypeObjectId;
        if(hasContent(document.REQUEST.oid)){
        document.MAINFORM.oid.value = document.REQUEST.oid;
        }
        submitForm();
}


var objTree = new jsTree();

var requiredAttArray = new Array();

var divWindow;


/////////////Add Colors///////////////////////////////


function addColors(){

    var selectedSC = getCheckBoxIds(document.MAINFORM.selectedIds);

    var selectedMaterials = "";
    var selectedMaterialSuppliers = "";
    var selectedMaterialColors = "";

    var allInputs = document.getElementsByTagName("INPUT");
    for (var k=0; k < allInputs.length ; k++) {
        if (allInputs[k].type == "checkbox" && allInputs[k].name=="selectedIds" && allInputs[k].checked == true) {
            if(allInputs[k].value.indexOf("VR:com.lcs.wc.material.LCSMaterial:") > -1){
                selectedMaterials = buildMOA(selectedMaterials, allInputs[k].value);
            }else if(allInputs[k].value.indexOf("VR:com.lcs.wc.material.LCSMaterialSupplier:") > -1){
                selectedMaterialSuppliers = buildMOA(selectedMaterialSuppliers, allInputs[k].value);
            }else if(allInputs[k].value.indexOf("OR:com.lcs.wc.material.LCSMaterialColor:") > -1){
                selectedMaterialColors = buildMOA(selectedMaterialColors, allInputs[k].value);
            }
        }
    }

    document.MAINFORM.materialIds.value = selectedMaterials;
    document.MAINFORM.materialSupplierIds.value = selectedMaterialSuppliers;
    document.MAINFORM.materialColorIds.value = selectedMaterialColors;

    if(hasContent(selectedSC)){
        divWindow = new jsWindow(document.body.clientWidth-30, Math.min(document.documentElement.clientHeight, document.body.clientHeight)-30, 5, 5, 30, "", 20, true, true, true);
        divWindow.showProcessingMessage();
        runPostAjaxRequest(urlContext + '/jsp/main/Chooser.jsp', 'ajaxWindow=true&detailRequest=false&module=COLOR&activity=FIND_COLOR&multiple=true&selectParameter=completeAddColors&idOnly=false&objectIdType=version&rootTypeId=<%=FormatHelper.getObjectId(colorRoot)%>&type=<%=FormatHelper.getObjectId(colorRoot)%>', 'ajaxDefaultResponse');
    }
}




 function completeAddColors(ids){
        document.MAINFORM.colorIds.value = ids;
        runPostAjaxRequest(urlContext+ '<%=PageManager.getMasterControllerUrl()%>','activity=BULK_CREATE_MATERIALCOLORS&ajaxWindow=true&colorIds=' + ids + '&materialSupplierIds=' + document.MAINFORM.materialSupplierIds.value + '&materialColorIds=' + document.MAINFORM.materialColorIds.value  + '&materialIds=' + document.MAINFORM.materialIds.value, 'ajaxSaveAddColorsFinish');
}


function ajaxSaveAddColorsFinish(xml, text){
    text = replaceAll(text, "MAINFORM", "AJAXFORM");
    divWindow.addHTML(text);
    var selectAllCheckbox = document.getElementById('selectAllChooserCheckBox');
    selectAllCheckbox.checked = true;
    toggleAllChooserItems();
    parseScripts(text);
}



function toggleAllChooserItems() {
    var selectAllCheckbox = document.getElementById('selectAllChooserCheckBox');

    var checkbox = document.getElementsByName('selectedChooserIds');
    for (i=0; i<checkbox.length; i++) {
        if (selectAllCheckbox.checked) {
            checkbox[i].checked = true;
        } else {
            checkbox[i].checked = false;
        }
    }
}

function createMaterialColors(){
        var dataStringArray = ajaxFormElementsArray(document.AJAXFORM, document.AJAXFORM.selectedChooserIds);
        var missingRequiredAttribute = false;
        var tableKey;
        var attKey;

        if(dataStringArray['selectedChooserIds']){
            for (tableKey in dataStringArray){
                for (attKey in requiredAttArray){
                        if(tableKey.indexOf("_" + attKey) > -1 && (tableKey.indexOf("_" + attKey) + attKey.length + 1)==tableKey.length){
                            if(tableKey.indexOf("M_F_")>-1 && dataStringArray['selectedChooserIds'].indexOf(tableKey.substring(0, tableKey.indexOf("_LCSMATERIALCOLOR"))) > -1){
                                if(!hasContentAllowZero(dataStringArray[tableKey]) || (document.getElementById(tableKey + "Display") && dataStringArray[tableKey]=='0')){
                                    alert(mustEnterValueForMSG + requiredAttArray[attKey]);
                                    missingRequiredAttribute = true;
                                    break;
                                }
                            }
                        }
                }
                if(missingRequiredAttribute){
                    break;
                }
            }
            if(!missingRequiredAttribute && hasContent(dataStringArray['selectedChooserIds'])){
                var dataString = ajaxFormElementsString(document.AJAXFORM, document.AJAXFORM.selectedChooserIds);
                divWindow.showProcessingMessage();
                runPostAjaxRequest(urlContext+ '<%=PageManager.getMasterControllerUrl()%>','activity=BULK_CREATE_MATERIALCOLORS&action=SAVE&dataString=' + dataString, 'ajaxCreateMCFinish');
            }
        }
}




function ajaxCreateMCFinish(xml, text){
    divWindow.addHTML(text);
}



//////////////End Add Colors///////////////////


//////////////Add Suppliers///////////////////

function addSuppliers(el){
    var selectedSC = getCheckBoxIds(document.MAINFORM.selectedIds);
    var selectedMaterials = "";
    var selectedMaterialSuppliers = "";
    var selectedMaterialColors = "";

    var allInputs = document.getElementsByTagName("INPUT");
    for (var k=0; k < allInputs.length ; k++) {
        if (allInputs[k].type == "checkbox" && allInputs[k].name=="selectedIds" && allInputs[k].checked == true) {
            if(allInputs[k].value.indexOf("VR:com.lcs.wc.material.LCSMaterial:") > -1){
                selectedMaterials = buildMOA(selectedMaterials, allInputs[k].value);
            }else if(allInputs[k].value.indexOf("VR:com.lcs.wc.material.LCSMaterialSupplier:") > -1){
                selectedMaterialSuppliers = buildMOA(selectedMaterialSuppliers, allInputs[k].value);
            }else if(allInputs[k].value.indexOf("OR:com.lcs.wc.material.LCSMaterialColor:") > -1){
                selectedMaterialColors = buildMOA(selectedMaterialColors, allInputs[k].value);
            }
        }
    }

    document.MAINFORM.materialIds.value = selectedMaterials;
    document.MAINFORM.materialSupplierIds.value = selectedMaterialSuppliers;
    document.MAINFORM.materialColorIds.value = selectedMaterialColors;

    if(hasContent(selectedSC)){
        divWindow = new jsWindow(document.body.clientWidth-30, Math.min(document.documentElement.clientHeight, document.body.clientHeight)-30, 5, 5, 30, "", 20, true, true, true);
        runPostAjaxRequest(urlContext + '/jsp/main/Chooser.jsp', 'ajaxWindow=true&detailRequest=false&module=SUPPLIER&activity=FIND_SUPPLIER&multiple=true&selectParameter=completeAddSuppliers&idOnly=false&objectIdType=version&rootTypeId=<%=FormatHelper.getObjectId(supplierRoot)%>&type=<%=FormatHelper.getObjectId(supplierRoot)%>', 'ajaxDefaultResponse');
    }
}




 function completeAddSuppliers(ids){
        divWindow.showProcessingMessage();
        runPostAjaxRequest(urlContext+ '<%=PageManager.getMasterControllerUrl()%>','activity=BULK_CREATE_MATERIALSUPPLIERS&ajaxWindow=true&supplierIds=' + ids + '&materialSupplierIds=' + document.MAINFORM.materialSupplierIds.value + '&materialColorIds=' + document.MAINFORM.materialColorIds.value  + '&materialIds=' + document.MAINFORM.materialIds.value + "&materialTypeId=<%=FormatHelper.getObjectId(flexType)%>", 'ajaxSaveAddSuppliersFinish');
}


function ajaxSaveAddSuppliersFinish(xml, text){
    text = replaceAll(text, "MAINFORM", "AJAXFORM");
    divWindow.addHTML(text);
    var selectAllCheckbox = document.getElementById('selectAllChooserCheckBox');
    selectAllCheckbox.checked = true;
    toggleAllChooserItems();
    parseScripts(text);
}




function createMaterialSuppliers(){
        var dataStringArray = ajaxFormElementsArray(document.AJAXFORM, document.AJAXFORM.selectedChooserIds);
        var missingRequiredAttribute = false;
        var tableKey;
        var attKey;
        if(dataStringArray['selectedChooserIds']){
            for (tableKey in dataStringArray){
                for (attKey in requiredAttArray){
                        if(tableKey.indexOf("_" + attKey) > -1 && (tableKey.indexOf("_" + attKey) + attKey.length + 1)==tableKey.length){
                            if(tableKey.indexOf("M_F_")>-1 && dataStringArray['selectedChooserIds'].indexOf(tableKey.substring(0, tableKey.indexOf("_LCSMATERIALSUPPLIER"))) > -1){
                                if(!hasContentAllowZero(dataStringArray[tableKey]) || (document.getElementById(tableKey + "Display") && dataStringArray[tableKey]=='0')){
                                    alert(mustEnterValueForMSG + requiredAttArray[attKey]);
                                    missingRequiredAttribute = true;
                                    break;
                                }
                            }
                        }
                }
                if(missingRequiredAttribute){
                    break;
                }
            }
            if(!missingRequiredAttribute && hasContent(dataStringArray['selectedChooserIds'])){
                var dataString = ajaxFormElementsString(document.AJAXFORM, document.AJAXFORM.selectedChooserIds);
                divWindow.showProcessingMessage();
                runPostAjaxRequest(urlContext+ '<%=PageManager.getMasterControllerUrl()%>','activity=BULK_CREATE_MATERIALSUPPLIERS&action=SAVE&dataString=' + dataString, 'ajaxCreateMSFinish');
            }
        }
}




function ajaxCreateMSFinish(xml, text){
    divWindow.addHTML(text);


}



//////////////End Add Suppliers///////////////////


///////////Add Color Development Sample ////////////////////



function addColorDevelopmentSamples(){

    var selectedSC = getCheckBoxIds(document.MAINFORM.selectedIds);

    var continueCreation = true;
    if(hasContent(selectedSC)){



        var selectedMaterials = "";
        var selectedMaterialSuppliers = "";
        var selectedMaterialColors = "";

        var allInputs = document.getElementsByTagName("INPUT");
        for (var k=0; k < allInputs.length ; k++) {
            if (allInputs[k].type == "checkbox" && allInputs[k].name=="selectedIds" && allInputs[k].checked == true) {
                if(allInputs[k].value.indexOf("VR:com.lcs.wc.material.LCSMaterial:") > -1){
                    //selectedMaterials = buildMOA(selectedMaterials, allInputs[k].value);
                }else if(allInputs[k].value.indexOf("VR:com.lcs.wc.material.LCSMaterialSupplier:") > -1){
                    alert('<%=FormatHelper.encodeForJavascript(selectedObjectsDoNotHaveAssociatedColorMessage)%>');
                    continueCreation = false;
                    //selectedMaterialSuppliers = buildMOA(selectedMaterialSuppliers, allInputs[k].value);
                }else if(allInputs[k].value.indexOf("OR:com.lcs.wc.material.LCSMaterialColor:") > -1){
                    selectedMaterialColors = buildMOA(selectedMaterialColors, allInputs[k].value);
                }
            }
        }
        if(continueCreation){
            divWindow = new jsWindow(document.body.clientWidth-30, Math.min(document.documentElement.clientHeight, document.body.clientHeight)-30, 5, 5, 30, "", 20, true, true, true);
            divWindow.showProcessingMessage();

            document.MAINFORM.materialIds.value = selectedMaterials;
            document.MAINFORM.materialSupplierIds.value = selectedMaterialSuppliers;
            document.MAINFORM.materialColorIds.value = selectedMaterialColors;
            runPostAjaxRequest(urlContext+ '<%=PageManager.getMasterControllerUrl()%>','activity=BULK_CREATE_COLORDEVELOPMENTS&ajaxWindow=true&materialSupplierIds=' + document.MAINFORM.materialSupplierIds.value + '&materialColorIds=' + document.MAINFORM.materialColorIds.value  + '&materialIds=' + document.MAINFORM.materialIds.value, 'ajaxSaveAddColorDevelopmentFinish');
        }
    }
}

function ajaxSaveAddColorDevelopmentFinish(xml, text){
    text = replaceAll(text, "MAINFORM", "AJAXFORM");
    divWindow.addHTML(text);
    var selectAllCheckbox = document.getElementById('selectAllChooserCheckBox');
    selectAllCheckbox.checked = true;
    toggleAllChooserItems();
    parseScripts(text);

}


function completeCreateColorDevelopmentSamples(){

        var missingRequiredAttribute = false;
        var dataStringArray = ajaxFormElementsArray(document.AJAXFORM, document.AJAXFORM.selectedChooserIds);
        var tableKey;
        var attKey;
        if(dataStringArray['selectedChooserIds']){

            for (tableKey in dataStringArray){
                for (attKey in requiredAttArray){
                        if(tableKey.indexOf("_" + attKey) > -1 && (tableKey.indexOf("_" + attKey) + attKey.length + 1)==tableKey.length){
                            if(tableKey.indexOf("M_F_")>-1 && dataStringArray['selectedChooserIds'].indexOf(tableKey.substring(0, tableKey.indexOf("_LCSSAMPLEREQUEST"))) > -1){
                                if(!hasContentAllowZero(dataStringArray[tableKey]) || (document.getElementById(tableKey + "Display") && dataStringArray[tableKey]=='0')){
                                    alert(mustEnterValueForMSG + requiredAttArray[attKey]);
                                    missingRequiredAttribute = true;
                                    break;
                                }
                            }
                        }
                }
                if(missingRequiredAttribute){
                    break;
                }
            }

            if(!missingRequiredAttribute && hasContent(dataStringArray['selectedChooserIds'])){
                var dataString = ajaxFormElementsString(document.AJAXFORM, document.AJAXFORM.selectedChooserIds);
                divWindow.showProcessingMessage();
                runPostAjaxRequest(urlContext+ '<%=PageManager.getMasterControllerUrl()%>','activity=BULK_CREATE_COLORDEVELOPMENTS&action=SAVE&dataString=' + dataString, 'ajaxCreateColorDevelopmentFinish');
            }
        }

}



function ajaxCreateColorDevelopmentFinish(xml, text){
    divWindow.addHTML(text);

}


///////////End Add Color Development Sample ////////////////////

///////////Start Compare Materials/////////////

function compareObjects(firstOid){
    if(firstOid){
        hideDiv("columnHide" + CompareObjects.tableId, "divFrame" + CompareObjects.tableId);
    }
    var group = document.MAINFORM.selectedIds;

        var selectedObjects = getCheckBoxIds(group);

    var selectedCount = 0;
    for (var k =0; k < group.length; k++)  {
                if (group[k].checked){
                           selectedCount ++;
                }
            }
    if (selectedCount == 1){
            alert('<%=needToSelectMoreThanOneCompareMaterialsMSG%>' );
            return false;
        }
     else if (selectedCount > <%=CompareMaterials_MAXNUMBER%>){
            alert('<%=excceedMaxNumberCompareMaterialsMSG%>' );
            return false;
        }

        var compflexType = '<%=FormatHelper.encodeAndFormatForHTMLContent(type)%>';

    var allInputs = document.getElementsByTagName("INPUT");

    document.MAINFORM.materialIds.value = selectedObjects;

    if(hasContent(selectedObjects)){
        divWindow = new jsWindow(document.body.clientWidth-30, Math.min(document.documentElement.clientHeight, document.body.clientHeight), 5, 5, 30, "", 20, true, true, true);
        divWindow.showProcessingMessage();

            runPostAjaxRequest(urlContext + '/jsp/compare/CompareController.jsp', 'ajaxWindow=true&activity=COMPARE_OBJECT&oids='+selectedObjects+ '&compareFlexType='+compflexType + '&firstSelectedObject='+ firstOid, 'ajaxDefaultResponse');

        }
}
</script>
