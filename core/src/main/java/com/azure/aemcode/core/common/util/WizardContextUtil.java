package com.azure.aemcode.core.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;

/**
 * Context object capturing the wizard steps data from the HTTP POST.
 * This doesn't include meta data form parameters.
 */
public class WizardContextUtil {

    private List<String> esiteLocales = new ArrayList<String>();
    private List<String> relatedAssets = new ArrayList<String>();
    private List<String> productCatelogs = new ArrayList<String>();
    private List<String> acls = new ArrayList<String>();

    public WizardContextUtil(SlingHttpServletRequest request) {
        Enumeration<?> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = (String) parameterNames.nextElement();
            String[] parameterValues = request.getParameterValues(key);
            if (null != parameterValues)
                addParameterToSpecificCategory(key, parameterValues);
        }
    }

    private void addParameterToSpecificCategory(String parameter, String[] parameterValues) {
        if (null != parameterValues) {
            if (parameter.startsWith("esite:")) {
                esiteLocales.addAll(Arrays.asList(parameterValues));
            } else if (parameter.startsWith("aclWidget:")) {
                acls.addAll(Arrays.asList(parameterValues));
            } else if (parameter.startsWith("product:") || parameter.startsWith("category:")) {
                productCatelogs.addAll(Arrays.asList(parameterValues));
            } else if (parameter.equals("./relatedAssets")) {
                relatedAssets.addAll(Arrays.asList(parameterValues));
            }
        }
    }

    public List<String> getEsiteLocales() {
        return esiteLocales;
    }

    public List<String> getRelatedAssets() {
        return relatedAssets;
    }

    public List<String> getProductCatelogs() {
        return productCatelogs;
    }

    public List<String> getAcls() {
        return acls;
    }
}
