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
import org.orbisgis.orbiswps.groovyapi.attributes.JDBCColumnAttribute;
import org.orbisgis.orbiswps.groovyapi.attributes.InputAttribute;
import org.orbisgis.orbiswps.groovyapi.attributes.OutputAttribute;
import org.orbisgis.orbiswps.service.model.DataType;
import org.orbisgis.orbiswps.service.model.JDBCColumn;
import org.orbisgis.orbiswps.serviceapi.model.MalformedScriptException;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;

/**
 * Test class for the JDBCColumnParser
 *
 * @author Sylvain PALOMINOS
 */
public class JDBCColumnParserTest {

    /** JDBCColumn parser. */
    private JDBCColumnParser jdbcColumnParser;
    /** Valid JDBCColumn type list to use in test. */
    private List<DataType> validJDBCColumnTypeList;
    /** Valid excluded type list to use in test. */
    private List<DataType> validExcludedTypeList;
    /** Valid excluded name list to use in test. */
    private List<String> validExcludedNameList;

    @Before
    public void initialization(){
        jdbcColumnParser = new JDBCColumnParser();

        validJDBCColumnTypeList = new ArrayList<>();
        validJDBCColumnTypeList.add(DataType.MULTILINESTRING);
        validJDBCColumnTypeList.add(DataType.POLYGON);

        validExcludedTypeList = new ArrayList<>();
        validExcludedTypeList.add(DataType.POINT);
        validExcludedTypeList.add(DataType.MULTIPOINT);

        validExcludedNameList = new ArrayList<>();
        validExcludedNameList.add("name1");
        validExcludedNameList.add("name2");
    }

    /**
     * Tests the annotation linked to this parser.
     */
    @Test
    public void testAnnotation(){
        Assert.assertEquals("The JDBCColumnParser annotation class should be 'JDBCColumnAttribute'.",
                JDBCColumnAttribute.class, jdbcColumnParser.getAnnotation());
    }

    /**
     * Tests the parsing of the simplest input.
     */
    @Test
    public void testSimplestParseInput(){
        Field field = null;
        try {
            field = JDBCColumnParserTest.FieldProvider.class.getDeclaredField("simplestInput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'simplestInput' from class FieldProvider).",
                field);
        InputDescriptionType inputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            inputDescriptionType = jdbcColumnParser.parseInput(field, new String[]{"column1", "column2", "column3"},
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
                "JDBCColumn.", dataDescriptionType instanceof JDBCColumn);
        JDBCColumn jdbcColumn = (JDBCColumn) dataDescriptionType;
        Assert.assertArrayEquals("The JDBCColumn defaultValues attribute should be ['column1', 'column2', 'column3'].",
                new String[]{"column1", "column2", "column3"}, jdbcColumn.getDefaultValues());
        Assert.assertTrue("The JDBCColumn jdbc table identifier attribute should contains 'reference'.",
                jdbcColumn.getJDBCTableIdentifier().toString().contains("reference"));
        Assert.assertTrue("The JDBCColumn dataTypeList attribute should be empty.",
                jdbcColumn.getDataTypeList().isEmpty());
        Assert.assertTrue("The JDBCColumn excludedTypeList attribute should be empty.",
                jdbcColumn.getExcludedTypeList().isEmpty());
        Assert.assertTrue("The JDBCColumn excludedNameList attribute should be empty.",
                jdbcColumn.getExcludedNameList().isEmpty());
        Assert.assertFalse("The JDBCColumn multiSelection attribute should be false.",
                jdbcColumn.isMultiSelection());
        Assert.assertTrue("The JDBCColumn isSourceModified attribute should be true.",
                jdbcColumn.isSourceModified());

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
            field = JDBCColumnParserTest.FieldProvider.class.getDeclaredField("complexInput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'complexInput' from class FieldProvider).",
                field);
        InputDescriptionType inputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            inputDescriptionType = jdbcColumnParser.parseInput(field, new String[]{"column1", "column2", "column3"},
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
                "JDBCColumn.", dataDescriptionType instanceof JDBCColumn);
        JDBCColumn jdbcColumn = (JDBCColumn) dataDescriptionType;
        Assert.assertArrayEquals("The JDBCColumn defaultValues attribute should be ['column1', 'column2', 'column3'].",
                new String[]{"column1", "column2", "column3"}, jdbcColumn.getDefaultValues());
        Assert.assertTrue("The JDBCColumn jdbc table identifier attribute should contains 'reference'.",
                jdbcColumn.getJDBCTableIdentifier().toString().contains("reference"));
        Assert.assertTrue("The JDBCColumn dataTypeList attribute should contains 'MULTILINESTRING', 'POLYGON'.",
                listEqualsNoOrder(jdbcColumn.getDataTypeList(), validJDBCColumnTypeList));
        Assert.assertTrue("The JDBCColumn excludedTypeList attribute should contains 'POINT', 'MULTIPOINT'.",
                listEqualsNoOrder(jdbcColumn.getExcludedTypeList(), validExcludedTypeList));
        Assert.assertTrue("The JDBCColumn excludedNameList attribute should contains 'name1', 'name2'.",
                listEqualsNoOrder(jdbcColumn.getExcludedNameList(), validExcludedNameList));
        Assert.assertTrue("The JDBCColumn multiSelection attribute should be true.",
                jdbcColumn.isMultiSelection());
        Assert.assertTrue("The JDBCColumn isSourceModified attribute should be true.",
                jdbcColumn.isSourceModified());

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
            field = JDBCColumnParserTest.FieldProvider.class.getDeclaredField("simplestOutput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'simplestOutput' from class FieldProvider).",
                field);
        OutputDescriptionType outputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            outputDescriptionType = jdbcColumnParser.parseOutput(field, new String[]{"column1", "column2", "column3"},
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
                "JDBCColumn.", dataDescriptionType instanceof JDBCColumn);
        JDBCColumn jdbcColumn = (JDBCColumn) dataDescriptionType;
        Assert.assertArrayEquals("The JDBCColumn defaultValues attribute should be ['column1', 'column2', 'column3'].",
                new String[]{"column1", "column2", "column3"}, jdbcColumn.getDefaultValues());
        Assert.assertTrue("The JDBCColumn jdbc table identifier attribute should contains 'reference'.",
                jdbcColumn.getJDBCTableIdentifier().toString().contains("reference"));
        Assert.assertTrue("The JDBCColumn dataTypeList attribute should be empty.",
                jdbcColumn.getDataTypeList().isEmpty());
        Assert.assertTrue("The JDBCColumn excludedTypeList attribute should be empty.",
                jdbcColumn.getExcludedTypeList().isEmpty());
        Assert.assertTrue("The JDBCColumn excludedNameList attribute should be empty.",
                jdbcColumn.getExcludedNameList().isEmpty());
        Assert.assertFalse("The JDBCColumn multiSelection attribute should be false.",
                jdbcColumn.isMultiSelection());
        Assert.assertTrue("The JDBCColumn isSourceModified attribute should be true.",
                jdbcColumn.isSourceModified());

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
            field = JDBCColumnParserTest.FieldProvider.class.getDeclaredField("complexInput");
        } catch (NoSuchFieldException ignored) {}
        Assert.assertNotNull("Unable to get the field to parse (field 'complexInput' from class FieldProvider).",
                field);
        OutputDescriptionType outputDescriptionType = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            outputDescriptionType = jdbcColumnParser.parseOutput(field, new String[]{"column1", "column2", "column3"},
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
                "JDBCColumn.", dataDescriptionType instanceof JDBCColumn);
        JDBCColumn jdbcColumn = (JDBCColumn) dataDescriptionType;
        Assert.assertArrayEquals("The JDBCColumn defaultValues attribute should be ['column1', 'column2', 'column3'].",
                new String[]{"column1", "column2", "column3"}, jdbcColumn.getDefaultValues());
        Assert.assertTrue("The JDBCColumn jdbc table identifier attribute should contains 'reference'.",
                jdbcColumn.getJDBCTableIdentifier().toString().contains("reference"));
        Assert.assertTrue("The JDBCColumn dataTypeList attribute should contains 'MULTILINESTRING', 'POLYGON'.",
                listEqualsNoOrder(jdbcColumn.getDataTypeList(), validJDBCColumnTypeList));
        Assert.assertTrue("The JDBCColumn excludedTypeList attribute should contains 'POINT', 'MULTIPOINT'.",
                listEqualsNoOrder(jdbcColumn.getExcludedTypeList(), validExcludedTypeList));
        Assert.assertTrue("The JDBCColumn excludedNameList attribute should contains 'name1', 'name2'.",
                listEqualsNoOrder(jdbcColumn.getExcludedNameList(), validExcludedNameList));
        Assert.assertTrue("The JDBCColumn multiSelection attribute should be true.",
                jdbcColumn.isMultiSelection());
        Assert.assertTrue("The JDBCColumn isSourceModified attribute should be true.",
                jdbcColumn.isSourceModified());

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
        @JDBCColumnAttribute(jdbcTableReference = "reference")
        @InputAttribute
        @DescriptionTypeAttribute(title = "title")
        private String[] simplestInput = {"column1", "column2", "column3"};

        /** A complex input declaration */
        @JDBCColumnAttribute(
                multiSelection = true,
                dataTypes = {"MULTILINESTRING", "POLYGON"},
                excludedTypes = {"POINT", "MULTIPOINT"},
                excludedNames = {"name1", "name2"},
                jdbcTableReference = "reference"
        )
        @InputAttribute(maxOccurs = 2, minOccurs = 0)
        @DescriptionTypeAttribute(
                title = "title",
                description = "description",
                keywords = {"keyword"},
                identifier = "identifier",
                metadata = {"role","title"}
        )
        private String[] complexInput = {"column1", "column2", "column3"};

        /** The simplest output declaration */
        @JDBCColumnAttribute(jdbcTableReference = "reference")
        @OutputAttribute
        @DescriptionTypeAttribute(title = "title")
        private String[] simplestOutput = {"column1", "column2", "column3"};

        /** A complex output declaration */
        @JDBCColumnAttribute(
                multiSelection = true,
                dataTypes = {"MULTILINESTRING", "POLYGON"},
                excludedTypes = {"POINT", "MULTIPOINT"},
                excludedNames = {"name1", "name2"},
                jdbcTableReference = "reference"
        )
        @OutputAttribute
        @DescriptionTypeAttribute(
                title = "title",
                description = "description",
                keywords = {"keyword"},
                identifier = "identifier",
                metadata = {"role","title"}
        )
        private String[] complexOutput = {"column1", "column2", "column3"};
    }
}
