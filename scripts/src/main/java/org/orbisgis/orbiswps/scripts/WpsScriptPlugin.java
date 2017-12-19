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
package org.orbisgis.orbiswps.scripts;

import org.orbisgis.orbiswps.client.api.WpsClient;
import org.orbisgis.orbiswps.server.WpsServer;
import org.osgi.service.component.annotations.*;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.util.ArrayList;

/**
 * Main class of the plugin which declares the scripts to add, their locations in the process tree and the icons
 * associated.
 * When the plugin is launched , the 'activate()' method is call. This method load the scripts in the
 * WpsService and refresh the WpsClient.
 * When the plugin is stopped or uninstalled, the 'deactivate()' method is called. This method removes the loaded script
 * from the WpsService and refreshes the WpsClient.
 *
 */
@Component(immediate = true)
public class WpsScriptPlugin extends WpsScriptsPackage {

    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(WpsScriptPlugin.class);

    /**
     * OSGI method used to give to the plugin the WpsService. (Be careful before any modification)
     * @param wpsServer WpsServer to used to load scripts.
     */
    @Reference
    public void setWpsServer(WpsServer wpsServer) {
        this.wpsServer = wpsServer;
    }

    /**
     * OSGI method used to remove from the plugin the WpsService. (Be careful before any modification)
     * @param wpsServer WpsServer to used to load scripts.
     */
    public void unsetWpsServer(WpsServer wpsServer) {
        this.wpsServer = null;
    }

    /**
     * OSGI method used to give to the plugin the WpsClient. (Be careful before any modification)
     * @param wpsClient WpsClient to use to add script metadata.
     */
    @Reference
    public void setWpsClient(WpsClient wpsClient) {
        this.wpsClient = wpsClient;
    }

    /**
     * OSGI method used to remove from the plugin the WpsClient. (Be careful before any modification)
     * @param wpsClient WpsClient to use to add script metadata.
     */
    public void unsetWpsClient(WpsClient wpsClient) {
        this.wpsClient = null;
    }

    /**
     * This methods is called once the plugin is loaded.
     *
     * It first check if the WpsService is ready.
     * If it is the case:
     *      Load the processes in the WpsService and save their identifier in the 'listIdProcess' list.
     *      Check if the WpsClient is ready.
     *      If it is the case :
     *          Refresh the WpsClient to display the processes.
     *      If not :
     *          Warn the user in the log that the WpsClient could not be found.
     * If not :
     *      Log the error and skip the process loading.
     *
     * In this class there is two methods to add the scripts :
     * The default one :
     *      This method adds all the scripts of the contained by the 'scripts' resources folder under the specified
     *      'nodePath' in the WpsClient. It keeps the file tree structure.
     * The custom one :
     *      This methods adds each script one by one under a specific node for each one.
     */
    @Activate
    public void activate(){
        listIdProcess = new ArrayList<>();
        //Check the WpsService
        if(wpsServer != null){
            //Mark the string to translate which will be used in the tree path.
            I18n.marktr("OrbisGIS");
            I18n.marktr("Network");
            I18n.marktr("Table");
            I18n.marktr("Geometry2D");
            I18n.marktr("Convert");
            I18n.marktr("Create");
            I18n.marktr("Buffer");
            I18n.marktr("Properties");
            I18n.marktr("Transform");
            I18n.marktr("Import");
            I18n.marktr("Export");
            I18n.marktr("Select");
            I18n.marktr("Indices");
            //Default method to load the scripts
            loadAllScripts();
        }
        else{
            LoggerFactory.getLogger(WpsScriptsPackage.class).error(
                    I18N.tr("Unable to retrieve the WpsService from OrbisGIS.\n" +
                            "The processes won't be loaded."));
        }
    }

    /**
     * This method is called when the plugin is deactivated.
     * If the WpsService is ready, removes all the previously loaded scripts.
     * If not, log the error and skip the process removing.
     * Then if the WpsClient is ready, refresh it.
     */
    @Deactivate
    public void deactivate(){
        if(wpsServer != null) {
            removeAllScripts();
        }
        else{
            LoggerFactory.getLogger(WpsScriptsPackage.class).error(
                    I18N.tr("Unable to retrieve the WpsService from OrbisGIS.\n" +
                            "The processes won't be removed."));
        }
    }
}
