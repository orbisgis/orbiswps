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
package org.orbisgis.orbiswps.scripts.scripts.Geometry2D.Topology

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*
/********************/
/** Process method **/
/********************/

/**
 * This process compute the shared part of a geometry with each other
 * 
 * @author Erwan BOCHER
 */
@Process(
        title = "Shared part",
        description = "Compute the shared part of a geometry with each other.",
        keywords = ["Vector","Geometry", "Topology"],
        properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
        version = "1.0")
    def processing() {
    
    def the_geom = geometricField[0] ;
    def id = isH2?"_rowid_":"ctid"
    
    if(fieldId?.empty){
        id = fieldId[0]
    }
    
    String query = "CREATE TABLE ${outputTableName} as select st_intersection(a.${the_geom}, b.${the_geom}) as  ${the_geom},\n\
 a.${id} as first_id, b.${id} as second_id FROM ${inputJDBCTable} as a,${inputJDBCTable} as b where a.${id}<>b.${id} and a.${id}<b.${id} AND a.${the_geom} && b.${the_geom} AND st_intersects(a.${the_geom},b.${the_geom})" 
    
    
    if(dropOutputTable){
	sql.execute "drop table if exists " + outputTableName
    }
    if(useSpatialIndex){
        def spatialIndexQuery = isH2?"create spatial index on ${inputJDBCTable}(${the_geom})":"create index on ${inputJDBCTable}  USING GIST(${the_geom})"
        sql.execute(spatialIndexQuery.toString())
    }
    
    //Execute the query
    sql.execute(query.toString())
    
    if(dropInputTable){
        sql.execute "drop table if exists " + inputJDBCTable
    }
    
    literalOutput = i18n.tr("Process done")
}


/****************/
/** INPUT Data **/
/****************/

/** This JDBCTable is the input model source for the buffer. */
@JDBCTableInput(
        title = "Input table",
        description = "The spatial table to compute the shared parts.",
        dataTypes = "GEOMETRY")
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

/** Identifier to keep. */
@JDBCColumnInput(
        title = "Column identifier",
        description = "The column identifier used to link to the source geometries.",
        excludedTypes=["GEOMETRY"],
        multiSelection = false,
        minOccurs = 0,
        jdbcTableReference = "inputJDBCTable")
String[] fieldId

@LiteralDataInput(
    title = "Create a spatial index",
    description = "Create a spatial index to optimize the process. ")
Boolean useSpatialIndex 

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

