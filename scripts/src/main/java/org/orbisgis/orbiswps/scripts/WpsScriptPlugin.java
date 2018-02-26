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
package org.orbisgis.orbiswps.scripts;

import org.apache.commons.io.IOUtils;
import org.orbisgis.orbiswps.serviceapi.process.ProcessMetadata;
import org.orbisgis.orbiswps.serviceapi.WpsScriptBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Main class of the plugin which implements the WpsScriptBundle.
 */
@Component(immediate = true)
public class WpsScriptPlugin implements WpsScriptBundle {

    /** {@link I18n} object */
    private static final I18n I18N = I18nFactory.getI18n(WpsScriptPlugin.class);
    /** {@link Logger} */
    private static final Logger LOGGER = LoggerFactory.getLogger(WpsScriptPlugin.class);

    /** String parameters for the plugin. */
    /** Resource path to the folder containing the scripts. */
    private static final String SCRIPTS_RESOURCE_FOLDER_PATH = "scripts";
    /** Resource path to the folder containing the icons. */
    private static final String ICONS_RESOURCE_FOLDER_PATH = "icons";
    /** Name of the icon file to use. */
    private static final String ICON_NAME = "orbisgis.png";
    /** Groovy extension. */
    private static final String GROOVY_EXTENSION = ".groovy";
    /** Base path of the script. */
    private static final String BASE_PATH = "OrbisGIS";

    /** Icon {@link URL} array transmitted as process {@link ProcessMetadata.INTERNAL_METADATA INTERNAL_METADATA}. */
    private URL[] icons = new URL[]{};
    /** Cached map of the script path, the script {@link URL} as key, the path as value. This map is build when the
     *  method {@link #getScriptsList()} is called.*/
    private Map<URL, String> cachedPath;

    @Activate
    public void activate(){
        //Initialize icons and path collections
        icons = new URL[]{this.getClass().getResource(ICONS_RESOURCE_FOLDER_PATH+"/"+ICON_NAME)};
        cachedPath = new HashMap<>();

        //Mark the path parts to translate
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

    @Deactivate
    public void deactivate(){
        //Nothing to do
    }

    @Override
    public Map<String, Object> getGroovyProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("i18n",I18N);
        return properties;
    }

    @Override
    public List<URL> getScriptsList() {
        File f = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        URL url = this.getClass().getResource(SCRIPTS_RESOURCE_FOLDER_PATH);
        if(!url.toString().startsWith("bundle")){
            try {
                return getAllSubJarUrl(new JarFile(f), "org/orbisgis/orbiswps/scripts/scripts");
            } catch (IOException e) {
                LOGGER.error(I18N.tr("Unable to read the scripts inside the jar :\n{0}", e.getMessage()));
            }
        }
        return getAllSubUrl(this.getClass().getResource(SCRIPTS_RESOURCE_FOLDER_PATH));
    }

    @Override
    public Map<ProcessMetadata.INTERNAL_METADATA, Object> getScriptMetadata(URL scriptUrl) {
        Map<ProcessMetadata.INTERNAL_METADATA, Object> map = new HashMap<>();
        map.put(ProcessMetadata.INTERNAL_METADATA.IS_REMOVABLE, false);
        map.put(ProcessMetadata.INTERNAL_METADATA.NODE_PATH, cachedPath.get(scriptUrl));
        map.put(ProcessMetadata.INTERNAL_METADATA.ICON_ARRAY, icons);
        return map;
    }

    @Override
    public I18n getI18n() {
        return I18nFactory.getI18n(WpsScriptPlugin.class);
    }

    /**
     * Return the list of the {@link URL} of the Groovy files inside the given root url. It also puts in cache all the
     * path associated to the script url.
     * @param url Root {@link URL} to explore to return the groovy files.
     * @return List of {@link URL} of the groovy files.
     */
    private List<URL> getAllSubUrl(URL url){
        List<URL> list = new ArrayList<>();
        //Iterates over all the file and folder URL contained in the root URL
        for(URL u : getChildUrl(url)){
            //If the URL is a groovy file, build the node path, cache it and add the url to the list to return.
            if(u.getFile().endsWith(GROOVY_EXTENSION)){
                int pathStartIndex = this.getClass().getResource(SCRIPTS_RESOURCE_FOLDER_PATH).toString().length();
                String[] pathArray = new File(u.toString()).getParent().substring(pathStartIndex).split("/");
                StringBuilder finalPath = new StringBuilder(I18N.tr(BASE_PATH));
                for(String pathPart : pathArray){
                    finalPath.append("/").append(I18N.tr(pathPart));
                }
                cachedPath.put(u, finalPath.toString());
                list.add(u);
            }
            //Else, explore the URL
            else{
                list.addAll(getAllSubUrl(u));
            }
        }
        return list;
    }

    /**
     * Return the list of the {@link URL} of the Groovy files inside the given root url. It also puts in cache all the
     * path associated to the script url.
     * @return List of {@link URL} of the groovy files.
     */
    private List<URL> getAllSubJarUrl(JarFile jarFile, String path){
        List<URL> list = new ArrayList<>();
        //Iterates over all the file and folder URL contained in the root URL
        final Enumeration<JarEntry> entries = jarFile.entries(); //gives ALL entries in jar
        while(entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith(path + "/") && entry.getName().endsWith(".groovy")) { //filter according to the path
                try {
                    File file = File.createTempFile("orbisgis", ".groovy");
                    Writer writer = new FileWriter(file);
                    IOUtils.copy(jarFile.getInputStream(jarFile.getEntry(entry.getName())), writer);
                    writer.flush();
                    writer.close();
                    list.add(file.toURI().toURL());
                } catch (IOException e) {
                    LOGGER.error(I18N.tr("Unable to copy the script {0} from the jar file.", entry.getName()));
                }
            }
        }
        return list;
    }

    /**
     * Returns the list of child {@link URL} of a root {@link URL} no matter if the file are located in an OSG bundle.
     * @param url Root {@link URL}.
     * @return List of child {@link URL}.
     */
    private List<URL> getChildUrl(URL url){
        List<URL> childUrl = new ArrayList<>();

        //Case of an osgi bundle
        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        if(bundle != null) {
            Enumeration<URL> enumUrl = bundle.findEntries(url.getFile(), "*", false);
            while(enumUrl.hasMoreElements()) {
                childUrl.add(enumUrl.nextElement());
            }
            return childUrl;
        }

        //Other case
        try {
            File f = new File(url.toURI());
            if(f.exists()){
                for(File child : f.listFiles()){
                    childUrl.add(child.toURI().toURL());
                }
                return childUrl;
            }
        } catch (URISyntaxException|MalformedURLException ignored) {}

        //Unknown case, return empty list
        LOGGER.error(I18N.tr("Unable to explore the URL {0}", url));
        return new ArrayList<>();
    }
}
