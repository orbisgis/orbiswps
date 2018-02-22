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

import com.vividsolutions.jts.geom.Geometry;
import groovy.lang.GroovyClassLoader;
import groovy.sql.Sql;
import junit.framework.Assert;
import org.h2.tools.RunScript;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.*;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;

/**
 * Class to test the the scripts themselves.
 *
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS
 */
public class ScriptTests {

    /** Sql {@link Connection} to an embedded H2GIS database. */
    private static Connection connection;
    /** {@link Sql} object get from the connection. */
    private static Sql sql;
    /** {@link groovy.lang.GroovyClassLoader} object used to parse and execute scripts. */
    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader());
    /** {@link java.sql.Statement} object used to execute Sql queries. */
    private Statement st;


    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(ScriptTests.class.getSimpleName());
        sql = new Sql(connection);        
        // Set up test model
        InputStreamReader reader = new InputStreamReader(ScriptTests.class.getResourceAsStream("wps_scripts_test.sql"));
        RunScript.execute(connection, reader);
        reader.close();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

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
        String scriptPath = ScriptTests.class.getResource("basicWPSScript.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputValue", "good ");
        Map<String, Object> propertyMap = new HashMap<>();
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("The script has been executed.",outputMap.get("outputValue"));
    }

     @Test
    public void testBasicScriptNullProperties() throws Exception {
        String scriptPath = ScriptTests.class.getResource("basicWPSScript.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputValue", "good ");
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, null, inputMap);
        Assert.assertEquals("The script has been executed.",outputMap.get("outputValue"));
    }

    @Test
    public void testScriptWithoutInput() throws Exception {
        String scriptPath = ScriptTests.class.getResource("wpsScriptWithoutInput.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        Map<String, Object> propertyMap = new HashMap<>();
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
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
         //Drop the output table(s)
        st.execute("DROP TABLE if exists buffer_table");
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
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
         //Drop the output table(s)
        st.execute("DROP TABLE if exists buffer_table");
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
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
        //Drop the output table(s)
        st.execute("DROP TABLE if exists center_table");
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
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
        //Drop the output table(s)
        st.execute("DROP TABLE if exists center_table");
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
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
        propertyMap.put("ish2", true);
        //Add table to reproject
        st.execute("drop table if exists to_be_reprojected"
                + "; create table to_be_reprojected(the_geom point, id int);"
                + "insert into to_be_reprojected values(st_geomfromtext('POINT(2.114551393 50.345609791)',4326), 99);");
        //Drop the output table(s)
        st.execute("DROP TABLE if exists reprojected_table");
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
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
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
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
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
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
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT count(*) FROM input_table_b_filetered;");
        assertTrue(rs.next());
        Assert.assertEquals(2, rs.getInt(1));
        rs.close();
    }

    @Test
    public void testSpatialFiltering1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Select/spatialFiltering.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("fromSelectedTable", "input_table_b");
        inputMap.put("geometricFieldFromSelected", new String[]{"the_geom"});
        inputMap.put("operation", new String[]{"st_disjoint"});
        inputMap.put("toSelectedTable", "input_table_a");
        inputMap.put("geometricFieldToSelected", new String[]{"the_geom"});
        inputMap.put("dropTable", true);
        inputMap.put("outputTableName", "input_table_filetered");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT count(*) FROM input_table_filetered;");
        assertTrue(rs.next());
        Assert.assertEquals(2, rs.getInt(1));
        rs.close();
    }

    @Test
    public void testExpressionFiltering1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Select/expressionFiltering.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("fromSelectedTable", "input_table_b");
        inputMap.put("fromSelectedValue", "limit 2");
        inputMap.put("dropTable", true);
        inputMap.put("outputTableName", "input_table_b_filetered");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT count(*) FROM input_table_b_filetered;");
        assertTrue(rs.next());
        Assert.assertEquals(2, rs.getInt(1));
        rs.close();
    }

    @Test
    public void testExpressionFiltering2() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Select/expressionFiltering.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("fromSelectedTable", "input_table_b");
        inputMap.put("fromSelectedValue", "order by id desc");
        inputMap.put("dropTable", true);
        inputMap.put("outputTableName", "input_table_b_filetered");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM input_table_b_filetered;");
        assertTrue(rs.next());
        Assert.assertEquals(3, rs.getInt(2));
        assertTrue(rs.next());
        Assert.assertEquals(2, rs.getInt(2));
        assertTrue(rs.next());
        Assert.assertEquals(1, rs.getInt(2));
        rs.close();
    }


    @Test
    public void testpolygonCompactnessIndices1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Indices/compactnessIndices.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputTable", "geomForms");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("idField", new String[]{"id"});
        inputMap.put("operations", new String[]{"gravelius","miller", "morton"});
        inputMap.put("dropTable", true);
        inputMap.put("outputTableName", "geomForms_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        //Add model
        st.execute("drop table if exists geomForms; create table geomForms (the_geom polygon, id int, form varchar); "
                + "INSERT INTO geomForms VALUES(ST_GeomFromText('POLYGON ((100 300, 200 300, 200 200, 100 200, 100 300))'), 1,'square')"
                + ",(ST_GeomFromText('POLYGON ((100 300, 300 300, 300 200, 100 200, 100 300))'), 2,'rectangle'),"
                + "(ST_GeomFromText('POLYGON ((100 300, 200 400, 300 400, 400 300, 400 200, 300 100, 200 100, 100 200, 100 300))'), 3,'hexagon'),"
                + "(ST_GeomFromText('POLYGON ((20 10, 19.807852804032304 8.049096779838717, 19.238795325112868 6.173165676349102, 18.314696123025453 4.444297669803978, 17.071067811865476 2.9289321881345254, 15.555702330196024 1.6853038769745474, 13.826834323650898 0.7612046748871322, 11.950903220161283 0.1921471959676957, 10 0, 8.049096779838719 0.1921471959676957, 6.173165676349103 0.7612046748871322, 4.44429766980398 1.6853038769745474, 2.9289321881345254 2.9289321881345245, 1.6853038769745474 4.444297669803978, 0.7612046748871322 6.173165676349106, 0.1921471959676939 8.049096779838722, 0 10.000000000000007, 0.1921471959676975 11.950903220161292, 0.7612046748871375 13.826834323650909, 1.6853038769745545 15.555702330196034, 2.928932188134537 17.071067811865486, 4.444297669803992 18.314696123025463, 6.173165676349122 19.238795325112875, 8.04909677983874 19.807852804032308, 10.000000000000025 20, 11.950903220161308 19.8078528040323, 13.826834323650925 19.238795325112857, 15.555702330196048 18.314696123025435, 17.071067811865497 17.07106781186545, 18.31469612302547 15.555702330195993, 19.238795325112882 13.826834323650862, 19.80785280403231 11.950903220161244, 20 10))'), 4,'circle')");
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM geomForms_res;");
        assertTrue(rs.next());
        //Gravelius square
        Assert.assertEquals(1.12, rs.getDouble(1), 0.01);
        //Miller square
        Assert.assertEquals(0.78, rs.getDouble(2), 0.01);
        //Morton square
        Assert.assertEquals(0.63, rs.getDouble(3), 0.01);
        //Assert.assertEquals(3, rs.getInt(2));
        assertTrue(rs.next());
        //Gravelius rectangle
        Assert.assertEquals(1.19, rs.getDouble(1), 0.01);
        //Miller rectangle
        Assert.assertEquals(0.69, rs.getDouble(2), 0.01);
        //Morton rectangle
        Assert.assertEquals(0.51, rs.getDouble(3), 0.01);
        assertTrue(rs.next());
        //Gravelius hexagon
        Assert.assertEquals(1.03, rs.getDouble(1), 0.01);
        //Miller hexagon
        Assert.assertEquals(0.9, rs.getDouble(2), 0.1);
        //Morton hexagon
        Assert.assertEquals(0.8, rs.getDouble(3), 0.1);
        assertTrue(rs.next());
        //Gravelius circle
        Assert.assertEquals(1, rs.getDouble(1), 0.1);
        //Miller circle
        Assert.assertEquals(1, rs.getDouble(2), 0.1);
        //Morton circle
        Assert.assertEquals(1, rs.getDouble(3), 0.1);
        rs.close();
    }

    @Test
    public void testmainDirectionSMBR1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Indices/maindirectionSMBR.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputTable", "geomForms");
        inputMap.put("geometryColumn", new String[]{"the_geom"});
        inputMap.put("idField", new String[]{"id"});
        inputMap.put("dropTable", true);
        inputMap.put("outputTableName", "geomForms_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        //Add model
        st.execute("create table geomForms (the_geom polygon, id int); "
                + "INSERT INTO geomForms VALUES(ST_GeomFromText('POLYGON ((140 370, 180 370, 180 120, 140 120, 140 370))'), 1)"
                + ",(ST_GeomFromText('POLYGON ((100 300, 300 300, 300 270, 100 270, 100 300))'), 2),"
                + "(ST_GeomFromText('POLYGON ((118.68272016354703 224.8959235991435, 260.10407640085657 366.317279836453, 281.31727983645294 345.1040764008565, 139.89592359914346 203.68272016354706, 118.68272016354703 224.8959235991435))'), 3),"
                + "(ST_GeomFromText('POLYGON ((260.10407640085657 203.68272016354703, 118.68272016354706 345.10407640085657, 139.89592359914354 366.31727983645294, 281.317279836453 224.89592359914346, 260.10407640085657 203.68272016354703))'), 4,)");
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done", outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM geomForms_res;");
        assertTrue(rs.next());
        Assert.assertEquals(0, rs.getDouble(2), 0.01);
        assertTrue(rs.next());
        Assert.assertEquals(90, rs.getDouble(2), 0.01);
        assertTrue(rs.next());
        Assert.assertEquals(45, rs.getDouble(2), 0.1);
        assertTrue(rs.next());
        Assert.assertEquals(135, rs.getDouble(2), 0.1);
        rs.close();
    }

    @Test
    public void testequalAreaCircle1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Indices/equalAreaCircle.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputTable", "geomForms");
        inputMap.put("geometryColumn", new String[]{"the_geom"});
        inputMap.put("idField", new String[]{"id"});
        inputMap.put("dropTable", true);
        inputMap.put("outputTableName", "geomForms_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        //Add model
        st.execute("create table geomForms (the_geom polygon, id int); "
                + "INSERT INTO geomForms VALUES(ST_GeomFromText('POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))'), 1)"
                + ",(ST_GeomFromText('POLYGON ((100 300, 200 400, 300 400, 400 300, 400 200, 300 100, 200 100, 100 200, 100 300))'), 2)");
        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done", outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM geomForms_res;");
        assertTrue(rs.next());
        //Square
        Assert.assertEquals(56.4, rs.getDouble(3), 0.1);
        assertTrue(rs.next());
        //Hexagon
        Assert.assertEquals(149.3, rs.getDouble(3), 0.1);
        rs.close();
    }


    @Test
    public void testConcavityIndice1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Indices/concavityIndex.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputTable", "geomForms");
        inputMap.put("geometryColumn", new String[]{"the_geom"});
        inputMap.put("idField", new String[]{"id"});
        inputMap.put("dropTable", true);
        inputMap.put("outputTableName", "geomForms_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        //Add model
        st.execute("drop table if exists geomForms; create table geomForms (the_geom polygon, id int, form varchar); "
                + "INSERT INTO geomForms VALUES(ST_GeomFromText('POLYGON ((100 300, 200 300, 200 200, 100 200, 100 300))'), 1,'square')"
                + ",(ST_GeomFromText('POLYGON ((100 300, 300 300, 300 200, 100 200, 100 300))'), 2,'rectangle'),"
                + "(ST_GeomFromText('POLYGON ((100 300, 200 400, 300 400, 400 300, 400 200, 300 100, 200 100, 100 200, 100 300))'), 3,'hexagon'),"
                + "(ST_GeomFromText('POLYGON ((20 10, 19.807852804032304 8.049096779838717, 19.238795325112868 6.173165676349102, 18.314696123025453 4.444297669803978, 17.071067811865476 2.9289321881345254, 15.555702330196024 1.6853038769745474, 13.826834323650898 0.7612046748871322, 11.950903220161283 0.1921471959676957, 10 0, 8.049096779838719 0.1921471959676957, 6.173165676349103 0.7612046748871322, 4.44429766980398 1.6853038769745474, 2.9289321881345254 2.9289321881345245, 1.6853038769745474 4.444297669803978, 0.7612046748871322 6.173165676349106, 0.1921471959676939 8.049096779838722, 0 10.000000000000007, 0.1921471959676975 11.950903220161292, 0.7612046748871375 13.826834323650909, 1.6853038769745545 15.555702330196034, 2.928932188134537 17.071067811865486, 4.444297669803992 18.314696123025463, 6.173165676349122 19.238795325112875, 8.04909677983874 19.807852804032308, 10.000000000000025 20, 11.950903220161308 19.8078528040323, 13.826834323650925 19.238795325112857, 15.555702330196048 18.314696123025435, 17.071067811865497 17.07106781186545, 18.31469612302547 15.555702330195993, 19.238795325112882 13.826834323650862, 19.80785280403231 11.950903220161244, 20 10))'), 4,'circle'),"
                + "(ST_GeomFromText('POLYGON ((100 320, 100 120, 400 120, 400 320, 300 320, 300 200, 200 200, 200 320, 100 320))'), 4,'polygon')");

        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT * FROM geomForms_res;");
        assertTrue(rs.next());
        //square
        Assert.assertEquals(1, rs.getDouble(2), 0.01);        
        assertTrue(rs.next());     
        //rectangle
        Assert.assertEquals(1, rs.getDouble(2), 0.01);       
        assertTrue(rs.next());   
        //hexagon
        Assert.assertEquals(1, rs.getDouble(2), 0.01);
        assertTrue(rs.next());   
        //circle
        Assert.assertEquals(1, rs.getDouble(2), 0.1);        
        assertTrue(rs.next());   
        //polygon
        Assert.assertTrue(rs.getDouble(2)<1);
        rs.close();
    }
    
    @Test
    public void testPolygonize1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Convert/polygonize.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "geomForms");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("dropOutputTable", true);
        inputMap.put("outputTableName", "polygonize_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        propertyMap.put("isH2", true);

        //Add data
        st.execute("drop table if exists geomForms; create table geomForms (the_geom polygon); "
                + "INSERT INTO geomForms VALUES(ST_GeomFromText('POLYGON ((100 330, 220 330, 220 230, 100 230, 100 330))'))"
                + ",(ST_GeomFromText('POLYGON ((160 340, 290 340, 290 260, 160 260, 160 340))')),"
                + "(ST_GeomFromText('POLYGON ((60 270, 140 270, 140 180, 60 180, 60 270))'))");

        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done", outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT count(*) FROM polygonize_res;");
        assertTrue(rs.next());
        Assert.assertEquals(3, rs.getInt(1));
        rs.close();
    }
    
    @Test
    public void testPolygonize2() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Convert/polygonize.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "geomForms");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("node", true);
        inputMap.put("dropOutputTable", true);
        inputMap.put("outputTableName", "polygonize_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        propertyMap.put("isH2", true);

        //Add data
        st.execute("drop table if exists geomForms; create table geomForms (the_geom polygon); "
                + "INSERT INTO geomForms VALUES(ST_GeomFromText('POLYGON ((100 330, 220 330, 220 230, 100 230, 100 330))'))"
                + ",(ST_GeomFromText('POLYGON ((160 340, 290 340, 290 260, 160 260, 160 340))')),"
                + "(ST_GeomFromText('POLYGON ((60 270, 140 270, 140 180, 60 180, 60 270))'))");

        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done", outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT count(*) FROM polygonize_res;");
        assertTrue(rs.next());
        Assert.assertEquals(5, rs.getInt(1));
        rs.close();
    }
    
    @Test
    public void testPolygonize3() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Convert/polygonize.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "geomForms");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("node", true);
        inputMap.put("dropOutputTable", true);
        inputMap.put("outputTableName", "polygonize_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        propertyMap.put("isH2", true);
        //Add data
        st.execute("drop table if exists geomForms; create table geomForms (the_geom linestring); "
                + "INSERT INTO geomForms VALUES(ST_GeomFromText('LINESTRING (90 200, 240 330)'))"
                + ",(ST_GeomFromText('LINESTRING (150 340, 290 240)')),"
                +" (ST_GeomFromText('LINESTRING (100 290, 190 200, 270 310)')),"
                + "(ST_GeomFromText('LINESTRING (220 360, 90 260, 140 180)'))");

        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done", outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT count(*) FROM polygonize_res;");
        assertTrue(rs.next());
        Assert.assertEquals(3, rs.getInt(1));
        rs.close();
    }
    
    
    @Test
    public void testUnion1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Aggregate/union.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "geomUnion");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("dropOutputTable", true);
        inputMap.put("outputTableName", "geomUnion_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);        
        propertyMap.put("isH2", true);
        //Add data
        st.execute("drop table if exists geomForms; create table geomUnion (the_geom polygon, id int, form varchar); "
                + "INSERT INTO geomUnion VALUES(ST_GeomFromText('POLYGON ((100 400, 200 400, 200 300, 100 300, 100 400))'), 1,'square')"
                + ",(ST_GeomFromText('POLYGON ((200 350, 280 350, 280 250, 200 250, 200 350))'), 2,'rectangle'),"
                + "(ST_GeomFromText('POLYGON ((60 220, 150 220, 150 170, 60 170, 60 220))'), 3,'rectangle'),"
                + "(ST_GeomFromText('POLYGON ((100 170, 220 170, 220 120, 100 120, 100 170))'), 4,'rectangle'),"
                + "(ST_GeomFromText('POLYGON ((250 250, 338 250, 338 195, 250 195, 250 250))'), 5,'square')");

        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT st_accum(the_geom) FROM geomUnion_res;");
        assertTrue(rs.next());  
        assertGeometryEquals("MULTIPOLYGON (((100 400, 200 400, 200 350, 280 350, 280 250, 338 250, 338 195, 250 195, 250 250, 200 250, 200 300, 100 300, 100 400)), ((150 170, 220 170, 220 120, 100 120, 100 170, 60 170, 60 220, 150 220, 150 170)))", rs.getObject(1));
        rs.close();
    }
    
    @Test
    public void testUnion2() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Aggregate/union.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "geomUnion");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("groupby", new String[]{"form"});
        inputMap.put("dropOutputTable", true);
        inputMap.put("outputTableName", "geomUnion_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);        
        propertyMap.put("isH2", true);
        //Add data
        st.execute("drop table if exists geomForms; create table geomUnion (the_geom polygon, id int, form varchar); "
                + "INSERT INTO geomUnion VALUES(ST_GeomFromText('POLYGON ((100 400, 200 400, 200 300, 100 300, 100 400))'), 1,'square')"
                + ",(ST_GeomFromText('POLYGON ((200 350, 280 350, 280 250, 200 250, 200 350))'), 2,'rectangle'),"
                + "(ST_GeomFromText('POLYGON ((60 220, 150 220, 150 170, 60 170, 60 220))'), 3,'rectangle'),"
                + "(ST_GeomFromText('POLYGON ((100 170, 220 170, 220 120, 100 120, 100 170))'), 4,'rectangle'),"
                + "(ST_GeomFromText('POLYGON ((250 250, 338 250, 338 195, 250 195, 250 250))'), 5,'square')");

        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT st_accum(the_geom) FROM geomUnion_res;");
        assertTrue(rs.next());  
        assertGeometryEquals("MULTIPOLYGON (((250 250, 338 250, 338 195, 250 195, 250 250)), ((100 400, 200 400, 200 300, 100 300, 100 400)), ((200 350, 280 350, 280 250, 200 250, 200 350)), ((150 170, 220 170, 220 120, 100 120, 100 170, 60 170, 60 220, 150 220, 150 170)))", rs.getObject(1));
        rs.close();
    }
    
    @Test
    public void testUnion3() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Aggregate/union.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "geomUnion");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("buffer", 1.0);
        inputMap.put("dropOutputTable", true);
        inputMap.put("outputTableName", "geomUnion_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);        
        propertyMap.put("isH2", true);
        //Add data
        st.execute("drop table if exists geomForms; create table geomUnion (the_geom polygon, id int, form varchar); "
                + "INSERT INTO geomUnion VALUES(ST_GeomFromText('POLYGON ((10 20, 20 20, 20 7, 10 7, 10 20))'), 1,'square')"
                + ",(ST_GeomFromText('POLYGON ((21 20, 30 20, 30 7, 21 7, 21 20))'), 2,'rectangle'),"
                + "(ST_GeomFromText('POLYGON ((10 4, 30 4, 30 2, 10 2, 10 4))'), 3,'rectangle')");

        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT st_accum(the_geom) FROM geomUnion_res;");
        assertTrue(rs.next()); 
        assertGeometryEquals("MULTIPOLYGON (((9 4, 9.01921471959677 4.195090322016129, 9.076120467488714 4.38268343236509, 9.168530387697455 4.555570233019602, 9.292893218813452 4.707106781186548, 9.444429766980399 4.831469612302545, 9.61731656763491 4.923879532511287, 9.804909677983872 4.98078528040323, 10 5, 30 5, 30.195090322016128 4.98078528040323, 30.38268343236509 4.923879532511287, 30.5555702330196 4.831469612302545, 30.707106781186546 4.707106781186548, 30.831469612302545 4.555570233019602, 30.923879532511286 4.38268343236509, 30.980785280403232 4.195090322016128, 31 4, 31 2, 30.980785280403232 1.8049096779838718, 30.923879532511286 1.6173165676349102, 30.831469612302545 1.4444297669803978, 30.707106781186546 1.2928932188134525, 30.5555702330196 1.1685303876974547, 30.38268343236509 1.0761204674887133, 30.195090322016128 1.0192147195967696, 30 1, 10 1, 9.804909677983872 1.0192147195967696, 9.61731656763491 1.0761204674887135, 9.444429766980399 1.1685303876974547, 9.292893218813452 1.2928932188134525, 9.168530387697455 1.444429766980398, 9.076120467488714 1.6173165676349104, 9.01921471959677 1.8049096779838716, 9 2, 9 4)), ((9 20, 9.01921471959677 20.195090322016128, 9.076120467488714 20.38268343236509, 9.168530387697455 20.5555702330196, 9.292893218813452 20.707106781186546, 9.444429766980399 20.831469612302545, 9.61731656763491 20.923879532511286, 9.804909677983872 20.980785280403232, 10 21, 20 21, 20.195090322016128 20.980785280403232, 20.38268343236509 20.923879532511286, 20.5 20.861172520678902, 20.61731656763491 20.923879532511286, 20.804909677983872 20.980785280403232, 21 21, 30 21, 30.195090322016128 20.980785280403232, 30.38268343236509 20.923879532511286, 30.5555702330196 20.831469612302545, 30.707106781186546 20.707106781186546, 30.831469612302545 20.5555702330196, 30.923879532511286 20.38268343236509, 30.980785280403232 20.195090322016128, 31 20, 31 7, 30.980785280403232 6.804909677983872, 30.923879532511286 6.61731656763491, 30.831469612302545 6.444429766980398, 30.707106781186546 6.292893218813452, 30.5555702330196 6.168530387697455, 30.38268343236509 6.076120467488713, 30.195090322016128 6.01921471959677, 30 6, 21 6, 20.804909677983872 6.01921471959677, 20.61731656763491 6.076120467488714, 20.5 6.1388274793210975, 20.38268343236509 6.076120467488713, 20.195090322016128 6.01921471959677, 20 6, 10 6, 9.804909677983872 6.01921471959677, 9.61731656763491 6.076120467488714, 9.444429766980399 6.168530387697455, 9.292893218813452 6.292893218813452, 9.168530387697455 6.444429766980398, 9.076120467488714 6.61731656763491, 9.01921471959677 6.804909677983872, 9 7, 9 20)))", rs.getObject(1));
        rs.close();
    }
    
    @Test
    public void testSharedPart1() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Topology/sharedPart.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "geomshared");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("dropOutputTable", true);
        inputMap.put("outputTableName", "geomshared_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);        
        propertyMap.put("isH2", true);
        //Add data
        st.execute("drop table if exists geomForms; create table geomshared (the_geom polygon, id int); "
                + "INSERT INTO geomshared VALUES(ST_GeomFromText('POLYGON ((100 400, 200 400, 200 300, 100 300, 100 400))'), 1)"
                + ",(ST_GeomFromText('POLYGON ((100 300, 200 300, 200 220, 100 220, 100 300))'), 2),"
                + "(ST_GeomFromText('POLYGON ((200 170, 290 170, 290 110, 200 110, 200 170))'), 3)");

        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT st_accum(the_geom) FROM geomshared_res;");
        assertTrue(rs.next()); 
        assertGeometryEquals("MULTILINESTRING ((200 300, 100 300))", rs.getObject(1));
        rs.close();
    }
    
    @Test
    public void testSharedPart2() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Topology/sharedPart.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "geomshared");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("dropOutputTable", true);
        inputMap.put("outputTableName", "geomshared_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);        
        propertyMap.put("isH2", true);
        //Add data
        st.execute("drop table if exists geomForms; create table geomshared (the_geom POINT, id int); "
                + "INSERT INTO geomshared VALUES(ST_GeomFromText('POINT (100 100)'), 1)"
                + ",(ST_GeomFromText('POINT (100 100)'), 2),"
                + "(ST_GeomFromText('POINT (12 100)'), 3)");

        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT st_accum(the_geom) FROM geomshared_res;");
        assertTrue(rs.next()); 
        assertGeometryEquals("MULTIPOINT ((100 100))", rs.getObject(1));
        rs.close();
    }
    
     @Test
    public void testSharedPart3() throws Exception {
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Geometry2D/Topology/sharedPart.groovy").getPath();
        //Prepare input and output values
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", "geomshared");
        inputMap.put("geometricField", new String[]{"the_geom"});
        inputMap.put("dropOutputTable", true);
        inputMap.put("outputTableName", "geomshared_res");
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);        
        propertyMap.put("isH2", true);
        //Add data
        st.execute("drop table if exists geomForms; create table geomshared (the_geom LINESTRING, id int); "
                + "INSERT INTO geomshared VALUES(ST_GeomFromText('LINESTRING (100 300, 200 300))'), 1)"
                + ",(ST_GeomFromText('LINESTRING (150 300, 300 300)'), 2),"
                + "(ST_GeomFromText('LINESTRING (100 200, 300 200)'), 3),"
                + "(ST_GeomFromText('LINESTRING (200 140, 200 240)'), 4)");

        //Execute
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        Assert.assertEquals("Process done",outputMap.get("literalOutput"));
        ResultSet rs = st.executeQuery(
                "SELECT st_accum(the_geom) FROM geomshared_res;");
        assertTrue(rs.next()); 
        assertGeometryEquals("GEOMETRYCOLLECTION (POINT (200 200), LINESTRING (150 300, 200 300))", rs.getObject(1));
        rs.close();
    }
}
