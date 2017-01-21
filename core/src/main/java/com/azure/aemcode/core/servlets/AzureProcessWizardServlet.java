package com.azure.aemcode.core.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HtmlResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.asset.api.AssetManager;
import com.azure.aemcode.core.common.util.WizardContextUtil;
import com.azure.aemcode.core.service.WizardUpdateDataService;
import com.day.cq.commons.servlets.HtmlStatusResponseHelper;
import com.day.cq.dam.api.Asset;

@SlingServlet(resourceTypes = { "com/aemcodea/dam/processwizard" }, methods = { "POST" }, metatype = true)
public class AzureProcessWizardServlet extends SlingAllMethodsServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8444735921735535152L;
	private static final Logger log = LoggerFactory.getLogger(AzureProcessWizardServlet.class);
	
	@Reference
    WizardUpdateDataService assetUpdateService;

    private static final String uploadsFolderPattern = "/content/dam/aemcode(/.*/)uploads";
    private static final Pattern uploadPattern = Pattern.compile(uploadsFolderPattern);

	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		HtmlResponse htmlResponse = null;
		try {
			ResourceResolver resourceResolver = request.getResourceResolver();

			// Extracting the required POST data parameters.
			WizardContextUtil contextObj = new WizardContextUtil(request);

			// Loop through all the assets and update the data.
			AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);
			String[] assetPaths = request.getParameterValues("assetPath");
			if (null != assetPaths) {
				for (int i = 0; i < assetPaths.length; i++) {
					Resource assetResource = resourceResolver.resolve(assetPaths[i]);
					log.debug("Processing Asset: {}", assetPaths[i]);
					if (null != assetResource && (null != assetResource.adaptTo(Asset.class))) {
						Asset wizardAsset = assetResource.adaptTo(Asset.class);
						updateMetadataAndMoveAsset(wizardAsset, contextObj, resourceResolver, assetManager);
					}
				}
			}

			// committing the pending changes to repository
			resourceResolver.commit();
			htmlResponse = HtmlStatusResponseHelper.createStatusResponse(true, "DAM asset processed");
			htmlResponse.send(response, true);

		} catch (Exception ex) {
			log.error("Exception occured during Process of wizard", ex);
			htmlResponse = HtmlStatusResponseHelper.createStatusResponse(false, "Error During Processing");
			htmlResponse.send(response, true);
		}
	}
	
	/**
     * @param wizardAsset
     * @param contextObj
     * @param resourceResolver
     * @param assetManager
     * @throws PersistenceException
     * @throws InterruptedException 
     */
    private void updateMetadataAndMoveAsset(Asset wizardAsset, WizardContextUtil contextObj,
            ResourceResolver resourceResolver, AssetManager assetManager) throws PersistenceException {

        if (null != wizardAsset) {
            
            //updating the metadata node with the request parameters excluding the data from meta-data form
            // Note - Meta data form parameters are taken care at the filter level
            assetUpdateService.updateRelatedAssets(contextObj, wizardAsset);
            assetUpdateService.updateAcls(contextObj, wizardAsset);
            assetUpdateService.updateEsiteData(contextObj, wizardAsset);
            assetUpdateService.updateProductDetails(contextObj, wizardAsset);
            assetUpdateService.setIsProcessed(wizardAsset);
            
            // Committing the pending changes to repository before moving
            resourceResolver.commit();
            
            // Move the asset to the processed folder.
            Asset processedAsset = null;
            if (isUploadFolder(wizardAsset.getPath())) {
                String processedFolderURL =
                        StringUtils.substringBefore(wizardAsset.getPath(), "/uploads")
                                + "/uploads/processed";
                processedAsset = moveToProcessedFolder(wizardAsset, resourceResolver, assetManager,
                        processedFolderURL);
            }
            
            if(null == processedAsset)
                processedAsset = wizardAsset;
            
            // Copy assets to esite folders
            if (null != contextObj.getEsiteLocales()) {
                copyAssetToLocales(processedAsset, contextObj.getEsiteLocales(), resourceResolver,
                        assetManager);
                resourceResolver.commit();
            }
        }
    }
    
    /**
     * 
     * @param wizardAsset
     * @param resourceResolver
     * @param assetManager
     * @param processedFolderURL
     * @return 
     */
    private Asset moveToProcessedFolder(Asset wizardAsset, ResourceResolver resourceResolver,
            AssetManager assetManager, String processedFolderURL) {
        if (!assetManager.assetExists(processedFolderURL + "/" + wizardAsset.getName())) {
            log.debug("Moving asset {} to folder {}", wizardAsset.getPath(), processedFolderURL);
            assetManager.moveAsset(wizardAsset.getPath(),
                    processedFolderURL + "/" + wizardAsset.getName());
            Resource newAssetResource = resourceResolver.resolve(processedFolderURL + "/" + wizardAsset.getName());
            return newAssetResource.adaptTo(Asset.class);
        }
        return null;
    }
    
    /**
     *
     * @param wizardAsset
     * @param list
     * @param resourceResolver
     * @param assetManager
     */
    private void copyAssetToLocales(Asset wizardAsset, List<String> list,
                                    ResourceResolver resourceResolver, AssetManager assetManager) {
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String folderPath = iterator.next();
            log.debug("Asset being processed: {}", folderPath);
            if (null != resourceResolver.resolve(folderPath)
                    && !assetManager.assetExists(folderPath + "/" + wizardAsset.getName())) {
                assetManager.copyAsset(wizardAsset.getPath(),
                        folderPath + "/" + wizardAsset.getName());
            }
        }
    }

    /**
     *
     * @param itemPath
     * @return
     */
    private boolean isUploadFolder(String itemPath) {
        Matcher uploadMatch = uploadPattern.matcher(itemPath);
        return (uploadMatch.find());
    }

	@Activate
	protected void activate(ComponentContext context) {

	}

}
