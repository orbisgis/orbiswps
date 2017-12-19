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
package org.orbisgis.orbiswps.scripts.scripts.Import

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

/********************/
/** Process method **/
/********************/

/**
 * This process creates a Point layer from a .CSV file.
 * The user has to specify (mandatory) :
 *  - The input CSV file (JDBCTable)
 *  - The CSV separators (Enumeration)
 *  - If the field name is on the first line (LiteralData)
 *  - The X field 
 *  - The Y field 
 *  - The output data source (JDBCTable) *
 *
 * @return The point layer data source created from a CSV file.
 *
 * @see http://www.h2gis.org/docs/dev/ST_SeST_MakePointSRID/
 * @author Sylvain PALOMINOS
 * @author Erwan BOCHER
 */
@Process(title = ["Point table from CSV","en","Table ponctuelle depuis un CSV","fr"],
    description = ["Creates a point layer from a CSV file containing the id of the point, its X and Y coordinate.","en",
                "Création d'une table de geometries ponctuelles à partir d'un fichier CSV contenant l'identifiant du point ainsi que ses coordonnées X et Y.","fr"],
    keywords = ["OrbisGIS,Importer, Fichier","fr",
                "OrbisGIS,Import, File","en"],
    properties = ["DBMS_TYPE","H2GIS"],
    version = "1.0")
def processing() {
    //Open the CSV file
    File csvFile = new File(csvDataInput[0])
    name = csvFile.getName()
    tableName = name.substring(0, name.lastIndexOf(".")).toUpperCase()
    
    if(jdbcTableOutputName != null){
	tableName = jdbcTableOutputName
    }    
    if(dropTable){
	sql.execute "drop table if exists " + tableName    }
    
    String csvRead = "CSVRead('"+csvFile.absolutePath+"', NULL, 'fieldSeparator="+separator+"')";   
    sql.execute("CREATE TABLE "+ tableName + " AS SELECT "+idField+", ST_MakePoint("+xField+", "+yField+") THE_GEOM FROM "+csvRead+";");    
    
    if(createIndex){
        sql.execute "create spatial index on "+ tableName + " (the_geom)"
    }
    
    literalDataOutput = "Process done"
}

/****************/
/** INPUT Data **/
/****************/

/** This JDBCTable is the input CSV file containing the points coordinates. It should be formed this way :
 * |ID|X|Y|
 * |--|-|-|
 * |1 |1|1|
 * ........
 * */
@RawDataInput(
    title = ["Input csv","en","Fichier CSV","fr"],
    description = ["The input CSV file containing the point data.","en",
                "Le fichier CSV d'entrée contenant les données ponctuelles.","fr"],
    fileTypes = ["csv"],
    isDirectory = false)
String[] csvDataInput


/**********************/
/** INPUT Parameters **/
/**********************/
@EnumerationInput(
    title = ["CSV separator","en","Séparateur CSV","fr"],
    description = ["The CSV separator.","en",
                "Le séparateur CSV.","fr"],
    values=[",", "\t", " ", ";"],
    names=["Coma, Tabulation, Space, Semicolon","en","Virgule, Tabulation, Espace, Point virgule","fr"],
    isEditable = true)
String[] separator = [";"]

@LiteralDataInput(
    title = ["Id field","en","Champ identifiant","fr"],
    description = ["The point id field.","en",
                "Le champ contenant l'identifiant du point.","fr"])
String idField

@LiteralDataInput(
    title = ["X field","en","Champ X","fr"],
    description = ["The X coordinate field.","en",
                "Le champ de la coordonnée X.","fr"])
String xField

@LiteralDataInput(
    title = ["Y field","en","Champ Y","fr"],
    description = ["The Y coordinate field.","en",
                "Le champ de la coordonnée Y.","fr"])
String yField


@LiteralDataInput(
    title = [
				"Add a spatial index","en",
				"Créer un index spatial","fr"],
    description = [
				"Add a spatial index on the geometry column.","en",
				"Ajout d'un index spatial sur la géometrie de la table.","fr"])
Boolean createIndex


@LiteralDataInput(
    title = [
				"Drop the existing table","en",
				"Supprimer la table existante","fr"],
    description = [
				"Drop the existing table.","en",
				"Supprimer la table existante.","fr"])
Boolean dropTable 

/** Output JDBCTable name. */
@LiteralDataInput(
    title = ["Output points table","en","Table de points","fr"],
    description = ["Name of the output table. If it is not defined the name of the file will be used.","en",
                "Nom de la table de sortie. Par défaut le nom de la table correspond au nom du fichier.","fr"],
    minOccurs = 0)
String jdbcTableOutputName

/************/
/** OUTPUT **/
/************/
@LiteralDataOutput(
    title = ["Output message","en",
                "Message de sortie","fr"],
    description = ["Output message.","en",
                "Message de sortie.","fr"])
String literalDataOutput
