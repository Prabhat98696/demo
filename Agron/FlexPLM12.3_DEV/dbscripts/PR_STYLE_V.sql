
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "PR_STYLE_V" ("LOCALID", "HOSTREF", "STYLE", "WEBSITE", "NAME", "DESCRIPTION", "ALPHA", "SUPPLIERSTYLE", "WEIGHT", "ATTRIBUTELABEL1", "ATTRIBUTELABEL2", "ATTRIBUTELABEL3", "ATTRIBUTELABEL4", "ATTRIBUTELABEL5", "TYPENAME") AS 
  SELECT DISTINCT 1033 localID,
    trim(ptc_lng_4typeInfoLCSProduct) hostref,
    trim(ptc_lng_4typeInfoLCSProduct) Style,
    'AGRONB2B' WebSite,
    lcsproduct.ptc_str_1typeInfoLCSProduct name,
    lcsproduct.ptc_str_13typeInfoLCSProduct Description,
    CAST (NULL AS CHAR(10)) Alpha,
    CAST (NULL AS CHAR(10)) SupplierStyle,
    CAST (NULL AS CHAR(10)) weight,
    'Size' AttributeLabel1,
    'Color' AttributeLabel2,
    'Article No.' AttributeLabel3,
    CAST (NULL AS CHAR(10)) AttributeLabel4,
    CAST (NULL AS CHAR(10)) AttributeLabel5,
    CASE flextype.BRANCHIDITERATIONINFO
	  WHEN 50130022
      THEN 'SockTeam'
      WHEN 50131394
      THEN 'SpAcc'
      WHEN 50122187
      THEN 'Bag'
      WHEN 50120693
      THEN 'Backpack'
      WHEN 50126196
      THEN 'Knits'
      WHEN 50124975
      THEN 'Hats'
      WHEN 50128713
      THEN 'Athletic'
      WHEN 50132845
      THEN 'Underwear'
    END AS typename
  FROM lcsproduct
  JOIN WTTypeDefinition flextype
   --lcsproduct.FLEXTYPEREFIDXXX = flextype.ida2a2 
  ON lcsproduct.BRANCHIDA2TYPEDEFINITIONREFE = flextype.BRANCHIDITERATIONINFO
  JOIN pr_sku_v
  ON pr_sku_v.ida3a12                  = lcsproduct.ida3masterreference
  WHERE lcsproduct.latestiterationinfo = 1
  AND lcsproduct.versionida2versioninfo='A'
 --AND lcsproduct.ptc_lng_4typeInfoLCSProduct                IS NOT NULL -- Product : Old Work#
  AND lcsproduct.ptc_lng_4typeInfoLCSProduct                 <> 0
 -- AND lcsproduct.FLEXTYPEREFIDXXX			   !=79510 -- Product\SMU & Clearance type
   AND lcsproduct.flextypeidpath			  <> '\43162\50134427'
  ORDER BY hostref;

