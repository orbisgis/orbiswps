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
package org.orbisgis.orbiswps.scripts.scripts.Geometry2D.Aggregate

import org.orbisgis.orbiswps.groovyapi.input.JDBCColumnInput
import org.orbisgis.orbiswps.groovyapi.input.JDBCTableInput
import org.orbisgis.orbiswps.groovyapi.input.LiteralDataInput
import org.orbisgis.orbiswps.groovyapi.output.JDBCTableOutput
import org.orbisgis.orbiswps.groovyapi.process.Process

/**
 * This process aggregate geometries using the union operator
 *
 * @author Erwan Bocher (CNRS)
 */
@Process(
		title = "Union",
		description = "Calculates the geometric union of any number of geometries.<br> Note : The unioned geometries\
                 are exploded to its constituent parts",
		keywords = ["Vector","Geometry", "Union"],
		properties = ["DBMS_TYPE", "H2GIS","DBMS_TYPE", "POSTGIS"],
        identifier = "orbisgis:wps:official:union",
        version = "1.0")
def processing() {
    def the_geom = geometricField[0]
    def query = "CREATE TABLE ${outputTableName} AS SELECT  "
    if (isH2) {
        query += "EXPLOD_ID as id"
        if (groupby?.empty) {
            query += ",${groupby.join(",")}"
        }
        query += ",the_geom FROM st_explode('(select ST_UNION(ST_ACCUM(" //Use a distance
        if (buffer > 0) {
            query += "st_buffer(${the_geom}, ${buffer}))) as the_geom "
        } else {
            query += " ${the_geom})) as the_geom"
        }
        if (groupby?.empty) {
            query += ",${groupby.join(",")} FROM  ${inputTable} "
            query += " group by ${groupby.join(",")}"
            query += ")')"
        } else {
            query += " FROM ${inputTable} )')"
        }
    } else {
        query += "row_number() OVER () AS id, the_geom "
        if (groupby?.empty) {
            query += ", ${groupby.join(",")}"
        }
        query += " from (select (ST_Dump( ST_Union("
        //Use a distance
        if (buffer > 0) {
            query += "st_buffer(${the_geom}, ${buffer})))).geom as the_geom"
        } else {
            query += " ${the_geom}))).geom as the_geom"
        }
        if (groupby?.empty) {
            query += ",${groupby.join(",")} FROM ${inputTable}"
            query += "  group by ${groupby.join(",")}"
            query += " ) as foo"
        } else {
            query += "  FROM ${inputTable}) as foo"
        }
    }
    if (dropOutputTable) {
        sql.execute("drop table if exists ${outputTableName}".toString())
    }
    sql.execute(query.toString())
    if (dropInputTable) {
        sql.execute("drop table if exists ${inputTable}".toString())
    }
    outputJDBCTable = outputTableName
}


/****************/
/** INPUT Data **/
/****************/

@JDBCTableInput(
		title = "Input table",
		description = "Table that contains the input geometries.",
        dataTypes = ["GEOMETRY"],
        identifier = "inputTable")
String inputTable

/**********************/
/** INPUT Parameters **/
/**********************/

/** Name of the Geometric field of the JDBCTable inputTable. */
@JDBCColumnInput(
		title = "Geometric column",
		description = "The geometric column of the input table.",
        jdbcTableReference = "inputTable",
        dataTypes = ["GEOMETRY"],
        identifier = "geometricField")
String[] geometricField


@LiteralDataInput(
        title = "Buffering",
        description = "Apply a buffer arround the geometries before unioning it.",
        identifier = "buffering")
double buffer = 0

/** Fields to keep. */
@JDBCColumnInput(
        title = "Columns to group",
        description = "The columns that will be used to group the geometry.",
        excludedTypes=["GEOMETRY"],
        multiSelection = true,
        minOccurs = 0,
        jdbcTableReference = "inputTable",
        identifier = "groupBy")
String[] groupby


@LiteralDataInput(
        title = "Drop the output table if exists",
        description = "Drop the output table if exists.",
        identifier = "dropOutput")
Boolean dropOutputTable 

@LiteralDataInput(
		title = "Output table name",
		description = "Name of the table containing the result of the process.",
        identifier = "outputName")
String outputTableName


@LiteralDataInput(
        title = "Drop the input table",
        description = "Drop the input table when the script is finished.",
        identifier = "dropInput")
Boolean dropInputTable 


/*****************/
/** OUTPUT Data **/
/*****************/

@JDBCTableOutput(
        title = "output table",
        description = "Table that contains the output.",
        identifier = "outputTable")
String outputJDBCTable