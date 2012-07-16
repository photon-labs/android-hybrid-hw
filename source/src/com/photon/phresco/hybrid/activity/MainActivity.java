/*
 * ###
 * PHR_android-hybrid-hw
 * %%
 * Copyright (C) 1999 - 2012 Photon Infotech Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ###
 */
package com.photon.phresco.hybrid.activity;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;

import com.phonegap.DroidGap;
import com.photon.phresco.hybrid.config.ConfigReader;
import com.photon.phresco.hybrid.config.Configuration;
import com.photon.phresco.hybrid.logger.PhrescoLogger;
import com.photon.phresco.hybrid.utility.ConnectivityMessaging;
import com.photon.phresco.hybrid.utility.Constants;
import com.photon.phresco.hybrid.utility.Utility;

public class MainActivity extends DroidGap {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	initApplicationEnvironment();
		readConfigXML();
    	super.onCreate(savedInstanceState);
    	
    	/*
    	 * Important
    	 * If this application expects data from outside(from internet) remove the following comments 
    	 */
    	/* if(!ConnectivityMessaging.checkNetworkConnectivity(this)){
			ConnectivityMessaging.showNetworkConectivityAlert(this);
		}
		else if(!ConnectivityMessaging.checkURLStatus(Constants.getWebContextURL() + Constants.getHomeURL())){
			ConnectivityMessaging.showServiceAlert(this);
		} */
        super.loadUrl("file:///android_asset/www/index.html");
    }
    
    /**
	 * Create the required folder structures on external storage Read the device
	 * information
	 */
	private void initApplicationEnvironment() {

		try {
			// Delete existing log file, when application starts, so the log
			// file doesn't consume more place on external device
			Utility.deleteLogFile();

			// Create all the directories required for this application
			Utility.createRequiredDirectory();

			// Get the device information
			Utility.getDeviceInfo();

		} catch (Exception ex) {
			PhrescoLogger.info(TAG
					+ " initApplicationEnvironment -  Exception "
					+ ex.toString());
			PhrescoLogger.warning(ex);
		}
	}

	/**
	 * Read phresco-env-config.xml file to get to connect to web service
	 */
	public void readConfigXML() {
		try {

			String protocol = "protocol";
			String host = "host";
			String port = "port";
			String context = "context";
			String additionalContext = "additional_context";
			Resources resources = getResources();
			AssetManager assetManager = resources.getAssets();
			Properties properties = new Properties();

			// Read from the /assets directory
			InputStream inputStream = assetManager.open(Constants.PHRESCO_ENV_CONFIG);
			ConfigReader confReaderObj = new ConfigReader(inputStream);
			PhrescoLogger.info(TAG + "Default ENV = "
					+ confReaderObj.getDefaultEnvName());
			List<Configuration> configByEnv = confReaderObj
					.getConfigByEnv(confReaderObj.getDefaultEnvName());

			for (Configuration configuration : configByEnv) {
				properties = configuration.getProperties();
				PhrescoLogger.info(TAG + "config value = "
						+ configuration.getProperties());
				String webServiceProtocol = properties.getProperty(protocol)
						.endsWith("://") ? properties.getProperty(protocol)
						: properties.getProperty(protocol) + "://"; // http://

				String webServiceHost = properties.getProperty(port)
						.equalsIgnoreCase("") ? (properties.getProperty(host)
						.endsWith("/") ? properties.getProperty(host)
						: properties.getProperty(host) + "/") : properties
						.getProperty(host); // localhost/
											// localhost

				String webServicePort = properties.getProperty(port)
						.equalsIgnoreCase("") ? "" : (properties.getProperty(
						port).startsWith(":") ? properties.getProperty(port)
						: ":" + properties.getProperty(port)); // "" (blank)
																// :1313

				String webServiceContext = properties.getProperty(context)
						.startsWith("/") ? properties.getProperty(context)
						: "/" + properties.getProperty(context); // /phresco

				String webServiceAdditionalContext = null;
						
				try {
					webServiceAdditionalContext = properties.getProperty(additionalContext)
							.startsWith("/") ? properties.getProperty(additionalContext)
							: "/" + properties.getProperty(additionalContext);
				} catch (Exception e) {
					webServiceAdditionalContext = null;
				}
						
						
				if(webServiceAdditionalContext != null && webServiceAdditionalContext.length() > 1) {// > 1 beacuse of "/"
					Constants.setWebContextURL(webServiceProtocol + webServiceHost
							+ webServicePort + webServiceContext + webServiceAdditionalContext + "&userAgent=android");
				} else {
					Constants.setWebContextURL(webServiceProtocol + webServiceHost
							+ webServicePort + webServiceContext + "?userAgent=android");
				}

				PhrescoLogger.info(TAG + "Constants.webContextURL : "
						+ Constants.getWebContextURL());
			}

		} catch (ParserConfigurationException ex) {
			PhrescoLogger.info(TAG
					+ "readConfigXML : ParserConfigurationException: "
					+ ex.toString());
			PhrescoLogger.warning(ex);
		} catch (SAXException ex) {
			PhrescoLogger.info(TAG + "readConfigXML : SAXException: "
					+ ex.toString());
			PhrescoLogger.warning(ex);
		} catch (IOException ex) {
			PhrescoLogger.info(TAG + "readConfigXML : IOException: "
					+ ex.toString());
			PhrescoLogger.warning(ex);
		} catch (Exception ex) {
			PhrescoLogger.info(TAG + "readConfigXML : Exception: "
					+ ex.toString());
			PhrescoLogger.warning(ex);
		}
	}
}