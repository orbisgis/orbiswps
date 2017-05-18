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

import net.opengis.ows._2.*;
import net.opengis.wps._2_0.*;
import net.opengis.wps._2_0.GetCapabilitiesType;
import net.opengis.wps._2_0.ObjectFactory;
import org.orbiswps.server.controller.process.ProcessIdentifier;
import org.orbiswps.server.controller.process.ProcessManager;
import org.orbiswps.server.controller.utils.Job;
import org.orbiswps.server.execution.ProcessWorker;
import org.orbiswps.server.model.JaxbContainer;
import org.orbiswps.server.utils.WpsServerListener;
import org.orbiswps.server.utils.WpsServerProperties_2_0;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * This class is an implementation of a WPS server.
 * It is used a a base for the OrbisGIS local WPS server.
 *
 * @author Sylvain PALOMINOS
 */
@Component(immediate = true, service = WpsServer.class)
public class WpsServerImpl implements WpsServer {

    /** Name of the folder containing the cached scripts. */
    private static final String SCRIPT_CACHE_FOLDER_NAME = "wpsscripts";
    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(WpsServerImpl.class);
    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(WpsServerImpl.class);

    /** Process manager which contains all the loaded scripts. */
    private ProcessManager processManager;
    /** Map containing the WPS Jobs and their UUID */
    /** ExecutorService of OrbisGIS */
    private ExecutorService executorService;
    /** Database connected to the WPS server */
    private Database database;
    /** Map containing all the properties to give to the groovy object.
     * The following words are reserved and SHOULD NOT be used as keys : 'logger', 'sql', 'isH2'. */
    private Map<String, Object> propertiesMap;
    /** True if a process is running, false otherwise. */
    private boolean processRunning = false;
    /** FIFO list of ProcessWorker, it is used to run the processes one by one in the good order. */
    private LinkedList<ProcessWorker> workerFIFO;
    /** Properties of the wps server */
    private WpsServerProperties_2_0 wpsProp;
    /** String path to the script folder. */
    private String scriptFolder;
    /** List of OrbisGISWpsServerListener. */
    private List<WpsServerListener> wpsServerListenerList = new ArrayList<>();
    /** Class execution the WPS 2.0 operations. */
    private WPS_2_0_Operations wps20Operations;


    /**********************************************/
    /** Initialisation method of the WPS service **/
    /**********************************************/

    /**
     * EmptyConstructor which load all its properties from the resource WpsServer properties file.
     */
    public WpsServerImpl(){
        propertiesMap = new HashMap<>();
        //Creates the attribute for the processes execution
        processManager = new ProcessManager(null, this);
        workerFIFO = new LinkedList<>();
        this.setScriptFolder(System.getProperty("java.io.tmpdir") + File.separator + SCRIPT_CACHE_FOLDER_NAME);
        wps20Operations = new WPS_2_0_OperationsImpl(this, new WpsServerProperties_2_0(null));
    }

    /**
     * Initialization of the WpsServer with the given properties.
     *
     * @param scriptFolder String path to the OrbisGIS script folder.
     * @param dataSource DataSource to be used by the server.
     */
    public WpsServerImpl(String scriptFolder, DataSource dataSource){
        propertiesMap = new HashMap<>();
        //Creates the attribute for the processes execution
        processManager = new ProcessManager(dataSource, this);
        workerFIFO = new LinkedList<>();
        this.setScriptFolder(scriptFolder);
        wps20Operations = new WPS_2_0_OperationsImpl(this, new WpsServerProperties_2_0(null));
    }

    /**
     * Method called on bundle activation.
     */
    @Activate
    public void activate(){}

    /*******************************************************************/
    /** Methods from the WpsService interface.                        **/
    /*******************************************************************/

    @Override
    public OutputStream callOperation(InputStream xml) {
        Object result = null;
        ObjectFactory factory = new ObjectFactory();
        try {
            Unmarshaller unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
            Object o = unmarshaller.unmarshal(xml);
            if(o instanceof JAXBElement){
                o = ((JAXBElement) o).getValue();
            }
            //Call the WPS method associated to the unmarshalled object
            if(o instanceof GetCapabilitiesType){
                Object answer = wps20Operations.getCapabilities((GetCapabilitiesType)o);
                if(answer instanceof WPSCapabilitiesType) {
                    result = factory.createCapabilities((WPSCapabilitiesType)answer);
                }
                else{
                    result = answer;
                }
            }
            else if(o instanceof DescribeProcess){
                result = wps20Operations.describeProcess((DescribeProcess)o);
            }
            else if(o instanceof ExecuteRequestType){
                result = wps20Operations.execute((ExecuteRequestType)o);
            }
            else if(o instanceof GetStatus){
                result = wps20Operations.getStatus((GetStatus)o);
            }
            else if(o instanceof GetResult){
                result = wps20Operations.getResult((GetResult)o);
            }
            else if(o instanceof Dismiss){
                result = wps20Operations.dismiss((Dismiss)o);
            }
        } catch (JAXBException e) {
            LOGGER.error(I18N.tr("Unable to parse the incoming xml.\nCause : {0}.", e.getMessage()));
            return new ByteArrayOutputStream();
        }
        //Write the request answer in an ByteArrayOutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if(result != null){
            try {
                //Marshall the WpsService answer
                Marshaller marshaller = JaxbContainer.JAXBCONTEXT.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(result, out);
            } catch (JAXBException e) {
                LOGGER.error(I18N.tr("Unable to parse the outcoming xml.\nCause : {0}.", e.getMessage()));
            }
        }
        return out;
    }

    @Override
    public void cancelProcess(UUID jobId){
        processManager.cancelProcess(jobId);
    }

    /*************************/
    /** Getters and setters **/
    /*************************/

    @Override
    public void setDatabase(Database database){
        this.database = database;
        processManager.filterProcessByDatabase();
    }
    @Override
    public Database getDatabase() {
        return database;
    }
    @Override
    public void setDataSource(DataSource dataSource){
        this.processManager.setDataSource(dataSource);
    }
    @Override
    public void setExecutorService(ExecutorService executorService){
        this.executorService = executorService;
    }

    /************************/
    /** Utilities methods. **/
    /************************/

    /**
     * Returns the list of processes managed by the wpsService.
     * @return The list of processes managed by the wpsService.
     */
    protected List<ProcessDescriptionType> getProcessList(){
        List<ProcessDescriptionType> processList = new ArrayList<>();
        List<ProcessIdentifier> piList = processManager.getAllProcessIdentifier();
        for(ProcessIdentifier pi : piList){
            processList.add(pi.getProcessDescriptionType());
        }
        return processList;
    }

    @Override
    public List<ProcessIdentifier> addProcess(File f){
        List<ProcessIdentifier> piList = new ArrayList<>();
        if(f.getName().endsWith(".groovy")) {
            ProcessIdentifier pi = this.processManager.addScript(f.toURI());
            if(pi != null && pi.getProcessOffering() != null && pi.getProcessDescriptionType() != null){
                piList.add(pi);
            }
        }
        else if(f.isDirectory()){
            piList.addAll(this.processManager.addLocalSource(f.toURI()));
        }
        for(WpsServerListener listener : wpsServerListenerList){
            listener.onScriptAdd();
        }
        return piList;
    }

    @Override
    public void removeProcess(URI identifier){
        CodeType codeType = new CodeType();
        codeType.setValue(identifier.toString());
        ProcessDescriptionType process = this.processManager.getProcess(codeType);
        if(process != null) {
            this.processManager.removeProcess(process);
        }
        for(WpsServerListener listener : wpsServerListenerList){
            listener.onScriptRemoved();
        }
    }

    @Override
    public void addGroovyProperties(Map<String, Object> propertiesMap){
        //Before adding an entry, check if it is not already defined.
        for(Map.Entry<String, Object> entry : propertiesMap.entrySet()){
            if(!this.propertiesMap.containsKey(entry.getKey()) &&
                    !entry.getKey().equals("logger") &&
                    !entry.getKey().equals("isH2") &&
                    !entry.getKey().equals("sql")){
                this.propertiesMap.put(entry.getKey(), entry.getValue());
            }
            else{
                LOGGER.error(I18N.tr("Unable to set the property {0}, the name is already used.", entry.getKey()));
            }
        }
    }

    @Override
    public void removeGroovyProperties(Map<String, Object> propertiesMap){
        for(Map.Entry<String, Object> entry : propertiesMap.entrySet()){
            if(this.propertiesMap.containsKey(entry.getKey()) &&
                    !entry.getKey().equals("logger") &&
                    !entry.getKey().equals("isH2") &&
                    !entry.getKey().equals("sql")){
                this.propertiesMap.remove(entry.getKey());
            }
            else{
                LOGGER.error(I18N.tr("Unable to remove the property {0}, the name protected or not defined.",
                        entry.getKey()));
            }
        }
    }

    @Override
    public void setScriptFolder(String scriptFolder){
        this.scriptFolder = scriptFolder;
    }

    @Override
    public String getScriptFolder(){
        return scriptFolder;
    }

    @Override
    public void addWpsServerListener(WpsServerListener wpsServerListener) {
        this.wpsServerListenerList.add(wpsServerListener);
    }

    @Override
    public void removeWpsServerListener(WpsServerListener wpsServerListener) {
        this.wpsServerListenerList.remove(wpsServerListener);
    }

    /**
     * Schedule the destroying of a generated result at the given date.
     * @param resultUri Uri of the result to destroy.
     * @param date Date when the result should be destroyed.
     */
    protected void scheduleResultDestroying(URI resultUri, XMLGregorianCalendar date){
        //To be implemented
    }

    /**
     * Indicates if a process is actually running.
     * @return True if a process is running, false otherwise.
     */
    protected void executeNewProcessWorker(Job job, ProcessIdentifier processIdentifier, Map<URI, Object> dataMap){
        ProcessWorker worker = new ProcessWorker(job, processIdentifier, processManager, dataMap, this);

        if(processRunning){
            workerFIFO.push(worker);
        }
        else {
            //Run the worker
            processRunning = true;
            if (executorService != null) {
                executorService.execute(worker);
            } else {
                worker.run();
            }
        }
    }

    /**
     * Action done when a ProcessWorker has finished.
     */
    public void onProcessWorkerFinished(){
        processRunning = false;
        //If other process are waiting, run them
        if(!processRunning && workerFIFO.size()>0){
            processRunning = true;
            if (executorService != null) {
                executorService.execute(workerFIFO.pollFirst());
            } else {
                workerFIFO.pollFirst().run();
            }
        }
    }

    /**
     * Returns the Map containing all the properties which will be given to the Groovy engine.
     * @return The Map of the groovy properties.
     */
    public Map<String, Object> getGroovyPropertiesMap(){
        return propertiesMap;
    }

    /**
     * Returns the ProcessManager.
     * @return The ProcessManager.
     */
    protected ProcessManager getProcessManager(){
        return processManager;
    }
}
