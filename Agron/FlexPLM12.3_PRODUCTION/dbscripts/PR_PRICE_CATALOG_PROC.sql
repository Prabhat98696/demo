create or replace PROCEDURE PR_PRICE_CATALOG_PROC
IS
BEGIN
EXECUTE IMMEDIATE 'TRUNCATE TABLE TABLE_CATALOG';
EXECUTE IMMEDIATE 'TRUNCATE TABLE TABLE_PRICE';
EXECUTE IMMEDIATE 'TRUNCATE TABLE TRAIL_CATALOG';
EXECUTE IMMEDIATE 'TRUNCATE TABLE TRAIL_PRICE';
EXECUTE IMMEDIATE 'TRUNCATE TABLE PRICE_CATALOG_TAB';

INSERT INTO TABLE_CATALOG  
(select DISTINCT SCL.ptc_str_3typeInfoLCSSKUSeaso  str,
                moa.ptc_str_2typeInfoLCSMOAObjec SKU,
				DECODE (SN.ptc_str_4typeInfoLCSSeason, 'agrFallWinter' , concat ('10/15/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrFall' , concat ('10/15/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSpringSummer' , concat ('05/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSpring' , concat ('05/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSummer' , concat ('05/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrWinter' , concat ('10/15/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4))) DATEEFFECTIVE, 
				0 PRICECODE,
				moa.ptc_str_2typeInfoLCSMOAObjec ARTICLENO, 
				case SN.ptc_str_4typeInfoLCSSeason 
				when 'agrFallWinter' then 'FW' 
				when 'agrSpringSummer' then 'SS' 
				when 'agrSummer' then 'SU'
				when 'agrWinter' then 'WI'
				when 'agrSpring' then 'SP'
				when 'agrFall' then 'FA'
				end SEASONTYPE,
				case SN.ptc_str_3typeInfoLCSSeason
				when 'agr2015' then '15'
				when 'agr2016' then '16'
				when 'agr2017' then '17'
				when 'agr2018' then '18'
				when 'agr2019' then '19'
				when 'agr2020' then '20'
				when 'agr2021' then '21'
				end SEASONYEAR,
				SN.ptc_str_1typeInfoLCSSeason SEASONNAME
  from LCSMOAOBJECT_DATADUMP MOA, LCSSEASON SN, LCSSKUSEASONLINK SCL, LCSSKU SK, LCSPRODUCT PR
  where  
  PR.BRANCHIDITERATIONINFO = SCL.PRODUCTAREVID and 
  sk.ida3a12 = SCL.productmasterid and
  SCL.SKUMASTERID = SK.ida3masterreference AND
  SCL.SEASONREVID = SN.BRANCHIDITERATIONINFO and
  PR.VERSIONIDA2VERSIONINFO = 'A' and
  PR.flextypeidpath is not null and
  PR.ptc_str_15typeInfoLCSProduct is not null AND
  SN.FLEXTYPEIDPATH = '\43182\50140904' AND 
  PR.LATESTITERATIONINFO = '1' and
  MOA.ida3a5 = SK.ida3masterreference and
SCL.seasonremoved =0 and
moa.ptc_bln_1typeInfoLCSMOAObjec = '1' and
SK.LATESTITERATIONINFO = '1' AND
SN.latestiterationinfo = 1 AND
SN.active = 1 AND
SK.ptc_bln_1typeInfoLCSSKU = '1' AND
SCL.SEASONLINKTYPE='SKU' and
SK.VERSIONIDA2VERSIONINFO = 'A' and
SCL.EFFECTLATEST = '1' and
SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agr%' and
moa.ptc_str_2typeInfoLCSMOAObjec IS NOT NULL and
SCL.ptc_str_3typeInfoLCSSKUSeaso is not null and
SCL.EFFECTOUTDATE is null
AND SK.ptc_str_4typeInfoLCSSKU not like '-%'
AND PR.ptc_lng_4typeInfoLCSProduct > 0  
AND SN.PTC_STR_1TYPEINFOLCSSEASON not in (select distinct PTC_STR_1TYPEINFOLCSSEASON from lcsseason where FLEXTYPEIDPATH ='\43182\50140904' AND PTC_STR_3TYPEINFOLCSSEASON ='agr2021' and PTC_STR_4TYPEINFOLCSSEASON ='agrFallWinter')
AND ((SCL.ptc_bln_2typeInfoLCSSKUSeaso='0' or SCL.ptc_bln_2typeInfoLCSSKUSeaso is null) and (SCL.ptc_bln_3typeInfoLCSSKUSeaso='0' or SCL.ptc_bln_3typeInfoLCSSKUSeaso is null)) and
(SCL.ptc_str_3typeInfoLCSSKUSeaso not like '%agrSMU%' AND ((PR.ptc_str_45typeInfoLCSProduct like '%agrBOS%' OR PR.ptc_str_45typeInfoLCSProduct like '%agrCore%') AND (SCL.ptc_str_3typeInfoLCSSKUSeaso like '%Inline%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerSpeciality%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerTeam%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrTeam%') AND SCL.ptc_str_3typeInfoLCSSKUSeaso not like '%agrOriginals%'))

UNION 

select DISTINCT SCL.ptc_str_3typeInfoLCSSKUSeaso  str,
                SKSIZE.ptc_str_1typeInfoSKUSize SKU,
				DECODE (SN.ptc_str_4typeInfoLCSSeason, 'agrFallWinter' , concat ('10/15/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrFall' , concat ('10/15/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSpringSummer' , concat ('05/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSpring' , concat ('05/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSummer' , concat ('05/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrWinter' , concat ('10/15/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4))) DATEEFFECTIVE, 
				0 PRICECODE,
				SKSIZE.ptc_str_1typeInfoSKUSize ARTICLENO, 
				case SN.ptc_str_4typeInfoLCSSeason 
				when 'agrFallWinter' then 'FW' 
				when 'agrSpringSummer' then 'SS' 
				when 'agrSummer' then 'SU'
				when 'agrWinter' then 'WI'
				when 'agrSpring' then 'SP'
				when 'agrFall' then 'FA'
				end SEASONTYPE,
				case SN.ptc_str_3typeInfoLCSSeason
				when 'agr2015' then '15'
				when 'agr2016' then '16'
				when 'agr2017' then '17'
				when 'agr2018' then '18'
				when 'agr2019' then '19'
				when 'agr2020' then '20'
				when 'agr2021' then '21'
				when 'agr2022' then '22'
				when 'agr2023' then '23'
				when 'agr2024' then '24'
				when 'agr2025' then '25'
				when 'agr2026' then '26'
				end SEASONYEAR,
				SN.ptc_str_1typeInfoLCSSeason SEASONNAME
  from LCSSEASON SN, skuseasonlink SCL, LCSSKU SK, LCSPRODUCT PR, SKUSIZEMASTER SKMSTR, SKUSIZE SKSIZE, SKUSIZETOSEASON SKUSIZETOSEASON, SKUSIZETOSEASONMASTER
  where  
  PR.BRANCHIDITERATIONINFO = SCL.PRODUCTAREVID and 
  sk.ida3a12 = SCL.productmasterid and
  SCL.ida3a5 = SKMSTR.ida3a6 AND
  SCL.SEASONREVID = SN.BRANCHIDITERATIONINFO and
  SCL.SKUMASTERID = SK.ida3masterreference AND
  SKMSTR.IDA3A6 = SK.IDA3MASTERREFERENCE AND
  SKMSTR.IDA2A2 = SKSIZE.IDA3MASTERREFERENCE AND
  SKUSIZETOSEASON.IDA3MASTERREFERENCE = SKUSIZETOSEASONMASTER.IDA2A2 AND
  SKUSIZETOSEASONMASTER.IDA3B6 = SKMSTR.IDA2A2 AND
  SN.ida3masterreference=SKUSIZETOSEASONMASTER.IDA3A6 AND
  SN.FLEXTYPEIDPATH = '\43182\50140904\50413105' AND 
  PR.VERSIONIDA2VERSIONINFO = 'A' and
  PR.flextypeidpath is not null and
  ( PR.ptc_str_15typeInfoLCSProduct  is not null OR
  PR.ptc_str_4typeInfoLCSProduct  is not null) and 
  PR.LATESTITERATIONINFO = 1 and
SCL.seasonremoved =0 and
SK.LATESTITERATIONINFO = 1 AND
SN.latestiterationinfo = 1 AND
SN.active = 1 AND
SKSIZE.active = 1 AND
SKSIZE.latestiterationinfo = 1 AND
SK.ptc_bln_1typeInfoLCSSKU = 1 AND
SCL.SEASONLINKTYPE='SKU' and
SK.VERSIONIDA2VERSIONINFO = 'A' and
SCL.EFFECTLATEST = 1 and
SKUSIZETOSEASON.active = 1 AND
SKUSIZETOSEASON.latestiterationinfo = 1 AND
SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agr%' and
SKSIZE.ptc_str_1typeInfoSKUSize is not null and
SCL.ptc_str_3typeInfoLCSSKUSeaso is not null and
SCL.EFFECTOUTDATE is null
AND SK.ptc_str_4typeInfoLCSSKU not like '-%'
AND PR.ptc_lng_4typeInfoLCSProduct > 0  
and ((SCL.ptc_bln_2typeInfoLCSSKUSeaso=0 or SCL.ptc_bln_2typeInfoLCSSKUSeaso is null) and (SCL.ptc_bln_3typeInfoLCSSKUSeaso=0 or SCL.ptc_bln_3typeInfoLCSSKUSeaso is null)) and
(SCL.ptc_str_3typeInfoLCSSKUSeaso not like '%agrSMU%' AND ((PR.ptc_str_45typeInfoLCSProduct like '%agrBOS%' OR PR.ptc_str_45typeInfoLCSProduct like '%agrCore%') AND  (SCL.ptc_str_3typeInfoLCSSKUSeaso like '%Inline%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerSpeciality%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerTeam%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrTeam%') AND SCL.ptc_str_3typeInfoLCSSKUSeaso not like '%agrOriginals%')));



INSERT INTO TABLE_PRICE 
(select DISTINCT SPL.PTC_STR_3TYPEINFOLCSPRODUCTS  str,
                moa.ptc_str_2typeInfoLCSMOAObjec SKU,
				case SN.ptc_str_4typeInfoLCSSeason 
				when 'agrFallWinter' then 'FW' 
				when 'agrSpringSummer' then 'SS' 
				when 'agrSummer' then 'SU'
				when 'agrWinter' then 'WI'
				when 'agrSpring' then 'SP'
				when 'agrFall' then 'FA'
				end SEASONTYPE,
				case SN.ptc_str_3typeInfoLCSSeason
				when 'agr2015' then '15'
				when 'agr2016' then '16'
				when 'agr2017' then '17'
				when 'agr2018' then '18'
				when 'agr2019' then '19'
				when 'agr2020' then '20'
				when 'agr2021' then '21'
				end SEASONYEAR,
				SN.ptc_str_1typeInfoLCSSeason SEASONNAME,
				SPL.ptc_dbl_5typeInfoLCSProductS PriceMSRP,
                SPL.ptc_dbl_6typeInfoLCSProductS PriceWholesale, 
                SPL.ptc_dbl_6typeInfoLCSProductS PriceNet
from LCSMOAOBJECT_DATADUMP MOA, LCSSEASON SN, LCSPRODUCTSEASONLINK SPL, LCSSKU SK, LCSPRODUCT PR
  where  
  PR.BRANCHIDITERATIONINFO = SPL.PRODUCTAREVID and 
  sk.ida3a12 = SPL.productmasterid and
  SPL.SEASONREVID = SN.BRANCHIDITERATIONINFO and
  PR.VERSIONIDA2VERSIONINFO = 'A' and
  PR.flextypeidpath is not null and
  PR.ptc_str_15typeInfoLCSProduct is not null AND
  SN.FLEXTYPEIDPATH = '\43182\50140904' AND 
  PR.LATESTITERATIONINFO = '1' and
  MOA.ida3a5 = SK.ida3masterreference and
 --pr.att19 = '104450' and
SPL.seasonremoved =0 and
moa.ptc_bln_1typeInfoLCSMOAObjec = '1' and
SK.LATESTITERATIONINFO = '1' AND
SN.latestiterationinfo = 1 AND
SN.active = 1 AND
SK.ptc_bln_1typeInfoLCSSKU = '1' AND
SPL.SEASONLINKTYPE='PRODUCT' and
SK.VERSIONIDA2VERSIONINFO = 'A' and
SPL.EFFECTLATEST = '1' and
moa.ptc_str_2typeInfoLCSMOAObjec IS NOT NULL and
SPL.EFFECTOUTDATE is null
AND SN.PTC_STR_1TYPEINFOLCSSEASON not in (select distinct PTC_STR_1TYPEINFOLCSSEASON from lcsseason where FLEXTYPEIDPATH ='\43182\50140904' AND PTC_STR_3TYPEINFOLCSSEASON ='agr2021' and PTC_STR_4TYPEINFOLCSSEASON ='agrFallWinter')
AND SK.ptc_str_4typeInfoLCSSKU not like '-%'
AND PR.ptc_lng_4typeInfoLCSProduct > 0  

UNION 

select DISTINCT SPL.PTC_STR_3TYPEINFOLCSPRODUCTS  str,
               SKSIZE.ptc_str_1typeInfoSKUSize SKU,
				case SN.ptc_str_4typeInfoLCSSeason 
				when 'agrFallWinter' then 'FW' 
				when 'agrSpringSummer' then 'SS' 
				when 'agrSummer' then 'SU'
				when 'agrWinter' then 'WI'
				when 'agrSpring' then 'SP'
				when 'agrFall' then 'FA'
				end SEASONTYPE,
				case SN.ptc_str_3typeInfoLCSSeason
				when 'agr2015' then '15'
				when 'agr2016' then '16'
				when 'agr2017' then '17'
				when 'agr2018' then '18'
				when 'agr2019' then '19'
				when 'agr2020' then '20'
				when 'agr2021' then '21'
        		when 'agr2022' then '22'
				when 'agr2023' then '23'
        		when 'agr2024' then '24'
				when 'agr2025' then '25'
				end SEASONYEAR,
				SN.ptc_str_1typeInfoLCSSeason SEASONNAME,
				SPL.ptc_dbl_5typeInfoLCSProductS PriceMSRP,
                SPL.ptc_dbl_6typeInfoLCSProductS PriceWholesale, 
                SPL.ptc_dbl_6typeInfoLCSProductS PriceNet                
from LCSSEASON SN, LCSPRODUCTSEASONLINK SPL, LCSSKU SK, LCSPRODUCT PR,  SKUSIZEMASTER SKMSTR, SKUSIZE SKSIZE, SKUSIZETOSEASON SKUSIZETOSEASON, SKUSIZETOSEASONMASTER
  where  
  PR.BRANCHIDITERATIONINFO = SPL.PRODUCTAREVID and 
  sk.ida3a12 = SPL.productmasterid and
  SPL.SEASONREVID = SN.BRANCHIDITERATIONINFO and
  --
  SKMSTR.IDA3A6 = SK.IDA3MASTERREFERENCE AND
  SKMSTR.IDA2A2 = SKSIZE.IDA3MASTERREFERENCE AND
   SKUSIZETOSEASON.IDA3MASTERREFERENCE = SKUSIZETOSEASONMASTER.IDA2A2 AND
  SKUSIZETOSEASONMASTER.IDA3B6 = SKMSTR.IDA2A2 AND
  SN.ida3masterreference=SKUSIZETOSEASONMASTER.IDA3A6 AND
  SN.FLEXTYPEIDPATH = '\43182\50140904\50413105' AND 
  PR.VERSIONIDA2VERSIONINFO = 'A' and
  PR.flextypeidpath is not null and
 (PR.ptc_str_15typeInfoLCSProduct  is not null OR
  PR.ptc_str_4typeInfoLCSProduct  is not null) and 
 PR.LATESTITERATIONINFO = '1' and
SPL.seasonremoved =0 and
SK.LATESTITERATIONINFO = '1' AND
SN.latestiterationinfo = 1 AND
SN.active = 1 AND
SKSIZE.active = 1 AND
SKSIZE.latestiterationinfo = 1 AND
SK.ptc_bln_1typeInfoLCSSKU = '1' AND
SPL.SEASONLINKTYPE='PRODUCT' and
SK.VERSIONIDA2VERSIONINFO = 'A' and
SPL.EFFECTLATEST = '1' and
SKUSIZETOSEASON.active = 1 AND
SKUSIZETOSEASON.latestiterationinfo = 1 AND
SKSIZE.ptc_str_1typeInfoSKUSize is not null and
--SKSIZE.ptc_str_10typeinfoskusize is not null and
SPL.EFFECTOUTDATE is null
AND SK.ptc_str_4typeInfoLCSSKU not like '-%'
AND PR.ptc_lng_4typeInfoLCSProduct > 0  
);

UPDATE TABLE_CATALOG SET STR = REPLACE(TRIM(STR),'|~*~|',',');
UPDATE TABLE_CATALOG set STR = SUBSTR(STR,1,INSTR(STR,',',-1)-1);

UPDATE TABLE_PRICE SET STR = REPLACE(TRIM(STR),'|~*~|',',');
UPDATE TABLE_PRICE set STR = SUBSTR(STR,1,INSTR(STR,',',-1)-1);

INSERT INTO TRAIL_CATALOG 
(SELECT 1033 LOCALEID,
           trim(regexp_substr(TABLE_CATALOG.STR, '[^,]+', 1, lines.column_value)) HOSTREF,
		   trim(regexp_substr(TABLE_CATALOG.STR, '[^,]+', 1, lines.column_value)) PRICECATALOG,
		   'AGRONB2B' WEBSITE,
		   TABLE_CATALOG.ARTICLENO,
		   TABLE_CATALOG.SEASONTYPE,
		   TABLE_CATALOG.SEASONYEAR,
		   TABLE_CATALOG.SEASONNAME,
		   trim(regexp_substr(TABLE_CATALOG.STR, '[^,]+', 1, lines.column_value)) CATALOG,
		   TABLE_CATALOG.PRICECODE,
		   TABLE_CATALOG.SKU SKU,
		   TABLE_CATALOG.DATEEFFECTIVE
     FROM TABLE_CATALOG ,
      TABLE (CAST (MULTISET
     (SELECT LEVEL FROM dual CONNECT BY LEVEL <= regexp_count(TABLE_CATALOG.STR, ',')+1)
                  AS sys.odciNumberList
                  )
            ) lines);
			
			
INSERT INTO TRAIL_PRICE  
(SELECT 1033 LOCALEID,
           trim(regexp_substr(TABLE_PRICE.STR, '[^,]+', 1, lines.column_value)) HOSTREF,
		   trim(regexp_substr(TABLE_PRICE.STR, '[^,]+', 1, lines.column_value)) PRICECATALOG,
		   'AGRONB2B' WEBSITE,
		    trim(regexp_substr(TABLE_PRICE.STR, '[^,]+', 1, lines.column_value)) CATALOG,
		    TABLE_PRICE.SKU SKU,
			TABLE_PRICE.SEASONTYPE,
			TABLE_PRICE.SEASONYEAR,
			TABLE_PRICE.SEASONNAME,
		   TABLE_PRICE.PRICEMSRP PriceMSRP,
           TABLE_PRICE.PRICEWHOLESALE PriceWholesale, 
           TABLE_PRICE.PRICENET PriceNet
    FROM TABLE_PRICE ,
      TABLE (CAST (MULTISET
     (SELECT LEVEL FROM dual CONNECT BY LEVEL <= regexp_count(TABLE_PRICE.STR, ',')+1)
                  AS sys.odciNumberList
                  )
            ) lines);
			
INSERT INTO PRICE_CATALOG_TAB
 select c.localeid, 
C.PRICECODE||C.ARTICLENO||C.SEASONTYPE||C.SEASONYEAR||SUBSTR(c.HOSTREF,4) HOSTREF,
C.PRICECODE||C.ARTICLENO||C.SEASONTYPE||C.SEASONYEAR||SUBSTR(c.PRICECATALOG,4) PRICECATALOG,
c.website,
null salesorg, 
C.SEASONTYPE||C.SEASONYEAR||SUBSTR(C.CATALOG,4) CATALOG,
c.pricecode, 
c.sku,
c.dateeffective, 
p.pricemsrp,
p.pricewholesale,
p.pricenet 
from 
TRAIL_CATALOG c, TRAIL_PRICE p 
where c.sku = p.sku
AND C.SEASONTYPE = P.SEASONTYPE
AND C.SEASONNAME = P.SEASONNAME;
			
			
DELETE FROM  PRICE_CATALOG_TAB A 
WHERE   a.rowid >  ANY (SELECT B.rowid FROM PRICE_CATALOG_TAB B
     WHERE 
        A.SKU = B.SKU
     AND A.CATALOG = B.CATALOG);
			
COMMIT;
END;