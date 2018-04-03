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
 * Copyright (C) 2015-2018 CNRS (Lab-STICC UMR CNRS 6285)
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.opengis.ows._1.*;
import org.apache.commons.io.FilenameUtils;
import org.orbisgis.orbiswps.serviceapi.operations.WpsProperties;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._1999.xlink.ActuateType;
import org.w3._1999.xlink.ShowType;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Properties of the wps service version 1.0.0.
 *
 * @author Sylvain PALOMINOS
 */
@Component(service = WpsProperties.class)
public class WpsServerProperties_1_0_0 implements WpsProperties {

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(WpsServerProperties_1_0_0.class);
    /** I18N */
    private static final I18n I18N = I18nFactory.getI18n(WpsServerProperties_1_0_0.class);
    /** Name of the default server properties file */
    private static final String BASIC_SERVER_PROPERTIES = "wps_1_0_0_conf.json";

    /** Properties objects */
    public GlobalProperties GLOBAL_PROPERTIES;
    public ServiceIdentificationProperties SERVICE_IDENTIFICATION_PROPERTIES;
    public ServiceProviderProperties SERVICE_PROVIDER_PROPERTIES;
    public OperationsMetadataProperties OPERATIONS_METADATA_PROPERTIES;
    public WSDLProperties WSDL_PROPERTIES;
    public CustomProperties CUSTOM_PROPERTIES;

    /**
     * Creates a WpsServerProperties_1_0_0 object which contains all the properties used in a WpsServer.
     * @param propertyFileLocation Location of the properties file. If null or invalid, uses the default properties file.
     */
    public WpsServerProperties_1_0_0(String propertyFileLocation){
        boolean propertiesLoaded = false;
        if(propertyFileLocation != null) {
            //Load the property file
            File propertiesFile = new File(propertyFileLocation);
            if (propertiesFile.exists()) {
                if (FilenameUtils.getExtension(propertiesFile.getName()).equalsIgnoreCase("json")) {
                    try {
                        loadProperties(propertiesFile.toURI().toURL());
                    } catch (Exception ex) {
                        LOGGER.error(I18N.tr("Unable to load the server configuration.\nCause : {0}\nLoading the " +
                                "default configuration.", ex.getMessage()));
                        GLOBAL_PROPERTIES = null;
                    }
                }
            }
        }
        if(GLOBAL_PROPERTIES == null) {
            loadProperties(WpsServerProperties_1_0_0.class.getResource(BASIC_SERVER_PROPERTIES));
        }
    }

    /**
     * Creates a WpsServerProperties_1_0_0 object which contains all the properties used in a WpsServer with the
     * default properties file.
     */
    public WpsServerProperties_1_0_0(){
        loadProperties(WpsServerProperties_1_0_0.class.getResource(BASIC_SERVER_PROPERTIES));
    }

    /**
     * Open and parse the given URL in order to set the different properties objects.
     * @param propertiesFileUrl Url of the properties file
     * @throws Exception Instance of IOException if the URL is invalid or instance of Exception of the properties
     * file is malformed.
     */
    private void loadProperties(URL propertiesFileUrl){
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
        ObjectMapper objMapper = new ObjectMapper(jsonFactory);
        try {
            ObjectNode objNode = objMapper.readValue(propertiesFileUrl, ObjectNode.class);
            GLOBAL_PROPERTIES = new GlobalProperties(objNode);
            SERVICE_IDENTIFICATION_PROPERTIES = new ServiceIdentificationProperties(objNode);
            SERVICE_PROVIDER_PROPERTIES = new ServiceProviderProperties(objNode);
            OPERATIONS_METADATA_PROPERTIES = new OperationsMetadataProperties(objNode);
            WSDL_PROPERTIES = new WSDLProperties(objNode);
            CUSTOM_PROPERTIES = new CustomProperties(objNode);
        } catch (IOException e) {
            LOGGER.warn(I18N.tr("Unable to load the wps properties.\n"+e.getMessage()));
            GLOBAL_PROPERTIES = null;
        } catch (Exception e) {
            LOGGER.error(I18N.tr("Unable to load the server configuration.\nCause : {0}\nLoading the " +
                    "default configuration.", e.getMessage()));
            GLOBAL_PROPERTIES = null;
        }
    }

    @Override
    public String getWpsVersion() {
        return "1.0.0";
    }

    /** Global properties of the server */
    public static class GlobalProperties{
        /** Service provided by the server, WPS by default */
        public final String SERVICE;
        /** Version of the server. */
        public final String SERVER_VERSION;
        /** Supported version of the WPS (1.0.0, 2.0.0 ...). */
        public final String[] SUPPORTED_VERSIONS;
        /** Update sequence. */
        public final String UPDATE_SEQUENCE;
        /** Default languages. */
        public final String DEFAULT_LANGUAGE;
        /** Supported languages. */
        public final String[] SUPPORTED_LANGUAGES;
        /** Supported format for the communication with the client. */
        public final String[] SUPPORTED_FORMATS;
        /** Indicates if complex data output(s) from all the processes can be requested to be store by the WPS server
         * as web-accessible resources**/
        public final boolean STORE_SUPPORTED;
        /** Indicates if Execute operation response can be returned quickly with status information */
        public final boolean STATUS_SUPPORTED;

        public GlobalProperties(ObjectNode objNode) throws Exception {
            if(!objNode.has("service")){
                throw new Exception("The property file should contains a field 'service'");
            }
            SERVICE = objNode.get("service").asText();
            if(!objNode.has("service_version")){
                throw new Exception("The property file should contains a field 'service_version'");
            }
            SERVER_VERSION = objNode.get("service_version").asText();
            if(objNode.has("supported_versions")) {
                SUPPORTED_VERSIONS = nodeToArray(objNode.get("supported_versions"));
            }
            else{
                SUPPORTED_VERSIONS = null;
            }
            if(objNode.has("update_sequence")){
                UPDATE_SEQUENCE = objNode.get("update_sequence").asText();
            }
            else{
                UPDATE_SEQUENCE = null;
            }
            if(!objNode.has("supported_languages")){
                throw new Exception("The property file should contains a field 'supported_languages'");
            }
            SUPPORTED_LANGUAGES = nodeToArray(objNode.get("supported_languages"));
            if(!objNode.has("default_language")){
                throw new Exception("The property file should contains a field 'default_language'");
            }
            DEFAULT_LANGUAGE = objNode.get("default_language").asText();
            if(objNode.has("supported_format")) {
                SUPPORTED_FORMATS = nodeToArray(objNode.get("supported_format"));
            }
            else{
                SUPPORTED_FORMATS = null;
            }
            STORE_SUPPORTED = objNode.has("store_supported") && objNode.get("store_supported").asBoolean();
            STATUS_SUPPORTED = objNode.has("status_supported") && objNode.get("status_supported").asBoolean();
        }
    }

    /** Properties associated to the service identification part of the server */
    public static class ServiceIdentificationProperties{
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

        public ServiceIdentificationProperties(ObjectNode objNode) throws Exception {
            if(!objNode.has("service_identification")){
                throw new Exception("The property file should contains a field 'service_identification'");
            }
            JsonNode jsonNode = objNode.get("service_identification");
            // Sets the service type property
            SERVICE_TYPE = new CodeType();
            if(!jsonNode.has("service_type")){
                throw new Exception("The property file should contains a field 'service_type'");
            }
            SERVICE_TYPE.setValue(jsonNode.get("service_type").asText());

            // Sets the service type version which is an array of values.
            if(!jsonNode.has("service_type_version")){
                throw new Exception("The property file should contains a field 'service_type_version'");
            }
            SERVICE_TYPE_VERSIONS = nodeToArray(jsonNode.get("service_type_version"));

            // Sets the service type version which is an array of values.
            if(jsonNode.has("profile")) {
                PROFILE = nodeToArray(jsonNode.get("profile"));
            }
            else{
                PROFILE = null;
            }

            // Sets the title which is an array of LanguageStringType.
            if(!jsonNode.has("title")){
                throw new Exception("The property file should contains a field 'title'");
            }
            JsonNode titles = jsonNode.get("title");
            TITLE = new LanguageStringType[titles.size()];
            if(titles.size() == 0){
                throw new Exception("The 'title' property should ne be empty");
            }
            for(int i=0; i<titles.size(); i++){
                LanguageStringType type = new LanguageStringType();
                if(!titles.get(i).has("value")){
                    throw new Exception("A language string should contains a field 'value'");
                }
                type.setValue(titles.get(i).get("value").asText());
                if(!titles.get(i).has("lang")){
                    throw new Exception("A language string should contains a field 'lang'");
                }
                type.setLang(titles.get(i).get("lang").asText());
                TITLE[i] = type;
            }

            //Sets the abstract which, like the title, is composed of an array of LanguageStringType.
            if(jsonNode.has("abstract")) {
                JsonNode abstracts = jsonNode.get("abstract");
                ABSTRACT = new LanguageStringType[abstracts.size()];
                for (int i = 0; i < abstracts.size(); i++) {
                    LanguageStringType type = new LanguageStringType();
                    if(!abstracts.get(i).has("value")){
                        throw new Exception("A language string should contains a field 'value'");
                    }
                    type.setValue(abstracts.get(i).get("value").asText());
                    if(!abstracts.get(i).has("lang")){
                        throw new Exception("A language string should contains a field 'lang'");
                    }
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
                    if(!keywords.get(i).has("keyword")){
                        throw new Exception("A keywords should contains a field 'keyword'");
                    }
                    JsonNode keyword = keywords.get(i).get("keyword");
                    KEYWORDS[i] = new KeywordsType();
                    for (int j = 0; j < keyword.size(); j++) {
                        LanguageStringType type = new LanguageStringType();
                        if(!keyword.get(j).has("value")){
                            throw new Exception("A language string should contains a field 'value'");
                        }
                        type.setValue(keyword.get(j).get("value").asText());
                        if(!keyword.get(j).has("lang")){
                            throw new Exception("A language string should contains a field 'lang'");
                        }
                        type.setLang(keyword.get(j).get("lang").asText());
                        KEYWORDS[i].getKeyword().add(type);
                    }
                }
            }
            else{
                KEYWORDS = null;
            }

            if(jsonNode.has("fees")) {
                FEES = jsonNode.get("fees").asText();
            }
            else{
                FEES = null;
            }

            if(jsonNode.has("access_constraints")) {
                ACCESS_CONSTRAINTS = nodeToArray(jsonNode.get("access_constraints"));
            }
            else{
                ACCESS_CONSTRAINTS = null;
            }
        }
    }

    /** Properties associated to the service provider part of the server */
    public static class ServiceProviderProperties{
        /** Service provider name. */
        public final String PROVIDER_NAME;
        /** Reference to the most relevant web site of the service provider. */
        public final OnlineResourceType PROVIDER_SITE;
        /** Information for contacting the service provider. */
        public final ResponsiblePartySubsetType SERVICE_CONTACT;

        public ServiceProviderProperties(ObjectNode objNode) throws Exception {

            if(!objNode.has("service_provider")){
                throw new Exception("The property file should contains a field 'service_provider'");
            }
            JsonNode jsonObj = objNode.get("service_provider");

            if(!jsonObj.has("provider_name")){
                throw new Exception("The property file should contains a field 'provider_name'");
            }
            PROVIDER_NAME = jsonObj.get("provider_name").asText();


            if(jsonObj.has("provider_site")) {
                JsonNode obj = jsonObj.get("provider_site");
                PROVIDER_SITE = new OnlineResourceType();
                if(obj.has("href")) {
                    PROVIDER_SITE.setHref(obj.get("href").asText());
                }
                if(obj.has("role")) {
                    PROVIDER_SITE.setRole(obj.get("role").asText());
                }
                if(obj.has("arcrole")) {
                    PROVIDER_SITE.setArcrole(obj.get("arcrole").asText());
                }
                if(obj.has("title")) {
                    PROVIDER_SITE.setTitle(obj.get("title").asText());
                }
                if(obj.has("actuate")) {
                    String str = obj.get("actuate").asText();
                    if (str != null) {
                        PROVIDER_SITE.setActuate(ActuateType.fromValue(str.toLowerCase()));
                    }
                }
                if(obj.has("show")) {
                    String str = obj.get("show").asText();
                    if (str != null) {
                        PROVIDER_SITE.setShow(ShowType.fromValue(str.toLowerCase()));
                    }
                }
            }
            else{
                PROVIDER_SITE = null;
            }

            if(jsonObj.has("service_contact")) {
                JsonNode obj = jsonObj.get("service_contact");
                SERVICE_CONTACT = new ResponsiblePartySubsetType();
                if(obj.has("individual_name")) {
                    SERVICE_CONTACT.setIndividualName(obj.get("individual_name").asText());
                }
                if(obj.has("position_name")) {
                    SERVICE_CONTACT.setPositionName(obj.get("position_name").asText());
                }
                if(obj.has("contact_info")) {
                    JsonNode objContact = obj.get("contact_info");
                    ContactType contactType = new ContactType();
                    if (objContact.has("phone")) {
                        JsonNode objPhone = objContact.get("phone");
                        TelephoneType telephoneType = new TelephoneType();
                        if(objPhone.has("voice")) {
                            JsonNode voiceArray = objPhone.get("voice");
                            if(voiceArray != null){
                                for(int i=0; i<voiceArray.size(); i++) {
                                    telephoneType.getVoice().add(voiceArray.get(i).asText());
                                }
                            }
                        }
                        if(objPhone.has("facsimile")) {
                            JsonNode facsimArray = objPhone.get("facsimile");
                            if (facsimArray != null) {
                                for (int i = 0; i < facsimArray.size(); i++) {
                                    telephoneType.getFacsimile().add(facsimArray.get(i).asText());
                                }
                            }
                        }
                        contactType.setPhone(telephoneType);
                    }
                    if(objContact.has("address")) {
                        JsonNode objAddress = objContact.get("address");
                        AddressType addressType = new AddressType();
                        if(objAddress.has("delivery_point")) {
                            JsonNode deliveryArray = objAddress.get("delivery_point");
                            if (deliveryArray != null) {
                                for (int i = 0; i < deliveryArray.size(); i++) {
                                    addressType.getDeliveryPoint().add(deliveryArray.get(i).asText());
                                }
                            }
                        }
                        if(objAddress.has("city")) {
                            addressType.setCity(objAddress.get("city").asText());
                        }
                        if(objAddress.has("administrative_area")) {
                            addressType.setAdministrativeArea(objAddress.get("administrative_area").asText());
                        }
                        if(objAddress.has("postal_code")) {
                            addressType.setPostalCode(objAddress.get("postal_code").asText());
                        }
                        if(objAddress.has("country")) {
                            addressType.setCountry(objAddress.get("country").asText());
                        }
                        if(objAddress.has("emails")) {
                            JsonNode emailArray = objAddress.get("emails");
                            if (emailArray != null) {
                                for (int i = 0; i < emailArray.size(); i++) {
                                    addressType.getElectronicMailAddress().add(emailArray.get(i).asText());
                                }
                            }
                        }
                        contactType.setAddress(addressType);
                    }
                    if(objContact.has("online_resource")) {
                        OnlineResourceType onlineResourceType = new OnlineResourceType();
                        JsonNode onlineObj = objContact.get("online_resource");
                        setOnlineResourceType(onlineObj, onlineResourceType);
                        contactType.setOnlineResource(onlineResourceType);
                    }
                    if(objContact.has("hours_of_service")) {
                        contactType.setHoursOfService(objContact.get("hours_of_service").asText());
                    }
                    if(objContact.has("instructions")) {
                        contactType.setContactInstructions(objContact.get("instructions").asText());
                    }
                    SERVICE_CONTACT.setContactInfo(contactType);
                }
                if(obj.has("role")) {
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
    public static class OperationsMetadataProperties{
        /** Operation list. */
        public final List<Operation> OPERATIONS;
        /**List of valid parameter domain. */
        public final List<DomainType> PARAMETERS;
        /**List of valid constraint domain. */
        public final List<DomainType> CONSTRAINTS;
        /**Extended capabilities. */
        public final Object EXTENDED_CAPABILITIES;

        public OperationsMetadataProperties(ObjectNode objNode) throws Exception {

            if(!objNode.has("operation_metadata")){
                throw new Exception("The property file should contains a field 'operation_metadata'");
            }
            JsonNode jsonNode = objNode.get("operation_metadata");
            OPERATIONS = new ArrayList<>();
            if(!jsonNode.has("operation")){
                throw new Exception("The operation metadata should contains a field 'operation'");
            }
            for(JsonNode node : jsonNode.get("operation")){
                Operation op = new Operation();
                if(!node.has("name")){
                    throw new Exception("The operation should contains a field 'name'");
                }
                op.setName(node.get("name").asText());
                if(!node.has("dcp")){
                    throw new Exception("The operation "+op.getName()+" should contains a field 'dcp'");
                }
                for(JsonNode dcpNode : node.get("dcp")) {
                    DCP dcp = new DCP();
                    HTTP http = new HTTP();
                    if(!dcpNode.has("http")){
                        throw new Exception("The operation "+op.getName()+" should contains a field 'http'");
                    }
                    JsonNode httpNode = dcpNode.get("http");
                    if(!httpNode.has("get_or_post")){
                        throw new Exception("The operation "+op.getName()+" should contains a field 'get_or_post'");
                    }
                    for(JsonNode getNode : httpNode.get("get_or_post")){
                        http.getGetOrPost().add(new ObjectFactory().createHTTPGet(getRequestMethodType(getNode)));
                    }
                    dcp.setHTTP(http);
                    op.getDCP().add(dcp);
                }
                if(node.has("parameter")) {
                    for (JsonNode paramNode : node.get("parameter")) {
                        op.getParameter().add(getDomainType(paramNode));
                    }
                }
                if(node.has("constraint")) {
                    for (JsonNode cstrNode : node.get("constraint")) {
                        op.getConstraint().add(getDomainType(cstrNode));
                    }
                }
                if(node.has("metadata")) {
                    for (JsonNode metaNode : node.get("metadata")) {
                        op.getMetadata().add(getMetaDataType(metaNode));
                    }
                }
                OPERATIONS.add(op);
            }
            if(jsonNode.has("parameter")) {
                PARAMETERS = new ArrayList<>();
                for (JsonNode paramNode : jsonNode.get("parameter")) {
                    PARAMETERS.add(getDomainType(paramNode));
                }
            }
            else{
                PARAMETERS = null;
            }
            if(jsonNode.has("constraint")) {
                CONSTRAINTS = new ArrayList<>();
                for (JsonNode cstrNode : jsonNode.get("constraint")) {
                   CONSTRAINTS.add(getDomainType(cstrNode));
                }
            }
            else{
                CONSTRAINTS = null;
            }
            if(jsonNode.has("extended_capabilities")) {
                if(jsonNode.get("extended_capabilities").isTextual()) {
                    EXTENDED_CAPABILITIES = jsonNode.get("extended_capabilities").asText();
                }
                else if(jsonNode.get("extended_capabilities").isNumber()) {
                    EXTENDED_CAPABILITIES = jsonNode.get("extended_capabilities").asDouble();
                }
                else{
                    EXTENDED_CAPABILITIES = null;
                }
            }
            else{
                EXTENDED_CAPABILITIES = null;
            }
        }
    }

    /**
     * Class containing WSDL properties which are not defined in the WPS standard.
     */
    public static class WSDLProperties{

        public final String HREF;

        public WSDLProperties(JsonNode jsonNode) throws Exception {
            if(jsonNode.has("wsdl")) {
                JsonNode customNode = jsonNode.get("wsdl");
                if(!customNode.has("href")){
                    throw new Exception("The WSDL should contains a field 'dcp'");
                }
                HREF = customNode.get("href").asText();
            }
            else{
                HREF = null;
            }
        }
    }

    /**
     * Class containing custom properties which are not defined in the WPS standard.
     */
    public static class CustomProperties{
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
        public final String MAXIMUM_MEGABYTES;
        public final String DEFAULT_JDBCTABLE_FORMAT;
        public final String[] AVAILABLE_JDBCTABLE_FORMAT;
        public final String WORKSPACE_PATH;

        /**
         * Properties which are not defined in the WPS standard.
         * @param jsonNode JsonNode.
         */
        public CustomProperties(JsonNode jsonNode) throws Exception {
            if(!jsonNode.has("custom_properties")){
                throw new Exception("The property file should contains a field 'custom_properties'");
            }
            JsonNode customNode = jsonNode.get("custom_properties");
            if(!customNode.has("destroy_duration")){
                throw new Exception("The property file should contains a field 'destroy_duration'");
            }
            destroyDelay = customNode.get("destroy_duration").asText();
            if(!customNode.has("base_process_polling_delay")){
                throw new Exception("The property file should contains a field 'base_process_polling_delay'");
            }
            BASE_PROCESS_POLLING_DELAY = customNode.get("base_process_polling_delay").asLong();
            if(!customNode.has("max_process_polling_delay")){
                throw new Exception("The property file should contains a field 'max_process_polling_delay'");
            }
            MAX_PROCESS_POLLING_DELAY = customNode.get("max_process_polling_delay").asLong();
            if(!customNode.has("maximum_megabytes")){
                throw new Exception("The property file should contains a field 'maximum_megabytes'");
            }
            MAXIMUM_MEGABYTES = customNode.get("maximum_megabytes").asText();
            if(!customNode.has("default_jdbctable_format")){
                DEFAULT_JDBCTABLE_FORMAT = null;
            }
            else {
                DEFAULT_JDBCTABLE_FORMAT = customNode.get("default_jdbctable_format").asText();
            }
            if(!customNode.has("available_jdbctable_format")){
                AVAILABLE_JDBCTABLE_FORMAT = null;
            }
            else {
                AVAILABLE_JDBCTABLE_FORMAT = nodeToArray(customNode.get("available_jdbctable_format"));
            }
            if(customNode.has("workspace_path")){
                WORKSPACE_PATH = customNode.get("workspace_path").asText();
            }
            else{
                WORKSPACE_PATH = System.getProperty("user.dir");
            }
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
     * Parse a JsonNode in order to build a DomainType object.
     * @param jsonNode JsonNode to parse.
     * @return A DomainType object.
     * @throws Exception Exception thrown if the JsonNode doesn't contains all the properties to build the DomainType.
     */
    private static DomainType getDomainType(JsonNode jsonNode) throws Exception {
        DomainType domainType = new DomainType();
        if(!jsonNode.has("name")){
            throw new Exception("The DomainType should contains a field 'name'");
        }
        domainType.setName(jsonNode.get("name").asText());
        if(!jsonNode.has("possible_value")){
            throw new Exception("The DomainType "+domainType.getName()+" should contains a field 'possible_value'");
        }
        JsonNode possNode = jsonNode.get("possible_value");
        if(possNode.has("allowed_values")){
            JsonNode allowNode = possNode.get("allowed_values");
            AllowedValues allowedValues = new AllowedValues();
            if(allowNode.has("value")){
                for(JsonNode valNode : allowNode.get("value")){
                    ValueType valueType = new ValueType();
                    valueType.setValue(valNode.asText());
                    allowedValues.getValueOrRange().add(valueType);
                }
            }
            if(allowNode.has("range")){
                for(JsonNode rangeNode : allowNode.get("range")){
                    RangeType rangeType = new RangeType();
                    if(rangeNode.has("minimum_value")) {
                        ValueType valueType = new ValueType();
                        valueType.setValue(rangeNode.get("minimum_value").asText());
                        rangeType.setMinimumValue(valueType);
                    }
                    if(rangeNode.has("maximum_value")) {
                        ValueType valueType = new ValueType();
                        valueType.setValue(rangeNode.get("maximum_value").asText());
                        rangeType.setMaximumValue(valueType);
                    }
                    if(rangeNode.has("spacing")) {
                        ValueType valueType = new ValueType();
                        valueType.setValue(rangeNode.get("spacing").asText());
                        rangeType.setSpacing(valueType);
                    }
                    if(rangeNode.has("range_closure")) {
                        rangeType.getRangeClosure().add(rangeNode.get("range_closure").asText());
                    }
                    allowedValues.getValueOrRange().add(rangeType);
                }
            }
            domainType.setAllowedValues(allowedValues);
        }
        else if(possNode.has("any_value")){
            domainType.setAnyValue(new AnyValue());
        }
        else if(possNode.has("no_values")){
            domainType.setNoValues(new NoValues());
        }
        else if(possNode.has("values_reference")){
            JsonNode valNode = possNode.get("values_reference");
            ValuesReference valuesReference = new ValuesReference();
            if(!valNode.has("name")){
                throw new Exception("The values_reference of "+domainType.getName()+" should contains a field 'name'");
            }
            valuesReference.setValue(valNode.get("name").asText());
            if(valNode.has("reference")){
                valuesReference.setReference(valNode.get("reference").asText());
            }
            domainType.setValuesReference(valuesReference);
        }
        else{
            throw new Exception("The DomainType "+domainType.getName()+" should have a 'possibleValue' set.");
        }
        if(jsonNode.has("default_value")){
            ValueType valueType = new ValueType();
            valueType.setValue(jsonNode.get("default_value").asText());
            domainType.setDefaultValue(valueType);
        }
        if(jsonNode.has("meaning")){
            JsonNode meanNode = jsonNode.get("meaning");
            DomainMetadataType domainMetadataType = new DomainMetadataType();
            if(!meanNode.has("name")){
                throw new Exception("The meaning of "+domainType.getName()+" should contains a field 'name'");
            }
            domainMetadataType.setValue(meanNode.get("name").asText());
            if(meanNode.has("reference")) {
                domainMetadataType.setReference(meanNode.get("reference").asText());
            }
            domainType.setMeaning(domainMetadataType);
        }
        if(jsonNode.has("data_type")){
            JsonNode dataNode = jsonNode.get("data_type");
            DomainMetadataType domainMetadataType = new DomainMetadataType();
            if(!dataNode.has("name")){
                throw new Exception("The data_type of "+domainType.getName()+" should contains a field 'name'");
            }
            domainMetadataType.setValue(dataNode.get("name").asText());
            if(dataNode.has("reference")) {
                domainMetadataType.setReference(dataNode.get("reference").asText());
            }
            domainType.setDataType(domainMetadataType);
        }
        if(jsonNode.has("values_unit")) {
            JsonNode unitNode = jsonNode.get("values_unit");
            if(unitNode.has("uom")){
                JsonNode uomNode = unitNode.get("uom");
                DomainMetadataType domainMetadataType = new DomainMetadataType();
                if(!uomNode.has("name")){
                    throw new Exception("The uom of "+domainType.getName()+" should contains a field 'name'");
                }
                domainMetadataType.setValue(uomNode.get("name").asText());
                if(uomNode.has("reference")) {
                    domainMetadataType.setReference(uomNode.get("reference").asText());
                }
                domainType.setUOM(domainMetadataType);
            }
            else if(unitNode.has("reference_system")){
                JsonNode systemNode = unitNode.get("reference_system");
                DomainMetadataType domainMetadataType = new DomainMetadataType();
                if(!systemNode.has("name")){
                    throw new Exception("The reference system of "+domainType.getName()+" should contains a field 'name'");
                }
                domainMetadataType.setValue(systemNode.get("name").asText());
                if(systemNode.has("reference")) {
                    domainMetadataType.setReference(systemNode.get("reference").asText());
                }
                domainType.setReferenceSystem(domainMetadataType);
            }
            else{
                throw new Exception("The values unit should have a UOM or a reference system set.");
            }
        }
        if(jsonNode.has("metadata")) {
            for(JsonNode metadataObj : jsonNode.get("metadata")){
                MetadataType metadataType = new MetadataType();
                if(metadataObj.has("href")){
                    metadataType.setHref(metadataObj.get("href").asText());
                }
                if(metadataObj.has("role")){
                    metadataType.setRole(metadataObj.get("role").asText());
                }
                if(metadataObj.has("arcrole")){
                    metadataType.setArcrole(metadataObj.get("arcrole").asText());
                }
                if(metadataObj.has("title")){
                    metadataType.setTitle(metadataObj.get("title").asText());
                }
                if(metadataObj.has("about")){
                    metadataType.setAbout(metadataObj.get("about").asText());
                }
                if(metadataObj.has("show")){
                    metadataType.setShow(ShowType.fromValue(metadataObj.get("show").asText().toLowerCase()));
                }
                if(metadataObj.has("actuate")){
                    metadataType.setActuate(ActuateType.fromValue(metadataObj.get("actuate").asText().toLowerCase()));
                }
                domainType.getMetadata().add(metadataType);
            }
        }
        return domainType;
    }

    /**
     * Parse a JsonNode in order to build a MetadataType object.
     * @param jsonNode JsonNode to parse.
     * @return A MetadataType object.
     */
    private static MetadataType getMetaDataType(JsonNode jsonNode){
        MetadataType metadataType = new MetadataType();
        if(jsonNode.has("href")){
            metadataType.setHref(jsonNode.get("href").asText());
        }
        if(jsonNode.has("role")){
            metadataType.setRole(jsonNode.get("role").asText());
        }
        if(jsonNode.has("arcrole")){
            metadataType.setArcrole(jsonNode.get("arcrole").asText());
        }
        if(jsonNode.has("title")){
            metadataType.setTitle(jsonNode.get("title").asText());
        }
        if(jsonNode.has("show")){
            metadataType.setShow(ShowType.fromValue(jsonNode.get("show").asText().toLowerCase()));
        }
        if(jsonNode.has("actuate")){
            metadataType.setActuate(ActuateType.fromValue(jsonNode.get("actuate").asText().toLowerCase()));
        }
        if(jsonNode.has("about")) {
            metadataType.setAbout(jsonNode.get("about").asText());
        }
        if(jsonNode.has("abstract_metadata")) {
            metadataType.setAbstractMetaData(jsonNode.get("abstract_metadata").asText());
        }
        return metadataType;
    }

    /**
     * Sets the given OnlineResource with the data of the given JsonNode.
     * @param jsonNode JsonNode to parse.
     * @param onlineResourceType OnlineResource to set.
     */
    private static void setOnlineResourceType(JsonNode jsonNode, OnlineResourceType onlineResourceType){
        if(jsonNode.has("href")){
            onlineResourceType.setHref(jsonNode.get("href").asText());
        }
        if(jsonNode.has("role")){
            onlineResourceType.setRole(jsonNode.get("role").asText());
        }
        if(jsonNode.has("arcrole")){
            onlineResourceType.setArcrole(jsonNode.get("arcrole").asText());
        }
        if(jsonNode.has("title")){
            onlineResourceType.setTitle(jsonNode.get("title").asText());
        }
        if(jsonNode.has("show")){
            onlineResourceType.setShow(ShowType.fromValue(jsonNode.get("show").asText().toLowerCase()));
        }
        if(jsonNode.has("actuate")){
            onlineResourceType.setActuate(ActuateType.fromValue(jsonNode.get("actuate").asText().toLowerCase()));
        }
    }

    /**
     * Parse a JsonNode in order to build a RequestMethodType object.
     * @param jsonNode JsonNode to parse.
     * @return A RequestMethodType object.
     * @throws Exception Exception thrown if the JsonNode doesn't contains all the properties to build the RequestMethodType.
     */
    private static RequestMethodType getRequestMethodType(JsonNode jsonNode) throws Exception {
        RequestMethodType requestMethodType = new RequestMethodType();
        if(!jsonNode.has("url")){
            throw new Exception("The MethodType should contains a field 'url'");
        }
        setOnlineResourceType(jsonNode.get("url"), requestMethodType);
        if(jsonNode.has("constraint")) {
            for (JsonNode cstrNode : jsonNode.get("constraint")) {
                requestMethodType.getConstraint().add(getDomainType(cstrNode));
            }
        }
        return requestMethodType;
    }

    /**
     * Convert an JsonNode to a String array
     * @param jsonNode JsonNode to convert
     * @return String array
     */
    private static String[] nodeToArray(JsonNode jsonNode) {
        List<String> list = new ArrayList<>();
        for(JsonNode node : jsonNode){
            list.add(node.asText());
        }
        return list.toArray(new String[]{});
    }
}
