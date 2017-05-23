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
import org.orbiswps.server.controller.process.ProcessIdentifier;
import org.orbiswps.server.controller.utils.Job;
import org.orbiswps.server.utils.ProcessTranslator;
import org.orbiswps.server.utils.WpsServerProperties_1_0_0;

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
    private WpsServerProperties_1_0_0 wpsProp;

    /** Main constructor */
    public WPS_1_0_0_OperationsImpl(WpsServerImpl wpsServer, WpsServerProperties_1_0_0 wpsProp){
        this.wpsServer = wpsServer;
        this.wpsProp = wpsProp;
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
                    languageFound = true;
                    break;
                }
            }
            //If not language was found, try to get one with best-effort semantic
            if(!languageFound){
                //avoid to test "*" language
                if(!requestLanguage.equals("*")) {
                    String baseLanguage = requestLanguage.substring(0, 2);
                    for (String serverLanguage : wpsProp.GLOBAL_PROPERTIES.SUPPORTED_LANGUAGES) {
                        if (serverLanguage.substring(0, 2).equals(baseLanguage)) {
                            languageFound = true;
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
        return null;
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
}
