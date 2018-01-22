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
package org.orbisgis.orbiswps.service.controller.parser;

import net.opengis.ows._2.CodeType;
import net.opengis.wps._2_0.Format;
import net.opengis.wps._2_0.InputDescriptionType;
import net.opengis.wps._2_0.OutputDescriptionType;
import org.orbisgis.orbiswps.groovyapi.attributes.DescriptionTypeAttribute;
import org.orbisgis.orbiswps.groovyapi.attributes.JDBCValueAttribute;
import org.orbisgis.orbiswps.groovyapi.attributes.InputAttribute;
import org.orbisgis.orbiswps.service.controller.utils.FormatFactory;
import org.orbisgis.orbiswps.service.controller.utils.ObjectAnnotationConverter;
import org.orbisgis.orbiswps.service.model.JDBCValue;
import org.orbisgis.orbiswps.serviceapi.data.MalformedScriptException;
import org.orbisgis.orbiswps.service.model.ObjectFactory;
import org.orbisgis.orbiswps.serviceapi.parser.Parser;

import javax.xml.bind.JAXBElement;
import java.lang.reflect.Field;
import java.net.URI;

/**
 * Parser for the JDBCValue input/output annotations.
 *
 * @author Sylvain PALOMINOS
 * @author Erwan Bocher
 **/

public class JDBCValueParser implements Parser {

    @Override
    public InputDescriptionType parseInput(Field f, Object defaultValue, URI processId) throws MalformedScriptException {
        //Instantiate the JDBCValue object
        JDBCValueAttribute jdbcValueAttribute = f.getAnnotation(JDBCValueAttribute.class);
        Format format = FormatFactory.getFormatFromExtension(FormatFactory.TEXT_EXTENSION);
        URI jdbcColumnUri;
        //If the jdbcValue attribute is not an URI, autoGenerate one.
        jdbcColumnUri = URI.create(processId+":"+jdbcValueAttribute.jdbcColumnReference());
        JDBCValue jdbcValue = ObjectAnnotationConverter.annotationToObject(jdbcValueAttribute, format, jdbcColumnUri);
        if(defaultValue != null && defaultValue instanceof String[]) {
            jdbcValue.setDefaultValues((String[])defaultValue);
        }

        //Instantiate the returned input
        InputDescriptionType input = new InputDescriptionType();
        JAXBElement<JDBCValue> jaxbElement = new ObjectFactory().createJDBCValue(jdbcValue);
        input.setDataDescription(jaxbElement);

        ObjectAnnotationConverter.annotationToObject(f.getAnnotation(InputAttribute.class), input);
        ObjectAnnotationConverter.annotationToObject(f.getAnnotation(DescriptionTypeAttribute.class), input,
                processId.toString());

        if(input.getIdentifier() == null){
            CodeType codeType = new CodeType();
            codeType.setValue(processId+":"+f.getName());
            input.setIdentifier(codeType);
        }

        return input;
    }

    @Override
    public OutputDescriptionType parseOutput(Field f, Object defaultValue, URI processId) throws MalformedScriptException {
        //Instantiate the JDBCValue object
        JDBCValueAttribute jdbcValueAttribute = f.getAnnotation(JDBCValueAttribute.class);
        Format format = FormatFactory.getFormatFromExtension(FormatFactory.TEXT_EXTENSION);
        URI jdbcColumnUri;
        //If the jdbcValue attribute is not an URI, autoGenerate one.
        jdbcColumnUri = URI.create(processId+":"+jdbcValueAttribute.jdbcColumnReference());
        JDBCValue jdbcValue = ObjectAnnotationConverter.annotationToObject(jdbcValueAttribute, format, jdbcColumnUri);
        if(defaultValue != null && defaultValue instanceof String[]) {
            jdbcValue.setDefaultValues((String[])defaultValue);
        }

        //Instantiate the returned output
        OutputDescriptionType output = new OutputDescriptionType();
        JAXBElement<JDBCValue> jaxbElement = new ObjectFactory().createJDBCValue(jdbcValue);
        output.setDataDescription(jaxbElement);

        ObjectAnnotationConverter.annotationToObject(f.getAnnotation(DescriptionTypeAttribute.class), output,
                processId.toString());

        if(output.getIdentifier() == null){
            CodeType codeType = new CodeType();
            codeType.setValue(processId+":"+f.getName());
            output.setIdentifier(codeType);
        }

        return output;
    }

    @Override
    public Class getAnnotation() {
        return JDBCValueAttribute.class;
    }
}
