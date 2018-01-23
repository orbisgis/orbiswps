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
package org.orbisgis.orbiswps.scripts.scripts.Table

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

/********************/
/** Process method **/
/********************/

/**
 * This process removes the given rows from the given table.
 * The user has to specify (mandatory):
 *  - The input table 
 *  - The primary key field 
 *  - The primary keys of the rows to remove 
 *
 * @author Sylvain PALOMINOS
 * @author Erwan Bocher
 */
@Process(
        title = "Delete rows",
        description = "Delete rows from a table.",
        keywords = ["Table","Delete"],
        properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
        version = "1.0",
        identifier = "orbisgis:wps:official:deleteRows"
)
def processing() {
    //Build the start of the query
    for (String s : pkToRemove) {
        String query = "DELETE FROM " + tableName + " WHERE " + pkField[0] + " = " + Long.parseLong(s)
        //Execute the query
        sql.execute(query)
    }
    literalOutput = i18n.tr("Delete done.")
}


/****************/
/** INPUT Data **/
/****************/

/** This JDBCTable is the input model source table. */
@JDBCTableInput(
        title = "Table",
        description = "The table to edit.",
        identifier = "tableName"
)
String tableName

/**********************/
/** INPUT Parameters **/
/**********************/

/** Name of the PrimaryKey field of the JDBCTable tableName. */
@JDBCColumnInput(
        title = "PKField",
        description = "The primary key column.",
        jdbcTableReference = "tableName",
        identifier = "pkField"
)
String[] pkField

/** List of primary keys to remove from the table. */
@JDBCValueInput(
        title = "Primary key values",
        description = "The array of the primary keys of the rows to remove.",
        jdbcColumnReference = "pkField",
        multiSelection = true,
        identifier = "pkToRemove"
)
String[] pkToRemove

/** Output message. */
@LiteralDataOutput(
        title = "Output message",
        description = "The output message.",
        identifier = "literalOutput")
String literalOutput

