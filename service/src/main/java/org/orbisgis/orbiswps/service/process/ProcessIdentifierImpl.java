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
import net.opengis.wps._2_0.ProcessOffering;
import org.orbisgis.orbiswps.service.model.wpsmodel.WpsModel;
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.net.URL;
import java.util.Map;

/**
 * Class containing information to identify a process.
 *
 * @author Sylvain PALOMINOS
 **/

public class ProcessIdentifierImpl implements ProcessIdentifier {

    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(ProcessIdentifierImpl.class);


    /** ProcessOffering object. */
    private ProcessOffering processOffering;
    /** File path of the process. */
    private String filePath;
    /** Source URL. */
    private URL sourceUrl;

    private I18n processI18n = I18N;

    private Map<String, Object> properties;

    private WpsModel model;

    /**
     * Main constructor.
     *
     * @param processOffering ProcessOffering containing all the information about a process.
     * @param filePath String representation of the file path.
     */
    public ProcessIdentifierImpl(ProcessOffering processOffering, String filePath){
        this.processOffering = processOffering;
        this.filePath = filePath;
        this.sourceUrl = null;
    }

    /**
     * Main constructor.
     *
     * @param processOffering ProcessOffering containing all the information about a process.
     * @param sourceUrl Source URL of the file.
     */
    public ProcessIdentifierImpl(ProcessOffering processOffering, URL sourceUrl){
        this.processOffering = processOffering;
        this.filePath = null;
        this.sourceUrl = sourceUrl;
    }

    @Override
    public void setProcessI18n(I18n processI18n){
        this.processI18n = processI18n;
    }

    /**
     * Returns the ProcessDescriptionType object.
     * @return The ProcessDescriptionType object.
     */
    public ProcessDescriptionType getProcessDescriptionType() {
        return processOffering.getProcess();
    }


    /**
     * Returns the ProcessOffering object.
     * @return The ProcessOffering object.
     */
    public ProcessOffering getProcessOffering(){
        return processOffering;
    }

    /**
     * Returns the process file path.
     * @return The process file path.
     */
    public String getFilePath(){
        return filePath;
    }

    /**
     * Returns the source URL of the file.
     * @return The source URL.
     */
    public URL getSourceUrl(){
        return sourceUrl;
    }

    /**
     * Returns the process I18N object.
     * @return The process I18N object.
     */
    public I18n getProcessI18n() {
        if(processI18n == null){
            processI18n = I18N;
        }
        return processI18n;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Returns true if the process identifier represents a model and not a simple process. Returns false otherwise.
     * @return true if the process identifier represents a model and not a simple process.
     */
    public boolean isModel(){
        return model != null;
    }

    /**
     * Sets the model.
     * @param model Model of the ProcessIdentifier
     */
    public void setModel(WpsModel model){
        this.model = model;
    }

    /**
     * Returns the model.
     */
    public WpsModel getModel(){
        return model;
    }

}
