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
package org.orbiswps.server;

import net.opengis.ows._1.*;
import net.opengis.wps._1_0_0.*;
import net.opengis.wps._1_0_0.DescribeProcess;
import net.opengis.wps._1_0_0.InputDescriptionType;
import net.opengis.wps._1_0_0.OutputDescriptionType;
import net.opengis.wps._1_0_0.ProcessDescriptionType;
import net.opengis.wps._1_0_0.ProcessOfferings;
import net.opengis.wps._1_0_0.WPSCapabilitiesType;
import net.opengis.wps._2_0.*;
import net.opengis.wps._2_0.ComplexDataType;
import net.opengis.wps._2_0.LiteralDataType;
import org.orbiswps.server.controller.process.ProcessIdentifier;
import org.orbiswps.server.controller.utils.Job;
import org.orbiswps.server.utils.ProcessTranslator;
import org.orbiswps.server.utils.WpsServerProperties_1_0_0;

import java.math.BigInteger;
import java.util.*;

/**
 * Implementations of the WPS 1.0.0 operations.
 *
 * @author Sylvain PALOMINOS
 */
public class WPS_1_0_0_OperationsImpl implements WPS_1_0_0_Operations {

    /** Map containing the WPS Jobs and their UUID */
    private Map<UUID, Job> jobMap;

    /** Instance of the WpsServer. */
    private WpsServerImpl wpsServer;

    /** WPS 2.0 properties of the server */
    private static WpsServerProperties_1_0_0 wpsProp;

    /** Main constructor */
    public WPS_1_0_0_OperationsImpl(WpsServerImpl wpsServer, WpsServerProperties_1_0_0 wpsProp){
        WPS_1_0_0_OperationsImpl.wpsProp = wpsProp;
        this.wpsServer = wpsServer;
        jobMap = new HashMap<>();
    }

    @Override
    public Object getCapabilities(GetCapabilities getCapabilities) {
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


        //Languages check
        //If the language is not supported, add an ExceptionType with the error.
        String requestLanguage = wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE;
        if(getCapabilities.getLanguage() != null && !getCapabilities.getLanguage().isEmpty()) {
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
            if(!languageFound){
                //avoid to test "*" language
                if(!requestedLanguage.equals("*")) {
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
            if(!languageFound  && isAnyLanguage){
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
        List<ProcessIdentifier> piList = wpsServer.getProcessManager().getAllProcessIdentifier();
        for (ProcessIdentifier pi : piList) {
            net.opengis.wps._2_0.ProcessDescriptionType process = pi.getProcessDescriptionType();
            ProcessBriefType processBriefType = new ProcessBriefType();
            net.opengis.wps._2_0.ProcessDescriptionType translatedProcess = ProcessTranslator.getTranslatedProcess(
                    process, requestLanguage, wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
            processBriefType.setProcessVersion(pi.getProcessOffering().getProcessVersion());
            processBriefType.setTitle(convertLanguageStringTypeList2to1(translatedProcess.getTitle()).get(0));
            processBriefType.setAbstract(convertLanguageStringTypeList2to1(translatedProcess.getAbstract()).get(0));
            processBriefType.setIdentifier(convertCodeType2to1(translatedProcess.getIdentifier()));
            for(net.opengis.ows._2.MetadataType metadataType : translatedProcess.getMetadata()) {
                processBriefType.getMetadata().add(convertMetadataType2to1(metadataType));
            }
            processOfferings.getProcess().add(processBriefType);
        }
        wpsCapabilitiesType.setProcessOfferings(processOfferings);

        WSDL wsdl = new WSDL();
        wsdl.setHref(wpsProp.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE.getHref());
        wpsCapabilitiesType.setWSDL(wsdl);

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
        wpsCapabilitiesType.setOperationsMetadata(operationsMetadata);

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
        wpsCapabilitiesType.setServiceIdentification(serviceIdentification);

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProviderName(wpsProp.SERVICE_PROVIDER_PROPERTIES.PROVIDER_NAME);
        serviceProvider.setProviderSite(wpsProp.SERVICE_PROVIDER_PROPERTIES.PROVIDER_SITE);
        wpsCapabilitiesType.setServiceProvider(serviceProvider);

        wpsCapabilitiesType.setUpdateSequence(wpsProp.GLOBAL_PROPERTIES.SERVER_VERSION);

        return wpsCapabilitiesType;
    }

    @Override
    public ProcessDescriptions describeProcess(DescribeProcess describeProcess) {
        String language = describeProcess.getLanguage();
        List<CodeType> codeTypeList = describeProcess.getIdentifier();
        ProcessDescriptions processDescriptions = new ProcessDescriptions();

        if(language.equals(wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE)){
            processDescriptions.setLang(language);
        }
        else {
            for(String lang : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES) {
                if (language.equals(lang)) {
                    processDescriptions.setLang(language);

                }
            }
        }
        if(!processDescriptions.isSetLang()){
            processDescriptions.setLang(wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
            language = wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE;
        }

        List<ProcessIdentifier> processList = wpsServer.getProcessList();
        for (ProcessIdentifier pId : processList) {
            net.opengis.wps._2_0.ProcessDescriptionType process = pId.getProcessDescriptionType();
            for(CodeType codeType : codeTypeList){
                if(process.getIdentifier().getValue().equals(codeType.getValue())) {
                    process = ProcessTranslator.getTranslatedProcess(process, language,
                            wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
                    ProcessDescriptionType processDescriptionType = convertProcessDescriptionType2to1(process);
                    processDescriptionType.setProcessVersion(pId.getProcessOffering().getProcessVersion());
                    processDescriptionType.setStatusSupported(wpsProp.CUSTOM_PROPERTIES.IS_STATUS_SUPPORTED);
                    processDescriptionType.setStoreSupported(wpsProp.CUSTOM_PROPERTIES.IS_STORE_SUPPORTED);
                    ProcessDescriptionType.DataInputs dataInputs = new ProcessDescriptionType.DataInputs();
                    dataInputs.getInput().addAll(convertInputDescriptionTypeList2to1(process.getInput(),
                            wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE, language));
                    processDescriptionType.setDataInputs(dataInputs);
                    processDescriptionType.setProcessOutputs(convertOutputDescriptionTypeList2to1(process.getOutput(),
                            wpsProp.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE, language));
                    processDescriptions.getProcessDescription().add(processDescriptionType);
                }
            }
        }
        return processDescriptions;
    }

    @Override
    public ExecuteResponse execute(Execute execute) {
        return null;
    }

    /**
     * Convert the OWS 2 LanguageStingType to version 1
     * @param languageStringType2 OWS 2 LanguageStingType
     * @return OWS 1 LanguageStingType
     */
    private static LanguageStringType convertLanguageStringType2to1(net.opengis.ows._2.LanguageStringType languageStringType2){
        LanguageStringType languageStringType1 = new LanguageStringType();
        languageStringType1.setValue(languageStringType2.getValue());
        languageStringType1.setLang(languageStringType2.getLang());
        return languageStringType1;
    }

    /**
     * Convert the OWS 2 LanguageStingType list to version 1 list
     * @param languageStringType2List OWS 2 LanguageStingType list
     * @return OWS 1 LanguageStingType list
     */
    private static List<LanguageStringType> convertLanguageStringTypeList2to1(
            List<net.opengis.ows._2.LanguageStringType> languageStringType2List){
        List<LanguageStringType> languageStringType1List = new ArrayList<>();
        for(net.opengis.ows._2.LanguageStringType languageStringType : languageStringType2List) {
            languageStringType1List.add(convertLanguageStringType2to1(languageStringType));
        }
        return languageStringType1List;
    }

    /**
     * Convert the OWS 2 CodeType to version 1
     * @param codeType2 OWS 2 CodeType
     * @return OWS 1 CodeType
     */
    private static CodeType convertCodeType2to1(net.opengis.ows._2.CodeType codeType2){
        CodeType codeType1 = new CodeType();
        codeType1.setValue(codeType2.getValue());
        codeType1.setCodeSpace(codeType2.getCodeSpace());
        return codeType1;
    }

    /**
     * Convert the OWS 2 MetadataType to version 1
     * @param metadataType2 OWS 2 MetadataType
     * @return OWS 1 MetadataType
     */
    private static MetadataType convertMetadataType2to1(net.opengis.ows._2.MetadataType metadataType2){
        MetadataType metadataType1 = new MetadataType();
        metadataType1.setAbout(metadataType2.getAbout());
        metadataType1.setArcrole(metadataType2.getArcrole());
        metadataType1.setActuate(metadataType2.getActuate());
        metadataType1.setAbstractMetaData(metadataType2.getAbstractMetaData());
        metadataType1.setHref(metadataType2.getHref());
        metadataType1.setRole(metadataType2.getRole());
        metadataType1.setShow(metadataType2.getShow());
        metadataType1.setTitle(metadataType2.getTitle());
        return metadataType1;
    }

    /**
     * Convert the OWS 2 MetadataType list to version 1 list
     * @param metadataType2List OWS 2 MetadataType list
     * @return OWS 1 MetadataType list
     */
    private static List<MetadataType> convertMetadataTypeList2to1(
            List<net.opengis.ows._2.MetadataType> metadataType2List){
        List<MetadataType> metadataType1List = new ArrayList<>();
        for(net.opengis.ows._2.MetadataType metadataType : metadataType2List) {
            metadataType1List.add(convertMetadataType2to1(metadataType));
        }
        return metadataType1List;
    }

    /**
     * Convert the WPS 2.0 ProcessDescriptionType to version 1.0.0
     * @param processDescriptionType2 WPS 2.0 ProcessDescriptionType
     * @return WPS 1.0.0 ProcessDescriptionType
     */
    private static ProcessDescriptionType convertProcessDescriptionType2to1(
            net.opengis.wps._2_0.ProcessDescriptionType processDescriptionType2){
        ProcessDescriptionType processDescriptionType1 = new ProcessDescriptionType();
        processDescriptionType1.setAbstract(convertLanguageStringType2to1(processDescriptionType2.getAbstract().get(0)));
        processDescriptionType1.setIdentifier(convertCodeType2to1(processDescriptionType2.getIdentifier()));
        processDescriptionType1.setTitle(convertLanguageStringType2to1(processDescriptionType2.getTitle().get(0)));
        processDescriptionType1.getMetadata().addAll(convertMetadataTypeList2to1(processDescriptionType2.getMetadata()));
        return processDescriptionType1;
    }

    /**
     * Convert the WPS 2 InputDescriptionType to version 1
     * @param inputDescriptionType2 WPS 2 InputDescriptionType
     * @return WPS 1 InputDescriptionType
     */
    private static InputDescriptionType convertInputDescriptionType2to1(
            net.opengis.wps._2_0.InputDescriptionType inputDescriptionType2,
            String defaultLanguage, String requestedLanguage){
        InputDescriptionType inputDescriptionType1 = new InputDescriptionType();
        if(inputDescriptionType2.getDataDescription().getValue() instanceof LiteralDataType){
            LiteralDataType literalDataType = (LiteralDataType) inputDescriptionType2.getDataDescription().getValue();
            inputDescriptionType1.setLiteralData(convertLiteralDataTypeToLiteralInputType(literalDataType));
        }
        else if(inputDescriptionType2.getDataDescription().getValue() instanceof BoundingBoxData){
            BoundingBoxData bBox = (BoundingBoxData)inputDescriptionType2.getDataDescription().getValue();
            inputDescriptionType1.setBoundingBoxData(convertBoundingBoxDataToSupportedCRSsType(bBox));
        }
        else if(inputDescriptionType2.getDataDescription().getValue() instanceof ComplexDataType){
            ComplexDataType complexData = (ComplexDataType) inputDescriptionType2.getDataDescription().getValue();
            inputDescriptionType1.setComplexData(convertBoundingBoxDataToSupportedCRSsType(complexData));
        }
        inputDescriptionType1.setMaxOccurs(BigInteger.valueOf(Long.decode(inputDescriptionType2.getMaxOccurs())));
        inputDescriptionType1.setMinOccurs(inputDescriptionType2.getMinOccurs());
        inputDescriptionType1.setIdentifier(convertCodeType2to1(inputDescriptionType2.getIdentifier()));
        boolean isLangSet = false;
        for(net.opengis.ows._2.LanguageStringType languageStringType : inputDescriptionType2.getAbstract()){
            if(languageStringType.getLang().equals(requestedLanguage)){
                isLangSet = true;
                inputDescriptionType1.setAbstract(convertLanguageStringType2to1(languageStringType));
            }
            else if(!isLangSet && languageStringType.getLang().equals(defaultLanguage)){
                inputDescriptionType1.setAbstract(convertLanguageStringType2to1(languageStringType));
            }
        }
        if(!isLangSet){
            inputDescriptionType1.setAbstract(convertLanguageStringType2to1(inputDescriptionType2.getAbstract().get(0)));
        }
        isLangSet = false;
        for(net.opengis.ows._2.LanguageStringType languageStringType : inputDescriptionType2.getTitle()){
            if(languageStringType.getLang().equals(requestedLanguage)){
                isLangSet = true;
                inputDescriptionType1.setTitle(convertLanguageStringType2to1(languageStringType));
            }
            else if(!isLangSet && languageStringType.getLang().equals(defaultLanguage)){
                inputDescriptionType1.setTitle(convertLanguageStringType2to1(languageStringType));
            }
        }
        if(!isLangSet){
            inputDescriptionType1.setTitle(convertLanguageStringType2to1(inputDescriptionType2.getTitle().get(0)));
        }
        inputDescriptionType1.setTitle(convertLanguageStringType2to1(inputDescriptionType2.getTitle().get(0)));
        inputDescriptionType1.getMetadata().addAll(convertMetadataTypeList2to1(inputDescriptionType2.getMetadata()));
        return inputDescriptionType1;
    }

    /**
     * Convert the WPS 2.0 InputDescriptionType list to version 1.0.0 list
     * @param inputDescriptionType2List WPS 2.0 InputDescriptionType list
     * @return WPS 1.0.0 InputDescriptionType list
     */
    private static List<InputDescriptionType> convertInputDescriptionTypeList2to1(
            List<net.opengis.wps._2_0.InputDescriptionType> inputDescriptionType2List,
            String defaultLanguage, String requestedLanguage){
        List<InputDescriptionType> inputDescriptionType1List = new ArrayList<>();
        for(net.opengis.wps._2_0.InputDescriptionType inputDescriptionType : inputDescriptionType2List) {
            inputDescriptionType1List.add(convertInputDescriptionType2to1(inputDescriptionType, defaultLanguage, requestedLanguage));
        }
        return inputDescriptionType1List;
    }

    /**
     * Convert the WPS 2.0 LiteralDataType to version 1.0.0
     * @param literalDataType WPS 2.0 LiteralDataType
     * @return WPS 1.0.0 LiteralInputType
     */
    private static LiteralInputType convertLiteralDataTypeToLiteralInputType(LiteralDataType literalDataType){
        LiteralInputType literalInputType = new LiteralInputType();
        LiteralDataType.LiteralDataDomain domain = literalDataType.getLiteralDataDomain().get(0);
        if(domain.getAllowedValues() != null) {
            literalInputType.setAllowedValues(convertAllowedValues2to1(domain.getAllowedValues()));
        }
        if(domain.getAnyValue() != null) {
            literalInputType.setAnyValue(convertAnyValue2to1(domain.getAnyValue()));
        }
        if(domain.getDefaultValue() != null && domain.getDefaultValue().getValue() != null) {
            literalInputType.setDefaultValue(domain.getDefaultValue().getValue());
        }
        if(domain.getValuesReference() != null) {
            literalInputType.setValuesReference(convertValuesReference2to1(domain.getValuesReference()));
        }
        if(domain.getDataType() != null) {
            literalInputType.setDataType(convertDomainMetadataType2to1(domain.getDataType()));
        }
        if(domain.getUOM() != null) {
            literalInputType.setUOMs(convertUOM2to1(domain.getUOM()));
        }
        return literalInputType;
    }

    /**
     * Convert the OWS 2 AllowedValues to version 1
     * @param allowedValues2 OWS 2 AllowedValues
     * @return OWS 1 AllowedValues
     */
    private static AllowedValues convertAllowedValues2to1(net.opengis.ows._2.AllowedValues allowedValues2){
        AllowedValues allowedValues1 = new AllowedValues();
        allowedValues1.getValueOrRange().addAll(allowedValues2.getValueOrRange());
        return allowedValues1;
    }

    /**
     * Convert the OWS 2 AnyValue to version 1
     * @param anyValue2 OWS 2 AnyValue
     * @return OWS 1 AnyValue
     */
    private static AnyValue convertAnyValue2to1(net.opengis.ows._2.AnyValue anyValue2){
        return new AnyValue();
    }

    /**
     * Convert the OWS 2 ValuesReference to version 1
     * @param valuesReference2 OWS 2 ValuesReference
     * @return OWS 1 ValuesReference
     */
    private static ValuesReferenceType convertValuesReference2to1(net.opengis.ows._2.ValuesReference valuesReference2){
        ValuesReferenceType valuesReferenceType = new ValuesReferenceType();
        valuesReferenceType.setReference(valuesReference2.getReference());
        valuesReferenceType.setValuesForm(valuesReference2.getValue());
        return valuesReferenceType;
    }

    /**
     * Convert the OWS 2 DomainMetadataType to version 1
     * @param domainMetadataType2 OWS 2 DomainMetadataType
     * @return OWS 1 DomainMetadataType
     */
    private static DomainMetadataType convertDomainMetadataType2to1(net.opengis.ows._2.DomainMetadataType domainMetadataType2){
        DomainMetadataType domainMetadataType1 = new DomainMetadataType();
        domainMetadataType1.setReference(domainMetadataType2.getReference());
        domainMetadataType1.setValue(domainMetadataType2.getValue());
        return domainMetadataType1;
    }

    /**
     * Convert the OWS 2 DomainMetadataType to version 1
     * @param domainMetadataType OWS 2 DomainMetadataType
     * @return OWS 1 SupportedUOMsType
     */
    private static SupportedUOMsType convertUOM2to1(net.opengis.ows._2.DomainMetadataType domainMetadataType){
        SupportedUOMsType supportedUOMsType = new SupportedUOMsType();
        SupportedUOMsType.Default dflt = new SupportedUOMsType.Default();
        dflt.setUOM(convertDomainMetadataType2to1(domainMetadataType));
        supportedUOMsType.setDefault(dflt);
        UOMsType uoMsType = new UOMsType();
        uoMsType.getUOM().add(convertDomainMetadataType2to1(domainMetadataType));
        supportedUOMsType.setSupported(uoMsType);
        return supportedUOMsType;
    }

    /**
     * Convert the WPS 2.0 BoundingBoxData to version 1.0.0 SupportedCRSsType
     * @param boundingBoxData WPS 2.0 BoundingBoxData
     * @return WPS 1.0.0 SupportedCRSsType
     */
    private static SupportedCRSsType convertBoundingBoxDataToSupportedCRSsType(BoundingBoxData boundingBoxData){
        SupportedCRSsType supportedCRSsType = new SupportedCRSsType();
        CRSsType crSsType = new CRSsType();
        for(SupportedCRS supportedCRS : boundingBoxData.getSupportedCRS()){
            if(supportedCRS.isDefault()){
                SupportedCRSsType.Default dflt = new SupportedCRSsType.Default();
                dflt.setCRS(supportedCRS.getValue());
                supportedCRSsType.setDefault(dflt);
            }
            else{
                crSsType.getCRS().add(supportedCRS.getValue());
            }
        }
        supportedCRSsType.setSupported(crSsType);
        return supportedCRSsType;
    }

    /**
     * Convert the WPS 2.0 ComplexDataType to version 1.0.0 SupportedComplexDataInputType
     * @param complexData WPS 2.0 ComplexDataType
     * @return WPS 1.0.0 SupportedComplexDataInputType
     */
    private static SupportedComplexDataInputType convertBoundingBoxDataToSupportedCRSsType(ComplexDataType complexData){
        SupportedComplexDataInputType complexDataInput = new SupportedComplexDataInputType();
        complexDataInput.setMaximumMegabytes(new BigInteger(wpsProp.CUSTOM_PROPERTIES.MAXIMUM_MEGABYTES));
        ComplexDataCombinationsType combinations = new ComplexDataCombinationsType();
        for(Format format : complexData.getFormat()) {
            if(format.isDefault()) {
                ComplexDataCombinationType combination = new ComplexDataCombinationType();
                ComplexDataDescriptionType descriptionType = new ComplexDataDescriptionType();
                descriptionType.setEncoding(format.getEncoding());
                descriptionType.setMimeType(format.getMimeType());
                descriptionType.setSchema(format.getSchema());
                combination.setFormat(descriptionType);
                complexDataInput.setDefault(combination);
            }
            else {
                ComplexDataDescriptionType descriptionType = new ComplexDataDescriptionType();
                descriptionType.setEncoding(format.getEncoding());
                descriptionType.setMimeType(format.getMimeType());
                descriptionType.setSchema(format.getSchema());
                combinations.getFormat().add(descriptionType);
            }
        }
        complexDataInput.setSupported(combinations);
        return complexDataInput;
    }

    /**
     * Convert the WPS 2.0 OutputDescriptionType list to version 1.0.0 ProcessOutputs
     * @param outputDescriptionTypeList WPS 2.0 OutputDescriptionType list
     * @return WPS 1.0.0 ProcessOutputs
     */
    private static ProcessDescriptionType.ProcessOutputs convertOutputDescriptionTypeList2to1(
            List<net.opengis.wps._2_0.OutputDescriptionType> outputDescriptionTypeList,
            String defaultLanguage, String requestedLanguage){
        ProcessDescriptionType.ProcessOutputs processOutputs = new ProcessDescriptionType.ProcessOutputs();
        for(net.opengis.wps._2_0.OutputDescriptionType outputDescriptionType : outputDescriptionTypeList) {
            OutputDescriptionType descriptionType = new OutputDescriptionType();
            if(outputDescriptionType.getDataDescription().getValue() instanceof LiteralDataType){
                LiteralDataType literalDataType = (LiteralDataType) outputDescriptionType.getDataDescription().getValue();
                descriptionType.setLiteralOutput(convertLiteralDataTypeToLiteralInputType(literalDataType));
            }
            else if(outputDescriptionType.getDataDescription().getValue() instanceof BoundingBoxData){
                BoundingBoxData bBox = (BoundingBoxData)outputDescriptionType.getDataDescription().getValue();
                descriptionType.setBoundingBoxOutput(convertBoundingBoxDataToSupportedCRSsType(bBox));
            }
            else if(outputDescriptionType.getDataDescription().getValue() instanceof ComplexDataType){
                ComplexDataType complexData = (ComplexDataType) outputDescriptionType.getDataDescription().getValue();
                descriptionType.setComplexOutput(convertBoundingBoxDataToSupportedCRSsType(complexData));
            }
            descriptionType.setIdentifier(convertCodeType2to1(outputDescriptionType.getIdentifier()));
            boolean isLangSet = false;
            for(net.opengis.ows._2.LanguageStringType languageStringType : outputDescriptionType.getAbstract()){
                if(languageStringType.getLang().equals(requestedLanguage)){
                    isLangSet = true;
                    descriptionType.setAbstract(convertLanguageStringType2to1(languageStringType));
                }
                else if(!isLangSet && languageStringType.getLang().equals(defaultLanguage)){
                    descriptionType.setAbstract(convertLanguageStringType2to1(languageStringType));
                }
            }
            if(!isLangSet){
                descriptionType.setAbstract(convertLanguageStringType2to1(outputDescriptionType.getAbstract().get(0)));
            }
            isLangSet = false;
            for(net.opengis.ows._2.LanguageStringType languageStringType : outputDescriptionType.getTitle()){
                if(languageStringType.getLang().equals(requestedLanguage)){
                    isLangSet = true;
                    descriptionType.setTitle(convertLanguageStringType2to1(languageStringType));
                }
                else if(!isLangSet && languageStringType.getLang().equals(defaultLanguage)){
                    descriptionType.setTitle(convertLanguageStringType2to1(languageStringType));
                }
            }
            if(!isLangSet){
                descriptionType.setTitle(convertLanguageStringType2to1(outputDescriptionType.getTitle().get(0)));
            }
            descriptionType.setTitle(convertLanguageStringType2to1(outputDescriptionType.getTitle().get(0)));
            processOutputs.getOutput().add(descriptionType);
        }
        return processOutputs;
    }
}