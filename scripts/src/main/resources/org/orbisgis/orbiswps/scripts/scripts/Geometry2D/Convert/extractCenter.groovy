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
package org.orbisgis.orbiswps.scripts.scripts.Geometry2D.Convert

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

/**
 * This process extract the center of a geometry table using  SQL functions.
 * The user has to specify (mandatory):
 *  - The input spatial model source (JDBCTable)
 *  - The geometry column (LiteralData)
 *  - The geometry operation (centroid or interior point)
 *  - The output model source (JDBCTable)
 *
 * @return A datadase table.
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS
 */
@Process(
		title = "Extract center",
		description = "Extract the center of a geometry.",
		keywords = ["Vector","Geometry","Extract","Center"],
		properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
                version = "1.0")
def processing() {
    //Build the start of the query
    String query = "CREATE TABLE "+outputTableName+" AS SELECT "
    
    
    if(operation[0].equalsIgnoreCase("centroid")){
        query += " ST_Centroid("+geometricField[0]+""
    }
    else{
        query += " ST_PointOnSurface("+geometricField[0]+""
    }
    //Build the end of the query
    query += ") AS the_geom "
    
    
    for(String field : fieldList) {
        if (field != null) {
            query += ", " + field;
        }
    }
    
    query+=" FROM "+inputJDBCTable+";"
    
    if(dropTable){
	sql.execute "drop table if exists " + outputTableName
    }
    
    //Execute the query
    sql.execute(query)
    if(dropInputTable){
        sql.execute "drop table if exists " + inputJDBCTable
    }
    literalOutput = i18n.tr("Process done")
}


/****************/
/** INPUT Data **/
/****************/

/** This JDBCTable is the input model source. */
@JDBCTableInput(
		title = "Extract center",
		description = "Extract the center of a geometry.",
        dataTypes = ["GEOMETRY"])
String inputJDBCTable

/**********************/
/** INPUT Parameters **/
/**********************/

/** Name of the Geometric field of the JDBCTable inputJDBCTable. */
@JDBCColumnInput(
		title = "Geometric column",
		description = "The geometric column of the model source.",
        jdbcTableReference = "inputJDBCTable",
        dataTypes = ["GEOMETRY"])
String[] geometricField

/** Fields to keep. */
@JDBCColumnInput(
        title = "Columns to keep",
        description = "The columns that will be kept in the output.",
        excludedTypes=["GEOMETRY"],
        multiSelection = true,
        minOccurs = 0,
        jdbcTableReference = "inputJDBCTable")
String[] fieldList

@EnumerationInput(
		title = "Operation",
		description = "Operation to extract the points.",
        values=["centroid", "interior"],
        names=["Centroid", "Interior"])
String[] operation = ["centroid"]

@LiteralDataInput(
    title = "Drop the output table if exists",
    description = "Drop the output table if exists.")
Boolean dropTable 

@LiteralDataInput(
		title = "Output table name",
		description = "Name of the table containing the result of the process.")
String outputTableName


@LiteralDataInput(
    title = "Drop the input table",
    description = "Drop the input table when the script is finished.")
Boolean dropInputTable 


/*****************/
/** OUTPUT Data **/
/*****************/

/** String output of the process. */
@LiteralDataOutput(
		title = "Output message",
		description = "The output message.")
String literalOutput

