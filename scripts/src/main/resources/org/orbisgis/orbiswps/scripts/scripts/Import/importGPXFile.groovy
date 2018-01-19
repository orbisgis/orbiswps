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
package org.orbisgis.orbiswps.scripts.scripts.Import

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

/**
 * @author Erwan Bocher
 */
@Process(title = "Import a GPX file",
    description = "Import a GPX file from path and creates several tables prefixed by tableName representing the file’s contents.\n Please go to  http://www.h2gis.org",
    keywords = ["OrbisGIS","Import","File","GPX"],
    properties = ["DBMS_TYPE","H2GIS"],
    version = "1.0")
def processing() {
    File fileData = new File(fileDataInput[0])
    name = fileData.getName()
    tableName = name.substring(0, name.lastIndexOf(".")).toUpperCase()
    query = "CALL GPXRead('"+ fileData.absolutePath+"','"
    if(jdbcTableOutputName != null){
	    tableName = jdbcTableOutputName
    }
    
    query += tableName+"')"	    

    sql.execute query
    

    literalDataOutput = i18n.tr("The GPX file has been imported.")
}


@RawDataInput(
    title = "Input GPX",
    description = "The input GPX file to be imported.",
    fileTypes = ["gpx"],
    isDirectory = false)
String[] fileDataInput



/** Optional table name. */
@LiteralDataInput(
    title = "Prefix for all tables",
    description = "Prefix for all table names to store the GPX file.",
    minOccurs = 0)
String jdbcTableOutputName





/************/
/** OUTPUT **/
/************/
@LiteralDataOutput(
    title = "Output message",
    description = "Output message.")
String literalDataOutput
