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
package org.orbiswps.scripts.scripts.Geometry2D.Convert

import org.orbiswps.groovyapi.input.*
import org.orbiswps.groovyapi.output.*
import org.orbiswps.groovyapi.process.*

/**
 * This process extract the center of a geometry table using  SQL functions.
 * The user has to specify (mandatory):
 *  - The input spatial data source (JDBCTable)
 *  - The geometry column (LiteralData)
 *  - The geometry operation (centroid or interior point)
 *  - The output data source (JDBCTable)
 *
 * @return A datadase table.
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS
 */
@Process(
		title = ["Extract center","en",
				"Extraction du centre","fr"],
		description = ["Extract the center of a geometry.","en",
				"Extraction du centre d'une géométrie.","fr"],
		keywords = ["Vector,Geometry,Extract,Center", "en",
				"Vecteur,Géométrie,Extraction,Centre", "fr"],

		properties = ["DBMS_TYPE", "H2GIS",
				"DBMS_TYPE", "POSTGIS"],
                version = "1.0")
def processing() {
	//Build the start of the query
	String query = "CREATE TEMPORARY TABLE "+outputTableName+" AS SELECT "
   

	if(operation[0].equalsIgnoreCase("centroid")){
		query += " ST_Centroid("+geometricField[0]+""
	}
	else{
		query += " ST_PointOnSurface("+geometricField[0]+""
	}
    //Build the end of the query
    query += ") AS the_geom ,"+ idField[0]+ " FROM "+inputJDBCTable+";"
    
    if(dropTable){
	sql.execute "drop table if exists " + outputTableName
    }
    
    //Execute the query
    sql.execute(query)
    if(dropInputTable){
        sql.execute "drop table if exists " + inputJDBCTable
    }
    literalOutput = "Process done"
}


/****************/
/** INPUT Data **/
/****************/

/** This JDBCTable is the input data source. */
@JDBCTableInput(
		title = ["Extract center","en",
				"Extraction du centre","fr"],
		description = [
				"Extract the center of a geometry.","en",
				"Extraction du centre d'une géométrie.","fr"],
        dataTypes = ["GEOMETRY"])
String inputJDBCTable

/**********************/
/** INPUT Parameters **/
/**********************/

/** Name of the Geometric field of the JDBCTable inputJDBCTable. */
@JDBCColumnInput(
		title = [
				"Geometric column","en",
				"Colonne géométrique","fr"],
		description = [
				"The geometric column of the data source.","en",
				"La colonne géométrique de la source de données.","fr"],
        jdbcTableReference = "inputJDBCTable",
        dataTypes = ["GEOMETRY"])
String[] geometricField

/** Name of the identifier field of the JDBCTable inputJDBCTable. */
@JDBCColumnInput(
		title = ["Column identifier","en",
				"Colonne identifiant","fr"],
		description = [
				"A column used as an identifier.","en",
				"Colonne utilisée comme identifiant.","fr"],
		excludedTypes=["GEOMETRY"],
		jdbcTableReference = "inputJDBCTable")
String[] idField

@EnumerationInput(
		title = ["Operation","en",
				"Opération","fr"],
		description = [
				"Operation to extract the points.","en",
				"Opération d'extraction des points.","fr"],
        values=["centroid", "interior"],
        names=["Centroid", "Interior"])
String[] operation = ["centroid"]

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
				"Name of the table containing the result of the process.","en",
				"Nom de la table contenant les résultats du traitement.","fr"])
String outputTableName


@LiteralDataInput(
    title = [
				"Drop the input table","en",
				"Supprimer la table d'entrée","fr"],
    description = [
				"Drop the input table when the script is finished.","en",
				"Supprimer la table d'entrée lorsque le script est terminé.","fr"])
Boolean dropInputTable 


/*****************/
/** OUTPUT Data **/
/*****************/

/** String output of the process. */
@LiteralDataOutput(
		title = ["Output message","en",
				"Message de sortie","fr"],
		description = [
				"The output message.","en",
				"Le message de sortie.","fr"])
String literalOutput

