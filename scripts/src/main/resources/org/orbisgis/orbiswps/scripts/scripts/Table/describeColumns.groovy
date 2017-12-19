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
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
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

/**
 * This process is used to describe the columns of a table
 * 
 *
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS
 */
@Process(
        title = ["Describe columns","en",
                "Décrire les colonnes","fr"],
        description = ["Extract the name, type and comment from all fields of a table.","en",
                "Extrait le nom, le type et le commentaire de chacun des champs d'un table.","fr"],
        keywords = ["Table,Describe", "en",
                "Table,Description", "fr"],
        properties = ["DBMS_TYPE", "H2GIS",
                "DBMS_TYPE", "POSTGIS"],
        version = "1.0")
def processing() {    
    String query;
    if(isH2){
        query =  "CREATE TABLE " + outputTableName +" as SELECT COLUMN_NAME as col_name, TYPE_NAME as col_type,  REMARKS as col_comment from INFORMATION_SCHEMA.COLUMNS where table_name = '"+ tableName+"';"
    }
    else{
        query =   "CREATE TABLE " + outputTableName +" as SELECT cols.column_name as col_name,cols.udt_name as col_type, pg_catalog.col_description(c.oid, cols.ordinal_position::int) as col_comment FROM pg_catalog.pg_class c, information_schema.columns cols WHERE cols.table_name = '"+tableName +"'AND cols.table_name = c.relname "
    } 
    
    if(dropTable){
	sql.execute "drop table if exists " + outputTableName
    }
    sql.execute(query);
    literalOutput = "The descriptions have been extracted."
}

/****************/
/** INPUT Data **/
/****************/

/** This JDBCTable is the input data source table. */
@JDBCTableInput(
        title = ["Table","en",
                "Table","fr"],
        description = ["Extract name, type and comments from the selected table.","en",
                "Extrait les noms, les types et les commentaires de la table.","fr"])
String tableName

@LiteralDataInput(
    title = [
				"Drop the output table if exists","en",
				"Supprimer la table de sortie si elle existe","fr"],
    description = [
				"Drop the output table if exists.","en",
				"Supprimer la table de sortie si elle existe.","fr"])
Boolean dropTable 

@LiteralDataInput(
        title = ["Output table name","en",
                "Nom de la table de sortie","fr"],
        description = [
                "Name of the table containing the descriptions.","en",
                "Nom de la table contenant les descriptions.","fr"])
String outputTableName


/** Output message. */
@LiteralDataOutput(
        title = ["Output message","en",
                "Message de sortie","fr"],
        description = [
                "The output message.","en",
                "Le message de sortie.","fr"])
String literalOutput

