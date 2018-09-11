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

import net.opengis.ows._2.*;
import net.opengis.wps._2_0.*;
import net.opengis.wps._2_0.GetCapabilitiesType;
import net.opengis.wps._2_0.ObjectFactory;
import org.orbisgis.orbiswps.service.process.ModelWorker;
import org.orbisgis.orbiswps.service.process.ProcessIdentifierImpl;
import org.orbisgis.orbiswps.service.process.ProcessTranslator;
import org.orbisgis.orbiswps.service.process.ProcessWorkerImpl;
import org.orbisgis.orbiswps.service.utils.Job;
import org.orbisgis.orbiswps.service.utils.WpsServerUtils;
import org.orbisgis.orbiswps.serviceapi.operations.WpsOperations;
import org.orbisgis.orbiswps.serviceapi.operations.WpsProperties;
import org.orbisgis.orbiswps.serviceapi.process.ProcessExecutionListener;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.orbisgis.orbiswps.serviceapi.process.ProcessManager;
import org.orbisgis.orbiswps.serviceapi.process.ProcessWorker;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.net.URI;
import java.util.*;

/**
 * Implementations of the WPS 2.0 operations. This class is called when a wps request with the version 2.0 is
 * received by the service.
 *
 * @author Sylvain PALOMINOS (CNRS 2017, UBS 2018)
 * @author Erwan Bocher (CNRS)
 */
@Component(immediate = true, service = WpsOperations.class)
public class WPS_2_0_Operations implements WpsOperations {

    /** WPS version */
    private static final String WPS_VERSION = "2.0";
    /** Encoding simple */
    private static final String ENCODING_SIMPLE = "simple";

    /** Map containing the WPS Jobs and their UUID */
    private Map<UUID, Job> jobMap = new HashMap<>();
    /** WPS 2.0 properties of the server */
    private WPS_2_0_ServerProperties wpsProp;
    /** DataSource used of the execution of the processes */
    private DataSource ds;
    /** Wps 2.0 ObjectFactory */
    private ObjectFactory factory = new ObjectFactory();
    /** Process Manager used for the process execution. */
    private ProcessManager processManager;

    /** Enumeration of the section names. */
    private enum SectionName {ServiceIdentification, ServiceProvider, OperationMetadata, Contents, Languages, All}
    /** Enumeration of the exception code. */
    private enum ExceptionCode
    {InvalidParameterValue, NoApplicableCode, VersionNegotiationFailed, NoSuchProcess, NoSuchJob}
    /** Enumeration of the exception locator. */
    private enum ExceptionLocator {Sections, AcceptLanguages, Lang, Identifier, NoSuchJob}


    ////////////////////
    /// Constructors ///
    ////////////////////


    /**
     * Main constructor.
     *
     * @param processManager Instance of the ProcessManager.
     * @param wpsProp WPS properties of the server.
     * @param dataSource DataSource used of the execution of the processes.
     */
    public WPS_2_0_Operations(ProcessManager processManager, WPS_2_0_ServerProperties wpsProp, DataSource dataSource){
        setWpsProperties(wpsProp);
        setProcessManager(processManager);
        setDataSource(dataSource);
    }

    /**
     * Constructor without properties
     *
     * @param processManager Instance of the ProcessManager.
     * @param dataSource DataSource used of the execution of the processes.
     */
    public WPS_2_0_Operations(ProcessManager processManager, DataSource dataSource){
        setProcessManager(processManager);
        setDataSource(dataSource);
    }

    /**
     * Empty constructor mainly used in case of an OSGI application. If it is not the case, use instead
     * {@code WPS_2_0_Operations(WpsServiceImpl wpsService, WPS_2_0_ServerProperties wpsProp, DataSource dataSource)}
     */
    public WPS_2_0_Operations(){}


    /////////////////////////////
    /// OSGI setter unsetters ///
    /////////////////////////////


    @Reference
    public void setDataSource(DataSource dataSource) {
        ds = dataSource;
    }
    public void unsetDataSource(DataSource dataSource) {
        ds = null;
    }

    @Reference
    @Override
    public boolean setWpsProperties(WpsProperties wpsProperties) {
        if(wpsProperties != null && wpsProperties.getWpsVersion().equals(WPS_VERSION)){
            this.wpsProp = (WPS_2_0_ServerProperties) wpsProperties;
            return true;
        }
        return false;
    }
    public void unsetWpsProperties(WpsProperties wpsProperties) {
        wpsProperties = null;
    }


    ////////////////////////
    /// Override methods ///
    ////////////////////////


    @Override
    public String getWpsVersion() {
        return WPS_VERSION;
    }

    @Override
    public boolean isRequestAccepted(Object request) {
        return request instanceof GetCapabilitiesType ||
                request instanceof DescribeProcess ||
                request instanceof ExecuteRequestType ||
                request instanceof GetStatus ||
                request instanceof GetResult ||
                request instanceof Dismiss;
    }

    @Override
    public Object executeRequest(Object request) {
        Object result = null;
        if(request instanceof GetCapabilitiesType){
            result = getCapabilities((GetCapabilitiesType)request);
        }
        else if(request instanceof DescribeProcess){
            result = describeProcess((DescribeProcess)request);
        }
        else if(request instanceof ExecuteRequestType){
            result = execute((ExecuteRequestType)request);
        }
        else if(request instanceof GetStatus){
            result = getStatus((GetStatus)request);
        }
        else if(request instanceof GetResult){
            result = getResult((GetResult)request);
        }
        else if(request instanceof Dismiss){
            result = dismiss((Dismiss)request);
        }
        return result;
    }

    @Override
    public void setProcessManager(ProcessManager processManager) {
        this.processManager = processManager;
    }


    /////////////////////////
    /// WPS 1.0.0 methods ///
    /////////////////////////


    /**
     * This operation allows a client to retrieve service metadata, basic process offerings, and the available
     * processes present on a WPS server.
     *
     * @param getCapabilities Request to a WPS server to perform the GetCapabilities operation.
     *                        This operation allows a client to retrieve a Capabilities XML document providing
     *                        metadata for the specific WPS server.
     * @return WPS GetCapabilities operation response.
     *             This document provides clients with service metadata about a specific service instance,
     *             including metadata about the processes that can be executed.
     *             Since the server does not implement the updateSequence and Sections parameters,
     *             the server shall always return the complete Capabilities document,
     *             without the updateSequence parameter.
     */
    private Object getCapabilities(GetCapabilitiesType getCapabilities){
        /* First check the getCapabilities for exceptions */
        ExceptionReport exceptionReport = new ExceptionReport();
        if(getCapabilities == null){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode(ExceptionCode.NoApplicableCode.name());
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }
        //Accepted versions check
        //If the version is not supported, add an ExceptionType with the error.
        if(getCapabilities.getAcceptVersions() != null &&
                getCapabilities.getAcceptVersions().getVersion() != null){
            boolean isVersionAccepted = false;
            for(String version1 : getCapabilities.getAcceptVersions().getVersion()){
                for(String version2 : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_VERSIONS){
                    if(version1.equals(version2)){
                        isVersionAccepted = true;
                    }
                }
            }
            if(!isVersionAccepted) {
                ExceptionType exceptionType = new ExceptionType();
                exceptionType.setExceptionCode(ExceptionCode.VersionNegotiationFailed.name());
                exceptionReport.getException().add(exceptionType);
                return exceptionReport;
            }
        }
        //Sections check
        //Check if all the section values are one of the 'SectionName' enum values.
        //If not, add an ExceptionType with the error.
        List<SectionName> requestedSections = new ArrayList<>();
        if(getCapabilities.getSections() != null && getCapabilities.getSections().getSection() != null) {
            for (String section : getCapabilities.getSections().getSection()) {
                boolean validSection = false;
                for (SectionName sectionName : SectionName.values()) {
                    if (section.equals(sectionName.name())) {
                        validSection = true;
                    }
                }
                if (!validSection) {
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode(ExceptionCode.InvalidParameterValue.name());
                    exceptionType.setLocator(ExceptionLocator.Sections.name()+":"+section);
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
                else{
                    requestedSections.add(SectionName.valueOf(section));
                }
            }
        }
        else{
            requestedSections.add(SectionName.All);
        }
        //Languages check
        //If the language is not supported, add an ExceptionType with the error.
        List<String> availableLanguages = new ArrayList<>();
        if(getCapabilities.getAcceptLanguages() != null &&
                getCapabilities.getAcceptLanguages().getLanguage() != null &&
                !getCapabilities.getAcceptLanguages().getLanguage().isEmpty()) {
            List<String> requestedLanguages = getCapabilities.getAcceptLanguages().getLanguage();
            if(requestedLanguages.contains("*")){
                Collections.addAll(availableLanguages, wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES);
            }
            else{
                for(String requestedLanguage : requestedLanguages){
                    String exactLanguage = null;
                    String bestEffortLanguage1 = null;
                    String bestEffortLanguage2 = null;
                    for(String serverLanguage : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES){
                        if(requestedLanguage.equalsIgnoreCase(serverLanguage)){
                            exactLanguage = requestedLanguage;
                        }
                        if(requestedLanguage.substring(0, 2).equalsIgnoreCase(serverLanguage)){
                            bestEffortLanguage1 = serverLanguage;
                        }
                        if(requestedLanguage.substring(0, 2).equalsIgnoreCase(serverLanguage.substring(0, 2))){
                            bestEffortLanguage2 = serverLanguage;
                        }
                    }
                    if(exactLanguage != null){
                        availableLanguages.add(exactLanguage);
                    }
                    else if(bestEffortLanguage1 != null){
                        availableLanguages.add(bestEffortLanguage1);
                    }
                    else if(bestEffortLanguage2 != null){
                        availableLanguages.add(bestEffortLanguage2);
                    }
                    else{
                        ExceptionType exceptionType = new ExceptionType();
                        exceptionType.setExceptionCode(ExceptionCode.InvalidParameterValue.name());
                        exceptionType.setLocator(ExceptionLocator.AcceptLanguages.name());
                        exceptionReport.getException().add(exceptionType);
                        return exceptionReport;
                    }
                }
            }
        }
        //If no compatible language has been found, all the service languages are used
        if (availableLanguages.isEmpty()) {
            Collections.addAll(availableLanguages, wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES);
        }

        /* Building of the WPSCapabilitiesTypeAnswer */

        //Copy the content of the basicCapabilities into the new one
        WPSCapabilitiesType capabilitiesType = new WPSCapabilitiesType();
        capabilitiesType.setExtension(new WPSCapabilitiesType.Extension());
        capabilitiesType.setUpdateSequence(wpsProp.GLOBAL_PROPERTIES.SERVER_VERSION);
        capabilitiesType.setVersion(WPS_VERSION);
        if(requestedSections.contains(SectionName.All) || requestedSections.contains(SectionName.Languages)) {
            CapabilitiesBaseType.Languages languages = new CapabilitiesBaseType.Languages();
            for(String language : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES) {
                languages.getLanguage().add(language);
            }
            capabilitiesType.setLanguages(languages);
        }
        if(requestedSections.contains(SectionName.All) || requestedSections.contains(SectionName.OperationMetadata)) {
            OperationsMetadata operationsMetadata = new OperationsMetadata();
            List<Operation> operationList = new ArrayList<>(wpsProp.OPERATIONS_METADATA_PROPERTIES.OPERATIONS);
            operationList.removeAll(Collections.singleton(null));
            operationsMetadata.getOperation().addAll(operationList);
            capabilitiesType.setOperationsMetadata(operationsMetadata);
        }
        if(requestedSections.contains(SectionName.All) || requestedSections.contains(SectionName.ServiceIdentification)) {
            ServiceIdentification serviceIdentification = new ServiceIdentification();
            if(wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.FEES!=null) {
                serviceIdentification.setFees(wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.FEES);
            }
            serviceIdentification.setServiceType(wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.SERVICE_TYPE);
            for(String version : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.SERVICE_TYPE_VERSIONS) {
                serviceIdentification.getServiceTypeVersion().add(version);
            }
            for(LanguageStringType title : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.TITLE) {
                serviceIdentification.getTitle().add(title);
            }
            for(LanguageStringType abstract_ : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.ABSTRACT) {
                serviceIdentification.getAbstract().add(abstract_);
            }
            if(wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.KEYWORDS != null) {
                for (KeywordsType keywords : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.KEYWORDS) {
                    serviceIdentification.getKeywords().add(keywords);
                }
            }
            if(wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.ACCESS_CONSTRAINTS != null) {
                for (String constraint : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.ACCESS_CONSTRAINTS) {
                    serviceIdentification.getAccessConstraints().add(constraint);
                }
            }
            capabilitiesType.setServiceIdentification(serviceIdentification);
        }
        if(requestedSections.contains(SectionName.All) || requestedSections.contains(SectionName.ServiceProvider)) {
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setProviderName(wpsProp.SERVICE_PROVIDER_PROPERTIES.PROVIDER_NAME);
            serviceProvider.setProviderSite(wpsProp.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE);
            serviceProvider.setServiceContact(wpsProp.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT);
            capabilitiesType.setServiceProvider(serviceProvider);
        }

        /* Sets the Contents */
        if(requestedSections.contains(SectionName.All) || requestedSections.contains(SectionName.Contents)) {
            Contents contents = new Contents();
            List<ProcessSummaryType> processSummaryTypeList = new ArrayList<>();
            List<ProcessIdentifier> processIdList = processManager.getAllProcessIdentifier();
            for (ProcessIdentifier pId : processIdList) {
                ProcessDescriptionType translatedProcess = ProcessTranslator.getTranslatedProcess(
                        pId, availableLanguages);
                ProcessSummaryType processSummaryType = new ProcessSummaryType();
                processSummaryType.getJobControlOptions().clear();
                processSummaryType.getJobControlOptions().addAll(Arrays.asList(wpsProp.GLOBAL_PROPERTIES.JOB_CONTROL_OPTIONS));
                processSummaryType.getOutputTransmission().clear();
                for(String str : wpsProp.GLOBAL_PROPERTIES.DATA_TRANSMISSION_TYPE){
                    if(str.equalsIgnoreCase(DataTransmissionModeType.VALUE.value())){
                        processSummaryType.getOutputTransmission().add(DataTransmissionModeType.VALUE);
                    }
                    else if(str.equalsIgnoreCase(DataTransmissionModeType.REFERENCE.value())){
                        processSummaryType.getOutputTransmission().add(DataTransmissionModeType.REFERENCE);
                    }
                }
                processSummaryType.setIdentifier(translatedProcess.getIdentifier());
                processSummaryType.getMetadata().clear();
                processSummaryType.getMetadata().addAll(translatedProcess.getMetadata());
                processSummaryType.getAbstract().clear();
                processSummaryType.getAbstract().addAll(translatedProcess.getAbstract());
                processSummaryType.getTitle().clear();
                processSummaryType.getTitle().addAll(translatedProcess.getTitle());
                processSummaryType.getKeywords().clear();
                processSummaryType.getKeywords().addAll(translatedProcess.getKeywords());
                processSummaryType.setProcessVersion(pId.getProcessOffering().getProcessVersion());
                processSummaryType.setProcessModel(pId.getProcessOffering().getProcessModel());

                processSummaryTypeList.add(processSummaryType);
            }
            contents.getProcessSummary().clear();
            contents.getProcessSummary().addAll(processSummaryTypeList);
            capabilitiesType.setContents(contents);
        }

        return factory.createCapabilities(capabilitiesType);
    }

    /**
     * The DescribeProcess operation allows WPS clients to query detailed process descriptions for the process
     * offerings.
     *
     * @param describeProcess WPS DescribeProcess operation request.
     * @return List structure that is returned by the WPS DescribeProcess operation.
     *         Contains XML descriptions for the queried process identifiers.
     */
    private Object describeProcess(DescribeProcess describeProcess) {

        ExceptionReport exceptionReport = new ExceptionReport();

        if(describeProcess.isSetLang()) {
            boolean isLang = Arrays.asList(wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES).contains(describeProcess.getLang());
            if (!isLang) {
                for (String suppLang : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES) {
                    if (suppLang.substring(0, 2).equalsIgnoreCase(describeProcess.getLang().substring(0, 2))) {
                        isLang = true;
                    }
                }
            }

            if (!isLang) {
                ExceptionType exceptionType = new ExceptionType();
                exceptionType.setExceptionCode(ExceptionCode.InvalidParameterValue.name());
                exceptionType.setLocator(ExceptionLocator.Lang.name());
                exceptionReport.getException().add(exceptionType);
                return exceptionReport;
            }
        }

        if(describeProcess.getIdentifier().isEmpty()){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode(ExceptionCode.InvalidParameterValue.name());
            exceptionType.setLocator(ExceptionLocator.Identifier.name());
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }

        //Get the list of the ids of the process to describe
        List<CodeType> idList = describeProcess.getIdentifier();

        ProcessOfferings processOfferings = new ProcessOfferings();
        List<ProcessOffering> processOfferingList = new ArrayList<>();
        List<String> wrongId = new ArrayList<>();
        //For each of the processes
        for(CodeType id : idList) {
            List<ProcessIdentifier> piList = processManager.getAllProcessIdentifier();
            ProcessOffering po = new ProcessOffering();
            //Find the process registered in the server with the same id
            for(ProcessIdentifier pi : piList){
                if(pi.getProcessDescriptionType().getIdentifier().getValue().equals(id.getValue())){
                    //Once the process found, build the corresponding processOffering to send to the client
                    if(pi.getProcessOffering() != null) {
                        //Build the new ProcessOffering which will be return
                        po.setProcessVersion(pi.getProcessOffering().getProcessVersion());
                        po.getJobControlOptions().clear();
                        po.getJobControlOptions().addAll(Arrays.asList(wpsProp.GLOBAL_PROPERTIES.JOB_CONTROL_OPTIONS));
                        //Get the translated process and add it to the ProcessOffering
                        List<DataTransmissionModeType> listTransmission = new ArrayList<>();
                        listTransmission.add(DataTransmissionModeType.VALUE);
                        po.getOutputTransmission().clear();
                        po.getOutputTransmission().addAll(listTransmission);
                        if(describeProcess.isSetLang()) {
                            List<String> languages = new ArrayList<>();
                            languages.add(describeProcess.getLang());
                            po.setProcess(ProcessTranslator.getTranslatedProcess(pi, languages));
                        }
                        else{
                            po.setProcess(pi.getProcessDescriptionType());
                        }
                    }
                }
            }
            if(!po.isSetProcess()){
                wrongId.add(id.getValue());
            }
            else {
                processOfferingList.add(po);
            }
        }
        if(processOfferingList.isEmpty()){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode(ExceptionCode.NoSuchProcess.name());
            exceptionType.getExceptionText().addAll(wrongId);
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }
        else {
            processOfferings.getProcessOffering().clear();
            processOfferings.getProcessOffering().addAll(processOfferingList);
            return processOfferings;
        }
    }

    /**
     * The Execute operation allows WPS clients to run a specified process implemented by a server,
     * using the input parameter values provided and returning the output values produced.
     * Inputs may be included directly in the Execute request (by value), or reference web accessible resources
     * (by reference).
     * The outputs may be returned in the form of an XML response document,
     * either embedded within the response document or stored as web accessible resources.
     * Alternatively, for a single output, the server may be directed to return that output in its raw form without
     * being wrapped in an XML response document.
     *
     * @param execute The Execute request is a common structure for synchronous and asynchronous execution.
     *                It inherits basic properties from the RequestBaseType and contains additional elements that
     *                identify the process that shall be executed, the model inputs and outputs, and the response type
     *                of the service.
     * @return Depending on the desired execution mode and the response type declared in the execute request,
     *         the execute response may take one of three different forms:
     *         A response document, a StatusInfo document, or raw model.
     */
    private Object execute(ExecuteRequestType execute) {
        //Generate the DataMap
        Map<URI, Object> dataMap = new HashMap<>();
        for(DataInputType input : execute.getInput()){
            URI id = URI.create(input.getId());
            Object data;
            if(input.getData().getContent().size() == 1){
                data = input.getData().getContent().get(0);
            }
            else if(input.getData().getContent().size() == 0){
                data = null;
            }
            else{
                data = input.getData().getContent();
            }
            dataMap.put(id, data);
        }

        ProcessIdentifier processIdentifier = processManager.getProcessIdentifier(execute.getIdentifier());
        if (processIdentifier == null){
            ExceptionReport exceptionReport = new ExceptionReport();
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode(WPS_2_0_Operations.ExceptionCode.NoSuchProcess.name());
            exceptionType.setLocator(execute.getIdentifier().getValue());
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }
        WPS_2_0_Worker worker;
        if(processIdentifier instanceof ProcessIdentifierImpl && ((ProcessIdentifierImpl) processIdentifier).isModel()){
            worker = new ModelWorker(wpsProp, (ProcessIdentifierImpl)processIdentifier, processManager, dataMap);
        }
        else {
            worker = new WPS_2_0_Worker(wpsProp, processIdentifier, processManager, dataMap);
        }
        //Generation of the StatusInfo
        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setJobID(worker.getJobId().toString());
        jobMap.put(worker.getJobId(), worker.getJob());
        statusInfo.setStatus(worker.getJob().getState().name());

        //Process execution in new thread
        processManager.executeNewProcessWorker(worker);
        //Return the StatusInfo to the user
        XMLGregorianCalendar date = WpsServerUtils.getXMLGregorianCalendar(worker.getJob().getProcessPollingTime());
        statusInfo.setNextPoll(date);
        return statusInfo;
    }

    /**
     * WPS GetStatus operation request. This operation is used to query status information of executed processes.
     * The response to a GetStatus operation is a StatusInfo document or an exception.
     * Depending on the implementation, a WPS may "forget" old process executions sooner or later.
     * In this case, there is no status information available and an exception shall be returned instead of a
     * StatusInfo response.
     *
     * @param getStatus GetStatus document. It contains an additional element that identifies the JobID of the
     *                  processing job, of which the status shall be returned.
     * @return StatusInfo document.
     */
    private Object getStatus(GetStatus getStatus) {
        //Get the job concerned by the getStatus request
        UUID jobId = UUID.fromString(getStatus.getJobID());
        Job job = jobMap.get(jobId);

        if (job == null){
            ExceptionReport exceptionReport = new ExceptionReport();
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode(ExceptionCode.NoSuchJob.name());
            exceptionType.setLocator(getStatus.getJobID());
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }

        //Generate the StatusInfo to return
        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setJobID(jobId.toString());
        statusInfo.setStatus(job.getState().name());
        int progress = job.getProgress();
        statusInfo.setPercentCompleted(progress);
        if(progress != 0) {
            long millisSpent = System.currentTimeMillis() - job.getStartTime();
            long millisLeft = (millisSpent / progress) * (100 - progress);
            statusInfo.setEstimatedCompletion(WpsServerUtils.getXMLGregorianCalendar(millisLeft));
        }
        if(!job.getState().equals(ProcessExecutionListener.ProcessState.FAILED) &&
                !job.getState().equals(ProcessExecutionListener.ProcessState.SUCCEEDED)) {
            XMLGregorianCalendar date = WpsServerUtils.getXMLGregorianCalendar(job.getProcessPollingTime());
            statusInfo.setNextPoll(date);
        }

        return statusInfo;
    }

    /**
     * WPS GetResult operation request. This operation is used to query the results of asynchrously
     * executed processes. The response to a GetResult operation is a wps:ProcessingResult, a raw model response, or an exception.
     * Depending on the implementation, a WPS may "forget" old process executions sooner or later.
     * In this case, there is no result information available and an exception shall be returned.
     *
     * @param getResult GetResult document. It contains an additional element that identifies the JobID of the
     *                  processing job, of which the result shall be returned.
     * @return Result document.
     */
    private Object getResult(GetResult getResult) {
        Result result = new Result();
        //generate the XMLGregorianCalendar Object to put in the Result Object
        long destructionDelay = wpsProp.CUSTOM_PROPERTIES.getDestroyDelayInMillis();
        result.setExpirationDate(WpsServerUtils.getXMLGregorianCalendar(destructionDelay));
        //Get the concerned Job
        UUID jobId = UUID.fromString(getResult.getJobID());
        Job job = jobMap.get(jobId);

        if (job == null){
            ExceptionReport exceptionReport = new ExceptionReport();
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode(ExceptionCode.NoSuchJob.name());
            exceptionType.setLocator(getResult.getJobID());
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }

        result.setJobID(jobId.toString());
        //Get the list of outputs to transmit
        List<DataOutputType> listOutput = new ArrayList<>();
        for(Map.Entry<URI, Object> entry : job.getDataMap().entrySet()) {
            boolean isOutput = false;
            for(OutputDescriptionType output : job.getProcess().getOutput()) {
                if (output.getIdentifier().getValue().equals(entry.getKey().toString())) {
                    isOutput = true;
                }
            }
            if (isOutput) {
                //Test if the URI is an Output URI.
                DataOutputType output = new DataOutputType();
                output.setId(entry.getKey().toString());
                Data data = new Data();
                data.setEncoding(ENCODING_SIMPLE);
                data.setMimeType("");
                List<Serializable> serializableList = new ArrayList<>();
                if (entry.getValue() == null) {
                    serializableList.add("");
                } else {
                    serializableList.add(entry.getValue().toString());
                }
                data.getContent().clear();
                data.getContent().addAll(serializableList);
                output.setData(data);
                listOutput.add(output);
                //Sets and schedule the destroy date
                if (destructionDelay != 0) {
                    processManager.scheduleResultDestroying(entry.getKey(),
                            WpsServerUtils.getXMLGregorianCalendar(destructionDelay));
                }
            }
        }
        result.getOutput().clear();
        result.getOutput().addAll(listOutput);

        jobMap.remove(jobId);

        return result;
    }

    /**
     * The dismiss operation allow a client to communicate that he is no longer interested in the results of a job.
     * In this case, the server may free all associated resources and “forget” the JobID.
     * For jobs that are still running, the server may cancel the execution at any time.
     * For jobs that were already finished, the associated status information and the stored results may be deleted
     * without further notice, regardless of the expiration time given in the last status report.
     * @param dismiss Dismiss request.
     * @return StatusInfo document.
     */
    private StatusInfo dismiss(Dismiss dismiss) {
        UUID jobId = UUID.fromString(dismiss.getJobID());
        processManager.cancelProcess(jobId);
        Job job = jobMap.get(jobId);
        //Generate the StatusInfo to return
        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setJobID(jobId.toString());
        statusInfo.setStatus(job.getState().name());
        if(!job.getState().equals(ProcessExecutionListener.ProcessState.FAILED) &&
                !job.getState().equals(ProcessExecutionListener.ProcessState.SUCCEEDED)) {
            XMLGregorianCalendar date = WpsServerUtils.getXMLGregorianCalendar(job.getProcessPollingTime());
            statusInfo.setNextPoll(date);
        }
        return statusInfo;
    }
}
