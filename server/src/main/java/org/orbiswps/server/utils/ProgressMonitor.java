/**
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
 * OrbisGIS is distributed under GPL 3 license.
 *
 * Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbiswps.server.utils;

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

    public static String PROPERTY_PROGRESS = "PROPERTY_PROGRESS";
    public static String PROPERTY_CANCEL = "PROPERTY_CANCEL";

    private double progressDone = 0;
    private String taskName;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private boolean isCancelled;
    private int stepCount;
    private int stepDone = 0;

    public ProgressMonitor(String taskName) {
        this.isCancelled = false;
        this.taskName = taskName;
    }

    private synchronized void pushProgression(double incProg) {
        if(progressDone + incProg > 1.0){
            progressDone = 1.0;
        }
        else{
            progressDone += incProg;
        }
    }

    public String getCurrentTaskName() {
        return taskName;
    }

    public void progressTo(long progress) {
        double oldProgress = progressDone;
        pushProgression((double)progress/stepCount);
        triggerPropertyChangeEvent(PROPERTY_PROGRESS, oldProgress*100, progressDone*100);
    }

    public void setTaskName(String taskName){
        this.taskName = taskName;
    }

    @Override
    public void endOfProgress() {
        progressTo(1);
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

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private void triggerPropertyChangeEvent(String propertyName, Object oldValue, Object newValue){
        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        propertyChangeSupport.firePropertyChange(event);
    }
}
