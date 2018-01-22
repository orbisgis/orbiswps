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
package org.orbisgis.orbiswps.service.controller.parser

import org.orbisgis.orbiswps.groovyapi.input.EnumerationInput
import org.orbisgis.orbiswps.groovyapi.input.JDBCColumnInput
import org.orbisgis.orbiswps.groovyapi.input.JDBCTableInput
import org.orbisgis.orbiswps.groovyapi.input.JDBCValueInput
import org.orbisgis.orbiswps.groovyapi.output.EnumerationOutput
import org.orbisgis.orbiswps.groovyapi.output.JDBCColumnOutput
import org.orbisgis.orbiswps.groovyapi.output.JDBCTableOutput
import org.orbisgis.orbiswps.groovyapi.output.JDBCValueOutput
import org.orbisgis.orbiswps.groovyapi.process.Process
/********************/
/** Process method **/
/********************/

/**
 * Test script for the Enumeration
 * @author Sylvain PALOMINOS
 */
@Process(title = "Process test",
        description = "Test script using the Enumeration ComplexData.",
        keywords = ["test","script","wps"],
        identifier = "orbisgis:test:enumeration",
        metadata = ["website","metadata"]
)
def processing() {
    sleep(500)
    enumerationOutput = inputEnumeration;
}


/****************/
/** INPUT Data **/
/****************/

/** This Enumeration is the input model source. */
@EnumerationInput(
        title = "Input Enumeration",
        description = "A Enumeration input.",
        keywords = "input",
        multiSelection = true,
        isEditable = true,
        values = ["value1", "value2"],
        names = ["name","name"],
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "input",
        metadata = ["website","metadata"]
        )
String[] inputEnumeration = ["value2"]

@JDBCTableInput(title = "JDBCTable for the JDBCValue",
        identifier = "orbisgis:test:jdbctable:input")
String jdbcTableInput

@JDBCColumnInput(title = "JDBCColumn for the JDBCValue",
        identifier = "orbisgis:test:jdbccolumn:input",
        jdbcTableReference = "orbisgis:test:jdbctable:input")
String jdbcColumnInput

/** This JDBCValue is the input model source. */
@JDBCValueInput(
        title = "Input JDBCValue",
        description = "A JDBCValue input.",
        keywords = "input",
        jdbcColumnReference = "orbisgis:test:jdbccolumn:input",
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "orbisgis:test:jdbcvalue:input",
        metadata = ["website","metadata"]
)
String inputJDBCValue

/*****************/
/** OUTPUT Data **/
/*****************/

/** This Enumeration is the output model source. */
@EnumerationOutput(
        title = "Output Enumeration",
        description = "A Enumeration output.",
        keywords = "output",
        values = ["value1", "value2"],
        identifier = "output",
        metadata = ["website","metadata"]
)
String[] enumerationOutput

@JDBCTableOutput(title = "JDBCTable for the JDBCValue",
        identifier = "orbisgis:test:jdbctable:output")
String jdbcTableOutput

@JDBCColumnOutput(title = "JDBCColumn for the JDBCValue",
        identifier = "orbisgis:test:jdbccolumn:output",
        jdbcTableReference = "orbisgis:test:jdbctable:output")
String[] jdbcColumnOutput

/** This JDBCValue is the output model source. */
@JDBCValueOutput(
        title = "Output JDBCValue",
        description = "A JDBCValue output.",
        keywords = "output",
        jdbcColumnReference = "orbisgis:test:jdbccolumn:output",
        multiSelection = true,
        identifier = "orbisgis:test:jdbcvalue:output",
        metadata = ["website","metadata"])
String[] jdbcValueOutput

