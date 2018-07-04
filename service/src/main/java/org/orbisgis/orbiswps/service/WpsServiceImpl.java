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
import org.orbisgis.orbiswps.service.process.ProcessManagerImpl;
import org.orbisgis.orbiswps.serviceapi.operations.WpsOperations;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.orbisgis.orbiswps.serviceapi.WpsServiceListener;
import org.orbisgis.orbiswps.serviceapi.*;
import org.orbisgis.orbiswps.service.model.JaxbContainer;
import org.orbisgis.orbiswps.serviceapi.process.ProcessMetadata;
import org.orbisgis.orbiswps.serviceapi.process.ProcessWorker;
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

/**
 * Implementations of a WPS Service.
 *
 * @author Sylvain PALOMINOS (CNRS 2017, UBS 2018)
 * @author Erwan Bocher (CNRS)
 */
@Component(immediate = true, service = WpsService.class)
public class WpsServiceImpl implements WpsService {

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(WpsServiceImpl.class);
    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(WpsServiceImpl.class);

    /** Process manager which contains all the loaded scripts. */
    private ProcessManagerImpl processManagerImpl;
    /** ExecutorService of OrbisGIS */
    private ExecutorService executorService;
    /** List of OrbisGISWpsServerListener. */
    private List<WpsServiceListener> wpsServiceListenerList = new ArrayList<>();

    private Map<UUID, Future> workerMap = new HashMap<>();
    private List<WpsOperations> wpsOperationsList = new ArrayList<>();


    /**********************************************/
    /** Initialisation method of the WPS service **/
    /**********************************************/

    /**
     * EmptyConstructor which load all its properties thanks to the set method setWpsProperties().
     */
    public WpsServiceImpl(){
        //Creates the attribute for the processes execution
        processManagerImpl = new ProcessManagerImpl(this);
    }

    /**
     * Initialization of the WpsService with the given properties.
     *
     * @param dataSource DataSource to be used by the server.
     */
    public WpsServiceImpl(DataSource dataSource, ExecutorService executorService){
        this.executorService = executorService;
        //Creates the attribute for the processes execution
        processManagerImpl = new ProcessManagerImpl(this, dataSource);
        wpsOperationsList.add(new WPS_2_0_Operations(processManagerImpl, dataSource));
        wpsOperationsList.add(new WPS_1_0_0_Operations(processManagerImpl, dataSource));
    }

    /**
     * Initialization of the WpsService with the given properties.
     *
     * @param dataSource DataSource to be used by the server.
     * @param propertyFileLocation Location of the property file of the Server.
     */
    public WpsServiceImpl(DataSource dataSource, String propertyFileLocation, ExecutorService executorService){
        this.executorService = executorService;
        //Creates the attribute for the processes execution
        processManagerImpl = new ProcessManagerImpl(this, dataSource);
        WPS_2_0_ServerProperties props20 = new WPS_2_0_ServerProperties(propertyFileLocation);
        wpsOperationsList.add(new WPS_2_0_Operations(processManagerImpl, props20, dataSource));
        WPS_1_0_0_ServerProperties props100 = new WPS_1_0_0_ServerProperties(propertyFileLocation);
        wpsOperationsList.add(new WPS_1_0_0_Operations(processManagerImpl, props100, dataSource));
    }

    /**
     * Initialization of the WpsService with the given properties.
     *
     * @param dataSource DataSource to be used by the server.
     */
    public WpsServiceImpl(DataSource dataSource, String property100FileLocation, String property20FileLocation,
                          ExecutorService executorService){
        this.executorService = executorService;
        //Creates the attribute for the processes execution
        processManagerImpl = new ProcessManagerImpl(this, dataSource);
        WPS_2_0_ServerProperties props20 = new WPS_2_0_ServerProperties(property20FileLocation);
        wpsOperationsList.add(new WPS_2_0_Operations(processManagerImpl, props20, dataSource));
        WPS_1_0_0_ServerProperties props100 = new WPS_1_0_0_ServerProperties(property100FileLocation);
        wpsOperationsList.add(new WPS_1_0_0_Operations(processManagerImpl, props100, dataSource));
    }

    @Reference
    public void setDataSource(DataSource dataSource) {
        processManagerImpl.setDataSource(dataSource);
    }
    public void unsetDataSource(DataSource dataSource) {
        processManagerImpl.setDataSource(null);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addWpsOperations(WpsOperations wpsOperations) {
        wpsOperations.setProcessManager(this.processManagerImpl);
        this.wpsOperationsList.add(wpsOperations);
    }
    public void removeWpsOperations(WpsOperations wpsOperations) {
        this.wpsOperationsList.remove(wpsOperations);
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
            ProcessIdentifier pi = this.processManagerImpl.addScript(url);
            if(pi != null && pi.getProcessOffering() != null) {
                pi.setProcessI18n(wpsScriptBundle.getI18n());
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
        for(WpsServiceListener listener : wpsServiceListenerList){
            listener.onScriptAdd();
        }
    }

    public void removeWpsScriptBundle(WpsScriptBundle wpsScriptBundle) {
        for(URL url : wpsScriptBundle.getScriptsList()) {
            this.processManagerImpl.removeProcess(url);
        }
        for(WpsServiceListener listener : wpsServiceListenerList){
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
        try {
            Unmarshaller unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
            Object o = unmarshaller.unmarshal(xml);
            if(o instanceof JAXBElement){
                o = ((JAXBElement) o).getValue();
            }
            //Call the WPS method associated to the unmarshalled object
            for(WpsOperations wpsOperations : wpsOperationsList){
                if(wpsOperations.isRequestAccepted(o)){
                    result = wpsOperations.executeRequest(o);
                }
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
        if(f.getName().endsWith(".groovy") || f.getName().endsWith(".xml")) {
            ProcessIdentifier pi = this.processManagerImpl.addScript(f.toURI());
            if(pi != null && pi.getProcessOffering() != null && pi.getProcessDescriptionType() != null){
                piList.add(pi);
            }
        }
        else if(f.isDirectory()){
            piList.addAll(this.processManagerImpl.addLocalSource(f.toURI()));
        }
        for(WpsServiceListener listener : wpsServiceListenerList){
            listener.onScriptAdd();
        }
        return piList;
    }

    @Override
    public void removeProcess(URI identifier){
        CodeType codeType = new CodeType();
        codeType.setValue(identifier.toString());
        ProcessDescriptionType process = this.processManagerImpl.getProcess(codeType);
        if(process != null) {
            this.processManagerImpl.removeProcess(process);
        }
        for(WpsServiceListener listener : wpsServiceListenerList){
            listener.onScriptRemoved();
        }
    }

    @Override
    public void addWpsServerListener(WpsServiceListener wpsServiceListener) {
        this.wpsServiceListenerList.add(wpsServiceListener);
    }

    @Override
    public void removeWpsServerListener(WpsServiceListener wpsServiceListener) {
        this.wpsServiceListenerList.remove(wpsServiceListener);
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
     * Execute a ProcessWorkerImpl
     */
    public Future executeNewProcessWorker(ProcessWorker processWorker) {
        if (executorService != null) {
            Future future = executorService.submit(processWorker);
            workerMap.put(processWorker.getJobId(), future);
            return future;
        } else {
            return Executors.newSingleThreadExecutor().submit(processWorker);
        }
    }

    /**
     * Action done when a ProcessWorkerImpl has finished.
     */
    public void onProcessWorkerFinished(UUID jobId){
        //clear the workerMap
        workerMap.remove(jobId);
    }

    /**
     * Cancel the running process corresponding to the given URI.
     * @param jobId Id of the job to cancel.
     */
    public void cancelProcess(UUID jobId) {
        processManagerImpl.cancelProcess(jobId);
        workerMap.get(jobId).cancel(true);
    }

    public ProcessManagerImpl getProcessManagerImpl() {
        return processManagerImpl;
    }
}
