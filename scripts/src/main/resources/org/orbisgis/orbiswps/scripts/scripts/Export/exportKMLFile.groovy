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

import org.h2gis.functions.io.kml.KMLDriverFunction
import org.orbisgis.orbiswps.groovyapi.input.JDBCTableInput
import org.orbisgis.orbiswps.groovyapi.input.LiteralDataInput
import org.orbisgis.orbiswps.groovyapi.input.RawDataInput
import org.orbisgis.orbiswps.groovyapi.output.RawDataOutput
import org.orbisgis.orbiswps.groovyapi.process.Process

/**
 * Export the table 'inputTable' into the KML file 'fileDataInput'.
 * If 'dropInputTable' set to true, drop the table 'inputTable'.
 * The KML file is returned as the output 'fileDataOutput'.
 *
 * @author Erwan Bocher (CNRS)
 */
@Process(title = "Export into a KML file",
        description = "Export a table to a KML file.",
        keywords = ["OrbisGIS","Export","File","KML"],
        properties = ["DBMS_TYPE", "H2GIS","DBMS_TYPE", "POSTGIS"],
        identifier = "orbisgis:wps:official:exportKml",
        version = "1.0")
def processing() {
    def outputFile = new File(fileDataInput[0].toString())
    def exp = new KMLDriverFunction()
    def con = sql.getDataSource() ? sql.getDataSource().getConnection() : sql.getConnection()
    exp.exportTable(con, inputTable, outputFile, progressMonitor)

    if (dropInputTable) {
        sql.execute "drop table if exists ${inputTable}"
    }
    fileDataOutput = [outputFile.getAbsolutePath()]
}

/***********/
/** INPUT **/
/***********/

@JDBCTableInput(
        title = "Table to export",
        description = "The table that will be exported in a KML file",
        dataTypes = ["GEOMETRY"],
        identifier = "inputTable")
String inputTable

@LiteralDataInput(
        title = "Drop the input table",
        description = "Drop the input table when the export is finished.",
        identifier = "dropInputTable")
Boolean dropInputTable

@RawDataInput(
        title = "KML file",
        description = "The KML file where the table is exported.",
        fileTypes = ["kml", "kmz"],
        isDirectory = false,
        identifier = "kmlFile")
String[] fileDataInput


/************/
/** OUTPUT **/
/************/

@RawDataOutput(
        title = "Output KML",
        description = "The output KML file.",
        fileTypes = ["kml"],
        isDirectory = false,
        identifier = "outputFile")
String[] fileDataOutput
