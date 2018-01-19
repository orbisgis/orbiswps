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
package org.orbisgis.orbiswps.service

import org.orbisgis.orbiswps.groovyapi.input.JDBCColumnInput
import org.orbisgis.orbiswps.groovyapi.input.JDBCTableInput
import org.orbisgis.orbiswps.groovyapi.output.JDBCColumnOutput
import org.orbisgis.orbiswps.groovyapi.output.JDBCTableOutput
import org.orbisgis.orbiswps.groovyapi.process.Process

/********************/
/** Process method **/
/********************/

/**
 * Test script for the JDBCColumn
 * @author Sylvain PALOMINOS
 * @author Erwan Bocher
 */
@Process(title = "JDBCColumn test",
        description = "Test script using the JDBCColumn ComplexData.",
        keywords = ["test","script","wps"],
        identifier = "orbisgis:test:jdbccolumn",
        metadata = ["website","metadata"]
)
def processing() {
    jdbcColumnOutput = inputJDBCColumn;
}


/****************/
/** INPUT Data **/
/****************/

@JDBCTableInput(title = "JDBCTable for the JDBCColumn",
        identifier = "orbisgis:test:jdbctable:input")
String jdbcTableInput

/** This JDBCColumn is the input data source. */
@JDBCColumnInput(
        title = "Input JDBCColumn",
        description = "A JDBCColumn input.",
        keywords = "input",
        jdbcTableReference = "orbisgis:test:jdbctable:input",
        excludedTypes = ["BOOLEAN"],
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "orbisgis:test:jdbccolumn:input",
        metadata = ["website","metadata"]
        )
String inputJDBCColumn

/*****************/
/** OUTPUT Data **/
/*****************/

@JDBCTableOutput(title = "JDBCTable for the JDBCColumn",
        identifier = "orbisgis:test:jdbctable:output")
String jdbcTableOutput

/** This JDBCColumn is the output data source. */
@JDBCColumnOutput(
        title = "Output JDBCColumn",
        description = "A JDBCColumn output.",
        keywords = "output",
        jdbcTableReference = "orbisgis:test:jdbctable:output",
        dataTypes = ["GEOMETRY", "NUMBER"],
        multiSelection = true,
        identifier = "orbisgis:test:jdbccolumn:output",
        metadata = ["website","metadata"]
)
String jdbcColumnOutput

