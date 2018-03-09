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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.json.UTF8StreamJsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.opengis.ows._1.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._1999.xlink.ActuateType;
import org.w3._1999.xlink.ShowType;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Properties of the wps server.
 *
 * @author Sylvain PALOMINOS
 */
public class WpsServerProperties_1_0_0 {

    /** CoreWorkspace of OrbisGIS */
    private static final Logger LOGGER = LoggerFactory.getLogger(WpsServerProperties_1_0_0.class);
    private static final I18n I18N = I18nFactory.getI18n(WpsServerProperties_1_0_0.class);
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
    public WpsServerProperties_1_0_0(String propertyFileLocation){
        Properties wpsProperties = null;
        if(propertyFileLocation != null) {
            //Load the property file
            File propertiesFile = new File(propertyFileLocation);
            if (propertiesFile.exists()) {
                if(FilenameUtils.getExtension(propertiesFile.getName()).equalsIgnoreCase("json")) {
                    ObjectNode objNode = null;
                    try {
                        JsonFactory jsonFactory = new JsonFactory();
                        jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
                        ObjectMapper objMapper = new ObjectMapper(jsonFactory);
                        objNode = objMapper.readValue(propertiesFile, ObjectNode.class);
                        GLOBAL_PROPERTIES = new GlobalProperties(objNode);
                        SERVICE_IDENTIFICATION_PROPERTIES = new ServiceIdentificationProperties(objNode);
                        SERVICE_PROVIDER_PROPERTIES = new ServiceProviderProperties(objNode);
                        /*OPERATIONS_METADATA_PROPERTIES = new OperationsMetadataProperties(objNode);
                        CUSTOM_PROPERTIES = new CustomProperties(objNode);*/
                    } catch (FileNotFoundException e) {
                        LOGGER.warn(I18N.tr("Unable to load the wps properties.\n"+e.getMessage()));
                        GLOBAL_PROPERTIES = null;
                    } catch (Exception ex) {
                        LOGGER.error(I18N.tr("Unable to load the server configuration.\nCause : {0}\nLoading the " +
                                "default configuration.", ex.getMessage()));
                        GLOBAL_PROPERTIES = null;
                    }
                    if (objNode == null) {
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
                                LOGGER.error(I18N.tr("Unable to load the server configuration.\nCause : {0}\nLoading the " +
                                        "default configuration.", ex.getMessage()));
                                GLOBAL_PROPERTIES = null;
                            }
                        }
                    }
                }
                else if(FilenameUtils.getExtension(propertiesFile.getName()).equalsIgnoreCase("properties")) {
                    try {
                        wpsProperties = new Properties();
                        wpsProperties.load(new FileInputStream(propertiesFile));
                    } catch (IOException e) {
                        LOGGER.warn(I18N.tr("Unable to restore the wps properties."));
                        wpsProperties = null;
                    }
                    if (wpsProperties != null) {
                        try {
                            GLOBAL_PROPERTIES = new GlobalProperties(wpsProperties);
                            SERVICE_IDENTIFICATION_PROPERTIES = new ServiceIdentificationProperties(wpsProperties);
                            SERVICE_PROVIDER_PROPERTIES = new ServiceProviderProperties(wpsProperties);
                            OPERATIONS_METADATA_PROPERTIES = new OperationsMetadataProperties(wpsProperties);
                            CUSTOM_PROPERTIES = new CustomProperties(wpsProperties);
                        } catch (Exception e) {
                            LOGGER.warn(I18N.tr("Unable to load the server configuration.\nCause : {0}\nLoading the default " +
                                    "configuration.", e.getMessage()));
                        }
                    }
                    else{
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
                                LOGGER.error(I18N.tr("Unable to load the server configuration.\nCause : {0}\nLoading the " +
                                        "default configuration.", ex.getMessage()));
                                GLOBAL_PROPERTIES = null;
                            }
                        }
                    }
                }
            }
        }
        else{
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
                    LOGGER.error(I18N.tr("Unable to load the server configuration.\nCause : {0}\nLoading the " +
                            "default configuration.", ex.getMessage()));
                    GLOBAL_PROPERTIES = null;
                }
            }
        }
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
        }

        public GlobalProperties(ObjectNode objNode) throws Exception {
            SERVICE = objNode.get("service").asText();
            SERVER_VERSION = objNode.get("service_version").asText();
            SUPPORTED_VERSIONS = nodeToArray(objNode.get("supported_versions"));
            SUPPORTED_LANGUAGES = nodeToArray(objNode.get("supported_languages"));
            DEFAULT_LANGUAGE = objNode.get("default_language").asText();
            SUPPORTED_FORMATS = nodeToArray(objNode.get("supported_format"));
        }
    }

    /** Properties associated to the service identification part of the server */
    public class ServiceIdentificationProperties{
        /** Service provided by the server, WPS by default */
        public final CodeType SERVICE_TYPE;
        /** Supported version of the WPS (1.0.0, 2.0 ...). */
        public final String[] SERVICE_TYPE_VERSIONS;
        /** Available profiles */
        public final String[] PROFILE;
        /** Title of the service */
        public final LanguageStringType[] TITLE;
        /** description of the service. */
        public final LanguageStringType[] ABSTRACT;
        /** Keywords of the service. */
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

            PROFILE = new String[]{"NONE"};

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

        public ServiceIdentificationProperties(ObjectNode objNode) throws Exception {
            JsonNode jsonNode = objNode.get("service_identification");
            // Sets the service type property
            SERVICE_TYPE = new CodeType();
            SERVICE_TYPE.setValue(jsonNode.get("service_type").asText());

            // Sets the service type version which is an array of values.
            SERVICE_TYPE_VERSIONS = nodeToArray(jsonNode.get("service_type_version"));

            // Sets the service type version which is an array of values.
            if(jsonNode.has("profile")) {
                PROFILE = nodeToArray(jsonNode.get("profile"));
            }
            else{
                PROFILE = null;
            }

            // Sets the title which is an array of LanguageStringType.
            JsonNode titles = jsonNode.get("title");
            TITLE = new LanguageStringType[titles.size()];
            for(int i=0; i<titles.size(); i++){
                LanguageStringType type = new LanguageStringType();
                type.setValue(titles.get(i).get("value").asText());
                type.setLang(titles.get(i).get("lang").asText());
                TITLE[i] = type;
            }

            //Sets the abstract which, like the title, is composed of an array of LanguageStringType.
            if(jsonNode.has("abstract")) {
                JsonNode abstracts = jsonNode.get("abstract");
                ABSTRACT = new LanguageStringType[abstracts.size()];
                for (int i = 0; i < abstracts.size(); i++) {
                    LanguageStringType type = new LanguageStringType();
                    type.setValue(abstracts.get(i).get("value").asText());
                    type.setLang(abstracts.get(i).get("lang").asText());
                    ABSTRACT[i] = type;
                }
            }
            else{
                ABSTRACT = null;
            }

            //Sets the keywords which, is composed of an array (which represent one keyword) of arrays of
            //LanguageStringType (which represents all the translation).
            if(jsonNode.has("keywords")){
                JsonNode keywords = jsonNode.get("keywords");
                KEYWORDS = new KeywordsType[keywords.size()];
                for (int i = 0; i < keywords.size(); i++) {
                    JsonNode keyword = keywords.get(i).get("keyword");
                    KEYWORDS[i] = new KeywordsType();
                    for (int j = 0; j < keyword.size(); j++) {
                        LanguageStringType type = new LanguageStringType();
                        type.setValue(keyword.get(j).get("value").asText());
                        type.setLang(keyword.get(j).get("lang").asText());
                        KEYWORDS[i].getKeyword().add(type);
                    }
                }
            }
            else{
                KEYWORDS = null;
            }

            FEES = jsonNode.get("fees").asText();

            if(jsonNode.has("access_constraints")) {
                ACCESS_CONSTRAINTS = nodeToArray(jsonNode.get("access_constraints"));
            }
            else{
                ACCESS_CONSTRAINTS = null;
            }
        }
    }

    /** Properties associated to the service provider part of the server */
    public class ServiceProviderProperties{
        /** Service provider name. */
        public final String PROVIDER_NAME;
        /** Reference to the most relevant web site of the service provider. */
        public final OnlineResourceType PROVIDER_SITE;
        /** Information for contacting the service provider. */
        public final ResponsiblePartySubsetType SERVICE_CONTACT;

        public ServiceProviderProperties(Properties properties) throws Exception {
            PROVIDER_NAME = properties.getProperty("PROVIDER_NAME");
            PROVIDER_SITE = new OnlineResourceType();
            PROVIDER_SITE.setHref(properties.getProperty("PROVIDER_SITE_HREF"));
            SERVICE_CONTACT = null;
        }

        public ServiceProviderProperties(ObjectNode objNode) throws Exception {

            JsonNode jsonObj = objNode.get("service_provider");

            PROVIDER_NAME = jsonObj.get("provider_name").asText();

            JsonNode obj = jsonObj.get("provider_site");
            if(obj != null) {
                PROVIDER_SITE = new OnlineResourceType();
                PROVIDER_SITE.setHref(obj.get("href").asText());
                PROVIDER_SITE.setRole(obj.get("role").asText());
                PROVIDER_SITE.setArcrole(obj.get("arcrole").asText());
                PROVIDER_SITE.setTitle(obj.get("title").asText());
                String str = obj.get("actuate").asText();
                if (str != null) {
                    PROVIDER_SITE.setActuate(ActuateType.fromValue(str.toLowerCase()));
                }
                str = obj.get("show").asText();
                if (str != null) {
                    PROVIDER_SITE.setShow(ShowType.fromValue(str.toLowerCase()));
                }
            }
            else{
                PROVIDER_SITE = null;
            }

            obj = jsonObj.get("service_contact");
            if(obj != null) {
                SERVICE_CONTACT = new ResponsiblePartySubsetType();
                SERVICE_CONTACT.setIndividualName(obj.get("individual_name").asText());
                SERVICE_CONTACT.setPositionName(obj.get("position_name").asText());
                if(obj.get("contact_info") != null) {
                    JsonNode objContact = obj.get("contact_info");
                    ContactType contactType = new ContactType();
                    if (objContact.get("phone") != null) {
                        JsonNode objPhone = objContact.get("phone");
                        TelephoneType telephoneType = new TelephoneType();
                        JsonNode voiceArray = objPhone.get("voice");
                        if(voiceArray != null){
                            for(int i=0; i<voiceArray.size(); i++) {
                                telephoneType.getVoice().add(voiceArray.get(i).asText());
                            }
                        }
                        JsonNode facsimArray = objPhone.get("facsimile");
                        if(facsimArray != null){
                            for(int i=0; i<facsimArray.size(); i++) {
                                telephoneType.getFacsimile().add(facsimArray.get(i).asText());
                            }
                        }
                        contactType.setPhone(telephoneType);
                    }
                    if(objContact.get("address") != null) {
                        JsonNode objAddress = objContact.get("address");
                        AddressType addressType = new AddressType();
                        JsonNode deliveryArray = objAddress.get("delivery_point");
                        if (deliveryArray != null) {
                            for (int i = 0; i < deliveryArray.size(); i++) {
                                addressType.getDeliveryPoint().add(deliveryArray.get(i).asText());
                            }
                        }
                        addressType.setCity(objAddress.get("city").asText());
                        addressType.setAdministrativeArea(objAddress.get("administrative_area").asText());
                        addressType.setPostalCode(objAddress.get("postal_code").asText());
                        addressType.setCountry(objAddress.get("country").asText());
                        contactType.setAddress(addressType);
                        JsonNode emailArray = objAddress.get("emails");
                        if (emailArray != null) {
                            for (int i = 0; i < emailArray.size(); i++) {
                                addressType.getElectronicMailAddress().add(emailArray.get(i).asText());
                            }
                        }
                    }
                    if(objContact.get("online_resource") != null) {
                        OnlineResourceType onlineResourceType = new OnlineResourceType();
                        JsonNode onlineObj = objContact.get("online_resource");
                        onlineResourceType.setHref(onlineObj.get("href").asText());
                        onlineResourceType.setRole(onlineObj.get("role").asText());
                        onlineResourceType.setArcrole(onlineObj.get("arcrole").asText());
                        onlineResourceType.setTitle(onlineObj.get("title").asText());
                        String str = onlineObj.get("actuate").asText();
                        if (str != null) {
                            onlineResourceType.setActuate(ActuateType.fromValue(str.toLowerCase()));
                        }
                        str = onlineObj.get("show").asText();
                        if (str != null) {
                            onlineResourceType.setShow(ShowType.fromValue(str.toLowerCase()));
                        }
                        contactType.setOnlineResource(onlineResourceType);
                    }
                    contactType.setHoursOfService(objContact.get("hours_of_service").asText());
                    contactType.setContactInstructions(objContact.get("instructions").asText());
                    SERVICE_CONTACT.setContactInfo(contactType);
                }
                if(obj.get("role") != null) {
                    CodeType codeType = new CodeType();
                    codeType.setValue(obj.get("role").asText());
                    SERVICE_CONTACT.setRole(codeType);
                }
            }
            else{
                SERVICE_CONTACT = null;
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
        public final boolean IS_STATUS_SUPPORTED;
        public final boolean IS_STORE_SUPPORTED;
        public final String MAXIMUM_MEGABYTES;

        /**
         * Properties which are not defined in the WPS standard.
         * @param properties Loaded Properties.
         */
        public CustomProperties(Properties properties){
            destroyDelay = properties.getProperty("DESTROY_DURATION");
            BASE_PROCESS_POLLING_DELAY = Long.decode(properties.getProperty("BASE_PROCESS_POLLING_DELAY"));
            MAX_PROCESS_POLLING_DELAY = Long.decode(properties.getProperty("MAX_PROCESS_POLLING_DELAY"));
            IS_STATUS_SUPPORTED = Boolean.valueOf(properties.getProperty("IS_STATUS_SUPPORTED"));
            IS_STORE_SUPPORTED = Boolean.valueOf(properties.getProperty("MAX_PROCESS_POLLING_DELAY"));
            MAXIMUM_MEGABYTES = properties.getProperty("MAXIMUM_MEGABYTES");
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

    /**
     * Convert an JsonNode to a String array
     * @param jsonNode JsonNode to convert
     * @return String array
     */
    private String[] nodeToArray(JsonNode jsonNode) {
        List<String> list = new ArrayList<>();
        for(JsonNode node : jsonNode){
            list.add(node.asText());
        }
        return list.toArray(new String[]{});
    }
}
