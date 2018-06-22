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
import org.orbisgis.orbiswps.service.utils.Job;
import org.orbisgis.orbiswps.serviceapi.process.ProcessExecutionListener;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.*;

/**
 *
 * @author Sylvain PALOMINOS (UBS 2018)
 * @author Erwan Bocher (CNRS)
 */
public class ModelWorker implements Runnable, PropertyChangeListener, ProcessExecutionListener {

    private WpsModel wpsModel;
    private WpsServiceImpl wpsServer;
    private ProcessManagerImpl processManagerImpl;

    public ModelWorker(WpsModel wpsModel, WpsServiceImpl wpsServer, ProcessManagerImpl processManagerImpl){
        this.wpsModel = wpsModel;
        this.wpsServer = wpsServer;
        this.processManagerImpl = processManagerImpl;
    }

    /**
     * Analyse the model to get the execution order of the different processes
     */
    public Map<Integer, List<String>> getExecutionTree(){
        Map<Integer, List<String>> processMap = new HashMap<>();
        List<Process> processes = new ArrayList<>();

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

    public void executeProcess(String id, Map<URI, Object> dataMap){
        CodeType codeType = new CodeType();
        codeType.setValue(id);
        ProcessIdentifier pi = processManagerImpl.getProcessIdentifier(codeType);
        Job job = new Job(pi.getProcessDescriptionType(), UUID.randomUUID(), dataMap,10000, 1000);
        job.addProcessExecutionlistener(this);
        ProcessWorkerImpl processWorkerImpl = new ProcessWorkerImpl(job, pi, processManagerImpl, dataMap, wpsServer);
        wpsServer.executeNewProcessWorker(processWorkerImpl);
    }

    @Override
    public void run() {

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
