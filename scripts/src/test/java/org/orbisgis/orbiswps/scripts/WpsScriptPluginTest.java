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

import junit.framework.Assert;
import net.opengis.ows._2.CodeType;
import net.opengis.wps._2_0.*;
import net.opengis.wps._2_0.GetCapabilitiesType;
import org.junit.Test;
import org.orbisgis.orbiswps.client.api.WpsClient;
import org.orbisgis.orbiswps.server.WpsServer;
import org.orbisgis.orbiswps.server.controller.process.ProcessIdentifier;
import org.orbisgis.orbiswps.server.utils.ProcessMetadata;
import org.orbisgis.orbiswps.server.utils.WpsServerListener;

import javax.sql.DataSource;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Class to test the well working of the plugin life cycle :
 *
 * On activating : add all the scripts of the resource folder (src/main/resources/org/orbisgis/wpsservicescripts/scripts)
 * to a fake WPS server {@code CustomWpsService}.
 *
 * On deactivating : remove all the scripts add from the WPS server.
 *
 * @author Sylvain PALOMINOS
 */
public class WpsScriptPluginTest {

    /**
     * Test the life cycle of the plugin.
     */
    @Test
    public void testPlugin(){
        //Initialize an instance of OrbisGISWpsScriptPlugin, CustomWpsService, CustomWpsClient and CustomCoreWorkspace
        CustomWpsServer customWpsServer = new CustomWpsServer();
        customWpsServer.setScriptFolder(System.getProperty("java.io.tmpdir"));
        CustomWpsClient customWpsClient = new CustomWpsClient();
        WpsScriptPlugin plugin = new WpsScriptPlugin();
        //Give to the OrbisGISWpsScriptPlugin the WpsServer and the WpsClient
        plugin.setWpsServer(customWpsServer);
        plugin.setWpsClient(customWpsClient);
        //Simulate the activation of the plugin and get back the list of script file add
        plugin.activate();
        List<File> addScriptList = customWpsServer.getAddScriptList();
        //Gets the list of the script files contained in the resource folder of the plugin
        File folder = new File(this.getClass().getResource("scripts").getFile());
        List<File> resourceGroovyScriptList = getAllGroovyScripts(folder);
        //Test if each file from the resource folder has been loaded in the WPS server
        for(File resourceScript : resourceGroovyScriptList){
            boolean isResourceScriptAdd = false;
            for(File addScript : addScriptList){
                if(resourceScript.getName().equals(addScript.getName())){
                    isResourceScriptAdd = true;
                }
            }
            Assert.assertTrue("The resource file '"+resourceScript.getName()+"' should be add by the plugin.",
                    isResourceScriptAdd);
        }
        Assert.assertEquals("There must be a metadata map in the client for each loaded script in the server.",
                addScriptList.size(), customWpsClient.getMetadataMap().size());
        //Simulate the deactivation of the plugin
        plugin.deactivate();
        //Test if all the script have been removed
        Assert.assertTrue("All the scripts should have been removed from the server.",
                customWpsServer.getAddScriptList().isEmpty());
        //Unset the CoreWorkspace and the WpsServer and the WpsClient
        plugin.unsetWpsServer(null);
        plugin.unsetWpsClient(null);
    }

    /**
     * Test the plugin life cycle without
     */
    @Test
    public void testPluginNoServerClient(){
        //Initialize an instance of OrbisGISWpsScriptPlugin, d CustomCoreWorkspace
        WpsScriptPlugin plugin = new WpsScriptPlugin();
        //Simulate the activation of the plugin and get back the list of script file add
        plugin.activate();
        Assert.assertNotNull("The plugin process id list should be initialized", plugin.listIdProcess);
        plugin.deactivate();
    }

    /**
     * Tests the method loadIcons() from the class WpsScriptsPackage.
     */
    @Test
    public void testLoadIconMethod(){
        String tmpFolderPath = System.getProperty("java.io.tmpdir") + File.separator;
        WpsScriptsPackage wpsScriptsPackage = new WpsScriptsPackage();
        //Test with a bad icon
        Object result = wpsScriptsPackage.loadIcon("UnExistingIcon.notAnExtension");
        Assert.assertNull("The loadIcon() method should return null if the given icon name is bad.", result);
        //Test with a bad script folder
        CustomWpsServer customWpsServer = new CustomWpsServer();
        customWpsServer.setScriptFolder(tmpFolderPath + "InvalidName"+'\0');
        wpsScriptsPackage.wpsServer = customWpsServer;
        result = wpsScriptsPackage.loadIcon("orbisgis.png");
        Assert.assertNull("The loadIcon() method should return null if the given script folder name is bad.", result);

        customWpsServer.setScriptFolder(tmpFolderPath + "ValidName"+UUID.randomUUID());
        result = wpsScriptsPackage.loadIcon("orbisgis.png");
        Assert.assertNotNull("The loadIcon() method should return a valid String file path.", result);
    }

    /**
     * Returns the list of the groovy script file of a given directory.
     * @param directory Directory to explore.
     * @return The list of the groovy script files.
     */
    private List<File> getAllGroovyScripts(File directory){
        List<File> scriptList = new ArrayList<>();
        File[] files = directory.listFiles();
        if(files != null){
            for(File f : files) {
                if (f.isDirectory()) {
                    scriptList.addAll(getAllGroovyScripts(f));
                }
                else {
                    scriptList.add(f);
                }
            }
        }
        return scriptList;
    }

    /**
     * A fake WpsServer implementation. Only addLocalSource(File,String[],boolean,String) and removeProcess(URI)
     * methods are implemented. It is used to simulate a WpsServer but it only store in a list the loaded script.
     * This list is accessible throw the methods getAddScriptList().
     */
    private class CustomWpsServer implements WpsServer {
        private List<File> addScriptList = new ArrayList<>();
        private String scriptFolder;

        @Override
        public List<ProcessIdentifier> addProcess(File f) {
            addScriptList.add(f);
            //Building of an empty processOffering
            CodeType codeType = new CodeType();
            codeType.setValue(f.toURI().toString());
            ProcessDescriptionType processDescriptionType = new ProcessDescriptionType();
            processDescriptionType.setIdentifier(codeType);
            ProcessOffering processOffering = new ProcessOffering();
            processOffering.setProcess(processDescriptionType);
            //Return the ProcessIdentifier of the source to add
            List<ProcessIdentifier> processIdentifierList = new ArrayList<>();
            processIdentifierList.add(new ProcessIdentifier(processOffering, f.getAbsolutePath()));
            return processIdentifierList;
        }

        /**
         * Returns the list of the script files add to the server.
         * @return The list of the script files add to the server.
         */
        public List<File> getAddScriptList(){ return addScriptList;}

        @Override public void removeProcess(URI identifier) {
            File fileToRemove = null;
            for(File f : addScriptList){
                if(f.toURI().toString().equals(identifier.toString())){
                    fileToRemove = f;
                }
            }
            addScriptList.remove(fileToRemove);
        }
        @Override public String getScriptFolder() {return scriptFolder;}
        @Override public void setScriptFolder(String scriptFolder) {this.scriptFolder = scriptFolder;}


        //Methods not used in the tests
        @Override public void setDataSource(DataSource dataSource) {}
        @Override public void setExecutorService(ExecutorService executorService) {}
        @Override public void addGroovyProperties(Map<String, Object> propertiesMap) {}
        @Override public void removeGroovyProperties(Map<String, Object> propertiesMap) {}
        @Override public OutputStream callOperation(InputStream xml) {return null;}
        @Override public void cancelProcess(UUID jobId) {}
        @Override public Database getDatabase() {return null;}
        @Override public void setDatabase(Database database) {}
        @Override public void addWpsServerListener(WpsServerListener wpsServerListener) {}
        @Override public void removeWpsServerListener(WpsServerListener wpsServerListener) {}
    }


    /**
     * A fake WpsClient implementation. Only addLocalSource(File,String[],boolean,String) and removeProcess(URI)
     * methods are implemented. It is used to simulate a WpsServer but it only store in a list the loaded script.
     * This list is accessible throw the methods getAddScriptList().
     */
    private class CustomWpsClient implements WpsClient{

        private Map<URI, Map<ProcessMetadata.INTERNAL_METADATA, Object>> metadataMap = new HashMap<>();

        @Override
        public void addProcessMetadata(URI processIdentifier, Map<ProcessMetadata.INTERNAL_METADATA,
                Object> metadataMap) {
            this.metadataMap.put(processIdentifier, metadataMap);
        }
        @Override
        public void removeProcessMetadata(URI processIdentifier) {
            this.metadataMap.remove(processIdentifier);
        }

        /**
         * Returns the metadata map.
         * @return The metadata map.
         */
        public Map<URI, Map<ProcessMetadata.INTERNAL_METADATA, Object>> getMetadataMap(){return metadataMap;}

        //Methods not used in the tests
        @Override public StatusInfo getJobStatus(UUID jobID) {return null;}
        @Override public Result getJobResult(UUID jobID) {return null;}
        @Override public StatusInfo dismissJob(UUID jobID) {return null;}
        @Override public StatusInfo executeProcess(URI processIdentifier, Map<URI, Object> dataMap) {return null;}
    }
}
