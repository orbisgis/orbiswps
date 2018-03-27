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

import net.opengis.wps._1_0_0.*;

/**
 * This interface describe all the operation that should be implemented by a WPS 1.0.0 server.
 *
 * @author Sylvain PALOMINOS
 */
public interface WPS_1_0_0_Operations extends WpsOperations {

    /**
     * The mandatory GetCapabilities operation allows clients to retrieve service metadata from a server.
     * The response to a GetCapabilities request shall contain service metadata about the server, including brief
     * metadata describing all the processes implemented.
     *
     * @param getCapabilities Request to a WPS server to perform the GetCapabilities operation.
     *                        This operation allows a client to retrieve a Capabilities XML document providing
     *                        metadata for the specific WPS server.
     * @return WPS GetCapabilities operation response.
     *             This document provides clients with service metadata about a specific service instance,
     *             including metadata about the processes that can be executed.
     */
    Object getCapabilities(GetCapabilities getCapabilities);

    /**
     * The DescribeProcess operation allows WPS clients to request a full description of one or more processes that
     * can be executed by the Execute operation.
     * This description includes the input and output parameters and formats.
     * @param describeProcess WPS DescribeProcess operation request.
     * @return List structure that is returned by the WPS DescribeProcess operation.
     *         Contains XML descriptions for the queried process identifiers.
     */
    Object describeProcess(DescribeProcess describeProcess);

    /**
     * The Execute operation allows WPS clients to run a specified process implemented by a server, using the input
     * parameter values provided and returning the output values produced. Inputs can be included directly in the
     * Execute request, or reference web accessible resources.
     * The outputs can be returned in the form of an XML response document, either embedded within the response
     * document or stored as web accessible resources. If the outputs are stored, the Execute response shall consist
     * of a XML document that includes a URL for each stored output, which the client can use to retrieve those outputs.
     * Alternatively, for a single output, the server can be directed to return that output in its raw form without
     * being wrapped in an XML reponse document.
     *
     * @param execute The Execute request is a common structure for execution.
     * @return  A response object.
     */
    Object execute(Execute execute);
}
