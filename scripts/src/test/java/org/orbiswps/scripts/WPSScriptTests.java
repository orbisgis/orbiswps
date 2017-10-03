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
package org.orbiswps.scripts;

import groovy.lang.GroovyClassLoader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Erwan Bocher
 */
public class WPSScriptTests {

    private static Connection connection;
    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader());

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(WPSScriptTests.class.getSimpleName());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }
    private Statement st;

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void testBasicScript() throws Exception {
        String scriptPath = WPSScriptTests.class.getResource("basicWPSScript.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputValue", "good ");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("str", "value");
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("outputValue", "Not executed");
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        System.out.println(outputMap.get("outputValue"));
    }

}
