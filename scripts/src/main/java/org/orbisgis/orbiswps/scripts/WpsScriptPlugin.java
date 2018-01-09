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

import org.orbisgis.orbiswps.serviceapi.ProcessMetadata;
import org.orbisgis.orbiswps.serviceapi.WpsScriptBundle;
import org.orbisgis.orbiswps.client.api.WpsClient;
import org.orbisgis.orbiswps.server.WpsServer;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.*;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

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
public class WpsScriptPlugin extends WpsScriptsPackage implements WpsScriptBundle {

    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(WpsScriptPlugin.class);
    /** String parameters for the plugin. */
    /** Resource path to the folder containing the scripts. */
    private static final String SCRIPTS_RESOURCE_FOLDER_PATH = "scripts";
    /** Resource path to the folder containing the icons. */
    private static final String ICONS_RESOURCE_FOLDER_PATH = "icons";
    /** Name of the icon to use. */
    private static final String ICON_NAME = "orbisgis.png";
    /** Groovy extension. */
    private static final String GROOVY_EXTENSION = ".groovy";
    /** Base path. */
    private static final String BASE_PATH = "OrbisGIS";

    /** Icon list which will be passed as metadata. */
    private URL[] icons = new URL[]{};
    /** Cached map of the script path, the script URL as key, the path as value. */
    private Map<URL, String> cachedPath;
    /** Resource URL of the scripts */
    URL resourceUrl;

    /**
     * This methods is called once the plugin is loaded.
     *
     *
     */
    @Activate
    public void activate(){
        listIdProcess = new ArrayList<>();
        URL iconUrl = this.getClass().getResource(ICONS_RESOURCE_FOLDER_PATH+File.separator+ICON_NAME);
        icons = new URL[]{iconUrl};
        cachedPath = new HashMap<>();

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

    @Override
    public Map<String, Object> getGroovyProperties() {
        return null;
    }

    @Override
    public List<URL> getScriptsList() {
        resourceUrl = this.getClass().getResource(SCRIPTS_RESOURCE_FOLDER_PATH);
        return getAllSubUrl(resourceUrl);
    }

    @Override
    public Map<ProcessMetadata.INTERNAL_METADATA, Object> getScriptMetadata(URL scriptUrl) {
        Map<ProcessMetadata.INTERNAL_METADATA, Object> map = new HashMap<>();
        map.put(ProcessMetadata.INTERNAL_METADATA.IS_REMOVABLE, false);
        map.put(ProcessMetadata.INTERNAL_METADATA.NODE_PATH, cachedPath.get(scriptUrl));
        map.put(ProcessMetadata.INTERNAL_METADATA.ICON_ARRAY, icons);
        return map;
    }

    /**
     * Return the list of the URL of the Groovy files inside the given root url.
     * @param url Root url to explore to return the groovy files.
     * @return List of URL of the groovy files.
     */
    private List<URL> getAllSubUrl(URL url){
        Enumeration<URL> enumUrl = FrameworkUtil.getBundle(this.getClass()).findEntries(url.getFile(), "*", false);
        List<URL> list = new ArrayList<>();
        while(enumUrl.hasMoreElements()){
            URL u = enumUrl.nextElement();
            if(u.getFile().endsWith(GROOVY_EXTENSION)){
                cachedPath.put(u, I18N.tr(BASE_PATH)+File.separator+new File(u.toString()).getParent().substring(resourceUrl.toString().length()));
                list.add(u);
            }
            else{
                list.addAll(getAllSubUrl(u));
            }
        }
        return list;
    }
}
