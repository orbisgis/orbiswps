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
package org.orbiswps.server.controller.parser;

import net.opengis.wps._2_0.ProcessDescriptionType;
import net.opengis.wps._2_0.ProcessOffering;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.orbiswps.groovyapi.attributes.DescriptionTypeAttribute;
import org.orbiswps.groovyapi.attributes.ProcessAttribute;
import org.orbiswps.server.model.MalformedScriptException;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.UUID;

/**
 * Test class for the ProcessParser
 *
 * @author Sylvain PALOMINOS
 */
public class ProcessParserTest {

    /** ProcessParser parser. */
    private ProcessParser processParser;

    @Before
    public void initialization(){
        processParser = new ProcessParser();
    }

    /**
     * Tests the parsing of the simplest method.
     */
    @Test
    public void testSimplestParse(){
        Method method = null;
        try {
            method = ProcessParserTest.MethodProvider.class.getDeclaredMethod("simplest");
        } catch (NoSuchMethodException ignored) {}
        Assert.assertNotNull("Unable to get the method to parse (method 'simplest' from class MethodProvider).",
                method);
        ProcessOffering processOffering = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            processOffering = processParser.parseProcess(method, URI.create(processId));
        } catch (MalformedScriptException ignored) {}
        Assert.assertNotNull("Unable to parse the method 'simplest'.", processOffering);

        //Tests the DataDescription from the InputDescriptionType
        Assert.assertTrue("The ProcessOffering jobControlOption should be empty",
                processOffering.getJobControlOptions().isEmpty());
        Assert.assertTrue("The ProcessOffering outputTransmission should be empty",
                processOffering.getOutputTransmission().isEmpty());
        Assert.assertTrue("The ProcessOffering version should be empty",
                processOffering.getProcessVersion().isEmpty());

        ProcessDescriptionType processDescriptionType = processOffering.getProcess();
        //Tests the DescriptionTypeAttribute part of the InputDescriptionType
        Assert.assertFalse("The InputDescriptionType title attribute should not be empty",
                processDescriptionType.getTitle().isEmpty());
        Assert.assertEquals("The InputDescriptionType title should be 'title'.", "title",
                processDescriptionType.getTitle().get(0).getValue());
        Assert.assertTrue("The InputDescriptionType description attribute should be empty",
                processDescriptionType.getAbstract().isEmpty());
        Assert.assertTrue("The InputDescriptionType keywords attribute should be empty",
                processDescriptionType.getKeywords().isEmpty());
        Assert.assertEquals("The InputDescriptionType identifier is incorrect.", processId,
                processDescriptionType.getIdentifier().getValue());
        Assert.assertTrue("The InputDescriptionType metadata attribute should be empty",
                processDescriptionType.getMetadata().isEmpty());
    }

    /**
     * Tests the parsing of a complex method.
     */
    @Test
    public void testComplexParse(){
        Method method = null;
        try {
            method = ProcessParserTest.MethodProvider.class.getDeclaredMethod("complex");
        } catch (NoSuchMethodException ignored) {}
        Assert.assertNotNull("Unable to get the method to parse (method 'complex' from class MethodProvider).",
                method);
        ProcessOffering processOffering = null;
        String processId = "processid:"+UUID.randomUUID().toString();
        try {
            processOffering = processParser.parseProcess(method, URI.create(processId));
        } catch (MalformedScriptException ignored) {}
        Assert.assertNotNull("Unable to parse the method 'complex'.", processOffering);

        //Tests the DataDescription from the InputDescriptionType
        Assert.assertTrue("The ProcessOffering jobControlOption should be empty",
                processOffering.getJobControlOptions().isEmpty());
        Assert.assertTrue("The ProcessOffering outputTransmission should be empty",
                processOffering.getOutputTransmission().isEmpty());
        Assert.assertEquals("The ProcessOffering version should be '1.0.0'",
                "1.0.0", processOffering.getProcessVersion());

        ProcessDescriptionType processDescriptionType = processOffering.getProcess();
        //Tests the DescriptionTypeAttribute part of the InputDescriptionType
        Assert.assertEquals("The InputDescriptionType title attribute should have a size of 2", 2,
                processDescriptionType.getTitle().size());
        Assert.assertEquals("The InputDescriptionType first title value is not the one expected", "title",
                processDescriptionType.getTitle().get(0).getValue());
        Assert.assertEquals("The InputDescriptionType first title language is not the one expected", "en",
                processDescriptionType.getTitle().get(0).getLang());
        Assert.assertEquals("The InputDescriptionType second title value is not the one expected", "titre",
                processDescriptionType.getTitle().get(1).getValue());
        Assert.assertEquals("The InputDescriptionType second title language is not the one expected", "fr",
                processDescriptionType.getTitle().get(1).getLang());

        Assert.assertEquals("The InputDescriptionType description attribute should have a size of 2", 2,
                processDescriptionType.getAbstract().size());
        Assert.assertEquals("The InputDescriptionType first abstract value is not the one expected", "description",
                processDescriptionType.getAbstract().get(0).getValue());
        Assert.assertEquals("The InputDescriptionType first abstract language is not the one expected", "en",
                processDescriptionType.getAbstract().get(0).getLang());
        Assert.assertEquals("The InputDescriptionType second abstract value is not the one expected", "description",
                processDescriptionType.getAbstract().get(1).getValue());
        Assert.assertEquals("The InputDescriptionType second abstract language is not the one expected", "fr",
                processDescriptionType.getAbstract().get(1).getLang());

        Assert.assertEquals("The InputDescriptionType keywords attribute should have a size of 1", 1,
                processDescriptionType.getKeywords().size());
        Assert.assertEquals("The InputDescriptionType first keywordList should have a size of 2", 2,
                processDescriptionType.getKeywords().get(0).getKeyword().size());
        Assert.assertEquals("The value of the first keyword of the first keywordList is not the one expected",
                "keyword", processDescriptionType.getKeywords().get(0).getKeyword().get(0).getValue());
        Assert.assertEquals("The language of the first keyword of the first keywordList is not the one expected",
                "en", processDescriptionType.getKeywords().get(0).getKeyword().get(0).getLang());
        Assert.assertEquals("The value of the first keyword of the first keywordList is not the one expected",
                "motclef", processDescriptionType.getKeywords().get(0).getKeyword().get(1).getValue());
        Assert.assertEquals("The language of the first keyword of the first keywordList is not the one expected",
                "fr", processDescriptionType.getKeywords().get(0).getKeyword().get(1).getLang());

        Assert.assertEquals("The InputDesciriptionType identifier is incorrect.", "identifier",
                processDescriptionType.getIdentifier().getValue());

        Assert.assertEquals("The InputDescriptionType metadata attribute size should be 3", 3,
                processDescriptionType.getMetadata().size());
        Assert.assertEquals("The role of the first metadata is not the one expected", "role",
                processDescriptionType.getMetadata().get(0).getRole());
        Assert.assertEquals("The title of the first metadata is not the one expected", "title",
                processDescriptionType.getMetadata().get(0).getTitle());
        Assert.assertEquals("The role of the second metadata is not the one expected", "DBMS_TYPE",
                processDescriptionType.getMetadata().get(1).getRole());
        Assert.assertEquals("The title of the second metadata is not the one expected", "H2GIS",
                processDescriptionType.getMetadata().get(1).getTitle());
        Assert.assertEquals("The role of the third metadata is not the one expected", "DBMS_TYPE",
                processDescriptionType.getMetadata().get(2).getRole());
        Assert.assertEquals("The title of the third metadata is not the one expected", "POSTGIS",
                processDescriptionType.getMetadata().get(2).getTitle());
    }


    /**
     * Class used to declare and get fields with the annotation to parse.
     */
    private class MethodProvider{
        /** The simplest input declaration */
        @ProcessAttribute()
        @DescriptionTypeAttribute(title = {"title"})
        public void simplest(){}

        /** A complex input declaration */
        @ProcessAttribute(
                language = "fr",
                version = "1.0.0",
                properties = {"DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"}
        )
        @DescriptionTypeAttribute(
                title = {"title", "en", "titre", "fr"},
                description = {"description", "en","description","fr"},
                keywords = {"keyword", "en", "motclef", "fr"},
                identifier = "identifier",
                metadata = {"role","title"}
        )
        public void complex(){}
    }
}
