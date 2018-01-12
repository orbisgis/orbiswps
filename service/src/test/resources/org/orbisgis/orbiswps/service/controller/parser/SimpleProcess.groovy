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
package org.orbisgis.orbiswps.server.controller.parser

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
@Process(title = ["Process test","en","Test du Process","fr"],
        description = ["Test script using the Enumeration ComplexData.","en",
                "Scripts test pour l'usage du ComplexData Enumeration.","fr"],
        keywords = ["test,script,wps","en","test,scripte,wps","fr"],
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

/** This Enumeration is the input data source. */
@EnumerationInput(
        title = ["Input Enumeration","en","Entrée Enumeration","fr"],
        description = ["A Enumeration input.","en","Une entrée Enumeration.","fr"],
        keywords = ["input","en","entrée","fr"],
        multiSelection = true,
        isEditable = true,
        values = ["value1", "value2"],
        names = ["name,name","en","nom,nom","fr"],
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

/** This JDBCValue is the input data source. */
@JDBCValueInput(
        title = ["Input JDBCValue","en","Entrée JDBCValue","fr"],
        description = ["A JDBCValue input.","en","Une entrée JDBCValue.","fr"],
        keywords = ["input","en","entrée","fr"],
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

/** This Enumeration is the output data source. */
@EnumerationOutput(
        title = ["Output Enumeration","en","Sortie Enumeration","fr"],
        description = ["A Enumeration output.","en","Une sortie Enumeration.","fr"],
        keywords = ["output","en","sortie","fr"],
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

/** This JDBCValue is the output data source. */
@JDBCValueOutput(
        title = ["Output JDBCValue","en","Sortie JDBCValue","fr"],
        description = ["A JDBCValue output.","en","Une sortie JDBCValue.","fr"],
        keywords = ["output","en","sortie","fr"],
        jdbcColumnReference = "orbisgis:test:jdbccolumn:output",
        multiSelection = true,
        identifier = "orbisgis:test:jdbcvalue:output",
        metadata = ["website","metadata"])
String[] jdbcValueOutput

