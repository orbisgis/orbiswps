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
package org.orbisgis.orbiswps.scripts.scripts.Export

import org.h2gis.api.DriverFunction
import org.h2gis.functions.io.dbf.DBFDriverFunction

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

import java.sql.Connection

/**
 * @author Erwan Bocher
 */
@Process(title = "Export DBF file",
    description = "Export a table to a DBF file.",
    keywords = ["OrbisGIS","Export","File","DBF"],
    properties = ["DBMS_TYPE", "H2GIS","DBMS_TYPE", "POSTGIS"],
    version = "1.0")
def processing() {
    File outputFile = new File(fileDataInput[0])    
    DriverFunction exp = new DBFDriverFunction()
    Connection con = sql.getDataSource()?sql.getDataSource().getConnection():sql.getConnection()
    exp.exportTable(con, inputJDBCTable, outputFile, progressMonitor)
    if(dropInputTable){
	    sql.execute "drop table if exists " + inputJDBCTable
    }
    literalDataOutput = i18n.tr("The DBF file has been created.")
}

/***********/
/** INPUT **/
/***********/

@JDBCTableInput(
    title = "Table to export",
    description = "The table that will be exported in a DBF file")
String inputJDBCTable


@LiteralDataInput(
    title = "Drop the input table",
    description = "Drop the input table when the export is finished.")
Boolean dropInputTable


@RawDataInput(
    title = "Output DBF",
    description = "The output DBF file to be exported.",
    fileTypes = ["dbf"],
    isDirectory = false)
String[] fileDataInput


/************/
/** OUTPUT **/
/************/

@LiteralDataOutput(
    title = "Output message",
    description = "Output message.")
String literalDataOutput
