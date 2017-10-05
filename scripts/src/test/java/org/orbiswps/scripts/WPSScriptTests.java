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
import groovy.sql.Sql;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import static junit.framework.Assert.assertTrue;
import org.h2.tools.RunScript;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;

/**
 *
 * @author Erwan Bocher
 */
public class WPSScriptTests {

    private static Connection connection;
    private static Sql sql;
    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader());

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(WPSScriptTests.class.getSimpleName());
        sql = new Sql(connection);        
        // Set up test data
        executeScript(connection, "wps_scripts_test.sql");
        
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
    
     static void executeScript(Connection connection, String fileName) throws SQLException
    {
    	 InputStreamReader reader = new InputStreamReader(
 				WPSScriptTests.class.getResourceAsStream(fileName));
 		RunScript.execute(connection, reader);

 		try {
 			reader.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
    }

    @Test
    public void testBasicScript() throws Exception {
        String scriptPath = WPSScriptTests.class.getResource("basicWPSScript.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputValue", "good ");
        Map<String, Object> propertyMap = new HashMap<>();
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("outputValue", "Not executed");
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        Assert.assertEquals("The script has been executed.",outputMap.get("outputValue"));
    }
    
     @Test
    public void testBasicScriptNullProperties() throws Exception {
        String scriptPath = WPSScriptTests.class.getResource("basicWPSScript.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputValue", "good ");
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("outputValue", "Not executed");
        WPSScriptExecute.run(groovyClassLoader, scriptPath, null, inputMap, outputMap);
        Assert.assertEquals("The script has been executed.",outputMap.get("outputValue"));
    }
    
    @Test
    public void testScriptWithoutInput() throws Exception {
        String scriptPath = WPSScriptTests.class.getResource("wpsScriptWithoutInput.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        Map<String, Object> propertyMap = new HashMap<>();
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("outputValue", "Not executed");
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        Assert.assertEquals("The script has been executed.",outputMap.get("outputValue"));
    }
    
    @Test
    public void testFixedDistanceBuffer() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Buffer/fixedDistanceBuffer.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "input_table_a ");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("bufferSize", 12.0d);
        inputMap.put("fieldList", new String[]{"id"});
        inputMap.put("outputTableName", "buffer_table");        
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("literalOutput", "Not executed");        
         //Drop the output table(s)
        st.execute("DROP TABLE if exists buffer_table");  
        //Execute
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM buffer_table;");
        assertTrue(rs.next());
        Assert.assertEquals(2,rs.getInt(2));
        assertGeometryEquals
        rs.close();
    }
    
    @Test
    public void testVariableDistanceBuffer() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Buffer/variableDistanceBuffer.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "input_table_a ");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("bufferSize", new String[]{"id"});
        inputMap.put("fieldList", new String[]{"id"});
        inputMap.put("outputTableName", "buffer_table");        
        Map<String, Object> propertyMap = new HashMap<>();
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("literalOutput", "Not executed");
         //Drop the output table(s)
        st.execute("DROP TABLE if exists buffer_table");       
        //Execute
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM buffer_table;");
        assertTrue(rs.next());
        Assert.assertEquals(2,rs.getInt(2));
        rs.close();
    }
    
    @Test
    public void testExtractCenter1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Convert/extractCenter.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "input_table_a ");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("fieldList", new String[]{"id"});
        inputMap.put("outputTableName", "center_table");        
        Map<String, Object> propertyMap = new HashMap<>();
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("literalOutput", "Not executed");
        //Drop the output table(s)
        st.execute("DROP TABLE if exists center_table");
        //Execute
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM buffer_table;");
        assertTrue(rs.next());
        Assert.assertEquals(2,rs.getInt(2));
        rs.close();
    }


}
