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

import org.orbiswps.groovyapi.input.EnumerationInput
import org.orbiswps.groovyapi.output.EnumerationOutput
import org.orbiswps.groovyapi.process.Process
/********************/
/** Process method **/
/********************/

/**
 * Test script for the Enumeration
 * @author Sylvain PALOMINOS
 */
@Process(title = ["Enumeration test","en","Test du Enumeration","fr"],
        description = ["Test script using the Enumeration ComplexData.","en","Scripts test pour l'usage du ComplexData Enumeration.","fr"],
        keywords = ["test,script,wps","en","test,scripte,wps","fr"],
        identifier = "orbisgis:test:enumerationLongProcess",
        metadata = ["website","metadata"]
)
def processing() {
    sleep(1000)
    enumerationOutput = inputEnumeration;
}


/****************/
/** INPUT Data **/
/****************/

/** This Enumeration is the input data source. */
@EnumerationInput(
        title = ["Input Enumeration","en","Entrée Enumeration","fr"],
        description = ["A Enumeration input.","en","Une entrée Enumeration.","fr"],
        keywords = ["input","en","sortie","fr"],
        multiSelection = true,
        isEditable = true,
        values = ["value1", "value2"],
        names = ["name1,name2","en","nom1,nom2","fr"],
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "orbisgis:test:enumerationLongProcess:input",
        metadata = ["website","metadata"]
        )
String[] inputEnumeration = ["value2"]

/*****************/
/** OUTPUT Data **/
/*****************/

/** This Enumeration is the output data source. */
@EnumerationOutput(
        title = ["Output Enumeration","en","Sortie Enumeration","fr"],
        description = ["A Enumeration output.","en",
                "Une sortie Enumeration.","fr"],
        keywords = ["output","en","sortie","fr"],
        values = ["value1", "value2"],
        identifier = "orbisgis:test:enumerationLongProcess:output",
        metadata = ["website","metadata"]
)
String[] enumerationOutput

