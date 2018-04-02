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

import com.vividsolutions.jts.geom.Geometry
import org.orbisgis.orbiswps.groovyapi.input.BoundingBoxInput
import org.orbisgis.orbiswps.groovyapi.input.EnumerationInput
import org.orbisgis.orbiswps.groovyapi.input.GeometryInput
import org.orbisgis.orbiswps.groovyapi.input.JDBCColumnInput
import org.orbisgis.orbiswps.groovyapi.input.JDBCTableInput
import org.orbisgis.orbiswps.groovyapi.input.JDBCValueInput
import org.orbisgis.orbiswps.groovyapi.input.LiteralDataInput
import org.orbisgis.orbiswps.groovyapi.input.PasswordInput
import org.orbisgis.orbiswps.groovyapi.input.RawDataInput
import org.orbisgis.orbiswps.groovyapi.output.BoundingBoxOutput
import org.orbisgis.orbiswps.groovyapi.output.EnumerationOutput
import org.orbisgis.orbiswps.groovyapi.output.GeometryOutput
import org.orbisgis.orbiswps.groovyapi.output.JDBCColumnOutput
import org.orbisgis.orbiswps.groovyapi.output.JDBCTableOutput
import org.orbisgis.orbiswps.groovyapi.output.JDBCValueOutput
import org.orbisgis.orbiswps.groovyapi.output.LiteralDataOutput
import org.orbisgis.orbiswps.groovyapi.output.PasswordOutput
import org.orbisgis.orbiswps.groovyapi.output.RawDataOutput
import org.orbisgis.orbiswps.groovyapi.process.Process
/********************/
/** Process method **/
/********************/

/**
 * Test script for the Enumeration
 * @author Sylvain PALOMINOS
 */
@Process(title = "full test script",
        description = "Full test script descr.",
        keywords = ["test","script","wps"],
        identifier = "orbisgis:test:full",
        metadata = ["website","metadata"],
        language = "en",
        properties = ["prop1", "value1"],
        version = "1.0.1"
)
def processing() {
    outputEnumeration = inputEnumeration;
    outputGeometry = inputGeometry;
    outputJDBCTable = inputJDBCTable;
    outputJDBCColumn = inputJDBCColumn;
    outputJDBCValue = inputJDBCValue;
    outputRawData = inputRawData;
    outputPassword = inputPassword;
    outputLiteralDouble = inputLiteralDouble;
    outputLiteralString = inputLiteralString;
    outputBoundingBox = inputBoundingBox;
}


/****************/
/** INPUT Data **/
/****************/

@EnumerationInput(
        title = "Input Enumeration",
        description = "A Enumeration input.",
        keywords = "input",
        multiSelection = true,
        isEditable = true,
        values = "value1",
        names = ["name","name"],
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "input:enumeration",
        metadata = ["website","metadata"]
        )
String[] inputEnumeration = ["value2"]

@GeometryInput(
        title = "Input Geometry",
        description = "A Geometry input.",
        keywords = "input",
        dimension = 3,
        excludedTypes = ["MULTIPOINT", "POINT"],
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "input:geometry",
        metadata = ["website","metadata"]
)
String inputGeometry

@JDBCTableInput(
        title = "Input JDBCTable",
        description = "A JDBCTable input.",
        keywords = "input",
        dataTypes = ["GEOMETRY"],
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "input:jdbctable",
        metadata = ["website","metadata"]
)
String inputJDBCTable

@JDBCColumnInput(
        title = "Input JDBCColumn",
        description = "A JDBCColumn input.",
        keywords = "input",
        jdbcTableReference = "input:jdbctable",
        excludedTypes = ["BOOLEAN"],
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "input:jdbccolumn",
        metadata = ["website","metadata"]
)
String[] inputJDBCColumn

@JDBCValueInput(
        title = "Input JDBCValue",
        description = "A JDBCValue input.",
        keywords = "input",
        jdbcColumnReference = "input:jdbccolumn",
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "input:jdbcvalue",
        metadata = ["website","metadata"]
)
String[] inputJDBCValue

@RawDataInput(
        title = "Input RawData",
        description = "A RawData input.",
        keywords = "input",
        isDirectory = false,
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "input:rawdata",
        metadata = ["website","metadata"]
)
String[] inputRawData

@PasswordInput(
        title = "Input Password",
        description = "A Password input.",
        keywords = "input",
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "input:password",
        metadata = ["website","metadata"]
)
String inputPassword

@LiteralDataInput(
        title = "Input LiteralDataDouble",
        description = "A LiteralDataDouble input.",
        keywords = "input",
        defaultDomain = "20",
        validDomains = ["20", "0;2;14"],
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "input:literaldatadouble",
        metadata = ["website","metadata"]
)
Double inputLiteralDouble = 10

@LiteralDataInput(
        title = "Input LiteralDataString",
        description = "A LiteralDataString input.",
        keywords = "input",
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "input:literaldatastring",
        metadata = ["website","metadata"]
)
String inputLiteralString = "dflt"

@BoundingBoxInput(
        title = "Input BoundingBoxData",
        description = "A BoundingBoxData input.",
        keywords = "input",
        supportedCRS = ["EPSG:4326", "EPSG:2000"],
        defaultCrs = "EPSG:4326",
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "input:boundingboxdata",
        dimension = 2,
        metadata = ["website","metadata"]
)
Geometry inputBoundingBox

/*****************/
/** OUTPUT Data **/
/*****************/

@EnumerationOutput(
        title = "Output Enumeration",
        description = "A Enumeration output.",
        keywords = "output",
        multiSelection = true,
        isEditable = true,
        values = "value1",
        names = ["name","name"],
        identifier = "output:enumeration",
        metadata = ["website","metadata"]
)
String[] outputEnumeration = ["value2"]

@GeometryOutput(
        title = "Output Geometry",
        description = "A Geometry output.",
        keywords = "output",
        dimension = 3,
        excludedTypes = ["MULTIPOINT", "POINT"],
        identifier = "output:geometry",
        metadata = ["website","metadata"]
)
String outputGeometry

@JDBCTableOutput(
        title = "Output JDBCTable",
        description = "A JDBCTable output.",
        keywords = "output",
        dataTypes = ["GEOMETRY"],
        identifier = "output:jdbctable",
        metadata = ["website","metadata"]
)
String outputJDBCTable

@JDBCColumnOutput(
        title = "Output JDBCColumn",
        description = "A JDBCColumn output.",
        keywords = "output",
        jdbcTableReference = "orbisgis:test:full:output:jdbctable",
        excludedTypes = ["BOOLEAN"],
        identifier = "output:jdbccolumn",
        metadata = ["website","metadata"]
)
String[] outputJDBCColumn

@JDBCValueOutput(
        title = "Output JDBCValue",
        description = "A JDBCValue output.",
        keywords = "output",
        jdbcColumnReference = "orbisgis:test:full:output:jdbccolumn",
        identifier = "output:jdbcvalue",
        metadata = ["website","metadata"]
)
String[] outputJDBCValue

@RawDataOutput(
        title = "Output RawData",
        description = "A RawData output.",
        keywords = "output",
        isDirectory = false,
        identifier = "output:rawdata",
        metadata = ["website","metadata"]
)
String[] outputRawData

@PasswordOutput(
        title = "Output Password",
        description = "A Password output.",
        keywords = "output",
        identifier = "output:password",
        metadata = ["website","metadata"]
)
String outputPassword

@LiteralDataOutput(
        title = "Output LiteralDataDouble",
        description = "A LiteralDataDouble output.",
        keywords = "output",
        defaultDomain = "20",
        validDomains = ["20", "0;2;14"],
        identifier = "output:literaldatadouble",
        metadata = ["website","metadata"]
)
Double outputLiteralDouble = 10

@LiteralDataOutput(
        title = "Output LiteralDataString",
        description = "A LiteralDataString output.",
        keywords = "output",
        identifier = "output:literaldatastring",
        metadata = ["website","metadata"]
)
String outputLiteralString = "dflt"

@BoundingBoxOutput(
        title = "Output BoundingBoxData",
        description = "A BoundingBoxData output.",
        keywords = "output",
        supportedCRS = ["EPSG:4326", "EPSG:2000"],
        defaultCrs = "EPSG:4326",
        identifier = "output:boundingboxdata",
        dimension = 2,
        metadata = ["website","metadata"]
)
Geometry outputBoundingBox

