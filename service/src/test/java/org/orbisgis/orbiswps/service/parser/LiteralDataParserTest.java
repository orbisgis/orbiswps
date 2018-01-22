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
package org.orbisgis.orbiswps.service.parser;

import net.opengis.ows._2.ValueType;
import net.opengis.wps._2_0.DataDescriptionType;
import net.opengis.wps._2_0.InputDescriptionType;
import net.opengis.wps._2_0.LiteralDataType;
import net.opengis.wps._2_0.OutputDescriptionType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.orbiswps.groovyapi.attributes.DescriptionTypeAttribute;
import org.orbisgis.orbiswps.groovyapi.attributes.InputAttribute;
import org.orbisgis.orbiswps.groovyapi.attributes.LiteralDataAttribute;
import org.orbisgis.orbiswps.groovyapi.attributes.OutputAttribute;
import org.orbisgis.orbiswps.serviceapi.model.MalformedScriptException;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;

/**
 * Test class for the literalDataParser
 *
 * @author Sylvain PALOMINOS
 */
public class LiteralDataParserTest {

    /** LiteralData parser. */
    private LiteralDataParser literalDataParser;

    @Before
    public void initialization(){
        literalDataParser = new LiteralDataParser();
    }

    /**
     * Tests the annotation linked to this parser.
     */
    @Test
    public void testAnnotation(){
        Assert.assertEquals("The literalDataParser annotation class should be 'LiteralDataAttribute'.",
                LiteralDataAttribute.class, literalDataParser.getAnnotation());
    }

    /**
     * Tests the parsing of the simplest input.
     */
    @Test
    public void testSimplestParseInput(){
        Field field = null;
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("simplestInput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'simplestInput' from class FieldProvider).",
                field);
        InputDescriptionType inputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            inputDescriptionType = literalDataParser.parseInput(field, "literal",
                    URI.create(processId));
        } catch (MalformedScriptException ignored) {}
        Assert.assertNotNull("Unable to parse the field 'simplestInput'.", inputDescriptionType);

        //Tests the DataDescription from the InputDescriptionType
        Assert.assertNotNull("The JAXBElement from the InputDescriptionType should no be null",
                inputDescriptionType.getDataDescription());
        DataDescriptionType dataDescriptionType = inputDescriptionType.getDataDescription().getValue();
        Assert.assertNotNull("The DataDescription from the InputDescriptionType should no be null",
                dataDescriptionType);
        Assert.assertTrue("The DataDescriptionType from the InputDescriptionType should be an instance of " +
                "LiteralDataType.", dataDescriptionType instanceof LiteralDataType);
        LiteralDataType LiteralData = (LiteralDataType) dataDescriptionType;
        Assert.assertEquals("The LiteralData literalDataDomain attribute  size should be 1",
                1, LiteralData.getLiteralDataDomain().size());
        Assert.assertTrue("The LiteralData literalDataDomain attribute should contains AnyValue object.",
                LiteralData.getLiteralDataDomain().get(0).isSetAnyValue());
        Assert.assertEquals("The LiteralData literalDataDomain model type should be 'STRING'.",
                "STRING", LiteralData.getLiteralDataDomain().get(0).getDataType().getValue());

        //Tests the InputAttribute part of the InputDescriptionType
        Assert.assertEquals("The InputDescriptionType maxOccurs attribute should be 1", "1",
                inputDescriptionType.getMaxOccurs());
        Assert.assertEquals("The InputDescriptionType minOccurs attribute should be 1", new BigInteger("1"),
                inputDescriptionType.getMinOccurs());

        //Tests the DescriptionTypeAttribute part of the InputDescriptionType
        Assert.assertFalse("The InputDescriptionType title attribute should not be empty",
                inputDescriptionType.getTitle().isEmpty());
        Assert.assertEquals("The InputDescriptionType title should be 'title'.", "title",
                inputDescriptionType.getTitle().get(0).getValue());
        Assert.assertTrue("The InputDescriptionType description attribute should be empty",
                inputDescriptionType.getAbstract().isEmpty());
        Assert.assertTrue("The InputDescriptionType keywords attribute should be empty",
                inputDescriptionType.getKeywords().isEmpty());
        Assert.assertEquals("The InputDescriptionType identifier is incorrect.", processId+":"+field.getName(),
                inputDescriptionType.getIdentifier().getValue());
        Assert.assertTrue("The InputDescriptionType metadata attribute should be empty",
                inputDescriptionType.getMetadata().isEmpty());
    }

    /**
     * Tests the parsing of a complex input.
     */
    @Test
    public void testComplexParseInput(){
        Field field = null;
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("complexInput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'complexInput' from class FieldProvider).",
                field);
        InputDescriptionType inputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            inputDescriptionType = literalDataParser.parseInput(field, "literal",
                    URI.create(processId));
        } catch (MalformedScriptException ignored) {}
        Assert.assertNotNull("Unable to parse the field 'complexInput'.", inputDescriptionType);

        //Tests the DataDescription from the InputDescriptionType
        Assert.assertNotNull("The JAXBElement from the InputDescriptionType should no be null",
                inputDescriptionType.getDataDescription());
        DataDescriptionType dataDescriptionType = inputDescriptionType.getDataDescription().getValue();
        Assert.assertNotNull("The DataDescription from the InputDescriptionType should no be null",
                dataDescriptionType);
        Assert.assertTrue("The DataDescriptionType from the InputDescriptionType should be an instance of " +
                "LiteralData.", dataDescriptionType instanceof LiteralDataType);
        LiteralDataType LiteralData = (LiteralDataType) dataDescriptionType;
        Assert.assertEquals("The LiteralData literalDataDomain attribute size should be 3.",
                3, LiteralData.getLiteralDataDomain().size());

        Assert.assertTrue("The LiteralData literalDataDomain should be the default one.",
                LiteralData.getLiteralDataDomain().get(0).isDefault());
        LiteralDataType.LiteralDataDomain literalDataDomain = LiteralData.getLiteralDataDomain().get(0);
        Assert.assertEquals("The LiteralData literalDataDomain model type should be 'STRING'.",
                "STRING", literalDataDomain.getDataType().getValue());
        Assert.assertTrue("The LiteralData allowedValues should be set.",
                literalDataDomain.getAllowedValues().isSetValueOrRange());
        Assert.assertEquals("The LiteralData valueOrRange size should be 1.",
                1, literalDataDomain.getAllowedValues().getValueOrRange().size());
        Assert.assertEquals("The first valueOrRange should be a ValueType.",
                ValueType.class, literalDataDomain.getAllowedValues().getValueOrRange().get(0).getClass());
        ValueType valueType = (ValueType)literalDataDomain.getAllowedValues().getValueOrRange().get(0);
        Assert.assertEquals("The first valueOrRange value should be 'defaultValue'.",
                "defaultValue", valueType.getValue());

        Assert.assertFalse("The LiteralData literalDataDomain should not be the default one.",
                LiteralData.getLiteralDataDomain().get(1).isDefault());
        literalDataDomain = LiteralData.getLiteralDataDomain().get(1);
        Assert.assertEquals("The LiteralData literalDataDomain model type should be 'STRING'.",
                "STRING", literalDataDomain.getDataType().getValue());
        Assert.assertTrue("The LiteralData allowedValues should be set.",
                literalDataDomain.getAllowedValues().isSetValueOrRange());
        Assert.assertEquals("The LiteralData valueOrRange size should be 1.",
                1, literalDataDomain.getAllowedValues().getValueOrRange().size());
        Assert.assertEquals("The first valueOrRange should be a ValueType.",
                ValueType.class, literalDataDomain.getAllowedValues().getValueOrRange().get(0).getClass());
        valueType = (ValueType)literalDataDomain.getAllowedValues().getValueOrRange().get(0);
        Assert.assertEquals("The first valueOrRange value should be 'defaultValue'.",
                "value1", valueType.getValue());

        Assert.assertFalse("The LiteralData literalDataDomain should not be the default one.",
                LiteralData.getLiteralDataDomain().get(2).isDefault());
        literalDataDomain = LiteralData.getLiteralDataDomain().get(2);
        Assert.assertEquals("The LiteralData literalDataDomain model type should be 'STRING'.",
                "STRING", literalDataDomain.getDataType().getValue());
        Assert.assertTrue("The LiteralData allowedValues should be set.",
                literalDataDomain.getAllowedValues().isSetValueOrRange());
        Assert.assertEquals("The LiteralData valueOrRange size should be 1.",
                1, literalDataDomain.getAllowedValues().getValueOrRange().size());
        Assert.assertEquals("The first valueOrRange should be a ValueType.",
                ValueType.class, literalDataDomain.getAllowedValues().getValueOrRange().get(0).getClass());
        valueType = (ValueType)literalDataDomain.getAllowedValues().getValueOrRange().get(0);
        Assert.assertEquals("The first valueOrRange value should be 'defaultValue'.",
                "value2", valueType.getValue());


        //Tests the InputAttribute part of the InputDescriptionType
        Assert.assertEquals("The InputDescriptionType maxOccurs attribute should be 1", "2",
                inputDescriptionType.getMaxOccurs());
        Assert.assertEquals("The InputDescriptionType minOccurs attribute should be 1", new BigInteger("0"),
                inputDescriptionType.getMinOccurs());

        //Tests the DescriptionTypeAttribute part of the InputDescriptionType
        Assert.assertEquals("The InputDescriptionType title attribute should have a size of 1", 1,
                inputDescriptionType.getTitle().size());
        Assert.assertEquals("The InputDescriptionType first title value is not the one expected", "title",
                inputDescriptionType.getTitle().get(0).getValue());

        Assert.assertEquals("The InputDescriptionType description attribute should have a size of 1", 1,
                inputDescriptionType.getAbstract().size());
        Assert.assertEquals("The InputDescriptionType first abstract value is not the one expected", "description",
                inputDescriptionType.getAbstract().get(0).getValue());

        Assert.assertEquals("The InputDescriptionType keywords attribute should have a size of 1", 1,
                inputDescriptionType.getKeywords().size());

        Assert.assertEquals("The InputDesciriptionType identifier is incorrect.", processId+":"+"identifier",
                inputDescriptionType.getIdentifier().getValue());

        Assert.assertEquals("The InputDescriptionType metadata attribute size should be 1", 1,
                inputDescriptionType.getMetadata().size());
        Assert.assertEquals("The role of the first metadata is not the one expected", "role",
                inputDescriptionType.getMetadata().get(0).getRole());
        Assert.assertEquals("The title of the first metadata is not the one expected", "title",
                inputDescriptionType.getMetadata().get(0).getTitle());
    }



    /**
     * Tests the parsing of the simplest output.
     */
    @Test
    public void testSimplestParseOutput(){
        Field field = null;
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("simplestOutput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'simplestOutput' from class FieldProvider).",
                field);
        OutputDescriptionType outputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            outputDescriptionType = literalDataParser.parseOutput(field, "literal",
                    URI.create(processId));
        } catch (MalformedScriptException ignored) {}
        Assert.assertNotNull("Unable to parse the field 'simplestOutput'.", outputDescriptionType);

        //Tests the DataDescription from the OutputDescriptionType
        Assert.assertNotNull("The JAXBElement from the OutputDescriptionType should no be null",
                outputDescriptionType.getDataDescription());
        DataDescriptionType dataDescriptionType = outputDescriptionType.getDataDescription().getValue();
        Assert.assertNotNull("The DataDescription from the OutputDescriptionType should no be null",
                dataDescriptionType);
        Assert.assertTrue("The DataDescriptionType from the InputDescriptionType should be an instance of " +
                "LiteralDataType.", dataDescriptionType instanceof LiteralDataType);
        LiteralDataType LiteralData = (LiteralDataType) dataDescriptionType;
        Assert.assertEquals("The LiteralData literalDataDomain attribute  size should be 1",
                1, LiteralData.getLiteralDataDomain().size());
        Assert.assertTrue("The LiteralData literalDataDomain attribute should contains AnyValue object.",
                LiteralData.getLiteralDataDomain().get(0).isSetAnyValue());

        //Tests the DescriptionTypeAttribute part of the OutputDescriptionType
        Assert.assertFalse("The OutputDescriptionType title attribute should not be empty",
                outputDescriptionType.getTitle().isEmpty());
        Assert.assertEquals("The OutputDescriptionType title should be 'title'.", "title",
                outputDescriptionType.getTitle().get(0).getValue());
        Assert.assertTrue("The OutputDescriptionType description attribute should be empty",
                outputDescriptionType.getAbstract().isEmpty());
        Assert.assertTrue("The OutputDescriptionType keywords attribute should be empty",
                outputDescriptionType.getKeywords().isEmpty());
        Assert.assertEquals("The OutputDescriptionType identifier is incorrect.", processId+":"+field.getName(),
                outputDescriptionType.getIdentifier().getValue());
        Assert.assertTrue("The OutputDescriptionType metadata attribute should be empty",
                outputDescriptionType.getMetadata().isEmpty());
    }

    /**
     * Tests the parsing of a complex output.
     */
    @Test
    public void testComplexParseOutput(){
        Field field = null;
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("complexInput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'complexInput' from class FieldProvider).",
                field);
        OutputDescriptionType outputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            outputDescriptionType = literalDataParser.parseOutput(field, "literal",
                    URI.create(processId));
        } catch (MalformedScriptException ignored) {}
        Assert.assertNotNull("Unable to parse the field 'complexInput'.", outputDescriptionType);

        //Tests the DataDescription from the OutputDescriptionType
        Assert.assertNotNull("The JAXBElement from the OutputDescriptionType should no be null",
                outputDescriptionType.getDataDescription());
        DataDescriptionType dataDescriptionType = outputDescriptionType.getDataDescription().getValue();
        Assert.assertNotNull("The DataDescription from the OutputDescriptionType should no be null",
                dataDescriptionType);
        Assert.assertTrue("The DataDescriptionType from the OutputDescriptionType should be an instance of " +
                "LiteralData.", dataDescriptionType instanceof LiteralDataType);
        LiteralDataType LiteralData = (LiteralDataType) dataDescriptionType;
        Assert.assertEquals("The LiteralData literalDataDomain attribute size should be 3.",
                3, LiteralData.getLiteralDataDomain().size());

        Assert.assertTrue("The LiteralData literalDataDomain should be the default one.",
                LiteralData.getLiteralDataDomain().get(0).isDefault());
        LiteralDataType.LiteralDataDomain literalDataDomain = LiteralData.getLiteralDataDomain().get(0);
        Assert.assertEquals("The LiteralData literalDataDomain model type should be 'STRING'.",
                "STRING", literalDataDomain.getDataType().getValue());
        Assert.assertTrue("The LiteralData allowedValues should be set.",
                literalDataDomain.getAllowedValues().isSetValueOrRange());
        Assert.assertEquals("The LiteralData valueOrRange size should be 1.",
                1, literalDataDomain.getAllowedValues().getValueOrRange().size());
        Assert.assertEquals("The first valueOrRange should be a ValueType.",
                ValueType.class, literalDataDomain.getAllowedValues().getValueOrRange().get(0).getClass());
        ValueType valueType = (ValueType)literalDataDomain.getAllowedValues().getValueOrRange().get(0);
        Assert.assertEquals("The first valueOrRange value should be 'defaultValue'.",
                "defaultValue", valueType.getValue());

        Assert.assertFalse("The LiteralData literalDataDomain should not be the default one.",
                LiteralData.getLiteralDataDomain().get(1).isDefault());
        literalDataDomain = LiteralData.getLiteralDataDomain().get(1);
        Assert.assertEquals("The LiteralData literalDataDomain model type should be 'STRING'.",
                "STRING", literalDataDomain.getDataType().getValue());
        Assert.assertTrue("The LiteralData allowedValues should be set.",
                literalDataDomain.getAllowedValues().isSetValueOrRange());
        Assert.assertEquals("The LiteralData valueOrRange size should be 1.",
                1, literalDataDomain.getAllowedValues().getValueOrRange().size());
        Assert.assertEquals("The first valueOrRange should be a ValueType.",
                ValueType.class, literalDataDomain.getAllowedValues().getValueOrRange().get(0).getClass());
        valueType = (ValueType)literalDataDomain.getAllowedValues().getValueOrRange().get(0);
        Assert.assertEquals("The first valueOrRange value should be 'defaultValue'.",
                "value1", valueType.getValue());

        Assert.assertFalse("The LiteralData literalDataDomain should not be the default one.",
                LiteralData.getLiteralDataDomain().get(2).isDefault());
        literalDataDomain = LiteralData.getLiteralDataDomain().get(2);
        Assert.assertEquals("The LiteralData literalDataDomain model type should be 'STRING'.",
                "STRING", literalDataDomain.getDataType().getValue());
        Assert.assertTrue("The LiteralData allowedValues should be set.",
                literalDataDomain.getAllowedValues().isSetValueOrRange());
        Assert.assertEquals("The LiteralData valueOrRange size should be 1.",
                1, literalDataDomain.getAllowedValues().getValueOrRange().size());
        Assert.assertEquals("The first valueOrRange should be a ValueType.",
                ValueType.class, literalDataDomain.getAllowedValues().getValueOrRange().get(0).getClass());
        valueType = (ValueType)literalDataDomain.getAllowedValues().getValueOrRange().get(0);
        Assert.assertEquals("The first valueOrRange value should be 'defaultValue'.",
                "value2", valueType.getValue());

        //Tests the DescriptionTypeAttribute part of the OutputDescriptionType
        Assert.assertEquals("The OutputDescriptionType title attribute should have a size of 1", 1,
                outputDescriptionType.getTitle().size());
        Assert.assertEquals("The OutputDescriptionType first title value is not the one expected", "title",
                outputDescriptionType.getTitle().get(0).getValue());

        Assert.assertEquals("The OutputDescriptionType description attribute should have a size of 1", 1,
                outputDescriptionType.getAbstract().size());
        Assert.assertEquals("The OutputDescriptionType first abstract value is not the one expected", "description",
                outputDescriptionType.getAbstract().get(0).getValue());

        Assert.assertEquals("The OutputDescriptionType keywords attribute should have a size of 1", 1,
                outputDescriptionType.getKeywords().size());

        Assert.assertEquals("The InputDesciriptionType identifier is incorrect.", processId+":"+"identifier",
                outputDescriptionType.getIdentifier().getValue());

        Assert.assertEquals("The OutputDescriptionType metadata attribute size should be 1", 1,
                outputDescriptionType.getMetadata().size());
        Assert.assertEquals("The role of the first metadata is not the one expected", "role",
                outputDescriptionType.getMetadata().get(0).getRole());
        Assert.assertEquals("The title of the first metadata is not the one expected", "title",
                outputDescriptionType.getMetadata().get(0).getTitle());
    }

    /**
     * Test if the LiteralDataType have the same DataType as the Field.
     */
    @Test
    public void testDataType(){

        //String field
        Field field = null;
        InputDescriptionType inputDescriptionType = null;
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_String");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_String'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, "", URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_String'.");
        }

        LiteralDataType data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'STRING'.",
                "STRING", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //int field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_int");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_int'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_int'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'INTEGER'.",
                "INTEGER", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //Integer field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_Integer");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_Integer'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_Integer'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'INTEGER'.",
                "INTEGER", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //float field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_float");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_float'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_float'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'FLOAT'.",
                "FLOAT", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //Float field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_float");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_float'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_float'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'FLOAT'.",
                "FLOAT", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //long field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_long");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_long'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_long'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'LONG'.",
                "LONG", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //Long field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_Long");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_Long'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_Long'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'LONG'.",
                "LONG", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //double field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_double");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_double'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_double'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'DOUBLE'.",
                "DOUBLE", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //Double field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_Double");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_Double'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_Double'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'DOUBLE'.",
                "DOUBLE", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //char field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_char");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_char'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_char'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'UNSIGNED_BYTE'.",
                "UNSIGNED_BYTE", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //Character field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_Character");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_Character'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_Character'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'UNSIGNED_BYTE'.",
                "UNSIGNED_BYTE", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //short field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_short");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_short'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_short'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'SHORT'.",
                "SHORT", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //Short field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_Short");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_Short'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_Short'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'SHORT'.",
                "SHORT", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //byte field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_byte");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_byte'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_byte'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'BYTE'.",
                "BYTE", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //Byte field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_Byte");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_Byte'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_Byte'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'BYTE'.",
                "BYTE", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //boolean field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_boolean");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_boolean'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_boolean'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'BOOLEAN'.",
                "BOOLEAN", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //Boolean field
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_Boolean");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_Boolean'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {
            Assert.fail("Unable to parse the field 'f_Boolean'.");
        }

        data = (LiteralDataType) inputDescriptionType.getDataDescription().getValue();
        Assert.assertTrue("The LiteralDataDomain should be set.", data.isSetLiteralDataDomain());
        Assert.assertFalse("The LiteralDataDomain should not be empty.", data.getLiteralDataDomain().isEmpty());
        Assert.assertEquals("The LiteralDataDomain dataType should be 'BOOLEAN'.",
                "BOOLEAN", data.getLiteralDataDomain().get(0).getDataType().getValue());

        //Object field
        inputDescriptionType = null;
        try {
            field = LiteralDataParserTest.FieldProvider.class.getDeclaredField("f_Object");
        } catch (NoSuchFieldException ignored) {
            Assert.fail("Unable to get the field 'f_Object'.");
        }
        try {
            inputDescriptionType = literalDataParser.parseInput(field, 0, URI.create(""));
        } catch (MalformedScriptException ignored) {}
        Assert.assertNull("The inputDescriptionType should be null.", inputDescriptionType);
    }

    /**
     * Returns true the the two lists contains the same elements, no matter the order.
     * @param l1 First list to test.
     * @param l2 Second list to test.
     * @param <T> Type of the elements contained by the lists.
     * @return True if the two lists contains the same elements (no matter the order), false otherwise.
     */
    private static <T> boolean listEqualsNoOrder(List<T> l1, List<T> l2) {
        final Set<T> s1 = new HashSet<>(l1);
        final Set<T> s2 = new HashSet<>(l2);
        return s1.equals(s2);
    }

    /**
     * Class used to declare and get fields with the annotation to parse.
     */
    private class FieldProvider{
        /** The simplest input declaration */
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private String simplestInput = "literal";

        /** A complex input declaration */
        @LiteralDataAttribute(
                defaultDomain = "defaultValue",
                validDomains = {"value1", "value2"}
        )
        @InputAttribute(maxOccurs = 2, minOccurs = 0)
        @DescriptionTypeAttribute(
                title = "title",
                description = "description",
                keywords = {"keyword"},
                identifier = "identifier",
                metadata = {"role","title"}
        )
        private String complexInput = "literal";

        /** The simplest output declaration */
        @LiteralDataAttribute()
        @OutputAttribute
        @DescriptionTypeAttribute(title = "title")
        private String simplestOutput = "literal";

        /** A complex output declaration */
        @LiteralDataAttribute(
                defaultDomain = "defaultValue",
                validDomains = {"value1", "value2"}
        )
        @OutputAttribute
        @DescriptionTypeAttribute(
                title = "title",
                description = "description",
                keywords = {"keyword"},
                identifier = "identifier",
                metadata = {"role","title"}
        )
        private String complexOutput = "literal";

        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private String f_String;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private int f_int;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private Integer f_Integer;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private float f_float;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private Float f_Float;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private long f_long;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private Long f_Long;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private double f_double;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private Double f_Double;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private char f_char;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private Character f_Character;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private short f_short;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private Short f_Short;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private byte f_byte;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private Byte f_Byte;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private boolean f_boolean;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private Boolean f_Boolean;
        @LiteralDataAttribute()
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private Object f_Object;
    }
}
