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

import com.vividsolutions.jts.geom.Geometry;
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
        inputMap.put("bufferSize", 2.0d);
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
        assertGeometryEquals("POLYGON ((219.90664392650675 161.9978199727558, 220.29819307909673 161.9776452886144, 220.67828283888568 161.88147080510785, 221.03230656956163 161.712992453702, 221.3466593376935 161.47868476295758, 221.6092607426931 161.18755204602869, 221.8100191598348 160.85078236995776, 221.9412195557345 160.48131760453361, 221.9978199727558 160.09335607349325, 221.9776452886144 159.70180692090327, 221.88147080510785 159.32171716111432, 221.712992453702 158.96769343043837, 221.47868476295758 158.6533406623065, 221.18755204602869 158.3907392573069, 220.85078236995776 158.1899808401652, 220.48131760453361 158.0587804442655, 220.09335607349325 158.0021800272442, 113.09335607349325 153.0021800272442, 112.70180692090325 153.0223547113856, 112.32171716111434 153.11852919489215, 111.96769343043839 153.287007546298, 111.65334066230649 153.52131523704242, 111.39073925730692 153.81244795397131, 111.1899808401652 154.14921763004224, 111.05878044426551 154.51868239546639, 111.00218002724422 154.90664392650675, 111.02235471138559 155.29819307909673, 111.11852919489215 155.67828283888568, 111.28700754629801 156.03230656956163, 111.52131523704243 156.3466593376935, 111.81244795397133 156.6092607426931, 112.14921763004224 156.8100191598348, 112.51868239546639 156.9412195557345, 112.90664392650673 156.9978199727558, 219.90664392650675 161.9978199727558))", rs.getString(1));
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
        assertGeometryEquals("POLYGON ((219.90664392650675 161.9978199727558, 220.29819307909673 161.9776452886144, 220.67828283888568 161.88147080510785, 221.03230656956163 161.712992453702, 221.3466593376935 161.47868476295758, 221.6092607426931 161.18755204602869, 221.8100191598348 160.85078236995776, 221.9412195557345 160.48131760453361, 221.9978199727558 160.09335607349325, 221.9776452886144 159.70180692090327, 221.88147080510785 159.32171716111432, 221.712992453702 158.96769343043837, 221.47868476295758 158.6533406623065, 221.18755204602869 158.3907392573069, 220.85078236995776 158.1899808401652, 220.48131760453361 158.0587804442655, 220.09335607349325 158.0021800272442, 113.09335607349325 153.0021800272442, 112.70180692090325 153.0223547113856, 112.32171716111434 153.11852919489215, 111.96769343043839 153.287007546298, 111.65334066230649 153.52131523704242, 111.39073925730692 153.81244795397131, 111.1899808401652 154.14921763004224, 111.05878044426551 154.51868239546639, 111.00218002724422 154.90664392650675, 111.02235471138559 155.29819307909673, 111.11852919489215 155.67828283888568, 111.28700754629801 156.03230656956163, 111.52131523704243 156.3466593376935, 111.81244795397133 156.6092607426931, 112.14921763004224 156.8100191598348, 112.51868239546639 156.9412195557345, 112.90664392650673 156.9978199727558, 219.90664392650675 161.9978199727558))", rs.getString(1));
        rs.close();
    }
    
    @Test
    public void testExtractCenter1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Convert/extractCenter.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "input_table_a ");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("fieldList", new String[]{"id", "type"});
        inputMap.put("outputTableName", "center_table");        
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("literalOutput", "Not executed");
        //Drop the output table(s)
        st.execute("DROP TABLE if exists center_table");
        //Execute
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM center_table;");
        assertTrue(rs.next());
           assertGeometryEquals("POINT (166.5 157.5)", rs.getString(1));
        Assert.assertEquals(2,rs.getInt(2));
        Assert.assertEquals("OrbisGIS",rs.getString(3));
        rs.close();
    }
    
    @Test
    public void testExtractCenter2() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Convert/extractCenter.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "input_table_a ");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("fieldList", new String[]{"id"});
        inputMap.put("operation", new String[]{"interior"});
        inputMap.put("outputTableName", "center_table");        
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);       
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("literalOutput", "Not executed");
        //Drop the output table(s)
        st.execute("DROP TABLE if exists center_table");
        //Execute
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM center_table;");
        assertTrue(rs.next());      
        assertGeometryEquals("POINT (113 155)", rs.getString(1));
        Assert.assertEquals(2,rs.getInt(2));
        rs.close();
    }

    @Test
    public void testReproject1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Transform/reprojectGeometries.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "to_be_reprojected ");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("fieldList", new String[]{"id"});
        inputMap.put("srid", new String[]{"2154"});
        inputMap.put("outputTableName", "reprojected_table");        
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);       
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("literalOutput", "Not executed");
        //Add table to reproject
        st.execute("drop table if exists to_be_reprojected"
                + "; create table to_be_reprojected(the_geom point, id int);"
                + "insert into to_be_reprojected values(st_geomfromtext('POINT(2.114551393 50.345609791)',4326), 99);");
        //Drop the output table(s)
        st.execute("DROP TABLE if exists reprojected_table");
        //Execute
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM reprojected_table;");
        assertTrue(rs.next());      
        Geometry geom = (Geometry) rs.getObject(1);
        Assert.assertEquals(2154,geom.getSRID());
        assertGeometryEquals("POINT(636890.7403226076 7027895.263553156)",geom.toText());
        Assert.assertEquals(99,rs.getInt(2));
        st.execute("drop table if exists to_be_reprojected,reprojected_table");
        rs.close();
    }
    
    @Test
    public void testAttributeFiltering1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Select/attributeFiltering.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("fromSelectedTable", "input_table_b");        
        inputMap.put("fromSelectedColumn", new String[]{"id"});     
        inputMap.put("operation", new String[]{"="});  
        inputMap.put("fromSelectedValue", "2");    
        inputMap.put("dropTable", true);    
        inputMap.put("outputTableName", "input_table_b_filetered");        
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);       
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("literalOutput", "Not executed");
        //Execute
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM input_table_b_filetered;");
        assertTrue(rs.next());      
        Assert.assertEquals("CNRS", rs.getString(3));
        rs.close();
    }
    
    @Test
    public void testAttributeFiltering2() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Select/attributeFiltering.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("fromSelectedTable", "input_table_b");        
        inputMap.put("fromSelectedColumn", new String[]{"id"});     
        inputMap.put("operation", new String[]{"limit"});  
        inputMap.put("fromSelectedValue", "2");    
        inputMap.put("dropTable", true);    
        inputMap.put("outputTableName", "input_table_b_filetered");        
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);       
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("literalOutput", "Not executed");
        //Execute
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT count(*) FROM input_table_b_filetered;");
        assertTrue(rs.next());      
        Assert.assertEquals(2, rs.getInt(1));
        rs.close();
    }
    
    @Test
    public void testAttributeFiltering3() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Select/attributeFiltering.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("fromSelectedTable", "input_table_b");        
        inputMap.put("fromSelectedColumn", new String[]{"id"});     
        inputMap.put("operation", new String[]{"in"});  
        inputMap.put("fromSelectedValue", "(1,3)");    
        inputMap.put("dropTable", true);    
        inputMap.put("outputTableName", "input_table_b_filetered");        
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);       
        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put("literalOutput", "Not executed");
        //Execute
        WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap, outputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT count(*) FROM input_table_b_filetered;");
        assertTrue(rs.next());      
        Assert.assertEquals(2, rs.getInt(1));
        rs.close();
    }

}
