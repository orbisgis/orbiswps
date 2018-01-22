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
package org.orbisgis.orbiswps.scripts;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.groovy.control.CompilationFailedException;
import org.orbisgis.orbiswps.groovyapi.attributes.OutputAttribute;

/**
 * An utility method to execute a WPS groovy script
 * 
 * @author Sylvain Palominos
 * @author Erwan Bocher
 */
public class WPSScriptExecute {

    /**
     * This method is used to test the groovy script execution. The given script is parsed ans the configured with the
     * given inputs and properties. Ons the method processing is run, the script output are tested with the given one.
     * @param groovyClassLoader the groovy class loader
     * @param scriptPath Path to the script to run.
     * @param inputMap Map containing the inputs. The keys are the script attribute name and the values are the attribute model.
     * @param propertyMap Map containing the groovy properties. The keys are the property attribute name and the values
     *                  are the property attribute model.
     * @return Map containing the process outputs. The keys are the script output attribute name and the values
     *                  are the output attribute model.
     * @throws java.lang.Exception
     */
    public static Map<String, Object> run(GroovyClassLoader groovyClassLoader, String scriptPath,
                                    Map<String, Object> propertyMap,
                                    Map<String, Object> inputMap) throws Exception{

        Map<String, Object> outputMap = new HashMap<>();

        if(groovyClassLoader==null){
            throw new Exception("The GroovyClassLoader cannot be null");
        }
        
        if(scriptPath==null){
             throw new Exception("The script '"+scriptPath+"'\n cannot be null");
        }
        
        //Step 1 : Parse the script
        Class scriptClass = null;
        try {
            File groovyFile = new File(scriptPath);
            groovyClassLoader.clearCache();
            scriptClass = groovyClassLoader.parseClass(groovyFile);
        } catch (IOException | CompilationFailedException e) {
            throw new Exception("Cannot parse the script '"+scriptPath+"'\n Cause : "+e.getLocalizedMessage());
        }        
        
        if(scriptClass==null){
            throw new Exception("The groovy object of '"+scriptPath+"' can not be null.");
        }
        
        //Step 2 : Create the groovy object
        GroovyObject groovyObject = null;
        try {
            groovyObject = (GroovyObject) scriptClass.newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
            throw new Exception("Cannot create the groovy object of '"+scriptPath+"'\n Cause : "+e.getLocalizedMessage());
        }
        
        if(inputMap!=null){
        //Step 3 : Sets the groovy object
        for(Map.Entry<String, Object> entry : inputMap.entrySet()){
            Field f = null;
            try {
                f = scriptClass.getDeclaredField(entry.getKey());
                if(f != null){
                    f.setAccessible(true);
                    f.set(groovyObject, entry.getValue());
                }
                else{
                    throw new Exception("Unable to get the field '"+entry.getKey()+"' from the script '"+ scriptPath+"'.");
                }
            } catch (NoSuchFieldException e) {
                throw new Exception("Unable to get the field '"+entry.getKey()+"' from the script '"+scriptPath+"'\n Cause : "+
                        e.getLocalizedMessage());
            } catch (IllegalAccessException e) {
                throw new Exception("Unable to set the field '"+entry.getKey()+"' from the script '"+scriptPath+"'\n Cause : "+
                        e.getLocalizedMessage());
            }
        }
        }
        
        if (propertyMap != null) {
            for (Map.Entry<String, Object> entry : propertyMap.entrySet()) {
                groovyObject.setProperty(entry.getKey(), entry.getValue());
            }
        }

        //Step 4 : Run the processing method
        groovyObject.invokeMethod("processing", null);

        //Step 5 : Test the outputs
        for(Field f : scriptClass.getDeclaredFields()){
            for(Annotation annot : f.getDeclaredAnnotations()){
                if(annot instanceof OutputAttribute) {
                    try {
                        f.setAccessible(true);
                        outputMap.put(f.getName(), f.get(groovyObject));
                    } catch (IllegalAccessException e) {
                        throw new Exception("Unable to get the field '" + f.getName() + "' from the script '" + scriptPath + "'\n Cause : " +
                                e.getLocalizedMessage());
                    }
                }
            }
        }
        return outputMap;
    }
    
}
