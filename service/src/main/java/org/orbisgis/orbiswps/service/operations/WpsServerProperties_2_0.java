/*
 * OrbisWPS contains a set of libraries to build a Web Processing Service (WPS)
 * compliant with the 2.0 specification.
 *
 * OrbisWPS is part of the OrbisGIS platform
 *
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 *
 * OrbisWPS is distributed under GPL 3 license.
 *
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * OrbisWPS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisWPS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisWPS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.orbiswps.service.operations;

import net.opengis.ows._2.*;
import org.orbisgis.orbiswps.serviceapi.operations.WpsProperties;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._1999.xlink.ActuateType;
import org.w3._1999.xlink.ShowType;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Properties of the wps server.
 *
 * @author Sylvain PALOMINOS
 */
@Component(service = WpsProperties.class)
public class WpsServerProperties_2_0 implements WpsProperties {

    /** CoreWorkspace of OrbisGIS */
    private static final Logger LOGGER = LoggerFactory.getLogger(WpsServerProperties_2_0.class);
    private static final I18n I18N = I18nFactory.getI18n(WpsServerProperties_2_0.class);
    private static final String SERVER_PROPERTIES = "wpsServer.properties";
    private static final String BASIC_SERVER_PROPERTIES = "basicWpsServer.properties";

    /** Properties objects */
    public GlobalProperties GLOBAL_PROPERTIES;
    public ServiceIdentificationProperties SERVICE_IDENTIFICATION_PROPERTIES;
    public ServiceProviderProperties SERVICE_PROVIDER_PROPERTIES;
    public OperationsMetadataProperties OPERATIONS_METADATA_PROPERTIES;
    public CustomProperties CUSTOM_PROPERTIES;

    /**
     * Creates a WpsServerProperties_2_0 object which contains all the properties used in a WpsServer.
     * @param propertyFileLocation Location of the properties file. If null, it uses the default properties file.
     */
    public WpsServerProperties_2_0(String propertyFileLocation){
        Properties wpsProperties = null;
        if(propertyFileLocation != null) {
            //Load the property file
            File propertiesFile = new File(propertyFileLocation);
            if (propertiesFile.exists()) {
                try {
                    wpsProperties = new Properties();
                    wpsProperties.load(new FileInputStream(propertiesFile));
                } catch (IOException e) {
                    LOGGER.warn(I18N.tr("Unable to restore the wps properties."));
                    wpsProperties = null;
                }
            }
            if (wpsProperties != null) {
                try {
                    GLOBAL_PROPERTIES = new GlobalProperties(wpsProperties);
                    SERVICE_IDENTIFICATION_PROPERTIES = new ServiceIdentificationProperties(wpsProperties);
                    SERVICE_PROVIDER_PROPERTIES = new ServiceProviderProperties(wpsProperties);
                    OPERATIONS_METADATA_PROPERTIES = new OperationsMetadataProperties(wpsProperties);
                    CUSTOM_PROPERTIES = new CustomProperties(wpsProperties);
                } catch (Exception e) {
                    LOGGER.warn(I18N.tr("Unable to load the server configuration.\nCause : {0}\nLoading the default configuration.", e.getMessage()));
                    wpsProperties = null;
                }
            }
        }
        if(wpsProperties == null){
            wpsProperties = new Properties();
            URL url = this.getClass().getResource(BASIC_SERVER_PROPERTIES);
            if(url == null){
                LOGGER.error(I18N.tr("Unable to find the basic server properties file."));
            }
            else {
                try {
                    wpsProperties.load(new InputStreamReader(url.openStream()));
                    GLOBAL_PROPERTIES = new GlobalProperties(wpsProperties);
                    SERVICE_IDENTIFICATION_PROPERTIES = new ServiceIdentificationProperties(wpsProperties);
                    SERVICE_PROVIDER_PROPERTIES = new ServiceProviderProperties(wpsProperties);
                    OPERATIONS_METADATA_PROPERTIES = new OperationsMetadataProperties(wpsProperties);
                    CUSTOM_PROPERTIES = new CustomProperties(wpsProperties);
                } catch (Exception ex) {
                    LOGGER.error(I18N.tr("Unable to load the server configuration.\nCause : {0}\nLoading the default configuration.", ex.getMessage()));
                    GLOBAL_PROPERTIES = null;
                }
            }
        }
    }

    /**
     * Creates a WpsServerProperties_2_0 object which contains all the properties used in a WpsServer.
     */
    public WpsServerProperties_2_0(){
        Properties wpsProperties = new Properties();
        URL url = this.getClass().getResource(BASIC_SERVER_PROPERTIES);
        if(url == null){
            LOGGER.error(I18N.tr("Unable to find the basic server properties file."));
        }
        else {
            try {
                wpsProperties.load(new InputStreamReader(url.openStream()));
                GLOBAL_PROPERTIES = new GlobalProperties(wpsProperties);
                SERVICE_IDENTIFICATION_PROPERTIES = new ServiceIdentificationProperties(wpsProperties);
                SERVICE_PROVIDER_PROPERTIES = new ServiceProviderProperties(wpsProperties);
                OPERATIONS_METADATA_PROPERTIES = new OperationsMetadataProperties(wpsProperties);
                CUSTOM_PROPERTIES = new CustomProperties(wpsProperties);
            } catch (Exception ex) {
                LOGGER.error(I18N.tr("Unable to load the server configuration.\nCause : {0}\nLoading the default configuration.", ex.getMessage()));
                GLOBAL_PROPERTIES = null;
            }
        }
    }

    @Override
    public String getWpsVersion() {
        return "2.0";
    }

    /** Global properties of the server */
    public class GlobalProperties{
        /** Service provided by the server, WPS by default */
        public final String SERVICE;
        /** Version of the server. */
        public final String SERVER_VERSION;
        /** Supported version of the WPS (1.0.0, 2.0.0 ...). */
        public final String[] SUPPORTED_VERSIONS;
        /** Default languages. */
        public final String DEFAULT_LANGUAGE;
        /** Supported languages. */
        public final String[] SUPPORTED_LANGUAGES;
        /** Supported format for the communication with the client. */
        public final String[] SUPPORTED_FORMATS;
        /** Available job control. */
        public final String[] JOB_CONTROL_OPTIONS;
        /** Available transmission type. */
        public final String[] DATA_TRANSMISSION_TYPE;

        public GlobalProperties(Properties properties) throws Exception {
            SERVICE = properties.getProperty("SERVICE");

            SERVER_VERSION = properties.getProperty("SERVER_VERSION");

            String supportedVersions = properties.getProperty("SUPPORTED_VERSIONS");
            if(supportedVersions == null || supportedVersions.isEmpty()){
                throw new Exception(I18N.tr("The property 'SUPPORTED_VERSIONS' isn't defined"));
            }
            SUPPORTED_VERSIONS = properties.getProperty("SUPPORTED_VERSIONS").split(",");

            String supportedLanguages = properties.getProperty("SUPPORTED_LANGUAGES");
            if(supportedLanguages == null || supportedLanguages.isEmpty()){
                throw new Exception(I18N.tr("The property 'SUPPORTED_LANGUAGES' isn't defined"));
            }
            SUPPORTED_LANGUAGES = properties.getProperty("SUPPORTED_LANGUAGES").split(",");

            DEFAULT_LANGUAGE = properties.getProperty("DEFAULT_LANGUAGE");

            String supportedFormats = properties.getProperty("SUPPORTED_FORMATS");
            if(supportedFormats == null || supportedFormats.isEmpty()){
                throw new Exception(I18N.tr("The property 'SUPPORTED_FORMATS' isn't defined"));
            }
            SUPPORTED_FORMATS = properties.getProperty("SUPPORTED_FORMATS").split(",");

            String jobControlOptions = properties.getProperty("JOB_CONTROL_OPTIONS");
            if(jobControlOptions == null || jobControlOptions.isEmpty()){
                throw new Exception(I18N.tr("The property 'JOB_CONTROL_OPTIONS' isn't defined"));
            }
            JOB_CONTROL_OPTIONS = properties.getProperty("JOB_CONTROL_OPTIONS").split(",");
            DATA_TRANSMISSION_TYPE = properties.getProperty("DATA_TRANSMISSION_TYPE").split(",");
        }
    }

    /** Properties associated to the service identification part of the server */
    public class ServiceIdentificationProperties{
        /** Service provided by the server, WPS by default */
        public final CodeType SERVICE_TYPE;
        /** Version of the server. */
        public final String[] SERVICE_TYPE_VERSIONS;
        /** Supported version of the WPS (1.0.0, 2.0.0 ...). */
        public final LanguageStringType[] TITLE;
        /** Default languages. */
        public final LanguageStringType[] ABSTRACT;
        /** Supported languages. */
        public final KeywordsType[] KEYWORDS;
        /** Supported format for the communication with the client. */
        public final String FEES;
        /** Default languages. */
        public final String[] ACCESS_CONSTRAINTS;

        public ServiceIdentificationProperties(Properties properties) throws Exception {
            // Sets the service type property
            SERVICE_TYPE = new CodeType();
            SERVICE_TYPE.setValue(properties.getProperty("SERVICE_TYPE"));

            // Sets the service type version which is an array of values. So first check if the property isn't null or
            // empty.
            String serviceTypeVersions = properties.getProperty("SERVICE_TYPE_VERSIONS");
            if(serviceTypeVersions == null || serviceTypeVersions.isEmpty()){
                throw new Exception(I18N.tr("The property 'SERVICE_TYPE_VERSIONS' isn't defined"));
            }
            SERVICE_TYPE_VERSIONS = properties.getProperty("SERVICE_TYPE_VERSIONS").split(",");

            // Sets the title which is an array of LanguageStringType. So the property is split with the ';' character
            // and the first string is under the first language, the second one in the second language ...
            String title = properties.getProperty("TITLE");
            //First test if the title property was set in the property file
            if(title == null || title.isEmpty()){
                throw new Exception(I18N.tr("The property 'TITLE' isn't defined"));
            }
            //Split the title property string and check if there is enough languages
            String[] titleSplit = title.split(";");
            if(titleSplit.length != GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES.length){
                throw new Exception(I18N.tr("The property 'TITLE' doesn't contain the good number of string."));
            }
            //Sets the title with the constructed LanguageStringType.
            TITLE = new LanguageStringType[titleSplit.length];
            for(int i=0; i<titleSplit.length; i++){
                TITLE[i] = new LanguageStringType();
                TITLE[i].setValue(titleSplit[i]);
                TITLE[i].setLang(GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES[i]);
            }

            //Sets the abstract which, like the title, is composed of an array of LanguageStringType. So the property
            // is split with the character ';' and stored in a LanguageStringType Object with the good language.
            String abstract_ = properties.getProperty("ABSTRACT");
            //First test if the abstract property was set in the property file
            if(abstract_ == null || abstract_.isEmpty()){
                throw new Exception(I18N.tr("The property 'ABSTRACT' isn't defined"));
            }
            //Split the abstract property string and check if there is enough languages
            String[] abstractSplit = abstract_.split(";");
            if(abstractSplit.length != GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES.length){
                throw new Exception(I18N.tr("The property 'ABSTRACT' doesn't contain the good number of string."));
            }
            //Sets the abstract with the constructed LanguageStringType.
            ABSTRACT = new LanguageStringType[abstractSplit.length];
            for(int i=0; i<abstractSplit.length; i++){
                ABSTRACT[i] = new LanguageStringType();
                ABSTRACT[i].setValue(abstractSplit[i]);
                ABSTRACT[i].setLang(GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES[i]);
            }

            //Sets the keywords which, like the title, is composed of an array of LanguageStringType. So the property
            // is split with the character ';' for each languages and then split with the character ',' to get each
            // keywords.
            String keywords = properties.getProperty("KEYWORDS");
            //First test if the abstract property was set in the property file
            if(keywords == null || keywords.isEmpty()){
                throw new Exception(I18N.tr("The property 'KEYWORDS' isn't defined"));
            }
            //Split the keywords property string by languages and check if there is enough languages
            String[] split = keywords.split(";");
            if(split.length != GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES.length){
                throw new Exception(I18N.tr("The property 'KEYWORDS' doesn't contain the good number of string."));
            }
            //Sets the abstract with the constructed KeywordsType and sets that there is the same number of keywords in
            // each languages.
            String [][] keywordsByLanguage = new String[split.length][];
            for(int i = 0; i<split.length; i++){
                keywordsByLanguage[i] = split[i].split(",");
            }
            for(int i=0; i<keywordsByLanguage.length-1; i++){
                if(keywordsByLanguage[i].length != keywordsByLanguage[i+1].length){
                    throw new Exception(I18N.tr("The property 'KEYWORDS' doesn't contain the same number of keywords " +
                            "for each languages."));
                }
            }
            KEYWORDS = new KeywordsType[keywordsByLanguage[0].length];
            for(int i=0; i<keywordsByLanguage[0].length; i++){
                KEYWORDS[i] = new KeywordsType();
            }
            //For each keyword (index j) add its languageStringType with the language (index i) to have the keywordType
            // (build this way : [ {key[0],lang[0]}, {key[2],lang[0]}, {key[2],lang[0]} ],
            //                   [ {key[0],lang[1]}, {key[2],lang[1]}, {key[2],lang[1]} ]
            for(int j=0; j<keywordsByLanguage[0].length; j++){ //Keyword loop
                List<LanguageStringType> keywordList = new ArrayList<>();
                for(int i = 0; i<keywordsByLanguage.length; i++){// Language loop
                    LanguageStringType keyword = new LanguageStringType();
                    keyword.setLang(GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES[i]);
                    keyword.setValue(keywordsByLanguage[i][j]);
                    keywordList.add(keyword);
                }
                KEYWORDS[j].getKeyword().addAll(keywordList);
            }

            FEES = properties.getProperty("FEES");


            String accessConstraints = properties.getProperty("ACCESS_CONSTRAINTS");
            if(accessConstraints == null || accessConstraints.isEmpty()){
                throw new Exception(I18N.tr("The property 'ACCESS_CONSTRAINTS' isn't defined"));
            }
            ACCESS_CONSTRAINTS = properties.getProperty("ACCESS_CONSTRAINTS").split(",");
        }
    }

    /** Properties associated to the service provider part of the server */
    public class ServiceProviderProperties{
        /** Service provider name. */
        public final String PROVIDER_NAME;
        /** Reference to the most relevant web site of the service provider. */
        public final OnlineResourceType PROVIDER_SITE;
        /** Information for contacting the service provider. he OnlineResource element within this ServiceContact
         * element should not be used to reference a web site of the service provider.. */
        public final ResponsiblePartySubsetType SERVICE_CONTACT;

        public ServiceProviderProperties(Properties properties) throws Exception {
            PROVIDER_NAME = properties.getProperty("PROVIDER_NAME");
            if(properties.containsKey("PROVIDER_SITE")) {
                PROVIDER_SITE = new OnlineResourceType();
                if (properties.containsKey("PROVIDER_SITE_HREF")) {
                    PROVIDER_SITE.setHref(properties.getProperty("PROVIDER_SITE_HREF"));
                }
                if (properties.containsKey("PROVIDER_SITE_ROLE")) {
                    PROVIDER_SITE.setRole(properties.getProperty("PROVIDER_SITE_ROLE"));
                }
                if (properties.containsKey("PROVIDER_SITE_ARCROLE")) {
                    PROVIDER_SITE.setArcrole(properties.getProperty("PROVIDER_SITE_ARCROLE"));
                }
                if (properties.containsKey("PROVIDER_SITE_TITLE")) {
                    PROVIDER_SITE.setTitle(properties.getProperty("PROVIDER_SITE_TITLE"));
                }
                if (properties.containsKey("PROVIDER_SITE_SHOW")) {
                    PROVIDER_SITE.setShow(ShowType.valueOf(properties.getProperty("PROVIDER_SITE_SHOW").toUpperCase()));
                }
                if (properties.containsKey("PROVIDER_SITE_ACTUATE")) {
                    PROVIDER_SITE.setActuate(ActuateType.valueOf(properties.getProperty("PROVIDER_SITE_ACTUATE").toUpperCase()));
                }
            }
            else{
                PROVIDER_SITE=null;
            }
            if(properties.containsKey("SERVICE_CONTACT")){
                SERVICE_CONTACT = new ResponsiblePartySubsetType();
                if(properties.containsKey("SERVICE_CONTACT_INDIVIDUAL_NAME")){
                    SERVICE_CONTACT.setIndividualName(properties.getProperty("SERVICE_CONTACT_INDIVIDUAL_NAME"));
                }
                if(properties.containsKey("SERVICE_CONTACT_POSITION_NAME")){
                    SERVICE_CONTACT.setPositionName(properties.getProperty("SERVICE_CONTACT_POSITION_NAME"));
                }
                if(properties.containsKey("SERVICE_CONTACT_INFO")){
                    ContactType contactType = new ContactType();
                    if(properties.containsKey("SERVICE_CONTACT_INFO_ADDRESS")){
                        AddressType addressType = new AddressType();
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ADDRESS_DELIVERY_POINT")) {
                            Collections.addAll(addressType.getDeliveryPoint(),
                                    properties.getProperty("SERVICE_CONTACT_INFO_ADDRESS_DELIVERY_POINT").split(","));
                        }
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ADDRESS_CITY")) {
                            addressType.setCity(properties.getProperty("SERVICE_CONTACT_INFO_ADDRESS_CITY"));
                        }
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ADDRESS_ADMINISTRATIVE_AREA")) {
                            addressType.setAdministrativeArea(properties.getProperty("SERVICE_CONTACT_INFO_ADDRESS_ADMINISTRATIVE_AREA"));
                        }
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ADDRESS_POSTAL_CODE")) {
                            addressType.setPostalCode(properties.getProperty("SERVICE_CONTACT_INFO_ADDRESS_POSTAL_CODE"));
                        }
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ADDRESS_COUNTRY")) {
                            addressType.setCountry(properties.getProperty("SERVICE_CONTACT_INFO_ADDRESS_COUNTRY"));
                        }
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ADDRESS_EMAILS")) {
                            Collections.addAll(addressType.getElectronicMailAddress(),
                                    properties.getProperty("SERVICE_CONTACT_INFO_ADDRESS_EMAILS").split(","));
                        }
                        contactType.setAddress(addressType);
                    }
                    if(properties.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE")){
                        OnlineResourceType onlineResourceType = new OnlineResourceType();
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_HREF")) {
                            onlineResourceType.setHref(properties.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_HREF"));
                        }
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ROLE")) {
                            onlineResourceType.setRole(properties.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ROLE"));
                        }
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ARCROLE")) {
                            onlineResourceType.setArcrole(properties.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ARCROLE"));
                        }
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_TITLE")) {
                            onlineResourceType.setTitle(properties.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_TITLE"));
                        }
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_SHOW")) {
                            onlineResourceType.setShow(ShowType.valueOf(properties
                                    .getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_SHOW").toUpperCase()));
                        }
                        if (properties.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ACTUATE")) {
                            onlineResourceType.setActuate(ActuateType.valueOf(properties
                                    .getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ACTUATE").toUpperCase()));
                        }
                        contactType.setOnlineResource(onlineResourceType);
                    }
                    if (properties.containsKey("SERVICE_CONTACT_INFO_HOURS_OF_SERVICE")) {
                        contactType.setHoursOfService(properties.getProperty("SERVICE_CONTACT_INFO_HOURS_OF_SERVICE"));
                    }
                    if (properties.containsKey("SERVICE_CONTACT_INFO_INSTRUCTIONS")) {
                        contactType.setContactInstructions(properties.getProperty("SERVICE_CONTACT_INFO_INSTRUCTIONS"));
                    }
                    SERVICE_CONTACT.setContactInfo(contactType);
                }
                if(properties.containsKey("SERVICE_CONTACT_ROLE")){
                    CodeType codeType = new CodeType();
                    codeType.setValue(properties.getProperty("SERVICE_CONTACT_ROLE"));
                    SERVICE_CONTACT.setRole(codeType);
                }
            }
            else{
                SERVICE_CONTACT=null;
            }
        }
    }

    /** Properties associated to the operations metadata part of the server */
    public class OperationsMetadataProperties{
        /** Get capabilities operation. */
        public final Operation GET_CAPABILITIES_OPERATION;
        /** Describe process operation. */
        public final Operation DESCRIBE_PROCESS_OPERATION;
        /** Execute operation. */
        public final Operation EXECUTE_OPERATION;
        /** Get status operation. */
        public final Operation GET_STATUS_OPERATION;
        /** Get result operation. */
        public final Operation GET_RESULT_OPERATION;
        /** DIsmiss operation. */
        public final Operation DISMISS_OPERATION;

        public OperationsMetadataProperties(Properties properties) throws Exception {
            ObjectFactory objectFactory = new ObjectFactory();
            if(properties.getProperty("GETCAPABILITIES_GET_HREF") != null &&
                    properties.getProperty("GETCAPABILITIES_POST_HREF") != null) {
                GET_CAPABILITIES_OPERATION = new Operation();
                GET_CAPABILITIES_OPERATION.setName("GetCapabilities");
                DCP dcp = new DCP();
                HTTP http = new HTTP();
                RequestMethodType get = new RequestMethodType();
                get.setHref(properties.getProperty("GETCAPABILITIES_GET_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(get));
                RequestMethodType post = new RequestMethodType();
                post.setHref(properties.getProperty("GETCAPABILITIES_POST_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(post));
                dcp.setHTTP(http);
                GET_CAPABILITIES_OPERATION.getDCP().add(dcp);
            }
            else{
                GET_CAPABILITIES_OPERATION = null;
            }
            if(properties.getProperty("DESCRIBEPROCESS_GET_HREF") != null &&
                    properties.getProperty("DESCRIBEPROCESS_POST_HREF") != null) {
                DESCRIBE_PROCESS_OPERATION = new Operation();
                DESCRIBE_PROCESS_OPERATION.setName("DescribeProcess");
                DCP dcp = new DCP();
                HTTP http = new HTTP();
                RequestMethodType get = new RequestMethodType();
                get.setHref(properties.getProperty("DESCRIBEPROCESS_GET_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(get));
                RequestMethodType post = new RequestMethodType();
                post.setHref(properties.getProperty("DESCRIBEPROCESS_POST_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(post));
                dcp.setHTTP(http);
                DESCRIBE_PROCESS_OPERATION.getDCP().add(dcp);
            }
            else{
                DESCRIBE_PROCESS_OPERATION = null;
            }
            if(properties.getProperty("EXECUTE_GET_HREF") != null &&
                    properties.getProperty("EXECUTE_POST_HREF") != null) {
                EXECUTE_OPERATION = new Operation();
                EXECUTE_OPERATION.setName("Execute");
                DCP dcp = new DCP();
                HTTP http = new HTTP();
                RequestMethodType get = new RequestMethodType();
                get.setHref(properties.getProperty("EXECUTE_GET_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(get));
                RequestMethodType post = new RequestMethodType();
                post.setHref(properties.getProperty("EXECUTE_POST_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(post));
                dcp.setHTTP(http);
                EXECUTE_OPERATION.getDCP().add(dcp);
            }
            else{
                EXECUTE_OPERATION = null;
            }
            if(properties.getProperty("GETSTATUS_GET_HREF") != null &&
                    properties.getProperty("GETSTATUS_POST_HREF") != null) {
                GET_STATUS_OPERATION = new Operation();
                GET_STATUS_OPERATION.setName("GetStatus");
                DCP dcp = new DCP();
                HTTP http = new HTTP();
                RequestMethodType get = new RequestMethodType();
                get.setHref(properties.getProperty("GETSTATUS_GET_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(get));
                RequestMethodType post = new RequestMethodType();
                post.setHref(properties.getProperty("GETSTATUS_POST_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(post));
                dcp.setHTTP(http);
                GET_STATUS_OPERATION.getDCP().add(dcp);
            }
            else{
                GET_STATUS_OPERATION = null;
            }
            if(properties.getProperty("GETRESULT_GET_HREF") != null &&
                    properties.getProperty("GETRESULT_POST_HREF") != null) {
                GET_RESULT_OPERATION = new Operation();
                GET_RESULT_OPERATION.setName("GetResult");
                DCP dcp = new DCP();
                HTTP http = new HTTP();
                RequestMethodType get = new RequestMethodType();
                get.setHref(properties.getProperty("GETRESULT_GET_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(get));
                RequestMethodType post = new RequestMethodType();
                post.setHref(properties.getProperty("GETRESULT_POST_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(post));
                dcp.setHTTP(http);
                GET_RESULT_OPERATION.getDCP().add(dcp);
            }
            else{
                GET_RESULT_OPERATION = null;
            }
            if(properties.getProperty("DISMISS_GET_HREF") != null &&
                    properties.getProperty("DISMISS_POST_HREF") != null) {
                DISMISS_OPERATION = new Operation();
                DISMISS_OPERATION.setName("Dismiss");
                DCP dcp = new DCP();
                HTTP http = new HTTP();
                RequestMethodType get = new RequestMethodType();
                get.setHref(properties.getProperty("DISMISS_GET_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(get));
                RequestMethodType post = new RequestMethodType();
                post.setHref(properties.getProperty("DISMISS_POST_HREF"));
                http.getGetOrPost().add(objectFactory.createHTTPGet(post));
                dcp.setHTTP(http);
                DISMISS_OPERATION.getDCP().add(dcp);
            }
            else{
                DISMISS_OPERATION = null;
            }
        }
    }

    /**
     * Class containing custom properties which are not defined in the WPS standard.
     */
    public class CustomProperties{
        /** Static value to convert seconds into milliseconds.*/
        private static final int secondsToMillis = 1000;
        /** Static value to convert minutes into milliseconds.*/
        private static final int minutesToMillis = 60 * secondsToMillis;
        /** Static value to convert hours into milliseconds.*/
        private static final int hoursToMillis = 60 * minutesToMillis;
        /** Static value to convert days into milliseconds.*/
        private static final int daysToMillis = 24 * hoursToMillis;
        /** Static value to convert years into milliseconds.*/
        private static final int yearsToMillis = (int)(365.25 * daysToMillis);

        /** String representation of the delay before destroying results. */
        private String destroyDelay;

        public final long BASE_PROCESS_POLLING_DELAY;
        public final long MAX_PROCESS_POLLING_DELAY;

        /**
         * Properties which are not defined in the WPS standard.
         * @param properties Loaded Properties.
         */
        public CustomProperties(Properties properties){
            destroyDelay = properties.getProperty("DESTROY_DURATION");
            BASE_PROCESS_POLLING_DELAY = Long.decode(properties.getProperty("BASE_PROCESS_POLLING_DELAY"));
            MAX_PROCESS_POLLING_DELAY = Long.decode(properties.getProperty("MAX_PROCESS_POLLING_DELAY"));
        }

        /**
         * Returns the delay before the result deletion in milliseconds.
         * @return The delay before the result deletion in milliseconds.
         */
        public long getDestroyDelayInMillis(){
            int years = Integer.decode(destroyDelay.substring(0, destroyDelay.indexOf("Y")));
            int days = Integer.decode(destroyDelay.substring(destroyDelay.indexOf("Y")+1, destroyDelay.indexOf("D")));
            int hours = Integer.decode(destroyDelay.substring(destroyDelay.indexOf("D")+1, destroyDelay.indexOf("H")));
            int minutes = Integer.decode(destroyDelay.substring(destroyDelay.indexOf("H")+1, destroyDelay.indexOf("M")));
            int seconds = Integer.decode(destroyDelay.substring(destroyDelay.indexOf("M")+1, destroyDelay.indexOf("S")));
            return seconds*secondsToMillis + minutes*minutesToMillis + hours*hoursToMillis + days*daysToMillis + years*yearsToMillis;
        }
    }
}
