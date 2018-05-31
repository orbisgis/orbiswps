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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.opengis.ows._1.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3._1999.xlink.ActuateType;
import org.w3._1999.xlink.ShowType;

import javax.xml.bind.JAXBElement;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Test the class WpsServiceProperty_1_0_0.
 * The class loads an full and a minimal example of a json configuration file and test each property to check the
 * expected value
 *
 * @author Sylvain PALOMINOS
 */
public class TestWpsServiceProperty_1_0_0 {

    private static WpsServerProperties_1_0_0 fullProps;

    private static WpsServerProperties_1_0_0 minProps;

    private static JsonNodeFactory jsonFactory = new JsonNodeFactory(true);

    @BeforeClass
    public static void init() {
        fullProps = new WpsServerProperties_1_0_0(
                TestWpsServiceProperty_1_0_0.class.getResource("fullWpsService100.json").getFile());
        minProps = new WpsServerProperties_1_0_0(
                TestWpsServiceProperty_1_0_0.class.getResource("minWpsService100.json").getFile());
    }

    /**
     * Tests the constructors.
     * @throws IOException
     */
    @Test
    public void testConstruction() throws IOException {
        //Test with property file which doesn't exists
        File f = new File("file.json");
        f.deleteOnExit();
        f.createNewFile();
        WpsServerProperties_1_0_0 test1 = new WpsServerProperties_1_0_0("./file.json");
        assertNotNull("The 'GLOBAL_PROPERTIES' object should not be null", test1.GLOBAL_PROPERTIES);
        assertNotNull("The 'CUSTOM_PROPERTIES' object should not be null", test1.CUSTOM_PROPERTIES);
        assertNotNull("The 'SERVICE_IDENTIFICATION_PROPERTIES' object should not be null", test1.SERVICE_IDENTIFICATION_PROPERTIES);
        assertNotNull("The 'OPERATIONS_METADATA_PROPERTIES' object should not be null", test1.OPERATIONS_METADATA_PROPERTIES);
        assertNotNull("The 'SERVICE_PROVIDER_PROPERTIES' object should not be null", test1.SERVICE_PROVIDER_PROPERTIES);
        assertNotNull("The 'WSDL_PROPERTIES' object should not be null", test1.WSDL_PROPERTIES);

        WpsServerProperties_1_0_0 test2 = new WpsServerProperties_1_0_0();
        assertNotNull("The 'GLOBAL_PROPERTIES' object should not be null", test2.GLOBAL_PROPERTIES);
        assertNotNull("The 'CUSTOM_PROPERTIES' object should not be null", test2.CUSTOM_PROPERTIES);
        assertNotNull("The 'SERVICE_IDENTIFICATION_PROPERTIES' object should not be null", test2.SERVICE_IDENTIFICATION_PROPERTIES);
        assertNotNull("The 'OPERATIONS_METADATA_PROPERTIES' object should not be null", test2.OPERATIONS_METADATA_PROPERTIES);
        assertNotNull("The 'SERVICE_PROVIDER_PROPERTIES' object should not be null", test2.SERVICE_PROVIDER_PROPERTIES);
        assertNotNull("The 'WSDL_PROPERTIES' object should not be null", test2.WSDL_PROPERTIES);
    }

    /**
     * Test that the WPS version is set to '1.0.0'
     */
    @Test
    public void testWpsVersion(){
        assertEquals("The WPS version should be '1.0.0'", "1.0.0", fullProps.getWpsVersion());
        assertEquals("The WPS version should be '1.0.0'", "1.0.0", minProps.getWpsVersion());
    }

    ////
    //Test bad property object
    ////

    /**
     * Test that GlobalProperties constructor returns an Exception if the ObjectNode doesn't contains the mandatory
     * values.
     */
    @Test
    public void testBadGlobalProperties() {
        ObjectNode obj = new ObjectNode(jsonFactory);
        assertExceptionOnGlobalProperties(obj);
        obj.put("service", "service");
        assertExceptionOnGlobalProperties(obj);
        obj.put("service_version", "service_version");
        assertExceptionOnGlobalProperties(obj);
        obj.put("supported_languages", "supported_languages");
        assertExceptionOnGlobalProperties(obj);
        obj.put("default_language", "default_language");
        try {
            new WpsServerProperties_1_0_0.GlobalProperties(obj);
        } catch (Exception ignored) {
            fail("The exception should have not been thrown");
        }
    }

    /**
     * Test if the construction of 'GlobalProperties' throw an error.
     * @param objectNode ObjectNode to parse to construct the 'GlobalProperties'.
     */
    private void assertExceptionOnGlobalProperties(ObjectNode objectNode){
        try {
            new WpsServerProperties_1_0_0.GlobalProperties(objectNode);
            fail("An exception should have been thrown");
        } catch (Exception ignore) {}
    }

    /**
     * Test that ServiceIdentification constructor returns an Exception if the ObjectNode doesn't contains the mandatory
     * values.
     */
    @Test
    public void testBadServiceIdentification() {
        ObjectNode obj = new ObjectNode(jsonFactory);
        assertExceptionOnServiceIdentification(obj);
        ObjectNode idNode = new ObjectNode(jsonFactory);
        obj.put("service_identification", idNode);
        assertExceptionOnServiceIdentification(obj);
        idNode.put("service_type", "service_type");
        assertExceptionOnServiceIdentification(obj);
        idNode.put("service_type_version", "service_type_version");
        assertExceptionOnServiceIdentification(obj);

        ArrayNode arrayTitle = new ArrayNode(jsonFactory);
        idNode.put("title", arrayTitle);
        assertExceptionOnServiceIdentification(obj);
        ObjectNode nodeTitle = new ObjectNode(jsonFactory);
        arrayTitle.add(nodeTitle);
        assertExceptionOnServiceIdentification(obj);
        nodeTitle.put("value", "value");
        assertExceptionOnServiceIdentification(obj);
        nodeTitle.put("lang", "lang");
        try {
            new WpsServerProperties_1_0_0.ServiceIdentificationProperties(obj);
        } catch (Exception ignored) {
            fail("The exception should have not been thrown");
        }

        ArrayNode arrayAbst = new ArrayNode(jsonFactory);
        idNode.put("abstract", arrayAbst);
        ObjectNode nodeAbst = new ObjectNode(jsonFactory);
        arrayAbst.add(nodeAbst);
        assertExceptionOnServiceIdentification(obj);
        nodeAbst.put("value", "value");
        assertExceptionOnServiceIdentification(obj);
        nodeAbst.put("lang", "lang");
        try {
            new WpsServerProperties_1_0_0.ServiceIdentificationProperties(obj);
        } catch (Exception ignored) {
            fail("The exception should have not been thrown");
        }

        ArrayNode arrayKey = new ArrayNode(jsonFactory);
        idNode.put("keywords", arrayKey);
        ObjectNode nodeKey = new ObjectNode(jsonFactory);
        arrayKey.add(nodeKey);
        assertExceptionOnServiceIdentification(obj);
        ArrayNode arrayK = new ArrayNode(jsonFactory);
        nodeKey.put("keyword", arrayK);
        ObjectNode nodeK = new ObjectNode(jsonFactory);
        arrayK.add(nodeK);
        assertExceptionOnServiceIdentification(obj);
        nodeK.put("value", "value");
        assertExceptionOnServiceIdentification(obj);
        nodeK.put("lang", "lang");
        try {
            new WpsServerProperties_1_0_0.ServiceIdentificationProperties(obj);
        } catch (Exception ignored) {
            fail("The exception should have not been thrown");
        }
    }

    /**
     * Test if the construction of 'ServiceIdentification' throw an error.
     * @param objectNode ObjectNode to parse to construct the 'ServiceIdentification'.
     */
    private void assertExceptionOnServiceIdentification(ObjectNode objectNode){
        try {
            new WpsServerProperties_1_0_0.ServiceIdentificationProperties(objectNode);
            fail("An exception should have been thrown");
        } catch (Exception ignore) {}
    }

    /**
     * Test that ServiceProvider constructor returns an Exception if the ObjectNode doesn't contains the mandatory
     * values.
     */
    @Test
    public void testBadServiceProviderProperties() {
        ObjectNode obj = new ObjectNode(jsonFactory);
        assertExceptionOnServiceProviderProperties(obj);
        ObjectNode providerNode = new ObjectNode(jsonFactory);
        obj.put("service_provider", providerNode);
        assertExceptionOnServiceProviderProperties(obj);
        providerNode.put("provider_name", "provider_name");
        try {
            new WpsServerProperties_1_0_0.ServiceProviderProperties(obj);
        } catch (Exception ignored) {
            fail("The exception should have not been thrown");
        }
    }

    /**
     * Test if the construction of 'ServiceProviderProperties' throw an error.
     * @param objectNode ObjectNode to parse to construct the 'ServiceProviderProperties'.
     */
    private void assertExceptionOnServiceProviderProperties(ObjectNode objectNode){
        try {
            new WpsServerProperties_1_0_0.ServiceProviderProperties(objectNode);
            fail("An exception should have been thrown");
        } catch (Exception ignore) {}
    }

    /**
     * Test that OperationsMetadata constructor returns an Exception if the ObjectNode doesn't contains the mandatory
     * values.
     */
    @Test
    public void testBadOperationsMetadataProperties() {
        ObjectNode obj = new ObjectNode(jsonFactory);
        assertExceptionOnOperationsMetadataProperties(obj);
        ObjectNode metaNode = new ObjectNode(jsonFactory);
        obj.put("operation_metadata", metaNode);
        assertExceptionOnOperationsMetadataProperties(obj);

        ArrayNode operationArray = new ArrayNode(jsonFactory);
        ObjectNode operationNode = new ObjectNode(jsonFactory);
        operationArray.add(operationNode);
        metaNode.put("operation", operationArray);
        assertExceptionOnOperationsMetadataProperties(obj);
        operationNode.put("name", "name");
        assertExceptionOnOperationsMetadataProperties(obj);
        ArrayNode dcpArray = new ArrayNode(jsonFactory);
        ObjectNode dcpNode = new ObjectNode(jsonFactory);
        dcpArray.add(dcpNode);
        operationNode.put("dcp", dcpArray);
        assertExceptionOnOperationsMetadataProperties(obj);
        ObjectNode httpNode = new ObjectNode(jsonFactory);
        dcpNode.put("http", httpNode);
        assertExceptionOnOperationsMetadataProperties(obj);
        ArrayNode getArray = new ArrayNode(jsonFactory);
        httpNode.put("get_or_post", getArray);
        ObjectNode getNode = new ObjectNode(jsonFactory);
        getArray.add(getNode);
        assertExceptionOnOperationsMetadataProperties(obj);
        getNode.put("url", "url");

        metaNode.put("extended_capabilities", "txt");
        try {
            new WpsServerProperties_1_0_0.OperationsMetadataProperties(obj);
        } catch (Exception ignored) {
            fail("The exception should have not been thrown");
        }

        metaNode.remove("extended_capabilities");
        metaNode.put("extended_capabilities", 0.0);
        try {
            new WpsServerProperties_1_0_0.OperationsMetadataProperties(obj);
        } catch (Exception ignored) {
            fail("The exception should have not been thrown");
        }
    }

    /**
     * Test if the construction of 'OperationsMetadataProperties' throw an error.
     * @param objectNode ObjectNode to parse to construct the 'OperationsMetadataProperties'.
     */
    private void assertExceptionOnOperationsMetadataProperties(ObjectNode objectNode){
        try {
            new WpsServerProperties_1_0_0.OperationsMetadataProperties(objectNode);
            fail("An exception should have been thrown");
        } catch (Exception ignore) {}
    }

    /**
     * Test that WSDLProperties constructor returns an Exception if the ObjectNode doesn't contains the mandatory
     * values.
     */
    @Test
    public void testBadWSDLProperties() {
        ObjectNode obj = new ObjectNode(jsonFactory);
        ObjectNode wsdlNode = new ObjectNode(jsonFactory);
        obj.put("wsdl", wsdlNode);
        assertExceptionOnWSDLProperties(obj);
        wsdlNode.put("href", "href");
        try {
            new WpsServerProperties_1_0_0.WSDLProperties(obj);
        } catch (Exception ignored) {
            fail("The exception should have not been thrown");
        }
    }

    /**
     * Test if the construction of 'WSDLProperties' throw an error.
     * @param objectNode ObjectNode to parse to construct the 'WSDLProperties'.
     */
    private void assertExceptionOnWSDLProperties(ObjectNode objectNode){
        try {
            new WpsServerProperties_1_0_0.WSDLProperties(objectNode);
            fail("An exception should have been thrown");
        } catch (Exception ignore) {}
    }

    /**
     * Test that CustomProperties constructor returns an Exception if the ObjectNode doesn't contains the mandatory
     * values.
     */
    @Test
    public void testBadCustomProperties() {
        ObjectNode obj = new ObjectNode(jsonFactory);
        ObjectNode customNode = new ObjectNode(jsonFactory);
        assertExceptionOnCustomProperties(obj);
        obj.put("custom_properties", customNode);
        assertExceptionOnCustomProperties(obj);
        customNode.put("destroy_duration", "0Y5D3H45M30S");
        assertExceptionOnCustomProperties(obj);
        customNode.put("base_process_polling_delay", 75);
        assertExceptionOnCustomProperties(obj);
        customNode.put("max_process_polling_delay", 500);
        assertExceptionOnCustomProperties(obj);
        customNode.put("maximum_megabytes", "maximum_megabytes");
        try {
            assertEquals("The destroy delay in millis should be '445530000'", 445530000, new WpsServerProperties_1_0_0.CustomProperties(obj).getDestroyDelayInMillis());
        } catch (Exception ignored) {
            fail("The exception should have not been thrown");
        }
    }

    /**
     * Test if the construction of 'CustomProperties' throw an error.
     * @param objectNode ObjectNode to parse to construct the 'CustomProperties'.
     */
    private void assertExceptionOnCustomProperties(ObjectNode objectNode){
        try {
            new WpsServerProperties_1_0_0.CustomProperties(objectNode);
            fail("An exception should have been thrown");
        } catch (Exception ignore) {}
    }

    /**
     * Test that DomainType function returns an Exception if the ObjectNode doesn't contains the mandatory
     * values.
     */
    @Test
    public void testBadDomainType() throws NoSuchMethodException {
        Method method = WpsServerProperties_1_0_0.class.getDeclaredMethod("getDomainType", JsonNode.class);
        method.setAccessible(true);
        ObjectNode obj = new ObjectNode(jsonFactory);
        assertExceptionOnMethod(obj, method);
        obj.put("name", "name");
        assertExceptionOnMethod(obj, method);
        ObjectNode value = new ObjectNode(jsonFactory);
        obj.put("possible_value", value);
        assertExceptionOnMethod(obj, method);
        ObjectNode ref = new ObjectNode(jsonFactory);
        value.put("values_reference", ref);
        assertExceptionOnMethod(obj, method);
        ref.put("name", "name");

        ObjectNode mean = new ObjectNode(jsonFactory);
        obj.put("meaning", mean);
        assertExceptionOnMethod(obj, method);
        mean.put("name", "name");

        ObjectNode type = new ObjectNode(jsonFactory);
        obj.put("data_type", type);
        assertExceptionOnMethod(obj, method);
        type.put("name", "name");

        ObjectNode unit = new ObjectNode(jsonFactory);
        obj.put("values_unit", unit);
        assertExceptionOnMethod(obj, method);
        ObjectNode uom = new ObjectNode(jsonFactory);
        unit.put("uom", uom);
        assertExceptionOnMethod(obj, method);
        unit.remove("uom");
        ObjectNode refe = new ObjectNode(jsonFactory);
        unit.put("reference_system", refe);
        assertExceptionOnMethod(obj, method);
        refe.put("name", "name");
        try {
            //method.invoke(minProps, obj);
        } catch (Exception ignored) {
            fail("The exception should have not been thrown");
        }
    }

    /**
     * Test if the construction of 'DomainType' throw an error.
     * @param objectNode ObjectNode to parse to construct the 'DomainType'.
     */
    private void assertExceptionOnMethod(ObjectNode objectNode, Method m){
        try {
            m.invoke(minProps, objectNode);
            fail("An exception should have been thrown");
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            fail("Unable to call the method "+m.getName());
        }
        catch (Exception ignore) {}
    }

    ////
    //Test of full properties
    ////
    @Test
    public void testFullGlobalProperties(){
        assertNotNull("The property 'GLOBAL_PROPERTIES' should not be null", fullProps.GLOBAL_PROPERTIES);
        assertEquals("The 'SERVICE' property of 'GLOBAL_PROPERTIES' should be 'WPS'",
                "WPS", fullProps.GLOBAL_PROPERTIES.SERVICE);
        assertEquals("The 'SERVER_VERSION' property of 'GLOBAL_PROPERTIES' should be '1.0.0'",
                "1.0.0", fullProps.GLOBAL_PROPERTIES.SERVER_VERSION);
        assertArrayEquals("The 'SUPPORTED_VERSION' property of 'GLOBAL_PROPERTIES' should be set to '[1.0.0]'",
                new String[]{"1.0.0"}, fullProps.GLOBAL_PROPERTIES.SUPPORTED_VERSIONS);
        assertEquals("The 'UPDATE_SEQUENCE' property of 'GLOBAL_PROPERTIES' should be '1.0.0'",
                "1.0.0", fullProps.GLOBAL_PROPERTIES.UPDATE_SEQUENCE);
        assertEquals("The 'DEFAULT_LANGUAGE' property of 'GLOBAL_PROPERTIES' should be 'en'",
                "en", fullProps.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
        assertArrayEquals("The 'SUPPORTED_LANGUAGES' property of 'GLOBAL_PROPERTIES' should be set to '[en, fr-fr]'",
                new String[]{"en", "fr-fr"}, fullProps.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES);
        assertArrayEquals("The 'SUPPORTED_FORMATS' property of 'GLOBAL_PROPERTIES' should be set to '[text/xml]'",
                new String[]{"text/xml"}, fullProps.GLOBAL_PROPERTIES.SUPPORTED_FORMATS);
        assertTrue("The 'STORE_SUPPORTED' property of 'GLOBAL_PROPERTIES' should be 'true'",
                fullProps.GLOBAL_PROPERTIES.STORE_SUPPORTED);
        assertTrue("The 'STATUS_SUPPORTED' property of 'GLOBAL_PROPERTIES' should be 'true'",
                fullProps.GLOBAL_PROPERTIES.STATUS_SUPPORTED);
    }

    @Test
    public void testFullServiceIdentificationProperties(){
        assertNotNull("The property 'SERVICE_IDENTIFICATION_PROPERTIES' should not be null",
                fullProps.SERVICE_IDENTIFICATION_PROPERTIES);
        assertEquals("The 'SERVICE_TYPE' property of 'SERVICE_IDENTIFICATION_PROPERTIES' should be 'WPS'",
                "WPS", fullProps.SERVICE_IDENTIFICATION_PROPERTIES.SERVICE_TYPE.getValue());
        assertArrayEquals("The 'SERVICE_TYPE_VERSIONS' property of 'SERVICE_IDENTIFICATION_PROPERTIES' should be set " +
                        "to '[1.0.0]'",
                new String[]{"1.0.0"}, fullProps.SERVICE_IDENTIFICATION_PROPERTIES.SERVICE_TYPE_VERSIONS);
        assertArrayEquals("The 'PROFILE' property of 'SERVICE_IDENTIFICATION_PROPERTIES' should be set to '[NONE]'",
                new String[]{"NONE"}, fullProps.SERVICE_IDENTIFICATION_PROPERTIES.PROFILE);
        assertNotNull("The property 'TITLE' of 'SERVICE_IDENTIFICATION_PROPERTIES' should not be null",
                fullProps.SERVICE_IDENTIFICATION_PROPERTIES.TITLE);
        for(LanguageStringType languageStringType : fullProps.SERVICE_IDENTIFICATION_PROPERTIES.TITLE){
            if(languageStringType.getLang().equals("en")){
                assertEquals("Wrong value for the 'en' title", "Local WPS Service", languageStringType.getValue());
            }
            else if(languageStringType.getLang().equals("fr-fr")){
                assertEquals("Wrong value for the 'fr-fr' title", "Service WPS locale", languageStringType.getValue());
            }
            else{
                fail("No titles with the language "+languageStringType.getLang());
            }
        }
        assertNotNull("The property 'ABSTRACT' of 'SERVICE_IDENTIFICATION_PROPERTIES' should not be null",
                fullProps.SERVICE_IDENTIFICATION_PROPERTIES.ABSTRACT);
        for(LanguageStringType languageStringType : fullProps.SERVICE_IDENTIFICATION_PROPERTIES.ABSTRACT){
            if(languageStringType.getLang().equals("en")){
                assertEquals("Wrong value for the 'en' title", "A local instance of a WPS Service",
                        languageStringType.getValue());
            }
            else if(languageStringType.getLang().equals("fr-fr")){
                assertEquals("Wrong value for the 'fr-fr' title", "Instance locale d'un service WPS",
                        languageStringType.getValue());
            }
            else{
                fail("No titles with the language "+languageStringType.getLang());
            }
        }
        assertNotNull("The property 'KEYWORDS' of 'SERVICE_IDENTIFICATION_PROPERTIES' should not be null",
                fullProps.SERVICE_IDENTIFICATION_PROPERTIES.KEYWORDS);
        for(KeywordsType keywordsType : fullProps.SERVICE_IDENTIFICATION_PROPERTIES.KEYWORDS){
            for(LanguageStringType languageStringType : keywordsType.getKeyword()) {
                if (languageStringType.getLang().equals("en")) {
                    assertTrue("Wrong value for a 'en' keyword",
                            languageStringType.getValue().equals("Toolbox")||
                                    languageStringType.getValue().equals("WPS service")||
                                    languageStringType.getValue().equals("OrbisGIS"));
                } else if (languageStringType.getLang().equals("fr-fr")) {
                    assertTrue("Wrong value for a 'fr-fr' title",
                            languageStringType.getValue().equals("Toolbox")||
                                    languageStringType.getValue().equals("Service WPS")||
                                    languageStringType.getValue().equals("OrbisGIS"));
                } else {
                    fail("No titles with the language " + languageStringType.getLang());
                }
            }
        }
        assertEquals("The 'FEES' property of 'SERVICE_IDENTIFICATION_PROPERTIES' should be 'NONE'",
                "NONE", fullProps.SERVICE_IDENTIFICATION_PROPERTIES.FEES);
        assertArrayEquals("The 'SERVICE_TYPE_VERSIONS' property of 'SERVICE_IDENTIFICATION_PROPERTIES' should be set " +
                        "to '[NONE]'",
                new String[]{"NONE"}, fullProps.SERVICE_IDENTIFICATION_PROPERTIES.ACCESS_CONSTRAINTS);
    }

    @Test
    public void testFullServiceProviderProperties(){
        assertNotNull("The 'SERVICE_PROVIDER_PROPERTIES' property should not be null'",
                fullProps.SERVICE_PROVIDER_PROPERTIES);
        assertEquals("The 'PROVIDER_NAME' property of 'SERVICE_PROVIDER_PROPERTIES' should be 'OrbisGIS'",
                "OrbisGIS", fullProps.SERVICE_PROVIDER_PROPERTIES.PROVIDER_NAME);

        assertNotNull("The 'PROVIDER_SITE' property of 'SERVICE_PROVIDER_PROPERTIES' should not be null",
                fullProps.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE);
        assertEquals("The 'href' property of 'SERVICE_PROVIDER_PROPERTIES' should be 'http://orbisgis.org/'",
                "http://orbisgis.org/", fullProps.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE.getHref());
        assertEquals("The 'role' property of 'SERVICE_PROVIDER_PROPERTIES' should be 'role'",
                "role", fullProps.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE.getRole());
        assertEquals("The 'arcrole' property of 'SERVICE_PROVIDER_PROPERTIES' should be 'arcrole'",
                "arcrole", fullProps.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE.getArcrole());
        assertEquals("The 'title' property of 'SERVICE_PROVIDER_PROPERTIES' should be 'title'",
                "title", fullProps.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE.getTitle());
        assertEquals("The 'show' property of 'SERVICE_PROVIDER_PROPERTIES' should be 'NONE'",
                ShowType.NONE, fullProps.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE.getShow());
        assertEquals("The 'actuate' property of 'SERVICE_PROVIDER_PROPERTIES' should be 'NONE'",
                ActuateType.NONE, fullProps.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE.getActuate());

        assertNotNull("The 'SERVICE_CONTACT' property of 'SERVICE_PROVIDER_PROPERTIES' should not be null",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT);
        assertTrue("The 'contactInfo' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be set",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.isSetContactInfo());
        assertTrue("The 'phone' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be set",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().isSetPhone());
        assertTrue("The 'voice' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be set",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getPhone().isSetVoice());
        assertArrayEquals("The 'voice' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be set to '[phone1,phone2]'",
                new String[]{"phone1", "phone2"},
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getPhone().getVoice().toArray());
        assertArrayEquals("The 'voice' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be set to '[facsim1,facsim2]'",
                new String[]{"facsim1", "facsim2"},
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getPhone().getFacsimile().toArray());
        assertTrue("The 'address' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be set",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().isSetAddress());
        assertArrayEquals("The 'deliveryPoint' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be set to '[point1,point2]'",
                new String[]{"point1", "point2"},
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getAddress().getDeliveryPoint().toArray());
        assertEquals("The 'city' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'city'", "city",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getAddress().getCity());
        assertEquals("The 'administrativeArea' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'area'", "area",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getAddress().getAdministrativeArea());
        assertEquals("The 'postalCode' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'code'", "code",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getAddress().getPostalCode());
        assertEquals("The 'country' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'country'", "country",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getAddress().getCountry());
        assertArrayEquals("The 'electronicMail' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be set to " +
                        "'[email1,email2]'", new String[]{"email1", "email2"},
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getAddress().getElectronicMailAddress().toArray());
        assertTrue("The 'onlineResource' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be set",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().isSetOnlineResource());
        assertEquals("The 'href' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'href'", "href",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getOnlineResource().getHref());
        assertEquals("The 'role' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'role'", "role",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getOnlineResource().getRole());
        assertEquals("The 'arcrole' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'arcrole'", "arcrole",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getOnlineResource().getArcrole());
        assertEquals("The 'title' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'title'", "title",
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getOnlineResource().getTitle());
        assertEquals("The 'show' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'NONE'", ShowType.NONE,
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getOnlineResource().getShow());
        assertEquals("The 'actuate' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'NONE'", ActuateType.NONE,
                fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getOnlineResource().getActuate());
        assertEquals("The 'hoursOfService' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'hours'",
                "hours", fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getHoursOfService());
        assertEquals("The 'hoursOfService' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'instructions'",
                "instructions", fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getContactInfo().getContactInstructions());
        assertEquals("The 'role' property of 'SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT' should be 'role'",
                "role", fullProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT.getRole().getValue());
    }

    @Test
    public void testFullOperationMetadataProperties(){
        boolean isGetCapabilities = false;
        boolean isDescribeProcess = false;
        boolean isExecute = false;
        for(Operation operation : fullProps.OPERATIONS_METADATA_PROPERTIES.OPERATIONS){
            if(operation.getName().equals("GetCapabilities")){
                assertTrue("The 'DCP' property of 'GetCapabilities' should be set", operation.isSetDCP());
                assertEquals("The 'DCP' property of 'GetCapabilities' should contains only one value", 1,
                        operation.getDCP().size());
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The 'http' property of 'GetCapabilities' should be set", dcp.isSetHTTP());
                    assertTrue("The 'getOrPost' property of 'GetCapabilities' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertTrue("The 'getOrPost' property of 'GetCapabilities' should contains at least one value", 1 <=
                            dcp.getHTTP().getGetOrPost().size());
                    boolean isUrl1 = false;
                    boolean isUrl2 = false;
                    for(JAXBElement<RequestMethodType> element : dcp.getHTTP().getGetOrPost()){
                        if("getcapabilitiesurl1".equals(element.getValue().getHref())) {
                            assertEquals("The 'href' property of 'GetCapabilities.getOrPost' should be set to 'getcapabilitiesurl1'",
                                    "getcapabilitiesurl1", element.getValue().getHref());
                            assertEquals("The 'role' property of 'GetCapabilities.getOrPost' should be set to 'role'",
                                    "role", element.getValue().getRole());
                            assertEquals("The 'arcrole' property of 'GetCapabilities.getOrPost' should be set to 'arcrole'",
                                    "arcrole", element.getValue().getArcrole());
                            assertEquals("The 'title' property of 'GetCapabilities.getOrPost' should be set to 'title'",
                                    "title", element.getValue().getTitle());
                            assertEquals("The 'show' property of 'GetCapabilities.getOrPost' should be set to 'NONE'",
                                    ShowType.NONE, element.getValue().getShow());
                            assertEquals("The 'actuate' property of 'GetCapabilities.getOrPost' should be set to 'NONE'",
                                    ActuateType.NONE, element.getValue().getActuate());

                            assertTrue("The 'constraint' property of 'GetCapabilities.getOrPost' should be set",
                                    element.getValue().isSetConstraint());
                            boolean isCstr1=false;
                            boolean isCstr2=false;
                            boolean isCstr3=false;
                            boolean isCstr4=false;
                            for(DomainType constraint : element.getValue().getConstraint()){
                                if("cstr1".equals(constraint.getName())){
                                    assertEquals("The 'name' property of 'constraint' should be set to 'cstr1'",
                                            "cstr1", constraint.getName());
                                    assertTrue("The 'possibleValue' property should be set to 'allowedValues'",
                                            constraint.isSetAllowedValues() && !constraint.isSetAnyValue() &&
                                                    !constraint.isSetNoValues() && !constraint.isSetValuesReference());
                                    assertTrue("The 'valueOrRange' property should be set",
                                            constraint.getAllowedValues().isSetValueOrRange());
                                    for(Object valueOrRange : constraint.getAllowedValues().getValueOrRange()){
                                        if(valueOrRange instanceof ValueType){
                                            ValueType value = (ValueType)valueOrRange;
                                            if(!value.getValue().equals("value1") && !value.getValue().equals("value2")){
                                                fail("unknown value");
                                            }
                                        }
                                        else if(valueOrRange instanceof RangeType){
                                            RangeType range = (RangeType)valueOrRange;
                                            assertEquals("The 'minimumValue' property of the range should be '0'", "0",
                                                    range.getMinimumValue().getValue());
                                            assertEquals("The 'maximumValue' property of the range should be '10'", "10",
                                                    range.getMaximumValue().getValue());
                                            assertEquals("The 'spacing' property of the range should be '2'", "2",
                                                    range.getSpacing().getValue());
                                            assertTrue("The 'rangeClosure' property of the range should be 'closed'",
                                                    range.getRangeClosure().size() == 1 &&
                                                            range.getRangeClosure().get(0).equals("closed"));
                                        }
                                        else{
                                            fail("Unknown valueOrRange type");
                                        }
                                    }
                                    assertEquals("The 'defaultValue' property of 'constraint' should be set to 'dfltvalue'",
                                            "dfltvalue", constraint.getDefaultValue().getValue());
                                    assertTrue("The 'meaning' property should be set", constraint.isSetMeaning());
                                    assertEquals("The 'name' property of 'meaning' should be set to 'name'",
                                            "name", constraint.getMeaning().getValue());
                                    assertEquals("The 'reference' property of 'meaning' should be set to 'uri'",
                                            "uri", constraint.getMeaning().getReference());

                                    assertTrue("The 'dataType' property should be set", constraint.isSetDataType());
                                    assertEquals("The 'name' property of 'dataType' should be set to 'name'",
                                            "name", constraint.getDataType().getValue());
                                    assertEquals("The 'reference' property of 'dataType' should be set to 'uri'",
                                            "uri", constraint.getDataType().getReference());

                                    assertFalse("The 'referenceSystem' property should be set", constraint.isSetReferenceSystem());
                                    assertTrue("The 'uom' property should be set", constraint.isSetUOM());
                                    assertEquals("The 'name' property of 'uom' should be set to 'name'",
                                            "name", constraint.getUOM().getValue());
                                    assertEquals("The 'reference' property of 'dataType' should be set to 'uri'",
                                            "uri", constraint.getUOM().getReference());

                                    assertTrue("The 'metadata' property should be set", constraint.isSetMetadata());
                                    MetadataType metadataType = constraint.getMetadata().get(0);
                                    assertEquals("The 'href' property of 'metadata' should be 'href'", "href",
                                            metadataType.getHref());
                                    assertEquals("The 'role' property of 'metadata' should be 'role'", "role",
                                            metadataType.getRole());
                                    assertEquals("The 'arcrole' property of 'metadata' should be 'arcrole'", "arcrole",
                                            metadataType.getArcrole());
                                    assertEquals("The 'title' property of 'metadata' should be 'title'", "title",
                                            metadataType.getTitle());
                                    assertEquals("The 'show' property of 'metadata' should be 'NONE'", ShowType.NONE,
                                            metadataType.getShow());
                                    assertEquals("The 'actuate' property of 'metadata' should be 'NONE'", ActuateType.NONE,
                                            metadataType.getActuate());
                                    isCstr1 = true;
                                }
                                else if("cstr2".equals(constraint.getName())){
                                    assertEquals("The 'name' property of 'constraint' should be set to 'cstr2'",
                                            "cstr2", constraint.getName());
                                    assertTrue("The 'possibleValue' property should be set to 'anyValue'",
                                            !constraint.isSetAllowedValues() && constraint.isSetAnyValue() &&
                                                    !constraint.isSetNoValues() && !constraint.isSetValuesReference());

                                    assertFalse("The 'defaultValue' property should not be set", constraint.isSetDefaultValue());
                                    assertFalse("The 'meaning' property should not be set", constraint.isSetMeaning());
                                    assertFalse("The 'dataType' property should not be set", constraint.isSetDataType());
                                    assertFalse("The 'uom' property should be set", constraint.isSetUOM());
                                    assertTrue("The 'referenceSystem' property should be set", constraint.isSetReferenceSystem());
                                    assertEquals("The 'name' property of 'uom' should be set to 'name'",
                                            "name", constraint.getReferenceSystem().getValue());
                                    assertEquals("The 'reference' property of 'dataType' should be set to 'uri'",
                                            "uri", constraint.getReferenceSystem().getReference());

                                    assertFalse("The 'metadata' property should not be set", constraint.isSetMetadata());
                                    isCstr2 = true;
                                }
                                else if("cstr3".equals(constraint.getName())){
                                    assertEquals("The 'name' property of 'constraint' should be set to 'cstr3'",
                                            "cstr3", constraint.getName());
                                    assertTrue("The 'possibleValue' property should be set to 'noValue'",
                                            !constraint.isSetAllowedValues() && !constraint.isSetAnyValue() &&
                                                    constraint.isSetNoValues() && !constraint.isSetValuesReference());

                                    assertFalse("The 'defaultValue' property should not be set", constraint.isSetDefaultValue());
                                    assertFalse("The 'meaning' property should not be set", constraint.isSetMeaning());
                                    assertFalse("The 'dataType' property should not be set", constraint.isSetDataType());
                                    assertFalse("The 'referenceSystem' property should be set", constraint.isSetReferenceSystem());
                                    assertFalse("The 'uom' property should be set", constraint.isSetUOM());
                                    assertFalse("The 'metadata' property should not be set", constraint.isSetMetadata());
                                    isCstr3 = true;
                                }
                                else if("cstr4".equals(constraint.getName())){
                                    assertEquals("The 'name' property of 'constraint' should be set to 'cstr4'",
                                            "cstr4", constraint.getName());
                                    assertTrue("The 'possibleValue' property should be set to 'valueReference'",
                                            !constraint.isSetAllowedValues() && !constraint.isSetAnyValue() &&
                                                    !constraint.isSetNoValues() && constraint.isSetValuesReference());

                                    assertFalse("The 'defaultValue' property should not be set", constraint.isSetDefaultValue());
                                    assertFalse("The 'meaning' property should not be set", constraint.isSetMeaning());
                                    assertFalse("The 'dataType' property should not be set", constraint.isSetDataType());
                                    assertFalse("The 'referenceSystem' property should be set", constraint.isSetReferenceSystem());
                                    assertFalse("The 'uom' property should be set", constraint.isSetUOM());
                                    assertFalse("The 'metadata' property should not be set", constraint.isSetMetadata());
                                    isCstr4 = true;
                                }
                                else{
                                    fail("Unknown constraint");
                                }
                            }
                            if(!isCstr1){
                                fail("No constraint named 'cstr1' found");
                            }
                            if(!isCstr2){
                                fail("No constraint named 'cstr2' found");
                            }
                            if(!isCstr3){
                                fail("No constraint named 'cstr3' found");
                            }
                            if(!isCstr4){
                                fail("No constraint named 'cstr4' found");
                            }
                            isUrl2 = true;
                        }
                        else if("getcapabilitiesurl2".equals(element.getValue().getHref())) {
                            assertEquals("The 'href' property of 'GetCapabilities.getOrPost' should be set to 'getcapabilitiesurl2'",
                                    "getcapabilitiesurl2", element.getValue().getHref());
                            assertNull("The 'role' property of 'GetCapabilities.getOrPost' should be set null",
                                    element.getValue().getRole());
                            assertNull("The 'arcrole' property of 'GetCapabilities.getOrPost' should be null",
                                    element.getValue().getArcrole());
                            assertNull("The 'title' property of 'GetCapabilities.getOrPost' should be null",
                                    element.getValue().getTitle());
                            assertNull("The 'show' property of 'GetCapabilities.getOrPost' should be null",
                                    element.getValue().getShow());
                            assertNull("The 'actuate' property of 'GetCapabilities.getOrPost' should be null",
                                   element.getValue().getActuate());

                            assertFalse("The 'constraint' property of 'GetCapabilities.getOrPost' should not be set",
                                    element.getValue().isSetConstraint());
                            isUrl1 = true;
                        }
                        else{
                            fail("Unknown getOrPost");
                        }
                    }

                    if(!isUrl1){
                        fail("No getcapabilitiesurl1 found");
                    }
                    if(!isUrl2){
                        fail("No getcapabilitiesurl2 found");
                    }
                }


                assertTrue("The 'parameter' property of 'GetCapabilities' should be set",
                        operation.isSetParameter());
                assertEquals("The 'parameter' property of 'GetCapabilities' should contain only one value",
                        1, operation.getParameter().size());
                assertEquals("The 'parameter' property name of 'GetCapabilities' should be set to 'param'",
                        "param", operation.getParameter().get(0).getName());
                assertFalse("The 'dataType' property name of 'GetCapabilities' should not be set",
                        operation.getParameter().get(0).isSetDataType());
                assertFalse("The 'uom' property name of 'GetCapabilities' should not be set",
                        operation.getParameter().get(0).isSetUOM());
                assertFalse("The 'referenceSystem' property name of 'GetCapabilities' should not be set",
                        operation.getParameter().get(0).isSetReferenceSystem());
                assertFalse("The 'metadata' property name of 'GetCapabilities' should not be set",
                        operation.getParameter().get(0).isSetMetadata());
                assertFalse("The 'defaultValue' property name of 'GetCapabilities' should not be set",
                        operation.getParameter().get(0).isSetDefaultValue());
                assertFalse("The 'meaning' property name of 'GetCapabilities' should not be set",
                        operation.getParameter().get(0).isSetMeaning());
                assertTrue("Only 'anyValue' should be set",
                        operation.getParameter().get(0).isSetAnyValue() &&
                                !operation.getParameter().get(0).isSetNoValues() &&
                                !operation.getParameter().get(0).isSetValuesReference() &&
                                !operation.getParameter().get(0).isSetAllowedValues());

                assertTrue("The 'constraint' property of 'GetCapabilities' should be set",
                        operation.isSetConstraint());
                assertEquals("The 'constraint' property of 'GetCapabilities' should contain only one value",
                        1, operation.getConstraint().size());
                assertEquals("The 'constraint' property name of 'GetCapabilities' should be set to 'cstr'",
                        "cstr", operation.getConstraint().get(0).getName());
                assertFalse("The 'dataType' property name of 'GetCapabilities' should not be set",
                        operation.getConstraint().get(0).isSetDataType());
                assertFalse("The 'uom' property name of 'GetCapabilities' should not be set",
                        operation.getConstraint().get(0).isSetUOM());
                assertFalse("The 'referenceSystem' property name of 'GetCapabilities' should not be set",
                        operation.getConstraint().get(0).isSetReferenceSystem());
                assertFalse("The 'metadata' property name of 'GetCapabilities' should not be set",
                        operation.getConstraint().get(0).isSetMetadata());
                assertFalse("The 'defaultValue' property name of 'GetCapabilities' should not be set",
                        operation.getConstraint().get(0).isSetDefaultValue());
                assertFalse("The 'meaning' property name of 'GetCapabilities' should not be set",
                        operation.getConstraint().get(0).isSetMeaning());
                assertTrue("Only 'anyValue' should be set",
                        operation.getConstraint().get(0).isSetAnyValue() &&
                                !operation.getConstraint().get(0).isSetNoValues() &&
                                !operation.getConstraint().get(0).isSetValuesReference() &&
                                !operation.getConstraint().get(0).isSetAllowedValues());

                assertTrue("The 'metadata' property of 'GetCapabilities' should be set",
                        operation.isSetMetadata());
                assertEquals("The 'metadata' property of 'GetCapabilities' should contain only one value",
                        1, operation.getMetadata().size());
                assertEquals("The 'constraint' property name of 'GetCapabilities' 'metadata' should be set to 'href'",
                        "href", operation.getMetadata().get(0).getHref());
                assertTrue("The 'role' property name of 'GetCapabilities' 'metadata' should be set",
                        operation.getMetadata().get(0).isSetRole());
                assertEquals("The 'role' property of 'GetCapabilities' 'metadata' should be 'role'", "role",
                        operation.getMetadata().get(0).getRole());
                assertTrue("The 'about' property name of 'GetCapabilities' 'metadata' should be set",
                        operation.getMetadata().get(0).isSetAbout());
                assertEquals("The 'about' property of 'GetCapabilities' 'metadata' should be 'about'", "about",
                        operation.getMetadata().get(0).getAbout());
                assertTrue("The 'about' property name of 'GetCapabilities' 'metadata' should be set",
                        operation.getMetadata().get(0).isSetArcrole());
                assertEquals("The 'arcrole' property of 'GetCapabilities' 'metadata' should be 'arcrole'", "arcrole",
                        operation.getMetadata().get(0).getArcrole());
                assertTrue("The 'actuate' property name of 'GetCapabilities' 'metadata' should be set",
                        operation.getMetadata().get(0).isSetActuate());
                assertEquals("The 'actuate' property of 'GetCapabilities' 'metadata' should be 'NONE'", ActuateType.NONE,
                        operation.getMetadata().get(0).getActuate());
                assertTrue("The 'show' property name of 'GetCapabilities' 'metadata' should be set",
                        operation.getMetadata().get(0).isSetShow());
                assertEquals("The 'show' property of 'GetCapabilities' 'metadata' should be 'NONE'", ShowType.NONE,
                        operation.getMetadata().get(0).getShow());
                assertTrue("The 'title' property name of 'GetCapabilities' 'metadata' should be set",
                        operation.getMetadata().get(0).isSetTitle());
                assertEquals("The 'title' property of 'GetCapabilities' 'metadata' should be 'title'", "title",
                        operation.getMetadata().get(0).getTitle());
                assertTrue("The 'abstractMetadata' property name of 'GetCapabilities' 'metadata' should be set",
                        operation.getMetadata().get(0).isSetAbstractMetaData());
                assertEquals("The 'abstractMetadata' property of 'GetCapabilities' 'metadata' should be 'abstractMetadata'", "abstractMetadata",
                        operation.getMetadata().get(0).getAbstractMetaData());
                isGetCapabilities = true;
            }
            else if(operation.getName().equals("DescribeProcess")){
                assertTrue("The 'DCP' property of 'DescribeProcess' should be set", operation.isSetDCP());
                assertEquals("The 'DCP' property of 'DescribeProcess' should contains only one value", 1,
                        operation.getDCP().size());
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The 'http' property of 'DescribeProcess' should be set", dcp.isSetHTTP());
                    assertTrue("The 'getOrPost' property of 'DescribeProcess' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertTrue("The 'getOrPost' property of 'DescribeProcess' should contains at least one value", 1 <=
                            dcp.getHTTP().getGetOrPost().size());
                    boolean isUrl1 = false;
                    boolean isUrl2 = false;
                    for(JAXBElement<RequestMethodType> element : dcp.getHTTP().getGetOrPost()){
                        if("describeprocessurl1".equals(element.getValue().getHref())) {
                            assertEquals("The 'href' property of 'DescribeProcess.getOrPost' should be set to 'describeprocessurl1'",
                                    "describeprocessurl1", element.getValue().getHref());
                            assertFalse("The 'role' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetRole());
                            assertFalse("The 'actuate' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetActuate());
                            assertFalse("The 'show' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetShow());
                            assertFalse("The 'arcrole' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetArcrole());
                            assertFalse("The 'title' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetTitle());

                            assertFalse("The 'constraint' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetConstraint());
                            isUrl1 = true;
                        }
                        else if("describeprocessurl2".equals(element.getValue().getHref())) {
                            assertEquals("The 'href' property of 'DescribeProcess.getOrPost' should be set to 'describeprocessurl2'",
                                    "describeprocessurl2", element.getValue().getHref());
                            assertFalse("The 'role' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetRole());
                            assertFalse("The 'actuate' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetActuate());
                            assertFalse("The 'show' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetShow());
                            assertFalse("The 'arcrole' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetArcrole());
                            assertFalse("The 'title' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetTitle());

                            assertFalse("The 'constraint' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetConstraint());
                            isUrl2 = true;
                        }
                        else{
                            fail("Unknown getOrPost");
                        }
                    }
                    if(!isUrl1){
                        fail("No describeprocessurl1 found");
                    }
                    if(!isUrl2){
                        fail("No describeprocessurl2 found");
                    }
                }

                assertFalse("The 'parameter' property of 'DescribeProcess' should not be set",
                        operation.isSetParameter());
                assertFalse("The 'constraint' property of 'DescribeProcess' should not be set",
                        operation.isSetConstraint());
                assertFalse("The 'metadata' property of 'DescribeProcess' should not be set",
                        operation.isSetMetadata());
                isDescribeProcess = true;
            }
            else if(operation.getName().equals("Execute")){
                assertTrue("The 'DCP' property of 'Execute' should be set", operation.isSetDCP());
                assertEquals("The 'DCP' property of 'Execute' should contains only one value", 1,
                        operation.getDCP().size());
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The 'http' property of 'Execute' should be set", dcp.isSetHTTP());
                    assertTrue("The 'getOrPost' property of 'Execute' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertTrue("The 'getOrPost' property of 'Execute' should contains at least one value", 1 <=
                            dcp.getHTTP().getGetOrPost().size());
                    boolean isUrl1 = false;
                    boolean isUrl2 = false;
                    for(JAXBElement<RequestMethodType> element : dcp.getHTTP().getGetOrPost()){
                        if("executeurl1".equals(element.getValue().getHref())) {
                            assertEquals("The 'href' property of 'Execute.getOrPost' should be set to 'executeurl1'",
                                    "executeurl1", element.getValue().getHref());
                            assertFalse("The 'role' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetRole());
                            assertFalse("The 'actuate' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetActuate());
                            assertFalse("The 'show' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetShow());
                            assertFalse("The 'arcrole' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetArcrole());
                            assertFalse("The 'title' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetTitle());

                            assertFalse("The 'constraint' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetConstraint());
                            isUrl1 = true;
                        }
                        else if("executeurl2".equals(element.getValue().getHref())) {
                            assertEquals("The 'href' property of 'Execute.getOrPost' should be set to 'executeurl2'",
                                    "executeurl2", element.getValue().getHref());
                            assertFalse("The 'role' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetRole());
                            assertFalse("The 'actuate' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetActuate());
                            assertFalse("The 'show' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetShow());
                            assertFalse("The 'arcrole' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetArcrole());
                            assertFalse("The 'title' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetTitle());

                            assertFalse("The 'constraint' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetConstraint());
                            isUrl2 = true;
                        }
                        else{
                            fail("Unknown getOrPost");
                        }
                    }
                    if(!isUrl1){
                        fail("No executeurl1 found");
                    }
                    if(!isUrl2){
                        fail("No executeurl2 found");
                    }
                }

                assertFalse("The 'parameter' property of 'Execute' should not be set",
                        operation.isSetParameter());
                assertFalse("The 'constraint' property of 'Execute' should not be set",
                        operation.isSetConstraint());
                assertFalse("The 'metadata' property of 'Execute' should not be set",
                        operation.isSetMetadata());
                isExecute = true;
            }
            else{
                fail("Unknown operation");
            }
        }
        if(!isGetCapabilities){
            fail("No 'GetCapabilities' operation found");
        }
        if(!isDescribeProcess){
            fail("No 'DescribeProcess' operation found");
        }
        if(!isExecute){
            fail("No 'Execute' operation found");
        }
        assertNotNull("The 'PARAMETERS' property of 'OPERATIONS_METADATA_PROPERTIES' should be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS);
        assertEquals("The 'PARAMETERS' property of 'OPERATIONS_METADATA_PROPERTIES' should contain only one value",
                1, fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.size());
        assertEquals("The 'PARAMETERS' property name of 'OPERATIONS_METADATA_PROPERTIES' should be set to 'param'",
                "param", fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.get(0).getName());
        assertFalse("The 'dataType' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.get(0).isSetDataType());
        assertFalse("The 'uom' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.get(0).isSetUOM());
        assertFalse("The 'referenceSystem' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.get(0).isSetReferenceSystem());
        assertFalse("The 'metadata' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.get(0).isSetMetadata());
        assertFalse("The 'defaultValue' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.get(0).isSetDefaultValue());
        assertFalse("The 'meaning' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.get(0).isSetMeaning());
        assertTrue("Only 'anyValue' should be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.get(0).isSetAnyValue() &&
                        !fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.get(0).isSetNoValues() &&
                        !fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.get(0).isSetValuesReference() &&
                        !fullProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS.get(0).isSetAllowedValues());

        assertNotNull("The 'CONSTRAINTS' property of 'OPERATIONS_METADATA_PROPERTIES' should be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS);
        assertEquals("The 'CONSTRAINTS' property of 'OPERATIONS_METADATA_PROPERTIES' should contain only one value",
                1, fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.size());
        assertEquals("The 'CONSTRAINTS' property name of 'OPERATIONS_METADATA_PROPERTIES' should be set to 'cstr'",
                "cstr", fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.get(0).getName());
        assertFalse("The 'dataType' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.get(0).isSetDataType());
        assertFalse("The 'uom' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.get(0).isSetUOM());
        assertFalse("The 'referenceSystem' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.get(0).isSetReferenceSystem());
        assertFalse("The 'metadata' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.get(0).isSetMetadata());
        assertFalse("The 'defaultValue' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.get(0).isSetDefaultValue());
        assertFalse("The 'meaning' property name of 'OPERATIONS_METADATA_PROPERTIES' should not be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.get(0).isSetMeaning());
        assertTrue("Only 'anyValue' should be set",
                fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.get(0).isSetAnyValue() &&
                        !fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.get(0).isSetNoValues() &&
                        !fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.get(0).isSetValuesReference() &&
                        !fullProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS.get(0).isSetAllowedValues());
    }

    @Test
    public void testFullWsdl() {
        assertNotNull("The 'WSDL' should be set", fullProps.WSDL_PROPERTIES);
        assertEquals("The 'href' property of 'WSDL' should be set to 'href'", "href", fullProps.WSDL_PROPERTIES.HREF);
    }

    @Test
    public void testFullCustomProperties() {
        assertNotNull("The 'CUSTOM_PROPERTIES' should be set", fullProps.CUSTOM_PROPERTIES);
        assertEquals("The 'MAXIMUM_MEGABYTES' property of 'CUSTOM_PROPERTIES' should be set to '2000'", "2000",
                fullProps.CUSTOM_PROPERTIES.MAXIMUM_MEGABYTES);
        assertEquals("The 'MAX_PROCESS_POLLING_DELAY' property of 'CUSTOM_PROPERTIES' should be set to '10000'", 10000,
                fullProps.CUSTOM_PROPERTIES.MAX_PROCESS_POLLING_DELAY);
        assertEquals("The 'BASE_PROCESS_POLLING_DELAY' property of 'CUSTOM_PROPERTIES' should be set to '1000'", 1000,
                fullProps.CUSTOM_PROPERTIES.BASE_PROCESS_POLLING_DELAY);
    }

    ////
    //Test of minimal properties
    ////
    @Test
    public void testMinGlobalProperties(){
        assertNotNull("The property 'GLOBAL_PROPERTIES' should not be null", minProps.GLOBAL_PROPERTIES);
        assertEquals("The 'SERVICE' property of 'GLOBAL_PROPERTIES' should be 'WPS'",
                "WPS", minProps.GLOBAL_PROPERTIES.SERVICE);
        assertEquals("The 'SERVER_VERSION' property of 'GLOBAL_PROPERTIES' should be '1.0.0'",
                "1.0.0", minProps.GLOBAL_PROPERTIES.SERVER_VERSION);
        assertNull("The 'SUPPORTED_VERSION' property of 'GLOBAL_PROPERTIES' should be null",
                minProps.GLOBAL_PROPERTIES.SUPPORTED_VERSIONS);
        assertNull("The 'UPDATE_SEQUENCE' property of 'GLOBAL_PROPERTIES' should be null",
                minProps.GLOBAL_PROPERTIES.UPDATE_SEQUENCE);
        assertEquals("The 'DEFAULT_LANGUAGE' property of 'GLOBAL_PROPERTIES' should be 'en'",
                "en", minProps.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
        assertArrayEquals("The 'SUPPORTED_LANGUAGES' property of 'GLOBAL_PROPERTIES' should be set to '[en, fr-fr]'",
                new String[]{"en", "fr-fr"}, minProps.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES);
        assertNull("The 'SUPPORTED_FORMATS' property of 'GLOBAL_PROPERTIES' should be null",
                minProps.GLOBAL_PROPERTIES.SUPPORTED_FORMATS);
        assertFalse("The 'STORE_SUPPORTED' property of 'GLOBAL_PROPERTIES' should be 'false'",
                minProps.GLOBAL_PROPERTIES.STORE_SUPPORTED);
        assertFalse("The 'STATUS_SUPPORTED' property of 'GLOBAL_PROPERTIES' should be 'false'",
                minProps.GLOBAL_PROPERTIES.STATUS_SUPPORTED);
    }

    @Test
    public void testMinServiceIdentificationProperties(){
        assertNotNull("The property 'SERVICE_IDENTIFICATION_PROPERTIES' should not be null",
                minProps.SERVICE_IDENTIFICATION_PROPERTIES);
        assertEquals("The 'SERVICE_TYPE' property of 'SERVICE_IDENTIFICATION_PROPERTIES' should be 'WPS'",
                "WPS", minProps.SERVICE_IDENTIFICATION_PROPERTIES.SERVICE_TYPE.getValue());
        assertArrayEquals("The 'SERVICE_TYPE_VERSIONS' property of 'SERVICE_IDENTIFICATION_PROPERTIES' should be set " +
                        "to '[1.0.0]'",
                new String[]{"1.0.0"}, minProps.SERVICE_IDENTIFICATION_PROPERTIES.SERVICE_TYPE_VERSIONS);
        assertNull("The 'PROFILE' property of 'SERVICE_IDENTIFICATION_PROPERTIES' should be null",
                minProps.SERVICE_IDENTIFICATION_PROPERTIES.PROFILE);
        assertNotNull("The property 'TITLE' of 'SERVICE_IDENTIFICATION_PROPERTIES' should not be null",
                minProps.SERVICE_IDENTIFICATION_PROPERTIES.TITLE);
        for(LanguageStringType languageStringType : minProps.SERVICE_IDENTIFICATION_PROPERTIES.TITLE){
            if(languageStringType.getLang().equals("en")){
                assertEquals("Wrong value for the 'en' title", "Local WPS Service", languageStringType.getValue());
            }
            else if(languageStringType.getLang().equals("fr-fr")){
                assertEquals("Wrong value for the 'fr-fr' title", "Service WPS locale", languageStringType.getValue());
            }
            else{
                fail("No titles with the language "+languageStringType.getLang());
            }
        }
        assertNull("The property 'ABSTRACT' of 'SERVICE_IDENTIFICATION_PROPERTIES' should be null",
                minProps.SERVICE_IDENTIFICATION_PROPERTIES.ABSTRACT);
        assertNull("The property 'KEYWORDS' of 'SERVICE_IDENTIFICATION_PROPERTIES' should be null",
                minProps.SERVICE_IDENTIFICATION_PROPERTIES.KEYWORDS);
        assertNull("The 'FEES' property of 'SERVICE_IDENTIFICATION_PROPERTIES' should be null",
                minProps.SERVICE_IDENTIFICATION_PROPERTIES.FEES);
        assertNull("The 'SERVICE_TYPE_VERSIONS' property of 'SERVICE_IDENTIFICATION_PROPERTIES' should be null",
                minProps.SERVICE_IDENTIFICATION_PROPERTIES.ACCESS_CONSTRAINTS);
    }

    @Test
    public void testMinServiceProviderProperties(){
        assertNotNull("The 'SERVICE_PROVIDER_PROPERTIES' property should not be null'",
                minProps.SERVICE_PROVIDER_PROPERTIES);
        assertEquals("The 'PROVIDER_NAME' property of 'SERVICE_PROVIDER_PROPERTIES' should be 'OrbisGIS'",
                "OrbisGIS", minProps.SERVICE_PROVIDER_PROPERTIES.PROVIDER_NAME);

        assertNull("The 'PROVIDER_SITE' property of 'SERVICE_PROVIDER_PROPERTIES' should be null",
                minProps.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE);
        assertNull("The 'SERVICE_CONTACT' property of 'SERVICE_PROVIDER_PROPERTIES' should be null",
                minProps.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT);
    }

    @Test
    public void testMinOperationMetadataProperties(){
        boolean isGetCapabilities = false;
        boolean isDescribeProcess = false;
        boolean isExecute = false;
        for(Operation operation : minProps.OPERATIONS_METADATA_PROPERTIES.OPERATIONS){
            if(operation.getName().equals("GetCapabilities")){
                assertTrue("The 'DCP' property of 'GetCapabilities' should be set", operation.isSetDCP());
                assertEquals("The 'DCP' property of 'GetCapabilities' should contains only one value", 1,
                        operation.getDCP().size());
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The 'http' property of 'GetCapabilities' should be set", dcp.isSetHTTP());
                    assertTrue("The 'getOrPost' property of 'GetCapabilities' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertEquals("The 'getOrPost' property of 'GetCapabilities' should contains at least one value", 1,
                            dcp.getHTTP().getGetOrPost().size());
                    for(JAXBElement<RequestMethodType> element : dcp.getHTTP().getGetOrPost()){
                        if("getcapabilitiesurl1".equals(element.getValue().getHref())) {
                            assertEquals("The 'href' property of 'GetCapabilities.getOrPost' should be set to 'getcapabilitiesurl1'",
                                    "getcapabilitiesurl1", element.getValue().getHref());
                            assertFalse("The 'role' property of 'GetCapabilities.getOrPost' should not be set",
                                    element.getValue().isSetRole());
                            assertFalse("The 'arcrole' property of 'GetCapabilities.getOrPost' should not be set",
                                    element.getValue().isSetArcrole());
                            assertFalse("The 'title' property of 'GetCapabilities.getOrPost' should not be set",
                                    element.getValue().isSetTitle());
                            assertFalse("The 'show' property of 'GetCapabilities.getOrPost' should not be set",
                                    element.getValue().isSetShow());
                            assertFalse("The 'actuate' property of 'GetCapabilities.getOrPost' should not be set",
                                    element.getValue().isSetActuate());

                            assertFalse("The 'constraint' property of 'GetCapabilities.getOrPost' should not be set",
                                    element.getValue().isSetConstraint());
                        }
                        else{
                            fail("Unknown getOrPost");
                        }
                    }
                }


                assertFalse("The 'parameter' property of 'GetCapabilities' should not be set",
                        operation.isSetParameter());
                assertFalse("The 'constraint' property of 'GetCapabilities' should not be set",
                        operation.isSetConstraint());
                assertFalse("The 'metadata' property of 'GetCapabilities' should not be set",
                        operation.isSetMetadata());
                isGetCapabilities = true;
            }
            else if(operation.getName().equals("DescribeProcess")){
                assertTrue("The 'DCP' property of 'DescribeProcess' should be set", operation.isSetDCP());
                assertEquals("The 'DCP' property of 'DescribeProcess' should contains only one value", 1,
                        operation.getDCP().size());
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The 'http' property of 'DescribeProcess' should be set", dcp.isSetHTTP());
                    assertTrue("The 'getOrPost' property of 'DescribeProcess' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertEquals("The 'getOrPost' property of 'DescribeProcess' should contains one value", 1,
                            dcp.getHTTP().getGetOrPost().size());
                    for(JAXBElement<RequestMethodType> element : dcp.getHTTP().getGetOrPost()){
                        if("describeprocessurl1".equals(element.getValue().getHref())) {
                            assertEquals("The 'href' property of 'DescribeProcess.getOrPost' should be set to 'describeprocessurl1'",
                                    "describeprocessurl1", element.getValue().getHref());
                            assertFalse("The 'role' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetRole());
                            assertFalse("The 'actuate' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetActuate());
                            assertFalse("The 'show' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetShow());
                            assertFalse("The 'arcrole' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetArcrole());
                            assertFalse("The 'title' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetTitle());

                            assertFalse("The 'constraint' property of 'DescribeProcess.getOrPost' should not be set",
                                    element.getValue().isSetConstraint());
                        }
                        else{
                            fail("Unknown getOrPost");
                        }
                    }
                }
                assertFalse("The 'parameter' property of 'DescribeProcess' should not be set",
                        operation.isSetParameter());
                assertFalse("The 'constraint' property of 'DescribeProcess' should not be set",
                        operation.isSetConstraint());
                assertFalse("The 'metadata' property of 'DescribeProcess' should not be set",
                        operation.isSetMetadata());
                isDescribeProcess = true;
            }
            else if(operation.getName().equals("Execute")){
                assertTrue("The 'DCP' property of 'Execute' should be set", operation.isSetDCP());
                assertEquals("The 'DCP' property of 'Execute' should contains only one value", 1,
                        operation.getDCP().size());
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The 'http' property of 'Execute' should be set", dcp.isSetHTTP());
                    assertTrue("The 'getOrPost' property of 'Execute' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertEquals("The 'getOrPost' property of 'Execute' should contains at least one value", 1,
                            dcp.getHTTP().getGetOrPost().size());
                    for(JAXBElement<RequestMethodType> element : dcp.getHTTP().getGetOrPost()){
                        if("executeurl1".equals(element.getValue().getHref())) {
                            assertEquals("The 'href' property of 'Execute.getOrPost' should be set to 'executeurl1'",
                                    "executeurl1", element.getValue().getHref());
                            assertFalse("The 'role' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetRole());
                            assertFalse("The 'actuate' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetActuate());
                            assertFalse("The 'show' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetShow());
                            assertFalse("The 'arcrole' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetArcrole());
                            assertFalse("The 'title' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetTitle());

                            assertFalse("The 'constraint' property of 'Execute.getOrPost' should not be set",
                                    element.getValue().isSetConstraint());
                        }
                        else{
                            fail("Unknown getOrPost");
                        }
                    }
                }
                assertFalse("The 'parameter' property of 'Execute' should not be set",
                        operation.isSetParameter());
                assertFalse("The 'constraint' property of 'Execute' should not be set",
                        operation.isSetConstraint());
                assertFalse("The 'metadata' property of 'Execute' should not be set",
                        operation.isSetMetadata());
                isExecute = true;
            }
            else{
                fail("Unknown operation");
            }
        }
        if(!isGetCapabilities){
            fail("No 'GetCapabilities' operation found");
        }
        if(!isDescribeProcess){
            fail("No 'DescribeProcess' operation found");
        }
        if(!isExecute){
            fail("No 'Execute' operation found");
        }
        assertNull("The 'PARAMETERS' property of 'OPERATIONS_METADATA_PROPERTIES' should be null",
                minProps.OPERATIONS_METADATA_PROPERTIES.PARAMETERS);
        assertNull("The 'CONSTRAINTS' property of 'OPERATIONS_METADATA_PROPERTIES' should be null",
                minProps.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS);
    }

    @Test
    public void testMinWsdl() {
        assertNotNull("The 'WSDL' should be set", minProps.WSDL_PROPERTIES);
        assertNull("The 'href' property of 'WSDL' should be nul", minProps.WSDL_PROPERTIES.HREF);
    }

    @Test
    public void testMinCustomProperties() {
        assertNotNull("The 'CUSTOM_PROPERTIES' should be set", minProps.CUSTOM_PROPERTIES);
        assertEquals("The 'MAXIMUM_MEGABYTES' property of 'CUSTOM_PROPERTIES' should be set to '1'", "1",
                minProps.CUSTOM_PROPERTIES.MAXIMUM_MEGABYTES);
        assertEquals("The 'MAX_PROCESS_POLLING_DELAY' property of 'CUSTOM_PROPERTIES' should be set to '10000'", 10000,
                minProps.CUSTOM_PROPERTIES.MAX_PROCESS_POLLING_DELAY);
        assertEquals("The 'BASE_PROCESS_POLLING_DELAY' property of 'CUSTOM_PROPERTIES' should be set to '1000'", 1000,
                minProps.CUSTOM_PROPERTIES.BASE_PROCESS_POLLING_DELAY);
    }
}
