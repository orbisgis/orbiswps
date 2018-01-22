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
package org.orbisgis.orbiswps.scripts.scripts.Geometry2D.Buffer

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*
/********************/
/** Process method **/
/********************/

/**
 * This process execute a buffer on a spatial model source using the ST_Buffer() function.
 * The user has to specify (mandatory):
 *  - The input spatial model source (JDBCTable)
 *  - The BufferSize (LiteralData)
 *  - The output model source (JDBCTable)
 *
 * The user can specify (optional) :
 *  - The number of segments used to approximate a quarter circle (LiteralData)
 *  - The endcap style (Enumeration)
 *  - The join style (Enumeration)
 *  - The mitre ratio limit (only affects mitered join style) (LiteralData)
 *
 * @return A datadase table.
 * @author Sylvain PALOMINOS
 * @author Erwan BOCHER
 */
@Process(
        title = "Fixed distance buffer",
        description = "Execute a buffer on a geometric field with a constant distance.",
        keywords = ["Vector","Geometry"],
        properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
        version = "1.0")
def processing() {

    //Build the start of the query
    String query = "CREATE TABLE "+outputTableName+" AS SELECT ST_Buffer("+geometricField[0]+","+bufferSize
    //Build the third optional parameter
    String optionalParameter = "";
    //If quadSegs is defined
    if(quadSegs != null){
        optionalParameter += "quad_segs="+quadSegs+" "
    }
    //If endcapStyle is defined
    if(endcapStyle != null){
        optionalParameter += "endcap="+endcapStyle[0]+" "
    }
    //If joinStyle is defined
    if(joinStyle != null){
        optionalParameter += "join="+joinStyle[0]+" "
    }
    //If mitreLimit is defined
    if(mitreLimit != null){
        optionalParameter += "mitre_limit="+mitreLimit+" "
    }
    //If optionalParameter is not empty, add it to the request
    if(!optionalParameter.isEmpty()){
        query += ",'"+optionalParameter+"'";
    }
    //Build the end of the query
    query += ") AS the_geom";
    for(String field : fieldList) {
        if (field != null) {
            query += ", " + field;
        }
    }

    query+=" FROM "+inputJDBCTable+";"

    if(dropOutputTable){
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

/** This JDBCTable is the input model source for the buffer. */
@JDBCTableInput(
        title = "Input table",
        description = "The spatial table  for the buffer.",
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

/** Size of the buffer. */
@LiteralDataInput(
        title = "Buffer size",
        description = "The buffer size.")
Double bufferSize 

/** Mitre ratio limit (only affects mitered join style). */
@LiteralDataInput(
        title = "Mitre limit",
        description = "Mitre ratio limit (only affects mitered join style)",
        minOccurs = 0)
Double mitreLimit = 5.0

/** Number of segments used to approximate a quarter circle. */
@LiteralDataInput(
        title = "Segment number for a quarter circle",
        description = "Number of segments used to approximate a quarter circle.",
        minOccurs = 0)
Integer quadSegs = 8

/** Endcap style. */
@EnumerationInput(
        title = "Endcap style",
        description = "The endcap style.",
        values=["round", "flat", "butt", "square"],
        minOccurs = 0)
String[] endcapStyle = ["round"]

/** Join style. */
@EnumerationInput(
        title = "Join style",
        description = "The join style.",
        values=["round", "mitre", "miter", "bevel"],
        minOccurs = 0)
String[] joinStyle = ["round"]

/** Fields to keep. */
@JDBCColumnInput(
        title = "Columns to keep",
        description = "The columns that will be kept in the output.",
        excludedTypes=["GEOMETRY"],
        multiSelection = true,
        minOccurs = 0,
        jdbcTableReference = "inputJDBCTable")
String[] fieldList

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

