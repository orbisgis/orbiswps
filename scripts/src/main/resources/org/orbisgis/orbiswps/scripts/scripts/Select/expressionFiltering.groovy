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
    title = "Expression filtering",
    description = "Select rows from one table based on a SQL expression. ",
    keywords = "Filtering",
    properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
    version = "1.0",
    identifier = "orbisgis:wps:official:selectionExpression"
)
def processing() {
    
    //Build the start of the query
    String  outputTable = fromSelectedTable+"_filtered"

    if(outputTableName != null){
	outputTable  = outputTableName
    }

    String query = "CREATE TABLE " + outputTable + " AS SELECT a.*"
    query += " from " +  fromSelectedTable+ " as a   " + fromSelectedValue
 
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
    description = "The model source that contains the selected features.")
String fromSelectedTable



@LiteralDataInput(
    title = "SQL expression",
    description = "Write here a valid where SQL expression.")
String fromSelectedValue  



@LiteralDataInput(
    title = "Drop the output table if exists",
    description = "Drop the output table if exists.",
    minOccurs = 0)
Boolean dropTable 

@LiteralDataInput(
    title = "Output table name",
    description = "Name of the table containing the result of the process.",
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


