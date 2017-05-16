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
package org.orbiswps.server

import org.orbiswps.groovyapi.input.RawDataInput
import org.orbiswps.groovyapi.output.RawDataOutput
import org.orbiswps.groovyapi.process.Process
/********************/
/** Process method **/
/********************/

/**
 * Test script for the RawData
 * @author Sylvain PALOMINOS
 */
@Process(title = ["RawData test","en","Test du RawData","fr"],
        description = ["Test script using the RawData ComplexData.","en",
                "Scripts test pour l'usage du ComplexData RawData.","fr"],
        keywords = ["test,script,wps","en","test,scripte,wps","fr"],
        identifier = "orbisgis:test:rawdata",
        metadata = ["website","metadata"]
)
def processing() {
        rawDataOutput = inputRawData;
}


/****************/
/** INPUT Data **/
/****************/

/** This RawData is the input data source. */
@RawDataInput(
        title = ["Input RawData","en","Entrée RawData","fr"],
        description = ["A RawData input.","en","Une entrée RawData.","fr"],
        keywords = ["input","en","entrée","fr"],
        isDirectory = false,
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "orbisgis:test:rawdata:input",
        metadata = ["website","metadata"]
        )
String inputRawData

/*****************/
/** OUTPUT Data **/
/*****************/

/** This RawData is the output data source. */
@RawDataOutput(
        title = ["Output RawData","en","Sortie RawData","fr"],
        description = ["A RawData output.","en","Une sortie RawData.","fr"],
        keywords = ["output","en","sortie","fr"],
        isFile = false,
        multiSelection = true,
        identifier = "orbisgis:test:rawdata:output",
        metadata = ["website","metadata"]
)
String rawDataOutput

