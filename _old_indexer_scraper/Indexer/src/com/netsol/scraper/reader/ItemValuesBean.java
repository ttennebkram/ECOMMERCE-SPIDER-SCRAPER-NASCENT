package com.netsol.scraper.reader;

import org.apache.solr.client.solrj.beans.Field;

public class ItemValuesBean
{
    @Field("vendor_name")
    private String vendorName = "";
    @Field("image")
    private String image = "";
    @Field("url")
    private String url = "";
    @Field("name")
    private String description = "";
    @Field("id")
    private String itemNo = "";
    private String listPrice = "0.00";
    @Field("price")
    private String price = "0.00";
    @Field("catalog_id")
    private String catalogId = "";
    @Field("currency")
    private String currency = "";
    @Field("unit")
    private String unit = "";
    @Field("user_id")
    private String userId = "";
    @Field("supplier_name")
    private String supplierName = "";
    @Field("mat_nr")
    private String itemMatnr = "";
    @Field("price_unit")
    private String itemPriceUnit = "";
    @Field("leadtime")
    private String itemLeadTime = "";
    @Field("vendor")
    private String itemVendor = "";
    @Field("vendor_mat")
    private String itemVendorMat = "";
    @Field("manufact_code")
    private String itemManufactCode = "";
    @Field("manufact_mat")
    private String itemManufactMat = "";
    @Field("description")
    private String itemLongText1 = "";
    @Field("mat_group")
    private String itemMatGroup = "";
    @Field("cust_field1")
    private String itemCustField1 = "";
    @Field("cust_field2")
    private String itemCustField2 = "";
    @Field("cust_field3")
    private String itemCustField3 = "";
    @Field("cust_field4")
    private String itemCustField4 = "";
    @Field("cust_field5")
    private String itemCustField5 = "";
    @Field("quantity")
    private String itemQuantity = "";
    @Field("quote_quantity")
    private String itemQuoteQuantity = "";
    @Field("contract")
    private String itemContract = "";  //new
    @Field("contract_item")
    private String itemContractItem = "";  //new
    @Field("service")
    private String itemService = "";  //new
    @Field("ext_quote_id")
    private String extQuoteId = "";  //new
    @Field("ext_quote_item")
    private String extQuoteItem = "";  //new
    @Field("prod_id")
    private String itemProductId = "";  //new
    @Field("parent_id")
    private String parentId = "";  //new
    @Field("item_type")
    private String ItemType = "";  //new
    @Field("attachment")
    private String itemAttachment = "";  //new
    @Field("attachment_title")
    private String itemAttachmentTitle = "";  //new
    @Field("attachment_purpose")
    private String itemAttachmentPurpose = "";  //new
    @Field("schema_type")
    private String itemExtSchemaType = "";  //new
    @Field("catagory_id")
    private String itemExtCatagoryId = "";  //new
    @Field("catagory")
    private String itemExtCatagory = "";  //new
    @Field("sld_sys_name")
    private String itemSldSYSName = "";  //new
    @Field("inStock")
    private boolean inStock = true;
    @Field("catalog_type")
    private String catalogType = "1";

    public ItemValuesBean()
    {
    }

    public ItemValuesBean(String catalogId, String userId)
    {
        this.catalogId = catalogId;
        this.userId = userId;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getItemNo()
    {
        return itemNo;
    }

    public void setItemNo(String itemNo)
    {
        this.itemNo = itemNo;
    }

    public String getListPrice()
    {
        if (listPrice == null || listPrice.trim().isEmpty())
            listPrice = "0.00";

        return listPrice.replaceAll(",", "");
    }

    public void setListPrice(String listPrice)
    {
        this.listPrice = listPrice.replaceAll(",", "");

        if (this.listPrice == null || this.listPrice.trim().isEmpty())
            this.listPrice = "0.00";
    }

    public String getPrice()
    {
        if (price == null || price.trim().isEmpty())
            price = "0.00";

        return price.replaceAll("[\\s][\\s]*", "");
    }

    public void setPrice(String price)
    {
        if (price == null || price.trim().isEmpty())
            price = "0.00";

        this.price = price.replaceAll("[\\s][\\s]*", "");
    }

    public String getCatalogId()
    {
        return catalogId;
    }

    public void setCatalogId(String catalogId)
    {
        this.catalogId = catalogId;
    }

    public String getCurrency()
    {
        if (currency != null && currency.trim().equals("$"))
            currency = "USD";
        else if (currency == null || currency.trim().isEmpty())
            currency = "USD";

        return currency.replaceAll("[\\s][\\s]*", "");
    }

    public void setCurrency(String currency)
    {
        if (currency != null && currency.trim().equals("$"))
            currency = "USD";
        else if (currency == null || currency.trim().isEmpty())
            currency = "USD";

        this.currency = currency.replaceAll("[\\s][\\s]*", "");
    }

    public String getUnit()
    {
        if (unit != null && unit.trim().equalsIgnoreCase("box"))
            unit = "BX";
        else if (unit != null && unit.trim().equalsIgnoreCase("each"))
            unit = "EA";
        else if (unit == null || unit.trim().isEmpty())
            unit = "EA";

        return (unit + "").toUpperCase();
    }

    public void setUnit(String unit)
    {
        if (unit != null && unit.trim().equalsIgnoreCase("box"))
            unit = "BX";
        else if (unit != null && unit.trim().equalsIgnoreCase("each"))
            unit = "EA";
        else if (unit == null || unit.trim().isEmpty())
            unit = "EA";

        this.unit = (unit + "").toUpperCase();
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getItemVendor()
    {
        return itemVendor;
    }

    public void setItemVendor(String itemVendor)
    {
        this.itemVendor = itemVendor;
    }

    public String getItemVendorMat()
    {
        return itemVendorMat;
    }

    public void setItemVendorMat(String itemVendorMat)
    {
        this.itemVendorMat = itemVendorMat;
    }

    public void setItemLongText1(String itemLongText1)
    {
        this.itemLongText1 = itemLongText1;
    }

    public String getItemLongText1()
    {
        return itemLongText1;
    }

    public void setItemPriceUnit(String itemPriceUnit)
    {
        this.itemPriceUnit = itemPriceUnit;
    }

    public String getItemPriceUnit()
    {
        return itemPriceUnit;
    }

    public void setItemMatnr(String itemMatnr)
    {
        this.itemMatnr = itemMatnr;
    }

    public String getItemMatnr()
    {
        return itemMatnr;
    }

    public void setItemCustField1(String itemCustField1)
    {
        this.itemCustField1 = itemCustField1;
    }

    public String getItemCustField1()
    {
        return itemCustField1;
    }

    public void setItemCustField2(String itemCustField2)
    {
        this.itemCustField2 = itemCustField2;
    }

    public String getItemCustField2()
    {
        return itemCustField2;
    }

    public void setItemCustField3(String itemCustField3)
    {
        this.itemCustField3 = itemCustField3;
    }

    public String getItemCustField3()
    {
        return itemCustField3;
    }

    public void setItemCustField4(String itemCustField4)
    {
        this.itemCustField4 = itemCustField4;
    }

    public String getItemCustField4()
    {
        return itemCustField4;
    }

    public void setItemCustField5(String itemCustField5)
    {
        this.itemCustField5 = itemCustField5;
    }

    public String getItemCustField5()
    {
        return itemCustField5;
    }

    public void setItemQuantity(String itemQuantity)
    {
        this.itemQuantity = itemQuantity;
    }

    public String getItemQuantity()
    {
        return itemQuantity;
    }

    public void setItemContract(String itemContract)
    {
        this.itemContract = itemContract;
    }

    public String getItemContract()
    {
        return itemContract;
    }

    public void setItemContractItem(String itemContractItem)
    {
        this.itemContractItem = itemContractItem;
    }

    public String getItemContractItem()
    {
        return itemContractItem;
    }

    public void setItemService(String itemService)
    {
        this.itemService = itemService;
    }

    public String getItemService()
    {
        return itemService;
    }

    public void setExtQuoteId(String extQuoteId)
    {
        this.extQuoteId = extQuoteId;
    }

    public String getExtQuoteId()
    {
        return extQuoteId;
    }

    public void setExtQuoteItem(String extQuoteItem)
    {
        this.extQuoteItem = extQuoteItem;
    }

    public String getExtQuoteItem()
    {
        return extQuoteItem;
    }

    public void setItemProductId(String itemProductId)
    {
        this.itemProductId = itemProductId;
    }

    public String getItemProductId()
    {
        return itemProductId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public String getParentId()
    {
        return parentId;
    }

    public void setItemType(String itemType)
    {
        ItemType = itemType;
    }

    public String getItemType()
    {
        return ItemType;
    }

    public void setItemAttachment(String itemAttachment)
    {
        this.itemAttachment = itemAttachment;
    }

    public String getItemAttachment()
    {
        return itemAttachment;
    }

    public void setItemAttachmentTitle(String itemAttachmentTitle)
    {
        this.itemAttachmentTitle = itemAttachmentTitle;
    }

    public String getItemAttachmentTitle()
    {
        return itemAttachmentTitle;
    }

    public void setItemAttachmentPurpose(String itemAttachmentPurpose)
    {
        this.itemAttachmentPurpose = itemAttachmentPurpose;
    }

    public String getItemAttachmentPurpose()
    {
        return itemAttachmentPurpose;
    }

    public void setItemExtSchemaType(String itemExtSchemaType)
    {
        this.itemExtSchemaType = itemExtSchemaType;
    }

    public String getItemExtSchemaType()
    {
        return itemExtSchemaType;
    }

    public void setItemExtCatagoryId(String itemExtCatagoryId)
    {
        this.itemExtCatagoryId = itemExtCatagoryId;
    }

    public String getItemExtCatagoryId()
    {
        return itemExtCatagoryId;
    }

    public void setItemExtCatagory(String itemExtCatagory)
    {
        this.itemExtCatagory = itemExtCatagory;
    }

    public String getItemExtCatagory()
    {
        return itemExtCatagory;
    }

    public void setItemSldSYSName(String itemSldSYSName)
    {
        this.itemSldSYSName = itemSldSYSName;
    }

    public String getItemSldSYSName()
    {
        return itemSldSYSName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setItemManufactMat(String itemManufactMat)
    {
        this.itemManufactMat = itemManufactMat;
    }

    public String getItemManufactMat()
    {
        return itemManufactMat;
    }

    public void setItemManufactCode(String itemManufactCode)
    {
        this.itemManufactCode = itemManufactCode;
    }

    public String getItemManufactCode()
    {
        return itemManufactCode;
    }

    public void setItemMatGroup(String itemMatGroup)
    {
        this.itemMatGroup = itemMatGroup;
    }

    public String getItemMatGroup()
    {
        return itemMatGroup;
    }

    public void setVendorName(String vendorName)
    {
        this.vendorName = vendorName;
    }

    public String getVendorName()
    {
        return vendorName;
    }

    public String getItemLeadTime()
    {
        return itemLeadTime;
    }

    public void setItemLeadTime(String itemLeadTime)
    {
        this.itemLeadTime = itemLeadTime;
    }

    public String getItemQuoteQuantity()
    {
        return itemQuoteQuantity;
    }

    public void setItemQuoteQuantity(String itemQuoteQuantity)
    {
        this.itemQuoteQuantity = itemQuoteQuantity;
    }

    public String getCatalogType()
    {
        return catalogType;
    }

    public void setCatalogType(String catalogType)
    {
        this.catalogType = catalogType;
    }
}

