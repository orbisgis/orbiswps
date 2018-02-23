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
package org.orbisgis.orbiswps.testsuite;

import net.opengis.ows._2.*;
import net.opengis.wps._2_0.*;
import net.opengis.wps._2_0.GetCapabilitiesType;
import net.opengis.wps._2_0.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.orbiswps.service.model.JaxbContainer;
import org.orbisgis.orbiswps.service.operations.WpsServerProperties_2_0;
import org.orbisgis.orbiswps.serviceapi.WpsServer;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Test all the cases of a GetCapabilities request.
 *
 * @author Sylvain PALOMINOS
 */
public class GetCapabilitiesTest {
    private WpsServer service;
    private Unmarshaller unmarshaller;
    private Marshaller marshaller;
    private ObjectFactory factory;

    /** Test configuration properties **/
    private Properties props;

    @Before
    public void init() throws SQLException, JAXBException, IOException {
        service = WpsServiceFactory.getService();
        unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
        marshaller = JaxbContainer.JAXBCONTEXT.createMarshaller();
        factory = new ObjectFactory();
        props = new Properties();
        URL url = WpsServerProperties_2_0.class.getResource("basicWpsServer.properties");
        props.load(new InputStreamReader(url.openStream()));
    }

    /**
     * Send the parameter object as request to the WPS service and return the unmarshalled answer.
     * @param obj Request object to send.
     * @return The service answer.
     */
    private Object sendRequest(Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            marshaller.marshal(obj, out);
        } catch (JAXBException e) {
            fail("Exception get on marshalling the request :\n" + e.getLocalizedMessage());
        }
        InputStream in = new DataInputStream(new ByteArrayInputStream(out.toByteArray()));
        ByteArrayOutputStream xml = (ByteArrayOutputStream) service.callOperation(in);
        ByteArrayInputStream resultXml = new ByteArrayInputStream(xml.toByteArray());
        try {
            return unmarshaller.unmarshal(resultXml);
        } catch (JAXBException e) {
            fail("Exception get on sending the request to the service :\n" + e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Transform an ExceptionReport object into a throwable IllegalArgumentException.
     * @param report ExceptionReport object get from the server.
     * @return A throwable IllegalArgumentException object.
     */
    private Exception getException(ExceptionReport report){
        IllegalArgumentException exception = new IllegalArgumentException();
        if(report.isSetException()) {
            String text = "";
            ExceptionType exceptionType = (report).getException().get(0);
            if(exceptionType.isSetExceptionText()) {
                text += exceptionType.getExceptionText().get(0);
            }
            if(exceptionType.isSetExceptionCode()) {
                if(!text.isEmpty()){
                    text += "\n";
                }
                text += exceptionType.getExceptionCode();
            }
            if(exceptionType.isSetLocator()) {
                if(!text.isEmpty()){
                    text += "\n";
                }
                text += exceptionType.getLocator();
            }
            if(!text.isEmpty()){
                exception = new IllegalArgumentException(text);
            }
        }
        throw exception;
    }

    /**
     * Tests the basic mandatory properties of a WPSCapabilities.
     * @param capabilities WPSCapabilities to test.
     */
    private void testBasicsContent(WPSCapabilitiesType capabilities) {
        assertTrue("The 'version' property should be set", capabilities.isSetContents());
        assertTrue("The 'processSummary' property should be set", capabilities.getContents().isSetProcessSummary());
        for (ProcessSummaryType summary : capabilities.getContents().getProcessSummary()) {
            assertTrue("The 'title' property should be set", summary.isSetTitle());
            assertTrue("The 'identifier' property should be set", summary.isSetIdentifier());
            assertTrue("The 'jobControlOptions' property should be set", summary.isSetJobControlOptions());
            String[] defaultControl = props.getProperty("JOB_CONTROL_OPTIONS").split(",");
            Arrays.sort(defaultControl);
            String[] serviceControl = summary.getJobControlOptions().toArray(new String[summary.getJobControlOptions().size()]);
            Arrays.sort(serviceControl);
            assertArrayEquals("The 'jobControlOptions' property does not contains the desired value",
                    defaultControl, serviceControl);
            assertTrue("The 'outputTransmission' property should be set", summary.isSetOutputTransmission());
            String[] defaultTransmission = props.getProperty("DATA_TRANSMISSION_TYPE").split(",");
            Arrays.sort(defaultTransmission);
            Object[] serviceTransmission = new Object[summary.getOutputTransmission().size()];
            for (int i = 0; i < summary.getOutputTransmission().size(); i++) {
                serviceTransmission[i] = summary.getOutputTransmission().get(i).value();
            }
            Arrays.sort(serviceTransmission);
            assertArrayEquals("The 'outputTransmission' property does not contains the desired value",
                    defaultTransmission, serviceTransmission);
        }
    }

    /**
     * Test the answer of the server to an empty GetCapabilities request. The answer should contains at least the
     * properties :
     * - version (ows:VersionType)
     * - service (String equals to 'WPS')
     * - content (List of ProcessSummary)
     * <p>
     * The process summaries contained inside the content property should contain at least the properties :
     * - title (ows:title)
     * - identifier (ows:identifier)
     * - jobControlOptions (String 'sync-execute' and/or 'async-execute', {@link GetCapabilitiesTest#props})
     * - outputTransmission (String 'value' and/or 'reference', {@link GetCapabilitiesTest#props})
     */
    @Test
    public void testEmptyGetCapabilities() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        assertFalse("The field 'AcceptFormat' should not be set.", getCapabilities.isSetAcceptFormats());
        assertFalse("The field 'AcceptLanguages' should not be set.", getCapabilities.isSetAcceptLanguages());
        assertFalse("The field 'AcceptVersions' should not be set.", getCapabilities.isSetAcceptVersions());
        assertFalse("The field 'Sections' should not be set.", getCapabilities.isSetSections());
        assertFalse("The field 'UpdateSequence' should not be set.", getCapabilities.isSetUpdateSequence());

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object
        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'content' property
        testBasicsContent(capabilities);
    }

    /**
     * Test the answer of the server to a GetCapabilities request with the 'updateSequence' property set.
     * <p>
     * The capabilities answer contained inside the content property should contain the last updateSequence
     */
    @Test
    public void testGetCapabilitiesUpdateSequence() throws Exception {
        String updateSeq = props.getProperty("SERVER_VERSION");

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object
        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        assertEquals("The 'updateSequence' property return by the server should be " + updateSeq,
                updateSeq, capabilities.getUpdateSequence());

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'content' property
        testBasicsContent(capabilities);
    }

    /**
     * Test the answer of the server to a GetCapabilities request with the 'version' property set.
     * <p>
     * The capabilities answer should be from the given version if available, from the last version otherwise.
     */
    @Test
    public void testGetCapabilitiesAcceptVersions() throws Exception {
        String versionStr = "1.0.0";

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        AcceptVersionsType version = new AcceptVersionsType();
        version.getVersion().add(versionStr);
        getCapabilities.setAcceptVersions(version);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object
        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof net.opengis.wps._1_0_0.WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPS 1.0.0 WPSCapabilitiesType");
        }
        net.opengis.wps._1_0_0.WPSCapabilitiesType capabilities =
                (net.opengis.wps._1_0_0.WPSCapabilitiesType) element.getValue();

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());
        assertEquals("The 'version' property should be " + versionStr, versionStr, capabilities.getVersion());
    }

    /**
     * Test the answer of the server to a GetCapabilities request with a wrong 'version' property set.
     * <p>
     * The capabilities answer should be an Exception report with the code 'VersionNegotiationFailed'.
     */
    @Test
    public void testGetCapabilitiesBadAcceptVersions() throws Exception {
        String versionStr = "99.99.99";

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        AcceptVersionsType version = new AcceptVersionsType();
        version.getVersion().add(versionStr);
        getCapabilities.setAcceptVersions(version);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the ExceptionReport object
        if (!(result instanceof ExceptionReport)) {
            fail("The result object should be a JAXBElement");
        }
        ExceptionReport exceptionReport = (ExceptionReport)result;
        if(exceptionReport.isSetException()){
            for(ExceptionType exceptionType : exceptionReport.getException()){
                if(exceptionType.isSetExceptionCode()){
                    assertEquals("The exception code should be VersionNegotiationFailed",
                            "VersionNegotiationFailed", exceptionType.getExceptionCode());
                }
                else{
                    fail("The ExceptionType should contains an exception code");
                }
            }
        }
        else{
            fail("The ExceptionReport should contains exceptions");
        }
    }

    /**
     * Test the answer of the server to a GetCapabilities request with the 'sections' property set to
     * 'ServiceIdentification'.
     * <p>
     * The capabilities answer should contains only the mandatory and 'ServiceIdentification' property set.
     */
    @Test
    public void testGetCapabilitiesSectionServiceIdentification() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        SectionsType sectionsType = new SectionsType();
        sectionsType.getSection().add("ServiceIdentification");
        getCapabilities.setSections(sectionsType);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object
        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        assertTrue("The 'serviceIdentification' field should be set.", capabilities.isSetServiceIdentification());
        assertFalse("The 'serviceProvider' property should not be set.", capabilities.isSetServiceProvider());
        assertFalse("The 'operationMetadata' property should not be set.", capabilities.isSetOperationsMetadata());
        assertFalse("The 'contents' property should not be set.", capabilities.isSetContents());
        assertFalse("The 'languages' property should not be set.", capabilities.isSetLanguages());

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'serviceIdentification' property
        ServiceIdentification serviceId = capabilities.getServiceIdentification();

        assertTrue("The property 'serviceType' should be set.", serviceId.isSetServiceType());
        assertTrue("The property 'serviceType' value should be set.", serviceId.getServiceType().isSetValue());
        assertEquals("The 'serviceType' value should be "+ props.getProperty("SERVICE_TYPE"),
                props.getProperty("SERVICE_TYPE"), serviceId.getServiceType().getValue());

        assertTrue("The property 'serviceTypeVersion' should be set.", serviceId.isSetServiceTypeVersion());
        String[] values = props.getProperty("SERVICE_TYPE_VERSIONS").split(",");
        assertEquals("The 'serviceTypeVersion' should contains "+values.length+" values",
                values.length, serviceId.getServiceTypeVersion().size());
        for(String value : values) {
            assertTrue("The 'serviceTypeVersion' value should be " + value,
                    serviceId.getServiceTypeVersion().contains(value));
        }

        if(props.containsKey("PROFILE")){
            assertTrue("The property 'profile' should be set.", serviceId.isSetProfile());
        }
        else{
            assertFalse("The property 'profile' should not be set.", serviceId.isSetProfile());
        }

        if(props.containsKey("FEES")){
            assertTrue("The property 'fees' should be set.", serviceId.isSetFees());
            assertEquals("The property 'fees' should be "+props.getProperty("FEES"),
                    props.getProperty("FEES"), serviceId.getFees());
        }
        else{
            assertFalse("The property 'fees' should not be set.", serviceId.isSetFees());
        }

        if(props.containsKey("ACCESS_CONSTRAINTS")){
            assertTrue("The property 'accessConstraints' should be set.", serviceId.isSetAccessConstraints());
            values = props.getProperty("ACCESS_CONSTRAINTS").split(",");
            assertEquals("The 'accessConstraints' should contains "+values.length+" values",
                    values.length, serviceId.getAccessConstraints().size());
            for(String value : values) {
                assertTrue("The 'serviceTypeVersion' value should contains " + value,
                        serviceId.getAccessConstraints().contains(value));
            }
        }
        else{
            assertFalse("The property 'accessConstraints' should not be set.", serviceId.isSetAccessConstraints());
        }

        if(props.containsKey("TITLE")){
            assertTrue("The property 'title' should be set.", serviceId.isSetTitle());
            values = props.getProperty("TITLE").split(";");
            assertEquals("The 'title' should contains "+values.length+" values",
                    values.length, serviceId.getTitle().size());
            String[] langs = props.getProperty("SUPPORTED_LANGUAGES").split(",");
            for(int i=0; i<values.length; i++) {
                boolean isTitle = false;
                for(LanguageStringType str : serviceId.getTitle()) {
                    if(langs[i].equals(str.getLang()) && values[i].equals(str.getValue())){
                        isTitle = true;
                    }
                }
                assertTrue("The 'title' value should contains " + values[i] + "with the language " +
                        langs[i], isTitle);
            }
        }
        else{
            assertFalse("The property 'title' should not be set.", serviceId.isSetTitle());
        }

        if(props.containsKey("ABSTRACT")){
            assertTrue("The property 'abstract' should be set.", serviceId.isSetAbstract());
            values = props.getProperty("ABSTRACT").split(";");
            assertEquals("The 'abstract' should contains "+values.length+" values",
                    values.length, serviceId.getAbstract().size());
            String[] langs = props.getProperty("SUPPORTED_LANGUAGES").split(",");
            for(int i=0; i<values.length; i++) {
                boolean isAbstract = false;
                for(LanguageStringType str : serviceId.getAbstract()) {
                    if(langs[i].equals(str.getLang()) && values[i].equals(str.getValue())){
                        isAbstract = true;
                    }
                }
                assertTrue("The 'abstract' value should contains " + values[i] + "with the language " +
                        langs[i], isAbstract);
            }
        }
        else{
            assertFalse("The property 'abstract' should not be set.", serviceId.isSetAbstract());
        }

        if(props.containsKey("KEYWORDS")){
            assertTrue("The property 'keywords' should be set.", serviceId.isSetKeywords());
            values = props.getProperty("KEYWORDS").split(";");
            assertEquals("The 'keywords' should contains "+values.length+" values",
                    values.length, serviceId.getKeywords().get(0).getKeyword().size());
            String[] langs = props.getProperty("SUPPORTED_LANGUAGES").split(",");
            for(int i=0; i<values.length; i++) {
                String[] subValues = values[i].split(",");
                for(String subValue : subValues) {
                    boolean isKeyword = false;
                    for (KeywordsType keyword : serviceId.getKeywords()) {
                        for (LanguageStringType str : keyword.getKeyword()){
                            if(subValue.equals(str.getValue()) && langs[i].equals(str.getLang())){
                                isKeyword = true;
                            }
                        }
                    }
                    assertTrue("The 'keyword' value should contains " + subValue +
                            " with the language " + langs[i], isKeyword);
                }
            }
        }
        else{
            assertFalse("The property 'keywords' should not be set.", serviceId.isSetKeywords());
        }

    }

    /**
     * Test the answer of the server to a GetCapabilities request with the 'sections' property set to
     * 'ServiceProvider'.
     * <p>
     * The capabilities answer should contains only the mandatory and 'ServiceProvider' property set.
     */
    @Test
    public void testGetCapabilitiesSectionServiceProvider() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        SectionsType sectionsType = new SectionsType();
        sectionsType.getSection().add("ServiceProvider");
        getCapabilities.setSections(sectionsType);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object
        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        assertFalse("The 'serviceIdentification' field should be set.", capabilities.isSetServiceIdentification());
        assertTrue("The 'serviceProvider' property should not be set.", capabilities.isSetServiceProvider());
        assertFalse("The 'operationMetadata' property should not be set.", capabilities.isSetOperationsMetadata());
        assertFalse("The 'contents' property should not be set.", capabilities.isSetContents());
        assertFalse("The 'languages' property should not be set.", capabilities.isSetLanguages());

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'serviceProvider' property
        ServiceProvider serviceProvider = capabilities.getServiceProvider();

        assertTrue("The property 'providerName' should be set.", serviceProvider.isSetProviderName());
        assertEquals("The 'providerName' value should be "+ props.getProperty("PROVIDER_NAME"),
                props.getProperty("PROVIDER_NAME"), serviceProvider.getProviderName());

        if(props.containsKey("PROVIDER_SITE")){
            assertTrue("The property 'providerSite' should be set", serviceProvider.isSetProviderSite());
            if(props.containsKey("PROVIDER_SITE_HREF")) {
                assertTrue("The href of the property 'providerSite' should be set",
                        serviceProvider.getProviderSite().isSetHref());
                assertEquals("The href of the property 'providerSite' should be " +
                                props.getProperty("PROVIDER_SITE_HREF"), props.getProperty("PROVIDER_SITE_HREF"),
                        serviceProvider.getProviderSite().getHref());
            }
            else{
                assertFalse("The href of the property 'providerSite' should not be set",
                        serviceProvider.getProviderSite().isSetHref());
            }
            if(props.containsKey("PROVIDER_SITE_ROLE")) {
                assertTrue("The role of the property 'providerSite' should be set",
                        serviceProvider.getProviderSite().isSetRole());
                assertEquals("The role of the property 'providerSite' should be " +
                                props.getProperty("PROVIDER_SITE_ROLE"), props.getProperty("PROVIDER_SITE_ROLE"),
                        serviceProvider.getProviderSite().getRole());
            }
            else{
                assertFalse("The role of the property 'providerSite' should not be set",
                        serviceProvider.getProviderSite().isSetRole());
            }
            if(props.containsKey("PROVIDER_SITE_ARCROLE")) {
                assertTrue("The arcrole of the property 'providerSite' should be set",
                        serviceProvider.getProviderSite().isSetArcrole());
                assertEquals("The arcrole of the property 'providerSite' should be " +
                                props.getProperty("PROVIDER_SITE_ARCROLE"), props.getProperty("PROVIDER_SITE_ARCROLE"),
                        serviceProvider.getProviderSite().getArcrole());
            }
            else{
                assertFalse("The arcrole of the property 'providerSite' should not be set",
                        serviceProvider.getProviderSite().isSetArcrole());
            }
            if(props.containsKey("PROVIDER_SITE_TITLE")) {
                assertTrue("The title of the property 'providerSite' should be set",
                        serviceProvider.getProviderSite().isSetTitle());
                assertEquals("The title of the property 'providerSite' should be " +
                                props.getProperty("PROVIDER_SITE_TITLE"), props.getProperty("PROVIDER_SITE_TITLE"),
                        serviceProvider.getProviderSite().getTitle());
            }
            else{
                assertFalse("The title of the property 'providerSite' should not be set",
                        serviceProvider.getProviderSite().isSetTitle());
            }
            if(props.containsKey("PROVIDER_SITE_SHOW")) {
                assertTrue("The show of the property 'providerSite' should be set",
                        serviceProvider.getProviderSite().isSetShow());
                assertEquals("The show of the property 'providerSite' should be " +
                                props.getProperty("PROVIDER_SITE_SHOW"), props.getProperty("PROVIDER_SITE_SHOW"),
                        serviceProvider.getProviderSite().getShow().value());
            }
            else{
                assertFalse("The show of the property 'providerSite' should not be set",
                        serviceProvider.getProviderSite().isSetShow());
            }
            if(props.containsKey("PROVIDER_SITE_ACTUATE")) {
                assertTrue("The actuate of the property 'providerSite' should be set",
                        serviceProvider.getProviderSite().isSetActuate());
                assertEquals("The actuate of the property 'providerSite' should be " +
                                props.getProperty("PROVIDER_SITE_ACTUATE"), props.getProperty("PROVIDER_SITE_ACTUATE"),
                        serviceProvider.getProviderSite().getActuate().value());
            }
            else{
                assertFalse("The actuate of the property 'providerSite' should not be set",
                        serviceProvider.getProviderSite().isSetActuate());
            }
        }
        else{
            assertFalse("The property 'providerSite' should not be set", serviceProvider.isSetProviderSite());
        }

        assertTrue("The property 'serviceContact' should be set", serviceProvider.isSetServiceContact());
        if(props.containsKey("SERVICE_CONTACT_INDIVIDUAL_NAME")) {
            assertTrue("The individual name of the property 'serviceContact' should be set",
                    serviceProvider.getServiceContact().isSetIndividualName());
            assertEquals("The individual name of the property 'serviceContact' should be "+
                            props.getProperty("SERVICE_CONTACT_INDIVIDUAL_NAME"),
                    props.getProperty("SERVICE_CONTACT_INDIVIDUAL_NAME"),
                    serviceProvider.getServiceContact().getIndividualName());
        }
        else {
            assertFalse("The individual name of the property 'serviceContact' should not be set",
                    serviceProvider.getServiceContact().isSetIndividualName());
        }
        if(props.containsKey("SERVICE_CONTACT_POSITION_NAME")) {
            assertTrue("The position name of the property 'serviceContact' should be set",
                    serviceProvider.getServiceContact().isSetPositionName());
            assertEquals("The position name of the property 'serviceContact' should be "+
                            props.getProperty("SERVICE_CONTACT_POSITION_NAME"),
                    props.getProperty("SERVICE_CONTACT_POSITION_NAME"),
                    serviceProvider.getServiceContact().getPositionName());
        }
        else {
            assertFalse("The position name of the property 'serviceContact' should not be set",
                    serviceProvider.getServiceContact().isSetPositionName());
        }
        if(props.containsKey("SERVICE_CONTACT_INFO")) {
            assertTrue("The contact info of the property 'serviceContact' should be set",
                    serviceProvider.getServiceContact().isSetContactInfo());

            if(props.containsKey("SERVICE_CONTACT_INFO_ADDRESS")) {
                assertTrue("The contact info address of the property 'serviceContact' should be set",
                        serviceProvider.getServiceContact().getContactInfo().isSetAddress());

                if(props.containsKey("SERVICE_CONTACT_INFO_ADDRESS")) {
                    assertTrue("The contact info address of the property 'serviceContact' should be set",
                            serviceProvider.getServiceContact().getContactInfo().isSetAddress());

                    AddressType address = serviceProvider.getServiceContact().getContactInfo().getAddress();

                    if(props.containsKey("SERVICE_CONTACT_INFO_ADDRESS_DELIVERY_POINT")) {
                        assertTrue("The contact info address delivery point of the property 'serviceContact' should be set",
                                address.isSetDeliveryPoint());
                        String[] deliveryPoints = props.getProperty("SERVICE_CONTACT_INFO_ADDRESS_DELIVERY_POINT").split(",");
                        assertEquals("The delivery point should contains "+deliveryPoints.length+" values",
                                deliveryPoints.length, address.getDeliveryPoint().size());
                        for(String point : deliveryPoints){
                            boolean isPoint = false;
                            for(String pt : address.getDeliveryPoint()){
                                if(pt.equalsIgnoreCase(point)){
                                    isPoint=true;
                                    break;
                                }
                            }
                            assertTrue("The contact info address delivery point of the property 'serviceContact' should" +
                                    " contain the value "+point, isPoint);
                        }
                    }
                    else {
                        assertFalse("The contact info address delivery point of the property 'serviceContact' should not be set",
                                address.isSetDeliveryPoint());
                    }

                    if(props.containsKey("SERVICE_CONTACT_INFO_ADDRESS_CITY")) {
                        assertTrue("The contact info address city of the property 'serviceContact' should be set",
                                address.isSetCity());
                        assertEquals("The contact info address city of the property 'serviceContact' should be "+
                                        props.getProperty("SERVICE_CONTACT_INFO_ADDRESS_CITY"),
                                props.getProperty("SERVICE_CONTACT_INFO_ADDRESS_CITY"), address.getCity());
                    }
                    else {
                        assertFalse("The contact info address city of the property 'serviceContact' should not be set",
                                address.isSetCity());
                    }

                    if(props.containsKey("SERVICE_CONTACT_INFO_ADDRESS_ADMINISTRATIVE_AREA")) {
                        assertTrue("The contact info address administrative area of the property 'serviceContact' should be set",
                                address.isSetAdministrativeArea());
                        assertEquals("The contact info address administrative area of the property 'serviceContact' should be "+
                                        props.getProperty("SERVICE_CONTACT_INFO_ADDRESS_ADMINISTRATIVE_AREA"),
                                props.getProperty("SERVICE_CONTACT_INFO_ADDRESS_ADMINISTRATIVE_AREA"), address.getAdministrativeArea());
                    }
                    else {
                        assertFalse("The contact info address administrative area of the property 'serviceContact' should not be set",
                                address.isSetAdministrativeArea());
                    }

                    if(props.containsKey("SERVICE_CONTACT_INFO_ADDRESS_POSTAL_CODE")) {
                        assertTrue("The contact info address postal code of the property 'serviceContact' should be set",
                                address.isSetPostalCode());
                        assertEquals("The contact info address postal code of the property 'serviceContact' should be "+
                                        props.getProperty("SERVICE_CONTACT_INFO_ADDRESS_POSTAL_CODE"),
                                props.getProperty("SERVICE_CONTACT_INFO_ADDRESS_POSTAL_CODE"), address.getPostalCode());
                    }
                    else {
                        assertFalse("The contact info address postal code of the property 'serviceContact' should not be set",
                                address.isSetPostalCode());
                    }

                    if(props.containsKey("SERVICE_CONTACT_INFO_ADDRESS_COUNTRY")) {
                        assertTrue("The contact info address country of the property 'serviceContact' should be set",
                                address.isSetCountry());
                        assertEquals("The contact info address country of the property 'serviceContact' should be "+
                                        props.getProperty("SERVICE_CONTACT_INFO_ADDRESS_COUNTRY"),
                                props.getProperty("SERVICE_CONTACT_INFO_ADDRESS_COUNTRY"), address.getCountry());
                    }
                    else {
                        assertFalse("The contact info address country of the property 'serviceContact' should not be set",
                                address.isSetCountry());
                    }
                    if(props.containsKey("SERVICE_CONTACT_INFO_ADDRESS_EMAILS")) {
                        assertTrue("The contact info address email of the property 'serviceContact' should be set",
                                address.isSetElectronicMailAddress());
                        String[] emails = props.getProperty("SERVICE_CONTACT_INFO_ADDRESS_EMAILS").split(",");
                        assertEquals("The emails should contains "+emails.length+" values",
                                emails.length, address.getElectronicMailAddress().size());
                        for(String mail : emails){
                            boolean isMail = false;
                            for(String m : address.getElectronicMailAddress()){
                                if(m.equalsIgnoreCase(mail)){
                                    isMail=true;
                                    break;
                                }
                            }
                            assertTrue("The contact info address mail of the property 'serviceContact' should" +
                                    " contain the value "+mail, isMail);
                        }
                    }
                    else {
                        assertFalse("The contact info address email of the property 'serviceContact' should not be set",
                                address.isSetElectronicMailAddress());
                    }
                }
                else {
                    assertFalse("The contact info address of the property 'serviceContact' should not be set",
                            serviceProvider.getServiceContact().getContactInfo().isSetAddress());
                }
            }
            else {
                assertFalse("The contact info address of the property 'serviceContact' should not be set",
                        serviceProvider.getServiceContact().getContactInfo().isSetAddress());
            }
            if(props.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE")) {
                assertTrue("The contact info online resource of the property 'serviceContact' should be set",
                        serviceProvider.getServiceContact().getContactInfo().isSetOnlineResource());
                if(props.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_HREF")) {
                    assertTrue("The href of the online resource should be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetHref());
                    assertEquals("The href of the online resource should be " +
                                    props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_HREF"), props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_HREF"),
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().getHref());
                }
                else{
                    assertFalse("The href of the online resource should not be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetHref());
                }
                if(props.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ROLE")) {
                    assertTrue("The role of the online resource should be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetRole());
                    assertEquals("The role of the online resource should be " +
                                    props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ROLE"), props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ROLE"),
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().getRole());
                }
                else{
                    assertFalse("The role of the online resource should not be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetRole());
                }
                if(props.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ARCROLE")) {
                    assertTrue("The arcrole of the online resource should be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetArcrole());
                    assertEquals("The arcrole of the online resource should be " +
                                    props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ARCROLE"), props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ARCROLE"),
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().getArcrole());
                }
                else{
                    assertFalse("The arcrole of the online resource should not be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetArcrole());
                }
                if(props.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_TITLE")) {
                    assertTrue("The title of the online resource should be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetTitle());
                    assertEquals("The title of the online resource should be " +
                                    props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_TITLE"), props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_TITLE"),
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().getTitle());
                }
                else{
                    assertFalse("The title of the online resource should not be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetTitle());
                }
                if(props.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_SHOW")) {
                    assertTrue("The show of the online resource should be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetShow());
                    assertEquals("The show of the online resource should be " +
                                    props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_SHOW"), props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_SHOW"),
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().getShow().value());
                }
                else{
                    assertFalse("The show of the online resource should not be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetShow());
                }
                if(props.containsKey("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ACTUATE")) {
                    assertTrue("The actuate of the online resource should be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetActuate());
                    assertEquals("The actuate of the online resource should be " +
                                    props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ACTUATE"),
                            props.getProperty("SERVICE_CONTACT_INFO_ONLINE_RESOURCE_ACTUATE"),
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().getActuate().value());
                }
                else{
                    assertFalse("The actuate of the online resource should not be set",
                            serviceProvider.getServiceContact().getContactInfo().getOnlineResource().isSetActuate());
                }
            }
            else {
                assertFalse("The contact info online resource of the property 'serviceContact' should not be set",
                        serviceProvider.getServiceContact().getContactInfo().isSetOnlineResource());
            }
            if(props.containsKey("SERVICE_CONTACT_INFO_HOURS_OF_SERVICE")) {
                assertTrue("The contact info hours of service of the property 'serviceContact' should be set",
                        serviceProvider.getServiceContact().getContactInfo().isSetHoursOfService());
                assertEquals("The contact info hours of service of the property 'serviceContact' should be "+
                                props.getProperty("SERVICE_CONTACT_INFO_HOURS_OF_SERVICE"),
                        props.getProperty("SERVICE_CONTACT_INFO_HOURS_OF_SERVICE"),
                        serviceProvider.getServiceContact().getContactInfo().getHoursOfService());
            }
            else {
                assertFalse("The contact info hour of service of the property 'serviceContact' should not be set",
                        serviceProvider.getServiceContact().getContactInfo().isSetHoursOfService());
            }
            if(props.containsKey("SERVICE_CONTACT_INFO_INSTRUCTIONS")) {
                assertTrue("The contact info instructions of the property 'serviceContact' should be set",
                        serviceProvider.getServiceContact().getContactInfo().isSetContactInstructions());
                assertEquals("The contact info instructions of the property 'serviceContact' should be "+
                                props.getProperty("SERVICE_CONTACT_INFO_INSTRUCTIONS"),
                        props.getProperty("SERVICE_CONTACT_INFO_INSTRUCTIONS"),
                        serviceProvider.getServiceContact().getContactInfo().getContactInstructions());
            }
            else {
                assertFalse("The contact info instructions of the property 'serviceContact' should not be set",
                        serviceProvider.getServiceContact().getContactInfo().isSetContactInstructions());
            }
        }
        else {
            assertFalse("The contact info of the property 'serviceContact' should not be set",
                    serviceProvider.getServiceContact().isSetContactInfo());
        }
        if(props.containsKey("SERVICE_CONTACT_ROLE")) {
            assertTrue("The service contact role should be set",
                    serviceProvider.getServiceContact().isSetRole());
            assertEquals("The service contact role value should be " + props.getProperty("SERVICE_CONTACT_ROLE"),
                    props.getProperty("SERVICE_CONTACT_ROLE"), serviceProvider.getServiceContact().getRole().getValue());
        }
        else{
            assertFalse("The service contact role should not be set",
                    serviceProvider.getServiceContact().isSetRole());
        }
    }

    /**
     * Test the answer of the server to a GetCapabilities request with the 'sections' property set to
     * 'OperationMetadata'.
     * <p>
     * The capabilities answer should contains only the mandatory and 'OperationMetadata' property set.
     */
    @Test
    public void testGetCapabilitiesSectionOperationMetadata() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        SectionsType sectionsType = new SectionsType();
        sectionsType.getSection().add("OperationMetadata");
        getCapabilities.setSections(sectionsType);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object
        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        assertFalse("The 'serviceIdentification' field should be set.", capabilities.isSetServiceIdentification());
        assertFalse("The 'serviceProvider' property should not be set.", capabilities.isSetServiceProvider());
        assertTrue("The 'operationMetadata' property should not be set.", capabilities.isSetOperationsMetadata());
        assertFalse("The 'contents' property should not be set.", capabilities.isSetContents());
        assertFalse("The 'languages' property should not be set.", capabilities.isSetLanguages());

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'operationsMetadata' property
        OperationsMetadata operationsMetadata = capabilities.getOperationsMetadata();

        assertTrue("The property 'operation' should be set", operationsMetadata.isSetOperation());
        assertEquals("The 'operation' property should contains 6 operations", 6,
                operationsMetadata.getOperation().size());
        List<String> operationNames = Arrays.asList("GETCAPABILITIES", "DESCRIBEPROCESS", "EXECUTE", "GETSTATUS",
                "GETRESULT", "DISMISS");
        for(String opName : operationNames) {
            Operation operation = null;
            for (Operation op : operationsMetadata.getOperation()) {
                if(op.getName().equalsIgnoreCase(opName)){
                    operation = op;
                    break;
                }
            }
            assertNotNull("The property 'operationMetadata' should contains the operation "+opName, operation);
            if(props.containsKey(opName+"_GET") || props.containsKey(opName+"_POST")){
                assertTrue("The property 'dcp' should be set for the operation "+opName, operation.isSetDCP());
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The 'http' property of the DPC number "+operation.getDCP().indexOf(dcp)+
                            " of the operation "+opName+" should be set", dcp.isSetHTTP());
                }
            }
            else{
                assertFalse("The property 'dcp' should not be set for the operation "+opName, operation.isSetDCP());
            }
        }
    }

    /**
     * Test the answer of the server to a GetCapabilities request with the 'sections' property set to
     * 'Contents'.
     * <p>
     * The capabilities answer should contains only the mandatory and 'Contents' property set.
     */
    @Test
    public void testGetCapabilitiesSectionContents() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        SectionsType sectionsType = new SectionsType();
        //ServiceIdentification, ServiceProvider, OperationMetadata, Contents, Languages, All
        sectionsType.getSection().add("Contents");
        getCapabilities.setSections(sectionsType);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object
        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        assertFalse("The 'serviceIdentification' field should be set.", capabilities.isSetServiceIdentification());
        assertFalse("The 'serviceProvider' property should not be set.", capabilities.isSetServiceProvider());
        assertFalse("The 'operationMetadata' property should not be set.", capabilities.isSetOperationsMetadata());
        assertTrue("The 'contents' property should not be set.", capabilities.isSetContents());
        assertFalse("The 'languages' property should not be set.", capabilities.isSetLanguages());

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'operationsMetadata' property
        Contents contents = capabilities.getContents();

        assertTrue("The 'processSummary' property should be set", contents.isSetProcessSummary());

        for(ProcessSummaryType processSummaryType : contents.getProcessSummary()){
            assertTrue("The process should contains a title", processSummaryType.isSetTitle());
            assertTrue("The process should contains a identifier", processSummaryType.isSetIdentifier());
            assertTrue("The process should contains job control options", processSummaryType.isSetJobControlOptions());
            String[] options = props.getProperty("JOB_CONTROL_OPTIONS").split(",");
            for(String option : options) {
                assertTrue("The process should contains the job control option "+option,
                        processSummaryType.getJobControlOptions().contains(option));
            }
            assertTrue("The process should contains output transmission", processSummaryType.isSetOutputTransmission());
            String[] transmissions = props.getProperty("DATA_TRANSMISSION_TYPE").split(",");
            for(String transmission : transmissions) {
                assertTrue("The process should contains the job control option "+transmission.toUpperCase(),
                        processSummaryType.getOutputTransmission().contains(DataTransmissionModeType.valueOf(transmission.toUpperCase())));
            }
        }
    }

    /**
     * Test the answer of the server to a GetCapabilities request with the 'sections' property set to
     * 'Languages'.
     * <p>
     * The capabilities answer should contains only the mandatory and 'Languages' property set.
     */
    @Test
    public void testGetCapabilitiesSectionLanguages() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        SectionsType sectionsType = new SectionsType();
        //ServiceIdentification, ServiceProvider, OperationMetadata, Contents, Languages, All
        sectionsType.getSection().add("Languages");
        getCapabilities.setSections(sectionsType);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object

        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        assertFalse("The 'serviceIdentification' field should be set.", capabilities.isSetServiceIdentification());
        assertFalse("The 'serviceProvider' property should not be set.", capabilities.isSetServiceProvider());
        assertFalse("The 'operationMetadata' property should not be set.", capabilities.isSetOperationsMetadata());
        assertFalse("The 'contents' property should not be set.", capabilities.isSetContents());
        assertTrue("The 'languages' property should not be set.", capabilities.isSetLanguages());

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'languages' property
        CapabilitiesBaseType.Languages languages = capabilities.getLanguages();

        assertTrue("At least one language should be set", languages.isSetLanguage());
        String[] propsLanguages = props.getProperty("SUPPORTED_LANGUAGES").split(",");
        for(String propsLanguage : propsLanguages) {
            assertTrue("The 'languages' property should contains the language "+propsLanguage,
                    languages.getLanguage().contains(propsLanguage));
        }
    }

    /**
     * Test the answer of the server to a GetCapabilities request with the 'sections' property set to a bad value
     * <p>
     * The capabilities answer should be an Exception report with the code 'InvalidParameterValue'.
     */
    @Test
    public void testGetCapabilitiesBadSection() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        SectionsType sectionsType = new SectionsType();
        //ServiceIdentification, ServiceProvider, OperationMetadata, Contents, Languages, All
        sectionsType.getSection().add("IAmAUnicorn");
        getCapabilities.setSections(sectionsType);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the ExceptionReport object
        if (!(result instanceof ExceptionReport)) {
            fail("The result object should be a ExceptionReport");
        }
        ExceptionReport exceptionReport = (ExceptionReport)result;
        if(exceptionReport.isSetException()){
            for(ExceptionType exceptionType : exceptionReport.getException()){
                if(!exceptionType.isSetExceptionCode()){
                    assertFalse("The ExceptionType should contains an exception code", exceptionType.isSetExceptionCode());
                }
                else if(!exceptionType.isSetLocator()){
                    assertFalse("The ExceptionType should contains a locator", exceptionType.isSetLocator());
                }
                else{
                    assertEquals("The exception code should be InvalidParameterValue",
                            "InvalidParameterValue", exceptionType.getExceptionCode());
                    assertEquals("The exception locator should be Sections:IAmAUnicorn",
                            "Sections:IAmAUnicorn", exceptionType.getLocator());
                }
            }
        }
        else{
            fail("The ExceptionReport should contains exceptions");
        }
    }

    /**
     * Test the answer of the service to a GetCapabilities request with the 'sections' property set to
     * 'All'.
     * <p>
     * The capabilities answer should contains only the mandatory and 'All' property set.
     */
    @Test
    public void testGetCapabilitiesSectionAll() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        SectionsType sectionsType = new SectionsType();
        //ServiceIdentification, ServiceProvider, OperationMetadata, Contents, Languages, All
        sectionsType.getSection().add("All");
        getCapabilities.setSections(sectionsType);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object

        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        assertTrue("The 'serviceIdentification' field should be set.", capabilities.isSetServiceIdentification());
        assertTrue("The 'serviceProvider' property should not be set.", capabilities.isSetServiceProvider());
        assertTrue("The 'operationMetadata' property should not be set.", capabilities.isSetOperationsMetadata());
        assertTrue("The 'contents' property should not be set.", capabilities.isSetContents());
        assertTrue("The 'languages' property should not be set.", capabilities.isSetLanguages());

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'content' property
        testBasicsContent(capabilities);
    }

    /**
     * Test the answer of the service to a GetCapabilities request with the 'acceptFormat' property set to 'text/xml'
     *
     * The capabilities answer should be a valid xml document
     */
    @Test
    public void testGetCapabilitiesAcceptFormat() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        AcceptFormatsType acceptFormatsType = new AcceptFormatsType();;
        acceptFormatsType.getOutputFormat().add("text/xml");
        getCapabilities.setAcceptFormats(acceptFormatsType);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            marshaller.marshal(factory.createGetCapabilities(getCapabilities), out);
        } catch (JAXBException e) {
            fail("Exception get on marshalling the request :\n" + e.getLocalizedMessage());
        }
        InputStream in = new DataInputStream(new ByteArrayInputStream(out.toByteArray()));
        ByteArrayOutputStream xml = (ByteArrayOutputStream) service.callOperation(in);
        ByteArrayInputStream resultXml = new ByteArrayInputStream(xml.toByteArray());
        byte[] expectedByteArray = "<?xml version=\"1.0\"".getBytes();
        byte[] resultByteArray = new byte[expectedByteArray.length];
        int result = resultXml.read(resultByteArray, 0, expectedByteArray.length);
        assertTrue("The result document should be readable", result==expectedByteArray.length);
        assertArrayEquals("The result document does not seems to be an 'text/xml' document",
                expectedByteArray, resultByteArray);
    }

    /**
     * Test the answer of the service to a GetCapabilities request with the 'acceptFormat' property set to a wrong
     * mime type
     *
     * The capabilities answer should be a valid xml document
     */
    @Test
    public void testGetCapabilitiesBadAcceptFormat() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        AcceptFormatsType acceptFormatsType = new AcceptFormatsType();;
        acceptFormatsType.getOutputFormat().add("unicorn/mime/type");
        getCapabilities.setAcceptFormats(acceptFormatsType);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            marshaller.marshal(factory.createGetCapabilities(getCapabilities), out);
        } catch (JAXBException e) {
            fail("Exception get on marshalling the request :\n" + e.getLocalizedMessage());
        }
        InputStream in = new DataInputStream(new ByteArrayInputStream(out.toByteArray()));
        ByteArrayOutputStream xml = (ByteArrayOutputStream) service.callOperation(in);
        ByteArrayInputStream resultXml = new ByteArrayInputStream(xml.toByteArray());
        byte[] expectedByteArray = "<?xml version=\"1.0\"".getBytes();
        byte[] resultByteArray = new byte[expectedByteArray.length];
        int result = resultXml.read(resultByteArray, 0, expectedByteArray.length);
        assertTrue("The result document should be readable", result==expectedByteArray.length);
        assertArrayEquals("The result document does not seems to be an 'text/xml' document",
                expectedByteArray, resultByteArray);
    }

    /**
     * Test the answer of a service to a GetCapabilities request with the 'language' property set to '*'
     *
     * The property 'processSummary' of the answer should contains the human readable text in all the server language
     */
    @Test
    public void testGetCapabilitiesAllLanguage() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        net.opengis.ows._2.GetCapabilitiesType.AcceptLanguages acceptLanguages =
                new net.opengis.ows._2.GetCapabilitiesType.AcceptLanguages();
        acceptLanguages.getLanguage().add("*");
        getCapabilities.setAcceptLanguages(acceptLanguages);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object

        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'content' property
        testBasicsContent(capabilities);

        assertTrue("The property 'content' should be set.", capabilities.isSetContents());
        assertTrue("The property 'processummary' should be set.", capabilities.getContents().isSetProcessSummary());
        assertTrue("The property 'languages' should be set.", capabilities.isSetLanguages());
        List<String> languages = capabilities.getLanguages().getLanguage();
        for(ProcessSummaryType process : capabilities.getContents().getProcessSummary()){
            assertTrue("The property title should be set.", process.isSetTitle());
            for(String language : languages){
                boolean isTitleLanguage = false;
                for(LanguageStringType stringType : process.getTitle()){
                    if(language.equalsIgnoreCase(stringType.getLang())){
                        isTitleLanguage = true;
                    }
                }
                assertTrue("There should be a title with the language "+language, isTitleLanguage);

                boolean isAbstrLanguage = false;
                for(LanguageStringType stringType : process.getAbstract()){
                    if(language.equalsIgnoreCase(stringType.getLang())){
                        isAbstrLanguage = true;
                    }
                }
                assertTrue("There should be an abstract with the language "+language, isAbstrLanguage);

                for(KeywordsType keywordsType : process.getKeywords()){
                    boolean isKeywordLanguage = false;
                    for(LanguageStringType stringType : keywordsType.getKeyword()){
                        if(language.equalsIgnoreCase(stringType.getLang())) {
                            isKeywordLanguage = true;
                        }
                    }
                    assertTrue("There should be a keyword with the language "+language, isKeywordLanguage);
                }
            }
        }
    }

    /**
     * Test the answer of a service to a GetCapabilities request with the 'language' property set to 'en'
     *
     * The property 'processSummary' of the answer should contains the human readable text in the language 'en'
     */
    @Test
    public void testGetCapabilitiesOneLanguage() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        net.opengis.ows._2.GetCapabilitiesType.AcceptLanguages acceptLanguages =
                new net.opengis.ows._2.GetCapabilitiesType.AcceptLanguages();
        acceptLanguages.getLanguage().add("en");
        getCapabilities.setAcceptLanguages(acceptLanguages);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object

        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'content' property
        testBasicsContent(capabilities);

        assertTrue("The property 'content' should be set.", capabilities.isSetContents());
        assertTrue("The property 'processummary' should be set.", capabilities.getContents().isSetProcessSummary());
        assertTrue("The property 'languages' should be set.", capabilities.isSetLanguages());
        List<String> languages = capabilities.getLanguages().getLanguage();
        for(ProcessSummaryType process : capabilities.getContents().getProcessSummary()){
            assertTrue("The property title should be set.", process.isSetTitle());

            assertEquals("There should be only one title", 1,  process.getTitle().size());
            assertEquals("There should be a title with the language en", "en", process.getTitle().get(0).getLang());

            assertEquals("There should be only one abstract", 1,  process.getAbstract().size());
            assertEquals("There should be an abstract with the language en", "en", process.getAbstract().get(0).getLang());

            for(KeywordsType keywordsType : process.getKeywords()) {
                assertEquals("There should be only one keyword language", 1, keywordsType.getKeyword().size());
                assertEquals("There should be a keyword with the language en", "en", keywordsType.getKeyword().get(0).getLang());
            }
        }
    }

    /**
     * Test the answer of a service to a GetCapabilities request with the 'language' property set to 'fr'
     *
     * The property 'processSummary' of the answer should contains the human readable text in the language 'fr-fr'
     */
    @Test
    public void testGetCapabilitiesBestEffortLanguage() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        net.opengis.ows._2.GetCapabilitiesType.AcceptLanguages acceptLanguages =
                new net.opengis.ows._2.GetCapabilitiesType.AcceptLanguages();
        acceptLanguages.getLanguage().add("fr");
        getCapabilities.setAcceptLanguages(acceptLanguages);

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object

        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'content' property
        testBasicsContent(capabilities);

        assertTrue("The property 'content' should be set.", capabilities.isSetContents());
        assertTrue("The property 'processummary' should be set.", capabilities.getContents().isSetProcessSummary());
        assertTrue("The property 'languages' should be set.", capabilities.isSetLanguages());
        List<String> languages = capabilities.getLanguages().getLanguage();
        for(ProcessSummaryType process : capabilities.getContents().getProcessSummary()){
            assertTrue("The property title should be set.", process.isSetTitle());

            assertEquals("There should be only one title", 1,  process.getTitle().size());
            assertEquals("There should be a title with the language fr-fr", "fr-fr", process.getTitle().get(0).getLang());

            assertEquals("There should be only one abstract", 1,  process.getAbstract().size());
            assertEquals("There should be an abstract with the language fr-fr", "fr-fr", process.getAbstract().get(0).getLang());

            for(KeywordsType keywordsType : process.getKeywords()) {
                assertEquals("There should be only one keyword language", 1, keywordsType.getKeyword().size());
                assertEquals("There should be a keyword with the language fr-fr", "fr-fr", keywordsType.getKeyword().get(0).getLang());
            }
        }
    }

    /**
     * Test the answer of a service to a GetCapabilities request
     *
     * The property 'extension' of the answer should contains the value 'DISMISS'
     */
    @Test
    public void testGetCapabilitiesExtension() throws Exception {

        //Generate the request object
        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();

        //Send the request to the service
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities));
        if (result == null) {
            fail();
        }

        //Get the WPSCapabilitiesType object

        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof JAXBElement)) {
            fail("The result object should be a JAXBElement");
        }
        JAXBElement element = (JAXBElement) result;

        if (!(element.getValue() instanceof WPSCapabilitiesType)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();

        //Test the 'version' property
        assertTrue("The 'version' property should be set", capabilities.isSetVersion());

        //Test the 'service' property
        //By default the 'service' property is set to 'WPS'
        assertTrue(true);

        //Test the 'content' property
        testBasicsContent(capabilities);

        //Nothing more to test
    }
}
