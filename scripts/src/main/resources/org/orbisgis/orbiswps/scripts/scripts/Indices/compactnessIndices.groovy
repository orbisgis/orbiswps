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
package org.orbiswps.scripts.scripts.Morphology

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
				"Compactness indice(s)","en",
				"Indice(s) de compacité","fr"],
    description = [
				"The compactness indice is based on the geometry properties of a polygon : perimeter, area and longest line. 3 methods are available  : Miller, Morton and Gravélius.<p><em>Bibliography:</em></p><p>W. E. Dramstad, Spatial metrics–useful indicators for society or mainly fun tools for landscape ecologists?, Norsk Geografisk Tidsskrift-Norwegian Journal of Geography 63 (2009) 246–254.0</p><p> H. Gravelius, Grundriß der gesamten Gewässerkunde: in vier Bänden,vol.1, Göschen, 1914.</p>","en",
				"L'indice de compacité  est déduit des propriétés du polygon : superficie, périmètre et longueur maximale dans toutes les directions. 3 méthodes de calcul existent : Miller, Morton and Gravélius.<p><em>Bibliographie:</em></p><p>W. E. Dramstad, Spatial metrics–useful indicators for society or mainly fun tools for landscape ecologists?, Norsk Geografisk Tidsskrift-Norwegian Journal of Geography 63 (2009) 246–254.0</p><p> H. Gravelius, Grundriß der gesamten Gewässerkunde: in vier Bänden,vol.1, Göschen, 1914.</p>","fr"],
    keywords = ["Vector,Geometry,Index", "en",
				"Vecteur,Géométrie,Indice", "fr"],
    properties = ["DBMS_TYPE", "H2GIS",
				"DBMS_TYPE", "POSTGIS"],
    version = "1.0",
    identifier = "orbisgis:wps:official:compactnessIndices"
)
def processing() {
    
    //Build the start of the query
    String  outputTable = inputTable+"_compactnessindices"

    if(outputTableName != null){
	outputTable  = outputTableName
    }

    String query = "CREATE TABLE " + outputTable + " AS SELECT "

    if(keepgeom==true){
        query+= geometricField[0]+","
    }    
    
    for (String operation : operations) {
        if(operation.equals("gravelius")){
            query+= " ST_PERIMETER("+geometricField[0]+")/(2 * SQRT ( PI () * ST_AREA ("+ geometricField[0]+"))) as gravelius,"
        }
        else if(operation.equals("miller")){
            query+= " 4*PI ()*ST_AREA("+geometricField[0]+")/(POWER(ST_PERIMETER ("+ geometricField[0]+"),2)) as miller,"
        }
        else if(operation.equals("morton")){
            query+= "ST_AREA("+geometricField[0]+")/(PI () * (POWER(0.5 * ST_MAXDISTANCE ("+ geometricField[0]+","+ geometricField[0]+"),2))) as morton,"
        }
    }    
    query+=idField[0]+ " from " +  inputTable
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
String[] geometricField

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

@EnumerationInput(
    title = [
                "Indice","en",
                "Indice","fr"],
    description = [
                "Method to compute the indice.","en",
                "Méthode pour calculer l'indice.","fr"],
    values=["gravelius","miller", "morton"],
    names = ["Gravélius,Miller,Morton"],
    multiSelection = true)
String[] operations = ["gravelius"]


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


