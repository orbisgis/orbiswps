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

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

import org.h2gis.functions.io.geojson.GeoJsonDriverFunction
import org.h2gis.api.DriverFunction

import java.sql.Connection


/**
 * @author Erwan Bocher
 */
@Process(title = ["Export into a GeoJSON","en","Exporter dans un fichier GeoJSON","fr"],
    description = ["Export a table to a GeoJSON.","en",
                "Exporter une table dans un fichier GeoJSON.","fr"],
    keywords = ["OrbisGIS,Exporter, Fichier, GeoJSON","fr",
                "OrbisGIS,Export, File, GeoJSON","en"],
    properties = ["DBMS_TYPE", "H2GIS","DBMS_TYPE", "POSTGIS"],
    version = "1.0")
def processing() {
    File outputFile = new File(fileDataInput[0])
    DriverFunction exp = new GeoJsonDriverFunction()
    Connection con = sql.getDataSource()?sql.getDataSource().getConnection():sql.getConnection()
    exp.exportTable(con, inputJDBCTable, outputFile, progressMonitor)
    if(dropInputTable){
	    sql.execute "drop table if exists " + inputJDBCTable
    }
    literalDataOutput = "The GeoJSON file has been created."
}


/***********/
/** INPUT **/
/***********/

@JDBCTableInput(
    title = [
                "Table to export","en",
                "Table à exporter","fr"],
    description = [
                "The table that will be exported in a GeoJSON file","en",
                "La table à exporter dans un fichier GeoJSON.","fr"],
    dataTypes = ["GEOMETRY"])
String inputJDBCTable



@LiteralDataInput(
    title = [
				"Drop the input table","en",
				"Supprimer la table d'entrée","fr"],
    description = [
				"Drop the input table when the export is finished.","en",
				"Supprimer la table d'entrée à l'issue l'export.","fr"])
Boolean dropInputTable


@RawDataInput(
        title = ["Output GeoJSON","en","Fichier GeoJSON","fr"],
        description = ["The output GeoJSON file to be exported.","en",
                "Nom du fichier GeoJSON à exporter.","fr"],
        fileTypes = ["geojson"],
        isDirectory = false)
String[] fileDataInput


/************/
/** OUTPUT **/
/************/

@LiteralDataOutput(
    title = ["Output message","en",
                "Message de sortie","fr"],
    description = ["Output message.","en",
                "Message de sortie.","fr"])
String literalDataOutput
