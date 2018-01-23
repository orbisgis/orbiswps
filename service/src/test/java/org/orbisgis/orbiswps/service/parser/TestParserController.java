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

import net.opengis.wps._2_0.ProcessOffering;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.orbiswps.service.parser.ParserController;
import org.orbisgis.orbiswps.serviceapi.model.MalformedScriptException;

/**
 * Test class for the ParserController
 *
 * @author Sylvain PALOMINOS
 */
public class TestParserController {

    /** JDBCTable parser. */
    private ParserController parserController;

    @Before
    public void initialization(){
        parserController = new ParserController();
    }

    @Test
    public void testGetProcessClass(){
        Class c = parserController.getProcessClass(this.getClass().getResource("SimpleProcess.groovy").getFile());
        Assert.assertNotNull("The generated class should not be null.", c);
        Assert.assertEquals("The class name should be 'org.orbiswps.service.controller.parser.SimpleProcess'.",
                "org.orbisgis.orbiswps.service.controller.parser.SimpleProcess", c.getName());

        c = parserController.getProcessClass("absent.groovy");
        Assert.assertNull("The generated class should be null.", c);
    }

    @Test
    public void testParseProcess(){
        ProcessOffering processOffering = null;
        try {
            processOffering = parserController.parseProcess("absent.groovy");
        } catch (MalformedScriptException ignored) {}
        Assert.assertNull("The ProcessOffering should be null.", processOffering);


        processOffering = null;
        try {
            processOffering = parserController.parseProcess(this.getClass().getResource("NoProcessingProcess.groovy").getFile());
        } catch (MalformedScriptException ignored) {}
        Assert.assertNull("The ProcessOffering should be null.", processOffering);


        processOffering = null;
        try {
            processOffering = parserController.parseProcess(this.getClass().getResource("SimpleProcess.groovy").getFile());
        } catch (MalformedScriptException ignored) {}
        Assert.assertNotNull("The ProcessOffering should not be null.", processOffering);
    }
}
