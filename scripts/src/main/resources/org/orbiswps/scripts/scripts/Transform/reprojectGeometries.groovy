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

package org.orbiswps.scripts.scripts.Transform;

import org.orbiswps.groovyapi.input.*
import org.orbiswps.groovyapi.output.*
import org.orbiswps.groovyapi.process.*


/**
 * This process reproject a geometry table using the SQL function.
 * The user has to specify (mandatory):
 *  - The input spatial data source (DataStore)
 *  - The geometry column (LiteralData)
 *  - The SRID value selected from the spatial_ref table
 *  - The output data source (DataStore)
 *
 * @return A database table.
 * @author Erwan Bocher
 */
@Process(title = ["Reproject geometries", "en", "Reprojecter des géométries", "fr"],
        resume = ["Reproject geometries from one Coordinate Reference System to another.", "en", "Reprojecter des géométries d'un sytème de coordonnées vers un autre","fr"],
        keywords = ["Vector,Geometry,Reproject", "en", "Vecteur, Géométrie, Reprojection",""])
def processing() {
//Build the start of the query
    String query = "CREATE TABLE "+dataStoreOutput+" AS SELECT ST_TRANSFORM("   
query += geometricField+","+srid[0]
   
    //Build the end of the query
    query += ") AS the_geom ";

if(fieldsList!=null){
query += ", "+ fieldsList;
}

 	query +=  " FROM "+inputDataStore+";"

    //Execute the query
    sql.execute(query)
}


/** This DataStore is the input data source. */
@DataStoreInput(
        title = "Input spatial data",
        resume = "The spatial data source to be reprojected.",
        isSpatial = true)
String inputDataStore


/** Name of the Geometric field of the DataStore inputDataStore. */
@DataFieldInput(
        title = "Geometric field",
        resume = "The geometric field of the data source",
        dataStore = "inputDataStore",
        fieldTypes = ["GEOMETRY"])
String geometricField


/** The spatial_ref SRID */
@FieldValueInput(title="SRID",
resume="The spatial reference system identifier",
dataField = "\$public\$spatial_ref_sys\$srid\$",
multiSelection = false)
String[] srid


/** Fields to keep. */
@DataFieldInput(
        title = "Fields to keep",
        resume = "The fields that will be kept in the ouput",
	excludedTypes=["GEOMETRY"],
	isMultipleField=true,
	minOccurs = 0,
        dataStore = "inputDataStore")
String fieldsList


/** This DataStore is the output data source. */
@DataStoreOutput(
        title="Reprojected data",
        resume="The output spatial data source to store the new geometries.",
        isSpatial = true)
String dataStoreOutput


