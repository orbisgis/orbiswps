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
package org.orbisgis.orbiswps.service.parser;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import net.opengis.wps._2_0.InputDescriptionType;
import net.opengis.wps._2_0.OutputDescriptionType;
import net.opengis.wps._2_0.ProcessDescriptionType;
import net.opengis.wps._2_0.ProcessOffering;
import org.orbisgis.orbiswps.groovyapi.attributes.InputAttribute;
import org.orbisgis.orbiswps.groovyapi.attributes.OutputAttribute;
import org.orbisgis.orbiswps.service.model.*;
import org.orbisgis.orbiswps.serviceapi.model.MalformedScriptException;
import org.orbisgis.orbiswps.serviceapi.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This controller manage the different Parser and is able to parse a script into a process.
 *
 * @author Sylvain PALOMINOS
 * @author Erwan Bocher
 **/

public class ParserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParserController.class);

    /** Parser list */
    private List<Parser> parserList;
    private ProcessParser processParser;
    private GroovyClassLoader groovyClassLoader;
    /** I18N object */
    private static final I18n I18N = I18nFactory.getI18n(ParserController.class);

    public ParserController(){
        //Instantiate the parser list
        parserList = new ArrayList<>();
        parserList.add(new LiteralDataParser());
        parserList.add(new BoundingBoxParser());
        parserList.add(new JDBCTableParser());
        parserList.add(new JDBCColumnParser());
        parserList.add(new JDBCValueParser());
        parserList.add(new EnumerationParser());
        parserList.add(new RawDataParser());
        parserList.add(new GeometryParser());
        parserList.add(new PasswordParser());
        processParser = new ProcessParser();
        groovyClassLoader = new GroovyShell().getClassLoader();
    }

    public Class getProcessClass(String filePath){
        try {
            File groovyFile = new File(filePath);
            groovyClassLoader.clearCache();
            return groovyClassLoader.parseClass(groovyFile);
        } catch (Exception e) {
            LOGGER.error(I18N.tr("Can not parse the process : {0}\n Cause : {1}.", filePath, e.getLocalizedMessage()));
        }
        return null;
    }

    public Class getProcessClass(URL url){
        //Retrieve the class corresponding to the Groovy script.
        Class clazz = null;
        try {
            groovyClassLoader.clearCache();
            clazz =  groovyClassLoader.parseClass(new GroovyCodeSource(url));
        } catch (Exception e) {
            LOGGER.error(I18N.tr("Can not parse the process : {0}\n Cause : {1}.", url, e.getLocalizedMessage()));
        }
        return clazz;
    }

    /**
     * Parse a groovy file under a wps process and the groovy class representing the script.
     * @param processUrl URL path of the file to parse.
     * @return An entry with the process and the class object.
     * @throws MalformedScriptException
     */
    public ProcessOffering parseProcess(URL processUrl) throws MalformedScriptException {
        //Retrieve the class corresponding to the Groovy script.
        Class clazz = null;
        try {
            groovyClassLoader.clearCache();
            clazz =  groovyClassLoader.parseClass(new GroovyCodeSource(processUrl));
        } catch (Exception e) {
            LOGGER.error(I18N.tr("Can not parse the process : {0}\n Cause : {1}.", processUrl, e.getLocalizedMessage()));
        }
        if(clazz == null){
            return null;
        }

        //Parse the process
        ProcessOffering processOffering;
        try {
            URI uri = processUrl.toURI();
            processOffering = processParser.parseProcess(clazz.getDeclaredMethod("processing"), uri);
            setProcessOffering(processOffering, clazz);
        } catch (NoSuchMethodException e) {
            LOGGER.error(I18N.tr("No method called 'processing' found."));
            return null;
        } catch (URISyntaxException e) {
            LOGGER.error(I18N.tr("Unable to generate the URI of the process {0}.", processUrl.toString()));
            return null;
        }
        return processOffering;
    }

    /**
     * Parse a groovy file under a wps process and the groovy class representing the script.
     * @param processPath String path of the file to parse.
     * @return An entry with the process and the class object.
     * @throws MalformedScriptException
     */
    public ProcessOffering parseProcess(String processPath) throws MalformedScriptException {
        //Retrieve the class corresponding to the Groovy script.
        File processFile = new File(processPath);
        Class clazz = getProcessClass(processPath);
        if(clazz == null){
            return null;
        }

        //Parse the process
        ProcessOffering processOffering;
        try {
            processOffering = processParser.parseProcess(clazz.getDeclaredMethod("processing"), processFile.toURI());
            setProcessOffering(processOffering, clazz);
        } catch (NoSuchMethodException e) {
            return null;
        }
        return processOffering;
    }

    /**
     * Configure the given ProcessOffering with the given class.
     * @param processOffering ProcessOffering to configure
     * @param clazz Class used to do the configuration.
     * @throws MalformedScriptException
     */
    private void setProcessOffering(ProcessOffering processOffering, Class clazz) throws MalformedScriptException {

        ProcessDescriptionType process = processOffering.getProcess();

        //Retrieve the list of input and output of the script.
        List<InputDescriptionType> inputList = new ArrayList<>();
        List<OutputDescriptionType> outputList = new ArrayList<>();
        Object scriptObject = null;
        try {
            scriptObject = clazz.newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
            LOGGER.error(I18N.tr("Unable to create a new instance of the groovy script.\nCause : {0}", e.getMessage()));
        }

        for(Field f : clazz.getDeclaredFields()){
            f.setAccessible(true);
            for(Annotation a : f.getDeclaredAnnotations()){
                if(a instanceof InputAttribute){
                    Object defaultValue = null;
                    if(scriptObject != null) {
                        try {
                            f.setAccessible(true);
                            defaultValue = f.get(scriptObject);
                        } catch (IllegalAccessException e) {
                            LOGGER.error(I18N.tr("Unable to retrieve the default value of the field : {0}.\n" +
                                    "Cause : {1}.", f, e.getMessage()));
                        }
                    }
                    //Find the good parser and parse the input.
                    boolean parsed = false;
                    for(Parser parser : parserList){
                        if(f.getAnnotation(parser.getAnnotation())!= null){
                            InputDescriptionType input = parser.parseInput(f, defaultValue,
                                    URI.create(process.getIdentifier().getValue()));
                            if(input.getInput() != null && !input.getInput().isEmpty()){
                                for(InputDescriptionType in : input.getInput()){
                                    inputList.add(in);
                                }
                            }
                            else{
                                inputList.add(input);
                            }
                            parsed = true;
                        }
                    }
                    if(!parsed){
                        throw new MalformedScriptException(ParserController.class, a.toString(),
                                I18N.tr("Unable to find the Parser fo the annotation {0}.", a.toString()));
                    }
                }
                if(a instanceof OutputAttribute){
                    Object defaultValue = null;
                    if(scriptObject != null) {
                        try {
                            f.setAccessible(true);
                            defaultValue = f.get(scriptObject);
                        } catch (IllegalAccessException e) {
                            LOGGER.error(I18N.tr("Unable to retrieve the default value of the field : {0}.\n" +
                                    "Cause : {1}.", f, e.getMessage()));
                        }
                    }
                    //Find the good parser and parse the output.
                    boolean parsed = false;
                    for(Parser parser : parserList){
                        if(f.getAnnotation(parser.getAnnotation())!= null){
                            OutputDescriptionType output = parser.parseOutput(f, defaultValue,
                                    URI.create(process.getIdentifier().getValue()));
                            if(output.getOutput() != null && !output.getOutput().isEmpty()){
                                for(OutputDescriptionType out : output.getOutput()){
                                    outputList.add(out);
                                }
                            }
                            else{
                                outputList.add(output);
                            }
                            parsed = true;
                        }
                    }
                    if(!parsed){
                        throw new MalformedScriptException(ParserController.class, a.toString(),
                                I18N.tr("Unable to find the Parser for the annotation {0}.", a.toString()));
                    }
                }
            }
        }
        process.getOutput().clear();
        process.getOutput().addAll(outputList);
        process.getInput().clear();
        process.getInput().addAll(inputList);

        link(processOffering.getProcess());
    }

    /**
     * Links the input and output with the 'parent'.
     * i.e. : The JDBCTable contains a list of JDBCColumn related.
     * @param p Process to link.
     */
    private void link(ProcessDescriptionType p){
        //Link the JDBCColumn with its JDBCTable
        for(InputDescriptionType i : p.getInput()){
            if(i.getDataDescription().getValue() instanceof JDBCColumn){
                JDBCColumn jdbcColumn = (JDBCColumn)i.getDataDescription().getValue();
                for(InputDescriptionType jdbcTable : p.getInput()){
                    if(jdbcTable.getIdentifier().getValue().equals(jdbcColumn.getJDBCTableIdentifier().toString())){
                        ((JDBCTable)jdbcTable.getDataDescription().getValue()).addJDBCColumn(jdbcColumn);
                    }
                }
            }
        }
        //Link the JDBCValue with its JDBCColumn and its JDBCTable
        for(InputDescriptionType i : p.getInput()){
            if(i.getDataDescription().getValue() instanceof JDBCValue){
                JDBCValue jdbcValue = (JDBCValue)i.getDataDescription().getValue();
                for(InputDescriptionType input : p.getInput()){
                    if(input.getIdentifier().getValue().equals(jdbcValue.getJDBCColumnIdentifier().toString())){
                        JDBCColumn jdbcColumn = (JDBCColumn)input.getDataDescription().getValue();
                        jdbcColumn.addJDBCValue(jdbcValue);
                        jdbcValue.setJDBCTableIdentifier(jdbcColumn.getJDBCTableIdentifier());
                    }
                }
            }
        }
        //Link the JDBCColumn with its JDBCTable
        for(OutputDescriptionType o : p.getOutput()){
            if(o.getDataDescription().getValue() instanceof JDBCColumn){
                JDBCColumn jdbcColumn = (JDBCColumn)o.getDataDescription().getValue();
                for(OutputDescriptionType jdbcTable : p.getOutput()){
                    if(jdbcTable.getIdentifier().getValue().equals(jdbcColumn.getJDBCTableIdentifier().toString())){
                        ((JDBCTable)jdbcTable.getDataDescription().getValue()).addJDBCColumn(jdbcColumn);
                    }
                }
            }
        }
        //Link the JDBCValue with its JDBCColumn and its JDBCTable
        for(OutputDescriptionType o : p.getOutput()){
            if(o.getDataDescription().getValue() instanceof JDBCValue){
                JDBCValue jdbcValue = (JDBCValue)o.getDataDescription().getValue();
                for(OutputDescriptionType output : p.getOutput()){
                    if(output.getIdentifier().getValue().equals(jdbcValue.getJDBCColumnIdentifier().toString())){
                        JDBCColumn jdbcColumn = (JDBCColumn)output.getDataDescription().getValue();
                        jdbcColumn.addJDBCValue(jdbcValue);
                        jdbcValue.setJDBCTableIdentifier(jdbcColumn.getJDBCTableIdentifier());
                    }
                }
            }
        }
    }
}
