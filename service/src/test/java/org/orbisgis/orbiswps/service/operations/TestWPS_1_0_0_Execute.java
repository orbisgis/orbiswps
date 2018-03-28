package org.orbisgis.orbiswps.service.operations;

import net.opengis.ows._1.BoundingBoxType;
import net.opengis.ows._1.CodeType;
import net.opengis.ows._1.ExceptionReport;
import net.opengis.wps._1_0_0.*;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.service.process.ProcessManager;
import org.orbisgis.orbiswps.serviceapi.operations.WPS_1_0_0_Operations;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for the WPS_2_0_OperationsImpl
 *
 * @author Sylvain PALOMINOS
 */
public class TestWPS_1_0_0_Execute {

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
            url = this.getClass().getResource("simpleScript.groovy");
            assertNotNull("Unable to load the script 'simpleScript.groovy'", url);
            f = new File(url.toURI());
            wpsServer.addProcess(f);
            processManager.addScript(f.toURI());
        } catch (URISyntaxException e) {
            fail("Error on loading the scripts : "+e.getMessage());
        }

        assertNotNull("Unable to load the file 'minWpsService100.json'",
                TestWPS_1_0_0_Execute.class.getResource("minWpsService100.json").getFile());
        WpsServerProperties_1_0_0 minWpsProps = new WpsServerProperties_1_0_0(
                TestWPS_1_0_0_Execute.class.getResource("minWpsService100.json").getFile());
        minWps100Operations =  new WPS_1_0_0_OperationsImpl(wpsServer, minWpsProps, processManager);

        assertNotNull("Unable to load the file 'fullWpsService100.json'",
                TestWPS_1_0_0_Execute.class.getResource("fullWpsService100.json").getFile());
        WpsServerProperties_1_0_0 fullWpsProps = new WpsServerProperties_1_0_0(
                TestWPS_1_0_0_Execute.class.getResource("fullWpsService100.json").getFile());
        fullWps100Operations =  new WPS_1_0_0_OperationsImpl(wpsServer, fullWpsProps, processManager);
    }

    /**
     * Test an empty GetCapabilities request with a full WPS property
     */
    @Test
    public void testBadExecute(){
        //Test Execute without identifier
        Execute execute = new Execute();
        Object object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        ExceptionReport report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'MissingParameterValue'", "MissingParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'Identifier'", "Identifier",
                report.getException().get(0).getLocator());

        //Test Execute with bad identifier
        execute = new Execute();
        CodeType codeType = new CodeType();
        codeType.setValue("unicorn:process");
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'Identifier'", "Identifier",
                report.getException().get(0).getLocator());


        codeType = new CodeType();
        codeType.setValue("orbisgis:test:full");

        ////
        //DataInputs test
        ////

        //Test Execute with empty dataInputs
        execute = new Execute();
        execute.setIdentifier(codeType);
        DataInputsType dataInputsType = new DataInputsType();
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input without identifier
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        InputType inputType = new InputType();
        DataType dataType = new DataType();
        dataType.setLiteralData(new LiteralDataType());
        inputType.setData(dataType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input without DataType
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        CodeType inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input with an empty dataType
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        inputType.setData(new DataType());
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input with a too full dataType
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        dataType = new DataType();
        dataType.setLiteralData(new LiteralDataType());
        dataType.setBoundingBoxData(new BoundingBoxType());
        dataType.setComplexData(new ComplexDataType());
        inputType.setData(dataType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input with a bad reference
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        InputReferenceType inputReferenceType = new InputReferenceType();
        inputType.setReference(inputReferenceType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with an input with a bad reference format
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        inputReferenceType = new InputReferenceType();
        inputReferenceType.setHref("file:href");
        inputReferenceType.setMimeType("unicorn/type");
        inputType.setReference(inputReferenceType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with bad complexData
        execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        CodeType rawInputCodeType = new CodeType();
        rawInputCodeType.setValue("orbisgis:test:full:input:rawdata");
        inputType.setIdentifier(rawInputCodeType);
        dataType = new DataType();
        ComplexDataType complexDataType = new ComplexDataType();
        dataType.setComplexData(complexDataType);
        inputType.setData(dataType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with ComplexData with unknown input format
        execute = new Execute();
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        dataType = new DataType();
        complexDataType = new ComplexDataType();
        complexDataType.getOtherAttributes().put(new QName("string"), "test");
        complexDataType.setMimeType("unicorn/type");
        dataType.setComplexData(complexDataType);
        inputType.setData(dataType);
        rawInputCodeType = new CodeType();
        rawInputCodeType.setValue("orbisgis:test:full:input:rawdata");
        inputType.setIdentifier(rawInputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with LiteralData with unknown dataType
        execute = new Execute();
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        dataType = new DataType();
        LiteralDataType literalDataType = new LiteralDataType();
        literalDataType.setDataType("unicornType");
        literalDataType.setValue("value");
        dataType.setLiteralData(literalDataType);
        inputType.setData(dataType);
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with LiteralData with no value
        execute = new Execute();
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        dataType = new DataType();
        literalDataType = new LiteralDataType();
        literalDataType.setDataType(org.orbisgis.orbiswps.service.model.DataType.DOUBLE.getUri().toString());
        dataType.setLiteralData(literalDataType);
        inputType.setData(dataType);
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:literaldatadouble");
        inputType.setIdentifier(inputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with BoundingBox without CRS
        execute = new Execute();
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        dataType = new DataType();
        BoundingBoxType boundingBoxType = new BoundingBoxType();
        boundingBoxType.getLowerCorner().add(0.0);
        boundingBoxType.getLowerCorner().add(1.0);
        boundingBoxType.getUpperCorner().add(0.0);
        boundingBoxType.getUpperCorner().add(1.0);
        dataType.setBoundingBoxData(boundingBoxType);
        inputType.setData(dataType);
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:boundingboxdata");
        inputType.setIdentifier(inputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute with bad BoundingBox without corners
        execute = new Execute();
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        dataType = new DataType();
        boundingBoxType = new BoundingBoxType();
        boundingBoxType.setCrs("http://www.opengis.net/def/crs/EPSG/8.9.2/4326");
        dataType.setBoundingBoxData(boundingBoxType);
        inputType.setData(dataType);
        inputCodeType = new CodeType();
        inputCodeType.setValue("orbisgis:test:full:input:boundingboxdata");
        inputType.setIdentifier(inputCodeType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());

        //Test Execute without a mandatory input
        execute = new Execute();
        CodeType simpleCodeType = new CodeType();
        simpleCodeType.setValue("orbisgis:test:simple");
        execute.setIdentifier(simpleCodeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'exceptionCode' should be set to 'DataInputs'", "DataInputs",
                report.getException().get(0).getLocator());


        ////
        //ResponseForm test
        ////

        //Test Execute with empty responseForm
        execute = new Execute();
        execute.setIdentifier(codeType);
        ResponseFormType responseFormType = new ResponseFormType();
        execute.setResponseForm(responseFormType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());

        //Test Execute with too full responseForm
        execute = new Execute();
        execute.setIdentifier(codeType);
        responseFormType = new ResponseFormType();
        responseFormType.setResponseDocument(new ResponseDocumentType());
        responseFormType.setRawDataOutput(new OutputDefinitionType());
        execute.setResponseForm(responseFormType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());

        //Test Execute with storageNotSupported
        execute = new Execute();
        execute.setIdentifier(codeType);
        responseFormType = new ResponseFormType();
        ResponseDocumentType responseDocumentType = new ResponseDocumentType();
        responseDocumentType.setStoreExecuteResponse(true);
        DocumentOutputDefinitionType output = new DocumentOutputDefinitionType();
        CodeType outputCodeType = new CodeType();
        outputCodeType.setValue("orbisgis:test:full:output:rawdata");
        output.setIdentifier(outputCodeType);
        responseDocumentType.getOutput().add(output);
        responseFormType.setResponseDocument(responseDocumentType);
        execute.setResponseForm(responseFormType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'StorageNotSupported'", "StorageNotSupported",
                report.getException().get(0).getExceptionCode());

        //Test Execute with statusNotSupported
        execute = new Execute();
        execute.setIdentifier(codeType);
        responseFormType = new ResponseFormType();
        responseDocumentType = new ResponseDocumentType();
        responseDocumentType.setStatus(true);
        output = new DocumentOutputDefinitionType();
        outputCodeType = new CodeType();
        outputCodeType.setValue("orbisgis:test:full:output:rawdata");
        output.setIdentifier(outputCodeType);
        responseDocumentType.getOutput().add(output);
        responseFormType.setResponseDocument(responseDocumentType);
        execute.setResponseForm(responseFormType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());

        //Test Execute with responseDocument with output without identifier
        execute = new Execute();
        responseFormType = new ResponseFormType();
        responseDocumentType = new ResponseDocumentType();
        DocumentOutputDefinitionType outputDefinitionsType = new DocumentOutputDefinitionType();
        responseDocumentType.getOutput().add(outputDefinitionsType);
        responseFormType.setResponseDocument(responseDocumentType);
        execute.setResponseForm(responseFormType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());

        //Test Execute with responseDocument with unknown output format
        execute = new Execute();
        responseFormType = new ResponseFormType();
        responseDocumentType = new ResponseDocumentType();
        outputDefinitionsType = new DocumentOutputDefinitionType();
        outputDefinitionsType.setMimeType("unicorn/type");
        CodeType rawOutputCodeType = new CodeType();
        rawOutputCodeType.setValue("orbisgis:test:full:output:rawdata");
        outputDefinitionsType.setIdentifier(rawOutputCodeType);
        responseDocumentType.getOutput().add(outputDefinitionsType);
        responseFormType.setResponseDocument(responseDocumentType);
        execute.setResponseForm(responseFormType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());
        assertTrue("The exception 'exceptionText' should be set", report.getException().get(0).isSetExceptionText());
        assertEquals("The exception 'exceptionText' should be set to 'The format unicorn/type is not supported'",
                "The format unicorn/type is not supported", report.getException().get(0).getExceptionText().get(0));

        //Test Execute with rawData output  without identifier
        execute = new Execute();
        responseFormType = new ResponseFormType();
        outputDefinitionsType = new DocumentOutputDefinitionType();
        responseFormType.setRawDataOutput(outputDefinitionsType);
        execute.setResponseForm(responseFormType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());

        //Test Execute with rawData output format
        execute = new Execute();
        responseFormType = new ResponseFormType();
        outputDefinitionsType = new DocumentOutputDefinitionType();
        outputDefinitionsType.setMimeType("unicorn/type");
        rawOutputCodeType = new CodeType();
        rawOutputCodeType.setValue("orbisgis:test:full:output:rawdata");
        outputDefinitionsType.setIdentifier(rawOutputCodeType);
        responseFormType.setRawDataOutput(outputDefinitionsType);
        execute.setResponseForm(responseFormType);
        execute.setIdentifier(codeType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'ResponseForm'", "ResponseForm",
                report.getException().get(0).getLocator());
        assertTrue("The exception 'exceptionText' should be set", report.getException().get(0).isSetExceptionText());
        assertEquals("The exception 'exceptionText' should be set to 'The format unicorn/type is not supported'",
                "The format unicorn/type is not supported", report.getException().get(0).getExceptionText().get(0));


        ////
        //Other code types
        ////

        //Test Execute with bad language
        execute = new Execute();
        execute.setIdentifier(codeType);
        execute.setLanguage("uni:co:rn");
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'InvalidParameterValue'", "InvalidParameterValue",
                report.getException().get(0).getExceptionCode());
        assertTrue("The exception 'locator' should be set", report.getException().get(0).isSetLocator());
        assertEquals("The exception 'locator' should be set to 'language'", "language",
                report.getException().get(0).getLocator());

        //Test Execute with serverBusy
        /*execute = new Execute();
        execute.setIdentifier(codeType);
        minWps100Operations.execute(execute);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'ServerBusy'", "ServerBusy",
                report.getException().get(0).getExceptionCode());*/


        //Test Execute with fileSizeExceeded
        /*execute = new Execute();
        execute.setIdentifier(codeType);
        dataInputsType = new DataInputsType();
        inputType = new InputType();
        rawInputCodeType = new CodeType();
        rawInputCodeType.setValue("orbisgis:test:full:input:rawdata");
        inputType.setIdentifier(rawInputCodeType);
        DataType dataType = new DataType();
        dataType.setComplexData(new ComplexDataType());
        inputType.setData(dataType);
        inputReferenceType = new InputReferenceType();
        try {
            inputReferenceType.setHref(TestWPS_1_0_0_Execute.class.getResource("tooBigFile.txt").toURI().toString());
        } catch (URISyntaxException ignored) {}
        inputType.setReference(inputReferenceType);
        dataInputsType.getInput().add(inputType);
        execute.setDataInputs(dataInputsType);
        object = minWps100Operations.execute(execute);
        assertTrue("The result of the Execute operation should be an ExceptionReport", object instanceof ExceptionReport);
        report = (ExceptionReport)object;
        assertTrue("The exception of Exception report should be set", report.isSetException());
        assertFalse("The exception list of ExceptionReport should not be empty", report.getException().isEmpty());
        assertTrue("The exception 'exceptionCode' should be set", report.getException().get(0).isSetExceptionCode());
        assertEquals("The exception 'exceptionCode' should be set to 'FileSizeExceeded'", "FileSizeExceeded",
                report.getException().get(0).getExceptionCode());*/
    }
}
