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
import net.opengis.wps._1_0_0.InputDescriptionType;
import net.opengis.wps._1_0_0.OutputDescriptionType;
import net.opengis.wps._1_0_0.ProcessDescriptionType;
import net.opengis.wps._2_0.BoundingBoxData;
import net.opengis.wps._2_0.ComplexDataType;
import net.opengis.wps._2_0.*;
import net.opengis.wps._2_0.GetCapabilitiesType;
import org.orbisgis.orbiswps.service.model.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sylvain PALOMINOS
 */
public class Converter {

    /**
     * Convert the OWS 2 LanguageStingType to version 1
     * @param languageStringType2 OWS 2 LanguageStingType
     * @return OWS 1 LanguageStingType
     */
    public static LanguageStringType convertLanguageStringType2to1(net.opengis.ows._2.LanguageStringType languageStringType2){
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
    public static List<LanguageStringType> convertLanguageStringTypeList2to1(
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
    public static CodeType convertCodeType2to1(net.opengis.ows._2.CodeType codeType2){
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
    public static MetadataType convertMetadataType2to1(net.opengis.ows._2.MetadataType metadataType2){
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
    public static List<MetadataType> convertMetadataTypeList2to1(
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
    public static ProcessDescriptionType convertProcessDescriptionType2to1(
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
    public static InputDescriptionType convertInputDescriptionType2to1(
            net.opengis.wps._2_0.InputDescriptionType inputDescriptionType2,
            String defaultLanguage, String requestedLanguage, BigInteger maxMb){
        InputDescriptionType inputDescriptionType1 = new InputDescriptionType();
        DataDescriptionType dataDescriptionType = inputDescriptionType2.getDataDescription().getValue();
        if(dataDescriptionType instanceof net.opengis.wps._2_0.LiteralDataType){
            net.opengis.wps._2_0.LiteralDataType literalDataType =
                    (net.opengis.wps._2_0.LiteralDataType) dataDescriptionType;
            inputDescriptionType1.setLiteralData(convertLiteralDataTypeToLiteralInputType(literalDataType));
        }
        else if(dataDescriptionType instanceof org.orbisgis.orbiswps.service.model.BoundingBoxData){
            org.orbisgis.orbiswps.service.model.BoundingBoxData bBox =
                    (org.orbisgis.orbiswps.service.model.BoundingBoxData)dataDescriptionType;
            inputDescriptionType1.setBoundingBoxData(convertComplexDataTypeToSupportedCrssType(bBox));
        }
        else if(dataDescriptionType instanceof ComplexDataType && !(dataDescriptionType instanceof JDBCTable)){
            ComplexDataType complexData = (ComplexDataType) dataDescriptionType;
            inputDescriptionType1.setLiteralData(convertComplexDataTypeToLiteralData(complexData));
        }
        else {
            ComplexDataType complexData = (ComplexDataType) dataDescriptionType;
            inputDescriptionType1.setComplexData(convertComplexDataTypeToSupportedComplexDataInputType(complexData, maxMb));
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
    public static List<InputDescriptionType> convertInputDescriptionTypeList2to1(
            List<net.opengis.wps._2_0.InputDescriptionType> inputDescriptionType2List,
            String defaultLanguage, String requestedLanguage, BigInteger maxMb){
        List<InputDescriptionType> inputDescriptionType1List = new ArrayList<>();
        for(net.opengis.wps._2_0.InputDescriptionType inputDescriptionType : inputDescriptionType2List) {
            inputDescriptionType1List.add(convertInputDescriptionType2to1(inputDescriptionType, defaultLanguage, requestedLanguage, maxMb));
        }
        return inputDescriptionType1List;
    }

    /**
     * Convert the WPS 2.0 LiteralDataType to version 1.0.0
     * @param literalDataType WPS 2.0 LiteralDataType
     * @return WPS 1.0.0 LiteralInputType
     */
    public static LiteralInputType convertLiteralDataTypeToLiteralInputType(
            net.opengis.wps._2_0.LiteralDataType literalDataType){
        LiteralInputType literalInputType = new LiteralInputType();
        net.opengis.wps._2_0.LiteralDataType.LiteralDataDomain domain =
                literalDataType.getLiteralDataDomain().get(0);
        //Particular case of boolean
        /*if(domain.getDataType().getValue().equalsIgnoreCase("boolean")) {
            AllowedValues allowedValues = new AllowedValues();
            ValueType valueTrue = new ValueType();
            valueTrue.setValue("true");
            ValueType valueFalse = new ValueType();
            valueFalse.setValue("false");
            allowedValues.getValueOrRange().add(valueTrue);
            allowedValues.getValueOrRange().add(valueFalse);
            literalInputType.setAllowedValues(allowedValues);
            DomainMetadataType domainMetadataType = new DomainMetadataType();
            domainMetadataType.setValue("string");
            domainMetadataType.setReference("https://www.w3.org/2001/XMLSchema#string");
            literalInputType.setDataType(domainMetadataType);
            return literalInputType;
        }
        //General case
        else {*/
            if (domain.getAllowedValues() != null) {
                literalInputType.setAllowedValues(convertAllowedValues2to1(domain.getAllowedValues()));
            }
            if (domain.getAnyValue() != null) {
                literalInputType.setAnyValue(convertAnyValue2to1(domain.getAnyValue()));
            }
            if (domain.getDefaultValue() != null && domain.getDefaultValue().getValue() != null) {
                literalInputType.setDefaultValue(domain.getDefaultValue().getValue());
            }
            if (domain.getValuesReference() != null) {
                literalInputType.setValuesReference(convertValuesReference2to1(domain.getValuesReference()));
            }
            if (domain.getDataType() != null) {
                literalInputType.setDataType(convertDomainMetadataType2to1(domain.getDataType()));
            }
            if (domain.getUOM() != null) {
                literalInputType.setUOMs(convertUOM2to1(domain.getUOM()));
            }
            return literalInputType;
        //}
    }

    /**
     * Convert the OWS 2 AllowedValues to version 1
     * @param allowedValues2 OWS 2 AllowedValues
     * @return OWS 1 AllowedValues
     */
    public static AllowedValues convertAllowedValues2to1(net.opengis.ows._2.AllowedValues allowedValues2){
        AllowedValues allowedValues1 = new AllowedValues();
        allowedValues1.getValueOrRange().addAll(allowedValues2.getValueOrRange());
        return allowedValues1;
    }

    /**
     * Convert the OWS 2 AnyValue to version 1
     * @param anyValue2 OWS 2 AnyValue
     * @return OWS 1 AnyValue
     */
    public static AnyValue convertAnyValue2to1(net.opengis.ows._2.AnyValue anyValue2){
        return new AnyValue();
    }

    /**
     * Convert the OWS 2 ValuesReference to version 1
     * @param valuesReference2 OWS 2 ValuesReference
     * @return OWS 1 ValuesReference
     */
    public static ValuesReferenceType convertValuesReference2to1(net.opengis.ows._2.ValuesReference valuesReference2){
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
    public static DomainMetadataType convertDomainMetadataType2to1(net.opengis.ows._2.DomainMetadataType domainMetadataType2){
        DomainMetadataType domainMetadataType1 = new DomainMetadataType();
        domainMetadataType1.setReference(domainMetadataType2.getReference().replace(
                "http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#",
                "https://www.w3.org/2001/XMLSchema#"));
        domainMetadataType1.setValue(domainMetadataType2.getValue().toLowerCase());
        return domainMetadataType1;
    }

    /**
     * Convert the OWS 2 DomainMetadataType to version 1
     * @param domainMetadataType OWS 2 DomainMetadataType
     * @return OWS 1 SupportedUOMsType
     */
    public static SupportedUOMsType convertUOM2to1(net.opengis.ows._2.DomainMetadataType domainMetadataType){
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
    public static SupportedCRSsType convertComplexDataTypeToSupportedCrssType(
            org.orbisgis.orbiswps.service.model.BoundingBoxData boundingBoxData){
        SupportedCRSsType supportedCRSsType = new SupportedCRSsType();
        CRSsType crSsType = new CRSsType();
        for(SupportedCRS supportedCRS : boundingBoxData.getSupportedCrs()){
            if(supportedCRS.isDefault()){
                SupportedCRSsType.Default dflt = new SupportedCRSsType.Default();
                dflt.setCRS(supportedCRS.getValue());
                supportedCRSsType.setDefault(dflt);
            }
            crSsType.getCRS().add(supportedCRS.getValue());
        }
        supportedCRSsType.setSupported(crSsType);
        return supportedCRSsType;
    }

    /**
     * Convert the WPS 2.0 ComplexDataType to version 1.0.0 SupportedComplexDataInputType
     * @param complexData WPS 2.0 ComplexDataType
     * @return WPS 1.0.0 SupportedComplexDataInputType
     */
    public static SupportedComplexDataInputType convertComplexDataTypeToSupportedComplexDataInputType(ComplexDataType complexData, BigInteger maxMb){
        SupportedComplexDataInputType complexDataInput = new SupportedComplexDataInputType();
        complexDataInput.setMaximumMegabytes(maxMb);
        ComplexDataCombinationsType combinations = new ComplexDataCombinationsType();
        for(Format format : complexData.getFormat()) {
            if(format.isSetDefault() && format.isDefault()) {
                ComplexDataCombinationType combination = new ComplexDataCombinationType();
                ComplexDataDescriptionType descriptionType = new ComplexDataDescriptionType();
                descriptionType.setEncoding(format.getEncoding());
                descriptionType.setMimeType(format.getMimeType());
                descriptionType.setSchema(format.getSchema());
                combination.setFormat(descriptionType);
                complexDataInput.setDefault(combination);
                combinations.getFormat().add(descriptionType);
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
     * Convert the WPS 2.0 ComplexDataType to version 1.0.0 Object. The object can be a complex daa or a literal model.
     * @param complexData WPS 2.0 ComplexDataType
     * @return WPS 1.0.0 object, complex or literal model.
     */
    public static LiteralInputType convertComplexDataTypeToLiteralData(ComplexDataType complexData){
        if(complexData instanceof Enumeration){
            Enumeration enumeration = (Enumeration)complexData;
            LiteralInputType literalInputType = new LiteralInputType();
            AllowedValues allowedValues = new AllowedValues();
            for(String value : enumeration.getValues()) {
                ValueType valueType = new ValueType();
                valueType.setValue(value);
                allowedValues.getValueOrRange().add(valueType);
            }
            literalInputType.setAllowedValues(allowedValues);
            if(enumeration.getDefaultValues() != null && enumeration.getDefaultValues().length != 0) {
                literalInputType.setDefaultValue(enumeration.getDefaultValues()[0]);
            }
            DomainMetadataType domainMetadataType = new DomainMetadataType();
            domainMetadataType.setValue("string");
            domainMetadataType.setReference("https://www.w3.org/2001/XMLSchema#string");
            literalInputType.setDataType(domainMetadataType);
            if(enumeration.getDefaultValues() != null && enumeration.getDefaultValues().length>0) {
                literalInputType.setDefaultValue(enumeration.getDefaultValues()[0]);
            }
            return literalInputType;
        }
        else if(complexData instanceof GeometryData){
            GeometryData geometryData = (GeometryData)complexData;
            LiteralInputType literalInputType = new LiteralInputType();
            literalInputType.setAnyValue(new AnyValue());
            if(geometryData.getDefaultValue() != null) {
                literalInputType.setDefaultValue(geometryData.getDefaultValue());
            }
            DomainMetadataType domainMetadataType = new DomainMetadataType();
            domainMetadataType.setValue("string");
            domainMetadataType.setReference("https://www.w3.org/2001/XMLSchema#string");
            literalInputType.setDataType(domainMetadataType);
            return literalInputType;
        }
        else if(complexData instanceof JDBCColumn){
            JDBCColumn jdbcColumn = (JDBCColumn)complexData;
            LiteralInputType literalInputType = new LiteralInputType();
            literalInputType.setAnyValue(new AnyValue());
            if(jdbcColumn.getDefaultValues() != null && jdbcColumn.getDefaultValues().length != 0) {
                literalInputType.setDefaultValue(jdbcColumn.getDefaultValues()[0]);
            }
            DomainMetadataType domainMetadataType = new DomainMetadataType();
            domainMetadataType.setValue("string");
            domainMetadataType.setReference("https://www.w3.org/2001/XMLSchema#string");
            literalInputType.setDataType(domainMetadataType);
            return literalInputType;
        }
        else if(complexData instanceof JDBCValue){
            JDBCValue jdbcValue = (JDBCValue)complexData;
            LiteralInputType literalInputType = new LiteralInputType();
            literalInputType.setAnyValue(new AnyValue());
            if(jdbcValue.getDefaultValues() != null && jdbcValue.getDefaultValues().length != 0) {
                literalInputType.setDefaultValue(jdbcValue.getDefaultValues()[0]);
            }
            DomainMetadataType domainMetadataType = new DomainMetadataType();
            domainMetadataType.setValue("string");
            domainMetadataType.setReference("https://www.w3.org/2001/XMLSchema#string");
            literalInputType.setDataType(domainMetadataType);
            return literalInputType;
        }
        else if(complexData instanceof Password){
            LiteralInputType literalInputType = new LiteralInputType();
            literalInputType.setAnyValue(new AnyValue());
            DomainMetadataType domainMetadataType = new DomainMetadataType();
            domainMetadataType.setValue("string");
            domainMetadataType.setReference("https://www.w3.org/2001/XMLSchema#string");
            literalInputType.setDataType(domainMetadataType);
            return literalInputType;
        }
        else if(complexData instanceof RawData){
            RawData rawData = (RawData)complexData;
            LiteralInputType literalInputType = new LiteralInputType();
            literalInputType.setAnyValue(new AnyValue());
            if(rawData.getDefaultValues() != null && rawData.getDefaultValues().length != 0) {
                literalInputType.setDefaultValue(rawData.getDefaultValues()[0]);
            }
            DomainMetadataType domainMetadataType = new DomainMetadataType();
            domainMetadataType.setValue("string");
            domainMetadataType.setReference("https://www.w3.org/2001/XMLSchema#string");
            literalInputType.setDataType(domainMetadataType);
            return literalInputType;
        }
        return null;
    }

    /**
     * Convert the WPS 2.0 OutputDescriptionType list to version 1.0.0 ProcessOutputs
     * @param outputDescriptionTypeList WPS 2.0 OutputDescriptionType list
     * @return WPS 1.0.0 ProcessOutputs
     */
    public static ProcessDescriptionType.ProcessOutputs convertOutputDescriptionTypeList2to1(
            List<net.opengis.wps._2_0.OutputDescriptionType> outputDescriptionTypeList,
            String defaultLanguage, String requestedLanguage, BigInteger maxMb){
        ProcessDescriptionType.ProcessOutputs processOutputs = new ProcessDescriptionType.ProcessOutputs();
        for(net.opengis.wps._2_0.OutputDescriptionType outputDescriptionType : outputDescriptionTypeList) {
            OutputDescriptionType descriptionType = new OutputDescriptionType();
            DataDescriptionType dataDescriptionType = outputDescriptionType.getDataDescription().getValue();
            if(dataDescriptionType instanceof net.opengis.wps._2_0.LiteralDataType){
                net.opengis.wps._2_0.LiteralDataType literalDataType =
                        (net.opengis.wps._2_0.LiteralDataType) outputDescriptionType.getDataDescription().getValue();
                descriptionType.setLiteralOutput(convertLiteralDataTypeToLiteralInputType(literalDataType));
            }
            else if(dataDescriptionType instanceof org.orbisgis.orbiswps.service.model.BoundingBoxData){
                org.orbisgis.orbiswps.service.model.BoundingBoxData bBox =
                        (org.orbisgis.orbiswps.service.model.BoundingBoxData)outputDescriptionType.getDataDescription().getValue();
                descriptionType.setBoundingBoxOutput(convertComplexDataTypeToSupportedCrssType(bBox));
            }
            else if(dataDescriptionType instanceof ComplexDataType && !(dataDescriptionType instanceof JDBCTable)){
                ComplexDataType complexData = (ComplexDataType) dataDescriptionType;
                descriptionType.setLiteralOutput(convertComplexDataTypeToLiteralData(complexData));
            }
            else {
                ComplexDataType complexData = (ComplexDataType) dataDescriptionType;
                descriptionType.setComplexOutput(convertComplexDataTypeToSupportedComplexDataInputType(complexData, maxMb));
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
            descriptionType.getMetadata().addAll(convertMetadataTypeList2to1(outputDescriptionType.getMetadata()));
            processOutputs.getOutput().add(descriptionType);
        }
        return processOutputs;
    }

    public static GetCapabilitiesType convertGetCapabilities1to2(GetCapabilities capabilities100) {
        GetCapabilitiesType capabilitiesType = new GetCapabilitiesType();
        if(capabilities100.isSetAcceptVersions()){
            net.opengis.ows._1.AcceptVersionsType version100 = capabilities100.getAcceptVersions();
            net.opengis.ows._2.AcceptVersionsType version20 = new net.opengis.ows._2.AcceptVersionsType();
            version20.getVersion().addAll(version100.getVersion());
            capabilitiesType.setAcceptVersions(version20);
        }
        if(capabilities100.isSetLanguage()){
            net.opengis.ows._2.GetCapabilitiesType.AcceptLanguages languages = new net.opengis.ows._2.GetCapabilitiesType.AcceptLanguages();
            languages.getLanguage().add(capabilities100.getLanguage());
            capabilitiesType.setAcceptLanguages(languages);
        }
        return capabilitiesType;
    }

    public static GetCapabilities convertGetCapabilities2to1(GetCapabilitiesType getCapabilities20, String defaultLang) {
        GetCapabilities getCapabilities = new GetCapabilities();
        if(getCapabilities20.isSetAcceptLanguages()) {
            if(getCapabilities20.getAcceptLanguages().getLanguage().contains(defaultLang)) {
                getCapabilities.setLanguage(defaultLang);
            }
            if(!getCapabilities.isSetLanguage()) {
                getCapabilities.setLanguage(getCapabilities20.getAcceptLanguages().getLanguage().get(0));
            }
        }
        if(getCapabilities20.isSetAcceptVersions()) {
            AcceptVersionsType acceptVersionsType100 = new AcceptVersionsType();
            acceptVersionsType100.getVersion().addAll(getCapabilities20.getAcceptVersions().getVersion());
            getCapabilities.setAcceptVersions(acceptVersionsType100);
        }
        return getCapabilities;
    }
}
