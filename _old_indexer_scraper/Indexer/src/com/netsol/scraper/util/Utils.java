package com.netsol.scraper.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import com.netsol.scraper.reader.ItemValuesBean;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2010</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Utils {
    public static String createVendorSolrMessage(String catalogId) throws
            UnsupportedEncodingException {
        String dataPost = "";

        dataPost = URLEncoder.encode("CATALOG_ID", "UTF-8")
                   + "="
                   + URLEncoder.encode(catalogId, "UTF-8")
                   + "&"
                   + URLEncoder.encode("action", "UTF-8")
                   + "="
                   + URLEncoder.encode("vendorId", "UTF-8");
        return dataPost;
    }

    public static String createVendorNameSolrMessage(String catalogId) throws
            UnsupportedEncodingException {
        String dataPost = "";

        dataPost = URLEncoder.encode("CATALOG_ID", "UTF-8")
                   + "="
                   + URLEncoder.encode(catalogId, "UTF-8")
                   + "&"
                   + URLEncoder.encode("action", "UTF-8")
                   + "="
                   + URLEncoder.encode("vendorName", "UTF-8");
        return dataPost;
    }


    public static String createCommitSolrMessage(String unitId) throws
            UnsupportedEncodingException {
        String dataPost = "";

        dataPost = URLEncoder.encode("UNIT_ID", "UTF-8")
                   + "="
                   + URLEncoder.encode(unitId, "UTF-8")
                   + "&"
                   + URLEncoder.encode("action", "UTF-8")
                   + "="
                   + URLEncoder.encode("commit", "UTF-8");

        return dataPost;
    }

    public static String createSolrMessage(ItemValuesBean bean) throws
            UnsupportedEncodingException {
        String dataPost = "";

        dataPost = URLEncoder.encode("CATALOG_ID", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getCatalogId(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("NEW_ITEM-VENDORMAT", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getItemVendorMat(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("SUPPLIER_NAME", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getSupplierName(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("NEW_ITEM-DESCRIPTION", "UTF-8") + "="
                   + URLEncoder.encode(bean.getDescription(), "UTF-8") + "&"
                   + URLEncoder.encode("NEW_ITEM-CURRENCY", "UTF-8") + "="
                   + URLEncoder.encode(bean.getCurrency(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("NEW_ITEM-PRICE", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getPrice(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("NEW_ITEM-PRICEUNIT", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getItemPriceUnit(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("NEW_ITEM-MANUFACTMAT", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getItemManufactMat(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("NEW_ITEM-MANUFACTCODE", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getItemManufactCode(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("NEW_ITEM-LONGTEXT", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getItemLongText1(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("NEW_ITEM-IMAGE", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getImage(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("NEW_ITEM-URL", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getUrl(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("NEW_ITEM-MATGROUP", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getItemMatGroup(), "UTF-8")
                   + "&"
                   + URLEncoder.encode("NEW_ITEM-UNIT", "UTF-8")
                   + "="
                   + URLEncoder.encode(bean.getUnit(), "UTF-8")
                   ;
        return dataPost;
    }
}
