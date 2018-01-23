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
package org.orbisgis.orbiswps.scripts.scripts.Geometry2D.Properties

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

/********************/
/** Process method **/
/********************/



/**
 * This process extract the center of a geometry table using the SQL function.
 * The user has to specify (mandatory):
 *  - The input spatial model source (JDBCTable)
 *  - The geometry column (LiteralData)
 *  - A column identifier (LiteralData)
 *  - The geometry operations (centroid or interior point)
 *  - The output model source (JDBCTable)
 *
 * @return A database table or a file.
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS
 */
@Process(
        title = "Geometry properties",
        description = "Compute some basic geometry properties.",
        keywords = ["Vector","Geometry","Properties"],
        properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
        version = "1.0",
        identifier = "orbisgis:wps:official:geometryProperties"
)
def processing() {
//Build the start of the query
    String query = "CREATE TABLE "+outputTableName+" AS SELECT "

    for (String operation : operations) {
        if(operation.equals("geomtype")){
            query += " ST_GeometryType("+geometricField[0]+") as geomType,"
        }
        else if(operation.equals("srid")){
            query += " ST_SRID("+geometricField[0]+") as srid,"
        }
        else if(operation.equals("length")){
            query += " ST_Length("+geometricField[0]+") as length,"
        }
        else if(operation.equals("perimeter")){
            query += " ST_Perimeter("+geometricField[0]+") as perimeter,"
        }
        else if(operation.equals("area")){
            query += " ST_Area("+geometricField[0]+") as area,"
        }
        else if(operation.equals("dimension")){
            query += " ST_Dimension("+geometricField[0]+") as dimension,"
        }
        else if(operation.equals("coorddim")){
            query += " ST_Coorddim("+geometricField[0]+") as coorddim,"
        }
        else if(operation.equals("num_geoms")){
            query += " ST_NumGeometries("+geometricField[0]+") as numGeometries,"
        }
        else if(operation.equals("num_pts")){
            query += " ST_NPoints("+geometricField[0]+") as numPts,"
        }
        else if(operation.equals("issimple")){
            query += " ST_Issimple("+geometricField[0]+") as issimple,"
        }
        else if(operation.equals("isvalid")){
            query += " ST_Isvalid("+geometricField[0]+") as isvalid,"
        }
        else if(operation.equals("isempty")){
            query += " ST_Isempty("+geometricField[0]+") as isempty,"
        }
    }


    //Add the field id
    query += idField[0] + " FROM "+inputJDBCTable+";"

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
        title = "Input spatial model",
        description = "The spatial model source to compute the geometry properties.",
        dataTypes = ["GEOMETRY"],
        identifier = "inputJDBCTable")
String inputJDBCTable

/**********************/
/** INPUT Parameters **/
/**********************/

/** Name of the Geometric field of the JDBCTable inputJDBCTable. */
@JDBCColumnInput(
        title = "Geometric column",
        description = "The geometric column of the model source.",
        jdbcTableReference = "inputJDBCTable",
        identifier = "geometricField",
        dataTypes = ["GEOMETRY"])
String[] geometricField

/** Name of the identifier field of the JDBCTable inputJDBCTable. */
@JDBCColumnInput(
        title = "Column identifier",
        description = "A column used as an identifier.",
	    excludedTypes=["GEOMETRY"],
        jdbcTableReference = "inputJDBCTable",
        identifier = "idField")
String[] idField

@EnumerationInput(
        title = "Operation",
        description = "Operation to compute the properties.",
        values=["geomtype","srid", "length","perimeter","area", "dimension", "coorddim", "num_geoms", "num_pts", "issimple", "isvalid", "isempty"],
        names = ["Geometry type","SRID","Length","Perimeter","Area","Geometry dimension","Coordinate dimension","Number of geometries","Number of points","Is simple","Is valid","Is empty"],
        multiSelection = true,
        identifier = "operations")
String[] operations = ["geomtype"]

@LiteralDataInput(
    title = "Drop the output table if exists",
    description = "Drop the output table if exists.")
Boolean dropTable 

@LiteralDataInput(
        title = "Output table name",
        description = "Name of the table containing the result of the process.",
        identifier = "outputTableName")
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
        description = "The output message.",
        identifier = "literalOutput")
String literalOutput

