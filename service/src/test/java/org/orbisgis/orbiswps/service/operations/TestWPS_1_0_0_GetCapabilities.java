package org.orbisgis.orbiswps.service.operations;

import net.opengis.ows._1.*;
import net.opengis.wps._1_0_0.GetCapabilities;
import net.opengis.wps._1_0_0.ProcessBriefType;
import net.opengis.wps._1_0_0.WPSCapabilitiesType;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.service.process.ProcessManager;
import org.orbisgis.orbiswps.serviceapi.operations.WPS_1_0_0_Operations;
import org.w3._1999.xlink.ActuateType;
import org.w3._1999.xlink.ShowType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Test class for the WPS_2_0_OperationsImpl
 *
 * @author Sylvain PALOMINOS
 */
public class TestWPS_1_0_0_GetCapabilities {

    /** Wps Operation object. */
    private WPS_1_0_0_Operations minWps100Operations;
    private WPS_1_0_0_Operations fullWps100Operations;

    /**
     * Initialize a wps service for processing all the tests.
     */
    @Before
    public void initialize() {

        WpsServerImpl wpsServer = new WpsServerImpl();
        ProcessManager processManager = new ProcessManager(null, wpsServer);
        try {
            URL url = this.getClass().getResource("simpleScript.groovy");
            assertNotNull("Unable to load the script 'simpleScript.groovy'", url);
            File f = new File(url.toURI());
            wpsServer.addProcess(f);
            processManager.addScript(f.toURI());
        } catch (URISyntaxException e) {
            fail("Error on loading the scripts : "+e.getMessage());
        }

        assertNotNull("Unable to load the file 'minWpsService100.json'",
                TestWPS_1_0_0_GetCapabilities.class.getResource("minWpsService100.json").getFile());
        WpsServerProperties_1_0_0 minWpsProps = new WpsServerProperties_1_0_0(
                TestWPS_1_0_0_GetCapabilities.class.getResource("minWpsService100.json").getFile());
        minWps100Operations =  new WPS_1_0_0_OperationsImpl(wpsServer, minWpsProps, processManager);

        assertNotNull("Unable to load the file 'fullWpsService100.json'",
                TestWPS_1_0_0_GetCapabilities.class.getResource("fullWpsService100.json").getFile());
        WpsServerProperties_1_0_0 fullWpsProps = new WpsServerProperties_1_0_0(
                TestWPS_1_0_0_GetCapabilities.class.getResource("fullWpsService100.json").getFile());
        fullWps100Operations =  new WPS_1_0_0_OperationsImpl(wpsServer, fullWpsProps, processManager);
    }

    /**
     * Test an empty GetCapabilities request with a full WPS property
     */
    @Test
    public void testFullEmptyGetCapabilities(){
        //Ask for the GetCapabilities
        GetCapabilities getCapabilities = new GetCapabilities();
        Object object = fullWps100Operations.getCapabilities(getCapabilities);
        assertTrue("The wps service answer should be 'WPSCapabilitiesType",object instanceof WPSCapabilitiesType);
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType)object;
        
        //version tests
        assertTrue("The wps service 'version' should be set", capabilities.isSetVersion());
        assertEquals("The wps service 'version' should be '1.0.0'", "1.0.0", capabilities.getVersion());

        //update sequence tests
        assertTrue("The wps service 'updateSequence' should be set", capabilities.isSetUpdateSequence());
        assertEquals("The wps service 'updateSequence' should be '1.0.0'", "1.0.0", capabilities.getUpdateSequence());

        //lang tests
        assertTrue("The wps service 'lang' should be set", capabilities.isSetLang());
        assertEquals("The wps service 'lang' should be 'en'", "en", capabilities.getLang());

        testFullServiceIdentification(capabilities);
        testFullServiceProvider(capabilities);
        testFullOperationMetadata(capabilities);

        //WSDL test
        assertTrue("The wps service 'WSDL' should be set", capabilities.isSetWSDL());
        assertTrue("The wps service 'WSDL' 'href' should be set", capabilities.getWSDL().isSetHref());
        assertEquals("The wps service 'WSDL' 'href' should be set to 'href'", "href", capabilities.getWSDL().getHref());

        //operation metadata tests

        //language tests
        assertTrue("The wps service 'languages' should be set",
                capabilities.isSetLanguages());
        assertTrue("The wps service 'languages' 'default' should be set",
                capabilities.getLanguages().isSetDefault());
        assertTrue("The wps service 'languages' 'default' 'language' should be set",
                capabilities.getLanguages().getDefault().isSetLanguage());
        assertEquals("The wps service 'languages' 'default' 'language' should be 'en'",
                "en", capabilities.getLanguages().getDefault().getLanguage());
        assertTrue("The wps service 'languages' 'supported' should be set",
                capabilities.getLanguages().isSetSupported());
        assertTrue("The wps service 'languages' 'supported' 'language' should be set",
                capabilities.getLanguages().getSupported().isSetLanguage());
        assertArrayEquals("The wps service 'languages' 'supported' 'language' should be set to [en, fr-fr]",
                new String[]{"en", "fr-fr"}, capabilities.getLanguages().getSupported().getLanguage().toArray());

        //ProcessOffering test
        assertTrue("The wps service 'processOffering' should be set", capabilities.isSetProcessOfferings());
        assertTrue("The wps service 'processOffering' should be set", capabilities.getProcessOfferings().isSetProcess());
        for(ProcessBriefType process : capabilities.getProcessOfferings().getProcess()){
            assertTrue("The process 'id' should be set", process.isSetIdentifier());
            assertTrue("The process 'title' should be set", process.isSetTitle());
        }
    }

    /**
     * Test a GetCapabilities request with a full WPS property
     */
    @Test
    public void testFullGetCapabilities(){
        //Ask for the GetCapabilities
        GetCapabilities getCapabilities = new GetCapabilities();
        getCapabilities.setLanguage("en");
        AcceptVersionsType acceptVersionsType = new AcceptVersionsType();
        acceptVersionsType.getVersion().add("1.0.0");
        getCapabilities.setAcceptVersions(acceptVersionsType);
        Object object = fullWps100Operations.getCapabilities(getCapabilities);
        assertTrue("The wps service answer should be 'WPSCapabilitiesType",object instanceof WPSCapabilitiesType);
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType)object;

        //version tests
        assertTrue("The wps service 'version' should be set", capabilities.isSetVersion());
        assertEquals("The wps service 'version' should be '1.0.0'", "1.0.0", capabilities.getVersion());

        //update sequence tests
        assertTrue("The wps service 'updateSequence' should be set", capabilities.isSetUpdateSequence());
        assertEquals("The wps service 'updateSequence' should be '1.0.0'", "1.0.0", capabilities.getUpdateSequence());

        //lang tests
        assertTrue("The wps service 'lang' should be set", capabilities.isSetLang());
        assertEquals("The wps service 'lang' should be 'en'", "en", capabilities.getLang());

        testFullServiceIdentification(capabilities);
        testFullServiceProvider(capabilities);
        testFullOperationMetadata(capabilities);

        //WSDL test
        assertTrue("The wps service 'WSDL' should be set", capabilities.isSetWSDL());
        assertTrue("The wps service 'WSDL' 'href' should be set", capabilities.getWSDL().isSetHref());
        assertEquals("The wps service 'WSDL' 'href' should be set to 'href'", "href", capabilities.getWSDL().getHref());

        //operation metadata tests

        //language tests
        assertTrue("The wps service 'languages' should be set",
                capabilities.isSetLanguages());
        assertTrue("The wps service 'languages' 'default' should be set",
                capabilities.getLanguages().isSetDefault());
        assertTrue("The wps service 'languages' 'default' 'language' should be set",
                capabilities.getLanguages().getDefault().isSetLanguage());
        assertEquals("The wps service 'languages' 'default' 'language' should be 'en'",
                "en", capabilities.getLanguages().getDefault().getLanguage());
        assertTrue("The wps service 'languages' 'supported' should be set",
                capabilities.getLanguages().isSetSupported());
        assertTrue("The wps service 'languages' 'supported' 'language' should be set",
                capabilities.getLanguages().getSupported().isSetLanguage());
        assertArrayEquals("The wps service 'languages' 'supported' 'language' should be set to [en, fr-fr]",
                new String[]{"en", "fr-fr"}, capabilities.getLanguages().getSupported().getLanguage().toArray());

        //ProcessOffering test
        assertTrue("The wps service 'processOffering' should be set", capabilities.isSetProcessOfferings());
        assertTrue("The wps service 'processOffering' should be set", capabilities.getProcessOfferings().isSetProcess());
        for(ProcessBriefType process : capabilities.getProcessOfferings().getProcess()){
            assertTrue("The process 'id' should be set", process.isSetIdentifier());
            assertTrue("The process 'title' should be set", process.isSetTitle());
        }
    }

    /**
     * Test the given getCapabilities OperationMetadata sections.
     * @param capabilities Object to test
     */
    private void testFullOperationMetadata(WPSCapabilitiesType capabilities){
        assertTrue("The wps service 'operationMetadata' should be set",
                capabilities.isSetOperationsMetadata());
        assertTrue("The wps service operation metadata operation should be set",
                capabilities.getOperationsMetadata().isSetOperation());
        assertEquals("The wps service operation metadata operation should contains three elements",
                3, capabilities.getOperationsMetadata().getOperation().size());
        boolean isGetCapabilities = false;
        boolean isDescribeProcess = false;
        boolean isExecute = false;
        for(Operation operation : capabilities.getOperationsMetadata().getOperation()){
            assertTrue(operation.isSetName());
            if("GetCapabilities".equals(operation.getName())){
                assertTrue("The operation 'GetCapabilities' 'dcp' should be set", operation.isSetDCP());
                assertFalse("The operation 'GetCapabilities' 'dcp' should not be empty", operation.getDCP().isEmpty());
                boolean isUrl1 = false;
                boolean isUrl2 = false;
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' should be set", dcp.isSetHTTP());
                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' should not be empty",
                            dcp.getHTTP().getGetOrPost().isEmpty());
                    for(JAXBElement<RequestMethodType> element : dcp.getHTTP().getGetOrPost()){
                        RequestMethodType requestMethodType = element.getValue();
                        assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'href' should be set",
                                requestMethodType.isSetHref());
                        if("getcapabilitiesurl1".equals(requestMethodType.getHref())){
                            assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'role' should be set",
                                    requestMethodType.isSetRole());
                            assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'role' should be set to 'role'",
                                    "role", requestMethodType.getRole());
                            assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'arcrole' should be set",
                                    requestMethodType.isSetArcrole());
                            assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'arcrole' should be set to 'arcrole'",
                                    "arcrole", requestMethodType.getArcrole());
                            assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'show' should be set",
                                    requestMethodType.isSetShow());
                            assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'show' should be set to 'NONE'",
                                    ShowType.NONE, requestMethodType.getShow());
                            assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'actuate' should be set",
                                    requestMethodType.isSetActuate());
                            assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'actuate' should be set to 'NONE'",
                                    ActuateType.NONE, requestMethodType.getActuate());
                            assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'title' should be set",
                                    requestMethodType.isSetTitle());
                            assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'title' should be set to 'title'",
                                    "title", requestMethodType.getTitle());

                            assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' should be set",
                                    requestMethodType.isSetConstraint());
                            boolean isAnyValue = false;
                            boolean isNoValues = false;
                            boolean isValuesReference = false;
                            boolean isAllowedValues = false;
                            for(DomainType domainType : requestMethodType.getConstraint()){
                                if(domainType.isSetAllowedValues()){
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'name' should be set", domainType.isSetName());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint'" +
                                            " 'name' should be set to cstr1", "cstr1", domainType.getName());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'anyValue' should not be set", domainType.isSetAnyValue());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'noValues' should not be set", domainType.isSetNoValues());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'valuesReference' should not be set", domainType.isSetValuesReference());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'allowedValues' should be set", domainType.isSetAllowedValues());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                            "'valueOrRange' should be set", domainType.getAllowedValues().isSetValueOrRange());
                                    boolean isValue1 = false;
                                    boolean isValue2 = false;
                                    boolean isRange = false;
                                    for(Object valueOrRange : domainType.getAllowedValues().getValueOrRange()){
                                        if(valueOrRange instanceof ValueType){
                                            ValueType value = (ValueType)valueOrRange;
                                            assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                                    "'valueOrRange' 'value' 'value' should be set", value.isSetValue());
                                            if("value1".equals(value.getValue())){
                                                isValue1 = true;
                                            }
                                            else if("value2".equals(value.getValue())){
                                                isValue2 = true;
                                            }
                                        }
                                        else if(valueOrRange instanceof RangeType){
                                            RangeType range = (RangeType)valueOrRange;
                                            assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                                    "'valueOrRange' 'range' 'maximumValue' should be set", range.isSetMaximumValue());
                                            assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                                    "'valueOrRange' 'range' 'maximumValue' should be set to '10'", "10", range.getMaximumValue().getValue());
                                            assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                                    "'valueOrRange' 'range' 'minimumValue' should be set", range.isSetMinimumValue());
                                            assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                                    "'valueOrRange' 'range' 'minimumValue' should be set to 'O'", "0", range.getMinimumValue().getValue());
                                            assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                                    "'valueOrRange' 'range' 'spacing' should be set", range.isSetSpacing());
                                            assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                                    "'valueOrRange' 'range' 'spacing' should be set to '2'", "2", range.getSpacing().getValue());
                                            assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                                    "'valueOrRange' 'range' 'rangeClosure' should be set", range.isSetRangeClosure());
                                            assertArrayEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                                    "'valueOrRange' 'range' 'rangeClosure' should be set to [closed]", new String[]{"closed"}, range.getRangeClosure().toArray());

                                            isRange = true;
                                        }
                                        else{
                                            fail("Unknown value or range");
                                        }
                                    }
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                            "'valueOrRange' 'value' 'value1' not found", isValue1);
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                            "'valueOrRange' 'value' 'value2' not found", isValue2);
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'allowedValues'" +
                                            "'valueOrRange' 'range' 'range' not found", isRange);

                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'defaultValue' shoudl be set",
                                            domainType.isSetDefaultValue());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint'" +
                                            " 'defaultValue' should be set to 'dfltvalue'", "dfltvalue", domainType.getDefaultValue().getValue());

                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'meaning' should be set",
                                            domainType.isSetMeaning());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'meaning' 'value' should be set",
                                            domainType.getMeaning().isSetValue());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint'" +
                                            " 'meaning' 'value' should be set to 'name'", "name", domainType.getMeaning().getValue());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'meaning' 'reference' should be set",
                                            domainType.getMeaning().isSetReference());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint'" +
                                            " 'meaning' 'reference' should be set to 'uri'", "uri", domainType.getMeaning().getReference());

                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'dataType' should be set",
                                            domainType.isSetDataType());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'dataType' 'value' should be set",
                                            domainType.getDataType().isSetValue());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint'" +
                                            " 'dataType' 'value' should be set to 'name'", "name", domainType.getDataType().getValue());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'meaning' 'reference' should be set",
                                            domainType.getDataType().isSetReference());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint'" +
                                            " 'dataType' 'reference' should be set to 'uri'", "uri", domainType.getDataType().getReference());

                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'uom' should be set",
                                            domainType.isSetUOM());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'uom' 'value'" +
                                            " should be set", domainType.getUOM().isSetValue());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'uom' 'value'" +
                                            " should be set to 'name'", "name", domainType.getUOM().getValue());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'uom' 'reference'" +
                                            " should be set", domainType.getUOM().isSetReference());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'uom' 'reference'" +
                                            " should be set to 'uri'", "uri", domainType.getUOM().getReference());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'referenceSystem' should be set",
                                            domainType.isSetReferenceSystem());

                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' " +
                                            "should be set", domainType.isSetMetadata());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' should have one value",
                                            1, domainType.getMetadata().size());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' 'role' should be set",
                                            domainType.getMetadata().get(0).isSetRole());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' 'role' should be set to 'role'",
                                            "role", domainType.getMetadata().get(0).getRole());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' 'arcrole' should be set",
                                            domainType.getMetadata().get(0).isSetArcrole());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' 'arcrole' should be set to 'arcrole'",
                                            "arcrole", domainType.getMetadata().get(0).getArcrole());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' 'show' should be set",
                                            domainType.getMetadata().get(0).isSetShow());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' 'show' should be set to 'NONE'",
                                            ShowType.NONE, domainType.getMetadata().get(0).getShow());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' 'actuate' should be set",
                                            domainType.getMetadata().get(0).isSetActuate());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' 'actuate' should be set to 'NONE'",
                                            ActuateType.NONE, domainType.getMetadata().get(0).getActuate());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' 'title' should be set",
                                            domainType.getMetadata().get(0).isSetTitle());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' 'title' should be set to 'title'",
                                            "title", domainType.getMetadata().get(0).getTitle());
                                    isAllowedValues = true;
                                }
                                else if(domainType.isSetAnyValue()){
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'name' should be set", domainType.isSetName());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint'" +
                                            " 'name' should be set to cstr2", "cstr2", domainType.getName());

                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'anyValue' should be set", domainType.isSetAnyValue());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'noValues' should not be set", domainType.isSetNoValues());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'valuesReference' should not be set", domainType.isSetValuesReference());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'allowedValues' should not be set", domainType.isSetAllowedValues());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'defaultValue' should not be set",
                                            domainType.isSetDefaultValue());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'meaning' should not be set",
                                            domainType.isSetMeaning());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'dataType' should not be set",
                                            domainType.isSetDataType());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'uom' should not be set",
                                            domainType.isSetUOM());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'referenceSystem' should be set",
                                            domainType.isSetReferenceSystem());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'getReferenceSystem' 'value'" +
                                            " should be set", domainType.getReferenceSystem().isSetValue());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'getReferenceSystem' 'value'" +
                                            " should be set to 'name'", "name", domainType.getReferenceSystem().getValue());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'getReferenceSystem' 'reference'" +
                                            " should be set", domainType.getReferenceSystem().isSetReference());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'getReferenceSystem' 'reference'" +
                                            " should be set to 'uri'", "uri", domainType.getReferenceSystem().getReference());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' " +
                                            "should not be set", domainType.isSetMetadata());
                                    isAnyValue = true;
                                }
                                else if(domainType.isSetNoValues()){
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'name' should be set", domainType.isSetName());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint'" +
                                            " 'name' should be set to cstr3", "cstr3", domainType.getName());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'anyValue' should not be set", domainType.isSetAnyValue());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'noValues' should be set", domainType.isSetNoValues());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'valuesReference' should not be set", domainType.isSetValuesReference());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'allowedValues' should not be set", domainType.isSetAllowedValues());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'defaultValue' should not be set",
                                            domainType.isSetDefaultValue());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'meaning' should not be set",
                                            domainType.isSetMeaning());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'dataType' should not be set",
                                            domainType.isSetDataType());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'uom' should not be set",
                                            domainType.isSetUOM());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'referenceSystem' should not be set",
                                            domainType.isSetReferenceSystem());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' " +
                                            "should not be set", domainType.isSetMetadata());
                                    isNoValues = true;
                                }
                                else if(domainType.isSetValuesReference()){
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'name' should be set", domainType.isSetName());
                                    assertEquals("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint'" +
                                            " 'name' should be set to cstr4", "cstr4", domainType.getName());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'anyValue' should not be set", domainType.isSetAnyValue());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'noValues' should not be set", domainType.isSetNoValues());
                                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'valuesReference' should be set", domainType.isSetValuesReference());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' " +
                                            "'allowedValues' should not be set", domainType.isSetAllowedValues());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'defaultValue' should not be set",
                                            domainType.isSetDefaultValue());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'meaning' should not be set",
                                            domainType.isSetMeaning());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'dataType' should not be set",
                                            domainType.isSetDataType());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'uom' should not be set",
                                            domainType.isSetUOM());
                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' 'referenceSystem' should not be set",
                                            domainType.isSetReferenceSystem());

                                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'metadata' " +
                                            "should not be set", domainType.isSetMetadata());
                                    isValuesReference = true;
                                }
                                else{
                                    fail("Unknown constraint");
                                }
                            }
                            assertTrue("The operation 'GetCapabilities' 'dcp' http' 'getOrPost' 'constraint' should " +
                                    "contains an 'anyValue", isAnyValue);
                            assertTrue("The operation 'GetCapabilities' 'dcp' http' 'getOrPost' 'constraint' should " +
                                    "contains an 'noValue", isNoValues);
                            assertTrue("The operation 'GetCapabilities' 'dcp' http' 'getOrPost' 'constraint' should " +
                                    "contains an 'valuesReference", isValuesReference);
                            assertTrue("The operation 'GetCapabilities' 'dcp' http' 'getOrPost' 'constraint' should " +
                                    "contains an 'allowedValues", isAllowedValues);
                            isUrl1 = true;
                        }
                        else if("getcapabilitiesurl2".equals(requestMethodType.getHref())){
                            assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'role' should not be set",
                                    requestMethodType.isSetRole());
                            assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'arcrole' should not be set",
                                    requestMethodType.isSetArcrole());
                            assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'show' should not be set",
                                    requestMethodType.isSetShow());
                            assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'actuate' should not be set",
                                    requestMethodType.isSetActuate());
                            assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'title' should not be set",
                                    requestMethodType.isSetTitle());
                            assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' should not be set",
                                    requestMethodType.isSetConstraint());
                            isUrl2 = true;
                        }
                        else{
                            fail("Unknowm Get or Post");
                        }
                    }

                }
                assertTrue("The operation 'GetCapabilities' 'dcp' should contains a value named 'getcapabilitiesurl1", isUrl1);
                assertTrue("The operation 'GetCapabilities' 'dcp' should contains a value named 'getcapabilitiesurl2", isUrl2);

                assertTrue("The operation 'GetCapabilities' 'constraint' should be set", operation.isSetConstraint());
                assertEquals("The operation 'GetCapabilities' 'constraint' should have only one value",
                        1, operation.getConstraint().size());
                assertTrue("The operation 'GetCapabilities' 'constraint' 'name' should be set",
                        operation.getConstraint().get(0).isSetName());
                assertEquals("The operation 'GetCapabilities' 'constraint' 'name' should be set to 'param",
                        "cstr", operation.getConstraint().get(0).getName());
                assertFalse("The operation 'GetCapabilities' 'constraint' 'meaning' should not be set",
                        operation.getConstraint().get(0).isSetMeaning());
                assertFalse("The operation 'GetCapabilities' 'constraint' 'defaultValue' should not be set",
                        operation.getConstraint().get(0).isSetDefaultValue());
                assertFalse("The operation 'GetCapabilities' 'constraint' 'metadata' should not be set",
                        operation.getConstraint().get(0).isSetMetadata());
                assertFalse("The operation 'GetCapabilities' 'constraint' 'referenceSystem' should not be set",
                        operation.getConstraint().get(0).isSetReferenceSystem());
                assertFalse("The operation 'GetCapabilities' 'constraint' 'UOM' should not be set",
                        operation.getConstraint().get(0).isSetUOM());
                assertFalse("The operation 'GetCapabilities' 'constraint' 'valuesReference' should not be set",
                        operation.getConstraint().get(0).isSetValuesReference());
                assertFalse("The operation 'GetCapabilities' 'constraint' 'dataType' should not be set",
                        operation.getConstraint().get(0).isSetDataType());
                assertFalse("The operation 'GetCapabilities' 'constraint' 'allowedValue' should not be set",
                        operation.getConstraint().get(0).isSetAllowedValues());
                assertFalse("The operation 'GetCapabilities' 'constraint' 'noValues' should not be set",
                        operation.getConstraint().get(0).isSetNoValues());
                assertTrue("The operation 'GetCapabilities' 'constraint' 'anyValue' should not be set",
                        operation.getConstraint().get(0).isSetAnyValue());

                assertTrue("The operation 'GetCapabilities' 'parameter' should be set", operation.isSetParameter());
                assertEquals("The operation 'GetCapabilities' 'parameter' should have only one value",
                        1, operation.getParameter().size());
                assertTrue("The operation 'GetCapabilities' 'parameter' 'name' should be set",
                        operation.getParameter().get(0).isSetName());
                assertEquals("The operation 'GetCapabilities' 'parameter' 'name' should be set to 'param",
                        "param", operation.getParameter().get(0).getName());
                assertFalse("The operation 'GetCapabilities' 'parameter' 'meaning' should not be set",
                        operation.getParameter().get(0).isSetMeaning());
                assertFalse("The operation 'GetCapabilities' 'parameter' 'defaultValue' should not be set",
                        operation.getParameter().get(0).isSetDefaultValue());
                assertFalse("The operation 'GetCapabilities' 'parameter' 'metadata' should not be set",
                        operation.getParameter().get(0).isSetMetadata());
                assertFalse("The operation 'GetCapabilities' 'parameter' 'referenceSystem' should not be set",
                        operation.getParameter().get(0).isSetReferenceSystem());
                assertFalse("The operation 'GetCapabilities' 'parameter' 'UOM' should not be set",
                        operation.getParameter().get(0).isSetUOM());
                assertFalse("The operation 'GetCapabilities' 'parameter' 'valuesReference' should not be set",
                        operation.getParameter().get(0).isSetValuesReference());
                assertFalse("The operation 'GetCapabilities' 'parameter' 'dataType' should not be set",
                        operation.getParameter().get(0).isSetDataType());
                assertFalse("The operation 'GetCapabilities' 'parameter' 'allowedValue' should not be set",
                        operation.getParameter().get(0).isSetAllowedValues());
                assertFalse("The operation 'GetCapabilities' 'parameter' 'noValues' should not be set",
                        operation.getParameter().get(0).isSetNoValues());
                assertTrue("The operation 'GetCapabilities' 'parameter' 'anyValue' should not be set",
                        operation.getParameter().get(0).isSetAnyValue());

                assertTrue("The operation 'GetCapabilities' 'metadata' should be set", operation.isSetMetadata());
                assertEquals("The operation 'GetCapabilities' 'metadata' should have only one value",
                        1, operation.getMetadata().size());
                assertTrue("The operation 'GetCapabilities' 'metadata' 'about' should not be set",
                        operation.getMetadata().get(0).isSetAbout());
                assertTrue("The operation 'GetCapabilities' 'metadata' 'title' should not be set",
                        operation.getMetadata().get(0).isSetTitle());
                assertTrue("The operation 'GetCapabilities' 'metadata' 'show' should not be set",
                        operation.getMetadata().get(0).isSetShow());
                assertTrue("The operation 'GetCapabilities' 'metadata' 'arcrole' should not be set",
                        operation.getMetadata().get(0).isSetArcrole());
                assertTrue("The operation 'GetCapabilities' 'metadata' 'actuate' should not be set",
                        operation.getMetadata().get(0).isSetActuate());
                assertTrue("The operation 'GetCapabilities' 'metadata' 'role' should not be set",
                        operation.getMetadata().get(0).isSetRole());
                assertTrue("The operation 'GetCapabilities' 'metadata' 'abstractMedatada' should not be set",
                        operation.getMetadata().get(0).isSetAbstractMetaData());
                assertTrue("The operation 'GetCapabilities' 'metadata' 'href' should be set",
                        operation.getMetadata().get(0).isSetHref());
                assertEquals("The operation 'GetCapabilities' 'metadata' 'href' should be set to 'href",
                        "href", operation.getMetadata().get(0).getHref());
                isGetCapabilities = true;
            }
            else if("DescribeProcess".equals(operation.getName())){
                assertTrue("The operation 'DescribeProcess' 'dcp' should be set", operation.isSetDCP());
                assertFalse("The operation 'DescribeProcess' 'dcp' should not be empty", operation.getDCP().isEmpty());
                boolean isUrl1 = false;
                boolean isUrl2 = false;
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The operation 'DescribeProcess' 'dcp' 'http' should be set", dcp.isSetHTTP());
                    assertTrue("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' should not be empty",
                            dcp.getHTTP().getGetOrPost().isEmpty());
                    for(JAXBElement<RequestMethodType> element : dcp.getHTTP().getGetOrPost()){
                        RequestMethodType requestMethodType = element.getValue();
                        assertTrue("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'href' should be set",
                                requestMethodType.isSetHref());
                        if("describeprocessurl1".equals(requestMethodType.getHref())){
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'role' should not be set",
                                    requestMethodType.isSetRole());
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'arcrole' should not be set",
                                    requestMethodType.isSetArcrole());
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'show' should not be set",
                                    requestMethodType.isSetShow());
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'actuate' should not be set",
                                    requestMethodType.isSetActuate());
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'title' should not be set",
                                    requestMethodType.isSetTitle());
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'constraint' should not be set",
                                    requestMethodType.isSetConstraint());
                            isUrl1 = true;
                        }
                        else if("describeprocessurl2".equals(requestMethodType.getHref())){
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'role' should not be set",
                                    requestMethodType.isSetRole());
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'arcrole' should not be set",
                                    requestMethodType.isSetArcrole());
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'show' should not be set",
                                    requestMethodType.isSetShow());
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'actuate' should not be set",
                                    requestMethodType.isSetActuate());
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'title' should not be set",
                                    requestMethodType.isSetTitle());
                            assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'constraint' should not be set",
                                    requestMethodType.isSetConstraint());
                            isUrl2 = true;
                        }
                        else{
                            fail("Unknowm Get or Post");
                        }
                    }

                }
                assertTrue("The operation 'DescribeProcess' 'dcp' should contains a value named 'describeprocessurl1", isUrl1);
                assertTrue("The operation 'DescribeProcess' 'dcp' should contains a value named 'describeprocessurl2", isUrl2);

                assertFalse("The operation 'DescribeProcess' 'constraint' should not be set", operation.isSetConstraint());
                assertFalse("The operation 'DescribeProcess' 'parameter' should not be set", operation.isSetParameter());
                assertFalse("The operation 'DescribeProcess' 'metadata' should not be set", operation.isSetMetadata());
                isDescribeProcess = true;
            }
            else if("Execute".equals(operation.getName())){
                assertTrue("The operation 'Execute' 'dcp' should be set", operation.isSetDCP());
                assertFalse("The operation 'Execute' 'dcp' should not be empty", operation.getDCP().isEmpty());
                boolean isUrl1 = false;
                boolean isUrl2 = false;
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The operation 'Execute' 'dcp' 'http' should be set", dcp.isSetHTTP());
                    assertTrue("The operation 'Execute' 'dcp' 'http' 'getOrPost' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' should not be empty",
                            dcp.getHTTP().getGetOrPost().isEmpty());
                    for(JAXBElement<RequestMethodType> element : dcp.getHTTP().getGetOrPost()){
                        RequestMethodType requestMethodType = element.getValue();
                        assertTrue("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'href' should be set",
                                requestMethodType.isSetHref());
                        if("executeurl1".equals(requestMethodType.getHref())){
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'role' should not be set",
                                    requestMethodType.isSetRole());
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'arcrole' should not be set",
                                    requestMethodType.isSetArcrole());
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'show' should not be set",
                                    requestMethodType.isSetShow());
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'actuate' should not be set",
                                    requestMethodType.isSetActuate());
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'title' should not be set",
                                    requestMethodType.isSetTitle());
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'constraint' should not be set",
                                    requestMethodType.isSetConstraint());
                            isUrl1 = true;
                        }
                        else if("executeurl2".equals(requestMethodType.getHref())){
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'role' should not be set",
                                    requestMethodType.isSetRole());
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'arcrole' should not be set",
                                    requestMethodType.isSetArcrole());
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'show' should not be set",
                                    requestMethodType.isSetShow());
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'actuate' should not be set",
                                    requestMethodType.isSetActuate());
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'title' should not be set",
                                    requestMethodType.isSetTitle());
                            assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'constraint' should not be set",
                                    requestMethodType.isSetConstraint());
                            isUrl2 = true;
                        }
                        else{
                            fail("Unknowm Get or Post");
                        }
                    }

                }
                assertTrue("The operation 'Execute' 'dcp' should contains a value named 'executeurl1", isUrl1);
                assertTrue("The operation 'Execute' 'dcp' should contains a value named 'executeurl2", isUrl2);

                assertFalse("The operation 'Execute' 'constraint' should not be set", operation.isSetConstraint());
                assertFalse("The operation 'Execute' 'parameter' should not be set", operation.isSetParameter());
                assertFalse("The operation 'Execute' 'metadata' should not be set", operation.isSetMetadata());
                isExecute = true;
            }
            else {
                fail("Operation not found");
            }
        }
        assertTrue("The wps service 'operationMetadata' should contains the operation 'GetCapabilities", isGetCapabilities);
        assertTrue("The wps service 'operationMetadata' should contains the operation 'DescribeProcess", isDescribeProcess);
        assertTrue("The wps service 'operationMetadata' should contains the operation 'Execute", isExecute);



        assertTrue("The 'operationMetadata' 'constraint' should be set", capabilities.getOperationsMetadata().isSetConstraint());
        assertEquals("The 'operationMetadata' 'constraint' should have only one value",
                1, capabilities.getOperationsMetadata().getConstraint().size());
        assertTrue("The 'operationMetadata' 'constraint' 'name' should be set",
                capabilities.getOperationsMetadata().getConstraint().get(0).isSetName());
        assertEquals("The 'operationMetadata' 'constraint' 'name' should be set to 'param",
                "cstr", capabilities.getOperationsMetadata().getConstraint().get(0).getName());
        assertFalse("The 'operationMetadata' 'constraint' 'meaning' should not be set",
                capabilities.getOperationsMetadata().getConstraint().get(0).isSetMeaning());
        assertFalse("The 'operationMetadata' 'constraint' 'defaultValue' should not be set",
                capabilities.getOperationsMetadata().getConstraint().get(0).isSetDefaultValue());
        assertFalse("The 'operationMetadata' 'constraint' 'metadata' should not be set",
                capabilities.getOperationsMetadata().getConstraint().get(0).isSetMetadata());
        assertFalse("The 'operationMetadata' 'constraint' 'referenceSystem' should not be set",
                capabilities.getOperationsMetadata().getConstraint().get(0).isSetReferenceSystem());
        assertFalse("The 'operationMetadata' 'constraint' 'UOM' should not be set",
                capabilities.getOperationsMetadata().getConstraint().get(0).isSetUOM());
        assertFalse("The 'operationMetadata' 'constraint' 'valuesReference' should not be set",
                capabilities.getOperationsMetadata().getConstraint().get(0).isSetValuesReference());
        assertFalse("The 'operationMetadata' 'constraint' 'dataType' should not be set",
                capabilities.getOperationsMetadata().getConstraint().get(0).isSetDataType());
        assertFalse("The 'operationMetadata' 'constraint' 'allowedValue' should not be set",
                capabilities.getOperationsMetadata().getConstraint().get(0).isSetAllowedValues());
        assertFalse("The 'operationMetadata' 'constraint' 'noValues' should not be set",
                capabilities.getOperationsMetadata().getConstraint().get(0).isSetNoValues());
        assertTrue("The 'operationMetadata' 'constraint' 'anyValue' should not be set",
                capabilities.getOperationsMetadata().getConstraint().get(0).isSetAnyValue());

        assertTrue("The 'operationMetadata' 'parameter' should be set", capabilities.getOperationsMetadata().isSetParameter());
        assertEquals("The 'operationMetadata' 'parameter' should have only one value",
                1, capabilities.getOperationsMetadata().getParameter().size());
        assertTrue("The 'operationMetadata' 'parameter' 'name' should be set",
                capabilities.getOperationsMetadata().getParameter().get(0).isSetName());
        assertEquals("The 'operationMetadata' 'parameter' 'name' should be set to 'param",
                "param", capabilities.getOperationsMetadata().getParameter().get(0).getName());
        assertFalse("The 'operationMetadata' 'parameter' 'meaning' should not be set",
                capabilities.getOperationsMetadata().getParameter().get(0).isSetMeaning());
        assertFalse("The 'operationMetadata' 'parameter' 'defaultValue' should not be set",
                capabilities.getOperationsMetadata().getParameter().get(0).isSetDefaultValue());
        assertFalse("The 'operationMetadata' 'parameter' 'metadata' should not be set",
                capabilities.getOperationsMetadata().getParameter().get(0).isSetMetadata());
        assertFalse("The 'operationMetadata' 'parameter' 'referenceSystem' should not be set",
                capabilities.getOperationsMetadata().getParameter().get(0).isSetReferenceSystem());
        assertFalse("The 'operationMetadata' 'parameter' 'UOM' should not be set",
                capabilities.getOperationsMetadata().getParameter().get(0).isSetUOM());
        assertFalse("The 'operationMetadata' 'parameter' 'valuesReference' should not be set",
                capabilities.getOperationsMetadata().getParameter().get(0).isSetValuesReference());
        assertFalse("The 'operationMetadata' 'parameter' 'dataType' should not be set",
                capabilities.getOperationsMetadata().getParameter().get(0).isSetDataType());
        assertFalse("The 'operationMetadata' 'parameter' 'allowedValue' should not be set",
                capabilities.getOperationsMetadata().getParameter().get(0).isSetAllowedValues());
        assertFalse("The 'operationMetadata' 'parameter' 'noValues' should not be set",
                capabilities.getOperationsMetadata().getParameter().get(0).isSetNoValues());
        assertTrue("The 'operationMetadata' 'parameter' 'anyValue' should not be set",
                capabilities.getOperationsMetadata().getParameter().get(0).isSetAnyValue());

        assertFalse("The 'operationMetadata' 'extendedCapabilities' should not be set",
                capabilities.getOperationsMetadata().isSetExtendedCapabilities());
    }


    /**
     * Test the given getCapabilities ServiceProvider sections.
     * @param capabilities Object to test
     */
    private void testFullServiceProvider(WPSCapabilitiesType capabilities){

        assertTrue("The wps service 'serviceProvider' should be set", capabilities.isSetServiceProvider());
        assertEquals("The wps service 'serviceProvider' 'name' should be 'OrbisGIS'",
                capabilities.getServiceProvider().getProviderName(), "OrbisGIS");
        assertTrue("The wps service 'serviceProvider' 'site' should be set",
                capabilities.getServiceProvider().isSetProviderSite());
        assertTrue("The wps service 'serviceProvider' 'site' should be set",
                capabilities.getServiceProvider().isSetProviderSite());
        assertTrue("The wps service 'serviceProvider' 'site' 'href' should be set",
                capabilities.getServiceProvider().getProviderSite().isSetHref());
        assertEquals("The wps service 'serviceProvider' 'site' 'href' should be set to 'http://orbisgis.org/'",
                "http://orbisgis.org/", capabilities.getServiceProvider().getProviderSite().getHref());
        assertTrue("The wps service 'serviceProvider' 'site' 'role' should be set",
                capabilities.getServiceProvider().getProviderSite().isSetRole());
        assertEquals("The wps service 'serviceProvider' 'site' 'role' should be set to 'role'",
                "role", capabilities.getServiceProvider().getProviderSite().getRole());
        assertTrue("The wps service 'serviceProvider' 'site' 'title' should be set",
                capabilities.getServiceProvider().getProviderSite().isSetTitle());
        assertEquals("The wps service 'serviceProvider' 'site' 'title' should be set to 'title'",
                "title", capabilities.getServiceProvider().getProviderSite().getTitle());
        assertTrue("The wps service 'serviceProvider' 'site' 'arcrole' should be set",
                capabilities.getServiceProvider().getProviderSite().isSetArcrole());
        assertEquals("The wps service 'serviceProvider' 'site' 'arcrole' should be set to 'title'",
                "arcrole", capabilities.getServiceProvider().getProviderSite().getArcrole());
        assertTrue("The wps service 'serviceProvider' 'site' 'actuate' should be set",
                capabilities.getServiceProvider().getProviderSite().isSetActuate());
        assertEquals("The wps service 'serviceProvider' 'site' 'actuate' should be set to 'NONE'",
                ActuateType.NONE, capabilities.getServiceProvider().getProviderSite().getActuate());
        assertTrue("The wps service 'serviceProvider' 'site' 'show' should be set",
                capabilities.getServiceProvider().getProviderSite().isSetShow());
        assertEquals("The wps service 'serviceProvider' 'site' 'show' should be set to 'NONE'",
                ShowType.NONE, capabilities.getServiceProvider().getProviderSite().getShow());


        assertTrue("The wps service 'serviceProvider' 'serviceContact' should be set",
                capabilities.getServiceProvider().isSetServiceContact());
        ResponsiblePartySubsetType serviceContact = capabilities.getServiceProvider().getServiceContact();
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'individualName' should be set",
                serviceContact.isSetIndividualName());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'individualName' should be set to 'name'",
                "name", serviceContact.getIndividualName());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'positionName' should be set",
                serviceContact.isSetPositionName());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'positionName' should be set to 'position'",
                "position", serviceContact.getPositionName());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' should be set",
                serviceContact.isSetContactInfo());

        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'phone' should be set",
                serviceContact.getContactInfo().isSetPhone());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'phone' 'voice' should be set",
                serviceContact.getContactInfo().getPhone().isSetVoice());
        assertArrayEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'phone' should be set to '[phone1, phone2]'",
                new String[]{"phone1", "phone2"}, serviceContact.getContactInfo().getPhone().getVoice().toArray());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'phone' 'facsimile' should be set",
                serviceContact.getContactInfo().getPhone().isSetFacsimile());
        assertArrayEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'facsimile' should be set to '[facsim1, facsim2]'",
                new String[]{"facsim1", "facsim2"}, serviceContact.getContactInfo().getPhone().getFacsimile().toArray());

        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' should be set",
                serviceContact.getContactInfo().isSetAddress());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'deliveryPoint' should be set",
                serviceContact.getContactInfo().getAddress().isSetDeliveryPoint());
        assertArrayEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'deliveryPoint' should be set to '[point1, point2]'",
                new String[]{"point1", "point2"}, serviceContact.getContactInfo().getAddress().getDeliveryPoint().toArray());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'city' should be set",
                serviceContact.getContactInfo().getAddress().isSetCity());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'city' should be set to 'city'",
                "city", serviceContact.getContactInfo().getAddress().getCity());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'administrativeArea' should be set",
                serviceContact.getContactInfo().getAddress().isSetAdministrativeArea());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'administrativeArea' should be set to 'area'",
                "area", serviceContact.getContactInfo().getAddress().getAdministrativeArea());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'postalCode' should be set",
                serviceContact.getContactInfo().getAddress().isSetPostalCode());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'postalCode' should be set to 'code'",
                "code", serviceContact.getContactInfo().getAddress().getPostalCode());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'country' should be set",
                serviceContact.getContactInfo().getAddress().isSetCountry());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'country' should be set to 'country'",
                "country", serviceContact.getContactInfo().getAddress().getCountry());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'emails' should be set",
                serviceContact.getContactInfo().getAddress().isSetElectronicMailAddress());
        assertArrayEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'address' 'emails' should be set to '[email1, email2]'",
                new String[]{"email1", "email2"}, serviceContact.getContactInfo().getAddress().getElectronicMailAddress().toArray());

        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' should be set",
                serviceContact.getContactInfo().isSetOnlineResource());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'href' should be set",
                serviceContact.getContactInfo().getOnlineResource().isSetHref());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'href' should be set to 'href'",
                "href", serviceContact.getContactInfo().getOnlineResource().getHref());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'role' should be set",
                serviceContact.getContactInfo().getOnlineResource().isSetRole());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'role' should be set to 'role'",
                "role", serviceContact.getContactInfo().getOnlineResource().getRole());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'title' should be set",
                serviceContact.getContactInfo().getOnlineResource().isSetTitle());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'title' should be set to 'title'",
                "title", serviceContact.getContactInfo().getOnlineResource().getTitle());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'arcrole' should be set",
                serviceContact.getContactInfo().getOnlineResource().isSetArcrole());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'arcrole' should be set to 'title'",
                "arcrole", serviceContact.getContactInfo().getOnlineResource().getArcrole());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'actuate' should be set",
                serviceContact.getContactInfo().getOnlineResource().isSetActuate());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'actuate' should be set to 'NONE'",
                ActuateType.NONE, serviceContact.getContactInfo().getOnlineResource().getActuate());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'show' should be set",
                serviceContact.getContactInfo().getOnlineResource().isSetShow());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'onlineResource' 'show' should be set to 'NONE'",
                ShowType.NONE, serviceContact.getContactInfo().getOnlineResource().getShow());

        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'hoursOfService' should be set",
                serviceContact.getContactInfo().isSetHoursOfService());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'hoursOfService' should be set to 'hours'",
                "hours", serviceContact.getContactInfo().getHoursOfService());
        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'instructions' should be set",
                serviceContact.getContactInfo().isSetHoursOfService());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'contactInfo' 'instructions' should be set to 'instructions'",
                "instructions", serviceContact.getContactInfo().getContactInstructions());

        assertTrue("The wps service 'serviceProvider' 'serviceContact' 'role' should be set",
                serviceContact.isSetRole());
        assertEquals("The wps service 'serviceProvider' 'serviceContact' 'role' should be set to 'role'",
                "role", serviceContact.getRole().getValue());
    }


    /**
     * Test the given getCapabilities ServiceIdentification sections.
     * @param capabilities Object to test
     */
    private void testFullServiceIdentification(WPSCapabilitiesType capabilities){
        assertTrue("The wps service 'serviceIdentification' should be set",
                capabilities.isSetServiceIdentification());
        assertTrue("The wps service 'serviceIdentification' 'serviceType' should be set",
                capabilities.getServiceIdentification().isSetServiceType());
        assertEquals("The wps service 'serviceIdentification' 'serviceType' should 'WPS'",
                capabilities.getServiceIdentification().getServiceType().getValue(), "WPS");

        assertTrue("The wps service 'serviceIdentification' 'serviceTypeVersion' should be set",
                capabilities.getServiceIdentification().isSetServiceTypeVersion());
        assertFalse("The wps service 'serviceIdentification' 'serviceTypeVersion' should not be empty",
                capabilities.getServiceIdentification().getServiceTypeVersion().isEmpty());
        assertEquals("The wps service 'serviceIdentification' 'serviceTypeVersion' should be '1.0.0'",
                capabilities.getServiceIdentification().getServiceTypeVersion().get(0), "1.0.0");

        assertNotNull("The wps service 'serviceIdentification' 'profile' should not be null",
                capabilities.getServiceIdentification().getProfile());
        assertFalse("The wps service 'serviceIdentification' 'profile' should not be empty",
                capabilities.getServiceIdentification().getProfile().isEmpty());
        assertEquals("The wps service 'serviceIdentification' 'profile' should be 'NONE'",
                "NONE", capabilities.getServiceIdentification().getProfile().get(0));

        assertTrue("The wps service 'serviceIdentification' 'fees' should be set",
                capabilities.getServiceIdentification().isSetFees());
        assertEquals("The wps service 'serviceIdentification' 'fees' should be 'NONE'",
                "NONE", capabilities.getServiceIdentification().getFees());

        assertTrue("The wps service 'serviceIdentification' 'constraint' should be set",
                capabilities.getServiceIdentification().isSetAccessConstraints());
        assertFalse("The wps service 'serviceIdentification' 'constraint' should not be empty",
                capabilities.getServiceIdentification().getAccessConstraints().isEmpty());
        assertEquals("The wps service 'serviceIdentification' 'constraint' should be 'NONE'",
                "NONE", capabilities.getServiceIdentification().getAccessConstraints().get(0));

        assertTrue("The wps service 'serviceIdentification' 'title' should be set",
                capabilities.getServiceIdentification().isSetTitle());
        assertFalse("The wps service 'serviceIdentification' 'title' should not be empty",
                capabilities.getServiceIdentification().getTitle().isEmpty());
        boolean isSetEnTitle = false;
        for(LanguageStringType languageStringType : capabilities.getServiceIdentification().getTitle()){
            if(languageStringType.getLang().equals("en")){
                assertEquals("The wps service 'serviceIdentification' 'title' value should be 'Local WPS Service'",
                        languageStringType.getValue(), "Local WPS Service");
                assertEquals("The wps service 'serviceIdentification' 'title' language should be 'en'",
                        languageStringType.getLang(), "en");
                isSetEnTitle = true;
            }
            else{
                fail("Unknown title");
            }
        }
        assertTrue("The 'en' title should be set", isSetEnTitle);

        assertTrue("The wps service 'serviceIdentification' 'abstract' should be set",
                capabilities.getServiceIdentification().isSetAbstract());
        assertFalse("The wps service 'serviceIdentification' 'abstract' should not be empty",
                capabilities.getServiceIdentification().getAbstract().isEmpty());
        boolean isSetEnAbstract = false;
        for(LanguageStringType languageStringType : capabilities.getServiceIdentification().getAbstract()){
            if(languageStringType.getLang().equals("en")){
                assertEquals("The wps service 'serviceIdentification' 'abstract' value should be 'A local instance of a WPS Service'",
                        languageStringType.getValue(), "A local instance of a WPS Service");
                assertEquals("The wps service 'serviceIdentification' 'abstract' language should be 'en'",
                        languageStringType.getLang(), "en");
                isSetEnAbstract = true;
            }
            else{
                fail("Unknown abstract");
            }
        }
        assertTrue("The 'en' abstract should be set", isSetEnAbstract);

        assertTrue("The wps service 'serviceIdentification' 'keywords' should be set",
                capabilities.getServiceIdentification().isSetKeywords());
        assertFalse("The wps service 'serviceIdentification' 'keywords' should not be empty",
                capabilities.getServiceIdentification().getKeywords().isEmpty());
        boolean isWps = false;
        boolean isToolbox = false;
        boolean isOrbisgis = false;
        for(KeywordsType keywordType : capabilities.getServiceIdentification().getKeywords()){
            assertTrue("The wps service 'serviceIdentification' 'keywords' 'keyword' should be set",
                    keywordType.isSetKeyword());
            assertFalse("The 'keywords' should not be empty", keywordType.getKeyword().isEmpty());
            assertTrue("The 'keyword' 'value' should be set", keywordType.getKeyword().get(0).isSetValue());
            if(keywordType.getKeyword().get(0).getValue().contains("WPS")) {
                boolean isSetFr = false;
                boolean isSetEn = false;
                for (LanguageStringType languageStringType : keywordType.getKeyword()) {
                    if (languageStringType.getLang().equals("en")) {
                        assertEquals("The wps service 'serviceIdentification' 'keywords' value should be 'WPS service'",
                                languageStringType.getValue(), "WPS service");
                        assertEquals("The wps service 'serviceIdentification' 'keywords' language should be 'en'",
                                languageStringType.getLang(), "en");
                        isSetEn = true;
                    } else if (languageStringType.getLang().equals("fr-fr")) {
                        assertEquals("The wps service 'serviceIdentification' 'keywords' value should be 'Service WPS'",
                                languageStringType.getValue(), "Service WPS");
                        assertEquals("The wps service 'serviceIdentification' 'keywords' language should be 'fr-fr'",
                                languageStringType.getLang(), "fr-fr");
                        isSetFr = true;
                    } else {
                        fail("Unknown abstract");
                    }
                }
                assertTrue("The 'en' keyword should be set", isSetEn);
                assertTrue("The 'fr-fr' keyword should be set", isSetFr);
                isWps = true;
            }
            else if(keywordType.getKeyword().get(0).getValue().contains("Toolbox")) {
                boolean isSetFr = false;
                boolean isSetEn = false;
                for (LanguageStringType languageStringType : keywordType.getKeyword()) {
                    if (languageStringType.getLang().equals("en")) {
                        assertEquals("The wps service 'serviceIdentification' 'keywords' value should be 'Toolbox'",
                                languageStringType.getValue(), "Toolbox");
                        assertEquals("The wps service 'serviceIdentification' 'keywords' language should be 'en'",
                                languageStringType.getLang(), "en");
                        isSetEn = true;
                    } else if (languageStringType.getLang().equals("fr-fr")) {
                        assertEquals("The wps service 'serviceIdentification' 'keywords' value should be 'Toolbox'",
                                languageStringType.getValue(), "Toolbox");
                        assertEquals("The wps service 'serviceIdentification' 'keywords' language should be 'fr-fr'",
                                languageStringType.getLang(), "fr-fr");
                        isSetFr = true;
                    } else {
                        fail("Unknown abstract");
                    }
                }
                assertTrue("The 'en' keyword should be set", isSetEn);
                assertTrue("The 'fr-fr' keyword should be set", isSetFr);
                isToolbox = true;
            }
            else if(keywordType.getKeyword().get(0).getValue().contains("OrbisGIS")) {
                boolean isSetFr = false;
                boolean isSetEn = false;
                for (LanguageStringType languageStringType : keywordType.getKeyword()) {
                    if (languageStringType.getLang().equals("en")) {
                        assertEquals("The wps service 'serviceIdentification' 'keywords' value should be 'OrbisGIS'",
                                languageStringType.getValue(), "OrbisGIS");
                        assertEquals("The wps service 'serviceIdentification' 'keywords' language should be 'en'",
                                languageStringType.getLang(), "en");
                        isSetEn = true;
                    } else if (languageStringType.getLang().equals("fr-fr")) {
                        assertEquals("The wps service 'serviceIdentification' 'keywords' value should be 'OrbisGIS'",
                                languageStringType.getValue(), "OrbisGIS");
                        assertEquals("The wps service 'serviceIdentification' 'keywords' language should be 'fr-fr'",
                                languageStringType.getLang(), "fr-fr");
                        isSetFr = true;
                    } else {
                        fail("Unknown abstract");
                    }
                }
                assertTrue("The 'en' keyword should be set", isSetEn);
                assertTrue("The 'fr-fr' keyword should be set", isSetFr);
                isOrbisgis = true;
            }
            else{
                fail("Unknown keyword");
            }
        }
        assertTrue("The 'WPS' keyword should be set", isWps);
        assertTrue("The 'Toolbox' keyword should be set", isToolbox);
        assertTrue("The 'OrbisGIS' keyword should be set", isOrbisgis);
    }

    /**
     * Test an empty GetCapabilities request with a minimal WPS property
     */
    @Test
    public void testMinEmptyGetCapabilities(){
        //Ask for the GetCapabilities
        GetCapabilities getCapabilities = new GetCapabilities();
        Object object = minWps100Operations.getCapabilities(getCapabilities);
        assertTrue("The wps service answer should be 'WPSCapabilitiesType",object instanceof WPSCapabilitiesType);
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType)object;

        //version tests
        assertTrue("The wps service 'version' should be set", capabilities.isSetVersion());
        assertEquals("The wps service 'version' should be '1.0.0'", "1.0.0", capabilities.getVersion());

        //update sequence tests
        assertFalse("The wps service 'updateSequence' should not be set", capabilities.isSetUpdateSequence());

        //lang tests
        assertTrue("The wps service 'lang' should be set", capabilities.isSetLang());
        assertEquals("The wps service 'lang' should be 'en'", "en", capabilities.getLang());

        //ServiceIdentification tests
        assertTrue("The wps service 'serviceIdentification' should be set",
                capabilities.isSetServiceIdentification());
        assertTrue("The wps service 'serviceIdentification' 'serviceType' should be set",
                capabilities.getServiceIdentification().isSetServiceType());
        assertEquals("The wps service 'serviceIdentification' 'serviceType' should 'WPS'",
                capabilities.getServiceIdentification().getServiceType().getValue(), "WPS");

        assertTrue("The wps service 'serviceIdentification' 'serviceTypeVersion' should be set",
                capabilities.getServiceIdentification().isSetServiceTypeVersion());
        assertFalse("The wps service 'serviceIdentification' 'serviceTypeVersion' should not be empty",
                capabilities.getServiceIdentification().getServiceTypeVersion().isEmpty());
        assertEquals("The wps service 'serviceIdentification' 'serviceTypeVersion' should be '1.0.0'",
                capabilities.getServiceIdentification().getServiceTypeVersion().get(0), "1.0.0");

        assertFalse("The wps service 'serviceIdentification' 'profile' should not be set",
                capabilities.getServiceIdentification().isSetProfile());
        assertFalse("The wps service 'serviceIdentification' 'fees' should not be set",
                capabilities.getServiceIdentification().isSetFees());
        assertFalse("The wps service 'serviceIdentification' 'constraint' should not be set",
                capabilities.getServiceIdentification().isSetAccessConstraints());

        assertTrue("The wps service 'serviceIdentification' 'title' should be set",
                capabilities.getServiceIdentification().isSetTitle());
        assertFalse("The wps service 'serviceIdentification' 'title' should not be empty",
                capabilities.getServiceIdentification().getTitle().isEmpty());
        boolean isSetEnTitle = false;
        for(LanguageStringType languageStringType : capabilities.getServiceIdentification().getTitle()){
            if(languageStringType.getLang().equals("en")){
                assertEquals("The wps service 'serviceIdentification' 'title' value should be 'Local WPS Service'",
                        languageStringType.getValue(), "Local WPS Service");
                assertEquals("The wps service 'serviceIdentification' 'title' language should be 'en'",
                        languageStringType.getLang(), "en");
                isSetEnTitle = true;
            }
            else{
                fail("Unknown title");
            }
        }
        assertTrue("The 'en' title should be set", isSetEnTitle);

        assertFalse("The wps service 'serviceIdentification' 'abstract' should not be set",
                capabilities.getServiceIdentification().isSetAbstract());
        assertFalse("The wps service 'serviceIdentification' 'keywords' should not be set",
                capabilities.getServiceIdentification().isSetKeywords());

        //Test ServiceProvider
        assertTrue("The wps service 'serviceProvider' should be set", capabilities.isSetServiceProvider());
        assertEquals("The wps service 'serviceProvider' 'name' should be 'OrbisGIS'",
                capabilities.getServiceProvider().getProviderName(), "OrbisGIS");
        assertFalse("The wps service 'serviceProvider' 'site' should not be set",
                capabilities.getServiceProvider().isSetProviderSite());
        assertFalse("The wps service 'serviceProvider' 'serviceContact' should not be set",
                capabilities.getServiceProvider().isSetServiceContact());

        //OperationMetadata test
        assertTrue("The wps service 'operationMetadata' should be set",
                capabilities.isSetOperationsMetadata());
        assertTrue("The wps service operation metadata operation should be set",
                capabilities.getOperationsMetadata().isSetOperation());
        assertEquals("The wps service operation metadata operation should contains three elements",
                3, capabilities.getOperationsMetadata().getOperation().size());
        boolean isGetCapabilities = false;
        boolean isDescribeProcess = false;
        boolean isExecute = false;
        for(Operation operation : capabilities.getOperationsMetadata().getOperation()){
            assertTrue(operation.isSetName());
            if("GetCapabilities".equals(operation.getName())) {
                assertTrue("The operation 'GetCapabilities' 'dcp' should be set", operation.isSetDCP());
                assertFalse("The operation 'GetCapabilities' 'dcp' should not be empty", operation.getDCP().isEmpty());
                for (DCP dcp : operation.getDCP()) {
                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' should be set", dcp.isSetHTTP());
                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' should not be empty",
                            dcp.getHTTP().getGetOrPost().isEmpty());
                    JAXBElement<RequestMethodType> element = dcp.getHTTP().getGetOrPost().get(0);
                    RequestMethodType requestMethodType = element.getValue();
                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'href' should be set",
                            requestMethodType.isSetHref());
                    if ("getcapabilitiesurl1".equals(requestMethodType.getHref())) {
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'role' should not be set",
                                requestMethodType.isSetRole());
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'arcrole' should not be set",
                                requestMethodType.isSetArcrole());
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'show' should not be set",
                                requestMethodType.isSetShow());
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'actuate' should not be set",
                                requestMethodType.isSetActuate());
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'title' should not be set",
                                requestMethodType.isSetTitle());
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' should not be set",
                                requestMethodType.isSetConstraint());
                    } else {
                        fail("Unknowm Get or Post");
                    }
                }
                assertFalse("The operation 'DescribeProcess' 'constraint' should not be set", operation.isSetConstraint());
                assertFalse("The operation 'DescribeProcess' 'parameter' should not be set", operation.isSetParameter());
                assertFalse("The operation 'DescribeProcess' 'metadata' should not be set", operation.isSetMetadata());
                isGetCapabilities = true;
            }
            else if("DescribeProcess".equals(operation.getName())){
                assertTrue("The operation 'DescribeProcess' 'dcp' should be set", operation.isSetDCP());
                assertFalse("The operation 'DescribeProcess' 'dcp' should not be empty", operation.getDCP().isEmpty());
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The operation 'DescribeProcess' 'dcp' 'http' should be set", dcp.isSetHTTP());
                    assertTrue("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' should not be empty",
                            dcp.getHTTP().getGetOrPost().isEmpty());
                    JAXBElement<RequestMethodType> element = dcp.getHTTP().getGetOrPost().get(0);
                    RequestMethodType requestMethodType = element.getValue();
                    assertTrue("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'href' should be set",
                            requestMethodType.isSetHref());
                    if("describeprocessurl1".equals(requestMethodType.getHref())){
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'role' should not be set",
                                requestMethodType.isSetRole());
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'arcrole' should not be set",
                                requestMethodType.isSetArcrole());
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'show' should not be set",
                                requestMethodType.isSetShow());
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'actuate' should not be set",
                                requestMethodType.isSetActuate());
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'title' should not be set",
                                requestMethodType.isSetTitle());
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'constraint' should not be set",
                                requestMethodType.isSetConstraint());
                    }
                    else{
                        fail("Unknowm Get or Post");
                    }
                }
                assertFalse("The operation 'DescribeProcess' 'constraint' should not be set", operation.isSetConstraint());
                assertFalse("The operation 'DescribeProcess' 'parameter' should not be set", operation.isSetParameter());
                assertFalse("The operation 'DescribeProcess' 'metadata' should not be set", operation.isSetMetadata());
                isDescribeProcess = true;
            }
            else if("Execute".equals(operation.getName())){
                assertTrue("The operation 'Execute' 'dcp' should be set", operation.isSetDCP());
                assertFalse("The operation 'Execute' 'dcp' should not be empty", operation.getDCP().isEmpty());
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The operation 'Execute' 'dcp' 'http' should be set", dcp.isSetHTTP());
                    assertTrue("The operation 'Execute' 'dcp' 'http' 'getOrPost' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' should not be empty",
                            dcp.getHTTP().getGetOrPost().isEmpty());
                    JAXBElement<RequestMethodType> element = dcp.getHTTP().getGetOrPost().get(0);
                    RequestMethodType requestMethodType = element.getValue();
                    assertTrue("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'href' should be set",
                            requestMethodType.isSetHref());
                    if("executeurl1".equals(requestMethodType.getHref())){
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'role' should not be set",
                                requestMethodType.isSetRole());
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'arcrole' should not be set",
                                requestMethodType.isSetArcrole());
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'show' should not be set",
                                requestMethodType.isSetShow());
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'actuate' should not be set",
                                requestMethodType.isSetActuate());
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'title' should not be set",
                                requestMethodType.isSetTitle());
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'constraint' should not be set",
                                requestMethodType.isSetConstraint());
                    }
                    else{
                        fail("Unknowm Get or Post");
                    }
                }

                assertFalse("The operation 'Execute' 'constraint' should not be set", operation.isSetConstraint());
                assertFalse("The operation 'Execute' 'parameter' should not be set", operation.isSetParameter());
                assertFalse("The operation 'Execute' 'metadata' should not be set", operation.isSetMetadata());
                isExecute = true;
            }
            else {
                fail("Operation not found");
            }
        }
        assertTrue("The wps service 'operationMetadata' should contains the operation 'GetCapabilities", isGetCapabilities);
        assertTrue("The wps service 'operationMetadata' should contains the operation 'DescribeProcess", isDescribeProcess);
        assertTrue("The wps service 'operationMetadata' should contains the operation 'Execute", isExecute);

        assertFalse("The 'operationMetadata' 'constraint' should not be set",
                capabilities.getOperationsMetadata().isSetConstraint());
        assertFalse("The 'operationMetadata' 'parameter' should not be set",
                capabilities.getOperationsMetadata().isSetParameter());
        assertFalse("The 'operationMetadata' 'extendedCapabilities' should not be set",
                capabilities.getOperationsMetadata().isSetExtendedCapabilities());

        //WSDL test
        assertFalse("The wps service 'WSDL' should not be set", capabilities.isSetWSDL());

        //language tests
        assertTrue("The wps service 'languages' should be set",
                capabilities.isSetLanguages());
        assertTrue("The wps service 'languages' 'default' should be set",
                capabilities.getLanguages().isSetDefault());
        assertTrue("The wps service 'languages' 'default' 'language' should be set",
                capabilities.getLanguages().getDefault().isSetLanguage());
        assertEquals("The wps service 'languages' 'default' 'language' should be 'en'",
                "en", capabilities.getLanguages().getDefault().getLanguage());
        assertTrue("The wps service 'languages' 'supported' should be set",
                capabilities.getLanguages().isSetSupported());
        assertTrue("The wps service 'languages' 'supported' 'language' should be set",
                capabilities.getLanguages().getSupported().isSetLanguage());
        assertArrayEquals("The wps service 'languages' 'supported' 'language' should be set to [en, fr-fr]",
                new String[]{"en", "fr-fr"}, capabilities.getLanguages().getSupported().getLanguage().toArray());

        //ProcessOffering test
        assertTrue("The wps service 'processOffering' should be set", capabilities.isSetProcessOfferings());
        assertTrue("The wps service 'processOffering' should be set", capabilities.getProcessOfferings().isSetProcess());
        for(ProcessBriefType process : capabilities.getProcessOfferings().getProcess()){
            assertTrue("The process 'id' should be set", process.isSetIdentifier());
            assertTrue("The process 'title' should be set", process.isSetTitle());
        }
    }


    /**
     * Test a GetCapabilities request with a minimal WPS property
     */
    @Test
    public void testMinGetCapabilities(){
        //Ask for the GetCapabilities
        GetCapabilities getCapabilities = new GetCapabilities();
        getCapabilities.setLanguage("en");
        AcceptVersionsType acceptVersionsType = new AcceptVersionsType();
        acceptVersionsType.getVersion().add("1.0.0");
        getCapabilities.setAcceptVersions(acceptVersionsType);
        Object object = minWps100Operations.getCapabilities(getCapabilities);
        assertTrue("The wps service answer should be 'WPSCapabilitiesType",object instanceof WPSCapabilitiesType);
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType)object;

        //version tests
        assertTrue("The wps service 'version' should be set", capabilities.isSetVersion());
        assertEquals("The wps service 'version' should be '1.0.0'", "1.0.0", capabilities.getVersion());

        //update sequence tests
        assertFalse("The wps service 'updateSequence' should not be set", capabilities.isSetUpdateSequence());

        //lang tests
        assertTrue("The wps service 'lang' should be set", capabilities.isSetLang());
        assertEquals("The wps service 'lang' should be 'en'", "en", capabilities.getLang());

        //ServiceIdentification tests
        assertTrue("The wps service 'serviceIdentification' should be set",
                capabilities.isSetServiceIdentification());
        assertTrue("The wps service 'serviceIdentification' 'serviceType' should be set",
                capabilities.getServiceIdentification().isSetServiceType());
        assertEquals("The wps service 'serviceIdentification' 'serviceType' should 'WPS'",
                capabilities.getServiceIdentification().getServiceType().getValue(), "WPS");

        assertTrue("The wps service 'serviceIdentification' 'serviceTypeVersion' should be set",
                capabilities.getServiceIdentification().isSetServiceTypeVersion());
        assertFalse("The wps service 'serviceIdentification' 'serviceTypeVersion' should not be empty",
                capabilities.getServiceIdentification().getServiceTypeVersion().isEmpty());
        assertEquals("The wps service 'serviceIdentification' 'serviceTypeVersion' should be '1.0.0'",
                capabilities.getServiceIdentification().getServiceTypeVersion().get(0), "1.0.0");

        assertFalse("The wps service 'serviceIdentification' 'profile' should not be set",
                capabilities.getServiceIdentification().isSetProfile());
        assertFalse("The wps service 'serviceIdentification' 'fees' should not be set",
                capabilities.getServiceIdentification().isSetFees());
        assertFalse("The wps service 'serviceIdentification' 'constraint' should not be set",
                capabilities.getServiceIdentification().isSetAccessConstraints());

        assertTrue("The wps service 'serviceIdentification' 'title' should be set",
                capabilities.getServiceIdentification().isSetTitle());
        assertFalse("The wps service 'serviceIdentification' 'title' should not be empty",
                capabilities.getServiceIdentification().getTitle().isEmpty());
        boolean isSetEnTitle = false;
        for(LanguageStringType languageStringType : capabilities.getServiceIdentification().getTitle()){
            if(languageStringType.getLang().equals("en")){
                assertEquals("The wps service 'serviceIdentification' 'title' value should be 'Local WPS Service'",
                        languageStringType.getValue(), "Local WPS Service");
                assertEquals("The wps service 'serviceIdentification' 'title' language should be 'en'",
                        languageStringType.getLang(), "en");
                isSetEnTitle = true;
            }
            else{
                fail("Unknown title");
            }
        }
        assertTrue("The 'en' title should be set", isSetEnTitle);

        assertFalse("The wps service 'serviceIdentification' 'abstract' should not be set",
                capabilities.getServiceIdentification().isSetAbstract());
        assertFalse("The wps service 'serviceIdentification' 'keywords' should not be set",
                capabilities.getServiceIdentification().isSetKeywords());

        //Test ServiceProvider
        assertTrue("The wps service 'serviceProvider' should be set", capabilities.isSetServiceProvider());
        assertEquals("The wps service 'serviceProvider' 'name' should be 'OrbisGIS'",
                capabilities.getServiceProvider().getProviderName(), "OrbisGIS");
        assertFalse("The wps service 'serviceProvider' 'site' should not be set",
                capabilities.getServiceProvider().isSetProviderSite());
        assertFalse("The wps service 'serviceProvider' 'serviceContact' should not be set",
                capabilities.getServiceProvider().isSetServiceContact());

        //OperationMetadata test
        assertTrue("The wps service 'operationMetadata' should be set",
                capabilities.isSetOperationsMetadata());
        assertTrue("The wps service operation metadata operation should be set",
                capabilities.getOperationsMetadata().isSetOperation());
        assertEquals("The wps service operation metadata operation should contains three elements",
                3, capabilities.getOperationsMetadata().getOperation().size());
        boolean isGetCapabilities = false;
        boolean isDescribeProcess = false;
        boolean isExecute = false;
        for(Operation operation : capabilities.getOperationsMetadata().getOperation()){
            assertTrue(operation.isSetName());
            if("GetCapabilities".equals(operation.getName())) {
                assertTrue("The operation 'GetCapabilities' 'dcp' should be set", operation.isSetDCP());
                assertFalse("The operation 'GetCapabilities' 'dcp' should not be empty", operation.getDCP().isEmpty());
                for (DCP dcp : operation.getDCP()) {
                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' should be set", dcp.isSetHTTP());
                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' should not be empty",
                            dcp.getHTTP().getGetOrPost().isEmpty());
                    JAXBElement<RequestMethodType> element = dcp.getHTTP().getGetOrPost().get(0);
                    RequestMethodType requestMethodType = element.getValue();
                    assertTrue("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'href' should be set",
                            requestMethodType.isSetHref());
                    if ("getcapabilitiesurl1".equals(requestMethodType.getHref())) {
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'role' should not be set",
                                requestMethodType.isSetRole());
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'arcrole' should not be set",
                                requestMethodType.isSetArcrole());
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'show' should not be set",
                                requestMethodType.isSetShow());
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'actuate' should not be set",
                                requestMethodType.isSetActuate());
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'title' should not be set",
                                requestMethodType.isSetTitle());
                        assertFalse("The operation 'GetCapabilities' 'dcp' 'http' 'getOrPost' 'constraint' should not be set",
                                requestMethodType.isSetConstraint());
                    } else {
                        fail("Unknowm Get or Post");
                    }
                }
                assertFalse("The operation 'DescribeProcess' 'constraint' should not be set", operation.isSetConstraint());
                assertFalse("The operation 'DescribeProcess' 'parameter' should not be set", operation.isSetParameter());
                assertFalse("The operation 'DescribeProcess' 'metadata' should not be set", operation.isSetMetadata());
                isGetCapabilities = true;
            }
            else if("DescribeProcess".equals(operation.getName())){
                assertTrue("The operation 'DescribeProcess' 'dcp' should be set", operation.isSetDCP());
                assertFalse("The operation 'DescribeProcess' 'dcp' should not be empty", operation.getDCP().isEmpty());
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The operation 'DescribeProcess' 'dcp' 'http' should be set", dcp.isSetHTTP());
                    assertTrue("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' should not be empty",
                            dcp.getHTTP().getGetOrPost().isEmpty());
                    JAXBElement<RequestMethodType> element = dcp.getHTTP().getGetOrPost().get(0);
                    RequestMethodType requestMethodType = element.getValue();
                    assertTrue("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'href' should be set",
                            requestMethodType.isSetHref());
                    if("describeprocessurl1".equals(requestMethodType.getHref())){
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'role' should not be set",
                                requestMethodType.isSetRole());
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'arcrole' should not be set",
                                requestMethodType.isSetArcrole());
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'show' should not be set",
                                requestMethodType.isSetShow());
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'actuate' should not be set",
                                requestMethodType.isSetActuate());
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'title' should not be set",
                                requestMethodType.isSetTitle());
                        assertFalse("The operation 'DescribeProcess' 'dcp' 'http' 'getOrPost' 'constraint' should not be set",
                                requestMethodType.isSetConstraint());
                    }
                    else{
                        fail("Unknowm Get or Post");
                    }
                }
                assertFalse("The operation 'DescribeProcess' 'constraint' should not be set", operation.isSetConstraint());
                assertFalse("The operation 'DescribeProcess' 'parameter' should not be set", operation.isSetParameter());
                assertFalse("The operation 'DescribeProcess' 'metadata' should not be set", operation.isSetMetadata());
                isDescribeProcess = true;
            }
            else if("Execute".equals(operation.getName())){
                assertTrue("The operation 'Execute' 'dcp' should be set", operation.isSetDCP());
                assertFalse("The operation 'Execute' 'dcp' should not be empty", operation.getDCP().isEmpty());
                for(DCP dcp : operation.getDCP()){
                    assertTrue("The operation 'Execute' 'dcp' 'http' should be set", dcp.isSetHTTP());
                    assertTrue("The operation 'Execute' 'dcp' 'http' 'getOrPost' should be set",
                            dcp.getHTTP().isSetGetOrPost());
                    assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' should not be empty",
                            dcp.getHTTP().getGetOrPost().isEmpty());
                    JAXBElement<RequestMethodType> element = dcp.getHTTP().getGetOrPost().get(0);
                    RequestMethodType requestMethodType = element.getValue();
                    assertTrue("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'href' should be set",
                            requestMethodType.isSetHref());
                    if("executeurl1".equals(requestMethodType.getHref())){
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'role' should not be set",
                                requestMethodType.isSetRole());
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'arcrole' should not be set",
                                requestMethodType.isSetArcrole());
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'show' should not be set",
                                requestMethodType.isSetShow());
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'actuate' should not be set",
                                requestMethodType.isSetActuate());
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'title' should not be set",
                                requestMethodType.isSetTitle());
                        assertFalse("The operation 'Execute' 'dcp' 'http' 'getOrPost' 'constraint' should not be set",
                                requestMethodType.isSetConstraint());
                    }
                    else{
                        fail("Unknowm Get or Post");
                    }
                }

                assertFalse("The operation 'Execute' 'constraint' should not be set", operation.isSetConstraint());
                assertFalse("The operation 'Execute' 'parameter' should not be set", operation.isSetParameter());
                assertFalse("The operation 'Execute' 'metadata' should not be set", operation.isSetMetadata());
                isExecute = true;
            }
            else {
                fail("Operation not found");
            }
        }
        assertTrue("The wps service 'operationMetadata' should contains the operation 'GetCapabilities", isGetCapabilities);
        assertTrue("The wps service 'operationMetadata' should contains the operation 'DescribeProcess", isDescribeProcess);
        assertTrue("The wps service 'operationMetadata' should contains the operation 'Execute", isExecute);

        assertFalse("The 'operationMetadata' 'constraint' should not be set",
                capabilities.getOperationsMetadata().isSetConstraint());
        assertFalse("The 'operationMetadata' 'parameter' should not be set",
                capabilities.getOperationsMetadata().isSetParameter());
        assertFalse("The 'operationMetadata' 'extendedCapabilities' should not be set",
                capabilities.getOperationsMetadata().isSetExtendedCapabilities());

        //WSDL test
        assertFalse("The wps service 'WSDL' should not be set", capabilities.isSetWSDL());

        //language tests
        assertTrue("The wps service 'languages' should be set",
                capabilities.isSetLanguages());
        assertTrue("The wps service 'languages' 'default' should be set",
                capabilities.getLanguages().isSetDefault());
        assertTrue("The wps service 'languages' 'default' 'language' should be set",
                capabilities.getLanguages().getDefault().isSetLanguage());
        assertEquals("The wps service 'languages' 'default' 'language' should be 'en'",
                "en", capabilities.getLanguages().getDefault().getLanguage());
        assertTrue("The wps service 'languages' 'supported' should be set",
                capabilities.getLanguages().isSetSupported());
        assertTrue("The wps service 'languages' 'supported' 'language' should be set",
                capabilities.getLanguages().getSupported().isSetLanguage());
        assertArrayEquals("The wps service 'languages' 'supported' 'language' should be set to [en, fr-fr]",
                new String[]{"en", "fr-fr"}, capabilities.getLanguages().getSupported().getLanguage().toArray());

        //ProcessOffering test
        assertTrue("The wps service 'processOffering' should be set", capabilities.isSetProcessOfferings());
        assertTrue("The wps service 'processOffering' should be set", capabilities.getProcessOfferings().isSetProcess());
        for(ProcessBriefType process : capabilities.getProcessOfferings().getProcess()){
            assertTrue("The process 'id' should be set", process.isSetIdentifier());
            assertTrue("The process 'title' should be set", process.isSetTitle());
        }
    }

    /**
     * Tests the GetCapabilities operation with a malformed GetCapabilities.
     *
     * @throws JAXBException Exception get if the marshaller fails.
     * @throws IOException Exception get if the resource getting fails.
     */
    @Test
    public void testBadGetCapabilities() throws JAXBException, IOException {
        //Create a well formed get capabilities
        GetCapabilities getCapabilities = new GetCapabilities();
        AcceptVersionsType acceptVersionsType = new AcceptVersionsType();
        acceptVersionsType.getVersion().add("1.0.0");
        getCapabilities.setAcceptVersions(acceptVersionsType);
        getCapabilities.setLanguage("en");

        //Null Capabilities test
        Object resultObject = minWps100Operations.getCapabilities(null);

        assertNotNull("Error on unmarshalling the WpsService answer, the object should not be null",
                resultObject);
        assertTrue("Error on unmarshalling the WpsService answer, the object should be a ExceptionReport",
                resultObject instanceof ExceptionReport);
        assertEquals("Error on unmarshalling the WpsService answer, the exception should be 'NoApplicableCode'",
                ((ExceptionReport) resultObject).getException().get(0).getExceptionCode(), "NoApplicableCode");

        //Bad version
        AcceptVersionsType badAcceptVersionsType = new AcceptVersionsType();
        badAcceptVersionsType.getVersion().add("0.0.0.badVersion");
        getCapabilities.setAcceptVersions(badAcceptVersionsType);
        resultObject = minWps100Operations.getCapabilities(getCapabilities);
        getCapabilities.setAcceptVersions(acceptVersionsType);

        assertNotNull("Error on unmarshalling the WpsService answer, the object should not be null",
                resultObject);
        assertTrue("Error on unmarshalling the WpsService answer, the object should be a ExceptionReport",
                resultObject instanceof ExceptionReport);
        assertEquals("Error on unmarshalling the WpsService answer, the exception should be 'VersionNegotiationFailed'",
                ((ExceptionReport) resultObject).getException().get(0).getExceptionCode(), "VersionNegotiationFailed");

        //Bad language
        getCapabilities.setLanguage("NotALanguage");
        resultObject = minWps100Operations.getCapabilities(getCapabilities);

        assertNotNull("Error on unmarshalling the WpsService answer, the object should not be null",
                resultObject);
        assertTrue("Error on unmarshalling the WpsService answer, the object should be a ExceptionReport",
                resultObject instanceof ExceptionReport);
        assertEquals("Error on unmarshalling the WpsService answer, the exception should be 'InvalidParameterValue'",
                ((ExceptionReport) resultObject).getException().get(0).getExceptionCode(), "InvalidParameterValue");
        assertEquals("Error on unmarshalling the WpsService answer, the exception locator should be 'AcceptLanguages'",
                ((ExceptionReport) resultObject).getException().get(0).getLocator(), "AcceptLanguages");
    }
}
