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
package org.orbisgis.orbiswps.serviceapi.model;

import net.opengis.wps._2_0.ComplexDataType;
import org.xnap.commons.i18n.I18n;

import java.util.List;

/**
 * This interface is used to make a WPS ComplexData translatable and gives a method to translate the attributes of
 * the ComplexData
 *
 * @author Sylvain PALOMINOS
 */
public interface TranslatableComplexData {

    /**
     * Returns a translated version of this object.
     * The translated version must be the same as the original excepted the human readable strings which can be
     * translated.
     * This method receive the list of asked languages by the client. The translated language must be, if
     * possible, the client language or if not found the server language. If none of the language are found, uses any
     * language. If the server or the client language is '*', all the languages are accepted.
     *
     * As example :
     * server language : 'en', client languages : 'fr_FR'
     * 1) try to get the fr_FR translated language. (first client requested language)
     * 2) try to get the fr translated language. (first client requested language without the regional one)
     * 4) try to get the en translated language. (server default language)
     * 5) any language.
     *
     * @return A copy of the object itself but with its attribute translated.
     */
    ComplexDataType getTranslatedData(I18n i18n, List<String> languages);
}
