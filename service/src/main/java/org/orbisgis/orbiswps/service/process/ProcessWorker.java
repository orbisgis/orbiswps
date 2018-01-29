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
package org.orbisgis.orbiswps.service.process;

import net.opengis.wps._2_0.ProcessDescriptionType;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.serviceapi.process.ProcessExecutionListener;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.orbisgis.orbiswps.service.utils.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

/**
 * Class extending the SwingWorkerPM class dedicated to the WPS process execution.
 *
 * @author Sylvain PALOMINOS
 */
public class ProcessWorker implements Runnable, PropertyChangeListener {

    /** Process execution listener which will be watching the execution */
    private Job job;
    /** Process to execute */
    private ProcessIdentifier processIdentifier;
    /** The process manager */
    private ProcessManager processManager;
    /** Map containing the process execution output/input model and URI */
    private Map<URI, Object> dataMap;
    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(ProcessWorker.class);
    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessWorker.class);
    private ProgressMonitor progressMonitor;
    private WpsServerImpl wpsServer;

    public ProcessWorker(Job job,
                         ProcessIdentifier processIdentifier,
                         ProcessManager processManager,
                         Map<URI, Object> dataMap,
                         WpsServerImpl wpsServer){
        this.job = job;
        this.processIdentifier = processIdentifier;
        this.processManager = processManager;
        this.dataMap = dataMap;
        this.wpsServer = wpsServer;
        progressMonitor = new ProgressMonitor(job.getProcess().getTitle().get(0).getValue());
        progressMonitor.addPropertyChangeListener(ProgressMonitor.PROPERTY_PROGRESS, this.job);
        progressMonitor.addPropertyChangeListener(ProgressMonitor.PROPERTY_CANCEL, this);
    }

    @Override
    public void run() {
        String title = job.getProcess().getTitle().get(0).getValue();
        progressMonitor.setTaskName(I18N.tr("{0} : Preprocessing", title));
        if(job != null) {
            job.setProcessState(ProcessExecutionListener.ProcessState.RUNNING);
        }
        ProcessDescriptionType process = processIdentifier.getProcessDescriptionType();
        //Catch all the Exception that can be thrown during the script execution.
        try {
            //Print in the log the process execution start
            if(job != null) {
                job.appendLog(ProcessExecutionListener.LogType.INFO, I18N.tr("Start the process."));
            }

            //Pre-process the model
            if(job != null) {
                job.appendLog(ProcessExecutionListener.LogType.INFO, I18N.tr("Pre-processing."));
            }

            //Execute the process and retrieve the groovy object.
            if(job != null) {
                job.appendLog(ProcessExecutionListener.LogType.INFO, I18N.tr("Execute the script."));
            }
            progressMonitor.setTaskName(I18N.tr("{0} : Execution", title));
            processManager.executeProcess(job.getId(), processIdentifier, dataMap, processIdentifier.getProperties(), progressMonitor);
            progressMonitor.setTaskName(I18N.tr("{0} : Postprocessing", title));
            //Post-process the model
            if(job != null) {
                job.appendLog(ProcessExecutionListener.LogType.INFO, I18N.tr("Post-processing."));
            }

            //Print in the log the process execution end
            if(job != null) {
                job.appendLog(ProcessExecutionListener.LogType.INFO, I18N.tr("End of the process."));
                job.setProcessState(ProcessExecutionListener.ProcessState.SUCCEEDED);
            }
            progressMonitor.endOfProgress();
            wpsServer.onProcessWorkerFinished();
        }
        catch (Exception e) {
            if(job != null) {
                job.setProcessState(ProcessExecutionListener.ProcessState.FAILED);
                LOGGER.error(e.getLocalizedMessage());
                //Print in the log the process execution error
                job.appendLog(ProcessExecutionListener.LogType.ERROR, e.getMessage());
            }
            else{
                LOGGER.error(I18N.tr("Error on execution the WPS  process {0}.\nCause : {1}.",
                        process.getTitle(),e.getMessage()));
            }
            wpsServer.onProcessWorkerFinished();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if(propertyChangeEvent.getPropertyName().equals(ProgressMonitor.PROPERTY_CANCEL)){
            processManager.cancelProcess(job.getId());
        }
    }

    public UUID getJobId(){
        return job.getId();
    }
}
