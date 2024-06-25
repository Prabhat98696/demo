
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "COSTSHEET_VIEW" ("Cost Sheet ID", "Work #", "Cost Sheet Name", "Specification", "Vendor #", "PRIMARYSOURCE", "PRIMARYSTSL", "Effective FOB", "SEASON") AS 
  SELECT DISTINCT 
  cs.ptc_lng_4typeInfoLCSProductC "Cost Sheet ID",
  prdArev.ptc_lng_4typeInfoLCSProduct "Work #",
  cs.ptc_str_1typeInfoLCSProductC "Cost Sheet Name",
  fs.ptc_str_1typeInfoFlexSpecifi "Specification",
  sp.ptc_str_9typeInfoLCSSupplier "Vendor #",
  sourcemaster.PRIMARYSOURCE "PRIMARYSOURCE",
  STSEFF.PRIMARYSTSL "PRIMARYSTSL",
  cs.ptc_dbl_2typeInfoLCSProductC "Effective FOB",
  case sn.ptc_str_3typeInfoLCSSeason
	when 'agr2015' then '2015'
	when 'agr2016' then '2016'
	when 'agr2017' then '2017'
	when 'agr2018' then '2018'
	when 'agr2019' then '2019'
	when 'agr2020' then '2020'
	when 'agr2021' then '2021'
  when 'agr2022' then '2022'
  when 'agr2023' then '2023'
  when 'agr2024' then '2024'
  when 'agr2025' then '2025'
  when 'agr2026' then '2026'
  when 'agr2027' then '2027'
   when 'agr2028' then '2028'
   when 'agr2029' then '2029'
   when 'agr2030' then '2030'
  end ||' '||
  case sn.ptc_str_4typeInfoLCSSeason 
  when 'agrFallWinter' then 'Q3' 
  when 'agrSpringSummer' then 'Q1' 
	when 'agrSummer' then 'Q2'
	when 'agrWinter' then 'Q4'
	when 'agrSpring' then 'Q1'
	when 'agrFall' then 'Q3'
  end Season 
FROM lcsproductcostsheet CS, 
  FlexSpecification FS, 	
  PRODAREV PRDAREV,
	LCSSEASON SN,
  LCSSUPPLIER SP,
  LCSCOSTSHEETMASTER CSMST,
  LCSSOURCINGCONFIG_EFF SCF,
  lcssourcingconfigmaster sourcemaster,
  LCSSourceToSeasonLinkMaster STSMSTR,
  lcssourcetoseasonlink_eff STSEFF
  
where CS.PRODUCTAREVID=PRDAREV.BRANCHIDITERATIONINFO 
AND CS.SEASONMASTERID = SN.idA3masterReference(+)
AND CS.idA3E11=FS.idA3masterReference(+)
AND CS.IDA3MASTERREFERENCE = CSMST.IDA2A2
AND CSMST.IDA3A6 = SCF.IDA3MASTERREFERENCE 
AND SCF.BRANCHIDA3B2TYPEINFOLCSSOURC = SP.BRANCHIDITERATIONINFO(+) 
AND SCF.branchIdA3B2typeInfoLCSSourc = SP.BRANCHIDITERATIONINFO(+) 
AND sourcemaster.IDA2A2 = SCF.IDA3MASTERREFERENCE
and STSEFF.idA3masterReference=STSMSTR.idA2A2 
and STSMSTR.idA3A6=SCF.idA3masterReference  
AND PRDAREV.LATESTITERATIONINFO=1 
AND CS.COSTSHEETTYPE='PRODUCT'
AND CS.LATESTITERATIONINFO=1 
AND SN.LATESTITERATIONINFO =1 
AND SCF.LATESTITERATIONINFO =1 
AND SN.ACTIVE=1
AND SN.ptc_str_10typeinfolcsseason='agrYes'
AND CS.PTC_DBL_2TYPEINFOLCSPRODUCTC <> 0 
AND TRIM(CS.PTC_STR_1TYPEINFOLCSPRODUCTC) !=  'Baseline'
AND SP.LATESTITERATIONINFO=1;

