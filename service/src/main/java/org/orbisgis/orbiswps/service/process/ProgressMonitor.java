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

import org.h2gis.api.ProgressVisitor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Process monitor used in the WPS script to set the progression of a process. As it extends the H2GIS ProgressVisitor
 * interface, it can be use in the drivers export function for example.
 *
 * @author Sylvain PALOMINOS
 */
public class ProgressMonitor implements ProgressVisitor {

    /** Properties for the PropertiesChangeEvent. */
    public static String PROPERTY_PROGRESS = "PROPERTY_PROGRESS";
    public static String PROPERTY_CANCEL = "PROPERTY_CANCEL";
    public static String PROPERTY_NAME = "PROPERTY_NAME";

    /** Progress done by the process. Should be between 0.0 and 1.0 included. */
    private double progressDone = 0;
    /** Name of the task. */
    private String taskName;
    /** PropertyChangeSupport object managing the PropertyChangeEvent. */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    /** True if the process has been cancelled, false otherwise. */
    private boolean isCancelled;
    /** Count of step to do. */
    private int stepCount = 1;
    /** Count of step done. */
    private int stepDone = 0;

    /**
     * Main constructor.
     * @param taskName Name of the task.
     */
    public ProgressMonitor(String taskName) {
        this.isCancelled = false;
        this.taskName = taskName;
    }

    /**
     * Increase the progression with the given value.
     * @param incProg Increment of the progression.
     */
    private synchronized void pushProgression(double incProg) {
        if(progressDone + incProg > 1.0){
            progressDone = 1.0;
        }
        else{
            progressDone += incProg;
        }
    }

    /**
     * Returns the task name.
     * @return The task name.
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Sets te task name.
     * @param taskName New task name.
     */
    public void setTaskName(String taskName){
        String oldName = taskName;
        this.taskName = taskName;
        triggerPropertyChangeEvent(PROPERTY_NAME, oldName, taskName);
    }

    /**
     * Progress to the given value
     * @param progress Progression value to use to set the progressDone.
     */
    public void progressTo(long progress) {
        double oldProgress = progressDone;
        pushProgression((double)progress/stepCount-progressDone);
        triggerPropertyChangeEvent(PROPERTY_PROGRESS, oldProgress*100, progressDone*100);
    }

    @Override
    public void endOfProgress() {
        progressTo(stepCount);
    }

    @Override
    public ProgressVisitor subProcess(int i) {
        stepCount = i;
        stepDone = 0;
        return this;
    }

    @Override
    public void endStep() {
        stepDone++;
        progressTo(stepDone);
    }

    @Override
    public void setStep(int i) {
        stepDone = i;
        progressTo(stepDone);
    }

    @Override
    public int getStepCount() {
        return stepCount;
    }

    @Override
    public double getProgression() {
        return progressDone;
    }

    @Override
    public boolean isCanceled() {
        return isCancelled;
    }

    @Override
    public void cancel() {
        boolean oldValue = this.isCancelled;
        this.isCancelled = true;
        propertyChangeSupport.firePropertyChange(PROPERTY_CANCEL, oldValue, isCancelled);
    }

    @Override
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(property, listener);
    }

    /**
     * Removes the given PropertyChangeListener object.
     * @param listener Listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Creates a PropertyChangeEvent and fire it.
     * @param propertyName Name of the property.
     * @param oldValue Old value of the property.
     * @param newValue New Value of the property.
     */
    private void triggerPropertyChangeEvent(String propertyName, Object oldValue, Object newValue){
        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        propertyChangeSupport.firePropertyChange(event);
    }
}
