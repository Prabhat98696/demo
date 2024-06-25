
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "PR_PRICE_CATALOG_V" ("LOCALEID", "HOSTREF", "PRICECATALOG", "WEBSITE", "SALESORG", "CATALOG", "PRICECODE", "SKU", "DATEEFFECTIVE", "PRICEMSRP", "PRICEWHOLESALE", "PRICENET") AS 
  SELECT DISTINCT "LOCALEID","HOSTREF","PRICECATALOG","WEBSITE","SALESORG","CATALOG","PRICECODE","SKU","DATEEFFECTIVE","PRICEMSRP","PRICEWHOLESALE","PRICENET" FROM PRICE_CATALOG_TAB;

