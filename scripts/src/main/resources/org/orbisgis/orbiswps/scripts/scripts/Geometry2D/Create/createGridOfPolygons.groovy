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
package org.orbisgis.orbiswps.scripts.scripts.Geometry2D.Create

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

/********************/
/** Process method **/
/********************/

/**
 * This process is used to create a grid of polygons.
 *
 * @return A datadase table.
 * @author Erwan BOCHER
 * @author Sylvain PALOMINOS
 */
@Process(
        title = "Create a grid of polygons",
        description = "Create a grid of polygons.",
        keywords = ["Vector","Geometry","Create"],
        properties = ["DBMS_TYPE", "H2GIS"],
        version = "1.0")
def processing() {

    //Build the start of the query
    String query = "CREATE TABLE "+outputTableName+" AS SELECT * from ST_MakeGrid('"+inputJDBCTable+"',"+x_distance+","+y_distance+")"
    
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

@JDBCTableInput(
        title = "Input spatial model",
        description = "The spatial model source to compute the grid. The extend of grid is based on the full extend of the table.",
        dataTypes = ["GEOMETRY"])
String inputJDBCTable

/**********************/
/** INPUT Parameters **/
/**********************/

@LiteralDataInput(
        title = "X cell size",
        description = "The X cell size.")
Double x_distance =1

@LiteralDataInput(
        title = "Y cell size",
        description = "The Y cell size.")
Double y_distance =1


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

