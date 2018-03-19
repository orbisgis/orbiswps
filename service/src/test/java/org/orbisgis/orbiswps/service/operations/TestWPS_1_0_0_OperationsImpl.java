package org.orbisgis.orbiswps.service.operations;

import junit.framework.Assert;
import net.opengis.ows._1.*;
import net.opengis.wps._1_0_0.*;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.service.operations.WPS_1_0_0_OperationsImpl;
import org.orbisgis.orbiswps.service.process.ProcessManager;
import org.orbisgis.orbiswps.service.operations.WpsServerProperties_1_0_0;
import org.orbisgis.orbiswps.serviceapi.operations.WPS_1_0_0_Operations;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * Test class for the WPS_2_0_OperationsImpl
 *
 * @author Sylvain PALOMINOS
 */
public class TestWPS_1_0_0_OperationsImpl {

    /** Wps 2.0 Operation object. */
    private WPS_1_0_0_Operations wps100Operations;

    /** Wps server object. */
    private WpsServerImpl wpsServer;

    /** Wps 1.0.0 properties. */
    private WpsServerProperties_1_0_0 wpsProps;

    /**
     * Initialize a wps server for processing all the tests.
     */
    @Before
    public void initialize() {
        wpsServer = new WpsServerImpl();
        ProcessManager processManager = new ProcessManager(null, wpsServer);
        try {
            URL url = this.getClass().getResource("../JDBCTable.groovy");
            Assert.assertNotNull("Unable to load the script 'JDBCTable.groovy'", url);
            File f = new File(url.toURI());
            wpsServer.addProcess(f);
            processManager.addScript(f.toURI());
        } catch (URISyntaxException e) {
            Assert.fail("Error on loading the scripts : "+e.getMessage());
        }
        wpsProps = new WpsServerProperties_1_0_0(null);
        wps100Operations =  new WPS_1_0_0_OperationsImpl(wpsServer, wpsProps, processManager);
    }


    /**
     * Check if once initialized, the wps server has loaded it basic capabilities.
     */
    @Test
    public void initialisationTest(){
        //Ask for the GetCapabilities
        GetCapabilities getCapabilities = new GetCapabilities();
        AcceptVersionsType acceptVersionsType = new AcceptVersionsType();
        acceptVersionsType.getVersion().add("1.0.0");
        getCapabilities.setAcceptVersions(acceptVersionsType);
        getCapabilities.setLanguage("en");
        Object object = wps100Operations.getCapabilities(getCapabilities);
        StringBuilder reason = new StringBuilder();
        if(object instanceof ExceptionReport){
            for(ExceptionType exception : ((ExceptionReport)object).getException()){
                reason.append(exception.getExceptionCode());
                if(exception.getLocator() != null && !exception.getLocator().isEmpty()){
                    reason.append(" : ").append(exception.getLocator());
                }
                reason.append("\n");
            }
        }
        if(reason.length() == 0){
            reason = new StringBuilder("Unknown reason");
        }
        Assert.assertTrue("The wps server capabilities is invalid : " + reason ,
                object instanceof WPSCapabilitiesType);
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType)object;

        //service identification tests
        Assert.assertNotNull("The wps server service identification service identification should not be null.",
                capabilities.getServiceIdentification());
        Assert.assertNotNull("The wps server service identification service type should not be null.",
                capabilities.getServiceIdentification().getServiceType());
        Assert.assertEquals("The wps server service identification service type should 'WPS'.",
                capabilities.getServiceIdentification().getServiceType().getValue(), "WPS");

        Assert.assertNotNull("The wps server service identification service type version should not be null",
                capabilities.getServiceIdentification().getServiceTypeVersion());
        Assert.assertFalse("The wps server service identification service type version should not be empty",
                capabilities.getServiceIdentification().getServiceTypeVersion().isEmpty());
        Assert.assertEquals("The wps server service identification service type version should be '1.0.0'",
                capabilities.getServiceIdentification().getServiceTypeVersion().get(0), "1.0.0");

        Assert.assertNotNull("The wps server service identification profile should not be null",
                capabilities.getServiceIdentification().getProfile());
        Assert.assertTrue("The wps server service identification profile should be empty",
                capabilities.getServiceIdentification().getProfile().isEmpty());

        Assert.assertNull("The wps server service identification fees should be null",
                capabilities.getServiceIdentification().getFees());

        Assert.assertNotNull("The wps server service identification access constraint should not be null",
                capabilities.getServiceIdentification().getAccessConstraints());
        Assert.assertTrue("The wps server service identification access constraint should be empty",
                capabilities.getServiceIdentification().getAccessConstraints().isEmpty());

        Assert.assertNotNull("The wps server service identification title should not be null",
                capabilities.getServiceIdentification().getTitle());
        Assert.assertFalse("The wps server service identification title should not be empty",
                capabilities.getServiceIdentification().getTitle().isEmpty());
        Assert.assertEquals("The wps server service identification title value should be 'Local WPS Service'",
                capabilities.getServiceIdentification().getTitle().get(0).getValue(), "Local WPS Service");
        Assert.assertEquals("The wps server service identification title language should be 'en'",
                capabilities.getServiceIdentification().getTitle().get(0).getLang(), "en");

        Assert.assertNotNull("The wps server service identification abstract should not be null",
                capabilities.getServiceIdentification().getAbstract());
        Assert.assertTrue("The wps server service identification abstract should be empty",
                capabilities.getServiceIdentification().getAbstract().isEmpty());

        //service provider tests
        Assert.assertNotNull("The wps server service provider should not be null",
                capabilities.getServiceProvider());
        Assert.assertEquals("The wps server service provider name should be 'OrbisGIS'",
                capabilities.getServiceProvider().getProviderName(), "OrbisGIS");

        Assert.assertNull("The wps server service provider site should be null",
                capabilities.getServiceProvider().getProviderSite());

        ResponsiblePartySubsetType serviceContact = capabilities.getServiceProvider().getServiceContact();
        Assert.assertNull("The wps server service contact should be null", serviceContact);

        //operation metadata tests
        Assert.assertNotNull("The wps server operation metadata should not be null",
                capabilities.getOperationsMetadata());
        Assert.assertNotNull("The wps server operation metadata operation should not be null",
                capabilities.getOperationsMetadata().getOperation());
        Assert.assertEquals("The wps server operation metadata operation should contains six elements",
                capabilities.getOperationsMetadata().getOperation().size(), 3);
        String[] operations={"GetCapabilities", "DescribeProcess", "Execute"};
        List<Operation> serverOperations = capabilities.getOperationsMetadata().getOperation();
        for(String operation : operations){
            boolean isOperationFound = false;
            for(Operation serverOperation : serverOperations){
                if(serverOperation.getName().equals(operation)){
                    isOperationFound = true;
                    String error = null;
                    if(serverOperation.getDCP() == null){
                        error = "The wps operation metadata operation '"+operation+"' dcp should not be null";
                    }
                    else if(serverOperation.getDCP().isEmpty()) {
                        error = "The wps operation metadata operation '"+operation+"' dcp should not be empty";
                    }
                    else if(serverOperation.getDCP().get(0) == null) {
                        error = "The wps operation metadata operation '"+operation+"' dcp 0 should not be null";
                    }
                    else if(serverOperation.getDCP().get(0).getHTTP() == null) {
                        error = "The wps operation metadata operation '"+operation+"' dcp 0 HTTP should not be null";
                    }
                    else if(serverOperation.getDCP().get(0).getHTTP().getGetOrPost() == null) {
                        error = "The wps operation metadata operation '"+operation+"' dcp 0 HTTP Get or Post should not be null";
                    }
                    else if(serverOperation.getDCP().get(0).getHTTP().getGetOrPost().isEmpty()) {
                        error = "The wps operation metadata operation '"+operation+"' dcp 0 HTTP Get or Post should not be empty";
                    }
                    else if(serverOperation.getDCP().get(0).getHTTP().getGetOrPost().get(0) == null) {
                        error = "The wps operation metadata operation '"+operation+"' dcp 0 HTTP Get or Post 0 should not be null";
                    }
                    else if(serverOperation.getParameter() == null) {
                        error = "The wps operation metadata operation '"+operation+"' parameter should not be null";
                    }
                    else if(!serverOperation.getParameter().isEmpty()) {
                        error = "The wps operation metadata operation '"+operation+"' parameter should be empty";
                    }
                    else if(serverOperation.getConstraint() == null) {
                        error = "The wps operation metadata operation '"+operation+"' constraint should not be null";
                    }
                    else if(!serverOperation.getConstraint().isEmpty()) {
                        error = "The wps operation metadata operation '"+operation+"' constraint should be empty";
                    }
                    else if(serverOperation.getMetadata() == null) {
                        error = "The wps operation metadata operation '" + operation + "' metadata should not be null";
                    }
                    else if(!serverOperation.getMetadata().isEmpty()) {
                        error = "The wps operation metadata operation '" + operation + "' metadata should be empty";
                    }
                    else if(!serverOperation.getName().equals(operation)) {
                        error = "The wps operation metadata operation '"+operation+"' name should be "+operation;
                    }
                    Assert.assertNull(error, error);
                }
            }
            Assert.assertTrue("The operation "+operation+" is not found.", isOperationFound);
        }
        Assert.assertNotNull("The wps server operation metadata parameter should not be null",
                capabilities.getOperationsMetadata().getParameter());
        Assert.assertTrue("The wps server operation metadata parameter should be empty",
                capabilities.getOperationsMetadata().getParameter().isEmpty());
        Assert.assertNotNull("The wps server operation metadata constraint should not be null",
                capabilities.getOperationsMetadata().getConstraint());
        Assert.assertTrue("The wps server operation metadata constraint should be empty",
                capabilities.getOperationsMetadata().getConstraint().isEmpty());
        Assert.assertNull("The wps server operation metadata extended capabilities should be null",
                capabilities.getOperationsMetadata().getExtendedCapabilities());

        //language tests
        Assert.assertNotNull("The wps server languages should not be null",
                capabilities.getLanguages());
        Assert.assertNotNull("The wps server languages default should not be null",
                capabilities.getLanguages().getDefault());
        Assert.assertEquals("The wps server languages default language should be 'en'",
                "en", capabilities.getLanguages().getDefault().getLanguage());
        Assert.assertEquals("The wps server lang should be 'en'",
                "en",capabilities.getLang());

        //version tests
        Assert.assertEquals("The wps server version should be '1.0.0'",
                "1.0.0", capabilities.getVersion());

        //update sequence tests
        Assert.assertNotNull("The wps server update sequence should not be null",
                capabilities.getUpdateSequence());
    }



    /**
     * Tests the GetCapabilities operation with a bad formed GetCapabilities.
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
        Object resultObject = wps100Operations.getCapabilities(null);

        Assert.assertNotNull("Error on unmarshalling the WpsService answer, the object should not be null",
                resultObject);
        Assert.assertTrue("Error on unmarshalling the WpsService answer, the object should be a ExceptionReport",
                resultObject instanceof ExceptionReport);
        Assert.assertEquals("Error on unmarshalling the WpsService answer, the exception should be 'NoApplicableCode'",
                ((ExceptionReport) resultObject).getException().get(0).getExceptionCode(), "NoApplicableCode");

        //Bad version
        AcceptVersionsType badAcceptVersionsType = new AcceptVersionsType();
        badAcceptVersionsType.getVersion().add("0.0.0.badVersion");
        getCapabilities.setAcceptVersions(badAcceptVersionsType);
        resultObject = wps100Operations.getCapabilities(getCapabilities);
        getCapabilities.setAcceptVersions(acceptVersionsType);

        Assert.assertNotNull("Error on unmarshalling the WpsService answer, the object should not be null",
                resultObject);
        Assert.assertTrue("Error on unmarshalling the WpsService answer, the object should be a ExceptionReport",
                resultObject instanceof ExceptionReport);
        Assert.assertEquals("Error on unmarshalling the WpsService answer, the exception should be 'VersionNegotiationFailed'",
                ((ExceptionReport) resultObject).getException().get(0).getExceptionCode(), "VersionNegotiationFailed");

        //Bad language
        getCapabilities.setLanguage("NotALanguage");
        resultObject = wps100Operations.getCapabilities(getCapabilities);

        Assert.assertNotNull("Error on unmarshalling the WpsService answer, the object should not be null",
                resultObject);
        Assert.assertTrue("Error on unmarshalling the WpsService answer, the object should be a ExceptionReport",
                resultObject instanceof ExceptionReport);
        Assert.assertEquals("Error on unmarshalling the WpsService answer, the exception should be 'InvalidParameterValue'",
                ((ExceptionReport) resultObject).getException().get(0).getExceptionCode(), "InvalidParameterValue");
        Assert.assertEquals("Error on unmarshalling the WpsService answer, the exception locator should be 'AcceptLanguages'",
                ((ExceptionReport) resultObject).getException().get(0).getLocator(), "AcceptLanguages");
    }

    /**
     * Tests the DescribeProcessOperation
     */
    @Test
    public void testDescribeProcess(){
        //Ask for the DescribeProcess
        DescribeProcess describeProcess = new DescribeProcess();
        describeProcess.setLanguage("en");
        CodeType codeType = new CodeType();
        codeType.setValue("orbisgis:test:jdbctable");
        describeProcess.getIdentifier().add(codeType);
        ProcessDescriptions processDescriptions = wps100Operations.describeProcess(describeProcess);

        Assert.assertNotNull("The wps ProcessDescriptions should not be null" ,
                processDescriptions);

        Assert.assertEquals("The ProcessDescriptions language should be 'en'", "en",
                processDescriptions.getLang());

        Assert.assertNotNull("The ProcessDescriptions process description should be not be null",
                processDescriptions.getProcessDescription());
        Assert.assertEquals("The ProcessDescriptions process description size should be 1",
                processDescriptions.getProcessDescription().size(), 1);
        Assert.assertNotNull("The ProcessDescriptions process description 0 should not be null",
                processDescriptions.getProcessDescription().get(0));

        //DataInputs
        ProcessDescriptionType.DataInputs dataInputs = processDescriptions.getProcessDescription().get(0).getDataInputs();
        Assert.assertNotNull("The ProcessDescriptions DataInputs should not be null",
                dataInputs);
        Assert.assertNotNull("The ProcessDescriptions Input should not be null",
                dataInputs.getInput());
        Assert.assertEquals("The ProcessDescriptions Input size should be 1", 1,
                dataInputs.getInput().size());
        Assert.assertNotNull("The ProcessDescriptions Input 0 should not be null",
                dataInputs.getInput().get(0));
        Assert.assertFalse("The ProcessDescriptions Input 0 should not be a BoundingBox",
                dataInputs.getInput().get(0).isSetBoundingBoxData());
        Assert.assertTrue("The ProcessDescriptions Input 0 should be a ComplexData",
                dataInputs.getInput().get(0).isSetComplexData());
        Assert.assertEquals("The ProcessDescriptions Input 0 MaximumMegabytes should be '2000'",
                new BigInteger("2000"), dataInputs.getInput().get(0).getComplexData().getMaximumMegabytes());
        Assert.assertTrue("The ProcessDescriptions Input 0 Defautl should be set",
                dataInputs.getInput().get(0).getComplexData().isSetDefault());
        Assert.assertTrue("The ProcessDescriptions Input 0 Default Format should be set",
                dataInputs.getInput().get(0).getComplexData().getDefault().isSetFormat());
        Assert.assertEquals("The ProcessDescriptions Input 0 Default Format should be 'application/geojson'",
                "application/geojson", dataInputs.getInput().get(0).getComplexData().getDefault().getFormat().getMimeType());
        Assert.assertTrue("The ProcessDescriptions Input 0 Supported Format should be set",
                dataInputs.getInput().get(0).getComplexData().getSupported().isSetFormat());
        Assert.assertEquals("The ProcessDescriptions Input 0 Supported Format 1 should be 'application/geojson'",
                "application/geojson", dataInputs.getInput().get(0).getComplexData().getSupported().getFormat().get(0).getMimeType());
        Assert.assertEquals("The ProcessDescriptions Input 0 Supported Format 0 should be 'text/xml'",
                "text/xml", dataInputs.getInput().get(0).getComplexData().getSupported().getFormat().get(1).getMimeType());
        Assert.assertEquals("The ProcessDescriptions Input 0 Supported Format 1 should be 'text/plain'",
                "text/plain", dataInputs.getInput().get(0).getComplexData().getSupported().getFormat().get(2).getMimeType());
        Assert.assertFalse("The ProcessDescriptions Input 0 should not be a LiteralData",
                dataInputs.getInput().get(0).isSetLiteralData());
        Assert.assertTrue("The ProcessDescriptions Input 0 maxOccurs should be set",
                dataInputs.getInput().get(0).isSetMaxOccurs());
        Assert.assertEquals("The ProcessDescriptions Input 0 maxOccurs should be '2'",
                new BigInteger("2"), dataInputs.getInput().get(0).getMaxOccurs());
        Assert.assertEquals("The ProcessDescriptions Input 0 minOccurs should be '0'",
                new BigInteger("0"), dataInputs.getInput().get(0).getMinOccurs());
        Assert.assertTrue("The ProcessDescriptions Input 0 abstract should be set",
                dataInputs.getInput().get(0).isSetAbstract());
        Assert.assertEquals("The ProcessDescriptions Input 0 abstract lang should be set",
                "en", dataInputs.getInput().get(0).getAbstract().getLang());
        Assert.assertEquals("The ProcessDescriptions Input 0 abstract value should be set",
                "A JDBCTable input.", dataInputs.getInput().get(0).getAbstract().getValue());
        Assert.assertTrue("The ProcessDescriptions Input 0 identifier should be set",
                dataInputs.getInput().get(0).isSetIdentifier());
        Assert.assertEquals("The ProcessDescriptions Input 0 identifier should be set",
                "orbisgis:test:jdbctable:orbisgis:test:jdbctable:input",
                dataInputs.getInput().get(0).getIdentifier().getValue());
        Assert.assertTrue("The ProcessDescriptions Input 0 metadata should be set",
                dataInputs.getInput().get(0).isSetMetadata());
        Assert.assertTrue("The ProcessDescriptions Input 0 title should be set",
                dataInputs.getInput().get(0).isSetTitle());
        Assert.assertEquals("The ProcessDescriptions Input 0 metadata size should be 2",
                2, dataInputs.getInput().get(0).getMetadata().size());

        //ProcessOutputs
        ProcessDescriptionType.ProcessOutputs processOutputs =
                processDescriptions.getProcessDescription().get(0).getProcessOutputs();
        Assert.assertNotNull("The ProcessDescriptions ProcessOutputs should not be null",
                processOutputs);
        Assert.assertTrue("The ProcessDescription ProcessOutputs should be set",
                processOutputs.isSetOutput());
        Assert.assertFalse("The ProcessDescription ProcessOutputs should not be empty",
                processOutputs.getOutput().isEmpty());
        Assert.assertFalse("The ProcessDescriptions ProcessOutputs 0 should not be a BoundingBox",
                processOutputs.getOutput().get(0).isSetBoundingBoxOutput());
        Assert.assertTrue("The ProcessDescriptions ProcessOutputs 0 should be a ComplexData",
                processOutputs.getOutput().get(0).isSetComplexOutput());
        Assert.assertTrue("The ProcessDescriptions ProcessOutputs 0 Default should be set",
                processOutputs.getOutput().get(0).getComplexOutput().isSetDefault());
        Assert.assertTrue("The ProcessDescriptions ProcessOutputs 0 Default Format should be set",
                processOutputs.getOutput().get(0).getComplexOutput().getDefault().isSetFormat());
        Assert.assertEquals("The ProcessDescriptions ProcessOutputs 0 Default Format should be 'text/plain'",
                "text/plain", processOutputs.getOutput().get(0).getComplexOutput().getDefault().getFormat().getMimeType());
        Assert.assertTrue("The ProcessDescriptions ProcessOutputs 0 Supported Format should be set",
                processOutputs.getOutput().get(0).getComplexOutput().getSupported().isSetFormat());
        Assert.assertFalse("The ProcessDescriptions ProcessOutputs 0 should not be a LiteralData",
                processOutputs.getOutput().get(0).isSetLiteralOutput());
        Assert.assertTrue("The ProcessDescriptions ProcessOutputs 0 abstract should be set",
                processOutputs.getOutput().get(0).isSetAbstract());
        Assert.assertEquals("The ProcessDescriptions ProcessOutputs 0 abstract lang should be set",
                "en", processOutputs.getOutput().get(0).getAbstract().getLang());
        Assert.assertEquals("The ProcessDescriptions ProcessOutputs 0 abstract value should be set",
                "A JDBCTable output.", processOutputs.getOutput().get(0).getAbstract().getValue());
        Assert.assertTrue("The ProcessDescriptions ProcessOutputs 0 identifier should be set",
                processOutputs.getOutput().get(0).isSetIdentifier());
        Assert.assertEquals("The ProcessDescriptions ProcessOutputs 0 identifier should be set",
                "orbisgis:test:jdbctable:orbisgis:test:jdbctable:output",
                processOutputs.getOutput().get(0).getIdentifier().getValue());
        Assert.assertFalse("The ProcessDescriptions ProcessOutputs 0 metadata should not be set",
                processOutputs.getOutput().get(0).isSetMetadata());
        Assert.assertTrue("The ProcessDescriptions ProcessOutputs 0 title should be set",
                processOutputs.getOutput().get(0).isSetTitle());

        //Abstract
        Assert.assertNotNull("The ProcessDescriptions ProcessOutputs abstract should not be null",
                processDescriptions.getProcessDescription().get(0).getAbstract());
        Assert.assertEquals("The ProcessDescriptions ProcessOutputs abstract lang should not be 'en'",
                "en", processDescriptions.getProcessDescription().get(0).getAbstract().getLang());
        Assert.assertEquals("The ProcessDescriptions ProcessOutputs abstract value should not be 'en'",
                "Test script using the JDBCTable ComplexData.",
                processDescriptions.getProcessDescription().get(0).getAbstract().getValue());

        //Title
        Assert.assertNotNull("The ProcessDescriptions ProcessOutputs title should not be null",
                processDescriptions.getProcessDescription().get(0).getTitle());
        Assert.assertEquals("The ProcessDescriptions ProcessOutputs title lang should not be 'en'",
                "en", processDescriptions.getProcessDescription().get(0).getTitle().getLang());
        Assert.assertEquals("The ProcessDescriptions ProcessOutputs title value should not be 'JDBCTable test'",
                "JDBCTable test", processDescriptions.getProcessDescription().get(0).getTitle().getValue());

        //Identifier
        Assert.assertNotNull("The ProcessDescriptions ProcessOutputs identifier should not be null",
                processDescriptions.getProcessDescription().get(0).getIdentifier());
        Assert.assertEquals("The ProcessDescriptions ProcessOutputs identifier value should not be 'orbisgis:test:jdbctable'",
                "orbisgis:test:jdbctable", processDescriptions.getProcessDescription().get(0).getIdentifier().getValue());

        //Other
        Assert.assertFalse("The ProcessDescriptions ProcessOutputs version should be not be set",
                processDescriptions.getProcessDescription().get(0).isSetProcessVersion());
        Assert.assertEquals("The ProcessDescriptions ProcessOutputs metadata size should be '2'",
                2, processDescriptions.getProcessDescription().get(0).getMetadata().size());
    }
}
