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
 * Attributes for the BoundingBox.
 * Bounding box model serves a variety of purposes in spatial model processing.
 * Some simple applications are the definition of extents for a clipping operation or the definition of an
 * analysis region.
 *
 *
 * The following fields should be defined (mandatory) :
 *  - defaultCrs : String
 *      Default CRS.
 *
 * The following fields can be defined (optional) :
 *  - supportedCRS : String[]
 *      List of CRS supported by the BoundingBox model without the default one. Should be a string with the pattern :
 *      authority:code, like EPSG:2000.
 *  - dimension : int
 *      Dimension of the bounding box.
 *
 * @author Sylvain PALOMINOS
 */
@Retention(RetentionPolicy.RUNTIME)
@interface BoundingBoxAttribute {

    /** List of CRS supported by the BoundingBox model without the default one. Should be a string with the pattern :
     *  authority:code, like EPSG:2000. */
    String[] supportedCRS() default []

    /** Dimension of the bounding box. */
    int dimension() default 2

    /** Default CRS. */
    String defaultCrs()
}