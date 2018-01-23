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

import net.opengis.wps._2_0.DataDescriptionType;
import net.opengis.wps._2_0.InputDescriptionType;
import net.opengis.wps._2_0.OutputDescriptionType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.orbiswps.groovyapi.attributes.DescriptionTypeAttribute;
import org.orbisgis.orbiswps.groovyapi.attributes.InputAttribute;
import org.orbisgis.orbiswps.groovyapi.attributes.JDBCValueAttribute;
import org.orbisgis.orbiswps.groovyapi.attributes.OutputAttribute;
import org.orbisgis.orbiswps.service.model.JDBCValue;
import org.orbisgis.orbiswps.serviceapi.model.MalformedScriptException;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;

/**
 * Test class for the JDBCValueParser
 *
 * @author Sylvain PALOMINOS
 */
public class JDBCValueParserTest {

    /** JDBCValue parser. */
    private JDBCValueParser jdbcValueParser;

    @Before
    public void initialization(){
        jdbcValueParser = new JDBCValueParser();
    }

    /**
     * Tests the annotation linked to this parser.
     */
    @Test
    public void testAnnotation(){
        Assert.assertEquals("The JDBCValueParser annotation class should be 'JDBCValueAttribute'.",
                JDBCValueAttribute.class, jdbcValueParser.getAnnotation());
    }

    /**
     * Tests the parsing of the simplest input.
     */
    @Test
    public void testSimplestParseInput(){
        Field field = null;
        try {
            field = JDBCValueParserTest.FieldProvider.class.getDeclaredField("simplestInput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'simplestInput' from class FieldProvider).",
                field);
        InputDescriptionType inputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            inputDescriptionType = jdbcValueParser.parseInput(field, new String[]{"field1", "field2"},
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
                "JDBCValue.", dataDescriptionType instanceof JDBCValue);
        JDBCValue jdbcValue = (JDBCValue) dataDescriptionType;
        Assert.assertArrayEquals("The JDBCValue defaultValues attribute should contains 'field1', 'field2'.",
                new String[]{"field1", "field2"}, jdbcValue.getDefaultValues());
        Assert.assertTrue("The JDBCValue jdbc column identifier attribute should contains 'reference'.",
                jdbcValue.getJDBCColumnIdentifier().toString().contains("reference"));
        Assert.assertNull("The JDBCValue jdbc table identifier attribute should be null.",
                jdbcValue.getJDBCTableIdentifier());
        Assert.assertTrue("The JDBCValue jdbcColumnModified attribute should be true.",
                jdbcValue.isJDBCColumnModified());
        Assert.assertTrue("The JDBCValue jdbcTableModified attribute should be true.",
                jdbcValue.isJDBCTableModified());
        Assert.assertFalse("The JDBCValue multiSelection attribute should be true.",
                jdbcValue.isMultiSelection());

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
            field = JDBCValueParserTest.FieldProvider.class.getDeclaredField("complexInput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'complexInput' from class FieldProvider).",
                field);
        InputDescriptionType inputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            inputDescriptionType = jdbcValueParser.parseInput(field, new String[]{"field1", "field2"},
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
                "JDBCValue.", dataDescriptionType instanceof JDBCValue);
        JDBCValue jdbcValue = (JDBCValue) dataDescriptionType;
        Assert.assertArrayEquals("The JDBCValue defaultValues attribute should contains 'field1', 'field2'.",
                new String[]{"field1", "field2"}, jdbcValue.getDefaultValues());
        Assert.assertTrue("The JDBCValue jdbc column identifier attribute should contains 'reference'.",
                jdbcValue.getJDBCColumnIdentifier().toString().contains("reference"));
        Assert.assertNull("The JDBCValue jdbc table identifier attribute should be null.",
                jdbcValue.getJDBCTableIdentifier());
        Assert.assertTrue("The JDBCValue jdbcColumnModified attribute should be true.",
                jdbcValue.isJDBCColumnModified());
        Assert.assertTrue("The JDBCValue jdbcTableModified attribute should be true.",
                jdbcValue.isJDBCTableModified());
        Assert.assertTrue("The JDBCValue multiSelection attribute should be true.",
                jdbcValue.isMultiSelection());

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
            field = JDBCValueParserTest.FieldProvider.class.getDeclaredField("simplestOutput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'simplestOutput' from class FieldProvider).",
                field);
        OutputDescriptionType outputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            outputDescriptionType = jdbcValueParser.parseOutput(field, new String[]{"field1", "field2"},
                    URI.create(processId));
        } catch (MalformedScriptException ignored) {}
        Assert.assertNotNull("Unable to parse the field 'simplestOutput'.", outputDescriptionType);

        //Tests the DataDescription from the OutputDescriptionType
        Assert.assertNotNull("The JAXBElement from the OutputDescriptionType should no be null",
                outputDescriptionType.getDataDescription());
        DataDescriptionType dataDescriptionType = outputDescriptionType.getDataDescription().getValue();
        Assert.assertNotNull("The DataDescription from the OutputDescriptionType should no be null",
                dataDescriptionType);
        Assert.assertTrue("The DataDescriptionType from the OutputDescriptionType should be an instance of " +
                "JDBCValue.", dataDescriptionType instanceof JDBCValue);
        JDBCValue jdbcValue = (JDBCValue) dataDescriptionType;
        Assert.assertArrayEquals("The JDBCValue defaultValues attribute should contains 'field1', 'field2'.",
                new String[]{"field1", "field2"}, jdbcValue.getDefaultValues());
        Assert.assertTrue("The JDBCValue jdbc column identifier attribute should contains 'reference'.",
                jdbcValue.getJDBCColumnIdentifier().toString().contains("reference"));
        Assert.assertNull("The JDBCValue jdbc table identifier attribute should be null.",
                jdbcValue.getJDBCTableIdentifier());
        Assert.assertTrue("The JDBCValue jdbcColumnModified attribute should be true.",
                jdbcValue.isJDBCColumnModified());
        Assert.assertTrue("The JDBCValue jdbcTableModified attribute should be true.",
                jdbcValue.isJDBCTableModified());
        Assert.assertFalse("The JDBCValue multiSelection attribute should be true.",
                jdbcValue.isMultiSelection());

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
            field = JDBCValueParserTest.FieldProvider.class.getDeclaredField("complexInput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'complexInput' from class FieldProvider).",
                field);
        OutputDescriptionType outputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            outputDescriptionType = jdbcValueParser.parseOutput(field, new String[]{"field1", "field2"},
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
                "JDBCValue.", dataDescriptionType instanceof JDBCValue);
        JDBCValue jdbcValue = (JDBCValue) dataDescriptionType;
        Assert.assertArrayEquals("The JDBCValue defaultValues attribute should contains 'field1', 'field2'.",
                new String[]{"field1", "field2"}, jdbcValue.getDefaultValues());
        Assert.assertTrue("The JDBCValue jdbc column identifier attribute should contains 'reference'.",
                jdbcValue.getJDBCColumnIdentifier().toString().contains("reference"));
        Assert.assertNull("The JDBCValue jdbc table identifier attribute should be null.",
                jdbcValue.getJDBCTableIdentifier());
        Assert.assertTrue("The JDBCValue jdbcColumnModified attribute should be true.",
                jdbcValue.isJDBCColumnModified());
        Assert.assertTrue("The JDBCValue jdbcTableModified attribute should be true.",
                jdbcValue.isJDBCTableModified());
        Assert.assertTrue("The JDBCValue multiSelection attribute should be true.",
                jdbcValue.isMultiSelection());

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
        @JDBCValueAttribute(jdbcColumnReference = "reference")
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private String[] simplestInput = {"field1", "field2"};

        /** A complex input declaration */
        @JDBCValueAttribute(
                multiSelection = true,
                jdbcColumnReference = "reference"
        )
        @InputAttribute(maxOccurs = 2, minOccurs = 0)
        @DescriptionTypeAttribute(
                title = "title",
                description = "description",
                keywords = {"keyword"},
                identifier = "identifier",
                metadata = {"role","title"}
        )
        private String[] complexInput = {"field1", "field2"};

        /** The simplest output declaration */
        @JDBCValueAttribute(jdbcColumnReference = "reference")
        @OutputAttribute
        @DescriptionTypeAttribute(title = "title")
        private String[] simplestOutput = {"field1", "field2"};

        /** A complex output declaration */
        @JDBCValueAttribute(
                multiSelection = true,
                jdbcColumnReference = "reference"
        )
        @OutputAttribute
        @DescriptionTypeAttribute(
                title = "title",
                description = "description",
                keywords = {"keyword"},
                identifier = "identifier",
                metadata = {"role","title"}
        )
        private String[] complexOutput = {"field1", "field2"};
    }
}
