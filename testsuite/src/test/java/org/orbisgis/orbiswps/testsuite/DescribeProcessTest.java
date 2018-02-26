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
 * Copyright (C) 2015-2018 CNRS (Lab-STICC UMR CNRS 6285)
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
package org.orbisgis.orbiswps.testsuite;

import net.opengis.ows._2.*;
import net.opengis.wps._2_0.*;
import net.opengis.wps._2_0.GetCapabilitiesType;
import net.opengis.wps._2_0.ObjectFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.orbisgis.orbiswps.service.model.JaxbContainer;
import org.orbisgis.orbiswps.serviceapi.WpsServer;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;
import static org.orbisgis.orbiswps.testsuite.WpsServiceFactory.getException;
import static org.orbisgis.orbiswps.testsuite.WpsServiceFactory.sendRequest;

/**
 * Test all the cases of a DescribeProcess request.
 *
 * @author Sylvain PALOMINOS
 */
public class DescribeProcessTest {
    private static WpsServer service;
    private static Unmarshaller unmarshaller;
    private static Marshaller marshaller;
    private static ObjectFactory factory;

    /** Test configuration properties **/
    private static Properties props;

    private static List<CodeType> codeTypeList = new ArrayList<>();
    private static Map<String, String> dataTypes = new HashMap<>();

    @BeforeClass
    public static void init() throws SQLException, JAXBException, IOException {
        service = WpsServiceFactory.getService();
        unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
        marshaller = JaxbContainer.JAXBCONTEXT.createMarshaller();
        factory = new ObjectFactory();
        props = new Properties();
        URL url = GetCapabilitiesTest.class.getResource("fullWpsService.properties");
        props.load(new InputStreamReader(url.openStream()));

        GetCapabilitiesType getCapabilities = new GetCapabilitiesType();
        Object result = sendRequest(factory.createGetCapabilities(getCapabilities), marshaller, service, unmarshaller);
        JAXBElement element = (JAXBElement) result;
        WPSCapabilitiesType capabilities = (WPSCapabilitiesType) element.getValue();
        for(ProcessSummaryType processSummaryType : capabilities.getContents().getProcessSummary()){
            codeTypeList.add(processSummaryType.getIdentifier());
        }
        dataTypes.put("string", "http://www.w3.org/2001/XMLSchema#string");
        dataTypes.put("integer", "http://www.w3.org/2001/XMLSchema#integer");
        dataTypes.put("decimal", "http://www.w3.org/2001/XMLSchema#decimal");
        dataTypes.put("boolean", "http://www.w3.org/2001/XMLSchema#boolean");
        dataTypes.put("double", "http://www.w3.org/2001/XMLSchema#double");
        dataTypes.put("float", "http://www.w3.org/2001/XMLSchema#float");
    }

    @Test
    public void testDescribeProcessBadLang(){
        DescribeProcess describeProcess = new DescribeProcess();
        describeProcess.setLang("un-ic-or-n");
        describeProcess.getIdentifier().addAll(codeTypeList);

        Object result = sendRequest(describeProcess, marshaller, service, unmarshaller);

        //Get the ExceptionReport object
        if (!(result instanceof ExceptionReport)) {
            fail("The result object should be a ExceptionReport");
        }
        ExceptionReport exceptionReport = (ExceptionReport)result;
        if(exceptionReport.isSetException()){
            for(ExceptionType exceptionType : exceptionReport.getException()){
                if(!exceptionType.isSetExceptionCode()){
                    assertFalse("The ExceptionType should contains an exception code",
                            exceptionType.isSetExceptionCode());
                }
                else if(!exceptionType.isSetLocator()){
                    assertFalse("The ExceptionType should contains a locator", exceptionType.isSetLocator());
                }
                else{
                    assertEquals("The exception code should be InvalidParameterValue",
                            "InvalidParameterValue", exceptionType.getExceptionCode());
                    assertEquals("The exception locator should be Lang",
                            "Lang", exceptionType.getLocator());
                }
            }
        }
        else{
            fail("The ExceptionReport should contains exceptions");
        }
    }

    @Test
    public void testEmptyDescribeProcess(){
        DescribeProcess describeProcess = new DescribeProcess();
        describeProcess.setLang("en");

        Object result = sendRequest(describeProcess, marshaller, service, unmarshaller);

        //Get the ExceptionReport object
        if (!(result instanceof ExceptionReport)) {
            fail("The result object should be a ExceptionReport");
        }
        ExceptionReport exceptionReport = (ExceptionReport)result;
        if(exceptionReport.isSetException()){
            for(ExceptionType exceptionType : exceptionReport.getException()){
                if(!exceptionType.isSetExceptionCode()){
                    assertFalse("The ExceptionType should contains an exception code",
                            exceptionType.isSetExceptionCode());
                }
                else if(!exceptionType.isSetLocator()){
                    assertFalse("The ExceptionType should contains a locator", exceptionType.isSetLocator());
                }
                else{
                    assertEquals("The exception code should be InvalidParameterValue",
                            "InvalidParameterValue", exceptionType.getExceptionCode());
                    assertEquals("The exception locator should be Identifier",
                            "Identifier", exceptionType.getLocator());
                }
            }
        }
        else{
            fail("The ExceptionReport should contains exceptions");
        }
    }

    @Test
    public void testDescribeProcessResult() throws Exception {
        DescribeProcess describeProcess = new DescribeProcess();
        describeProcess.setLang("fr-fr");
        describeProcess.getIdentifier().addAll(codeTypeList);

        Object result = sendRequest(describeProcess, marshaller, service, unmarshaller);


        //Get the WPSCapabilitiesType object

        if(result instanceof ExceptionReport){
            throw getException((ExceptionReport)result);
        }
        else if (!(result instanceof ProcessOfferings)) {
            fail("The JAXBElement value should be a WPSCapabilitiesType");
        }
        ProcessOfferings processOfferings = (ProcessOfferings) result;

        assertTrue("The property 'processOffering' should be set", processOfferings.isSetProcessOffering());
        for(ProcessOffering processOffering : processOfferings.getProcessOffering()){
            assertTrue("The property 'jobControlOption' should be set",
                    processOffering.isSetJobControlOptions());
            List<String> controlList = Arrays.asList(props.getProperty("JOB_CONTROL_OPTIONS").split(","));
            assertTrue("The 'jobControlOption' contains invalid value(s)",
                    controlList.containsAll(processOffering.getJobControlOptions()));

            assertTrue("The property 'outputTransmission' should be set",
                    processOffering.isSetOutputTransmission());
            List<DataTransmissionModeType> transmissionList = new ArrayList<>();
            transmissionList.add(DataTransmissionModeType.REFERENCE);
            transmissionList.add(DataTransmissionModeType.VALUE);
            assertTrue("The 'outputTransmission' contains invalid value(s)",
                    transmissionList.containsAll(processOffering.getOutputTransmission()));

            if(processOffering.isSetProcessVersion()){
                String[] version = processOffering.getProcessVersion().split("\\.");
                assertEquals("The version should be composed of three values", 3, version.length);
                for(String val : version){
                    int i=0;
                    try{
                        i = Integer.parseInt(val);
                    }
                    catch(NumberFormatException | NullPointerException ignored){
                        fail("The value "+val+" of the version should be a number");
                    }
                    assertTrue("The value "+val+" should be under or equal 99", i<=99);
                }

            }

            //Process model is not supported
            //TODO support ProcessModel
            //if(processOffering.isSetProcessModel()){}
            //if(processOffering.isSetAny()){}

            assertTrue("The property 'process' should be set", processOffering.isSetProcess());

            ProcessDescriptionType process = processOffering.getProcess();

            assertTrue("The property 'title' should be set", process.isSetTitle());
            assertEquals("There should be 1 title", 1, process.getTitle().size());
            assertEquals("The language should be fr-fr", "fr-fr", process.getTitle().get(0).getLang());

            if(process.isSetAbstract()){
                assertEquals("There should be 1 abstract", 1, process.getAbstract().size());
                assertEquals("The language should be fr-fr", "fr-fr", process.getAbstract().get(0).getLang());
            }

            if(process.isSetKeywords()){
                for(KeywordsType keyword : process.getKeywords()) {
                    assertEquals("There should be 1 keyword", 1, keyword.getKeyword().size());
                    assertEquals("The language should be fr-fr", "fr-fr", keyword.getKeyword().get(0).getLang());
                }
            }

            assertTrue("The property 'identifier' should be set", process.isSetIdentifier());

            //Nothing need to be test for the metadata

            if(process.isSetInput()){
                for(InputDescriptionType input : process.getInput()){
                    assertTrue("The property 'title' of the input should be set", input.isSetTitle());
                    assertEquals("There should be 1 title of the input", 1, input.getTitle().size());
                    assertEquals("The language of the input should be fr-fr", "fr-fr", input.getTitle().get(0).getLang());

                    if(input.isSetAbstract()){
                        assertEquals("There should be 1 abstract of the input", 1, input.getAbstract().size());
                        assertEquals("The language of the input should be fr-fr", "fr-fr", input.getAbstract().get(0).getLang());
                    }

                    if(input.isSetKeywords()){
                        for(KeywordsType keyword : input.getKeywords()) {
                            assertEquals("There should be 1 keyword of the input", 1, keyword.getKeyword().size());
                            assertEquals("The language of the input should be fr-fr", "fr-fr", keyword.getKeyword().get(0).getLang());
                        }
                    }

                    assertTrue("The property 'identifier' of the input should be set", input.isSetIdentifier());

                    assertTrue("The property 'dataDescription' of the input should be set.", input.isSetDataDescription());

                    if(input.isSetMaxOccurs()) {
                        assertTrue("The property 'maxOccurs' should be a non-negative integer",
                                Integer.parseInt(input.getMaxOccurs())>=0);
                    }

                    if(input.isSetMinOccurs()) {
                        assertTrue("The property 'minOccurs' should be a non-negative integer",
                                input.getMinOccurs().intValue()>=0);
                    }

                    testDataDescriptionType(input.getDataDescription().getValue());
                }
            }

            assertTrue("The property 'output' should be set", process.isSetOutput());
            assertTrue("The property 'output' should contains at least one output", process.getOutput().size()>0);
            for(OutputDescriptionType output : process.getOutput()){

                assertTrue("The property 'title' of the input should be set", output.isSetTitle());
                assertEquals("There should be 1 title of the input", 1, output.getTitle().size());
                assertEquals("The language of the input should be fr-fr", "fr-fr", output.getTitle().get(0).getLang());

                if(output.isSetAbstract()){
                    assertEquals("There should be 1 abstract of the input", 1, output.getAbstract().size());
                    assertEquals("The language of the input should be fr-fr", "fr-fr", output.getAbstract().get(0).getLang());
                }

                if(output.isSetKeywords()){
                    for(KeywordsType keyword : output.getKeywords()) {
                        assertEquals("There should be 1 keyword of the input", 1, keyword.getKeyword().size());
                        assertEquals("The language of the input should be fr-fr", "fr-fr", keyword.getKeyword().get(0).getLang());
                    }
                }

                assertTrue("The property 'identifier' of the input should be set", output.isSetIdentifier());

                assertTrue("The property 'dataDescription' of the input should be set.", output.isSetDataDescription());

                testDataDescriptionType(output.getDataDescription().getValue());
            }
        }

    }

    private void testDataDescriptionType(DataDescriptionType dataDescriptionType){

        //Test format
        assertTrue("The property 'format' should be set.", dataDescriptionType.isSetFormat());
        boolean isDefaultFormat = false;
        for(Format format : dataDescriptionType.getFormat()){
            assertTrue("The format property 'mimeType' should be set", format.isSetMimeType());
            assertEquals("The format property 'encoding' should be 'simple'", "simple", format.getEncoding());
            assertTrue("The format property 'schema' should be 'simple'", format.isSetSchema());
            if(format.isSetDefault() && format.isDefault()){
                isDefaultFormat = true;
            }
        }
        assertTrue("The LiteralData should contain a default format", isDefaultFormat);

        if(dataDescriptionType instanceof LiteralDataType){
            LiteralDataType literal = (LiteralDataType)dataDescriptionType;
            //Test LiteralDataDomain
            assertTrue("The property 'literalDataDomain' should be set.", literal.isSetLiteralDataDomain());
            boolean isDefaultDomain = false;
            for(LiteralDataType.LiteralDataDomain ldd : literal.getLiteralDataDomain()){
                assertTrue("The literalDataDomain should have exactly one of those properties set :" +
                                "'AnyValue', 'AllowedValues' or 'ValuesReference'",
                        ldd.isSetAnyValue()^ldd.isSetAllowedValues()^ldd.isSetValuesReference());
                if(ldd.isSetValuesReference()){
                    assertTrue("The property 'reference' or 'value' should be set",
                            ldd.getValuesReference().isSetReference() || ldd.getValuesReference().isSetValue());
                }
                if(ldd.isSetAllowedValues()){
                    assertTrue("The property 'allowedValues' should contains at least one value/range",
                            ldd.getAllowedValues().isSetValueOrRange());
                    for(Object obj : ldd.getAllowedValues().getValueOrRange()){
                        if(obj instanceof ValueType){
                            ValueType value = (ValueType)(obj);
                            assertTrue("The property 'value' should be set", value.isSetValue());
                        }
                        else if(obj instanceof RangeType){
                            RangeType range = (RangeType)(obj);
                            //Nothing to test ass all the fields are optional.
                        }
                    }
                }
                assertTrue("The property 'dataType' should be set", ldd.isSetDataType());
                assertEquals("The dataType value is not compatible with its reference.",
                        dataTypes.get(ldd.getDataType().getValue().toLowerCase()), ldd.getDataType().getReference());
                if(ldd.isSetUOM()){
                    assertTrue("The property 'UOM' should contains at least a 'value' or a 'reference'",
                            ldd.getUOM().isSetReference()||ldd.getUOM().isSetValue());
                }
                if(ldd.isSetDefaultValue()){
                    assertTrue("The property 'value' should be set if 'defaultValue' is set",
                            ldd.getDefaultValue().isSetValue());
                }
                if(ldd.isDefault()){
                    if(isDefaultDomain){
                        isDefaultDomain = false;
                        break;
                    }
                    isDefaultDomain = true;
                }
            }
            assertTrue("The literalData should contain a default dataDomain", isDefaultDomain);

        }
        else if(dataDescriptionType instanceof BoundingBoxData){
            BoundingBoxData bbox = (BoundingBoxData) dataDescriptionType;
            assertTrue("The property 'supportedCRS should be set", bbox.isSetSupportedCRS());
            boolean isDefault = false;
            for(SupportedCRS supportedCRS : bbox.getSupportedCRS()){
                assertTrue("The property 'value' should be set", supportedCRS.isSetValue());
                if(supportedCRS.isDefault()){
                    if(isDefault){
                        isDefault = false;
                        break;
                    }
                    isDefault = true;
                }
            }
            assertTrue("Exactly one supported CRS should be declared as default one", isDefault);
        }
        else if(dataDescriptionType instanceof ComplexDataType){
            ComplexDataType complexDataType = (ComplexDataType) dataDescriptionType;
            if(complexDataType.isSetAny()){
                //Test OrbisGIS WPS flavour any objects
                for(Object obj : complexDataType.getAny()){
                    //TODO finish the tests.
                }
            }
        }
        else{
            fail("The input should be a Literal or aBoundingBox or a ComplexData");
        }
    }
}
