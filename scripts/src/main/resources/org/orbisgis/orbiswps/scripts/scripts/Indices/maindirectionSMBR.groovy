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
package org.orbisgis.orbiswps.scripts.scripts.Indices

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*
import org.h2gis.utilities.SFSUtilities
import org.h2gis.utilities.TableLocation


/**
 * @author Erwan Bocher
 */
@Process(
    title = "Main direction SMBR",
    description = "Compute the main direction (in degree) of a polygon, based on its Smallest Minimum Bounding Rectangle (SMBR).\n\
<p>The north is equal to 0°. Values are clockwise, so East = 90°.</p>\n\
<p>The value is ”modulo pi” expressed → the value is between 0 and 180°(e.g 355° becomes 175°).</p>\n\
<p><em>Bibliography:</em></p><p>Duchêne, C., Bard, S., Barillot, X., Ruas, A., Trévisan, J., and Holzapfel, F. (2003). Quantitative and qualitative description of building orientation, In Fifth workshop on progress in automated map generalisation, ICA, commission on map generalisation.</p>",
	keywords = ["Vector","Geometry","Index"],
    properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
    version = "1.0",
    identifier = "orbisgis:wps:official:mainDirectionSMBR"
)
def processing() {
    
    //Build the start of the query
    String  outputTable = inputTable+"_mainDirectionSMBR"

    if(outputTableName != null){
	outputTable  = outputTableName
    }

    String query = "CREATE TABLE " + outputTable + " AS SELECT "

    if(keepgeom==true){
        query+= geometryColumn[0]+","
    }
    query += idField[0]+",mod(CASE WHEN ST_LENGTH ( ST_MINIMUMDIAMETER("+geometryColumn[0]+ ") ) <0.1  THEN DEGREES( ST_AZIMUTH ( ST_STARTPOINT ("+geometryColumn[0]+") , ST_ENDPOINT ("+geometryColumn[0]+ ") ) ) ELSE DEGREES(ST_AZIMUTH (ST_STARTPOINT ( ST_ROTATE (ST_MINIMUMDIAMETER("+geometryColumn[0]+") , pi () /2) ) , ST_ENDPOINT ( ST_ROTATE ( ST_MINIMUMDIAMETER ("+geometryColumn[0]+") , pi () /2) ) ) ) END, 180) as maindirectionsmbr from "+ inputTable
    
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
    title = "Input table",
    description = "The spatial model source that contains the polygons.",
    dataTypes = ["POLYGON", "MULTIPOLYGON"]
)
String inputTable


@JDBCColumnInput(
    title = "Geometric column",
    description = "The geometric column of input table.",
    jdbcTableReference = "inputTable",
    dataTypes = ["POLYGON", "MULTIPOLYGON"]
)
String[] geometryColumn

/** Name of the identifier field of the JDBCTable inputJDBCTable. */
@JDBCColumnInput(
    title = "Column identifier",
    description = "A column used as an identifier.",
    excludedTypes=["GEOMETRY"],
    multiSelection = false,
    jdbcTableReference = "inputTable")
String[] idField

@LiteralDataInput(
    title = "Keep the geometry",
    description = "Keep the input geometry in the result table.",
    minOccurs = 0)
Boolean keepgeom;


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


