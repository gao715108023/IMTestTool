package com.gcj.json;

import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JSONUtils {

    private static final Log LOG = LogFactory.getLog(JSONUtils.class);

    public void str2Json(String src) {
        JSONObject jsonObject = JSONObject.fromObject(src);
        LOG.info(jsonObject);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        JSONUtils jsonUtils = new JSONUtils();
        String src = "{\"type\":\"text\",\"time\":\"2014-04-22 11:13:50\",\"uid\":\"d22348790547\",\"textBlocks\":[{\"type\":\"text\",\"data\":\"haha\"}]}";
        jsonUtils.str2Json(src);
    }
}
