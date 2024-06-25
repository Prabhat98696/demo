<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@ page language="java"
    errorPage="../exception/ControlException.jsp"
        import="com.lcs.wc.db.SearchResults,
                com.lcs.wc.client.web.PageManager,
                com.lcs.wc.client.web.WebControllers,
                com.lcs.wc.client.Activities,
                com.lcs.wc.epmstruct.LCSEPMDocumentHelper,
                com.lcs.wc.partstruct.LCSPartToProductLinkHelper,
                com.lcs.wc.flextype.*,
                com.lcs.wc.specification.FlexSpecification,
                com.lcs.wc.foundation.*,
                com.lcs.wc.db.*,
                com.lcs.wc.placeholder.*,
                com.lcs.wc.product.*,
                com.lcs.wc.part.*,
                com.lcs.wc.specification.*,
                wt.part.*,
                wt.util.*,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
                java.util.*,
                wt.fc.*,
                com.lcs.wc.sourcing.*,
                com.lcs.wc.season.*,
                com.lcs.wc.util.*,
                com.lcs.wc.client.web.ProductPageNames"
%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="productModel" scope="request" class="com.lcs.wc.product.LCSProductClientModel" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="request"/>
<jsp:useBean id="flexPlmWebRequestContext" scope="request" class="com.lcs.wc.product.html.FlexPlmWebRequestContext"/>
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>"/>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// STATIS JSP CODE //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.v2.product.ProductController");
    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");

    public static final String JSPNAME = "ProductController";

    public static final String MAINTEMPLATE = PageManager.getPageURL("MAINTEMPLATE", null);
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
    wt.util.WTContext.getContext().setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));
   response.setContentType("text/html; charset="+ defaultCharsetEncoding);
   String viewPgTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "view_PG_TLE", RB.objA ) ;
   String informationPgTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "information_PG_TLE", RB.objA ) ;
   String createPgTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "create_PG_TLE", RB.objA ) ;
   String copyPgTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "copy_PG_TLE", RB.objA ) ;
   String updatePgTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "update_PG_TLE", RB.objA ) ;
   String detailsPgTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "details_PG_TLE", RB.objA ) ;
   String searchCriteriaPgTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "searchCriteria_PG_TLE", RB.objA ) ;
   String searchResultsPgTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "searchResults_PG_TLE", RB.objA ) ;
   String specificationPgTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "specification_PG_TLE", RB.objA ) ;
   String documentDetailsPgTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "documentDetails_PG_TLE", RB.objA ) ;
   String ProductLabel = WTMessage.getLocalizedMessage ( RB.PRODUCT, "product_LBL", RB.objA ) ;
   String prodOfferingPgTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "prodOffering_PG_TLE",RB.objA ) ;
   String seasonLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "seasonColon_LBL", RB.objA ) ;
   String productLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "product_LBL", RB.objA ) ;
   String skuLabel = WTMessage.getLocalizedMessage ( RB.SEASON, "sku_LBL", RB.objA ) ;
   String updateProductTle = WTMessage.getLocalizedMessage ( RB.SEASON, "updateProduct_LBL", RB.objA ) ;
   String clipboardPgTle = WTMessage.getLocalizedMessage ( RB.CLIPBOARD, "clipboard_PG_TLE", RB.objA ) ;

    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String oid = request.getParameter("oid");
    String returnActivity = request.getParameter("returnActivity");
    String returnAction = request.getParameter("returnAction");
    String returnOid = request.getParameter("returnOid");
    String seasonId = request.getParameter("seasonId");
    String contextSKUId = appContext.getContextSKUId();
    boolean checkReturn = false;



    String componentlist = "";
    String seasonstring = "";
    String producttype = "";
    String copymode = "";

    String title = "";
    String errorMessage = request.getParameter("errorMessage");
    if (FormatHelper.hasContent(errorMessage)) {
        errorMessage = java.net.URLDecoder.decode(errorMessage, defaultCharsetEncoding);
    } else {
      errorMessage = "";
    }
    String infoMessage = request.getParameter("infoMessage");
    String view = null;
    String type = request.getParameter("type");
    String templateType = "";
    String formType = "";

    FlexType flexType = null;
    String contextHeaderPage = "";
    String additionalParameters = "";
    // Check that the placeholderId has a prefix. If not add it.

    String placeholderId = request.getParameter("placeholderId");
    if (FormatHelper.hasContent(placeholderId)) {
        if (placeholderId.indexOf(":") < 0)
        {
            placeholderId = "OR:com.lcs.wc.placeholder.Placeholder:" + placeholderId;
            if (logger.isDebugEnabled()){
                logger.debug("placeholderId = " + placeholderId);
            }
        }
    }
    ///////////////////////////////////////////////////////////////////
    if("VIEW_PRODUCT".equals(activity)){
        lcsContext.setCacheSafe(true);
        view = "VIEW_SP_PAGE";
        activity = "VIEW_SEASON_PRODUCT_LINK";
        //Restyled Header
        contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
        request.setAttribute("productPageTitle", "Details");
        request.setAttribute("layoutType", "DIV_LAYOUT");
        title = viewPgTle + ProductLabel + "/" + seasonLabel + informationPgTle;
        if(FormatHelper.parseBoolean(request.getParameter("autoSelectProductMenus"))){
            oid = appContext.setAutoSelectedProductId(oid);
        }else {
            appContext.setProductContext(oid);
        }

        if(oid != null && oid.indexOf("LCSProduct:") > -1 && oid.indexOf("LCSProductCostSheet")<0  && oid.indexOf("LCSProductSeasonLink")<0)
            productModel.load(oid);


    } else if("VIEW_PRODUCT_SKU_CREATE_PAGE".equals(activity)){
        title = createPgTle + ProductLabel;
        view = "CREATE_PRODUCT_SKU_COMPLETE_PAGE";


    ///////////////////////////////////////////////////////////////////
    } else if("CREATE_PRODUCT".equals(activity)){
        if(FormatHelper.hasContent(placeholderId)){
            additionalParameters = additionalParameters + "&placeholderId=" + placeholderId;
        }
        if(action == null || "INIT".equals(action) || "".equals(action)){
            title = createPgTle + ProductLabel;
            view = PageManager.CLASSIFY_PAGE;

        } else if("CLASSIFY".equals(action)){
            if(FormatHelper.hasContent(request.getParameter("directClassifyId"))){
                productModel.setTypeId(request.getParameter("directClassifyId"));
            }
            AttributeValueSetter.setAllAttributes(productModel, RequestHelper.hashRequest(request));
           /* if(FormatHelper.hasContent(placeholderId)){
                Placeholder ph = (Placeholder)LCSQuery.findObjectById(placeholderId);
                productModel.setTypeId(FormatHelper.getObjectId(ph.getFlexType()));
            }*/
            title = createPgTle + ProductLabel;
            String requestSeasonId=FormatHelper.getObjectId(productModel.getSeason());
            String appContextSeasonId=FormatHelper.getObjectId(appContext.getSeason());
            if(productModel.getSeason() != null){
                if(!requestSeasonId.equals(appContextSeasonId)){
                    appContext.setSeasonContext(FormatHelper.getObjectId(productModel.getSeason()));
                }
                view = PageManager.CREATE_PRODUCT_SEASON_PAGE;
            } else {
                view = PageManager.CREATE_PRODUCT_PAGE;
            }
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            request.setAttribute("layoutType", "DIV_LAYOUT");
            formType = "image";

        }else if("CREATE_ANOTHER".equals(action)){
            //AttributeValueSetter.setAllAttributes(productModel, RequestHelper.hashRequest(request));
            if(FormatHelper.hasContent(request.getParameter("directClassifyId"))){
                productModel.setTypeId(request.getParameter("directClassifyId"));
            }
           /* if(FormatHelper.hasContent(placeholderId)){
                Placeholder ph = (Placeholder)LCSQuery.findObjectById(placeholderId);
                productModel.setTypeId(FormatHelper.getObjectId(ph.getFlexType()));
            }*/
            title = createPgTle + ProductLabel;
            view = PageManager.CREATE_PRODUCT_PAGE;
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            request.setAttribute("layoutType", "DIV_LAYOUT");
            formType = "image";

            }else if("SAVE".equals(action)){
            try {
                AttributeValueSetter.setAllAttributes(productModel, RequestHelper.hashRequest(request));
                if(lcsContext.isVendor){
                    productModel.setSelectedVendorsId(request.getParameter("selectedVendorSuppliers"));
                }
                productModel.save();
                oid = FormatHelper.getVersionId(productModel.getBusinessObject());
                view = PageManager.SAFE_PAGE;
                title = viewPgTle + ProductLabel;
                action = "INIT";
                activity = Activities.VIEW_PRODUCT;
                additionalParameters = additionalParameters +"&canCreateAnother=true";
                checkReturn = true;

                session.setAttribute("lastCreatedOID", FormatHelper.getVersionId(productModel.getBusinessObject()));

                // IF WE ARE ADDING TO A SEASON, THEN TAKE THE USER
                // DIRECTLY TO THE UPDATE SEASON PRODUCT LINK PAGE.
                if(productModel.getSeason() != null){
                    LCSSeasonProductLink link = LCSSeasonQuery.findSeasonProductLink(productModel.getBusinessObject(), productModel.getBusinessObject().findSeasonUsed());
                    //appContext.setProductContext(FormatHelper.getObjectId(link));
                    oid = FormatHelper.getObjectId(link);
                    checkReturn = false;
                    action = "PSEUDO_CREATE";
                    view = "SAFE_PAGE";
                    activity = "UPDATE_SEASON_PRODUCT_LINK";
                    appContext.setProductContext(oid);
                    additionalParameters = additionalParameters + "&autoSelectProductMenus=true";

                }
                else{
                    appContext.setProductContext(oid);
                }

           } catch(LCSException e){
                view = PageManager.CREATE_PRODUCT_PAGE;
                title = createPgTle + ProductLabel;
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
                request.setAttribute("layoutType", "DIV_LAYOUT");
                // Exception is untyped (it is too generic), most likely we want to have the same FORM as the one we
                // got to get here (PRODUCT_UPDATE/SAVE). MV. TODO: We need to subtype exceptions to change formType according to
                // error.
                formType = "image";
                errorMessage=e.getLocalizedMessage();
           }
        }

        flexType = productModel.getFlexType();
    ///////////////////////////////////////////////////////////////////
    } else if("COPY_PRODUCT".equals(activity) || "COPY_PRODUCT_FROM_SEASON".equals(activity)){
        if(action == null || "COMPONENT_INIT".equals(action) || "".equals(action)){
            lcsContext.setCacheSafe(true);
            productModel.load(request.getParameter("copyFromOid"));
            LCSProduct revAProduct = (LCSProduct) VersionHelper.getVersion(productModel.getBusinessObject().getMaster(), "A");

            productModel.load(FormatHelper.getVersionId(revAProduct));

            if(FormatHelper.hasContent(seasonId)){
                productModel.setSeasonId(seasonId);
            }

            title = copyPgTle + ProductLabel;
            view = "SELECT_PRODUCT_COMPONENTS_PAGE";


        } else if(action == null || "INIT".equals(action) || "".equals(action)){

            productModel.load(request.getParameter("copyFromOid"));
            LCSProduct revAProduct = (LCSProduct) VersionHelper.getVersion(productModel.getBusinessObject().getMaster(), "A");

            productModel.load(FormatHelper.getVersionId(revAProduct));

            componentlist = request.getParameter("componentList");
            seasonstring = request.getParameter("toSeason");
            producttype = request.getParameter("productType");
            copymode = request.getParameter("copyMode");

            if(FormatHelper.hasContent(producttype)) {
                RetypeLogic.changeType(productModel, producttype, false);
                //FlexType newFlexType = (FlexType)FlexTypeCache.getFlexType(producttype);
                //productModel.setFlexType(newFlexType);
            }

            if(FormatHelper.hasContent(seasonId)){
                productModel.setSeasonId(seasonId);
            }
            if (FormatHelper.hasContent(copymode)) {
                PropertyBasedAttributeValueLogic.setAttributes(productModel, "com.lcs.wc.product.LCSProduct", "", "COPY."+copymode);
            }

            else {
            PropertyBasedAttributeValueLogic.setAttributes(productModel, "com.lcs.wc.product.LCSProduct", "", "COPY");
            }
            if(FormatHelper.hasContent(placeholderId)){
                additionalParameters = "&placeholderId="+ placeholderId;
            }

            FlexType productType = productModel.getFlexType();
            FlexTypeAttribute attributeCheck = productType.getAttribute("productName");
            String attType = attributeCheck.getAttVariableType();
            if(!"derivedString".equals(attType))  {
                String copyOf = WTMessage.getLocalizedMessage ( RB.DOCUMENT, "copyOf_LBL", RB.objA ) ;
                productModel.setValue("productName", copyOf + " " + productModel.getValue("productName"));
            }
            title = copyPgTle + ProductLabel;
            view = "COPY_PRODUCT_PAGE";

        } else if("SAVE".equals(action) ){


           componentlist = request.getParameter("componentsList");
            producttype = request.getParameter("copyProductType");
            copymode = request.getParameter("copyModes");
            seasonstring = request.getParameter("copySeason");
            if (logger.isDebugEnabled()){
                logger.debug("\n>>>>>>>>>>>>>componentlist = " + componentlist);
                logger.debug(">>>>>>>>>>>>seasonstring = " + seasonstring);
                logger.debug(">>>>>>>>>producttype = " + producttype);
                logger.debug(">>>>>>>>>>copymode = " + copymode);
            }

            try {

                //SPR 2145951 copy only image attributes firstly
                LCSProduct sourceprod = (LCSProduct)LCSQuery.findObjectById(request.getParameter("copyFromOid"));
                //force to get A rev for product in case of season product case
                sourceprod = SeasonProductLocator.getProductARev(sourceprod);
                AttributeValueSetter.copyImageAttributes((FlexTyped)sourceprod, (FlexTyped)productModel);

                AttributeValueSetter.setAllAttributes(productModel, RequestHelper.hashRequest(request));
                productModel.setParameters(componentlist, seasonstring, producttype, copymode);
                productModel.copyEnhanced();
                appContext.setProductContext(FormatHelper.getVersionId(productModel.getBusinessObject()));

                session.setAttribute("lastCreatedOID", FormatHelper.getVersionId(productModel.getBusinessObject()));

                if("A".equals("" + productModel.getBusinessObject().getVersionIdentifier().getValue())){
                    // COPIED REV A
                    oid = FormatHelper.getVersionId(productModel.getBusinessObject());
                    view = PageManager.SAFE_PAGE;
                    title = viewPgTle + ProductLabel;
                    action = "INIT";
                    activity = "VIEW_PRODUCT";
                    checkReturn = true;

                } else {

                    LCSSeasonProductLink link = LCSSeasonQuery.findSeasonProductLink(productModel.getBusinessObject(), productModel.getBusinessObject().findSeasonUsed());
                    if(FormatHelper.hasContent(placeholderId)){
                        link.setPlaceholder((Placeholder)LCSQuery.findObjectById(placeholderId) );
                    }

                    oid = FormatHelper.getObjectId(link);
                    checkReturn = false;
                    action = "PSEUDO_CREATE";
                    view = "SAFE_PAGE";
                    activity = "UPDATE_SEASON_PRODUCT_LINK";
                    additionalParameters = additionalParameters + "&pseudoCreateIsCopy=true";
                }

           } catch(LCSException e){
                view = "COPY_PRODUCT_PAGE";
                title = createPgTle + ProductLabel;
                errorMessage=e.getLocalizedMessage();
           }
        } else if("AJAXSEARCH".equals(action)&&"COPY_PRODUCT_FROM_SEASON".equals(activity)){
            //for SPR 2059541 ,only for placeholder ajax search , reorg placeholder search for
            //considering the  seasonid ,seleced product's attribute with placeholder scope and
            //product level. in here,mappedObject is the lcsproduct object, and mappedType is the placeholder.
           Map<String,String> attrimap = new HashMap<String,String>();
           String productARevId=request.getParameter("productARevId");
           String rootTypeId=request.getParameter("rootTypeId");
           String seasonMasterId=null;
           FlexTyped mappedObject=(FlexTyped) LCSQuery.findObjectById(productARevId);
           FlexType mappedType = FlexTypeCache.getFlexType(rootTypeId);
           // consider the authority of two objects
           if(!ACLHelper.hasViewAccess(mappedObject)||!ACLHelper.hasViewAccess(mappedType)){
               String stMessageKey="noAccessToViewThisType_MSG";
               throw new LCSAccessException(RB.EXCEPTION, stMessageKey,RB.objA);
           }
           if(FormatHelper.hasContent(seasonId)){
              LCSSeason season = (LCSSeason)LCSQuery.findObjectById(seasonId);
              seasonMasterId=FormatHelper.getNumericObjectIdFromObject((WTObject) season.getMaster());
           }

        //get the seasonmasterid as criteria
           if (FormatHelper.hasContent(seasonMasterId)) {
               attrimap.put("SEASONMASTERID", seasonMasterId);
             }
           if (FormatHelper.hasContent(rootTypeId)) {
               attrimap.put("childFlexType", rootTypeId);
             }



          String additionalCriteria="";
          if (!attrimap.isEmpty()){
               StringBuffer buffer = new StringBuffer();
               int i=0;
               String att="";
               String otherAtts="";
               for (Iterator<String> it = attrimap.keySet().iterator(); it.hasNext();) {
                     att = it.next();
                     otherAtts = attrimap.get(att);
                     if (i==0){
                         buffer.append(att);
                         buffer.append(MultiObjectHelper.NAME_VALUE_DELIMITER);
                         buffer.append(otherAtts);
                     }else{
                         buffer.append(MultiObjectHelper.ROW_DELIMITER);
                         buffer.append(att);
                         buffer.append(MultiObjectHelper.NAME_VALUE_DELIMITER);
                         buffer.append(otherAtts);
                     }
                     i++;
                }
               if (logger.isDebugEnabled()) {
                   logger.debug("generate the additional criteria: "+ buffer.toString());
                }
               additionalCriteria=buffer.toString();
               lcsContext.setCacheSafe(true);
               view = "AJAX_SEARCH";
               title = "";
               String contentPage = PageManager.getPageURL(view,null);
                %>
                <jsp:forward page="<%=subURLFolder+ contentPage %>">
                    <jsp:param name="title" value="<%= java.net.URLEncoder.encode(title, defaultCharsetEncoding)%>" />
                    <jsp:param name="additionalCriteria" value="<%= additionalCriteria%>" />
                </jsp:forward>
                <%
          }
        }

        flexType = productModel.getFlexType();

    } else if(Activities.UPDATE_PRODUCT.equals(activity)){

        if(action == null || "INIT".equals(action) || "".equals(action)){
            productModel.load(oid);
            view = PageManager.UPDATE_PRODUCT_PAGE;
            title = updateProductTle;
            formType = "image";
            contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
            request.setAttribute("layoutType", "DIV_LAYOUT");
        } else if("SAVE".equals(action)){
            try{
                additionalParameters = additionalParameters + "&forceReloadSideMenu=true";
                productModel.load(oid);
                String thumbnail = productModel.getPartPrimaryImageURL();
                boolean toRemove = FormatHelper.parseBoolean(request.getParameter("clearIfEmptyProductThumbnail"));
                String newThumb = request.getParameter("partPrimaryImageURL");
                if (FormatHelper.hasContent(newThumb))
                    newThumb = FormatHelper.formatImageUrl(newThumb);

                AttributeValueSetter.setAllAttributes(productModel, RequestHelper.hashRequest(request));

                if (toRemove) {
                    productModel.setPartPrimaryImageURL("");
                } else if (FormatHelper.hasContent(thumbnail) && !FormatHelper.hasContent(newThumb)) {
                    productModel.setPartPrimaryImageURL(thumbnail);
                }
                productModel.save();
                oid = FormatHelper.getVersionId(productModel.getBusinessObject());

                appContext.setProductContext(oid);

                view = PageManager.SAFE_PAGE;
                title = ProductLabel + detailsPgTle;
                action = "INIT";
                activity = Activities.VIEW_PRODUCT;
                checkReturn = true;

                if(!FormatHelper.hasContent(contextSKUId)){
                    contextSKUId = request.getParameter("contextSKUId");
                }
                additionalParameters = additionalParameters +"&contextSKUId="+contextSKUId;

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.UPDATE_PRODUCT_PAGE;
                title = updatePgTle + ProductLabel;
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
                request.setAttribute("layoutType", "DIV_LAYOUT");
            }
        }

        flexType = productModel.getFlexType();



    ///////////////////////////////////////////////////////////////////
    // ACTIVITY = FIND
    ///////////////////////////////////////////////////////////////////
    } else if(Activities.FIND_PRODUCT.equals(activity)){
        contextHeaderPage = "STANDARD_LIBRARY_CONTEXT_BAR";
        if(action == null || "".equals(action) || "INIT".equals(action)){
            view = PageManager.FIND_PRODUCT_CRITERIA;
            title = ProductLabel + searchCriteriaPgTle;
            type = "";

        } else if("CHANGE_TYPE".equals(action)){
            view = PageManager.FIND_PRODUCT_CRITERIA;
            title = ProductLabel + searchCriteriaPgTle;
            type = request.getParameter("type");
            flexType = FlexTypeCache.getFlexType(type);
        } else if("SEARCH".equals(action)){
            if("true".equals(request.getParameter("updateMode"))){
                formType = "image";
            }
			/*
			*	Agron custom code added.
			*/
			boolean isFromHeader = Boolean.parseBoolean(request.getParameter("checkAttribute"));
			if(isFromHeader){
				view = "FIND_PRODUCT_RESULTS_FROM_HEADER";
			}else{
            view = PageManager.FIND_PRODUCT_RESULTS;
				}
			
			/*
			*	Agron custom code ended.
			*/
            title = ProductLabel + searchResultsPgTle;

        } else if("AJAXSEARCH".equals(action)){
            lcsContext.setCacheSafe(true);
            view = "PRODUCT_AJAXSEARCH";
            title = "";
            String contentPage = PageManager.getPageURL(view,null);
            %>
            <jsp:forward page="<%=subURLFolder+ contentPage %>">
                <jsp:param name="title" value="<%= java.net.URLEncoder.encode(title, defaultCharsetEncoding)%>" />
            </jsp:forward>
            <%

        } else if("RETURN".equals(action)){
            checkReturn = true;
        }



    } else if("MANAGE_PRODUCT_OFFERING".equals(activity)){


        view = "PRODUCT_OFFERING_PAGE";
        title = prodOfferingPgTle;



    ///////////////////////////////////////////////////////////////////
    // ACTIVITY = VIEW_PRODUCT_SPECIFICATION
    ///////////////////////////////////////////////////////////////////
    } else if("VIEW_PRODUCT_SPECIFICATION".equals(activity)){

        lcsContext.setCacheSafe(true);

        LCSProduct productRevA = null;
        LCSSourcingConfig sourcingConfig = null;

        ////////////////////////////////////////////////////////////////////////////
        // LOCATE ALL CONTEXT OBJECTS
        ////////////////////////////////////////////////////////////////////////////
        if(oid.indexOf("LCSSourcingConfig") > -1){
            sourcingConfig = (LCSSourcingConfig) LCSQuery.findObjectById(oid);
            productRevA = SeasonProductLocator.getProductARev((LCSSourcingConfigMaster) sourcingConfig.getMaster());
            flexType = productRevA.getFlexType();
        }

        view = "PRODUCT_SPECIFICATION_PAGE";
        title = ProductLabel + specificationPgTle;
        templateType = "EMPTY";
    }
    else if ("COPY_IMAGES_FROM_SEASON".equals(activity)){
        String sourceSeasonId = request.getParameter("otherSeasons");
        LCSSeasonMaster sourceSeasonMaster = (LCSSeasonMaster)LCSSeasonQuery.findObjectById(sourceSeasonId);

        String prodId = request.getParameter("imagePageOwnerId");
        LCSProduct product = (LCSProduct)LCSSeasonQuery.findObjectById(prodId);

        LCSPartMaster phMaster = product.getPlaceholderMaster();

        LCSSeasonProductLink link = LCSSeasonQuery.findSeasonProductLink(phMaster, sourceSeasonMaster);

        LCSProduct sourceProduct = SeasonProductLocator.getProductSeasonRev(link);
        try{

            LCSProductHelper.service.copyProductImages(sourceProduct, product);
        }
        catch(Exception e){
            errorMessage = e.getLocalizedMessage();
        }

        view = "SAFE_PAGE";
        title = documentDetailsPgTle;
        action = "INIT";
        activity = Activities.VIEW_DOCUMENT;
        checkReturn = true;
        formType = "";

    }else if("PASTE_FROM_CLIPBOARD".equals(activity)){

        if(action==null){
            lcsContext.setCacheSafe(true);
            view = "VIEW_PASTE_CLIPBOARD";
            title = clipboardPgTle;
            activity = "PASTE_FROM_CLIPBOARD";
            checkReturn = true;
            formType = "";
        }

        if("PASTE".equals(action)){
        String sourceString = request.getParameter("lcsSourcingConfigIdString");
        String specString = request.getParameter("specificationIdString");
        String imagesString = request.getParameter("imagesIdString");
        String doc3DModelString = request.getParameter("doc3DModelIdString");
        String measurementsString = request.getParameter("measurementsIdString");
        String constructionString = request.getParameter("constructionIdString");
        String productSizeCategoriesString = request.getParameter("productSizeCategoriesIdString");
        String productDestinationsString = request.getParameter("productDestinationsIdString");
        String lcsSkuString = request.getParameter("lcsSkuIdString");
        String productCostSheetsString = request.getParameter("productCostSheetsIdString");
        String bomsString = request.getParameter("bomsIdString");
        String destinationProduct = request.getParameter("destinationProduct");
        String destinationSeason = request.getParameter("destinationSeason");
        String destinationSource = request.getParameter("destinationSource");
        String destinationSKU = request.getParameter("destinationSKU");
        String destinationSpec = request.getParameter("destinationSpec");

        String documentsIdString = request.getParameter("documentsIdString");
        String copyMode = request.getParameter("copyMode");
        logger.debug(documentsIdString);




       Map<String,String> clipBoardObjects = new HashMap<String,String>();
       clipBoardObjects.put("sourcingData",sourceString);
       clipBoardObjects.put("costsheetData",productCostSheetsString);
       clipBoardObjects.put("sizecategoryData",productSizeCategoriesString);
       clipBoardObjects.put("colorwayData",lcsSkuString);
       clipBoardObjects.put("destinationData",productDestinationsString);
       clipBoardObjects.put("specData",specString);
       clipBoardObjects.put("bomData",bomsString);
       clipBoardObjects.put("constructionData",constructionString);
       clipBoardObjects.put("measurementData",measurementsString);
       clipBoardObjects.put("productImagePages",imagesString);
       clipBoardObjects.put("productDoc3DModels",doc3DModelString);
       clipBoardObjects.put("destinationSource", destinationSource);
       clipBoardObjects.put("destinationSKU", destinationSKU);
       clipBoardObjects.put("destinationSpec", destinationSpec);
       clipBoardObjects.put("documentsData", documentsIdString);

       if(FormatHelper.hasContent(copyMode)) {
        clipBoardObjects.put("copyMode", "COPY." + copyMode);
       }
        if(logger.isDebugEnabled()){
        logger.debug("\n\n\n\n\ndestinationProduct :  " + destinationProduct);
        logger.debug(clipBoardObjects.toString());
        }

        logger.debug(sourceString);
        logger.debug(specString);
        logger.debug(imagesString);
        logger.debug(doc3DModelString);
        logger.debug(measurementsString);
        logger.debug(constructionString);
        logger.debug(productSizeCategoriesString);
        logger.debug(productDestinationsString);
        logger.debug(lcsSkuString);
        logger.debug(productCostSheetsString);
        logger.debug(bomsString);



        try {
            com.lcs.wc.document.LCSDocumentLogic.checkProductHasQuickSpec(imagesString, destinationProduct);
            LCSProductClientModel pcm = new LCSProductClientModel();
            pcm.pasteFromClipboard(clipBoardObjects, destinationSeason, destinationProduct);
        } catch(WTException e){
            errorMessage = e.getLocalizedMessage();
            view = PageManager.VIEW_SP_PAGE;
            title = ProductLabel + detailsPgTle;
            activity = Activities.VIEW_PRODUCT;
            checkReturn = true;
        }

        lcsContext.setCacheSafe(true);
        view = "VIEW_SP_PAGE";
        contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
        request.setAttribute("layoutType", "DIV_LAYOUT");
        activity = "VIEW_PRODUCT";

        }
    }
    else if("CREATE_PRODUCT_LINK".equals(activity)){

        if("SAVE".equals(action)){
            try{
                String parentId = request.getParameter("parentLinkedProductId");
                String childId = request.getParameter("childLinkedProductId");
                LCSProduct parentProd = (LCSProduct)LCSProductQuery.findObjectById(parentId);
                LCSProduct childProd = (LCSProduct)LCSProductQuery.findObjectById(childId);
                if(!ACLHelper.hasModifyAccess(parentProd) ||!ACLHelper.hasModifyAccess(childProd) ){
                    Object[] objB = {parentProd.getValue("productName"), childProd.getValue("productName") };
                    throw new LCSException(RB.EXCEPTION, "cantCreateLinkDontHaveEditAccess_MSG", objB);
                }
                LCSPartMaster parent = (LCSPartMaster)(parentProd).getMaster();
                LCSPartMaster child = (LCSPartMaster)(childProd).getMaster();

                String linkType = request.getParameter("linkType");
                if(!FormatHelper.hasContent(linkType)){
                    linkType = "Default";
                }

                LCSProductHelper.service.createProductToProductLink(parent, child, linkType);

                oid = request.getParameter("oid");

                view = PageManager.SAFE_PAGE;
                title = ProductLabel + detailsPgTle;
                action = "INIT";
                activity = Activities.VIEW_PRODUCT;
                checkReturn = true;

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = "VIEW_SP_PAGE";
                activity = "VIEW_SEASON_PRODUCT_LINK";
                contextHeaderPage = ProductPageNames.PRODUCT_PAGE_HEADER;
                request.setAttribute("layoutType", "DIV_LAYOUT");
                title = viewPgTle + ProductLabel + "/" + seasonLabel + informationPgTle;
                appContext.setProductContext(oid);
            }
        }

    }    else if("REMOVE_PRODUCT_LINK".equals(activity)){
            oid = request.getParameter("oid");
            try{
                boolean deLinkComponents = false;
                boolean copyComponentsOnDeLink = false;
                boolean deLinkSpecifications = false;
                if(FormatHelper.parseBoolean(request.getParameter("deLinkComponents"))){
                    deLinkComponents = true;
                }
                if(FormatHelper.parseBoolean(request.getParameter("deLinkAndCopyComponents"))){
                    deLinkComponents = true;
                    copyComponentsOnDeLink = true;
                }
                if(FormatHelper.parseBoolean(request.getParameter("deLinkSpecifications"))){
                    deLinkSpecifications = true;
                }
                ProductToProductLink link = (ProductToProductLink) LCSProductQuery.findObjectById(oid);

                LCSProduct parentProd = (LCSProduct) VersionHelper.latestIterationOf(link.getParentProduct(), true);
                LCSProduct childProd = (LCSProduct) VersionHelper.latestIterationOf(link.getChildProduct(), true);

                if (!ACLHelper.hasModifyAccess(parentProd) || !ACLHelper.hasModifyAccess(childProd)) {
                    Object[] objB = { parentProd.getValue("productName"), childProd.getValue("productName") };
                    throw new LCSException(RB.EXCEPTION, "cantDeleteLinkDontHaveEditAccess_MSG", objB);
                }

                LCSProductHelper.service.deleteProductToProductLink(link, deLinkComponents, copyComponentsOnDeLink, deLinkSpecifications);

                oid = request.getParameter("returnOid");

                view = PageManager.SAFE_PAGE;
                title = ProductLabel + detailsPgTle;
                action = "INIT";
                activity = Activities.VIEW_PRODUCT;
                checkReturn = true;

            } catch(LCSException e){
                errorMessage = e.getLocalizedMessage();
                view = PageManager.VIEW_SP_PAGE;
                title = ProductLabel + detailsPgTle;
                activity = Activities.VIEW_PRODUCT;
                checkReturn = true;

            }

    } else if("VIEW_PRODUCT_NAVIGATOR".equals(activity)){
        view = "PRODUCT_NAVIGATOR";
        templateType = "NONE";

    } else if("VIEW_SIDE_BAR_PRODUCT_NAVIGATOR".equals(activity)){
        view = "SIDE_BAR_PRODUCT_NAVIGATOR";
        templateType = "NONE";

    } else if("VIEW_PRODUCT_RELATIONSHIP_INFO".equals(activity)){
        view = "PRODUCT_RELATIONSHIP_INFO";
    ///////////////////////////////////EPM DOCUMENTS/////////////////////////////////////
    } else if(Activities.MANAGE_EPM_DOCUMENTS.equals(activity)) {
        if (Activities.ADD_EPM_DOCUMENTS.equals(action)) {
            errorMessage = "";
            LCSProduct prodARev = null;
            try {
                String partOid = (String)request.getParameter("partOid");
                LCSProduct product = (LCSProduct)LCSQuery.findObjectById(partOid);

                seasonstring = "VR:com.lcs.wc.season.LCSSeason:" + new Double("" + product.getSeasonRevId()).longValue();
                flexType = product.getFlexType();
                prodARev = SeasonProductLocator.getProductARev(product);
                prodARev = (LCSProduct)VersionHelper.latestIterationOf(prodARev);

                LCSEPMDocumentHelper.service.associateEPMDocuments(prodARev, request.getParameter("selectedIds"));
                prodARev = (LCSProduct)VersionHelper.latestIterationOf(prodARev);
                appContext.setProductContext(prodARev);
            } catch(Exception e) {
                e.printStackTrace();
                errorMessage = e.getLocalizedMessage();
            }
            view = PageManager.EPMDOCUMENT_REFERENCE_TABLE;
            String contentPage = PageManager.getPageURL(view, null);
        %>
            <jsp:forward page="<%=subURLFolder + contentPage %>">
                <jsp:param name="title" value="" />
                <jsp:param name="partOid" value="<%= FormatHelper.getObjectId(prodARev)%>" />
            </jsp:forward>
        <%

        } else if (Activities.REMOVE_EPM_DOCUMENTS.equals(action)) {
            errorMessage = "";
            LCSProduct prodARev = null;
            try {
                String partOid = (String)request.getParameter("partOid");
                LCSProduct product = (LCSProduct)LCSQuery.findObjectById(partOid);

                seasonstring = "VR:com.lcs.wc.season.LCSSeason:" + new Double("" + product.getSeasonRevId()).longValue();
                flexType = product.getFlexType();
                prodARev = SeasonProductLocator.getProductARev(product);
                prodARev = (LCSProduct)VersionHelper.latestIterationOf(prodARev);

                LCSEPMDocumentHelper.service.deleteEPMAssociations(prodARev, request.getParameter("removeIds"));
                prodARev = (LCSProduct)VersionHelper.latestIterationOf(prodARev);
                appContext.setProductContext(prodARev);
            } catch(Exception e) {
//              e.printStackTrace();
                errorMessage = e.getLocalizedMessage();
            }
            view = PageManager.EPMDOCUMENT_REFERENCE_TABLE;
            String contentPage = PageManager.getPageURL(view, null);
         %>
            <jsp:forward page="<%=subURLFolder + contentPage %>">
                <jsp:param name="title" value="" />
                <jsp:param name="errorMessage" value="<%= java.net.URLEncoder.encode(errorMessage, defaultCharsetEncoding) %>" />
                <jsp:param name="partOid" value="<%= FormatHelper.getObjectId(prodARev)%>" />
            </jsp:forward>
         <%
        }
    ///////////////////////////////////PARTS/////////////////////////////////////
    } else if (Activities.MANAGE_PARTS.equals(activity)) {

        // After selecting a part in the drop down, add it to the product.
        if (Activities.ADD_PARTS.equals(action)) {
            errorMessage = "";
            LCSProduct prodARev = null;
            try {
                String partOid = (String)request.getParameter("partOid");
                LCSProduct product = (LCSProduct)LCSQuery.findObjectById(partOid);
                seasonstring = "VR:com.lcs.wc.season.LCSSeason:" + new Double("" + product.getSeasonRevId()).longValue();
                flexType = product.getFlexType();
                prodARev = SeasonProductLocator.getProductARev(product);
                prodARev = (LCSProduct)VersionHelper.latestIterationOf(prodARev);

                LCSPartToProductLinkHelper.service.associatePart(prodARev, request.getParameter("selectedIds"));
                prodARev = (LCSProduct)VersionHelper.latestIterationOf(prodARev);
                appContext.setProductContext(prodARev);

            } catch(Exception e) {
                e.printStackTrace();
                errorMessage = e.getLocalizedMessage();
            }
            view = PageManager.PARTS_REFERENCE_TABLE;
            String contentPage = PageManager.getPageURL(view, null);
        %>
            <jsp:forward page="<%=subURLFolder + contentPage %>">
                <jsp:param name="title" value="" />
                <jsp:param name="errorMessage" value="<%= java.net.URLEncoder.encode(errorMessage, defaultCharsetEncoding) %>" />
                <jsp:param name="partOid" value="<%= FormatHelper.getObjectId(prodARev)%>" />
            </jsp:forward>
        <%

        }

        // Delete the parts if this is what is selected in the UI.
        if (Activities.REMOVE_PARTS.equals(action)) {
            errorMessage = "";
            LCSProduct prodARev = null;
            try {
                String partOid = (String)request.getParameter("partOid");
                LCSProduct product = (LCSProduct)LCSQuery.findObjectById(partOid);
                seasonstring = "VR:com.lcs.wc.season.LCSSeason:" + new Double("" + product.getSeasonRevId()).longValue();
                flexType = product.getFlexType();
                prodARev = SeasonProductLocator.getProductARev(product);
                prodARev = (LCSProduct)VersionHelper.latestIterationOf(prodARev);

                LCSPartToProductLinkHelper.service.deletePartAssociations(prodARev, request.getParameter("removeIds"));
                prodARev = (LCSProduct)VersionHelper.latestIterationOf(prodARev);
                appContext.setProductContext(prodARev);
            } catch(Exception e) {
                //e.printStackTrace();
                errorMessage = e.getLocalizedMessage();
            }

            view = PageManager.PARTS_REFERENCE_TABLE;
            String contentPage = PageManager.getPageURL(view, null);
        %>
            <jsp:forward page="<%=subURLFolder + contentPage %>">
                <jsp:param name="title" value="" />
                <jsp:param name="errorMessage" value="<%= java.net.URLEncoder.encode(errorMessage, defaultCharsetEncoding) %>" />
                <jsp:param name="partOid" value="<%= FormatHelper.getObjectId(prodARev)%>" />
            </jsp:forward>
        <%

        }
    }

    String workItemName = request.getParameter("workItemName");
    if(FormatHelper.hasContent(workItemName)){
        additionalParameters = additionalParameters + "&level1=" + request.getParameter("level1");
        additionalParameters = additionalParameters + "&level2=" + request.getParameter("level2");
        additionalParameters = additionalParameters + "&level3=" + request.getParameter("level3");
        additionalParameters = additionalParameters + "&level4=" + request.getParameter("level4");
    }

    String contentPage = null;
    if(view != null){
        if(flexType != null){
            contentPage = PageManager.getPageURL(view, null, flexType.getFullName(true));
        } else {
            contentPage = PageManager.getPageURL(view, null);
        }
    } else {
        contentPage = "";
    }
    flexPlmWebRequestContext.pageLookupKey = view;
    ////////////////////////////////////////////////////////////////////////////
    // GET THE CLIENT MODELS FLEX TYPE FULL NAME
    ////////////////////////////////////////////////////////////////////////////
    String flexTypeName = null;
    if (flexType != null) {
        flexTypeName = flexType.getFullNameDisplay(true);
        type = FormatHelper.getObjectId(flexType);
     }

    ////////////////////////////////////////////////////////////////////////////
    // CHECK RETURN ACTIVITY..
    ////////////////////////////////////////////////////////////////////////////
    if(FormatHelper.hasContent(returnActivity) && checkReturn){

        view = PageManager.SAFE_PAGE;
        title = WTMessage.getLocalizedMessage (RB.MAIN, "productName", RB.objA ) ;
        action = returnAction;
        activity = returnActivity;
        oid = returnOid;
        returnActivity = "";
        returnAction = "";
        returnOid = "";
    }
    String rootTypeId = FormatHelper.format(request.getParameter("rootTypeId"));
%>
<jsp:forward page="<%=subURLFolder+ MAINTEMPLATE %>">
    <jsp:param name="title" value="<%= java.net.URLEncoder.encode(title, defaultCharsetEncoding)%>" />
    <jsp:param name="infoMessage" value="<%= infoMessage %>" />
    <jsp:param name="errorMessage" value="<%= java.net.URLEncoder.encode(errorMessage, defaultCharsetEncoding) %>" />
    <jsp:param name="requestedPage" value="<%= contentPage %>" />
    <jsp:param name="contentPage" value="<%= contentPage %>" />
    <jsp:param name="oid" value="<%= oid %>" />
    <jsp:param name="action" value="<%= action %>" />
    <jsp:param name="activity" value="<%= activity %>" />
    <jsp:param name="objectType" value="Product" />
    <jsp:param name="typeClass" value="com.lcs.wc.product.LCSProduct" />
    <jsp:param name="type" value="<%= type %>" />
    <jsp:param name="rootTypeId" value="<%= rootTypeId %>" />
    <jsp:param name="formType" value="<%= formType %>" />
    <jsp:param name="flexTypeName" value="<%= FormatHelper.encodeAndFormatForHTMLContent(flexTypeName) %>" />
    <jsp:param name="templateType" value="<%= templateType %>" />
    <jsp:param name="workItemName" value="<%= workItemName %>" />
    <jsp:param name="contextHeaderPage" value="<%= contextHeaderPage %>" />
    <jsp:param name="componentsList" value="<%= componentlist %>" />
    <jsp:param name="toSeason" value="<%= seasonstring %>" />
    <jsp:param name="productType" value="<%= producttype %>" />
    <jsp:param name="copyMode" value="<%= copymode %>" />
    <jsp:param name="additionalParameters" value="<%= additionalParameters %>" />
</jsp:forward>

