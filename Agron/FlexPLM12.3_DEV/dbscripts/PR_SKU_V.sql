
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "PR_SKU_V" ("LOCALEID", "HOSTREF", "SKU", "WEBSITE", "UPC", "STYLE", "SKUALPHA", "ATTRIBUTEVALUE1", "ATTRIBUTEVALUE2", "ATTRIBUTEVALUE3", "ATTRIBUTEVALUE4", "ATTRIBUEVALUE5", "IMAGEFILENAMESMALL", "IMAGEFILENAMEMEDIUM", "IMAGEFILENAMELARGE", "ORDERMINIMUM", "ORDERMAXIMUM", "ORDERMULTIPLE", "UNITOFMEASURE", "SORTORDER", "IDA3A12", "SKUMASTERID") AS 
  SELECT DISTINCT 1033 localeid,
	--'<2022' SeasonFlag,
    trim(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec) hostref,
    trim(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec) SKU,
    'AGRONB2B' website,
    lcsmoaobject.ptc_str_5typeInfoLCSMOAObjec UPC,
    lcsproduct.ptc_lng_4typeInfoLCSProduct style,
    DECODE(trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,1)),'5', trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,7)), trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,6))) SkuAlpha,
    lcsmoaobject.ptc_str_4typeInfoLCSMOAObjec attributevalue1,
    REPLACE(lcssku.ptc_str_7typeInfoLCSSKU, '|', '') attributevalue2,
    DECODE(trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,1)),'5', trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,7)), trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,6))) attributevalue3,
    CAST (NULL AS CHAR(10)) attributevalue4,
    CAST (NULL AS CHAR(10)) attribuevalue5,
    DECODE(trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,1)),'5', trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,7)), trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,6)))
    || '.png' ImageFileNameSmall,
    DECODE(trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,1)),'5', trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,7)), trim(SUBSTR(lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec,0,6)))
    || '-2.png' ImageFileNameMedium,
    CAST (NULL AS CHAR(10)) ImageFileNameLarge,
    CAST (NULL AS CHAR(10)) OrderMinimum,
    CAST (NULL AS CHAR(10)) OrderMaximum,
    CAST (NULL AS CHAR(10)) OrderMultiple,
    'ea' UnitOfMeasure,
    CASE lcsmoaobject.ptc_str_4typeInfoLCSMOAObjec
      WHEN '0/1'
      THEN 10
      WHEN '10/11'
      THEN 35
      WHEN '12/13'
      THEN 40
      WHEN '14/15'
      THEN 45
      WHEN '2/3'
      THEN 15
      WHEN '4/5'
      THEN 20
      WHEN '6/7'
      THEN 25
      WHEN '8/9'
      THEN 30
      WHEN '1'
      THEN 10
      WHEN '11'
      THEN 60
      WHEN '13'
      THEN 70
      WHEN '3'
      THEN 20
      WHEN '5'
      THEN 30
      WHEN '9'
      THEN 50
      WHEN '7'
      THEN 40
      WHEN '7 1/2'
      THEN 60
      WHEN '7 1/4'
      THEN 50
      WHEN '7 1/8'
      THEN 45
      WHEN '7 3/4'
      THEN 70
      WHEN '7 3/8'
      THEN 55
      WHEN '7 5/8'
      THEN 65
      WHEN '11-13'
      THEN 40
      WHEN '2.5-4'
      THEN 10
      WHEN '5-6'
      THEN 20
      WHEN '7-8.5'
      THEN 30
      WHEN '9-10'
      THEN 35
      WHEN '9-10.5'
      THEN 37
      WHEN '28'
      THEN 10
      WHEN '30'
      THEN 20
      WHEN '32'
      THEN 30
      WHEN '34'
      THEN 40
      WHEN '36'
      THEN 50
      WHEN '38'
      THEN 60
      WHEN '2XL'
      THEN 80
      WHEN '2XS'
      THEN 20
      WHEN 'L'
      THEN 60
      WHEN 'M'
      THEN 50
      WHEN 'S'
      THEN 40
      WHEN 'XL'
      THEN 70
      WHEN 'XS'
      THEN 30
      WHEN 'XXL'
      THEN 80
      WHEN 'L/XL'
      THEN 60
      WHEN 'M/L'
      THEN 40
      WHEN 'S/M'
      THEN 20
      WHEN 'OSFA'
      THEN 0
      ELSE 0
    END sortorder,
    lcssku.ida3a12,
    lcssku.ida3masterreference skumasterid
  FROM lcssku
  JOIN lcsmoaobject
  ON lcsmoaobject.ida3a5 = lcssku.ida3masterreference
  JOIN lcsproduct
  ON lcssku.ida3a12                  = lcsproduct.ida3masterreference
  WHERE lcsmoaobject.effectoutdate  IS NULL
  AND LCSPRODUCT.LATESTITERATIONINFO = '1'
  AND lcsproduct.ptc_lng_4typeInfoLCSProduct              <> '0' -- Product : Work#
  AND LCSSKU.LATESTITERATIONINFO     = '1'
  AND lcssku.ptc_bln_1typeInfoLCSSKU                    = '1' -- Colorway : Assigned field
  AND LCSSKU.VERSIONIDA2VERSIONINFO  = 'A'
  AND lcsmoaobject.ptc_str_2typeInfoLCSMOAObjec             IS NOT NULL -- UPC Article Number MOA table : Article #

union 

select
DISTINCT 1033 localeid,
	--'>2021' SeasonFlag,
    trim(skusize.ptc_str_1typeInfoSKUSize) hostref,
    trim(skusize.ptc_str_1typeInfoSKUSize) SKU,
    'AGRONB2B' website,
    skusize.ptc_str_10typeInfoSKUSize UPC,
    lcsproduct.ptc_lng_4typeInfoLCSProduct style,
    DECODE(trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,1)),'5', trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,7)), trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,6))) SkuAlpha,
    skusizemaster.sizevalue attributevalue1,
    REPLACE(lcssku.ptc_str_7typeInfoLCSSKU, '|', '') attributevalue2,
    DECODE(trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,1)),'5', trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,7)), trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,6))) attributevalue3,
    CAST (NULL AS CHAR(10)) attributevalue4,
    CAST (NULL AS CHAR(10)) attribuevalue5,
    DECODE(trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,1)),'5', trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,7)), trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,6)))
    || '.png' ImageFileNameSmall,
    DECODE(trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,1)),'5', trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,7)), trim(SUBSTR(skusize.ptc_str_1typeInfoSKUSize,0,6)))
    || '-2.png' ImageFileNameMedium,
    CAST (NULL AS CHAR(10)) ImageFileNameLarge,
    CAST (NULL AS CHAR(10)) OrderMinimum,
    CAST (NULL AS CHAR(10)) OrderMaximum,
    CAST (NULL AS CHAR(10)) OrderMultiple,
    'ea' UnitOfMeasure,
    CASE skusizemaster.sizevalue
      WHEN '0/1'
      THEN 10
      WHEN '10/11'
      THEN 35
      WHEN '12/13'
      THEN 40
      WHEN '14/15'
      THEN 45
      WHEN '2/3'
      THEN 15
      WHEN '4/5'
      THEN 20
      WHEN '6/7'
      THEN 25
      WHEN '8/9'
      THEN 30
      WHEN '1'
      THEN 10
      WHEN '11'
      THEN 60
      WHEN '13'
      THEN 70
      WHEN '3'
      THEN 20
      WHEN '5'
      THEN 30
      WHEN '9'
      THEN 50
      WHEN '7'
      THEN 40
      WHEN '7 1/2'
      THEN 60
      WHEN '7 1/4'
      THEN 50
      WHEN '7 1/8'
      THEN 45
      WHEN '7 3/4'
      THEN 70
      WHEN '7 3/8'
      THEN 55
      WHEN '7 5/8'
      THEN 65
      WHEN '11-13'
      THEN 40
      WHEN '2.5-4'
      THEN 10
      WHEN '5-6'
      THEN 20
      WHEN '7-8.5'
      THEN 30
      WHEN '9-10'
      THEN 35
      WHEN '9-10.5'
      THEN 37
      WHEN '28'
      THEN 10
      WHEN '30'
      THEN 20
      WHEN '32'
      THEN 30
      WHEN '34'
      THEN 40
      WHEN '36'
      THEN 50
      WHEN '38'
      THEN 60
      WHEN '2XL'
      THEN 80
      WHEN '2XS'
      THEN 20
      WHEN 'L'
      THEN 60
      WHEN 'M'
      THEN 50
      WHEN 'S'
      THEN 40
      WHEN 'XL'
      THEN 70
      WHEN 'XS'
      THEN 30
      WHEN 'XXL'
      THEN 80
      WHEN 'L/XL'
      THEN 60
      WHEN 'M/L'
      THEN 40
      WHEN 'S/M'
      THEN 20
      WHEN 'OSFA'
      THEN 0
      ELSE 0
    END sortorder,
    lcssku.ida3a12,
    lcssku.ida3masterreference skumasterid
from lcssku
join lcspartmaster on
lcssku.IDA3MASTERREFERENCE=lcspartmaster.ida2a2
join lcsproduct on
lcssku.ida3a12 = lcsproduct.ida3masterreference
join skusizemaster on
skusizemaster.ida3a6=lcspartmaster.ida2a2
join skusize on
skusize.ida3masterreference=skusizemaster.ida2a2
  AND LCSPRODUCT.LATESTITERATIONINFO = '1'
  AND lcsproduct.ptc_lng_4typeInfoLCSProduct              <> '0' -- Product : Work#
--AND lcsproduct.ptc_str_19typeInfoLCSProduct              IS NOT NULL -- Product : Old Work#
  AND LCSSKU.LATESTITERATIONINFO     = '1'
  AND lcssku.ptc_bln_1typeInfoLCSSKU                    = '1' -- Colorway : Assigned field
 -- and lcssku.seasonrevid >0
--  AND LCSSKU.VERSIONIDA2VERSIONINFO  = 'A'
  AND skusize.ptc_str_1typeInfoSKUSize             IS NOT NULL
 AND skusize.active = 1 
 AND skusize.LATESTITERATIONINFO     = 1
ORDER BY HOSTREF;

