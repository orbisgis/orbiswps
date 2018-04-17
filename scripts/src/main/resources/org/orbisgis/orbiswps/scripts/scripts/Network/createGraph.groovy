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
package org.orbisgis.orbiswps.scripts.scripts.Network

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

/**
 * Create a graph stored in two tables nodes and edges from an input table that contains Multi or LineString.
 * If the input table has name 'input', then the output tables are named 'input_nodes' and 'input_edges'.
 *
 * @author Erwan BOCHER (CNRS)
 * @author Sylvain PALOMINOS (UBS 2018)
 */
@Process(
        title = "Create a graph",
        description = "Create a graph stored in two tables nodes and edges from an input table that contains Multi \
                or LineString.<br>\
                If the input table has name 'input', then the output tables are named 'input_nodes' and 'input_edges'.",
        keywords = ["Network","Geometry"],
        properties = ["DBMS_TYPE", "H2GIS"],
        version = "1.0")
def processing() {
    if (slope == null) {
        slope = false
    }

    def query = " SELECT ST_GRAPH('${inputTable}', '${geometricField[0]}',${tolerance}, ${slope})"

    //Execute the query
    sql.execute(query.toString())

    if (dropInputTable) {
        sql.execute("drop table if exists ${inputTable}".toString())
    }

    outputJDBCTable = outputTableName
}

/****************/
/** INPUT Data **/
/****************/

/** This JDBCTable is the input model source. */
@JDBCTableInput(
        title = "Input spatial model",
        description = "The spatial model source to create the graphe tables.",
        dataTypes = ["LINESTRING", "MULTILINESTRING"])
String inputTable


/** Name of the Geometric field of the JDBCTable inputTable. */
@JDBCColumnInput(
        title = "Geometric column",
        description = "The geometric column of the model source.",
        jdbcTableReference = "inputTable",
        dataTypes = ["LINESTRING", "MULTILINESTRING"])
String[] geometricField

/** Snapping tolerance. */
@LiteralDataInput(
        title = "Snapping tolerance",
        description = "The tolerance value is used specify the side length of a square Envelope around each node \
used to snap together other nodes within the same Envelope.")
Double tolerance

@LiteralDataInput(
        title = "Slope orientation ?",
        description = "True if edges should be oriented by the z-value of their first and last coordinates \
(decreasing).",
        minOccurs = 0)
Boolean slope

@LiteralDataInput(
    title = "Drop the input table",
    description = "Drop the input table when the script is finished.")
Boolean dropInputTable


/*****************/
/** OUTPUT Data **/
/*****************/

@JDBCTableOutput(
        title = "output table",
        description = "Table that contains the output.",
        identifier = "outputTable")
String outputJDBCTable



