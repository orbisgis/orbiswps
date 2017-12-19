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
package org.orbiswps.scripts.scripts.Indices

import org.orbiswps.groovyapi.input.*
import org.orbiswps.groovyapi.output.*
import org.orbiswps.groovyapi.process.*
import org.h2gis.utilities.SFSUtilities
import org.h2gis.utilities.TableLocation


/**
 * @author Erwan Bocher
 */
@Process(
    title = [
				"Main direction SMBR","en",
				"Direction principale PPRE","fr"],
    description = [
				"Compute the main direction (in degree) of a polygon, based on its Smallest Minimum Bounding Rectangle (SMBR).\n\
<p>The north is equal to 0°. Values are clockwise, so East = 90°.</p>\n\
<p>The value is ”modulo pi” expressed → the value is between 0 and 180°(e.g 355° becomes 175°).</p>\n\
<p><em>Bibliography:</em></p><p>Duchêne, C., Bard, S., Barillot, X., Ruas, A., Trévisan, J., and Holzapfel, F. (2003). Quantitative and qualitative description of building orientation, In Fifth workshop on progress in automated map generalisation, ICA, commission on map generalisation.</p>","en",
				"Calcule de la direction principale (en degré) d'un polygon sur la base du Plus Petit Rectangle Englobant (PPRE).\n\
<p>Le nord est égale à 0°. L'Est correspond à 90°.</p>\n\
<p>Les orientations sont rapportées sur l'intervalle 0 and 180°(e.g 355° = 175°)</p>\n\
<p><em>Bibliographie:</em></p><p>Duchêne, C., Bard, S., Barillot, X., Ruas, A., Trévisan, J., and Holzapfel, F. (2003). Quantitative and qualitative description of building orientation, In Fifth workshop on progress in automated map generalisation, ICA, commission on map generalisation.</p>","fr"],
    keywords = ["Vector,Geometry,Index", "en",
				"Vecteur,Géométrie,Indice", "fr"],
    properties = ["DBMS_TYPE", "H2GIS",
				"DBMS_TYPE", "POSTGIS"],
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

    literalOutput = "Process done"
    
}

/****************/
/** INPUT Data **/
/****************/

@JDBCTableInput(
    title = [
				"Input table","en",
				"Table d'entrée","fr"],
    description = [
				"The spatial data source that contains the polygons.","en",
				"La table qui contient les polygones.","fr"],
    dataTypes = ["POLYGON", "MULTIPOLYGON"]
)
String inputTable


@JDBCColumnInput(
    title = [
				"Geometric column","en",
				"Colonne géométrique","fr"],
    description = [
				"The geometric column of input table.","en",
				"La colonne géométrique de la table d'entrée","fr"],
    jdbcTableReference = "inputTable",
    dataTypes = ["POLYGON", "MULTIPOLYGON"]
)
String[] geometryColumn

/** Name of the identifier field of the JDBCTable inputJDBCTable. */
@JDBCColumnInput(
    title = [
                "Column identifier","en",
                "Colonne identifiant","fr"],
    description = [
                "A column used as an identifier.","en",
                "La colonne utilisée comme identifiant.","fr"],
    excludedTypes=["GEOMETRY"],
    multiSelection = false,
    jdbcTableReference = "inputTable")
String[] idField

@LiteralDataInput(
    title = [
				"Keep the geometry","en",
				"Conserver la géométrie","fr"],
    description = [
				"Keep the input geometry in the result table.","en",
				"Conserver la géométrie d'entrée dans la table résultante.","fr"],
    minOccurs = 0)
Boolean keepgeom;


@LiteralDataInput(
    title = [
				"Drop the output table if exists","en",
				"Supprimer la table de sortie si elle existe","fr"],
    description = [
				"Drop the output table if exists.","en",
				"Supprimer la table de sortie si elle existe.","fr"],
    minOccurs = 0)
Boolean dropTable 

@LiteralDataInput(
    title = [
				"Output table prefix","en",
				"Prefixe de la table de sortie","fr"],
    description = [
				"Prefix of the table containing the result of the process.","en",
				"Prefixe de la table contenant les résultats du traitement.","fr"],
    minOccurs = 0,
    identifier = "outputTableName"
)
String outputTableName



/** String output of the process. */
@LiteralDataOutput(
    title = [
				"Output message","en",
				"Message de sortie","fr"],
    description = [
				"The output message.","en",
				"Le message de sortie.","fr"],
    identifier = "literalOutput"
)
String literalOutput

