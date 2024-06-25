create or replace PROCEDURE PR_CATALOG_STYLE_PROC
IS
BEGIN
EXECUTE IMMEDIATE 'TRUNCATE TABLE TABLE_CATALOG_STYLE';
EXECUTE IMMEDIATE 'TRUNCATE TABLE CATALOG_STYLE_TAB';

INSERT INTO TABLE_CATALOG_STYLE  
(select DISTINCT SCL.ptc_str_3typeInfoLCSSKUSeaso  str,
                DECODE (SN.ptc_str_4typeInfoLCSSeason, 'agrFallWinter' , concat ('09/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1),
								  'agrFall' , concat ('09/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1),
								  'agrSpringSummer' , concat ('04/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1),
								  'agrSpring' , concat ('04/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1),
								  'agrSummer' , concat ('04/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1),
								  'agrWinter' , concat ('09/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1)) DATEEFFECTIVE, 
				DECODE (SN.ptc_str_4typeInfoLCSSeason, 'agrFallWinter' , concat ('12/31/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrFall' , concat ('12/31/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSpringSummer' , concat ('06/30/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSpring' , concat ('06/30/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSummer' , concat ('06/30/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrWinter' , concat ('12/31/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4))) dateexpiration, 
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
				PR.ptc_lng_4typeInfoLCSProduct STYLE
  from LCSMOAOBJECT_DATADUMP MOA, LCSSEASON SN, LCSSKUSEASONLINK SCL, LCSSKU SK, LCSPRODUCT PR
  where  
  PR.BRANCHIDITERATIONINFO = SCL.PRODUCTAREVID and 
  PR.ptc_lng_4typeInfoLCSProduct <> 0 and
  sk.ida3a12 = SCL.productmasterid and
  SCL.SKUMASTERID = SK.ida3masterreference AND
  SCL.SEASONREVID = SN.BRANCHIDITERATIONINFO and
  PR.VERSIONIDA2VERSIONINFO = 'A' and
  PR.flextypeidpath is not null and
  PR.ptc_str_15typeInfoLCSProduct is not null and 
  SN.FLEXTYPEIDPATH = '\43182\50140904' AND 
  PR.LATESTITERATIONINFO = '1' and
  MOA.ida3a5 = SK.ida3masterreference and
SCL.seasonremoved =0 and
moa.ptc_bln_1typeInfoLCSMOAObjec = '1' and
SK.LATESTITERATIONINFO = '1' AND
SN.latestiterationinfo = 1 AND
SK.ptc_bln_1typeInfoLCSSKU = '1' AND
SCL.SEASONLINKTYPE='SKU' and
SK.VERSIONIDA2VERSIONINFO = 'A' and
SN.active = 1 AND
SCL.EFFECTLATEST = '1' and
SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agr%' and
moa.ptc_str_2typeInfoLCSMOAObjec IS NOT NULL and
--moa.PTC_STR_5TYPEINFOLCSMOAOBJEC IS NOT NULL and
SCL.ptc_str_3typeInfoLCSSKUSeaso is not null and
PR.ptc_lng_4typeInfoLCSProduct > 0 and 
SCL.EFFECTOUTDATE is null 
AND SN.PTC_STR_1TYPEINFOLCSSEASON not in (select distinct PTC_STR_1TYPEINFOLCSSEASON from lcsseason where FLEXTYPEIDPATH ='\43182\50140904' AND PTC_STR_3TYPEINFOLCSSEASON ='agr2021' and PTC_STR_4TYPEINFOLCSSEASON ='agrFallWinter')
and ((SCL.ptc_bln_2typeInfoLCSSKUSeaso=0 or SCL.ptc_bln_2typeInfoLCSSKUSeaso is null) and (SCL.ptc_bln_3typeInfoLCSSKUSeaso=0 or SCL.ptc_bln_3typeInfoLCSSKUSeaso is null)) and
(SCL.ptc_str_3typeInfoLCSSKUSeaso not like '%agrSMU%' AND ((PR.ptc_str_45typeInfoLCSProduct like '%agrBOS%' OR PR.ptc_str_45typeInfoLCSProduct like '%agrCore%') AND  (SCL.ptc_str_3typeInfoLCSSKUSeaso like '%Inline%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerSpeciality%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerTeam%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrTeam%') AND SCL.ptc_str_3typeInfoLCSSKUSeaso not like '%agrOriginals%'))

union 

select DISTINCT SCL.ptc_str_3typeInfoLCSSKUSeaso  str,
                DECODE (SN.ptc_str_4typeInfoLCSSeason, 'agrFallWinter' , concat ('09/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1),
								  'agrFall' , concat ('09/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1),
								  'agrSpringSummer' , concat ('04/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1),
								  'agrSpring' , concat ('04/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1),
								  'agrSummer' , concat ('04/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1),
								  'agrWinter' , concat ('09/01/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)-1)) DATEEFFECTIVE, 
				DECODE (SN.ptc_str_4typeInfoLCSSeason, 'agrFallWinter' , concat ('12/31/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrFall' , concat ('12/31/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSpringSummer' , concat ('06/30/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSpring' , concat ('06/30/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrSummer' , concat ('06/30/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4)),
								  'agrWinter' , concat ('12/31/' ,substr(SN.ptc_str_3typeInfoLCSSeason, 4, 4))) dateexpiration, 
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
				PR.ptc_lng_4typeInfoLCSProduct STYLE

  from LCSSEASON SN, skuseasonlink SCL, LCSSKU SK, LCSPRODUCT PR, SKUSIZEMASTER SKMSTR, SKUSIZE SKSIZE, SKUSIZETOSEASON SKUSIZETOSEASON, SKUSIZETOSEASONMASTER
  where  
  PR.BRANCHIDITERATIONINFO = SCL.PRODUCTAREVID and 
  PR.ptc_lng_4typeInfoLCSProduct <> 0 and
  sk.ida3a12 = SCL.productmasterid and
  SCL.ida3a5 = SKMSTR.ida3a6 AND
  SCL.SEASONREVID = SN.BRANCHIDITERATIONINFO and
  SCL.SKUMASTERID = SK.ida3masterreference AND
  SKMSTR.IDA3A6 = SK.IDA3MASTERREFERENCE AND
  SKMSTR.IDA2A2 = SKSIZE.IDA3MASTERREFERENCE AND
  SKUSIZETOSEASON.IDA3MASTERREFERENCE = SKUSIZETOSEASONMASTER.IDA2A2 AND
  SKUSIZETOSEASONMASTER.IDA3B6 = SKMSTR.IDA2A2 AND
  SN.ida3masterreference=SKUSIZETOSEASONMASTER.IDA3A6 AND
  PR.VERSIONIDA2VERSIONINFO = 'A' and
  PR.flextypeidpath is not null and
  --PR.ptc_str_15typeInfoLCSProduct is not null AND
  (PR.ptc_str_15typeInfoLCSProduct  is not null OR
  PR.ptc_str_4typeInfoLCSProduct  is not null) AND  
  PR.LATESTITERATIONINFO = 1 and
SCL.seasonremoved =0 and
SK.LATESTITERATIONINFO = 1 AND
SN.latestiterationinfo = 1 AND
SK.ptc_bln_1typeInfoLCSSKU = 1 AND
SCL.SEASONLINKTYPE='SKU' and
SK.VERSIONIDA2VERSIONINFO = 'A' and
SCL.EFFECTLATEST = 1 and
SN.active = 1 AND
SN.FLEXTYPEIDPATH = '\43182\50140904\50413105' AND 
SKSIZE.active = 1 AND
SKSIZE.latestiterationinfo = 1 AND
SKUSIZETOSEASON.active = 1 AND
SKUSIZETOSEASON.latestiterationinfo = 1 AND
SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agr%' and
SKSIZE.ptc_str_1typeInfoSKUSize is not null and
SCL.ptc_str_3typeInfoLCSSKUSeaso is not null and
SCL.EFFECTOUTDATE is null and 
PR.ptc_lng_4typeInfoLCSProduct > 0 and 
--SKSIZE.ptc_str_10typeinfoskusize is not null 
((SCL.ptc_bln_2typeInfoLCSSKUSeaso=0 or SCL.ptc_bln_2typeInfoLCSSKUSeaso is null) and (SCL.ptc_bln_3typeInfoLCSSKUSeaso=0 or SCL.ptc_bln_3typeInfoLCSSKUSeaso is null)) and
(SCL.ptc_str_3typeInfoLCSSKUSeaso not like '%agrSMU%' AND ((PR.ptc_str_45typeInfoLCSProduct like '%agrBOS%' OR PR.ptc_str_45typeInfoLCSProduct like '%agrCore%') AND  (SCL.ptc_str_3typeInfoLCSSKUSeaso like '%Inline%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerSpeciality%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrSoccerTeam%' OR SCL.ptc_str_3typeInfoLCSSKUSeaso like '%agrTeam%') AND SCL.ptc_str_3typeInfoLCSSKUSeaso not like '%agrOriginals%')));

UPDATE TABLE_CATALOG_STYLE SET STR = REPLACE(TRIM(STR),'|~*~|',',');
UPDATE TABLE_CATALOG_STYLE set STR = SUBSTR(STR,1,INSTR(STR,',',-1)-1);

INSERT INTO CATALOG_STYLE_TAB 
(SELECT 1033 LOCALEID,
           TABLE_CATALOG_STYLE.SEASONTYPE||TABLE_CATALOG_STYLE.SEASONYEAR||SUBSTR(trim(regexp_substr(TABLE_CATALOG_STYLE.STR, '[^,]+', 1, lines.column_value)),4)||TABLE_CATALOG_STYLE.STYLE HOSTREF,
		   TABLE_CATALOG_STYLE.SEASONTYPE||TABLE_CATALOG_STYLE.SEASONYEAR||SUBSTR(trim(regexp_substr(TABLE_CATALOG_STYLE.STR, '[^,]+', 1, lines.column_value)),4)||TABLE_CATALOG_STYLE.STYLE CATALOGSTYLE,
		   'AGRONB2B' WEBSITE,
		   TABLE_CATALOG_STYLE.SEASONTYPE||TABLE_CATALOG_STYLE.SEASONYEAR||SUBSTR(trim(regexp_substr(TABLE_CATALOG_STYLE.STR, '[^,]+', 1, lines.column_value)),4) CATALOG,
		   TABLE_CATALOG_STYLE.STYLE,
		   TABLE_CATALOG_STYLE.DATEEFFECTIVE,
		   TABLE_CATALOG_STYLE.DATEEXPIRATION
     FROM TABLE_CATALOG_STYLE ,
      TABLE (CAST (MULTISET
     (SELECT LEVEL FROM dual CONNECT BY LEVEL <= regexp_count(TABLE_CATALOG_STYLE.STR, ',')+1)
                  AS sys.odciNumberList
                  )
            ) lines);

DELETE FROM  CATALOG_STYLE_TAB A 
WHERE   a.rowid >  ANY (SELECT B.rowid FROM CATALOG_STYLE_TAB B
     WHERE 
        A.STYLE = B.STYLE
     AND A.CATALOG = B.CATALOG);

COMMIT;
END;