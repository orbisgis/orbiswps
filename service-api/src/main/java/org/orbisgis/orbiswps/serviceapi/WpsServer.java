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
package org.orbisgis.orbiswps.serviceapi;

import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

/**
 * A WPS Service provides access to simple or complex computational processing services.
 *
 * @author Sylvain PALOMINOS
 */
public interface WpsServer {

    /**
     * Ask the WPS Server to execute the operation contained in the xml argument an returns the xml answer.
     * The xml is parsed and then the correct WPSService method is called.
     *
     * @param xml Xml containing the operation to execute.
     * @return The xml answer.
     */
    OutputStream callOperation(InputStream xml);

    /**
     * Add a local groovy file or directory of processes to the wps service.
     * @param f  File object to add to the service.
     * @return
     */
    List<ProcessIdentifier> addProcess(File f);

    /**
     * Remove the process corresponding to the given codeType.
     * @param identifier URI identifier of the process.
     */
    void removeProcess(URI identifier);

    /**
     * Registers a WpsServerListener.
     * @param wpsServerListener WpsServerListener to register.
     */
    void addWpsServerListener(WpsServerListener wpsServerListener);

    /**
     * Unregisters a WpsServerListener.
     * @param wpsServerListener WpsServerListener to unregister.
     */
    void removeWpsServerListener(WpsServerListener wpsServerListener);
}
