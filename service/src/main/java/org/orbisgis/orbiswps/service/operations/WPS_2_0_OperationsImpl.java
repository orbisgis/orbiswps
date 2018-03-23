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
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.serviceapi.operations.WpsProperties;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.orbisgis.orbiswps.service.process.ProcessManager;
import org.orbisgis.orbiswps.service.utils.Job;
import org.orbisgis.orbiswps.serviceapi.process.ProcessExecutionListener;
import org.orbisgis.orbiswps.service.process.ProcessTranslator;
import org.orbisgis.orbiswps.service.utils.WpsServerUtils;
import org.orbisgis.orbiswps.serviceapi.operations.WPS_2_0_Operations;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.net.URI;
import java.util.*;

/**
 * Implementations of the WPS 2.0 operations.
 * 
 * @author Sylvain PALOMINOS
 */
public class WPS_2_0_OperationsImpl implements WPS_2_0_Operations {

    /** Map containing the WPS Jobs and their UUID */
    private Map<UUID, Job> jobMap;

    /** Instance of the WpsServer. */
    private WpsServerImpl wpsServer;

    /** WPS 2.0 properties of the server */
    private WpsServerProperties_2_0 wpsProp;

    private ProcessManager processManager;

    /** Main constructor */
    public WPS_2_0_OperationsImpl(WpsServerImpl wpsServer, WpsServerProperties_2_0 wpsProp,
                                  ProcessManager processManager){
        this.wpsServer = wpsServer;
        this.wpsProp = wpsProp;
        this.processManager = processManager;
        jobMap = new HashMap<>();
    }

    @Override
    public void setWpsProperties(WpsProperties wpsProperties) {
        if(wpsProperties.getWpsVersion().equals("2.0")){
            this.wpsProp = (WpsServerProperties_2_0) wpsProperties;
        }
    }

    /** Enumeration of the section names. */
    private enum SectionName {ServiceIdentification, ServiceProvider, OperationMetadata, Contents, Languages, All}

    @Override
    public Object getCapabilities(GetCapabilitiesType getCapabilities){
        /** First check the getCapabilities for exceptions **/
        ExceptionReport exceptionReport = new ExceptionReport();
        if(getCapabilities == null){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("NoApplicableCode");
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
                exceptionType.setExceptionCode("VersionNegotiationFailed");
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
                    exceptionType.setExceptionCode("InvalidParameterValue");
                    exceptionType.setLocator("Sections:"+section);
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
                        exceptionType.setExceptionCode("InvalidParameterValue");
                        exceptionType.setLocator("AcceptLanguages");
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
        //Output format check
        //Only the text/xml mime type is support for now, so check are needed

        /** Building of the WPSCapabilitiesTypeAnswer **/

        //Copy the content of the basicCapabilities into the new one
        WPSCapabilitiesType capabilitiesType = new WPSCapabilitiesType();
        capabilitiesType.setExtension(new WPSCapabilitiesType.Extension());
        capabilitiesType.setUpdateSequence(wpsProp.GLOBAL_PROPERTIES.SERVER_VERSION);
        capabilitiesType.setVersion("2.0");
        if(requestedSections.contains(SectionName.All) || requestedSections.contains(SectionName.Languages)) {
            CapabilitiesBaseType.Languages languages = new CapabilitiesBaseType.Languages();
            for(String language : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES) {
                languages.getLanguage().add(language);
            }
            capabilitiesType.setLanguages(languages);
        }
        if(requestedSections.contains(SectionName.All) || requestedSections.contains(SectionName.OperationMetadata)) {
            OperationsMetadata operationsMetadata = new OperationsMetadata();
            List<Operation> operationList = new ArrayList<>();
            operationList.add(wpsProp.OPERATIONS_METADATA_PROPERTIES.DESCRIBE_PROCESS_OPERATION);
            operationList.add(wpsProp.OPERATIONS_METADATA_PROPERTIES.DISMISS_OPERATION);
            operationList.add(wpsProp.OPERATIONS_METADATA_PROPERTIES.EXECUTE_OPERATION);
            operationList.add(wpsProp.OPERATIONS_METADATA_PROPERTIES.GET_CAPABILITIES_OPERATION);
            operationList.add(wpsProp.OPERATIONS_METADATA_PROPERTIES.GET_RESULT_OPERATION);
            operationList.add(wpsProp.OPERATIONS_METADATA_PROPERTIES.GET_STATUS_OPERATION);
            operationList.removeAll(Collections.singleton(null));
            operationsMetadata.getOperation().addAll(operationList);
            capabilitiesType.setOperationsMetadata(operationsMetadata);
        }
        if(requestedSections.contains(SectionName.All) || requestedSections.contains(SectionName.ServiceIdentification)) {
            ServiceIdentification serviceIdentification = new ServiceIdentification();
            serviceIdentification.setFees(wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.FEES);
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
            for(KeywordsType keywords : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.KEYWORDS) {
                serviceIdentification.getKeywords().add(keywords);
            }
            for(String constraint : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.ACCESS_CONSTRAINTS) {
                serviceIdentification.getAccessConstraints().add(constraint);
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

        /** Sets the Contents **/
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

        return capabilitiesType;
    }

    @Override
    public Object describeProcess(DescribeProcess describeProcess) {

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
                exceptionType.setExceptionCode("InvalidParameterValue");
                exceptionType.setLocator("Lang");
                exceptionReport.getException().add(exceptionType);
                return exceptionReport;
            }
        }

        if(describeProcess.getIdentifier().isEmpty()){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.setLocator("Identifier");
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
            exceptionType.setExceptionCode("NoSuchProcess");
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

    @Override
    public Object execute(ExecuteRequestType execute) {
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
        //Generation of the StatusInfo
        StatusInfo statusInfo = new StatusInfo();
        //Generation of the Job unique ID
        UUID jobId = UUID.randomUUID();
        statusInfo.setJobID(jobId.toString());
        //Get the Process
        ProcessIdentifier processIdentifier = processManager.getProcessIdentifier(execute.getIdentifier());

        if (processIdentifier == null){
            ExceptionReport exceptionReport = new ExceptionReport();
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("NoSuchProcess");
            exceptionType.setLocator(execute.getIdentifier().getValue());
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }

        //Generate the processInstance
        Job job = new Job(processIdentifier.getProcessDescriptionType(), jobId, dataMap,
                wpsProp.CUSTOM_PROPERTIES.MAX_PROCESS_POLLING_DELAY,
                wpsProp.CUSTOM_PROPERTIES.BASE_PROCESS_POLLING_DELAY);
        jobMap.put(jobId, job);
        statusInfo.setStatus(job.getState().name());

        //Process execution in new thread
        wpsServer.executeNewProcessWorker(job, processIdentifier, dataMap);
        //Return the StatusInfo to the user
        statusInfo.setStatus(job.getState().name());
        XMLGregorianCalendar date = WpsServerUtils.getXMLGregorianCalendar(job.getProcessPollingTime());
        statusInfo.setNextPoll(date);
        return statusInfo;
    }

    @Override
    public Object getStatus(GetStatus getStatus) {
        //Get the job concerned by the getStatus request
        UUID jobId = UUID.fromString(getStatus.getJobID());
        Job job = jobMap.get(jobId);

        if (job == null){
            ExceptionReport exceptionReport = new ExceptionReport();
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("NoSuchJob");
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

    @Override
    public Object getResult(GetResult getResult) {
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
            exceptionType.setExceptionCode("NoSuchJob");
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
                data.setEncoding("simple");
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
                    wpsServer.scheduleResultDestroying(entry.getKey(),
                            WpsServerUtils.getXMLGregorianCalendar(destructionDelay));
                }
            }
        }
        result.getOutput().clear();
        result.getOutput().addAll(listOutput);

        jobMap.remove(jobId);

        return result;
    }

    @Override
    public StatusInfo dismiss(Dismiss dismiss) {
        UUID jobId = UUID.fromString(dismiss.getJobID());
        wpsServer.cancelProcess(jobId);
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
