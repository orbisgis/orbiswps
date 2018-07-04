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
package org.orbisgis.orbiswps.serviceapi.operations;

import org.orbisgis.orbiswps.serviceapi.process.ProcessManager;

/**
 * This interface describe the bases of the classes containing the operation dedicated to a defined version of WPS.
 * The properties of the server are set with the suitable WpsProperties object.
 * The WPS service will use the methods {@code getWpsVersion()} and {@code isRequestAccepted(Object request)} to find
 * the suitable WpsOperation class to execute a wps request.
 * The execution will be done with the method {@code executeRequest(Object object)} which will return the result of
 * the execution or a {@code null} value if the given request is not supported.
 *
 * @author Sylvain PALOMINOS (CNRS 2017, UBS 2018)
 * @author Erwan Bocher (CNRS)
 */
public interface WpsOperations {

    /**
     * Sets the properties of the WPS service.
     *
     * @param wpsProperties The WpsProperties object containing the properties of the service.
     *
     * @return True if the WpsProperties object has been recognized an loaded, false otherwise.
     */
    boolean setWpsProperties(WpsProperties wpsProperties);

    /**
     * Returns the string representation of the version of WPS supported i.e. "1.0.0" or "2.0".
     *
     * @return The version of WPS supported i.e. "1.0.0" or "2.0".
     */
    String getWpsVersion();

    /**
     * Returns true if the given Object is a supported WPS request.
     *
     * @param request WPS request which version is the same as the one returned from {@code getWpsVersion()}.
     *
     * @return True if the request is supported, false otherwise.
     */
    boolean isRequestAccepted(Object request);

    /**
     * Executes the given request and returns the result. If the request is not supported, a {@code null} value is
     * returned.
     *
     * @param request Request to execute.
     *
     * @return The request result or null if the request is not supported.
     */
    Object executeRequest(Object request);

    /**
     * Sets the ProcessManager
     *
     * @param processManager The ProcessManager
     */
    void setProcessManager(ProcessManager processManager);
}
