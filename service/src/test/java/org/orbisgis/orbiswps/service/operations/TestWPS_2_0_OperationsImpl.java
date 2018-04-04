package org.orbisgis.orbiswps.service.operations;

import junit.framework.Assert;
import net.opengis.ows._2.*;
import net.opengis.wps._2_0.*;
import net.opengis.wps._2_0.GetCapabilitiesType;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.service.process.ProcessManager;
import org.orbisgis.orbiswps.service.model.JaxbContainer;
import org.orbisgis.orbiswps.serviceapi.operations.WPS_2_0_Operations;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static java.lang.Thread.sleep;

/**
 * Test class for the WPS_2_0_OperationsImpl
 *
 * @author Sylvain PALOMINOS
 */
public class TestWPS_2_0_OperationsImpl {

    /** Wps 2.0 Operation object. */
    private WPS_2_0_Operations wps20Operations;

    /** Wps server object. */
    private WpsServerImpl wpsServer;

    private ProcessManager processManager;

    /**
     * Initialize a wps server for processing all the tests.
     */
    @Before
    public void initialize() {
        wpsServer = new WpsServerImpl();
        processManager = new ProcessManager(null, wpsServer);
        wps20Operations = new WPS_2_0_OperationsImpl(wpsServer, new WpsServerProperties_2_0(null), processManager);
    }


    /**
     * Check if once initialized, the wps server has loaded it basic capabilities.
     */
    @Test
    public void initialisationTest(){
        //Ask for the GetCapabilities
        GetCapabilitiesType getCapabilitiesType = new GetCapabilitiesType();
        GetCapabilitiesType.AcceptLanguages acceptLanguages = new GetCapabilitiesType.AcceptLanguages();
        acceptLanguages.getLanguage().add("*");
        getCapabilitiesType.setAcceptLanguages(acceptLanguages);
        AcceptVersionsType acceptVersionsType = new AcceptVersionsType();
        acceptVersionsType.getVersion().add("2.0");
        getCapabilitiesType.setAcceptVersions(acceptVersionsType);
        SectionsType sectionsType = new SectionsType();
        sectionsType.getSection().add("All");
        getCapabilitiesType.setSections(sectionsType);
        Object object = wps20Operations.getCapabilities(getCapabilitiesType);
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

        //Contents tests
        Assert.assertNotNull("The wps server contents should not be null.",
                capabilities.getContents());
        Assert.assertNotNull("The wps server process summary should not be null.",
                capabilities.getContents().getProcessSummary());
        Assert.assertTrue("The wps server process summary should be empty.",
                capabilities.getContents().getProcessSummary().isEmpty());

        //extensions tests
        Assert.assertNotNull("The wps server service identification extension should not be null.",
                capabilities.getExtension());

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
        Assert.assertEquals("The wps server service identification service type version should be '2.0.0'",
                capabilities.getServiceIdentification().getServiceTypeVersion().get(0), "2.0.0");

        Assert.assertNotNull("The wps server service identification profile should not be null",
                capabilities.getServiceIdentification().getProfile());
        Assert.assertTrue("The wps server service identification profile should be empty",
                capabilities.getServiceIdentification().getProfile().isEmpty());

        Assert.assertNotNull("The wps server service identification fees should not be null",
                capabilities.getServiceIdentification().getFees());
        Assert.assertEquals("The wps server service identification fees should be 'NONE'",
                capabilities.getServiceIdentification().getFees(), "NONE");

        Assert.assertNotNull("The wps server service identification access constraint should not be null",
                capabilities.getServiceIdentification().getAccessConstraints());
        Assert.assertTrue("The wps server service identification access constraint should contain 'NONE'",
                capabilities.getServiceIdentification().getAccessConstraints().contains("NONE"));

        Assert.assertNotNull("The wps server service identification title should not be null",
                capabilities.getServiceIdentification().getTitle());
        Assert.assertFalse("The wps server service identification title should not be empty",
                capabilities.getServiceIdentification().getTitle().isEmpty());
        Assert.assertEquals("The wps server service identification title value should be 'OrbisGIS Local WPS Service'",
                capabilities.getServiceIdentification().getTitle().get(0).getValue(), "OrbisGIS Local WPS Service");
        Assert.assertEquals("The wps server service identification title language should be 'en'",
                capabilities.getServiceIdentification().getTitle().get(0).getLang(), "en");

        Assert.assertNotNull("The wps server service identification abstract should not be null",
                capabilities.getServiceIdentification().getAbstract());
        Assert.assertFalse("The wps server service identification abstract should not be empty",
                capabilities.getServiceIdentification().getAbstract().isEmpty());
        Assert.assertEquals("The wps server service identification abstract value should be " +
                        "'OrbisGIS local instance of the WPS Service'",
                capabilities.getServiceIdentification().getAbstract().get(0).getValue(),
                "OrbisGIS local instance of the WPS Service");
        Assert.assertEquals("The wps server service identification abstract language should be 'en'",
                capabilities.getServiceIdentification().getAbstract().get(0).getLang(), "en");

        Assert.assertNotNull("The wps server service identification keywords should not be null",
                capabilities.getServiceIdentification().getKeywords());
        Assert.assertFalse("The wps server service identification keywords should not be empty",
                capabilities.getServiceIdentification().getKeywords().isEmpty());
        Assert.assertNotNull("The wps server service identification keywords 0 should not be null",
                capabilities.getServiceIdentification().getKeywords().get(0));
        Assert.assertNotNull("The wps server service identification keywords 0 keyword should not be null",
                capabilities.getServiceIdentification().getKeywords().get(0).getKeyword());
        Assert.assertFalse("The wps server service identification keywords 0 keyword should not be empty",
                capabilities.getServiceIdentification().getKeywords().get(0).getKeyword().isEmpty());
        Assert.assertEquals("The wps server service identification keywords 0 keyword value should be 'Toolbox'",
                capabilities.getServiceIdentification().getKeywords().get(0).getKeyword().get(0).getValue(), "Toolbox");
        Assert.assertEquals("The wps server service identification keywords 0 keyword language should be 'en'",
                capabilities.getServiceIdentification().getKeywords().get(0).getKeyword().get(0).getLang(), "en");
        Assert.assertNotNull("The wps server service identification keywords 1 should not be null",
                capabilities.getServiceIdentification().getKeywords().get(1));
        Assert.assertNotNull("The wps server service identification keywords 1 keyword should not be null",
                capabilities.getServiceIdentification().getKeywords().get(1).getKeyword());
        Assert.assertFalse("The wps server service identification keywords 1 keyword should not be empty",
                capabilities.getServiceIdentification().getKeywords().get(1).getKeyword().isEmpty());
        Assert.assertEquals("The wps server service identification keywords 1 keyword value should be 'WPS'",
                "WPS service", capabilities.getServiceIdentification().getKeywords().get(1).getKeyword().get(0).getValue());
        Assert.assertEquals("The wps server service identification keywords 1 keyword language should be 'en'",
                capabilities.getServiceIdentification().getKeywords().get(1).getKeyword().get(0).getLang(), "en");
        Assert.assertNotNull("The wps server service identification keywords 2 should not be null",
                capabilities.getServiceIdentification().getKeywords().get(2));
        Assert.assertNotNull("The wps server service identification keywords 2 keyword should not be null",
                capabilities.getServiceIdentification().getKeywords().get(2).getKeyword());
        Assert.assertFalse("The wps server service identification keywords 2 keyword should not be empty",
                capabilities.getServiceIdentification().getKeywords().get(2).getKeyword().isEmpty());
        Assert.assertEquals("The wps server service identification keywords 2 keyword value should be 'OrbisGIS'",
                capabilities.getServiceIdentification().getKeywords().get(2).getKeyword().get(0).getValue(),
                "OrbisGIS");
        Assert.assertEquals("The wps server service identification keywords 2 keyword language should be 'en'",
                capabilities.getServiceIdentification().getKeywords().get(2).getKeyword().get(0).getLang(), "en");

        //service provider tests
        Assert.assertNotNull("The wps server service provider should not be null",
                capabilities.getServiceProvider());
        Assert.assertEquals("The wps server service provider name should be 'OrbisGIS'",
                capabilities.getServiceProvider().getProviderName(), "OrbisGIS");

        Assert.assertNotNull("The wps server service provider site should not be null",
                capabilities.getServiceProvider().getProviderSite());
        Assert.assertEquals("The wps server service provider site href should be 'http://orbisgis.org/'",
                capabilities.getServiceProvider().getProviderSite().getHref(), "http://orbisgis.org/");
        Assert.assertNull("The wps server service provider site role should be null",
                capabilities.getServiceProvider().getProviderSite().getRole());
        Assert.assertNull("The wps server service provider site arcrole should be null",
                capabilities.getServiceProvider().getProviderSite().getArcrole());
        Assert.assertNull("The wps server service provider site title should be null",
                capabilities.getServiceProvider().getProviderSite().getTitle());
        Assert.assertNull("The wps server service provider site show should be null",
                capabilities.getServiceProvider().getProviderSite().getShow());
        Assert.assertNull("The wps server service provider site actuate should be null",
                capabilities.getServiceProvider().getProviderSite().getActuate());

        ResponsiblePartySubsetType serviceContact = capabilities.getServiceProvider().getServiceContact();
        Assert.assertNull("The wps server service contact should be null", serviceContact);

        //operation metadata tests
        Assert.assertNotNull("The wps server operation metadata should not be null",
                capabilities.getOperationsMetadata());
        Assert.assertNotNull("The wps server operation metadata operation should not be null",
                capabilities.getOperationsMetadata().getOperation());
        Assert.assertEquals("The wps server operation metadata operation should contains six elements",
                capabilities.getOperationsMetadata().getOperation().size(), 6);
        String[] operations={"GetCapabilities", "DescribeProcess", "Execute", "GetStatus", "GetResult", "Dismiss"};
        List<Operation> serverOperations = capabilities.getOperationsMetadata().getOperation();
        for(String operation : operations){
            boolean isOperationFound = false;
            for(Operation serverOperation : serverOperations){
                if(serverOperation.getName().equals(operation)){
                    isOperationFound = true;
                    String errorMessage = testOperation(serverOperation, operation);
                    Assert.assertNull(errorMessage, errorMessage);
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
        Assert.assertNotNull("The wps server languages language should not be null",
                capabilities.getLanguages().getLanguage());
        Assert.assertFalse("The wps server languages language should not be empty",
                capabilities.getLanguages().getLanguage().isEmpty());
        Assert.assertEquals("The wps server languages language 0 should be 'en'",
                capabilities.getLanguages().getLanguage().get(0), "en");

        //version tests
        Assert.assertEquals("The wps server version should be '2.0'",
                capabilities.getVersion(), "2.0");

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
        GetCapabilitiesType getCapabilitiesType = new GetCapabilitiesType();
        GetCapabilitiesType.AcceptLanguages acceptLanguages = new GetCapabilitiesType.AcceptLanguages();
        acceptLanguages.getLanguage().add("*");
        getCapabilitiesType.setAcceptLanguages(acceptLanguages);
        AcceptVersionsType acceptVersionsType = new AcceptVersionsType();
        acceptVersionsType.getVersion().add("2.0.0");
        getCapabilitiesType.setAcceptVersions(acceptVersionsType);
        SectionsType sectionsType = new SectionsType();
        sectionsType.getSection().add("All");
        getCapabilitiesType.setSections(sectionsType);

        //Null Capabilities test
        Object resultObject = wps20Operations.getCapabilities(null);

        Assert.assertNotNull("Error on unmarshalling the WpsService answer, the object should not be null",
                resultObject);
        Assert.assertTrue("Error on unmarshalling the WpsService answer, the object should be a ExceptionReport",
                resultObject instanceof ExceptionReport);
        Assert.assertEquals("Error on unmarshalling the WpsService answer, the exception should be 'NoApplicableCode'",
                ((ExceptionReport) resultObject).getException().get(0).getExceptionCode(), "NoApplicableCode");

        //Bad version
        AcceptVersionsType badAcceptVersionsType = new AcceptVersionsType();
        badAcceptVersionsType.getVersion().add("0.0.0.badVersion");
        getCapabilitiesType.setAcceptVersions(badAcceptVersionsType);
        resultObject = wps20Operations.getCapabilities(getCapabilitiesType);
        getCapabilitiesType.setAcceptVersions(acceptVersionsType);

        Assert.assertNotNull("Error on unmarshalling the WpsService answer, the object should not be null",
                resultObject);
        Assert.assertTrue("Error on unmarshalling the WpsService answer, the object should be a ExceptionReport",
                resultObject instanceof ExceptionReport);
        Assert.assertEquals("Error on unmarshalling the WpsService answer, the exception should be 'VersionNegotiationFailed'",
                ((ExceptionReport) resultObject).getException().get(0).getExceptionCode(), "VersionNegotiationFailed");

        //Bad section
        AcceptVersionsType goodAcceptVersionsType = new AcceptVersionsType();
        goodAcceptVersionsType.getVersion().add("2.0");
        getCapabilitiesType.setAcceptVersions(goodAcceptVersionsType);
        SectionsType badSectionsType = new SectionsType();
        badSectionsType.getSection().add("NotASection");
        getCapabilitiesType.setSections(badSectionsType);
        resultObject = wps20Operations.getCapabilities(getCapabilitiesType);
        getCapabilitiesType.setSections(sectionsType);

        Assert.assertNotNull("Error on unmarshalling the WpsService answer, the object should not be null",
                resultObject);
        Assert.assertTrue("Error on unmarshalling the WpsService answer, the object should be a ExceptionReport",
                resultObject instanceof ExceptionReport);
        Assert.assertEquals("Error on unmarshalling the WpsService answer, the exception should be 'InvalidParameterValue'",
                "InvalidParameterValue", ((ExceptionReport) resultObject).getException().get(0).getExceptionCode());
        Assert.assertEquals("Error on unmarshalling the WpsService answer, the exception locator should be 'Sections:NotASection'",
                "Sections:NotASection", ((ExceptionReport) resultObject).getException().get(0).getLocator());

        //Bad language
        GetCapabilitiesType.AcceptLanguages badAcceptLanguages = new GetCapabilitiesType.AcceptLanguages();
        badAcceptLanguages.getLanguage().add("NotALanguage");
        getCapabilitiesType.setAcceptLanguages(badAcceptLanguages);
        resultObject = wps20Operations.getCapabilities(getCapabilitiesType);
        getCapabilitiesType.setSections(sectionsType);

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
         * Test process execution with bad ExecuteRequestType object.
         *
         * @throws InterruptedException Exception get if the sleep method fails.
         */
    @Test
    public void testBadExecution() throws InterruptedException {
        File file = new File(TestWPS_2_0_OperationsImpl.class.getResource("simpleScript.groovy").getFile());
        wpsServer.addProcess(file);
        processManager.addScript(file.toURI());
        //Test process execution with an input model without any content
        ExecuteRequestType executeRequestType = new ExecuteRequestType();
        CodeType id = new CodeType();
        id.setValue("orbisgis:test:simple");
        executeRequestType.setIdentifier(id);
        DataInputType dataInputType = new DataInputType();
        dataInputType.setId("orbisgis:test:simple:input:enumeration");
        Data data = new Data();
        dataInputType.setData(data);
        executeRequestType.getInput().add(dataInputType);
        StatusInfo statusInfo = (StatusInfo)wps20Operations.execute(executeRequestType);
        sleep(200);
        GetResult getResult = new GetResult();
        getResult.setJobID(statusInfo.getJobID());
        Assert.assertNotNull("The Wps result should not be null.", wps20Operations.getResult(getResult));

        //Test process execution with an input model with more than one value as content
        data.getContent().add("Value1");
        data.getContent().add("Value2");
        statusInfo = (StatusInfo)wps20Operations.execute(executeRequestType);
        sleep(200);
        getResult = new GetResult();
        getResult.setJobID(statusInfo.getJobID());
        Assert.assertNotNull("The Wps result should not be null.", wps20Operations.getResult(getResult));
    }

    /**
     * Test the execution on a WpsServerImpl without executionService.
     *
     * @throws JAXBException Exception get if the marshaller fails.
     * @throws IOException Exception get if the resource getting fails.
     * @throws InterruptedException Exception get if the sleep method fails.
     */
    @Test
    public void testExecuteWithoutExecutionService() throws JAXBException, IOException, InterruptedException {
        File file = new File(TestWPS_2_0_OperationsImpl.class.getResource("simpleScript.groovy").getFile());
        wpsServer.addProcess(file);
        processManager.addScript(file.toURI());

        Unmarshaller unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
        //Build the Execute object
        File executeFile = new File(this.getClass().getResource("ExecuteRequest.xml").getFile());
        ExecuteRequestType executeRequestType = (ExecuteRequestType)((JAXBElement)unmarshaller.unmarshal(executeFile)).getValue();
        StatusInfo statusInfo = (StatusInfo)wps20Operations.execute(executeRequestType);
        sleep(200);

        GetResult getResult = new GetResult();
        getResult.setJobID(statusInfo.getJobID());
        Result result = (Result)wps20Operations.getResult(getResult);
        Assert.assertTrue("The process result should not contain outputs.", result.getOutput().isEmpty());
    }



    /**
     * Test the operation of the wps server operation metadata.
     * @param operation Operation to test.
     * @param name Name of the operation.
     * @return Null if there is no error, the error message otherwise.
     */
    private String testOperation(Operation operation, String name){
        String error = null;
        if(operation.getDCP() == null){
            error = "The wps operation metadata operation '"+name+"' dcp should not be null";
        }
        else if(operation.getDCP().isEmpty()) {
            error = "The wps operation metadata operation '"+name+"' dcp should not be empty";
        }
        else if(operation.getDCP().get(0) == null) {
            error = "The wps operation metadata operation '"+name+"' dcp 0 should not be null";
        }
        else if(operation.getDCP().get(0).getHTTP() == null) {
            error = "The wps operation metadata operation '"+name+"' dcp 0 HTTP should not be null";
        }
        else if(operation.getDCP().get(0).getHTTP().getGetOrPost() == null) {
            error = "The wps operation metadata operation '"+name+"' dcp 0 HTTP Get or Post should not be null";
        }
        else if(operation.getDCP().get(0).getHTTP().getGetOrPost().isEmpty()) {
            error = "The wps operation metadata operation '"+name+"' dcp 0 HTTP Get or Post should not be empty";
        }
        else if(operation.getDCP().get(0).getHTTP().getGetOrPost().get(0) == null) {
            error = "The wps operation metadata operation '"+name+"' dcp 0 HTTP Get or Post 0 should not be null";
        }
        else if(operation.getDCP().get(0).getHTTP().getGetOrPost().get(1) == null) {
            error = "The wps operation metadata operation '"+name+"' dcp 0 HTTP Get or Post 1 should not be null";
        }
        else if(operation.getParameter() == null) {
            error = "The wps operation metadata operation '"+name+"' parameter should not be null";
        }
        else if(!operation.getParameter().isEmpty()) {
            error = "The wps operation metadata operation '"+name+"' parameter should be empty";
        }
        else if(operation.getConstraint() == null) {
            error = "The wps operation metadata operation '"+name+"' constraint should not be null";
        }
        else if(!operation.getConstraint().isEmpty()) {
            error = "The wps operation metadata operation '"+name+"' constraint should be empty";
        }
        else if(operation.getMetadata() == null) {
            error = "The wps operation metadata operation '" + name + "' metadata should not be null";
        }
        else if(!operation.getMetadata().isEmpty()) {
            error = "The wps operation metadata operation '" + name + "' metadata should be empty";
        }
        else if(!operation.getName().equals(name)) {
            error = "The wps operation metadata operation '"+name+"' name should be "+name;
        }
        return error;
    }

    /**
     * Test that GetStatus will return an error (according to the WPS standard) when queried by an unknown UUID
     */
    @Test
    public void testGetStatusWithUnknownUuid(){
        GetStatus getStatus = new GetStatus();
        String value = UUID.randomUUID().toString();
        getStatus.setJobID(value);

        Object status = wps20Operations.getStatus(getStatus);

        assert (status instanceof ExceptionReport);

        ExceptionReport report = (ExceptionReport) status;
        ExceptionType exceptionType = report.getException().get(0);
        assert (exceptionType.getExceptionCode().equals("NoSuchJob"));

        assert (exceptionType.getLocator().equals(value));

    }

    /**
     * Test that GetResult will return an error (according to the WPS standard) when queried by an unknown UUID
     */
    @Test
    public void testGetResultWithUnknownUuid(){
        GetResult getResult = new GetResult();
        String value = UUID.randomUUID().toString();
        getResult.setJobID(value);

        Object status = wps20Operations.getResult(getResult);

        assert (status instanceof ExceptionReport);

        ExceptionReport report = (ExceptionReport) status;
        ExceptionType exceptionType = report.getException().get(0);
        assert (exceptionType.getExceptionCode().equals("NoSuchJob"));
        assert (exceptionType.getLocator().equals(value));
    }

    /**
     * Test that execute will return an error (according to the WPS standard) when queried with an unknown process
     * identifier
     */
    @Test
    public void testExecuteWithUnknownProcessIdentifier(){
        ExecuteRequestType execute = new ExecuteRequestType();
        CodeType codeType = new CodeType();
        String falsyIdentifier = "thisprocessidentifierdoesnotexist";
        codeType.setValue(falsyIdentifier);
        execute.setIdentifier(codeType);
        Object status = wps20Operations.execute(execute);

        assert (status instanceof ExceptionReport);

        ExceptionReport report = (ExceptionReport) status;
        ExceptionType exceptionType = report.getException().get(0);
        assert (exceptionType.getExceptionCode().equals("NoSuchProcess"));
        assert (exceptionType.getLocator().equals(falsyIdentifier));
    }
}
