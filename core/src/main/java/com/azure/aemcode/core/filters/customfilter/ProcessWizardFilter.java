package com.azure.aemcode.core.filters.customfilter;

import com.day.cq.commons.servlets.HtmlStatusResponseHelper;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HtmlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traps the meta-data form parameters and saves to the assets
 */
@SlingFilter(scope = SlingFilterScope.REQUEST, order = 0, generateService = true)
public class ProcessWizardFilter implements Filter {

    private static final String RESOURCE_TYPE = "com/aemcodea/dam/processwizard";
    private static final String JCR_METADATA = "jcr:content/metadata";
    private static final String NS_DEF = "./jcr:content/metadata/aemcode:";

    final static Logger log = LoggerFactory.getLogger(ProcessWizardFilter.class);

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;
        log.info("Servlet Filter Called.");
        if (slingRequest.getResource().getResourceType().equals(RESOURCE_TYPE)) {
            try {
                setMetadataAttributes(slingRequest);
            } catch (PersistenceException e) {
                log.error("Exception occured while persisting meta-data form parameters", e);
                HtmlResponse htmlResponse = HtmlStatusResponseHelper.createStatusResponse(false, "Error During Processing");
                htmlResponse.send(slingResponse, true);
                return;
            }
        }
        chain.doFilter(slingRequest, slingResponse);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    private void setMetadataAttributes(SlingHttpServletRequest request) throws PersistenceException {

        ModifiableValueMap modifiableMetadataMap = null;
        Enumeration<?> parameterNames = request.getParameterNames();
        ResourceResolver resourceResolver = request.getResourceResolver();
        
        String[] assetPaths = request.getParameterValues("assetPath");
        if(null != assetPaths) {
            for (String asset : assetPaths) {
                Resource assetResource = resourceResolver.resolve(asset);
                if(null != assetResource && null != assetResource.getChild(JCR_METADATA)) {
                    modifiableMetadataMap = assetResource.getChild(JCR_METADATA).adaptTo(ModifiableValueMap.class);
                }
                
                if(null != modifiableMetadataMap) {
                    while(parameterNames.hasMoreElements()) {
                        String nextParameter = (String) parameterNames.nextElement();
                        if(nextParameter.startsWith(NS_DEF) && !nextParameter.contains("TypeHint")) {
                            String propertyName = StringUtils.substringAfter(nextParameter, "./jcr:content/metadata/");
                            if(null != request.getParameter(nextParameter)) {
                                modifiableMetadataMap.put(propertyName, request.getParameter(nextParameter));
                            }
                        }
                    }
                }
            }
        }
        resourceResolver.commit();
    }
}