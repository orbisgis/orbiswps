package org.orbisgis.orbiswps.service.operations;

import net.opengis.ows._1.*;
import net.opengis.wps._1_0_0.*;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.service.process.ProcessManager;
import org.orbisgis.orbiswps.serviceapi.operations.WPS_1_0_0_Operations;

import java.io.File;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Test class for the WPS_2_0_OperationsImpl
 *
 * @author Sylvain PALOMINOS
 */
public class TestWPS_1_0_0_DescribeProcess {

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
            URL url = this.getClass().getResource("fullScript.groovy");
            assertNotNull("Unable to load the script 'fullScript.groovy'", url);
            File f = new File(url.toURI());
            wpsServer.addProcess(f);
            processManager.addScript(f.toURI());
        } catch (URISyntaxException e) {
            fail("Error on loading the scripts : "+e.getMessage());
        }

        assertNotNull("Unable to load the file 'minWpsService100.json'",
                TestWPS_1_0_0_DescribeProcess.class.getResource("minWpsService100.json").getFile());
        WpsServerProperties_1_0_0 minWpsProps = new WpsServerProperties_1_0_0(
                TestWPS_1_0_0_DescribeProcess.class.getResource("minWpsService100.json").getFile());
        minWps100Operations =  new WPS_1_0_0_OperationsImpl(wpsServer, minWpsProps, processManager);

        assertNotNull("Unable to load the file 'fullWpsService100.json'",
                TestWPS_1_0_0_DescribeProcess.class.getResource("fullWpsService100.json").getFile());
        WpsServerProperties_1_0_0 fullWpsProps = new WpsServerProperties_1_0_0(
                TestWPS_1_0_0_DescribeProcess.class.getResource("fullWpsService100.json").getFile());
        fullWps100Operations =  new WPS_1_0_0_OperationsImpl(wpsServer, fullWpsProps, processManager);
    }

    /**
     * Test an empty DescribeProcess request with a full WPS property and a full wps script
     */
    @Test
    public void testFullPropsFullScriptEmptyDescribeProcess(){
        //Ask for the DescribeProcess
        DescribeProcess describeProcess = new DescribeProcess();
        CodeType codeType = new CodeType();
        codeType.setValue("orbisgis:test:full");
        describeProcess.getIdentifier().add(codeType);
        Object object = fullWps100Operations.describeProcess(describeProcess);
        assertTrue("The wps service answer should be 'ProcessDescriptions",object instanceof ProcessDescriptions);
        ProcessDescriptions processDescriptions = (ProcessDescriptions)object;

        //lang tests
        assertTrue("The wps service 'processDescriptions' 'lang' should be set", processDescriptions.isSetLang());
        assertEquals("The wps service 'processDescriptions' 'lang' should be 'en'", "en", processDescriptions.getLang());

        //ProcessOffering test
        assertTrue("The wps service 'processDescriptions' 'processDescription' should be set",
                processDescriptions.isSetProcessDescription());
        for(ProcessDescriptionType process : processDescriptions.getProcessDescription()){
            assertTrue("The 'processDescriptions' 'processDescription' 'process' 'id' should be set",
                    process.isSetIdentifier());
            if("orbisgis:test:full".equals(process.getIdentifier().getValue())) {
                assertTrue("The 'orbisgis:test:full' 'title' should be set", process.isSetTitle());
                assertTrue("The 'orbisgis:test:full' 'title' 'value' should be set", process.getTitle().isSetValue());
                assertEquals("The 'orbisgis:test:full' 'title' 'value' should be set to",
                        "full test script", process.getTitle().getValue());
                assertTrue("The 'orbisgis:test:full' 'title' 'value' should be set", process.getTitle().isSetLang());
                assertEquals("The 'orbisgis:test:full' 'title' 'value' should be set to",
                        "en", process.getTitle().getLang());

                assertTrue("The 'orbisgis:test:full' 'abstract' should be set", process.isSetAbstract());
                assertTrue("The 'orbisgis:test:full' 'abstract' 'value' should be set",
                        process.getAbstract().isSetValue());
                assertEquals("The 'orbisgis:test:full' 'abstract' 'value' should be set to",
                        "Full test script descr.", process.getAbstract().getValue());
                assertTrue("The 'orbisgis:test:full' 'abstract' 'value' should be set",
                        process.getAbstract().isSetLang());
                assertEquals("The 'orbisgis:test:full' 'abstract' 'value' should be set to",
                        "en", process.getAbstract().getLang());

                boolean isMeta = false;
                boolean isProps = false;
                for(MetadataType metadataType : process.getMetadata()){
                    assertTrue("The 'orbisgis:test:full' 'metadata' should be set", metadataType.isSetRole());
                    if("website".equals(metadataType.getRole())){
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'abstractMetadata' should not be set",
                                metadataType.isSetAbstractMetaData());
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'about' should not be set",
                                metadataType.isSetAbout());
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'href' should not be set",
                                metadataType.isSetHref());
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'arcrole' should not be set",
                                metadataType.isSetArcrole());
                        assertTrue("The 'orbisgis:test:full' 'metadata' 'title' should not be set",
                                metadataType.isSetTitle());
                        assertEquals("The 'orbisgis:test:full' 'metadata' 'title' should not be set to 'metadata'",
                                "metadata", metadataType.getTitle());
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'show' should not be set",
                                metadataType.isSetShow());
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'actuate' should not be set",
                                metadataType.isSetActuate());
                        isMeta = true;
                    }
                    else if("prop1".equals(metadataType.getRole())){
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'abstractMetadata' should not be set",
                                metadataType.isSetAbstractMetaData());
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'about' should not be set",
                                metadataType.isSetAbout());
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'href' should not be set",
                                metadataType.isSetHref());
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'arcrole' should not be set",
                                metadataType.isSetArcrole());
                        assertTrue("The 'orbisgis:test:full' 'metadata' 'title' should not be set",
                                metadataType.isSetTitle());
                        assertEquals("The 'orbisgis:test:full' 'metadata' 'title' should not be set to 'value1'",
                                "value1", metadataType.getTitle());
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'show' should not be set",
                                metadataType.isSetShow());
                        assertFalse("The 'orbisgis:test:full' 'metadata' 'actuate' should not be set",
                                metadataType.isSetActuate());
                        isProps = true;
                    }
                    else{
                        fail("Unknown metadata");
                    }
                }
                assertTrue("The 'orbisgis:test:full' 'metadata' should have a value 'website'", isMeta);
                assertTrue("The 'orbisgis:test:full' 'metadata' should have a value 'props1'", isProps);

                assertFalse("The 'orbisgis:test:full' 'profile' should not be set", process.isSetProfile());

                assertTrue("The 'orbisgis:test:full' 'version' should be set", process.isSetProcessVersion());
                assertEquals("The 'orbisgis:test:full' 'version' should be set to '1.0.1'",
                        "1.0.1", process.getProcessVersion());

                assertTrue("The 'orbisgis:test:full' 'WSDL' should be set", process.isSetWSDL());
                assertTrue("The 'orbisgis:test:full' 'WSDL' 'href' should be set", process.getWSDL().isSetHref());
                assertEquals("The 'orbisgis:test:full' 'WSDL' 'href' should be set to 'href'",
                        "href", process.getWSDL().getHref());

                testInputs(process);
                testOutputs(process);

                assertTrue("The 'orbisgis:test:full' 'statusSupported' should be set", process.isSetStatusSupported());
                assertTrue("The 'orbisgis:test:full' 'statusSupported' should be set to 'true'",
                        process.isStatusSupported());

                assertTrue("The 'orbisgis:test:full' 'storeSupported' should be set", process.isSetStoreSupported());
                assertTrue("The 'orbisgis:test:full' 'storeSupported' should be set to 'true'",
                        process.isStoreSupported());
            }
            else{
                fail("Unknown process");
            }
        }
    }

    private void testInputs(ProcessDescriptionType process){
        assertTrue("The 'orbisgis:test:full' 'dataInputs' should be set", process.isSetDataInputs());
        assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' should be set",
                process.getDataInputs().isSetInput());
        for(InputDescriptionType input : process.getDataInputs().getInput()){
            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'identifier' should be set",
                    input.isSetIdentifier());
            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'title' should be set",
                    input.isSetTitle());
            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'minOccurs' should be set",
                    input.isSetMinOccurs());
            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'maxOccurs' should be set",
                    input.isSetMaxOccurs());
            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'inputFormCHoice' should be set",
                    input.isSetBoundingBoxData()^input.isSetLiteralData()^input.isSetComplexData());

            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' should be set",
                    input.isSetMetadata());
            assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' should have one value",
                    1, input.getMetadata().size());
            assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' 'actuate' should no be set",
                    input.getMetadata().get(0).isSetActuate());
            assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' 'show' should no be set",
                    input.getMetadata().get(0).isSetShow());
            assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' 'arcrole' should no be set",
                    input.getMetadata().get(0).isSetArcrole());
            assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' 'about' should no be set",
                    input.getMetadata().get(0).isSetAbout());
            assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' 'abstractMetadata' should no be set",
                    input.getMetadata().get(0).isSetAbstractMetaData());
            assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' 'href' should no be set",
                    input.getMetadata().get(0).isSetHref());
            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' 'role' should be set",
                    input.getMetadata().get(0).isSetRole());
            assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' 'role' should be set to 'website'",
                    "website", input.getMetadata().get(0).getRole());
            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' 'title' should be set",
                    input.getMetadata().get(0).isSetTitle());
            assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'metadata' 'title' should be set to 'metadata'",
                    "metadata", input.getMetadata().get(0).getTitle());

            assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'minOccurs' should be set to '0'",
                    BigInteger.valueOf(0), input.getMinOccurs());

            assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'maxOccurs' should be set to '2'",
                    BigInteger.valueOf(2), input.getMaxOccurs());

            String type = "";
            if("orbisgis:test:full:input:enumeration".equals(input.getIdentifier().getValue())){
                type = "Enumeration";
            }
            else if("orbisgis:test:full:input:geometry".equals(input.getIdentifier().getValue())){
                type = "Geometry";
            }
            else if("orbisgis:test:full:input:jdbctable".equals(input.getIdentifier().getValue())){
                type = "JDBCTable";
            }
            else if("orbisgis:test:full:input:jdbccolumn".equals(input.getIdentifier().getValue())){
                type = "JDBCColumn";
            }
            else if("orbisgis:test:full:input:jdbcvalue".equals(input.getIdentifier().getValue())){
                type = "JDBCValue";
            }
            else if("orbisgis:test:full:input:password".equals(input.getIdentifier().getValue())){
                type = "Password";
            }
            else if("orbisgis:test:full:input:rawdata".equals(input.getIdentifier().getValue())){
                type = "RawData";
            }
            else if("orbisgis:test:full:input:literaldatadouble".equals(input.getIdentifier().getValue())){
                type = "LiteralDataDouble";
            }
            else if("orbisgis:test:full:input:literaldatastring".equals(input.getIdentifier().getValue())){
                type = "LiteralDataString";
            }
            else if("orbisgis:test:full:input:boundingboxdata".equals(input.getIdentifier().getValue())){
                type = "BoundingBoxData";
            }
            else{
                fail("Unknown input : "+input.getIdentifier().getValue());
            }

            if("Geometry".equals(type) || "JDBCColumn".equals(type) ||
                    "JDBCValue".equals(type) || "Password".equals(type) || "RawData".equals(type)){
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' should be set",
                        input.isSetLiteralData());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' should not be set",
                        input.isSetBoundingBoxData());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' should not be set",
                        input.isSetComplexData());

                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'defaultValue' should be set",
                        input.getLiteralData().isSetDefaultValue());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' should be set",
                        input.getLiteralData().isSetDataType());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'value' should be set",
                        input.getLiteralData().getDataType().isSetValue());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'value' should be set to 'Double'",
                        "string", input.getLiteralData().getDataType().getValue());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'reference' should be set",
                        input.getLiteralData().getDataType().isSetReference());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'reference' should be set to 'https://www.w3.org/2001/XMLSchema#double'",
                        "https://www.w3.org/2001/XMLSchema#string", input.getLiteralData().getDataType().getReference());

                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should be set",
                        input.getLiteralData().isSetAnyValue());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should not be set",
                        input.getLiteralData().isSetValuesReference());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should not be set",
                        input.getLiteralData().isSetAllowedValues());
            }

            if("Enumeration".equals(type)){
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' should be set",
                        input.isSetLiteralData());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' should not be set",
                        input.isSetBoundingBoxData());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' should not be set",
                        input.isSetComplexData());

                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'defaultValue' should be set",
                        input.getLiteralData().isSetDefaultValue());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'defaultValue' should be set to '10.0'",
                        "value2", input.getLiteralData().getDefaultValue());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' should be set",
                        input.getLiteralData().isSetDataType());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'value' should be set",
                        input.getLiteralData().getDataType().isSetValue());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'value' should be set to 'Double'",
                        "string", input.getLiteralData().getDataType().getValue());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'reference' should be set",
                        input.getLiteralData().getDataType().isSetReference());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'reference' should be set to 'https://www.w3.org/2001/XMLSchema#double'",
                        "https://www.w3.org/2001/XMLSchema#string", input.getLiteralData().getDataType().getReference());

                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should not be set",
                        input.getLiteralData().isSetAnyValue());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should not be set",
                        input.getLiteralData().isSetValuesReference());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should be set",
                        input.getLiteralData().isSetAllowedValues());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' 'valueOrRange' should be set",
                        input.getLiteralData().getAllowedValues().isSetValueOrRange());
                for(Object obj : input.getLiteralData().getAllowedValues().getValueOrRange()){
                    if(obj instanceof ValueType){
                        ValueType value = (ValueType)obj;
                        assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'" +
                                " 'value' should be set", value.isSetValue());
                        if(!value.getValue().equalsIgnoreCase("value1") && !value.getValue().equalsIgnoreCase("value2")) {
                            fail("Unknown 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'");
                        }
                    }
                }
            }
            else if("JDBCTable".equals(type)){
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' should not be set",
                        input.isSetLiteralData());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' should not be set",
                        input.isSetBoundingBoxData());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' should be set",
                        input.isSetComplexData());

                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'maximumMegabytes' should be set",
                        input.getComplexData().isSetMaximumMegabytes());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'maximumMegabytes' should be set to '2000'",
                        BigInteger.valueOf(2000), input.getComplexData().getMaximumMegabytes());

                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'default' should be set",
                        input.getComplexData().isSetDefault());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'default' 'format' should be set",
                        input.getComplexData().getDefault().isSetFormat());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'default' 'format' 'mimeType' should be set",
                        input.getComplexData().getDefault().getFormat().isSetMimeType());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'default' 'format' 'mimeType' should be set to 'text/plain'",
                        "text/xml", input.getComplexData().getDefault().getFormat().getMimeType());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'default' 'format' 'schema' should be set",
                        input.getComplexData().getDefault().getFormat().isSetSchema());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'default' 'format' 'schema' should be ''",
                        "", input.getComplexData().getDefault().getFormat().getSchema());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'default' 'format' 'encoding' should be set",
                        input.getComplexData().getDefault().getFormat().isSetEncoding());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'default' 'format' 'encoding' should be set to 'simple'",
                        "simple", input.getComplexData().getDefault().getFormat().getEncoding());

                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' should be set",
                        input.getComplexData().isSetSupported());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' 'format' should be set",
                        input.getComplexData().getSupported().isSetFormat());

                boolean isPlain = false;
                boolean isGeojson = false;
                boolean isGml = false;
                boolean isXml = false;
                for(ComplexDataDescriptionType descriptionType : input.getComplexData().getSupported().getFormat()) {
                    assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' 'mimeType' should be set",
                            descriptionType.isSetMimeType());
                    assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' 'mimeType' should not be set",
                            descriptionType.isSetEncoding());
                    assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' 'mimeType' should not be set",
                            descriptionType.isSetSchema());
                    if("text/plain".equals(descriptionType.getMimeType())){
                        assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' " +
                                "'mimeType' should be set to 'text/plain'", "text/plain", descriptionType.getMimeType());
                        isPlain = true;
                    }
                    else if("application/geo+json".equals(descriptionType.getMimeType())){
                        assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' " +
                                        "'mimeType' should be set to 'application/geo+json'", "application/geo+json",
                                descriptionType.getMimeType());
                        assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' " +
                                        "'schema' should be set to 'https://tools.ietf.org/html/rfc7946'", "https://tools.ietf.org/html/rfc7946",
                                descriptionType.getSchema());
                        isGeojson = true;
                    }
                    else if("application/gml+xml".equals(descriptionType.getMimeType())){
                        assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' " +
                                        "'mimeType' should be set to 'application/gml+xml'", "application/gml+xml",
                                descriptionType.getMimeType());
                        assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' " +
                                        "'schema' should be set to 'https://tools.ietf.org/html/rfc7303'", "https://tools.ietf.org/html/rfc7303",
                                descriptionType.getSchema());
                        isGml = true;
                    }
                    else if("text/xml".equals(descriptionType.getMimeType())){
                        assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' " +
                                        "'mimeType' should be set to 'text/xml'", "text/xml",
                                descriptionType.getMimeType());
                        assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' " +
                                        "'schema' should be set to ''", "",
                                descriptionType.getSchema());
                        isXml = true;
                    }
                }
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' " +
                        "should contains 'text/plain", isPlain);
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' " +
                        "should contains 'application/vnd.geo+json", isGeojson);
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' " +
                        "should contains 'application/gml+xml", isGml);
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' 'supported' " +
                        "should contains 'text/xml", isXml);
            }
            else if("LiteralDataDouble".equals(type)){
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' should be set",
                        input.isSetLiteralData());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' should not be set",
                        input.isSetBoundingBoxData());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' should not be set",
                        input.isSetComplexData());

                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'UOM' should not be set",
                        input.getLiteralData().isSetUOMs());

                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'defaultValue' should be set",
                        input.getLiteralData().isSetDefaultValue());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'defaultValue' should be set to '10.0'",
                        "10.0", input.getLiteralData().getDefaultValue());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' should be set",
                        input.getLiteralData().isSetDataType());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'value' should be set",
                        input.getLiteralData().getDataType().isSetValue());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'value' should be set to 'Double'",
                        "double", input.getLiteralData().getDataType().getValue());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'reference' should be set",
                        input.getLiteralData().getDataType().isSetReference());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'reference' should be set to 'https://www.w3.org/2001/XMLSchema#double'",
                        "https://www.w3.org/2001/XMLSchema#double", input.getLiteralData().getDataType().getReference());

                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should not be set",
                        input.getLiteralData().isSetAnyValue());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should not be set",
                        input.getLiteralData().isSetValuesReference());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should be set",
                        input.getLiteralData().isSetAllowedValues());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' 'valueOrRange' should be set",
                        input.getLiteralData().getAllowedValues().isSetValueOrRange());
                for(Object obj : input.getLiteralData().getAllowedValues().getValueOrRange()){
                    if(obj instanceof ValueType){
                        ValueType value = (ValueType)obj;
                        assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'" +
                                " 'value' should be set", value.isSetValue());
                        assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'" +
                                " 'value' should be set to '20'", "20", value.getValue());
                    }
                    if(obj instanceof RangeType){
                        RangeType range = (RangeType)obj;
                        assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'" +
                                " 'range' 'rangeClosure' should be set", range.isSetRangeClosure());
                        assertArrayEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'" +
                                " 'range' 'rangeClosure' should be set to [closed]", new String[]{"closed"}, range.getRangeClosure().toArray());
                        assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'" +
                                " 'range' 'spacing' should be set", range.isSetSpacing());
                        assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'" +
                                " 'range' 'spacing' should be set to '2'", "2", range.getSpacing().getValue());
                        assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'" +
                                " 'range' 'maximumValue' should be set", range.isSetMaximumValue());
                        assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'" +
                                " 'range' 'maximumValue' should be set to '14'", "14", range.getMaximumValue().getValue());
                        assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'" +
                                " 'range' 'minimumValue' should be set", range.isSetMinimumValue());
                        assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue'" +
                                " 'range' 'minimumValue' should be set to '0'", "0", range.getMinimumValue().getValue());
                    }
                }
            }
            else if("LiteralDataString".equals(type)){
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' should be set",
                        input.isSetLiteralData());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' should not be set",
                        input.isSetBoundingBoxData());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' should not be set",
                        input.isSetComplexData());

                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'UOM' should not be set",
                        input.getLiteralData().isSetUOMs());

                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'defaultValue' should be set",
                        input.getLiteralData().isSetDefaultValue());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'defaultValue' should be set to 'dflt'",
                        "dflt", input.getLiteralData().getDefaultValue());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' should be set",
                        input.getLiteralData().isSetDataType());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'value' should be set",
                        input.getLiteralData().getDataType().isSetValue());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'value' should be set to 'String'",
                        "string", input.getLiteralData().getDataType().getValue());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'reference' should be set",
                        input.getLiteralData().getDataType().isSetReference());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'dataType' 'reference' should be set to 'https://www.w3.org/2001/XMLSchema#string'",
                        "https://www.w3.org/2001/XMLSchema#string", input.getLiteralData().getDataType().getReference());

                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should be set",
                        input.getLiteralData().isSetAnyValue());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should not be set",
                        input.getLiteralData().isSetValuesReference());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' 'allowedValue' should not be set",
                        input.getLiteralData().isSetAllowedValues());
            }
            else if("BoundingBoxData".equals(type)){
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'literalData' should not be set",
                        input.isSetLiteralData());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' should be set",
                        input.isSetBoundingBoxData());
                assertFalse("The 'orbisgis:test:full' 'dataInputs' 'input' 'complexData' should not be set",
                        input.isSetComplexData());

                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' 'default' should be set",
                        input.getBoundingBoxData().isSetDefault());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' 'default' 'crs' should be set",
                        input.getBoundingBoxData().getDefault().isSetCRS());
                assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' 'default' 'crs' should be set",
                        "EPSG:4326", input.getBoundingBoxData().getDefault().getCRS());

                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' 'supported' should be set",
                        input.getBoundingBoxData().isSetSupported());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' 'supported' 'crs' should be set",
                        input.getBoundingBoxData().getSupported().isSetCRS());
                assertArrayEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'boundingBox' 'supported' 'crs' should be set to '[EPSG:4326]'",
                        new String[]{"EPSG:4326", "EPSG:2000"}, input.getBoundingBoxData().getSupported().getCRS().toArray());
            }

            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'title' 'value' should be set",
                    input.getTitle().isSetValue());
            assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'title' 'value' should be set to" +
                    " 'Input "+type+"'", "Input "+type, input.getTitle().getValue());
            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'title' 'lang' should be set",
                    input.getTitle().isSetLang());
            assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'title' 'lang' should be set to 'en'",
                    "en", input.getTitle().getLang());

            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'abstract' should be set",
                    input.isSetAbstract());
            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'abstract' 'value' should be set",
                    input.getAbstract().isSetValue());
            assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'abstract' 'value' should be set to" +
                    " 'A "+type+" input.'", "A "+type+" input.", input.getAbstract().getValue());
            assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'abstract' 'lang' should be set",
                    input.getAbstract().isSetLang());
            assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'abstract' 'lang' should be set to 'en'",
                    "en", input.getAbstract().getLang());
        }
    }

    private void testOutputs(ProcessDescriptionType process){
        assertTrue("The 'orbisgis:test:full' 'dataOutputs' should be set", process.isSetProcessOutputs());
        assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' should be set",
                process.getProcessOutputs().isSetOutput());
        for(OutputDescriptionType output : process.getProcessOutputs().getOutput()){
            assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'identifier' should be set",
                    output.isSetIdentifier());
            assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'title' should be set",
                    output.isSetTitle());
            assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'outputFormCHoice' should be set",
                    output.isSetBoundingBoxOutput()^output.isSetLiteralOutput()^output.isSetComplexOutput());

            assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' should be set",
                    output.isSetMetadata());
            assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' should have one value",
                    1, output.getMetadata().size());
            assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' 'actuate' should no be set",
                    output.getMetadata().get(0).isSetActuate());
            assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' 'show' should no be set",
                    output.getMetadata().get(0).isSetShow());
            assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' 'arcrole' should no be set",
                    output.getMetadata().get(0).isSetArcrole());
            assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' 'about' should no be set",
                    output.getMetadata().get(0).isSetAbout());
            assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' 'abstractMetadata' should no be set",
                    output.getMetadata().get(0).isSetAbstractMetaData());
            assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' 'href' should no be set",
                    output.getMetadata().get(0).isSetHref());
            assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' 'role' should be set",
                    output.getMetadata().get(0).isSetRole());
            assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' 'role' should be set to 'website'",
                    "website", output.getMetadata().get(0).getRole());
            assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' 'title' should be set",
                    output.getMetadata().get(0).isSetTitle());
            assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'metadata' 'title' should be set to 'metadata'",
                    "metadata", output.getMetadata().get(0).getTitle());

            String type = "";
            if("orbisgis:test:full:output:enumeration".equals(output.getIdentifier().getValue())){
                type = "Enumeration";
            }
            else if("orbisgis:test:full:output:geometry".equals(output.getIdentifier().getValue())){
                type = "Geometry";
            }
            else if("orbisgis:test:full:output:jdbctable".equals(output.getIdentifier().getValue())){
                type = "JDBCTable";
            }
            else if("orbisgis:test:full:output:jdbccolumn".equals(output.getIdentifier().getValue())){
                type = "JDBCColumn";
            }
            else if("orbisgis:test:full:output:jdbcvalue".equals(output.getIdentifier().getValue())){
                type = "JDBCValue";
            }
            else if("orbisgis:test:full:output:password".equals(output.getIdentifier().getValue())){
                type = "Password";
            }
            else if("orbisgis:test:full:output:rawdata".equals(output.getIdentifier().getValue())){
                type = "RawData";
            }
            else if("orbisgis:test:full:output:literaldatadouble".equals(output.getIdentifier().getValue())){
                type = "LiteralDataDouble";
            }
            else if("orbisgis:test:full:output:literaldatastring".equals(output.getIdentifier().getValue())){
                type = "LiteralDataString";
            }
            else if("orbisgis:test:full:output:boundingboxdata".equals(output.getIdentifier().getValue())){
                type = "BoundingBoxData";
            }
            else{
                fail("Unknown output");
            }

            if("JDBCTable".equals(type)){
                assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' should not be set",
                        output.isSetLiteralOutput());
                assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'boundingBox' should not be set",
                        output.isSetBoundingBoxOutput());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' should be set",
                        output.isSetComplexOutput());

                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'default' should be set",
                        output.getComplexOutput().isSetDefault());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'default' 'format' should be set",
                        output.getComplexOutput().getDefault().isSetFormat());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'default' 'format' 'mimeType' should be set",
                        output.getComplexOutput().getDefault().getFormat().isSetMimeType());
                assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'default' 'format' 'mimeType' should be set to 'text/xml'",
                        "text/xml", output.getComplexOutput().getDefault().getFormat().getMimeType());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'default' 'format' 'schema' should be set",
                        output.getComplexOutput().getDefault().getFormat().isSetSchema());
                assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'default' 'format' 'schema' should be set to ''",
                        "", output.getComplexOutput().getDefault().getFormat().getSchema());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'default' 'format' 'encoding' should be set",
                        output.getComplexOutput().getDefault().getFormat().isSetEncoding());
                assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'default' 'format' 'encoding' should be set 'simple'",
                        "simple", output.getComplexOutput().getDefault().getFormat().getEncoding());

                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' should be set",
                        output.getComplexOutput().isSetSupported());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' 'format' should be set",
                        output.getComplexOutput().getSupported().isSetFormat());

                boolean isPlain = false;
                boolean isGeojson = false;
                boolean isGml = false;
                for(ComplexDataDescriptionType descriptionType : output.getComplexOutput().getSupported().getFormat()) {
                    assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' 'mimeType' should be set",
                            descriptionType.isSetMimeType());
                    assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' 'encoding' should be set",
                            descriptionType.isSetEncoding());
                    assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' 'encoding' should be set to 'simple'",
                            "simple", descriptionType.getEncoding());
                    assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' 'schema' should be set",
                            descriptionType.isSetSchema());
                    if("text/plain".equals(descriptionType.getMimeType())){
                        assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' " +
                                "'mimeType' should be set to 'text/plain'", "text/plain", descriptionType.getMimeType());
                        isPlain = true;
                    }
                    else if("application/geo+json".equals(descriptionType.getMimeType())){
                        assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' " +
                                        "'mimeType' should be set to 'application/geo+json'", "application/geo+json",
                                descriptionType.getMimeType());
                        assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' " +
                                        "'schema' should be set to 'https://tools.ietf.org/html/rfc7946'", "https://tools.ietf.org/html/rfc7946",
                                descriptionType.getSchema());
                        isGeojson = true;
                    }
                    else if("application/gml+xml".equals(descriptionType.getMimeType())){
                        assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' " +
                                        "'mimeType' should be set to 'application/gml+xml'", "application/gml+xml",
                                descriptionType.getMimeType());
                        assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' " +
                                "'schema' should be set to 'https://tools.ietf.org/html/rfc7303'", "https://tools.ietf.org/html/rfc7303", descriptionType.getSchema());
                        isGml = true;
                    }
                }
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' " +
                        "should contains 'text/plain", isPlain);
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' " +
                        "should contains 'application/geo+json", isGeojson);
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' 'supported' " +
                        "should contains 'application/gml+xml", isGml);
            }
            else if("LiteralDataDouble".equals(type)){
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' should be set",
                        output.isSetLiteralOutput());
                assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'boundingBox' should not be set",
                        output.isSetBoundingBoxOutput());
                assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' should not be set",
                        output.isSetComplexOutput());

                assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'UOM' should not be set",
                        output.getLiteralOutput().isSetUOMs());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'dataType' should be set",
                        output.getLiteralOutput().isSetDataType());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'dataType' 'value' should be set",
                        output.getLiteralOutput().getDataType().isSetValue());
                assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'dataType' 'value' should be set to 'Double'",
                        "double", output.getLiteralOutput().getDataType().getValue());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'dataType' 'reference' should be set",
                        output.getLiteralOutput().getDataType().isSetReference());
                assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'dataType' 'reference' should be set to 'https://www.w3.org/2001/XMLSchema#double'",
                        "https://www.w3.org/2001/XMLSchema#double", output.getLiteralOutput().getDataType().getReference());
            }
            else if("Enumeration".equals(type) || "Geometry".equals(type) || "JDBCColumn".equals(type) ||
                    "JDBCValue".equals(type) || "Password".equals(type) || "RawData".equals(type) ||
                    "LiteralDataString".equals(type)){
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' should be set",
                        output.isSetLiteralOutput());
                assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'boundingBox' should not be set",
                        output.isSetBoundingBoxOutput());
                assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' should not be set",
                        output.isSetComplexOutput());

                assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'UOM' should not be set",
                        output.getLiteralOutput().isSetUOMs());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'dataType' should be set",
                        output.getLiteralOutput().isSetDataType());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'dataType' 'value' should be set",
                        output.getLiteralOutput().getDataType().isSetValue());
                assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'dataType' 'value' should be set to 'String'",
                        "string", output.getLiteralOutput().getDataType().getValue());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'dataType' 'reference' should be set",
                        output.getLiteralOutput().getDataType().isSetReference());
                assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' 'dataType' 'reference' should be set to 'https://www.w3.org/2001/XMLSchema#string'",
                        "https://www.w3.org/2001/XMLSchema#string", output.getLiteralOutput().getDataType().getReference());
            }
            else if("BoundingBoxData".equals(type)){
                assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'literalOutput' should not be set",
                        output.isSetLiteralOutput());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'boundingBox' should be set",
                        output.isSetBoundingBoxOutput());
                assertFalse("The 'orbisgis:test:full' 'dataOutputs' 'output' 'complexOutput' should not be set",
                        output.isSetComplexOutput());

                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'boundingBox' 'default' should be set",
                        output.getBoundingBoxOutput().isSetDefault());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'boundingBox' 'default' 'crs' should be set",
                        output.getBoundingBoxOutput().getDefault().isSetCRS());
                assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'boundingBox' 'default' 'crs' should be set",
                        "EPSG:4326", output.getBoundingBoxOutput().getDefault().getCRS());

                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'boundingBox' 'supported' should be set",
                        output.getBoundingBoxOutput().isSetSupported());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'boundingBox' 'supported' 'crs' should be set",
                        output.getBoundingBoxOutput().getSupported().isSetCRS());
                assertArrayEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'boundingBox' 'supported' 'crs' should be set to '[EPSG:4326]'",
                        new String[]{"EPSG:4326", "EPSG:2000"}, output.getBoundingBoxOutput().getSupported().getCRS().toArray());
            }

            assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'title' 'value' should be set",
                    output.getTitle().isSetValue());
            assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'title' 'value' should be set to" +
                    " 'Output "+type+"'", "Output "+type, output.getTitle().getValue());
            assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'title' 'lang' should be set",
                    output.getTitle().isSetLang());
            assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'title' 'lang' should be set to 'en'",
                    "en", output.getTitle().getLang());

            assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'abstract' should be set",
                    output.isSetAbstract());
            assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'abstract' 'value' should be set",
                    output.getAbstract().isSetValue());
            assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'abstract' 'value' should be set to" +
                    " 'A "+type+" output'", "A "+type+" output.", output.getAbstract().getValue());
            assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'abstract' 'lang' should be set",
                    output.getAbstract().isSetLang());
            assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'abstract' 'lang' should be set to 'en'",
                    "en", output.getAbstract().getLang());
        }
    }

    /**
     * Test an empty DescribeProcess request with a full WPS property and a full wps script
     */
    @Test
    public void testMinPropsFullScriptEmptyDescribeProcess(){
        //Ask for the DescribeProcess
        DescribeProcess describeProcess = new DescribeProcess();
        CodeType codeType = new CodeType();
        codeType.setValue("orbisgis:test:full");
        describeProcess.getIdentifier().add(codeType);
        Object object = minWps100Operations.describeProcess(describeProcess);
        assertTrue("The wps service answer should be 'ProcessDescriptions",object instanceof ProcessDescriptions);
        ProcessDescriptions processDescriptions = (ProcessDescriptions)object;

        //lang tests
        assertTrue("The wps service 'processDescriptions' 'lang' should be set", processDescriptions.isSetLang());
        assertEquals("The wps service 'processDescriptions' 'lang' should be 'en'", "en", processDescriptions.getLang());

        //ProcessOffering test
        assertTrue("The wps service 'processDescriptions' 'processDescription' should be set",
                processDescriptions.isSetProcessDescription());
        for(ProcessDescriptionType process : processDescriptions.getProcessDescription()){
            assertTrue("The 'processDescriptions' 'processDescription' 'process' 'id' should be set",
                    process.isSetIdentifier());

            assertFalse("The 'orbisgis:test:full' 'profile' should not be set", process.isSetProfile());

            assertFalse("The 'orbisgis:test:full' 'WSDL' should be set", process.isSetWSDL());

            assertTrue("The 'orbisgis:test:full' 'statusSupported' should be set", process.isSetStatusSupported());
            assertFalse("The 'orbisgis:test:full' 'statusSupported' should be set to 'false'",
                    process.isStatusSupported());

            assertTrue("The 'orbisgis:test:full' 'storeSupported' should be set", process.isSetStoreSupported());
            assertFalse("The 'orbisgis:test:full' 'storeSupported' should be set to 'false'",
                    process.isStoreSupported());
        }
    }

    /**
     * Test an empty DescribeProcess request with a full WPS property and a full wps script
     */
    @Test
    public void testMinPropsFullScriptFrDescribeProcess(){
        //Ask for the DescribeProcess
        DescribeProcess describeProcess = new DescribeProcess();
        CodeType codeType = new CodeType();
        codeType.setValue("orbisgis:test:full");
        describeProcess.getIdentifier().add(codeType);
        describeProcess.setLanguage("fr-fr");
        Object object = minWps100Operations.describeProcess(describeProcess);
        assertTrue("The wps service answer should be 'ProcessDescriptions",object instanceof ProcessDescriptions);
        ProcessDescriptions processDescriptions = (ProcessDescriptions)object;

        //lang tests
        assertTrue("The wps service 'processDescriptions' 'lang' should be set", processDescriptions.isSetLang());
        assertEquals("The wps service 'processDescriptions' 'lang' should be 'en'", "fr-fr", processDescriptions.getLang());

        //ProcessOffering test
        assertTrue("The wps service 'processDescriptions' 'processDescription' should be set",
                processDescriptions.isSetProcessDescription());
        for(ProcessDescriptionType process : processDescriptions.getProcessDescription()){
            assertTrue("The 'processDescriptions' 'processDescription' 'process' 'id' should be set",
                    process.isSetIdentifier());
            if("orbisgis:test:full".equals(process.getIdentifier().getValue())) {
                assertTrue("The 'orbisgis:test:full' 'title' should be set", process.isSetTitle());
                assertTrue("The 'orbisgis:test:full' 'title' 'value' should be set", process.getTitle().isSetValue());
                assertEquals("The 'orbisgis:test:full' 'title' 'value' should be set to",
                        "full test script", process.getTitle().getValue());
                assertTrue("The 'orbisgis:test:full' 'title' 'value' should be set", process.getTitle().isSetLang());
                assertEquals("The 'orbisgis:test:full' 'title' 'value' should be set to",
                        "fr-fr", process.getTitle().getLang());

                assertTrue("The 'orbisgis:test:full' 'abstract' should be set", process.isSetAbstract());
                assertTrue("The 'orbisgis:test:full' 'abstract' 'value' should be set",
                        process.getAbstract().isSetValue());
                assertEquals("The 'orbisgis:test:full' 'abstract' 'value' should be set to",
                        "Full test script descr.", process.getAbstract().getValue());
                assertTrue("The 'orbisgis:test:full' 'abstract' 'value' should be set",
                        process.getAbstract().isSetLang());
                assertEquals("The 'orbisgis:test:full' 'abstract' 'value' should be set to",
                        "fr-fr", process.getAbstract().getLang());

                assertTrue("The 'orbisgis:test:full' 'dataInputs' should be set", process.isSetDataInputs());
                assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' should be set",
                        process.getDataInputs().isSetInput());
                for(InputDescriptionType input : process.getDataInputs().getInput()){
                    assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'identifier' should be set",
                            input.isSetIdentifier());
                    assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'title' should be set",
                            input.isSetTitle());


                    assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'title' 'value' should be set",
                            input.getTitle().isSetValue());
                    assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'title' 'lang' should be set",
                            input.getTitle().isSetLang());
                    assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'title' 'lang' should be set to 'en'",
                            "fr-fr", input.getTitle().getLang());

                    assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'abstract' should be set",
                            input.isSetAbstract());
                    assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'abstract' 'value' should be set",
                            input.getAbstract().isSetValue());
                    assertTrue("The 'orbisgis:test:full' 'dataInputs' 'input' 'abstract' 'lang' should be set",
                            input.getAbstract().isSetLang());
                    assertEquals("The 'orbisgis:test:full' 'dataInputs' 'input' 'abstract' 'lang' should be set to 'en'",
                            "fr-fr", input.getAbstract().getLang());
                }
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' should be set", process.isSetProcessOutputs());
                assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' should be set",
                        process.getProcessOutputs().isSetOutput());
                for(OutputDescriptionType output : process.getProcessOutputs().getOutput()){
                    assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'title' 'value' should be set",
                            output.getTitle().isSetValue());
                    assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'title' 'lang' should be set",
                            output.getTitle().isSetLang());
                    assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'title' 'lang' should be set to 'en'",
                            "fr-fr", output.getTitle().getLang());

                    assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'abstract' should be set",
                            output.isSetAbstract());
                    assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'abstract' 'value' should be set",
                            output.getAbstract().isSetValue());
                    assertTrue("The 'orbisgis:test:full' 'dataOutputs' 'output' 'abstract' 'lang' should be set",
                            output.getAbstract().isSetLang());
                    assertEquals("The 'orbisgis:test:full' 'dataOutputs' 'output' 'abstract' 'lang' should be set to 'en'",
                            "fr-fr", output.getAbstract().getLang());
                }
            }
            else{
                fail("Unknown process");
            }
        }
    }

    /**
     * Test an empty DescribeProcess request with a full WPS property and a full wps script
     */
    @Test
    public void testBadDescribeProcess(){
        //Ask for the DescribeProcess without code type
        DescribeProcess describeProcess = new DescribeProcess();
        Object object = minWps100Operations.describeProcess(describeProcess);
        assertTrue("The wps service answer should be 'ExceptionReport",object instanceof ExceptionReport);
        ExceptionReport exceptionReport = (ExceptionReport)object;
        assertTrue("The ExceptionReport should contains an exception", exceptionReport.isSetException());
        assertEquals("The ExceptionReport should contains one exception", 1, exceptionReport.getException().size());
        assertTrue("The Exception code should be set", exceptionReport.getException().get(0).isSetExceptionCode());
        assertEquals("The Exception code should be set", "MissingParameterValue", exceptionReport.getException().get(0).getExceptionCode());
        assertTrue("The Exception locator should be set", exceptionReport.getException().get(0).isSetLocator());
        assertEquals("The Exception locator should be set", "Identifier", exceptionReport.getException().get(0).getLocator());

        //Ask for the DescribeProcess with a bad code type
        describeProcess = new DescribeProcess();
        CodeType codeType = new CodeType();
        codeType.setValue("UnicornId");
        describeProcess.getIdentifier().add(codeType);
        object = minWps100Operations.describeProcess(describeProcess);
        assertTrue("The wps service answer should be 'ExceptionReport",object instanceof ExceptionReport);
        exceptionReport = (ExceptionReport)object;
        assertTrue("The ExceptionReport should contains an exception", exceptionReport.isSetException());
        assertEquals("The ExceptionReport should contains one exception", 1, exceptionReport.getException().size());
        assertTrue("The Exception code should be set", exceptionReport.getException().get(0).isSetExceptionCode());
        assertEquals("The Exception code should be set", "InvalidParameterValue", exceptionReport.getException().get(0).getExceptionCode());
        assertTrue("The Exception locator should be set", exceptionReport.getException().get(0).isSetLocator());
        assertEquals("The Exception locator should be set", "Identifier+UnicornId", exceptionReport.getException().get(0).getLocator());

        //Ask for the DescribeProcess with a bad language
        describeProcess = new DescribeProcess();
        codeType = new CodeType();
        codeType.setValue("orbisgis:test:full");
        describeProcess.getIdentifier().add(codeType);
        describeProcess.setLanguage("Uni-co-rn");
        object = minWps100Operations.describeProcess(describeProcess);
        assertTrue("The wps service answer should be 'ExceptionReport",object instanceof ExceptionReport);
        exceptionReport = (ExceptionReport)object;
        assertTrue("The ExceptionReport should contains an exception", exceptionReport.isSetException());
        assertEquals("The ExceptionReport should contains one exception", 1, exceptionReport.getException().size());
        assertTrue("The Exception code should be set", exceptionReport.getException().get(0).isSetExceptionCode());
        assertEquals("The Exception code should be set", "InvalidParameterValue", exceptionReport.getException().get(0).getExceptionCode());
        assertTrue("The Exception locator should be set", exceptionReport.getException().get(0).isSetLocator());
        assertEquals("The Exception locator should be set", "Language+Uni-co-rn", exceptionReport.getException().get(0).getLocator());
    }
}
