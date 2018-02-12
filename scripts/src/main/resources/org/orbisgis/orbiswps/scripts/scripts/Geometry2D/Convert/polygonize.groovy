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
 * This process converts geometries to polygons
 *
 * @author Erwan Bocher
 */
@Process(
		title = "Polygonize",
		description = "Convert a set of 2 or 3 dimensional geometries to polygons.\n\
                Optionally, polygonize extracts points where lines intersect and uses them,together with points from the original lines, to construct polygons",
		keywords = ["Vector","Geometry", "Convert"],
		properties = ["DBMS_TYPE", "H2GIS","DBMS_TYPE", "POSTGIS"],
                version = "1.0")
def processing() {
    the_geom = geometricField[0]     
    String query = "CREATE TABLE ${outputTableName} AS SELECT "    
    
    if(isH2){
        query+= " explod_id as id, the_geom from ST_EXPLODE('(SELECT "
        if(node){
            query +=  "st_polygonize(st_union(st_precisionreducer(st_node(st_accum(${the_geom})), 3))) as the_geom from ${inputJDBCTable} where st_dimension(${the_geom})>0)')"    
        }else{
            query +=  "st_polygonize(st_accum(${the_geom})) as the_geom from ${inputJDBCTable} where st_dimension(${the_geom})>0)')"    
        }
    }
    else{
        query+= " row_number() OVER () AS id, t.the_geom from "
        if(node){  
            query +=  "(select (st_dump(st_polygonize(ST_UnaryUnion(ST_SnapToGrid(${the_geom}, 0.001)))).geom as the_geom from  ${inputJDBCTable} where st_dimension(${the_geom})>0) as t"
        }else{
            query +=  "(select (st_dump(st_accum(${the_geom}))).geom as the_geom from  ${inputJDBCTable} where st_dimension(${the_geom})>0) as t"
        }
    }    
    
    if(dropOutputTable){
	sql.execute "drop table if exists ${outputTableName}".toString()
    }
    
    sql.execute(query)
    if(dropInputTable){
        sql.execute "drop table if exists ${inputJDBCTable}"
    }
    literalOutput = i18n.tr("Process done")
}


/****************/
/** INPUT Data **/
/****************/

@JDBCTableInput(
		title = "Table to polygonize",
		description = "The table to polygonize.",
        dataTypes = ["GEOMETRY"])
String inputJDBCTable

/**********************/
/** INPUT Parameters **/
/**********************/

/** Name of the Geometric field of the JDBCTable inputJDBCTable. */
@JDBCColumnInput(
		title = "Geometric column",
		description = "The geometric column of the input table.",
        jdbcTableReference = "inputJDBCTable",
        dataTypes = ["GEOMETRY"])
String[] geometricField

@LiteralDataInput(
    title = "Use line intersections ",
    description = "Extracts points where lines intersect.")
Boolean node 


@LiteralDataInput(
    title = "Drop the output table if exists",
    description = "Drop the output table if exists.")
Boolean dropOutputTable 

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
