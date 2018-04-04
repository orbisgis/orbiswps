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
package org.orbisgis.orbiswps.service;

import net.opengis.ows._2.*;
import net.opengis.wps._2_0.*;
import org.orbisgis.orbiswps.service.operations.*;
import org.orbisgis.orbiswps.serviceapi.operations.WPS_1_0_0_Operations;
import org.orbisgis.orbiswps.serviceapi.operations.WPS_2_0_Operations;
import org.orbisgis.orbiswps.serviceapi.operations.WpsProperties;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.orbisgis.orbiswps.serviceapi.WpsServerListener;
import org.orbisgis.orbiswps.serviceapi.*;
import org.orbisgis.orbiswps.service.process.ProcessManager;
import org.orbisgis.orbiswps.service.utils.Job;
import org.orbisgis.orbiswps.service.process.ProcessWorker;
import org.orbisgis.orbiswps.service.model.JaxbContainer;
import org.orbisgis.orbiswps.serviceapi.process.ProcessMetadata;
import org.osgi.service.component.annotations.*;
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
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.orbisgis.orbiswps.service.operations.Converter.convertGetCapabilities1to2;
import static org.orbisgis.orbiswps.service.operations.Converter.convertGetCapabilities2to1;

/**
 * This class is an implementation of a WPS server.
 * It is used a a base for the OrbisGIS local WPS server.
 *
 * @author Sylvain PALOMINOS
 */
@Component(immediate = true, service = WpsServer.class)
public class WpsServerImpl implements WpsServer {

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(WpsServerImpl.class);
    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(WpsServerImpl.class);

    /** Process manager which contains all the loaded scripts. */
    private ProcessManager processManager;
    /** ExecutorService of OrbisGIS */
    private ExecutorService executorService;
    /** True if a process is running, false otherwise. */
    private boolean processRunning = false;
    /** List of OrbisGISWpsServerListener. */
    private List<WpsServerListener> wpsServerListenerList = new ArrayList<>();
    /** Class execution the WPS 2.0 operations. */
    private WPS_2_0_Operations wps20Operations;
    private WpsServerProperties_2_0 props20;
    /** Class execution the WPS 1.0.0 operations. */
    private WPS_1_0_0_Operations wps100Operations;
    private WpsServerProperties_1_0_0 props100;

    private Map<UUID, Future> workerMap = new HashMap<>();


    /**********************************************/
    /** Initialisation method of the WPS service **/
    /**********************************************/

    /**
     * EmptyConstructor which load all its properties thanks to the set method setWpsProperties().
     */
    public WpsServerImpl(){
        //Creates the attribute for the processes execution
        processManager = new ProcessManager(null, this);
        wps20Operations = new WPS_2_0_OperationsImpl(this, props20, processManager);
        wps100Operations = new WPS_1_0_0_OperationsImpl(this, props100, processManager);
    }

    /**
     * Initialization of the WpsServer with the given properties.
     *
     * @param dataSource DataSource to be used by the server.
     */
    public WpsServerImpl(DataSource dataSource, ExecutorService executorService){
        this.executorService = executorService;
        //Creates the attribute for the processes execution
        processManager = new ProcessManager(dataSource, this);
        wps20Operations = new WPS_2_0_OperationsImpl(this, props20, processManager);
        wps100Operations = new WPS_1_0_0_OperationsImpl(this, props100, processManager);
    }

    /**
     * Initialization of the WpsServer with the given properties.
     *
     * @param dataSource DataSource to be used by the server.
     * @param propertyFileLocation Location of the property file of the Server.
     */
    public WpsServerImpl(DataSource dataSource, String propertyFileLocation, ExecutorService executorService){
        this.executorService = executorService;
        //Creates the attribute for the processes execution
        processManager = new ProcessManager(dataSource, this);
        props20 = new WpsServerProperties_2_0(propertyFileLocation);
        wps20Operations = new WPS_2_0_OperationsImpl(this, props20, processManager);
        props100 = new WpsServerProperties_1_0_0(propertyFileLocation);
        wps100Operations = new WPS_1_0_0_OperationsImpl(this, props100, processManager);
    }

    /**
     * Initialization of the WpsServer with the given properties.
     *
     * @param dataSource DataSource to be used by the server.
     */
    public WpsServerImpl(DataSource dataSource, String property100FileLocation, String property20FileLocation,
                         ExecutorService executorService){
        this.executorService = executorService;
        //Creates the attribute for the processes execution
        processManager = new ProcessManager(dataSource, this);
        props20 = new WpsServerProperties_2_0(property20FileLocation);
        wps20Operations = new WPS_2_0_OperationsImpl(this, props20, processManager);
        props100 = new WpsServerProperties_1_0_0(property100FileLocation);
        wps100Operations = new WPS_1_0_0_OperationsImpl(this, props100, processManager);
    }

    @Reference
    public void setDataSource(DataSource dataSource) {
        processManager.setDataSource(dataSource);
    }
    public void unsetDataSource(DataSource dataSource) {
        processManager.setDataSource(null);
    }

    @Reference
    public void setWpsProperties(WpsProperties wpsProperties) {
        if(wpsProperties.getWpsVersion().equals("1.0.0")){
            props100 = (WpsServerProperties_1_0_0)wpsProperties;
            if(wps100Operations != null){
                wps100Operations.setWpsProperties(props100);
            }
        }
        else if(wpsProperties.getWpsVersion().equals("2.0")){
            props20 = (WpsServerProperties_2_0)wpsProperties;
            if(wps20Operations != null){
                wps20Operations.setWpsProperties(props20);
            }
        }
    }
    public void unsetWpsProperties(WpsProperties wpsProperties) {
        if(wpsProperties.getWpsVersion().equals("1.0.0")){
            props100 = null;
            if(wps100Operations != null){
                wps100Operations.setWpsProperties(null);
            }
        }
        else if(wpsProperties.getWpsVersion().equals("2.0")){
            props20 = null;
            if(wps20Operations != null){
                wps20Operations.setWpsProperties(null);
            }
        }
    }

    @Reference
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
    public void unsetExecutorService(ExecutorService executorService) {
        this.executorService = null;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addWpsScriptBundle(WpsScriptBundle wpsScriptBundle) {
        List<URL> scriptList = wpsScriptBundle.getScriptsList();
        for(URL url : scriptList) {
            ProcessIdentifier pi = this.processManager.addScript(url);
            if(pi != null && pi.getProcessOffering() != null) {
                pi.setI18n(wpsScriptBundle.getI18n());
                pi.setProperties(wpsScriptBundle.getGroovyProperties());
                Map<ProcessMetadata.INTERNAL_METADATA, Object> map = wpsScriptBundle.getScriptMetadata(url);
                for (Map.Entry<ProcessMetadata.INTERNAL_METADATA, Object> entry : map.entrySet()) {
                    MetadataType metadataType = new MetadataType();
                    metadataType.setRole(entry.getKey().name());
                    Object obj = entry.getValue();
                    if (obj != null) {
                        if (obj instanceof URL[]) {
                            StringBuilder iconStr = new StringBuilder();
                            for (URL urlIcon : (URL[]) obj) {
                                if (iconStr.length() > 0) {
                                    iconStr.append(",");
                                }
                                iconStr.append(urlIcon.toString());
                            }
                            metadataType.setTitle(iconStr.toString());
                        } else {
                            metadataType.setTitle(obj.toString());
                        }
                    }
                    pi.getProcessDescriptionType().getMetadata().add(metadataType);
                }
            }
        }
        for(WpsServerListener listener : wpsServerListenerList){
            listener.onScriptAdd();
        }
    }

    public void removeWpsScriptBundle(WpsScriptBundle wpsScriptBundle) {
        for(URL url : wpsScriptBundle.getScriptsList()) {
            this.processManager.removeProcess(url);
        }
        for(WpsServerListener listener : wpsServerListenerList){
            listener.onScriptAdd();
        }
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
        net.opengis.wps._2_0.ObjectFactory factory20 = new net.opengis.wps._2_0.ObjectFactory();
        net.opengis.wps._1_0_0.ObjectFactory factory100 = new net.opengis.wps._1_0_0.ObjectFactory();
        try {
            Unmarshaller unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
            Object o = unmarshaller.unmarshal(xml);
            if(o instanceof JAXBElement){
                o = ((JAXBElement) o).getValue();
            }
            //Call the WPS method associated to the unmarshalled object
            //Case of the getCapabilities request
            if(o instanceof net.opengis.wps._1_0_0.GetCapabilities ||
                    o instanceof net.opengis.wps._2_0.GetCapabilitiesType) {
                //Get the version accepted
                List<String> versions = new ArrayList<>();
                if (o instanceof net.opengis.wps._1_0_0.GetCapabilities) {
                    net.opengis.wps._1_0_0.GetCapabilities capabilities = (net.opengis.wps._1_0_0.GetCapabilities) o;
                    if(capabilities.isSetAcceptVersions()) {
                        versions = capabilities.getAcceptVersions().getVersion();
                    }
                    else{
                        versions.add("1.0.0");
                    }
                } else {
                    net.opengis.wps._2_0.GetCapabilitiesType capabilities = (net.opengis.wps._2_0.GetCapabilitiesType) o;
                    if(capabilities.isSetAcceptVersions()) {
                        versions = capabilities.getAcceptVersions().getVersion();
                    }
                    else{
                        versions.add("2.0");
                    }
                }
                //Get the answer according to the higher version
                if(versions.contains("2.0")){
                    net.opengis.wps._2_0.GetCapabilitiesType capabilities;
                    if (o instanceof net.opengis.wps._1_0_0.GetCapabilities) {
                        capabilities = convertGetCapabilities1to2((net.opengis.wps._1_0_0.GetCapabilities)o);
                    } else {
                        capabilities = (net.opengis.wps._2_0.GetCapabilitiesType)o;
                    }
                    Object answer = wps20Operations.getCapabilities(capabilities);
                    if (answer instanceof net.opengis.wps._2_0.WPSCapabilitiesType) {
                        result = factory20.createCapabilities((net.opengis.wps._2_0.WPSCapabilitiesType) answer);
                    } else {
                        result = answer;
                    }
                }
                else if(versions.contains("1.0.0")){
                    net.opengis.wps._1_0_0.GetCapabilities capabilities;
                    if (o instanceof net.opengis.wps._1_0_0.GetCapabilities) {
                        capabilities = (net.opengis.wps._1_0_0.GetCapabilities)o;
                    } else {
                        capabilities = convertGetCapabilities2to1((net.opengis.wps._2_0.GetCapabilitiesType)o,
                                props100.GLOBAL_PROPERTIES.DEFAULT_LANGUAGE);
                    }
                    Object answer = wps100Operations.getCapabilities(capabilities);
                    if (answer instanceof net.opengis.wps._1_0_0.WPSCapabilitiesType) {
                        result = factory100.createCapabilities((net.opengis.wps._1_0_0.WPSCapabilitiesType) answer);
                    } else {
                        result = answer;
                    }
                }
                else{
                    net.opengis.wps._2_0.GetCapabilitiesType capabilities;
                    if (o instanceof net.opengis.wps._1_0_0.GetCapabilities) {
                        capabilities = convertGetCapabilities1to2((net.opengis.wps._1_0_0.GetCapabilities)o);
                    } else {
                        capabilities = (net.opengis.wps._2_0.GetCapabilitiesType)o;
                    }
                    Object answer = wps20Operations.getCapabilities(capabilities);
                    if (answer instanceof net.opengis.wps._2_0.WPSCapabilitiesType) {
                        result = factory20.createCapabilities((net.opengis.wps._2_0.WPSCapabilitiesType) answer);
                    } else {
                        result = answer;
                    }
                }
            }
            else if(o instanceof net.opengis.wps._1_0_0.DescribeProcess){
                result = wps100Operations.describeProcess((net.opengis.wps._1_0_0.DescribeProcess)o);
            }
            else if(o instanceof net.opengis.wps._2_0.DescribeProcess){
                result = wps20Operations.describeProcess((net.opengis.wps._2_0.DescribeProcess)o);
            }
            else if(o instanceof net.opengis.wps._1_0_0.Execute){
                result = wps100Operations.execute((net.opengis.wps._1_0_0.Execute)o);
            }
            else if(o instanceof net.opengis.wps._2_0.ExecuteRequestType){
                result = wps20Operations.execute((net.opengis.wps._2_0.ExecuteRequestType)o);
            }
            else if(o instanceof net.opengis.wps._2_0.GetStatus){
                result = wps20Operations.getStatus((net.opengis.wps._2_0.GetStatus)o);
            }
            else if(o instanceof net.opengis.wps._2_0.GetResult){
                result = wps20Operations.getResult((net.opengis.wps._2_0.GetResult)o);
            }
            else if(o instanceof net.opengis.wps._2_0.Dismiss){
                result = wps20Operations.dismiss((net.opengis.wps._2_0.Dismiss)o);
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

    /************************/
    /** Utilities methods. **/
    /************************/

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
    public void scheduleResultDestroying(URI resultUri, XMLGregorianCalendar date){
        //To be implemented
    }

    /**
     * Indicates if a process is actually running.
     * @return True if a process is running, false otherwise.
     */
    public Future executeNewProcessWorker(Job job, ProcessIdentifier processIdentifier, Map<URI, Object> dataMap) {
        ProcessWorker worker = new ProcessWorker(job, processIdentifier, processManager, dataMap, this);

        if (executorService != null) {
            Future future = executorService.submit(worker);
            workerMap.put(worker.getJobId(), future);
            return future;
        } else {
            return Executors.newSingleThreadExecutor().submit(worker);
        }
    }

    /**
     * Action done when a ProcessWorker has finished.
     */
    public void onProcessWorkerFinished(){
        //clear the workerMap
        List<UUID> toRemove = new ArrayList<>();
        for(Map.Entry<UUID, Future> entry : workerMap.entrySet()){
            if(entry.getValue().isDone()){
                toRemove.add(entry.getKey());
            }
        }
        for(UUID uuid : toRemove){
            workerMap.remove(uuid);
        }
    }

    /**
     * Cancel the running process corresponding to the given URI.
     * @param jobId Id of the job to cancel.
     */
    public void cancelProcess(UUID jobId) {
        processManager.cancelProcess(jobId);
        workerMap.get(jobId).cancel(true);
    }

    public WpsServerProperties_1_0_0 get100Properties(){
        return props100;
    }
}
