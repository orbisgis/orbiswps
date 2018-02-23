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
import org.orbisgis.orbiswps.serviceapi.process.ProcessIdentifier;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Class containing information to identify a process.
 *
 * @author Sylvain PALOMINOS
 **/

public class ProcessIdentifierImpl implements ProcessIdentifier {

    /** ProcessOffering object. */
    private ProcessOffering processOffering;
    /** File path of the process. */
    private String filePath;
    /** Source URL. */
    private URL sourceUrl;

    private I18n i18n;

    private Map<String, Object> properties;

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
        this.i18n = I18nFactory.getI18n(ProcessIdentifierImpl.class);
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
        this.i18n = I18nFactory.getI18n(ProcessIdentifierImpl.class);
    }

    @Override
    public void setI18n(I18n i18n){
        this.i18n = i18n;
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

    public I18n getI18n() {
        return i18n;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }
}
