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
package org.orbiswps.server;

import org.orbiswps.server.controller.process.ProcessIdentifier;
import org.orbiswps.server.utils.WpsServerListener;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * A WPS Server is a web server that provides access to simple or complex computational processing services.
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
     * Cancel the running process corresponding to the given URI.
     * @param jobId Id of the job to cancel.
     */
    void cancelProcess(UUID jobId);

    /**
     * Enumeration of the supported databases
     */
    enum Database {H2GIS, POSTGIS}

    /**
     * Returns the database which is connected to the WPS server.
     * @return The database which is connected to the WPS server.
     */
    Database getDatabase();

    /**
     * Sets the database which is connected to the WPS server.
     * @param database The database which is connected to the WPS server.
     */
    void setDatabase(Database database);

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
     * Adds to the server execution properties which will be set to the GroovyObject for the execution.
     * Those properties will be accessible inside the groovy script as variables which name is the map entry key.
     * For example :
     * If the propertiesMap contains <"message", "HelloWorld">, inside the groovy script you can print the message this
     * way : 'print message'
     * @param propertiesMap Map containing the properties to be passed to the GroovyObject
     */
    void addGroovyProperties(Map<String, Object> propertiesMap);

    /**
     * Removes the properties already set for the GroovyObject for the execution.
     * @param propertiesMap Map containing the properties to be removed
     */
    void removeGroovyProperties(Map<String, Object> propertiesMap);

    /**
     * Returns the path of the folder containing the WPS groovy scripts.
     * @return The path of the folder containing the WPS groovy scripts.
     */
    String getScriptFolder();

    /**
     * Sets the path of the folder containing the WPS groovy scripts.
     * @param scriptFolder The path of the folder containing the WPS groovy scripts.
     */
    void setScriptFolder(String scriptFolder);

    /**
     * Sets the data source.
     * @param dataSource DataSource to use.
     */
    void setDataSource(DataSource dataSource);

    /**
     * Sets the ExecutorService.
     * @param executorService ExecutorService to use.
     */
    void setExecutorService(ExecutorService executorService);

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
