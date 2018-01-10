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
package org.orbisgis.orbiswps.scripts.scripts.Morphology

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*
import org.h2gis.utilities.SFSUtilities
import org.h2gis.utilities.TableLocation


/**
 * @author Erwan Bocher
 */
@Process(
    title = [
				"Fractal dimension index","en",
				"Indice de dimension fractale","fr"],
    description = [
				"Compute the fractal dimension of a polygon. <p><em>Bibliography:</em></p><p>Herold, M., Scepan, J., and Clarke, K. C. (2002). The use of remote sensing and landscape metrics to describe structures and changes in urban land uses. Environment and Planning A, 34(8):1443–1458.</p><p>McGarigal, K. and Marks, B. J. (1995). Fragstats: spatial pattern analysis program for quantifying landscape structure. Gen. Tech. Rep. PNW-GTR-351. Portland, OR: U.S. Department of Agriculture, Forest Service, Pacific Northwest Research Station. 122 p.</p>","en",
				"Calcule la dimension fractale d'un polygone. <p><em>Bibliographie:</em></p><p>Herold, M., Scepan, J., and Clarke, K. C. (2002). The use of remote sensing and landscape metrics to describe structures and changes in urban land uses. Environment and Planning A, 34(8):1443–1458.</p><p>McGarigal, K. and Marks, B. J. (1995). Fragstats: spatial pattern analysis program for quantifying landscape structure. Gen. Tech. Rep. PNW-GTR-351. Portland, OR: U.S. Department of Agriculture, Forest Service, Pacific Northwest Research Station. 122 p.</p","fr"],
    keywords = ["Vector,Geometry,Morphology", "en",
				"Vecteur,Géométrie,Morphologie", "fr"],
    properties = ["DBMS_TYPE", "H2GIS",
				"DBMS_TYPE", "POSTGIS"],
    version = "1.0",
    identifier = "orbisgis:wps:official:fractalDimension"
)
def processing() {
    
    //Build the start of the query
    String  outputTable = inputTable+"_fractaldimensionIndex"

    if(outputTableName != null){
	outputTable  = outputTableName
    }


    String query = "CREATE TABLE " + outputTable + " AS SELECT "


    if(keepgeom==true){
        query+= geometryColumn[0]+","
    }
    query += idField[0]+","+ "(2 * LOG ( ST_PERIMETER ("+ geometryColumn[0] +") ) ) / LOG (ST_AREA ("+ geometryColumn[0] +") ) as fractaldim from " +  inputTable

    
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


