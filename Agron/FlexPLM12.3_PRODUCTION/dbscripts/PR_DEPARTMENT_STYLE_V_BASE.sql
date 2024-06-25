
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "C##WCPROD"."PR_DEPARTMENT_STYLE_V_BASE" ("LOCALID", "HOSTREF", "DEPARTMENTSTYLE", "WEBSITE", "DEPARTMENT", "STYLE", "SORTORDER") AS 
  SELECT DISTINCT 1033 localID,
  DECODE(LP.flextypeidpath , 
 '\43162\50118951\50127279\50128713' , concat (concat ('Socks' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
 '\43162\50118951\50120693' , concat ('Backpack' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
-- '\43162\50118951\50122187' , concat(concat (concat ('Bag' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)), 
-- '\43162\50118951\50122187' ,  DECODE(LP.ptc_str_16typeInfoLCSProduct,'agrBackPack', concat ('Backpack' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),concat( concat (concat ('Bag' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4))),
   '\43162\50118951\50122187', CASE  when SN.FLEXTYPEIDPATH='\43182\50140904\50413105'  and LP.ptc_str_16typeInfoLCSProduct ='agrBackPack' THEN  concat ('Backpack' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))) ELSE concat( concat (concat ('Bag' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)) END,
 '\43162\50118951\50123637\50124975' , concat (concat ('Hat' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
 '\43162\50118951\50123637\50126196' , concat (concat ('Knit' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
 '\43162\50118951\50131394', concat(concat (concat ('SpAcc' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)), 
 '\43162\50118951\50127279\50130022' , concat(concat ('SockTeam' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)), 
 '\43162\50118951\50132845' , concat (concat ('Underwear' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))))
  || LP.ptc_lng_4typeInfoLCSProduct HostRef,
   DECODE(LP.flextypeidpath , 
 '\43162\50118951\50127279\50128713' , concat (concat ('Socks' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
 '\43162\50118951\50120693' , concat ('Backpack' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
 --'\43162\50118951\50122187' , concat(concat (concat ('Bag' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)), 
-- '\43162\50118951\50122187' ,  DECODE(LP.ptc_str_16typeInfoLCSProduct,'agrBackPack', concat ('Backpack' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),concat( concat (concat ('Bag' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4))),
 '\43162\50118951\50122187', CASE  when SN.FLEXTYPEIDPATH='\43182\50140904\50413105'  and LP.ptc_str_16typeInfoLCSProduct ='agrBackPack' THEN  concat ('Backpack' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))) ELSE concat( concat (concat ('Bag' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)) END,
 '\43162\50118951\50123637\50124975' , concat (concat ('Hat' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
 '\43162\50118951\50123637\50126196' , concat (concat ('Knit' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
 '\43162\50118951\50131394' , concat(concat (concat ('SpAcc' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)), 
 '\43162\50118951\50127279\50130022' , concat(concat ('SockTeam' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)), 
 '\43162\50118951\50132845', concat (concat ('Underwear' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))))
 || LP.ptc_lng_4typeInfoLCSProduct DepartmentStyle,
 'AGRONB2B' WebSite,
   DECODE(LP.flextypeidpath , 
 '\43162\50118951\50127279\50128713' , concat (concat ('Socks' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
 '\43162\50118951\50120693' , concat ('Backpack' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
 --'\43162\50118951\50122187' , concat(concat (concat ('Bag' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)), 
 --'\43162\50118951\50122187' ,  DECODE(LP.ptc_str_16typeInfoLCSProduct,'agrBackPack', concat ('Backpack' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),concat( concat (concat ('Bag' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4))),
 '\43162\50118951\50122187', CASE  when SN.FLEXTYPEIDPATH='\43182\50140904\50413105'  and LP.ptc_str_16typeInfoLCSProduct ='agrBackPack' THEN  concat ('Backpack' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))) ELSE concat( concat (concat ('Bag' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))),SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)) END,
 '\43162\50118951\50123637\50124975' , concat (concat ('Hat' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
 '\43162\50118951\50123637\50126196' , concat (concat ('Knit' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), 
 '\43162\50118951\50131394', concat(concat (concat ('SpAcc' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)), 
 '\43162\50118951\50127279\50130022' , concat(concat ('SockTeam' ,DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))), SUBSTR(LP.ptc_str_18typeInfoLCSProduct,4)), 
 '\43162\50118951\50132845' , concat (concat ('Underwear' ,DECODE(LP.ptc_str_7typeInfoLCSProduct, 'agrWomens', 'Womens', 'agrMens' , 'Mens', 'agrGirls' , 'Girls', 'agrUnisex' , 'Unisex', 'agrUnisexUS' , 'Unisex', 'agrYouth' , 'Youth' )), DECODE(LP.ptc_str_4typeInfoLCSProduct, 'agrDuffels' ,'AthleticDuffels', 'agrSackpacks', 'AthleticSackpacks', 'agrSeasonal','OffCourt', 'agrWomens','WomensLifestyle', SUBSTR(LP.ptc_str_4typeInfoLCSProduct,4))))
  Department,
  LP.ptc_lng_4typeInfoLCSProduct Style,
  '-'
  ||LSP.ptc_dbl_5typeInfoLCSProductS SortOrder
  FROM LCSPRODUCTSEASONLINK LSP,
    LCSPRODUCT LP ,
    LCSSEASON SN,
     LCSSKU SKU,
    LCSSKUSEASONLINK SSL
  WHERE LP.BRANCHIDITERATIONINFO = LSP.PRODUCTAREVID
  AND LSP.SEASONREVID            = SN.BRANCHIDITERATIONINFO
  AND SN.VERSIONIDA2VERSIONINFO  = 'A'
  AND SN.LATESTITERATIONINFO     = '1'
  AND LSP.seasonlinktype         = 'PRODUCT'
  AND LSP.seasonremoved          = 0
  AND LP.VERSIONIDA2VERSIONINFO  = 'A'
  AND LP.flextypeidpath          IS NOT NULL
	AND (LP.ptc_str_15typeInfoLCSProduct  is not null OR
  LP.ptc_str_4typeInfoLCSProduct  is not null)  
  AND LP.flextypeidpath          <> '\43162\50134427'
  AND LP.LATESTITERATIONINFO     = '1'
  AND LSP.EFFECTLATEST           = '1'
  AND LP.ptc_lng_4typeInfoLCSProduct                  <> '0'
  AND LP.ptc_lng_4typeInfoLCSProduct  > 0
  AND SKU.PRODUCTAREVID = LP.BRANCHIDITERATIONINFO 
  AND SSL.SKUAREVID = SKU.BRANCHIDITERATIONINFO
  AND SSL.SEASONREVID = SN.BRANCHIDITERATIONINFO
  AND SSL.EFFECTLATEST           = '1'
  AND SSL.EFFECTOUTDATE is null
 AND (SSL.ptc_str_3typeInfoLCSSKUSeaso is null OR ((LP.ptc_str_45typeInfoLCSProduct like '%agrBOS%' OR LP.ptc_str_45typeInfoLCSProduct like '%agrCore%') AND (SSL.ptc_str_3typeInfoLCSSKUSeaso like '%Inline%' OR SSL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerSpeciality%' OR SSL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerTeam%' OR SSL.ptc_str_3typeInfoLCSSKUSeaso like '%agrTeam%') AND SSL.ptc_str_3typeInfoLCSSKUSeaso not like '%agrOriginals%'))
AND  SN.PTC_STR_1TYPEINFOLCSSEASON not in (select distinct PTC_STR_1TYPEINFOLCSSEASON from lcsseason where FLEXTYPEIDPATH ='\43182\50140904' AND PTC_STR_3TYPEINFOLCSSEASON ='agr2021' and PTC_STR_4TYPEINFOLCSSEASON ='agrFallWinter')
AND (LSP.ptc_tms_2typeInfoLCSProductS,LP.ptc_lng_4typeInfoLCSProduct)      IN
    (SELECT MAX(LSP.ptc_tms_2typeInfoLCSProductS),
      LP.ptc_lng_4typeInfoLCSProduct
       FROM LCSPRODUCTSEASONLINK LSP,
      LCSPRODUCT LP ,
      LCSSEASON SN,
       LCSSKU SKU,
     LCSSKUSEASONLINK SSL
    WHERE LP.BRANCHIDITERATIONINFO = LSP.PRODUCTAREVID
    AND LSP.SEASONREVID            = SN.BRANCHIDITERATIONINFO
    AND SN.VERSIONIDA2VERSIONINFO  = 'A'
    AND SN.LATESTITERATIONINFO     = '1'
    AND LSP.seasonlinktype         = 'PRODUCT'
    AND LSP.seasonremoved          = 0
    AND LP.VERSIONIDA2VERSIONINFO  = 'A'
    AND LP.flextypeidpath         IS NOT NULL
    --AND LP.ptc_str_15typeInfoLCSProduct                  IS NOT NULL
	  AND ( LP.ptc_str_15typeInfoLCSProduct  is not null OR
     LP.ptc_str_4typeInfoLCSProduct  is not null)  
    AND LP.flextypeidpath         <> '\43162\50134427'
    AND LP.LATESTITERATIONINFO     = '1'
    AND LSP.EFFECTLATEST           = '1'
    AND lsp.effectoutdate       IS NULL
    AND LP.ptc_lng_4typeInfoLCSProduct                  <> '0'
    --AND LP.att19                  IS NOT NULL
    AND LSP.ptc_tms_2typeInfoLCSProductS                 <=sysdate
    AND LP.ptc_lng_4typeInfoLCSProduct  > 0
    AND SKU.PRODUCTAREVID = LP.BRANCHIDITERATIONINFO 
    AND SSL.SKUAREVID = SKU.BRANCHIDITERATIONINFO
    AND SSL.SEASONREVID = SN.BRANCHIDITERATIONINFO
    AND (SSL.ptc_str_3typeInfoLCSSKUSeaso is null OR ((LP.ptc_str_45typeInfoLCSProduct like '%agrBOS%' OR LP.ptc_str_45typeInfoLCSProduct like '%agrCore%') AND (SSL.ptc_str_3typeInfoLCSSKUSeaso like '%Inline%' OR SSL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerSpeciality%' OR SSL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerTeam%' OR SSL.ptc_str_3typeInfoLCSSKUSeaso like '%agrTeam%') AND SSL.ptc_str_3typeInfoLCSSKUSeaso not like '%agrOriginals%'))
    GROUP BY LP.ptc_lng_4typeInfoLCSProduct
    );
