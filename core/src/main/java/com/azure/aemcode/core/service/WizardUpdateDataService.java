package com.azure.aemcode.core.service;

import com.azure.aemcode.core.common.util.WizardContextUtil;
import com.day.cq.dam.api.Asset;

/**
 * Application service updating the data POSTed from the wizard in asset metadata node.
 */
public interface WizardUpdateDataService {

    public void updateRelatedAssets(WizardContextUtil context, Asset wizardAsset);

    public void updateEsiteData(WizardContextUtil context, Asset wizardAsset);

    public void updateProductDetails(WizardContextUtil context, Asset wizardAsset);

    public void updateAcls(WizardContextUtil context, Asset wizardAsset);

    public void setIsProcessed(Asset wizardAsset);
}
