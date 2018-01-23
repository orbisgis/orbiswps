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
package org.orbisgis.orbiswps.scripts.scripts.Geometry2D.Transform

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*
import org.h2gis.utilities.SFSUtilities
import org.h2gis.utilities.TableLocation


/**
 * This process reproject a geometry table using the SQL function.
 * The user has to specify (mandatory):
 *  - The input spatial model source (JDBCTable)
 *  - The geometry column (LiteralData)
 *  - The SRID value selected from the spatial_ref table
 *  - The output model source (JDBCTable)
 *
 * @return A database table or a file.
 * @author Erwan Bocher
 */
@Process(
		title = "Reproject geometries",
		description = "Reproject geometries from one Coordinate Reference System to another.",
		keywords = ["Vector","Geometry","Reproject"],
		properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
                version = "1.0",
		identifier = "orbisgis:wps:official:reprojectGeometries"
)
def processing() {
    TableLocation table = TableLocation.parse(inputJDBCTable, ish2)
    int srid = SFSUtilities.getSRID(sql.getConnection(),table, geometricField[0])
    if(srid==0){
        logger.warn(i18n.tr("The input table must contains a SRID constraint."))
        literalOutput = i18n.tr("Fail to execute the process")
    }
    else{
		//Build the start of the query
		String query = "CREATE TABLE " + outputTableName + " AS SELECT ST_TRANSFORM("
		query += geometricField[0] + "," + srid[0]

		//Build the end of the query
		query += ") AS the_geom ";

		for (String field : fieldList) {
			if (field != null) {
				query += ", " + field;
			}
		}

		query +=  " FROM "+inputJDBCTable+";"

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
}

/****************/
/** INPUT Data **/
/****************/

/** This JDBCTable is the input model source. */
@JDBCTableInput(
		title = "Input spatial model",
		description = "The spatial model source to be reprojected.",
		dataTypes = ["GEOMETRY"],
		identifier = "inputJDBCTable"
)
String inputJDBCTable


/**********************/
/** INPUT Parameters **/
/**********************/

/** Name of the Geometric field of the JDBCTable inputJDBCTable. */
@JDBCColumnInput(
		title = "Geometric column",
		description = "The geometric field of the model source.",
        jdbcTableReference = "inputJDBCTable",
        dataTypes = ["GEOMETRY"],
		identifier = "geometryField"
)
String[] geometricField


/** The spatial_ref SRID */
@JDBCValueInput(
		title = "SRID",
		description = "The spatial reference system identifier.",
		jdbcColumnReference = "\$public\$spatial_ref_sys\$srid\$",
		multiSelection = false,
		identifier = "srid"
)
String[] srid


/** Fields to keep. */
@JDBCColumnInput(
		title = "Columns to keep",
		description = "The columns that will be kept in the output.",
		excludedTypes=["GEOMETRY"],
		multiSelection = true,
		minOccurs = 0,
        	jdbcTableReference = "inputJDBCTable",
		identifier = "fieldList"
)
String[] fieldList

@LiteralDataInput(
    title = "Drop the output table if exists",
    description = "Drop the output table if exists.")
Boolean dropTable 

@LiteralDataInput(
		title = "Output table name",
		description = "Name of the table containing the result of the process.",
		identifier = "outputTableName"
)
String outputTableName


@LiteralDataInput(
    title = "Drop the input table",
    description = "Drop the input table when the script is finished.")
Boolean dropInputTable 



/** String output of the process. */
@LiteralDataOutput(
		title = "Output message",
		description = "The output message.",
		identifier = "literalOutput"
)
String literalOutput


