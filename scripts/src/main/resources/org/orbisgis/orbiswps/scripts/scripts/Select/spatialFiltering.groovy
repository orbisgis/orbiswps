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
package org.orbisgis.orbiswps.scripts.scripts.Select

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*
import org.h2gis.utilities.SFSUtilities
import org.h2gis.utilities.TableLocation


/**
 * @author Erwan Bocher
 */
@Process(
    title = "Spatial filtering",
    description = "Select geometries from one table based on one other table.\nThe criteria for selecting features is based on the spatial relationship between eachfeature and the features in an additional layer. ",
	keywords = ["Vector","Geometry","Filtering"],
    properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
    version = "1.0",
    identifier = "orbisgis:wps:official:selectSpatial"
)
def processing() {
    
    //Build the start of the query
    String  outputTable = fromSelectedTable+"_filtered"

    if(outputTableName != null){
	outputTable  = outputTableName
    }

    String query = "CREATE TABLE " + outputTable + " AS SELECT b.*"
    query += " from " +  toSelectedTable+ " as a, "+ fromSelectedTable + " as b where " + operation[0]
    query += "(a."+ geometricFieldToSelected[0]+ ",b."+ geometricFieldFromSelected[0]+ ")"

    notGeomIndex = ["st_disjoint"]

    if(!notGeomIndex.contains(operation[0])){
    
        query +=" and a." + geometricFieldToSelected[0]+ "&& b."+ geometricFieldFromSelected[0]

    }  
    if(dropTable){
	sql.execute "drop table if exists " + outputTable
    }
    //Execute the query
    sql.execute(query)

    literalOutput = i18n.tr("Process done")
    
}

/****************/
/** INPUT Data **/
/****************/

@JDBCTableInput(
    title = "Table to select from",
    description = "The spatial model source that contains the selected features.",
    dataTypes = ["GEOMETRY"]
)
String fromSelectedTable


@JDBCColumnInput(
    title = "Geometric column from",
    description = "The geometric column of selected table.",
    jdbcTableReference = "fromSelectedTable",
    dataTypes = ["GEOMETRY"]
)
String[] geometricFieldFromSelected


@JDBCTableInput(
    title = "Table to select",
    description = "The spatial model source used to select the features.",
    dataTypes = ["GEOMETRY"]
)
String toSelectedTable

@JDBCColumnInput(
    title = "Geometric column to",
    description = "The geometric column of the mask table.",
    jdbcTableReference = "toSelectedTable",
    dataTypes = ["GEOMETRY"]
)
String[] geometricFieldToSelected


@EnumerationInput(
    title = "Spatial relationship",
    description = "Spatial relationship to select features.",
    values=["st_intersects", "st_contains", "st_disjoint", "st_crosses", "st_touches"],
    names=["Intersects","Contains","Disjoint","Crosses","Touches"])
String[] operation = ["st_intersects"]



@LiteralDataInput(
    title = "Drop the output table if exists",
    description = "Drop the output table if exists.",
    minOccurs = 0)
Boolean dropTable 

@LiteralDataInput(
    title = "Output table prefix",
    description = "Prefix of the table containing the result of the process.",
    minOccurs = 0,
    identifier = "outputTableName"
)
String outputTableName



/** String output of the process. */
@LiteralDataOutput(
    title = "Output message",
    description = "The output message.",
    identifier = "literalOutput"
)
String literalOutput


