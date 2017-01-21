package com.azure.aemcode.core.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.aemcode.core.common.util.WizardContextUtil;
import com.azure.aemcode.core.service.WizardUpdateDataService;
import com.day.cq.dam.api.Asset;

@Component
@Service
public class WizardUpdateDataServiceImpl implements WizardUpdateDataService {
    private static final String PROP_ESITES = "aemcode:eSite";
    private static final String PROP_RELATED_ASSETS = "aemcode:relatedAssets";
    private static final String PROP_RELATED_PRODUCTS = "aemcode:relatedProducts";
    private static final String PROP_TARGET_AUDIENCE = "aemcode:targetAudience";
    private static final String PROP_PARTNER_TYPE = "aemcode:partnerType";
    private static final String JCR_METADATA = "jcr:content/metadata";
    private static final String PROP_IS_PROCESSED_ASSET = "isProcessedAsset";
    
    private static Logger log = LoggerFactory.getLogger(WizardUpdateDataServiceImpl.class);

    @Override
    public void updateEsiteData(WizardContextUtil context, Asset wizardAsset) {
        if (!context.getEsiteLocales().isEmpty())
            setMetadataOnAsset(context.getEsiteLocales(), wizardAsset, PROP_ESITES);
    }

    @Override
    public void updateRelatedAssets(WizardContextUtil context, Asset wizardAsset) {
        if (!context.getRelatedAssets().isEmpty())
            setMetadataOnAsset(context.getRelatedAssets(), wizardAsset, PROP_RELATED_ASSETS);
    }

    @Override
    public void updateProductDetails(WizardContextUtil context, Asset wizardAsset) {
        if (!context.getProductCatelogs().isEmpty())
            setMetadataOnAsset(context.getProductCatelogs(), wizardAsset, PROP_RELATED_PRODUCTS);
    }

    @Override
    public void updateAcls(WizardContextUtil context, Asset wizardAsset) {
        if (!context.getAcls().isEmpty()) {
            List<String> partnerType = new ArrayList<String>();
            List<String> targetAudience = new ArrayList<String>();
            Iterator<String> aclIterator = context.getAcls().iterator();
            while (aclIterator.hasNext()) {
                String nextAcl = aclIterator.next();
                log.info("Sample YTest *****************888");
                if(nextAcl.startsWith("Target Audience/"))
                    targetAudience.add(StringUtils.substringAfter(nextAcl, "Target Audience/"));
                else if(nextAcl.startsWith("Partner Type/"))
                    partnerType.add(StringUtils.substringAfter(nextAcl, "Partner Type/"));
            }
            if(!partnerType.isEmpty())
                setMetadataOnAsset(context.getAcls(), wizardAsset, PROP_PARTNER_TYPE);
            if(!targetAudience.isEmpty())
                setMetadataOnAsset(context.getAcls(), wizardAsset, PROP_TARGET_AUDIENCE);
        }
    }

    private void setMetadataOnAsset(List<String> list, Asset wizardAsset, String property) {
        Resource assetResource = wizardAsset.adaptTo(Resource.class);
        if (null != assetResource.getChild(JCR_METADATA)) {
            ModifiableValueMap modifiableMetadataMap = assetResource.getChild(JCR_METADATA).adaptTo(ModifiableValueMap.class);
            modifiableMetadataMap.put(property, list.toArray(new String[list.size()]));
        }
    }

    @Override
    public void setIsProcessed(Asset wizardAsset) {
        Resource assetResource = wizardAsset.adaptTo(Resource.class);
        if(null != assetResource.getChild(JCR_METADATA)) {
            ModifiableValueMap modifiableMetadataMap = assetResource.getChild(JCR_METADATA).adaptTo(ModifiableValueMap.class);
            modifiableMetadataMap.put(PROP_IS_PROCESSED_ASSET, true);
        }
    }
}
