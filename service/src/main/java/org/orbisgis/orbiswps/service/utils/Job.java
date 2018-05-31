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
package org.orbisgis.orbiswps.service.utils;

import net.opengis.wps._2_0.ProcessDescriptionType;
import org.orbisgis.orbiswps.serviceapi.process.ProcessExecutionListener;
import org.orbisgis.orbiswps.service.process.ProgressMonitor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.*;

/**
 * Server-side object created by a processing service in response for a particular process execution.
 *
 * @author Sylvain PALOMINOS
 */
public class Job implements ProcessExecutionListener, PropertyChangeListener {

    /** Process polling time in milliseconds. */
    private final long MAX_PROCESS_POLLING_DELAY_MILLIS;
    private final long BASE_PROCESS_POLLING_DELAY_MILLIS;
    /** WPS process */
    private ProcessDescriptionType process;
    /** Unique identifier of the job */
    private UUID id;
    /** Time when the process has been started */
    private long startTime = -1;
    /** Map containing all the log messages */
    private Map<String, LogType> logMap;
    /** State of the process running */
    private ProcessState state;
    /** Map of the input/output model of the process execution */
    private Map<URI, Object> dataMap;
    /** Actual process polling delay in milliseconds */
    private long processPollingDelay;
    /** Progress of the job. */
    private int progress = 0;

    private List<ProcessExecutionListener> processExecutionListeners;

    /**
     * Main constructor.
     *
     * @param process Process done in the job.
     * @param id UUID of the job.
     * @param dataMap Map containing the model for the process execution.
     * @param maxPollingDelay Maximum value of the polling delay.
     * @param basePollingDelay Base value of the polling delay.
     */
    public Job(ProcessDescriptionType process, UUID id, Map<URI, Object> dataMap,
               long maxPollingDelay, long basePollingDelay){
        this.process = process;
        this.id = id;
        logMap = new HashMap<>();
        state = ProcessState.ACCEPTED;
        this.dataMap = dataMap;
        MAX_PROCESS_POLLING_DELAY_MILLIS = maxPollingDelay;
        BASE_PROCESS_POLLING_DELAY_MILLIS = basePollingDelay;
        processPollingDelay = basePollingDelay;
        processExecutionListeners = new ArrayList<>();
    }

    public void addProcessExecutionlistener(ProcessExecutionListener listener){
        processExecutionListeners.add(listener);
    }

    public void removeProcessExecutionListener(ProcessExecutionListener listener){
        processExecutionListeners.remove(listener);
    }

    /**
     * Returns the start time.
     * @return The start time.
     */
    public long getStartTime(){
        return startTime;
    }

    @Override
    public void appendLog(LogType logType, String message) {
        for(ProcessExecutionListener listener : processExecutionListeners){
            listener.appendLog(logType, message);
        }
        logMap.put(message, logType);
    }

    @Override
    public void setProcessState(ProcessState processState) {
        if(startTime == -1){
            startTime = System.currentTimeMillis();
        }
        state = processState;
        for(ProcessExecutionListener listener : processExecutionListeners){
            listener.setProcessState(processState);
        }
    }

    /**
     * Returns the process state.
     * @return The process state.
     */
    public ProcessState getState(){
        return state;
    }

    /**
     * Returns the dataMap.
     * @return The dataMap.
     */
    public Map<URI, Object> getDataMap(){
        return dataMap;
    }

    /**
     * Returns the logMap.
     * @return The logMap.
     */
    public Map<String, LogType> getLogMap(){
        return logMap;
    }

    /**
     * Returns the process.
     * @return The process.
     */
    public ProcessDescriptionType getProcess(){
        return process;
    }

    /**
     * Returns the job id.
     * @return the job id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the process polling time.
     * @return The process polling time.
     */
    public long getProcessPollingTime(){
        long time = processPollingDelay;
        if(processPollingDelay < MAX_PROCESS_POLLING_DELAY_MILLIS) {
            processPollingDelay += BASE_PROCESS_POLLING_DELAY_MILLIS;
        }
        return time;
    }

    /**
     * Set the job progress. Should be between 0and 100 included.
     * @param progress new progress.
     */
    public void setProgress(int progress){
        this.progress = progress;
    }

    /**
     * Returns the job progress.
     * @return The job progress.
     */
    public int getProgress(){
        return progress;
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if(propertyChangeEvent.getPropertyName().equals(ProgressMonitor.PROPERTY_PROGRESS)){
            Object value = propertyChangeEvent.getNewValue();
            if(value instanceof Double){
                setProgress(((Double)value).intValue());
            }
            else {
                setProgress((int)value);
            }
        }
    }
}
