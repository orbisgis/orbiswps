package org.orbisgis.orbiswps.service.operations;

import com.vividsolutions.jts.geom.Geometry;
import net.opengis.ows._1.BoundingBoxType;
import net.opengis.ows._1.ExceptionReport;
import net.opengis.ows._1.ExceptionType;
import net.opengis.ows._1.Operation;
import net.opengis.wps._1_0_0.*;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.service.model.JaxbContainer;
import org.orbisgis.orbiswps.service.process.ProcessTranslator;
import org.orbisgis.orbiswps.service.utils.Job;
import org.orbisgis.orbiswps.service.utils.WpsDataUtils;
import org.orbisgis.orbiswps.serviceapi.process.ProcessExecutionListener;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.orbisgis.orbiswps.service.operations.Converter.convertLanguageStringType2to1;

/**
 * Class managing the job execution and the generation of the Execute request response.
 *
 * @author Sylvain PALOMINOS
 */
public class WPS_1_0_0_JobRunner implements ProcessExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WPS_1_0_0_JobRunner.class);

    private Job job;
    private ResponseDocumentType responseDocumentType;
    private ExceptionReport exceptionReport;
    private String language;
    private ProcessIdentifier pi;
    private Map<URI, Object> dataMap;
    private Execute execute;
    private StatusType status;
    private ExecuteResponse.ProcessOutputs processOutputs = null;
    private Future future = null;
    private ExecuteResponse response = new ExecuteResponse();
    private WpsServerProperties_1_0_0 wpsProp;
    private WpsServerImpl wpsServer;

    public WPS_1_0_0_JobRunner(ExceptionReport exceptionReport, String language, ProcessIdentifier pi,
                               Map<URI, Object> dataMap, Execute execute, WpsServerProperties_1_0_0 wpsProperties,
                               WpsServerImpl wpsServer){
        if(execute.isSetResponseForm() && execute.getResponseForm().isSetResponseDocument()) {
            this.responseDocumentType = execute.getResponseForm().getResponseDocument();
        }
        this.exceptionReport = exceptionReport;
        this.language = language;
        this.pi = pi;
        this.dataMap = dataMap;
        this.execute = execute;
        this.wpsProp = wpsProperties;
        this.wpsServer = wpsServer;

        startJob();
        setResponse();
    }

    private void setResponse(){
        for (Operation op : wpsProp.OPERATIONS_METADATA_PROPERTIES.OPERATIONS) {
            if (op.getName().equalsIgnoreCase("getcapabilities")) {
                response.setServiceInstance(op.getDCP().get(0).getHTTP().getGetOrPost().get(0).getValue().getHref());
            }
        }
        if (execute.getLanguage() != null) {
            response.setLang(execute.getLanguage());
        } else {
            response.setLang(wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
        }
    }

    public Job getJob(){
        return job;
    }

    private void startJob(){
        //Generation of the Job unique ID
        UUID jobId = UUID.randomUUID();

        //Generate the processInstance
        List<String> languages = new ArrayList<>();
        languages.add(language);

        net.opengis.wps._2_0.ProcessDescriptionType process = ProcessTranslator.getTranslatedProcess(pi, languages);
        response.setProcess(Converter.convertProcessDescriptionType2to1(process));
        job = new Job(process, jobId, dataMap,
                wpsProp.CUSTOM_PROPERTIES.MAX_PROCESS_POLLING_DELAY,
                wpsProp.CUSTOM_PROPERTIES.BASE_PROCESS_POLLING_DELAY);
        job.addProcessExecutionlistener(this);

        //Process execution in new thread
        future = wpsServer.executeNewProcessWorker(job, pi, dataMap);

        //Sets the status parameter
        status = new StatusType();
        //Gets and set the process creationTime
        XMLGregorianCalendar xmlCalendar = null;
        try {
            GregorianCalendar gCalendar = new GregorianCalendar();
            gCalendar.setTime(new Date(job.getStartTime()));
            xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
        } catch (DatatypeConfigurationException e) {
            LOGGER.warn("Unable to get the current date into XMLGregorianCalendar : "+e.getMessage());
        }
        status.setCreationTime(xmlCalendar);
        updateStatus();
    }

    @Override
    public void appendLog(LogType logType, String message) {}

    @Override
    public void setProcessState(ProcessState processState) {
        updateStatus();
        response.setStatus(status);
        //If the process has finished and it should be store
        if(responseDocumentType != null && responseDocumentType.isSetStoreExecuteResponse() &&
                responseDocumentType.isStoreExecuteResponse() &&
                (processState.equals(ProcessState.FAILED) || processState.equals(ProcessState.SUCCEEDED))){
            getResponse();
        }
    }

    /**
     * Ask fo the process execution to finish and returns the result of the process as a ProcessOutputs object
     * containing all the outputs results.
     * @return A ProcessOutputs with the data of the outputs.
     */
    public ExecuteResponse.ProcessOutputs getResult(){
        job.removeProcessExecutionListener(this);
        if(future != null) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Error while waiting the process '" + job.getProcess().getIdentifier().getValue() + "' to" +
                        " finish\n" + e.getMessage());
            }
        }
        updateStatus();
        return processOutputs;
    }

    /**
     * Returns the response to the Execute request. If the generation of the response fails, return an ExceptionReport.
     * @return An ExecuteResponse if no fail, otherwise an ExceptionReport.
     */
    public Object getResponse(){
        updateStatus();
        response.setStatus(status);
        if(responseDocumentType != null){
            if (responseDocumentType.isLineage()) {
                response.setDataInputs(execute.getDataInputs());
                OutputDefinitionsType outputDefinitionsType = new OutputDefinitionsType();
                for (net.opengis.wps._2_0.OutputDescriptionType output : job.getProcess().getOutput()) {
                    DocumentOutputDefinitionType document = new DocumentOutputDefinitionType();
                    document.setTitle(convertLanguageStringType2to1(output.getTitle().get(0)));
                    if (output.getAbstract() == null && !output.getAbstract().isEmpty()) {
                        document.setAbstract(convertLanguageStringType2to1(output.getAbstract().get(0)));
                    }
                    outputDefinitionsType.getOutput().add(document);
                }
                response.setOutputDefinitions(outputDefinitionsType);
            }
            if (responseDocumentType.isStatus()) {
                //NotSupportedYet
            }

            if (!responseDocumentType.isSetStoreExecuteResponse() || !responseDocumentType.isStoreExecuteResponse()) {
                response.setProcessOutputs(getResult());
            } else {
                File f;
                try {
                    Marshaller marshaller = JaxbContainer.JAXBCONTEXT.createMarshaller();
                    f = new File(wpsProp.CUSTOM_PROPERTIES.WORKSPACE_PATH, job.getId().toString());
                    response.setStatusLocation(f.toURI().toString());
                    marshaller.marshal(response, new FileOutputStream(f));
                } catch (FileNotFoundException |JAXBException e) {
                    LOGGER.error("Error get on writing the response as an accessible " +
                            "resource.\n"+e.getMessage());
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode("NoApplicableCode");
                    exceptionType.getExceptionText().add("Error get on writing the response as an accessible " +
                            "resource.\n"+e.getMessage());
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
            }
        }
        else {
            response.setProcessOutputs(getResult());
        }
        return response;
    }

    /**
     * Updates the status of the process execution according to the job state.
     */
    private void updateStatus(){
        status.setProcessSucceeded(null);
        status.setProcessFailed(null);
        status.setProcessStarted(null);
        status.setProcessAccepted(null);
        status.setProcessPaused(null);
        switch(job.getState()){
            case IDLE:
                ProcessStartedType pst = new ProcessStartedType();
                pst.setPercentCompleted(job.getProgress());
                pst.setValue("idle");
                status.setProcessPaused(pst);
                break;
            case ACCEPTED:
                status.setProcessAccepted("accepted");
                break;
            case RUNNING:
                pst = new ProcessStartedType();
                pst.setPercentCompleted(job.getProgress());
                pst.setValue("running");
                status.setProcessStarted(pst);
                break;
            case FAILED:
                ProcessFailedType pft = new ProcessFailedType();
                //Sets a detailed exception to return to the client
                pft.setExceptionReport(exceptionReport);
                ExceptionType exceptionType = new ExceptionType();
                exceptionType.setExceptionCode("NoApplicableCode");
                for(Map.Entry<String, ProcessExecutionListener.LogType> entry : job.getLogMap().entrySet()) {
                    if(entry.getValue().equals(ProcessExecutionListener.LogType.ERROR)) {
                        exceptionType.getExceptionText().add(entry.getKey());
                    }
                }
                exceptionReport.getException().add(exceptionType);
                status.setProcessFailed(pft);
                break;
            case SUCCEEDED:
                Map<URI, Object> dataMap = job.getDataMap();
                status.setProcessSucceeded("succeeded");
                ProcessDescriptionType.ProcessOutputs outputs = Converter.convertOutputDescriptionTypeList2to1(
                        job.getProcess().getOutput(), wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE, language,
                        new BigInteger(wpsProp.CUSTOM_PROPERTIES.MAXIMUM_MEGABYTES));
                processOutputs = new ExecuteResponse.ProcessOutputs();
                //Iterate on the output defined in the Execute request and on the  output in the process to build
                // the response outputs
                if(responseDocumentType!=null && responseDocumentType.isSetOutput() &&
                        !responseDocumentType.getOutput().isEmpty()) {
                    for (DocumentOutputDefinitionType output : responseDocumentType.getOutput()) {
                        processOutputs.getOutput().addAll(getOutputData(outputs, output, dataMap));
                        response.setProcessOutputs(processOutputs);
                    }
                }
                else{
                    processOutputs.getOutput().addAll(getOutputData(outputs, null, dataMap));
                    response.setProcessOutputs(processOutputs);
                }
                break;
        }
    }

    /**
     * Returns the list of OutputDataType object generated from the given ProcessOutputs which match with the given
     * DocumentOutputDefinitionType if defined with the correct data from the given map.
     * If the DocumentOutputDefinitionType is set, try to use its specification (like mimeType) to set the outputs.
     * @param outputs Object containing all the process outputs.
     * @param output Output from the execute request.
     *               If not null, the returned OutputDataTypes should match with it.
     *               If null, return all the OutputDataTypes
     * @param dataMap Map containing the result data of the outputs
     * @return Lhe list of OutputDataType object generated
     */
    private List<OutputDataType> getOutputData(ProcessDescriptionType.ProcessOutputs outputs,
                                               DocumentOutputDefinitionType output, Map<URI, Object> dataMap){
        List<OutputDataType> list = new ArrayList<>();
        for(OutputDescriptionType outputDscrType : outputs.getOutput()) {
            if(output== null || output.getIdentifier().getValue().equals(outputDscrType.getIdentifier().getValue())) {
                URI uri = URI.create(outputDscrType.getIdentifier().getValue());
                Object o = dataMap.get(uri);

                OutputDataType outputDataType = new OutputDataType();
                list.add(outputDataType);
                outputDataType.setTitle(outputDscrType.getTitle());
                outputDataType.setAbstract(outputDscrType.getAbstract());
                outputDataType.setIdentifier(outputDscrType.getIdentifier());
                if(output != null && output.isSetAsReference() && output.isAsReference() && o != null) {
                    if (o instanceof Serializable) {
                        try {
                            File file = new File(wpsProp.CUSTOM_PROPERTIES.WORKSPACE_PATH,
                                    uri.toString().replaceAll(":", "_"));
                            FileOutputStream fout = new FileOutputStream(file);
                            ObjectOutputStream oos = new ObjectOutputStream(fout);
                            oos.writeObject(o);
                            OutputReferenceType referenceType = new OutputReferenceType();
                            referenceType.setHref(file.toURI().toURL().toString());
                            outputDataType.setReference(referenceType);
                        } catch (IOException e) {
                            LOGGER.error("Unable to write the serializable object '" + uri.toString() + "'\n" +
                                    e.getMessage());
                        }
                    } else {
                        LOGGER.warn("Unable to write the object '" + uri.toString() + "', it should be an instance of Serializable");
                    }
                }
                else {
                    DataType dataType = new DataType();
                    outputDataType.setData(dataType);

                    if (outputDscrType.isSetLiteralOutput()) {
                        LiteralDataType literalDataType = new LiteralDataType();
                        dataType.setLiteralData(literalDataType);
                        literalDataType.setDataType(outputDscrType.getLiteralOutput().getDataType().getValue());
                        if (o instanceof String[]) {
                            StringBuilder data = new StringBuilder();
                            for (String str : (String[]) o) {
                                if (data.length() > 0) {
                                    data.append(";");
                                }
                                data.append(str);
                            }
                            literalDataType.setValue(data.toString());
                            dataType.setLiteralData(literalDataType);
                        } else if (o != null) {
                            literalDataType.setValue(o.toString());
                            dataType.setLiteralData(literalDataType);
                        } else {
                            literalDataType.setValue(null);
                            dataType.setLiteralData(literalDataType);
                        }
                    } else if (outputDscrType.isSetBoundingBoxOutput()) {
                        if (o instanceof Geometry) {
                            dataType.setBoundingBoxData(WpsDataUtils.parseGeometryToOws1BoundingBox((Geometry) o));
                        } else {
                            LOGGER.error("The output '" + uri + "' should be a Geometry");
                            dataType.setBoundingBoxData(new BoundingBoxType());
                        }

                    } else if (outputDscrType.isSetComplexOutput()) {
                        ComplexDataCombinationType dflt = outputDscrType.getComplexOutput().getDefault();
                        ComplexDataType complexDataType = new ComplexDataType();
                        if (output != null && output.isSetMimeType()) {
                            complexDataType.setMimeType(output.getMimeType());
                        } else {
                            complexDataType.setMimeType(dflt.getFormat().getMimeType());
                        }
                        if (output != null && output.isSetEncoding()) {
                            complexDataType.setEncoding(output.getEncoding());
                        } else {
                            complexDataType.setEncoding(dflt.getFormat().getEncoding());
                        }
                        if (output != null && output.isSetSchema()) {
                            complexDataType.setSchema(output.getSchema());
                        } else {
                            complexDataType.setSchema(dflt.getFormat().getSchema());
                        }
                        complexDataType.getContent().add(o);
                        dataType.setComplexData(complexDataType);
                    }
                }
            }
        }
        return list;
    }
}
