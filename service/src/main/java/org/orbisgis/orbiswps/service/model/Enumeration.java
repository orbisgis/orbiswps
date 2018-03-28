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
package org.orbisgis.orbiswps.service.model;

import net.opengis.wps._2_0.ComplexDataType;
import net.opengis.wps._2_0.Format;
import org.orbisgis.orbiswps.serviceapi.model.MalformedScriptException;
import org.orbisgis.orbiswps.serviceapi.model.TranslatableComplexData;
import org.xnap.commons.i18n.I18n;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Enumeration model class
 * @author Sylvain PALOMINOS
 **/

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Enumeration",
        propOrder = {"values", "names", "defaultValues", "multiSelection", "isEditable"})
public class Enumeration extends ComplexDataType implements TranslatableComplexData {

    /** List of values.*/
    @XmlElement(name = "Value", namespace = "http://orbisgis.org")
    private String[] values;
    /** List of values names.*/
    @XmlElement(name = "Name", namespace = "http://orbisgis.org")
    private String[] names;
    /** Default values.*/
    @XmlElement(name = "DefaultValue", namespace = "http://orbisgis.org")
    private String[] defaultValues;
    /** Enable or not the selection of more than one value.*/
    @XmlAttribute(name = "multiSelection")
    private boolean multiSelection = false;
    /** Enable or not the user to use its own value.*/
    @XmlAttribute(name = "isEditable")
    private boolean isEditable = false;

    /**
     * Main constructor.
     * @param formatList Formats of the model accepted.
     * @param valueList List of values.
     * @throws MalformedScriptException
     */
    public Enumeration(List<Format> formatList, String[] valueList) throws MalformedScriptException {
        format = formatList;
        this.values = valueList;
    }

    /**
     * Protected empty constructor used in the ObjectFactory class for JAXB.
     */
    protected Enumeration(){
        super();
    }

    /**
     * Returns the list of the enumeration value.
     * @return The list of values.
     */
    public String[] getValues() {
        return values;
    }

    /**
     * Sets the default values of the geometry.
     * @param defaultValues Default values of the geometry.
     */
    public void setDefaultValues(String defaultValues[]){
        this.defaultValues = defaultValues;
    }

    /**
     * Returns the default values.
     * @return The default values.
     */
    public String[] getDefaultValues() {
        return defaultValues;
    }

    /**
     * Returns true if more than one value can be selected, false otherwise.
     * @return True if more than one value can be selected, false otherwise.
     */
    public boolean isMultiSelection() {
        return multiSelection;
    }

    /**
     * Sets if the user can select more than one value.
     * @param multiSelection True if more than one value can be selected, false otherwise.
     */
    public void setMultiSelection(boolean multiSelection) {
        this.multiSelection = multiSelection;
    }

    /**
     * Returns true if the user can use a custom value, false otherwise.
     * @return True if the user can use a custom value, false otherwise.
     */
    public boolean isEditable() {
        return isEditable;
    }

    /**
     * Sets if the user can use a custom value.
     * @param editable True if the user can use a custom value, false otherwise.
     */
    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    /**
     * Sets the names of the values. The names will be only used for the displaying.
     * @param names String array of the names. It should have the same size of the values array.
     */
    public void setValuesNames(String[] names){
        this.names = names;
    }

    /**
     * Returns the array of the values name.
     * @return The array of the values name.
     */
    public String[] getValuesNames(){
        return names;
    }

    @Override
    public ComplexDataType getTranslatedData(I18n i18n, List<String> languages) {
        try {
            i18n.setLocale(Locale.forLanguageTag(languages.get(0).substring(0, 2)));
            Enumeration enumeration = new Enumeration(format, values);
            enumeration.setEditable(this.isEditable());
            enumeration.setMultiSelection(this.isMultiSelection());
            enumeration.setDefaultValues(this.getDefaultValues());
            if(this.getValuesNames() != null) {
                List<String> translatedNames = new ArrayList<>();
                for (String str : this.getValuesNames()) {
                    translatedNames.add(i18n.tr(str));
                }
                enumeration.setValuesNames(translatedNames.toArray(new String[translatedNames.size()]));
            }
            return enumeration;
        } catch (MalformedScriptException ignored) {}
        return this;
    }
}
