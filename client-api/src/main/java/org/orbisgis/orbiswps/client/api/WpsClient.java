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
package org.orbisgis.orbiswps.client.api;

import net.opengis.wps._2_0.Result;
import net.opengis.wps._2_0.StatusInfo;
import org.orbisgis.orbiswps.serviceapi.process.ProcessMetadata;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

/**
 * The WPS client interface for the communication with WPS external servers.
 * A WPS client contains all the methods for the communication with the external WPS servers.
 * It contains others methods to allow other OrbisGIS components to get, run or open WPS processes throw the client.
 * Other methods for the process tracking thanks to JobStateListeners and some UI interaction are defined.
 *
 * @author Sylvain PALOMINOS
 */
public interface WpsClient {


    /*****************/
    /** WPS methods **/
    /*****************/

    /**
     * Ask to the WPS server running the job with the given id its status. Once the answer get, a job status event
     * should be launch.
     *
     * @param jobID UUID of the job.
     *
     * @return The StatusInfo object containing the status of the job with the given UUID.
     */
    StatusInfo getJobStatus(UUID jobID);

    /**
     * Ask the WpsService the result of the job corresponding to the given ID.
     *
     * @param jobID UUID of the job.
     *
     * @return The Result object containing the results of a job with the given UUID.
     */
    Result getJobResult(UUID jobID);

    /**
     * Ask the WpsService to dismiss the job corresponding to the given ID.
     *
     * @param jobID UUID of the job.
     *
     * @return The StatusInfo object containing the status of the job with the given UUID.
     */
    StatusInfo dismissJob(UUID jobID);

    /**
     * Build the Execution request with the process identifier and the model to process. Then send it to the server.
     * To finish return the StatusInfo object get back from the request.
     *
     * @param processIdentifier The identifier of the process to execute.
     * @param dataMap Map containing the inputs/outputs values. The map key are the URI of the input/output and the
     *                value is the value Object.
     *
     * @return The StatusInfo object containing the status of the job resulting of the request.
     */
    StatusInfo executeProcess(URI processIdentifier, Map<URI,Object> dataMap);

    /**
     * Adds metadata (like Icons to use, node path ...) to use in the WPS client for the process having the given
     * identifier.
     *
     * @param processIdentifier Identifier of the process.
     * @param metadataMap Map of metadata to use for the process in the client (like Icons to use, node path ...)
     */
    void addProcessMetadata(URI processIdentifier, Map<ProcessMetadata.INTERNAL_METADATA, Object> metadataMap);

    /**
     * Removes all the metadata registered for the process having the given identifier.
     *
     * @param processIdentifier Identifier of the process.
     */
    void removeProcessMetadata(URI processIdentifier);
}
