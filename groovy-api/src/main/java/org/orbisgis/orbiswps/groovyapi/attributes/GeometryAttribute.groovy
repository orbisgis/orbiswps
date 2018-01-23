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
package org.orbisgis.orbiswps.groovyapi.attributes

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Attributes for the Geometry complex model.
 * The Geometry is a complex model that represents a geometry.
 *
 * The following fields can be defined (optional) :
 *  - geometryTypes : String[]
 *      Array of geometry type allowed. If no types are specified, accept all.
 *
 *  - excludedTypes : String[]
 *      Array of the type not allowed for the geometry.
 *
 *  - dimension : int
 *      Dimension of the geometry (can be 2 or 3).
 *
 * @author Sylvain PALOMINOS
 */

@Retention(RetentionPolicy.RUNTIME)
@interface GeometryAttribute {

    /** Array of geometry type allowed. If no types are specified, accept all.*/
    String[] geometryTypes() default []

    /** Array of the type not allowed for the geometry.*/
    String[] excludedTypes() default []

    /** Dimension of the geometry (can be 2 or 3). */
    int dimension() default 2

}