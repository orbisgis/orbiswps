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
package org.orbisgis.orbiswps.service.process;

import net.opengis.ows._2.CodeType;
import org.orbisgis.orbiswps.service.WpsServiceImpl;
import org.orbisgis.orbiswps.service.model.wpsmodel.*;
import org.orbisgis.orbiswps.service.model.wpsmodel.Process;
import org.orbisgis.orbiswps.service.operations.WPS_2_0_ServerProperties;
import org.orbisgis.orbiswps.service.operations.WPS_2_0_Worker;
import org.orbisgis.orbiswps.service.utils.Job;
import org.orbisgis.orbiswps.serviceapi.WpsService;
import org.orbisgis.orbiswps.serviceapi.process.ProcessExecutionListener;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.orbisgis.orbiswps.serviceapi.process.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author Sylvain PALOMINOS (UBS 2018)
 * @author Erwan Bocher (CNRS)
 */
public class ModelWorker extends WPS_2_0_Worker implements Runnable, PropertyChangeListener, ProcessExecutionListener {

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelWorker.class);
    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(ModelWorker.class);

    private WpsModel wpsModel;

    private long basePoll;
    private long maxPool;

    public ModelWorker(WPS_2_0_ServerProperties wpsProps, ProcessIdentifierImpl pi, ProcessManager processManagerImpl){
        super(wpsProps, pi, processManagerImpl, new HashMap<URI, Object>());
        this.wpsModel = pi.getModel();
        basePoll = wpsProps.CUSTOM_PROPERTIES.BASE_PROCESS_POLLING_DELAY;
        maxPool = wpsProps.CUSTOM_PROPERTIES.MAX_PROCESS_POLLING_DELAY;
    }

    public Map<URI, Object> getDataMap(){
        return dataMap;
    }

    /**
     * Analyse the model to get the execution order of the different processes
     */
    public Map<Integer, List<String>> getExecutionTree(){
        Map<Integer, List<String>> processMap = new HashMap<>();
        List<Process> processes = new ArrayList<>();

        for(Input input : wpsModel.getInputs().getInput()){
            dataMap.put(URI.create(input.getIdentifier()), input.getData());
        }

        List<String> outputs = new ArrayList<>();
        for(Process process : wpsModel.getProcesses().getProcess()){
            processes.add(process);
            for(ProcessOutput output : process.getProcessOutput()){
                outputs.add(output.getIdentifier());
            }
        }

        int i=0;

        List<String> outputToRemove;

        do {
            outputToRemove = new ArrayList<>();
            List<Process> processToRemove = new ArrayList<>();
            List<String> processToAdd = new ArrayList<>();
            //Find out all the processes which input are not linked to the outputs
            for (Process process : processes) {
                boolean areAllInputSet = true;
                for (ProcessInput input : process.getProcessInput()) {
                    boolean isInputSet = true;
                    for (String output : outputs) {
                        if (output.equals(input.getValue())) {
                            isInputSet = false;
                        }
                    }
                    if (!isInputSet) {
                        areAllInputSet = false;
                    }
                }
                if (areAllInputSet) {
                    processToAdd.add(process.getIdentifier());
                    processToRemove.add(process);
                    for (ProcessOutput output : process.getProcessOutput()) {
                        outputToRemove.add(output.getIdentifier());
                    }
                }
            }
            if(!outputToRemove.isEmpty()) {
                outputs.removeAll(outputToRemove);
                processes.removeAll(processToRemove);
                processMap.put(i, processToAdd);
            }
            i++;
        }
        while(!outputToRemove.isEmpty());

        return processMap;
    }

    private void updateDataMapWithProcess(Process process){
        for(ProcessInput input : process.getProcessInput()){
            URI uri = URI.create(input.getValue());
            if(dataMap.containsKey(uri)){
                dataMap.put(URI.create(input.getIdentifier()), dataMap.get(uri));
            }
        }
    }

    public Future executeProcess(String id, Map<URI, Object> dataMap){
        CodeType codeType = new CodeType();
        codeType.setValue(id);
        ProcessIdentifier pi = processManager.getProcessIdentifier(codeType);
        Job job = new Job(pi.getProcessDescriptionType(), UUID.randomUUID(), dataMap,
                maxPool, basePoll);
        job.addProcessExecutionlistener(this);
        ProcessWorkerImpl processWorkerImpl = new ProcessWorkerImpl(job, pi, processManager, dataMap);
        return processManager.executeNewProcessWorker(processWorkerImpl);
    }

    @Override
    public void run() {
        Map<Integer, List<String>> map =  getExecutionTree();
        for(int i=0; i<map.size(); i++){
            List<Future> futureList = new ArrayList<>();
            for(String id : map.get(i)){
                for(Process process : wpsModel.getProcesses().getProcess()) {
                    if(process.getIdentifier().equals(id))
                        updateDataMapWithProcess(process);
                }
                futureList.add(executeProcess(id, dataMap));
            }
            for(Future future : futureList){
                try {
                    future.get();
                } catch (InterruptedException|ExecutionException e) {
                    LOGGER.error(I18N.tr("Error while executing sub process : {1}\n", e.getLocalizedMessage()));
                }
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }

    @Override
    public void appendLog(LogType logType, String message) {

    }

    @Override
    public void setProcessState(ProcessState processState) {
    }
}
