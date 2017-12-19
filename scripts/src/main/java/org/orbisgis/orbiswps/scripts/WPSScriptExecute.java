/*
 * Copyright (C) 2017 Lab-STICC - UMR CNRS 6285
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.orbisgis.orbiswps.scripts;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.codehaus.groovy.control.CompilationFailedException;

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
     * @param inputMap Map containing the inputs. The keys are the script attribute name and the values are the attribute data.
     * @param propertyMap Map containing the groovy properties. The keys are the property attribute name and the values
     *                    are the property attribute data.
     * @param outputMap Map containing the desired outputs. The keys are the script output attribute name and the values
     *                  are the desired output attribute data which will be tested.
     * @throws java.lang.Exception
     */
    public static  void run(GroovyClassLoader groovyClassLoader, String scriptPath,
                                    Map<String, Object> propertyMap,
                                    Map<String, Object> inputMap,
                                    Map<String, Object> outputMap) throws Exception{
        if(groovyClassLoader==null){
            throw new Exception("The GroovyClassLoader cannot be null"); 
        }
        
        if(scriptPath==null){
             throw new Exception("The script '"+scriptPath+"'\n cannot be null");    
        }  
        
        if(outputMap==null){
            throw new Exception("The outputMap cannot be null"); 
        }
         
        if(outputMap.isEmpty()){
            throw new Exception("The outputMap must contains at leat one key - value");
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
        for(Map.Entry<String, Object> entry : outputMap.entrySet()){
            Field f = null;
            try {
                f = scriptClass.getDeclaredField(entry.getKey());
                if(f != null){
                    f.setAccessible(true);
                    outputMap.put(entry.getKey(), f.get(groovyObject));
                }
                else{    
                    throw new Exception("Unable to get the field '" + entry.getKey() + "' from the script '"
                            + scriptPath + "'.");
                }
            } catch (IllegalAccessException|NoSuchFieldException e) {
                throw new Exception("Unable to get the field '"+entry.getKey()+"' from the script '"+scriptPath+"'\n Cause : "+
                        e.getLocalizedMessage());
            }
        }
    }
    
}
