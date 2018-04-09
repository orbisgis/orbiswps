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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import net.opengis.ows._1.*;
import net.opengis.ows._1.ReferenceType;
import net.opengis.wps._1_0_0.*;
import net.opengis.wps._1_0_0.ComplexDataType;
import net.opengis.wps._1_0_0.DataType;
import net.opengis.wps._1_0_0.DescribeProcess;
import net.opengis.wps._1_0_0.LiteralDataType;
import net.opengis.wps._1_0_0.OutputDefinitionType;
import net.opengis.wps._1_0_0.OutputDescriptionType;
import net.opengis.wps._1_0_0.ProcessDescriptionType;
import net.opengis.wps._1_0_0.ProcessOfferings;
import net.opengis.wps._1_0_0.WPSCapabilitiesType;
import net.opengis.wps._2_0.*;
import net.opengis.wps._2_0.DescriptionType;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.service.model.JaxbContainer;
import org.orbisgis.orbiswps.service.process.ProcessManager;
import org.orbisgis.orbiswps.service.process.ProcessTranslator;
import org.orbisgis.orbiswps.service.utils.Job;
import org.orbisgis.orbiswps.service.utils.WpsDataUtils;
import org.orbisgis.orbiswps.service.utils.WpsServerUtils;
import org.orbisgis.orbiswps.serviceapi.operations.WPS_1_0_0_Operations;
import org.orbisgis.orbiswps.serviceapi.operations.WpsProperties;
import org.orbisgis.orbiswps.serviceapi.process.ProcessExecutionListener;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
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
    /** I18N */
    private static final I18n I18N = I18nFactory.getI18n(WPS_1_0_0_OperationsImpl.class);

    /** Map containing the WPS Jobs and their UUID */
    private Map<UUID, Job> jobMap;

    /** Instance of the WpsServer. */
    private WpsServerImpl wpsServer;

    /** WPS 2.0 properties of the server */
    private WpsServerProperties_1_0_0 wpsProp;

    private ProcessManager processManager;

    private DataSource ds;

    /** Main constructor */
    public WPS_1_0_0_OperationsImpl(WpsServerImpl wpsServer, WpsServerProperties_1_0_0 wpsProp,
                                    ProcessManager processManager){
        this.wpsProp = wpsProp;
        this.wpsServer = wpsServer;
        this.processManager = processManager;
        jobMap = new HashMap<>();
    }

    @Reference
    public void setDataSource(DataSource dataSource) {
        ds = dataSource;
    }
    public void unsetDataSource(DataSource dataSource) {
        ds = null;
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
            if(title.isSetLang() && title.getLang().equalsIgnoreCase(requestLanguage)) {
                serviceIdentification.getTitle().add(title);
            }
        }
        if (wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.ABSTRACT != null) {
            for (LanguageStringType abstract_ : wpsProp.SERVICE_IDENTIFICATION_PROPERTIES.ABSTRACT) {
                if(abstract_.isSetLang() && abstract_.getLang().equalsIgnoreCase(requestLanguage)) {
                    serviceIdentification.getAbstract().add(abstract_);
                }
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
    public Object describeProcess(DescribeProcess describeProcess) {
        ExceptionReport exceptionReport = new ExceptionReport();

        if(!describeProcess.isSetIdentifier() || describeProcess.getIdentifier().isEmpty()){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("MissingParameterValue");
            exceptionType.setLocator("Identifier");
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }

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
        if(!processDescriptions.isSetLang()) {
            if(describeProcess.isSetLanguage()){
                ExceptionType exceptionType = new ExceptionType();
                exceptionType.setExceptionCode("InvalidParameterValue");
                exceptionType.setLocator("Language+"+describeProcess.getLanguage());
                exceptionReport.getException().add(exceptionType);
                return exceptionReport;
            }
            else {
                processDescriptions.setLang(wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
                language = wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE;
            }
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
        if(processDescriptions.getProcessDescription().isEmpty()){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("InvalidParameterValue");
            StringBuilder locator = new StringBuilder();
            for(CodeType codeType : describeProcess.getIdentifier()){
                if(locator.length() > 0){
                    locator.append(",");
                }
                locator.append(codeType.getValue());
            }
            exceptionType.setLocator("Identifier+"+locator);
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }
        return processDescriptions;
    }

    @Override
    public Object execute(Execute execute) {
        ExceptionReport exceptionReport = new ExceptionReport();
        exceptionReport.setVersion("1.0.0");
        exceptionReport.setLang(wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
        //Test if the identifier of the Execute request in valid
        if(!execute.isSetIdentifier() || !execute.getIdentifier().isSetValue()){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("MissingParameterValue");
            exceptionType.setLocator("Identifier");
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }
        //Get the Process
        net.opengis.ows._2.CodeType codeType = new net.opengis.ows._2.CodeType();
        codeType.setValue(execute.getIdentifier().getValue());
        codeType.setCodeSpace(execute.getIdentifier().getCodeSpace());
        ProcessIdentifier pi = processManager.getProcessIdentifier(codeType);
        if(pi == null){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.setLocator("Identifier");
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }
        //Test language parameter
        String language = null;
        if(execute.isSetLanguage()){
            String bestLang = null;
            for(String lang : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES){
                if(lang.equalsIgnoreCase(execute.getLanguage())){
                    language = lang;
                    break;
                }
                else if(lang.substring(0, 2).equalsIgnoreCase(execute.getLanguage())){
                    bestLang = lang.substring(0, 2);
                }
            }
            if(bestLang != null){
                language = bestLang;
            }
        }
        else{
            language = wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE;
        }

        if(language == null){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.setLocator("language");
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }

        //Test that all the inputs of the Execute request are valid
        ExceptionReport report = checkExecuteInputs(execute, pi);
        if(report != null){
            return report;
        }
        //Generate the DataMap
        Map<URI, Object> dataMap = new HashMap<>();
        if(execute.isSetDataInputs()) {
            for (InputType input : execute.getDataInputs().getInput()) {
                URI id = URI.create(input.getIdentifier().getValue());
                if (input.isSetData() && input.getData().isSetBoundingBoxData()) {
                    try {
                        dataMap.put(id, WpsDataUtils.parseOws1BoundingBoxToGeometry(input.getData().getBoundingBoxData()));
                    } catch (ParseException e) {
                        LOGGER.error("Unable to parse the boundingbox '"+input.getIdentifier().getValue()+"'\n"+
                                e.getLocalizedMessage());
                        dataMap.put(id, null);
                    }
                } else if (input.isSetData() && input.getData().isSetComplexData()) {
                    for(Object data : input.getData().getComplexData().getContent()){
                        dataMap.put(id, data);
                    }
                } else if (input.isSetData() && input.getData().isSetLiteralData())  {
                    dataMap.put(id, input.getData().getLiteralData().getValue());
                }
                else if(input.isSetReference() && input.getReference().isSetHref()){
                    Object data = getReferenceData(input.getReference());
                    if(data instanceof ExceptionType){
                        exceptionReport.getException().add((ExceptionType)data);
                        return exceptionReport;
                    }
                    else {
                        dataMap.put(id, data);
                    }
                }
            }
        }

        //Test that all the mandatory inputs are set
        for(net.opengis.wps._2_0.InputDescriptionType input : pi.getProcessDescriptionType().getInput()){
            if(input.isSetMinOccurs() && input.getMinOccurs().intValue()>0 &&
                    !dataMap.containsKey(URI.create(input.getIdentifier().getValue()))){
                ExceptionType exceptionType = new ExceptionType();
                exceptionType.setExceptionCode("InvalidParameterValue");
                exceptionType.setLocator("DataInputs");
                exceptionType.getExceptionText().add("All the mandatory inputs should be set");
                exceptionReport.getException().add(exceptionType);
                return exceptionReport;
            }
        }

        //Test that all the outputs of the Execute request are valid
        if(execute.isSetResponseForm() && execute.getResponseForm().isSetResponseDocument() &&
                execute.getResponseForm().getResponseDocument().isSetStoreExecuteResponse() &&
                execute.getResponseForm().getResponseDocument().isSetStatus()){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.setLocator("ResponseFormType");
            exceptionType.getExceptionText().add("The 'status' parameter requires 'storeExecuteResponse' set to true");
        }
        if(execute.isSetResponseForm() && execute.getResponseForm().isSetResponseDocument()
                && execute.getResponseForm().getResponseDocument().isSetOutput()) {
            for (OutputDefinitionType output : execute.getResponseForm().getResponseDocument().getOutput()) {
                report = checkExecuteOutputs(output, pi);
                if (report != null) {
                    return report;
                }
            }
        }
        if(execute.isSetResponseForm() && execute.getResponseForm().isSetRawDataOutput()) {
            report = checkExecuteOutputs(execute.getResponseForm().getRawDataOutput(), pi);
            if (report != null) {
                return report;
            }
        }

        //Tests the ResponseForm object
        report = checkExecuteResponseForm(execute);
        if(report != null){
            return report;
        }

        return new WPS_1_0_0_JobRunner(exceptionReport, language, pi, dataMap, execute, wpsProp, wpsServer, ds)
                .getResponse();
    }

    private Object getReferenceData(InputReferenceType referenceType){
        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL(referenceType.getHref());
            connection = (HttpURLConnection) url.openConnection();
            if(referenceType.isSetMethod()) {
                connection.setRequestMethod(referenceType.getMethod());
            }
            connection.setRequestProperty("Content-Length", wpsProp.CUSTOM_PROPERTIES.MAXIMUM_MEGABYTES);
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception ex) {
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.setLocator("DataInputs");
            exceptionType.getExceptionText().add("Unable to get the data from the reference.\n"+ex.getMessage());
            LOGGER.error("Unable to get the data from the reference.\n"+ex.getMessage());
            return exceptionType;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Checks if the output is well formed and compatible with the outputs of the process.
     * @param output Output to test.
     * @param processIdentifier The process.
     * @return An ExceptionReport object if there is an error, null otherwise.
     */
    private ExceptionReport checkExecuteOutputs(OutputDefinitionType output, ProcessIdentifier processIdentifier) {
        ExceptionReport exceptionReport = new ExceptionReport();
        if(!output.isSetIdentifier()){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.setLocator("ResponseForm");
            exceptionType.getExceptionText().add("Output without identifier");
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }
        boolean isOutput = false;
        boolean isOutputFormat = false;
        for(net.opengis.wps._2_0.OutputDescriptionType outputType :
                processIdentifier.getProcessDescriptionType().getOutput()){
            if(outputType.getIdentifier().getValue().equals(output.getIdentifier().getValue())){
                isOutput = true;
            }
            if(output.isSetMimeType()) {
                for (net.opengis.wps._2_0.Format format : outputType.getDataDescription().getValue().getFormat()) {
                    if(format.getMimeType().equals(output.getMimeType())){
                        isOutputFormat = true;
                    }
                }
            }
            else{
                isOutputFormat = true;
            }
        }
        if(!isOutput){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.setLocator("ResponseForm");
            exceptionType.getExceptionText().add("There is no output "+output.getIdentifier().getValue()+
                    " in the process");
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }
        if(!isOutputFormat){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.setLocator("ResponseForm");
            exceptionType.getExceptionText().add("The format "+output.getMimeType()+ " is not supported");
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }
        return null;
    }

    /**
     * Checks if all the inputs is well formed and compatible with the outputs of the process.
     * @param execute Execute request.
     * @param processIdentifier The process.
     * @return An ExceptionReport object if there is an error, null otherwise.
     */
    private ExceptionReport checkExecuteInputs(Execute execute, ProcessIdentifier processIdentifier){
        ExceptionReport exceptionReport = new ExceptionReport();
        if(execute.isSetDataInputs()) {
            if(!execute.getDataInputs().isSetInput()){
                ExceptionType exceptionType = new ExceptionType();
                exceptionType.setExceptionCode("InvalidParameterValue");
                exceptionType.setLocator("DataInputs");
                exceptionType.getExceptionText().add("There should be at least one input set");
                exceptionReport.getException().add(exceptionType);
                return exceptionReport;
            }
            for (InputType input : execute.getDataInputs().getInput()) {
                if(!input.isSetIdentifier()){
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode("InvalidParameterValue");
                    exceptionType.setLocator("DataInputs");
                    exceptionType.getExceptionText().add("Input without identifier");
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
                if(!input.isSetData() && !input.isSetReference()){
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode("InvalidParameterValue");
                    exceptionType.setLocator("DataInputs");
                    exceptionType.getExceptionText().add("Input without dataType or reference");
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
                if(input.isSetData() && input.isSetReference()){
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode("InvalidParameterValue");
                    exceptionType.setLocator("DataInputs");
                    exceptionType.getExceptionText().add("Input with dataType and reference");
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
                boolean isInput = false;
                boolean isInputFormat = false;
                for(net.opengis.wps._2_0.InputDescriptionType inputType :
                        processIdentifier.getProcessDescriptionType().getInput()){
                    if(inputType.getIdentifier().getValue().equals(input.getIdentifier().getValue())){
                        isInput = true;
                    }
                    if(input.isSetData() && input.getData().isSetComplexData() &&
                            input.getData().getComplexData().isSetMimeType()) {
                        for (net.opengis.wps._2_0.Format format : inputType.getDataDescription().getValue().getFormat()) {
                            if(format.getMimeType().equals(input.getData().getComplexData().getMimeType())){
                                isInputFormat = true;
                            }
                        }
                    }
                    else{
                        isInputFormat = true;
                    }
                    if(isInput && input.isSetData() && input.getData().isSetLiteralData() &&
                            input.getData().getLiteralData().isSetDataType() &&
                            inputType.getDataDescription().getValue() instanceof net.opengis.wps._2_0.LiteralDataType){
                        String dataType = input.getData().getLiteralData().getDataType();
                        net.opengis.wps._2_0.LiteralDataType literalDataType =
                                (net.opengis.wps._2_0.LiteralDataType)inputType.getDataDescription().getValue();
                        boolean isDataType = false;
                        for(LiteralDataDomainType domain : literalDataType.getLiteralDataDomain()){
                            if((domain.getDataType().isSetValue() && domain.getDataType().getValue().equalsIgnoreCase(dataType)) ||
                                    (domain.getDataType().isSetReference() && domain.getDataType().getReference().equalsIgnoreCase(dataType))){
                                isDataType=true;
                            }
                        }
                        if(!isDataType) {
                            ExceptionType exceptionType = new ExceptionType();
                            exceptionType.setExceptionCode("InvalidParameterValue");
                            exceptionType.setLocator("DataInputs");
                            exceptionType.getExceptionText().add("The ComplexData input doesn't contains any values");
                            exceptionReport.getException().add(exceptionType);
                            return exceptionReport;
                        }
                    }
                }
                if(!isInput){
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode("InvalidParameterValue");
                    exceptionType.setLocator("DataInputs");
                    exceptionType.getExceptionText().add("There is no input "+input.getIdentifier().getValue()+
                            " in the process");
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
                if(!isInputFormat){
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode("InvalidParameterValue");
                    exceptionType.setLocator("DataInputs");
                    exceptionType.getExceptionText().add("The format "+input.getData().getComplexData().getMimeType()+
                            " is not supported");
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
                if (input.isSetData() && input.getData().isSetBoundingBoxData()
                        && input.getData().isSetComplexData() && input.getData().isSetLiteralData()) {
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode("InvalidParameterValue");
                    exceptionType.setLocator("DataInputs");
                    exceptionType.getExceptionText().add("Only one of BoundingBoxData or ComplexData or LiteralData " +
                            "of the  input should be set");
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
                else if (input.isSetData() && input.getData().isSetBoundingBoxData()) {
                    if(!input.getData().getBoundingBoxData().isSetCrs()){
                        ExceptionType exceptionType = new ExceptionType();
                        exceptionType.setExceptionCode("InvalidParameterValue");
                        exceptionType.setLocator("DataInputs");
                        exceptionType.getExceptionText().add("The BoundingBox CRS should be set");
                        exceptionReport.getException().add(exceptionType);
                        return exceptionReport;
                    }
                    if(!input.getData().getBoundingBoxData().isSetLowerCorner() ||
                            input.getData().getBoundingBoxData().getLowerCorner().isEmpty()){
                        ExceptionType exceptionType = new ExceptionType();
                        exceptionType.setExceptionCode("InvalidParameterValue");
                        exceptionType.setLocator("DataInputs");
                        exceptionType.getExceptionText().add("The BoundingBox lower corner should be set");
                        exceptionReport.getException().add(exceptionType);
                        return exceptionReport;
                    }
                    if(!input.getData().getBoundingBoxData().isSetUpperCorner() ||
                            input.getData().getBoundingBoxData().getUpperCorner().isEmpty()){
                        ExceptionType exceptionType = new ExceptionType();
                        exceptionType.setExceptionCode("InvalidParameterValue");
                        exceptionType.setLocator("DataInputs");
                        exceptionType.getExceptionText().add("The BoundingBox upper corner should be set");
                        exceptionReport.getException().add(exceptionType);
                        return exceptionReport;
                    }
                }
                else if (input.isSetData() && input.getData().isSetComplexData()) {
                    if(input.getData().getComplexData().getContent().isEmpty()){
                        ExceptionType exceptionType = new ExceptionType();
                        exceptionType.setExceptionCode("InvalidParameterValue");
                        exceptionType.setLocator("DataInputs");
                        exceptionType.getExceptionText().add("The ComplexData input doesn't contains any values");
                        exceptionReport.getException().add(exceptionType);
                        return exceptionReport;
                    }
                }
                else if(input.isSetData() && !input.getData().isSetLiteralData()){
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode("InvalidParameterValue");
                    exceptionType.setLocator("DataInputs");
                    exceptionType.getExceptionText().add("One of BoundingBoxData or ComplexData or LiteralData of the" +
                            " input should be set");
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
                else if(input.isSetReference() && !input.getReference().isSetHref()){
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode("InvalidParameterValue");
                    exceptionType.setLocator("DataInputs");
                    exceptionType.getExceptionText().add("The reference 'href' should be set");
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
            }
        }
        return null;
    }

    /**
     * Checks if all the ResponseForm is well formed and compatible with the server properties.
     * @param execute Execute request.
     * @return An ExceptionReport object if there is an error, null otherwise.
     */
    private ExceptionReport checkExecuteResponseForm(Execute execute){
        ExceptionReport exceptionReport = new ExceptionReport();
        if(execute.isSetResponseForm() && execute.getResponseForm().isSetResponseDocument() &&
                execute.getResponseForm().isSetRawDataOutput()){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.setLocator("ResponseForm");
            exceptionType.getExceptionText().add("Only one of ResponseDocument or RawDataOutput should be set");
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }

        if(execute.isSetResponseForm() && !execute.getResponseForm().isSetResponseDocument() &&
                !execute.getResponseForm().isSetRawDataOutput()){
            ExceptionType exceptionType = new ExceptionType();
            exceptionType.setExceptionCode("InvalidParameterValue");
            exceptionType.setLocator("ResponseForm");
            exceptionType.getExceptionText().add("One of ResponseDocument or RawDataOutput should be set");
            exceptionReport.getException().add(exceptionType);
            return exceptionReport;
        }
        if(execute.isSetResponseForm() && execute.getResponseForm().isSetResponseDocument()) {
            if (execute.getResponseForm().getResponseDocument().isStoreExecuteResponse()) {
                if (!wpsProp.GLOBAL_PROPERTIES.STORE_SUPPORTED) {
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode("StorageNotSupported");
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
            }
            if (execute.getResponseForm().getResponseDocument().isStatus()) {
                if (!wpsProp.GLOBAL_PROPERTIES.STATUS_SUPPORTED) {
                    ExceptionType exceptionType = new ExceptionType();
                    exceptionType.setExceptionCode("InvalidParameterValue");
                    exceptionType.setLocator("ResponseForm");
                    exceptionType.getExceptionText().add("Status not supported");
                    exceptionReport.getException().add(exceptionType);
                    return exceptionReport;
                }
            }
        }
        return null;
    }

    @Override
    public void setWpsProperties(WpsProperties wpsProperties) {
        if(wpsProperties.getWpsVersion().equals("1.0.0")){
            this.wpsProp = (WpsServerProperties_1_0_0) wpsProperties;
        }
    }
}
