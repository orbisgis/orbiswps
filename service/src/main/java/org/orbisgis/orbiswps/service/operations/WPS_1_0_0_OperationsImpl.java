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

import net.opengis.ows._1.*;
import net.opengis.wps._1_0_0.*;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.service.process.ProcessManager;
import org.orbisgis.orbiswps.service.process.ProcessTranslator;
import org.orbisgis.orbiswps.service.utils.Job;
import org.orbisgis.orbiswps.service.utils.WpsServerUtils;
import org.orbisgis.orbiswps.serviceapi.operations.WPS_1_0_0_Operations;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.orbisgis.orbiswps.service.operations.Converter.*;

/**
 * Implementations of the WPS 1.0.0 operations.
 *
 * @author Sylvain PALOMINOS
 */
public class WPS_1_0_0_OperationsImpl implements WPS_1_0_0_Operations {

    private static final Logger LOGGER = LoggerFactory.getLogger(WPS_1_0_0_OperationsImpl.class);

    /** Map containing the WPS Jobs and their UUID */
    private Map<UUID, Job> jobMap;

    /** Instance of the WpsServer. */
    private WpsServerImpl wpsServer;

    /** WPS 2.0 properties of the server */
    private WpsServerProperties_1_0_0 wpsProp;

    private ProcessManager processManager;

    /** Main constructor */
    public WPS_1_0_0_OperationsImpl(WpsServerImpl wpsServer, WpsServerProperties_1_0_0 wpsProp,
                                    ProcessManager processManager){
        this.wpsProp = wpsProp;
        this.wpsServer = wpsServer;
        this.processManager = processManager;
        jobMap = new HashMap<>();
    }

    @Override
    public Object getCapabilities(GetCapabilities getCapabilities) {
        // First check the getCapabilities for exceptions
        ExceptionReport exceptionReport = new ExceptionReport();
        if (getCapabilities == null) {
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("NoApplicableCode");
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }

        //Accepted versions check
        //If the version is not supported, add an ExceptionType with the error.
        if (getCapabilities.isSetAcceptVersions() && getCapabilities.getAcceptVersions().isSetVersion()) {
            boolean isVersionAccepted = false;
            for (String version1 : getCapabilities.getAcceptVersions().getVersion()) {
                if(wpsProp.GLOBAL_PROPERTIES.SUPPORTED_VERSIONS == null){
                    if("1.0.0".equals(version1)){
                        isVersionAccepted = true;
                    }
                }
                else {
                    for (String version2 : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_VERSIONS) {
                        if (version1.equals(version2)) {
                            isVersionAccepted = true;
                        }
                    }
                }
            }
            if (!isVersionAccepted) {
                ExceptionType exceptionType = new ExceptionType();
                exceptionType.setExceptionCode("VersionNegotiationFailed");
                exceptionReport.getException().add(exceptionType);
                return exceptionReport;
            }
        }


        //Languages check
        //If the language is not supported, add an ExceptionType with the error.
        String requestLanguage = wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE;
        if (getCapabilities.getLanguage() != null && !getCapabilities.getLanguage().isEmpty()) {
            String requestedLanguage = getCapabilities.getLanguage();
            boolean isAnyLanguage = requestedLanguage.contains("*");
            boolean languageFound = false;
            //First try to find the first languages requested by the client which is supported by the server
            for (String language2 : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES) {
                if (language2.equals(requestedLanguage)) {
                    requestLanguage = language2;
                    languageFound = true;
                    break;
                }
            }
            //If not language was found, try to get one with best-effort semantic
            if (!languageFound) {
                //avoid to test "*" language
                if (!requestedLanguage.equals("*")) {
                    String baseLanguage = requestedLanguage.substring(0, 2);
                    for (String serverLanguage : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES) {
                        if (serverLanguage.substring(0, 2).equals(baseLanguage)) {
                            languageFound = true;
                            requestLanguage = baseLanguage;
                            break;
                        }
                    }
                }
            }
            //If not language was found, try to use any language if allowed
            if (!languageFound && isAnyLanguage) {
                requestLanguage = wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE;
                languageFound = true;
            }
            //If no compatible language has been found and not any language are accepted
            if (!languageFound) {
                ExceptionType exceptionType = new ExceptionType();
                exceptionType.setExceptionCode("InvalidParameterValue");
                exceptionType.setLocator("AcceptLanguages");
                exceptionReport.getException().add(exceptionType);
                return exceptionReport;
            }
        }

        getCapabilities.getLanguage();

        //Sets the WPSCapabilitiesType
        WPSCapabilitiesType wpsCapabilitiesType = new WPSCapabilitiesType();

        wpsCapabilitiesType.setLang(requestLanguage);

        Languages languages = new Languages();
        Languages.Default dflt = new Languages.Default();
        dflt.setLanguage(wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
        languages.setDefault(dflt);
        LanguagesType languagesType = new LanguagesType();
        languagesType.getLanguage().addAll(Arrays.asList(wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES));
        languages.setSupported(languagesType);
        wpsCapabilitiesType.setLanguages(languages);

        wpsCapabilitiesType.setVersion("1.0.0");

        ProcessOfferings processOfferings = new ProcessOfferings();
        List<ProcessIdentifier> piList = processManager.getAllProcessIdentifier();
        for (ProcessIdentifier pi : piList) {
            ProcessBriefType processBriefType = new ProcessBriefType();
            List<String> requestLanguages = new ArrayList<>();
            requestLanguages.add(requestLanguage);
            net.opengis.wps._2_0.ProcessDescriptionType translatedProcess = ProcessTranslator.getTranslatedProcess(
                    pi, requestLanguages);
            processBriefType.setProcessVersion(pi.getProcessOffering().getProcessVersion());
            processBriefType.setTitle(convertLanguageStringTypeList2to1(translatedProcess.getTitle()).get(0));
            processBriefType.setAbstract(convertLanguageStringTypeList2to1(translatedProcess.getAbstract()).get(0));
            processBriefType.setIdentifier(convertCodeType2to1(translatedProcess.getIdentifier()));
            for (net.opengis.ows._2.MetadataType metadataType : translatedProcess.getMetadata()) {
                processBriefType.getMetadata().add(convertMetadataType2to1(metadataType));
            }
            processOfferings.getProcess().add(processBriefType);
        }
        wpsCapabilitiesType.setProcessOfferings(processOfferings);

        if(wpsProp.WSDL_PROPERTIES != null && wpsProp.WSDL_PROPERTIES.HREF != null) {
            WSDL wsdl = new WSDL();
            wsdl.setHref(wpsProp.WSDL_PROPERTIES.HREF);
            wpsCapabilitiesType.setWSDL(wsdl);
        }

        OperationsMetadata operationsMetadata = new OperationsMetadata();
        List<Operation> operationList = new ArrayList<>();
        operationList.addAll(wpsProp.OPERATIONS_METADATA_PROPERTIES.OPERATIONS);
        operationList.removeAll(Collections.singleton(null));
        operationsMetadata.getOperation().addAll(operationList);
        if(wpsProp.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS != null) {
            operationsMetadata.getConstraint().addAll(wpsProp.OPERATIONS_METADATA_PROPERTIES.CONSTRAINTS);
        }
        if(wpsProp.OPERATIONS_METADATA_PROPERTIES.PARAMETERS != null) {
            operationsMetadata.getParameter().addAll(wpsProp.OPERATIONS_METADATA_PROPERTIES.PARAMETERS);
        }
        operationsMetadata.setExtendedCapabilities(wpsProp.OPERATIONS_METADATA_PROPERTIES.EXTENDED_CAPABILITIES);
        wpsCapabilitiesType.setOperationsMetadata(operationsMetadata);

        ServiceIdentification serviceIdentification = new ServiceIdentification();
        if(wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.FEES!=null) {
            serviceIdentification.setFees(wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.FEES);
        }
        if(wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.PROFILE!=null) {
            Collections.addAll(serviceIdentification.getProfile(), wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.PROFILE);
        }
        serviceIdentification.setServiceType(wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.SERVICE_TYPE);
        for (String version : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.SERVICE_TYPE_VERSIONS) {
            serviceIdentification.getServiceTypeVersion().add(version);
        }
        for (LanguageStringType title : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.TITLE) {
            serviceIdentification.getTitle().add(title);
        }
        if (wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.ABSTRACT != null) {
            for (LanguageStringType abstract_ : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.ABSTRACT) {
                serviceIdentification.getAbstract().add(abstract_);
            }
        }
        if (wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.KEYWORDS != null){
            for (KeywordsType keywords : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.KEYWORDS) {
                serviceIdentification.getKeywords().add(keywords);
            }
        }
        if(wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.ACCESS_CONSTRAINTS != null) {
            for (String constraint : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.ACCESS_CONSTRAINTS) {
                serviceIdentification.getAccessConstraints().add(constraint);
            }
        }
        wpsCapabilitiesType.setServiceIdentification(serviceIdentification);

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProviderName(wpsProp.SERVICE_PROVIDER_PROPERTIES.PROVIDER_NAME);
        serviceProvider.setProviderSite(wpsProp.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE);
        serviceProvider.setServiceContact(wpsProp.SERVICE_PROVIDER_PROPERTIES.SERVICE_CONTACT);
        wpsCapabilitiesType.setServiceProvider(serviceProvider);

        if(wpsProp.GLOBAL_PROPERTIES.UPDATE_SEQUENCE != null) {
            wpsCapabilitiesType.setUpdateSequence(wpsProp.GLOBAL_PROPERTIES.UPDATE_SEQUENCE);
        }

        return wpsCapabilitiesType;
    }

    @Override
    public ProcessDescriptions describeProcess(DescribeProcess describeProcess) {
        String language = describeProcess.getLanguage();
        List<CodeType> codeTypeList = describeProcess.getIdentifier();
        ProcessDescriptions processDescriptions = new ProcessDescriptions();

        if(wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE.equals(language)){
            processDescriptions.setLang(language);
        }
        else {
            for(String lang : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES) {
                if (lang.equals(language)) {
                    processDescriptions.setLang(language);

                }
            }
        }
        if(!processDescriptions.isSetLang()){
            processDescriptions.setLang(wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
            language = wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE;
        }

        List<ProcessIdentifier> processList = processManager.getAllProcessIdentifier();
        for (ProcessIdentifier pId : processList) {
            net.opengis.wps._2_0.ProcessDescriptionType process = pId.getProcessDescriptionType();
            for(CodeType codeType : codeTypeList){
                if(process.getIdentifier().getValue().equals(codeType.getValue())) {
                    List<String> languages = new ArrayList<>();
                    languages.add(language);
                    process = ProcessTranslator.getTranslatedProcess(pId, languages);
                    ProcessDescriptionType processDescriptionType = convertProcessDescriptionType2to1(process);
                    processDescriptionType.setProcessVersion(pId.getProcessOffering().getProcessVersion());
                    processDescriptionType.setStatusSupported(wpsProp.GLOBAL_PROPERTIES.STATUS_SUPPORTED);
                    processDescriptionType.setStoreSupported(wpsProp.GLOBAL_PROPERTIES.STORE_SUPPORTED);
                    ProcessDescriptionType.DataInputs dataInputs = new ProcessDescriptionType.DataInputs();
                    dataInputs.getInput().addAll(convertInputDescriptionTypeList2to1(process.getInput(),
                            wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE, language,
                            new BigInteger(wpsProp.CUSTOM_PROPERTIES.MAXIMUM_MEGABYTES)));
                    processDescriptionType.setDataInputs(dataInputs);
                    processDescriptionType.setProcessOutputs(convertOutputDescriptionTypeList2to1(process.getOutput(),
                            wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE, language,
                            new BigInteger(wpsProp.CUSTOM_PROPERTIES.MAXIMUM_MEGABYTES)));

                    if(wpsProp.WSDL_PROPERTIES != null && wpsProp.WSDL_PROPERTIES.HREF != null) {
                        WSDL wsdl = new WSDL();
                        wsdl.setHref(wpsProp.WSDL_PROPERTIES.HREF);
                        processDescriptionType.setWSDL(wsdl);
                    }

                    processDescriptions.getProcessDescription().add(processDescriptionType);
                }
            }
        }
        return processDescriptions;
    }

    @Override
    public Object execute(Execute execute) {
        //Generate the DataMap
        Map<URI, Object> dataMap = new HashMap<>();
        for(InputType input : execute.getDataInputs().getInput()){
            URI id = URI.create(input.getIdentifier().getValue());
            Object data = null;
            if(input.getData().isSetBoundingBoxData()){
                data = input.getData().getBoundingBoxData();
            }
            else if(input.getData().isSetComplexData()){
                data = input.getData().getComplexData();
            }
            else if(input.getData().isSetLiteralData()){
                data = input.getData().getLiteralData().getValue();
            }
            dataMap.put(id, data);
        }
        //Generation of the Job unique ID
        UUID jobId = UUID.randomUUID();
        //Get the Process
        net.opengis.ows._2.CodeType codeType = new net.opengis.ows._2.CodeType();
        codeType.setValue(execute.getIdentifier().getValue());
        codeType.setCodeSpace(execute.getIdentifier().getCodeSpace());
        ProcessIdentifier processIdentifier = processManager.getProcessIdentifier(codeType);

        //Generate the processInstance
        Job job = new Job(processIdentifier.getProcessDescriptionType(), jobId, dataMap,
                wpsProp.CUSTOM_PROPERTIES.MAX_PROCESS_POLLING_DELAY,
                wpsProp.CUSTOM_PROPERTIES.BASE_PROCESS_POLLING_DELAY);
        jobMap.put(jobId, job);

        //Process execution in new thread
        Future future = wpsServer.executeNewProcessWorker(job, processIdentifier, dataMap);

        Object object = null;

        //If the required output is a raw model
        if(execute.getResponseForm().isSetRawDataOutput()){
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Error while waiting thread to be finished : "+e.getMessage());
            }
            for(Map.Entry<URI, Object> entry : job.getDataMap().entrySet()){
                //Test if the URI is an Output URI.
                boolean contained = false;
                for(net.opengis.wps._2_0.OutputDescriptionType output : job.getProcess().getOutput()){
                    if(output.getIdentifier().getValue().equals(entry.getKey().toString())){
                        contained = true;
                    }
                }
                if(contained) {
                    object = entry.getValue();
                    //Sets and schedule the destroy date
                    long destructionDelay = wpsProp.CUSTOM_PROPERTIES.getDestroyDelayInMillis();
                    if(destructionDelay != 0) {
                        wpsServer.scheduleResultDestroying(entry.getKey(),
                                WpsServerUtils.getXMLGregorianCalendar(destructionDelay));
                    }
                }
            }

            jobMap.remove(jobId);

            return object;
        }
        else if(execute.getResponseForm().isSetResponseDocument()){
            ExecuteResponse response = new ExecuteResponse();
            if(execute.getResponseForm().getResponseDocument().isLineage()){
                response.setDataInputs(execute.getDataInputs());
                OutputDefinitionsType outputDefinitionsType = new OutputDefinitionsType();
                for(net.opengis.wps._2_0.OutputDescriptionType output : job.getProcess().getOutput()) {
                    DocumentOutputDefinitionType document = new DocumentOutputDefinitionType();
                    document.setTitle(convertLanguageStringType2to1(output.getTitle().get(0)));
                    if(output.getAbstract()==null && !output.getAbstract().isEmpty()) {
                        document.setAbstract(convertLanguageStringType2to1(output.getAbstract().get(0)));
                    }
                    outputDefinitionsType.getOutput().add(document);
                }
                response.setOutputDefinitions(outputDefinitionsType);
            }
            if(execute.getResponseForm().getResponseDocument().isStatus()){
                //NotSupportedYet
            }
            if(execute.getResponseForm().getResponseDocument().isStoreExecuteResponse()){
                //NotSupportedYet
            }
            response.setProcess(convertProcessDescriptionType2to1(job.getProcess()));
            StatusType status = new StatusType();
            XMLGregorianCalendar xmlCalendar = null;
            try {
                GregorianCalendar gCalendar = new GregorianCalendar();
                gCalendar.setTime(new Date(System.currentTimeMillis()));
                xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
            } catch (DatatypeConfigurationException e) {
                LOGGER.warn("Unable to get the current date into XMLGregorianCalendar : "+e.getMessage());
            }
            status.setCreationTime(xmlCalendar);
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
                    ExceptionReport exceptionReport = new ExceptionReport();
                    exceptionReport.setVersion("1.0.0");
                    exceptionReport.setLang(wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
                    pft.setExceptionReport(exceptionReport);
                    status.setProcessFailed(pft);
                    break;
                case SUCCEEDED:
                    status.setProcessSucceeded("succeeded");
                    ExecuteResponse.ProcessOutputs processOutputs = new ExecuteResponse.ProcessOutputs();
                    for(Map.Entry<URI, Object> entry : job.getDataMap().entrySet()){
                        //Test if the URI is an Output URI.
                        boolean contained = false;
                        for(net.opengis.wps._2_0.OutputDescriptionType output : job.getProcess().getOutput()){
                            if(output.getIdentifier().getValue().equals(entry.getKey().toString())){
                                contained = true;
                            }
                        }
                        if(contained) {
                            OutputDataType outputDataType = new OutputDataType();
                            DataType dataType = new DataType();
                            LiteralDataType literalDataType = new LiteralDataType();
                            literalDataType.setValue(entry.getValue().toString());
                            literalDataType.setDataType("string");
                            dataType.setLiteralData(literalDataType);
                            outputDataType.setData(dataType);
                            //Sets and schedule the destroy date
                            long destructionDelay = wpsProp.CUSTOM_PROPERTIES.getDestroyDelayInMillis();
                            if(destructionDelay != 0) {
                                wpsServer.scheduleResultDestroying(entry.getKey(),
                                        WpsServerUtils.getXMLGregorianCalendar(destructionDelay));
                            }
                        }
                    }
                    response.setProcessOutputs(processOutputs);
                    break;
            }
            response.setStatus(status);
            if(execute.getLanguage()!=null) {
                response.setLang(execute.getLanguage());
            }
            else{
                response.setLang(wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
            }
            return response;
        }
        return null;
    }
}
