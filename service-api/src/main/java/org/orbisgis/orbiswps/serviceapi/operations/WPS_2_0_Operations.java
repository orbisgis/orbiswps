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

import net.opengis.wps._2_0.*;

/**
 * This interface describe all the operation that should be implemented by a WPS 2.0 server.
 *
 * @author Sylvain PALOMINOS
 */
public interface WPS_2_0_Operations extends WpsOperations {

    /**
     * This operation allows a client to retrieve service metadata, basic process offerings, and the available
     * processes present on a WPS server.
     *
     * @param getCapabilities Request to a WPS server to perform the GetCapabilities operation.
     *                        This operation allows a client to retrieve a Capabilities XML document providing
     *                        metadata for the specific WPS server.
     * @return WPS GetCapabilities operation response.
     *             This document provides clients with service metadata about a specific service instance,
     *             including metadata about the processes that can be executed.
     *             Since the server does not implement the updateSequence and Sections parameters,
     *             the server shall always return the complete Capabilities document,
     *             without the updateSequence parameter.
     */
    Object getCapabilities(GetCapabilitiesType getCapabilities);

    /**
     * The DescribeProcess operation allows WPS clients to query detailed process descriptions for the process
     * offerings.
     *
     * @param describeProcess WPS DescribeProcess operation request.
     * @return List structure that is returned by the WPS DescribeProcess operation.
     *         Contains XML descriptions for the queried process identifiers.
     */
    Object describeProcess(DescribeProcess describeProcess);

    /**
     * The Execute operation allows WPS clients to run a specified process implemented by a server,
     * using the input parameter values provided and returning the output values produced.
     * Inputs may be included directly in the Execute request (by value), or reference web accessible resources
     * (by reference).
     * The outputs may be returned in the form of an XML response document,
     * either embedded within the response document or stored as web accessible resources.
     * Alternatively, for a single output, the server may be directed to return that output in its raw form without
     * being wrapped in an XML response document.
     *
     * @param execute The Execute request is a common structure for synchronous and asynchronous execution.
     *                It inherits basic properties from the RequestBaseType and contains additional elements that
     *                identify the process that shall be executed, the model inputs and outputs, and the response type
     *                of the service.
     * @return Depending on the desired execution mode and the response type declared in the execute request,
     *         the execute response may take one of three different forms:
     *         A response document, a StatusInfo document, or raw model.
     */
    Object execute(ExecuteRequestType execute);

    /**
     * WPS GetStatus operation request. This operation is used to query status information of executed processes.
     * The response to a GetStatus operation is a StatusInfo document or an exception.
     * Depending on the implementation, a WPS may "forget" old process executions sooner or later.
     * In this case, there is no status information available and an exception shall be returned instead of a
     * StatusInfo response.
     *
     * @param getStatus GetStatus document. It contains an additional element that identifies the JobID of the
     *                  processing job, of which the status shall be returned.
     * @return StatusInfo document.
     */
    Object getStatus(GetStatus getStatus);

    /**
     * WPS GetResult operation request. This operation is used to query the results of asynchrously
     * executed processes. The response to a GetResult operation is a wps:ProcessingResult, a raw model response, or an exception.
     * Depending on the implementation, a WPS may "forget" old process executions sooner or later.
     * In this case, there is no result information available and an exception shall be returned.
     *
     * @param getResult GetResult document. It contains an additional element that identifies the JobID of the
     *                  processing job, of which the result shall be returned.
     * @return Result document.
     */
    Object getResult(GetResult getResult);

    /**
     * The dismiss operation allow a client to communicate that he is no longer interested in the results of a job.
     * In this case, the server may free all associated resources and “forget” the JobID.
     * For jobs that are still running, the server may cancel the execution at any time.
     * For jobs that were already finished, the associated status information and the stored results may be deleted
     * without further notice, regardless of the expiration time given in the last status report.
     * @param dismiss Dismiss request.
     * @return StatusInfo document.
     */
    StatusInfo dismiss(Dismiss dismiss);
}
