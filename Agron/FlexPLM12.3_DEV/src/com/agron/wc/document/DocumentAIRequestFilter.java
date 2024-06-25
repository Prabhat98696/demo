package com.agron.wc.document;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.VersionHelper;

import wt.util.WTException;

/**
 * SSP for setting Document Image from Adobe illustrator .
 * 
 * @author Pawan Prajapat (updated version from Doug Ford)
 * FLexPLM upgrade to 11.1. Fox release 
 * DocumentAIRequestFilter class use to impliemnt filter class for tracking request 
 * This code is work for HTTP and HTTPS 
 * April 2020
 */
public class DocumentAIRequestFilter implements Filter {

	/** LOGGER field */
	public static final Logger logger = LogManager.getLogger("com.agron.wc.document");
	
	/**
	 * dofilter is use for tracking incoming request and response.
	 * we are taking if request is comming for particular uri then we have perform some opration.
	 *
	 * @throws WTException
	 * @throws LCSException
	 */
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		System.out.println("DocumentAIRequestFilter .... ");
		if (req instanceof HttpServletRequest) {
			//logger.debug("####REQUEST START############");
			HttpServletRequest request = (HttpServletRequest) req;

			HttpServletResponse response = (HttpServletResponse) res;
			String ae = request.getHeader("accept-encoding");
			HttpServletRequest httpServletRequest = (HttpServletRequest) req;
			
			String value = httpServletRequest.getRequestURI();
			System.out.println(value);
			if(value.startsWith("/Windchill/servlet/rest/p76/documents/") && value.endsWith("/checkin"))
			{
				String paramValue = value;
				paramValue = paramValue.replace("/Windchill/servlet/rest/p76/documents/", "").replace("/checkin", "");
				if (paramValue == null) {
					return;
				}
				String oid = URLDecoder.decode(paramValue.trim(), "UTF-8");
				//skuObj = (LCSSKU) VersionHelper.getVersion(document.getMaster(), "A");
				try {
					LCSDocument document = (LCSDocument) LCSQuery.findObjectById(oid);
					document = (LCSDocument) VersionHelper.latestIterationOf(document);
					AgronDocumentPlugins.setColorwayThumbnails(document);
				}  catch (WTException e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("Response :"+response.getStatus()+"type::"+response.getContentType()+"::getBufferSize-"+response.getBufferSize()+"::getHeaderNames -"+response.getHeaderNames());

			logger.debug("Response :"+response.getStatus()+"type::"+response.getContentType()+"::getBufferSize-"+response.getBufferSize()+"::getHeaderNames -"+response.getHeaderNames());
			//logger.debug("##**REQUEST COMPLETED***##");
			chain.doFilter(req, res);
		}
	}

	public void init(FilterConfig filterConfig) {
	}

	public void destroy() {
	}


}
