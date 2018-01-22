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
import groovy.sql.Sql;
import junit.framework.Assert;
import org.h2.tools.RunScript;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.*;
import org.orbisgis.orbiswps.service.process.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to test the the export script.
 *
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS
 */
public class ExportScriptTests {

    /** Sql {@link Connection} to an embedded H2GIS database. */
    private static Connection connection;
    /** {@link Sql} object get from the connection. */
    private static Sql sql;
    /** {@link GroovyClassLoader} object used to parse and execute scripts. */
    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader());
    /** {@link Statement} object used to execute Sql queries. */
    private Statement st;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(ExportScriptTests.class.getSimpleName());
        sql = new Sql(connection);        
        // Set up test model
        InputStreamReader reader = new InputStreamReader(ExportScriptTests.class.getResourceAsStream("wps_scripts_test.sql"));
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
    public void testExportCSVFileScript() throws Exception {
        String fileName = "target/test-classes/csvfile.csv";
        String tableName = "CSVEXPORT";
        //Prepare database
        st.execute("DROP TABLE IF EXISTS "+tableName+";");
        st.execute("CREATE TABLE "+tableName+" (ID INT, THE_GEOM POINT);");
        st.execute("INSERT INTO "+tableName+" VALUES (0, ST_GeomFromText('POINT (0 0)'))");
        st.execute("INSERT INTO "+tableName+" VALUES (5, ST_GeomFromText('POINT (15 15)'))");
        st.execute("INSERT INTO "+tableName+" VALUES (80, ST_GeomFromText('POINT (5680 5680)'))");
        //Prepare script execution
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Export/exportCSVFile.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", tableName);
        inputMap.put("dropInputTable", true);
        inputMap.put("fileDataInput", new String[]{fileName});
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        propertyMap.put("progressMonitor", new ProgressMonitor("root"));
        //Execute script
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        //Test the outputs
        Assert.assertEquals("The CSV file has been created.",outputMap.get("literalDataOutput"));
        File file = new File(fileName);
        Assert.assertTrue(file.exists());
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        Assert.assertEquals("\"ID\",\"THE_GEOM\"", reader.readLine());
        Assert.assertEquals("\"0\",\"POINT (0 0)\"", reader.readLine());
        Assert.assertEquals("\"5\",\"POINT (15 15)\"", reader.readLine());
        Assert.assertEquals("\"80\",\"POINT (5680 5680)\"", reader.readLine());
    }

    @Test
    public void testExportDBFFileScript() throws Exception {
        String fileName = "target/test-classes/dbffile.dbf";
        String tableName = "DBFEXPORT";
        //Prepare database
        st.execute("DROP TABLE IF EXISTS "+tableName+";");
        st.execute("CREATE TABLE "+tableName+" (ID INT, THE_GEOM POINT);");
        st.execute("INSERT INTO "+tableName+" VALUES (0, ST_GeomFromText('POINT (0 0)'))");
        st.execute("INSERT INTO "+tableName+" VALUES (5, ST_GeomFromText('POINT (15 15)'))");
        st.execute("INSERT INTO "+tableName+" VALUES (80, ST_GeomFromText('POINT (5680 5680)'))");
        //Prepare script execution
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Export/exportDBFFile.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", tableName);
        inputMap.put("dropInputTable", true);
        inputMap.put("fileDataInput", new String[]{fileName});
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        propertyMap.put("progressMonitor", new ProgressMonitor("root"));
        //Execute script
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        //Test the outputs
        Assert.assertEquals("The DBF file has been created.",outputMap.get("literalDataOutput"));
        File file = new File(fileName);
        Assert.assertTrue(file.exists());
    }

    @Test
    public void testExportGeoJsonFileScript() throws Exception {
        String fileName = "target/test-classes/geojsonfile.geojson";
        String tableName = "GEOJSONEXPORT";
        //Prepare database
        st.execute("DROP TABLE IF EXISTS "+tableName+";");
        st.execute("CREATE TABLE "+tableName+" (ID INT, THE_GEOM POINT);");
        st.execute("INSERT INTO "+tableName+" VALUES (0, ST_GeomFromText('POINT (0 0)'))");
        st.execute("INSERT INTO "+tableName+" VALUES (5, ST_GeomFromText('POINT (15 15)'))");
        st.execute("INSERT INTO "+tableName+" VALUES (80, ST_GeomFromText('POINT (5680 5680)'))");
        //Prepare script execution
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Export/exportGeoJsonFile.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", tableName);
        inputMap.put("dropInputTable", true);
        inputMap.put("fileDataInput", new String[]{fileName});
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        propertyMap.put("progressMonitor", new ProgressMonitor("root"));
        //Execute script
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        //Test the outputs
        Assert.assertEquals("The GeoJSON file has been created.",outputMap.get("literalDataOutput"));
        File file = new File(fileName);
        Assert.assertTrue(file.exists());
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        Assert.assertEquals("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\"" +
                ":{\"type\":\"Point\",\"coordinates\":[0.0,0.0]},\"properties\":{\"id\":0}},{\"type\":\"Feature\",\"" +
                "geometry\":{\"type\":\"Point\",\"coordinates\":[15.0,15.0]},\"properties\":{\"id\":5}},{\"type\":\"" +
                "Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[5680.0,5680.0]},\"properties\":{\"id\"" +
                ":80}}]}", reader.readLine());
    }

    @Test
    public void testExportKMLFileScript() throws Exception {
        String fileName = "target/test-classes/kmlfile.kml";
        String tableName = "KMLEXPORT";
        //Prepare database
        st.execute("DROP TABLE IF EXISTS "+tableName+";");
        st.execute("CREATE TABLE "+tableName+" (ID INT, THE_GEOM POINT);");
        st.execute("INSERT INTO "+tableName+" VALUES (0, ST_SetSRID(ST_GeomFromText('POINT (0 0)'), 4326))");
        st.execute("INSERT INTO "+tableName+" VALUES (5, ST_SetSRID(ST_GeomFromText('POINT (15 15)'), 4326))");
        st.execute("INSERT INTO "+tableName+" VALUES (80, ST_SetSRID(ST_GeomFromText('POINT (5680 5680)'), 4326))");
        //Prepare script execution
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Export/exportKMLFile.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", tableName);
        inputMap.put("dropInputTable", true);
        inputMap.put("fileDataInput", new String[]{fileName});
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        propertyMap.put("progressMonitor", new ProgressMonitor("root"));
        //Execute script
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        //Test the outputs
        Assert.assertEquals("The KML file has been created.",outputMap.get("literalDataOutput"));
        File file = new File(fileName);
        Assert.assertTrue(file.exists());
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/" +
                "kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" " +
                "xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:xal=\"urn:oasis:names:tc:ciq:xsdschema:xAL:2.0\">" +
                "<Document><Schema name=\"KMLEXPORT\" id=\"KMLEXPORT\"><SimpleField name=\"ID\" type=\"int\">" +
                "</SimpleField></Schema><Folder><name>KMLEXPORT</name><Placemark><ExtendedData><SchemaData " +
                "schemaUrl=\"#KMLEXPORT\"><SimpleData name=\"ID\">0</SimpleData></SchemaData></ExtendedData><Point>" +
                "<coordinates>0.0,0.0</coordinates></Point></Placemark><Placemark><ExtendedData><SchemaData " +
                "schemaUrl=\"#KMLEXPORT\"><SimpleData name=\"ID\">5</SimpleData></SchemaData></ExtendedData><Point>" +
                "<coordinates>15.0,15.0</coordinates></Point></Placemark><Placemark><ExtendedData><SchemaData " +
                "schemaUrl=\"#KMLEXPORT\"><SimpleData name=\"ID\">80</SimpleData></SchemaData></ExtendedData><Point>" +
                "<coordinates>5680.0,5680.0</coordinates></Point></Placemark></Folder></Document></kml>", reader.readLine());
    }

    @Test
    public void testExportShapeFileScript() throws Exception {
        String fileName = "target/test-classes/shapefile";
        String tableName = "SHPEXPORT";
        //Prepare database
        st.execute("DROP TABLE IF EXISTS "+tableName+";");
        st.execute("CREATE TABLE "+tableName+" (ID INT, THE_GEOM POINT);");
        st.execute("INSERT INTO "+tableName+" VALUES (0, ST_SetSRID(ST_GeomFromText('POINT (0 0)'), 4326))");
        st.execute("INSERT INTO "+tableName+" VALUES (5, ST_SetSRID(ST_GeomFromText('POINT (15 15)'), 4326))");
        st.execute("INSERT INTO "+tableName+" VALUES (80, ST_SetSRID(ST_GeomFromText('POINT (5680 5680)'), 4326))");
        //Prepare script execution
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Export/exportShapeFile.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", tableName);
        inputMap.put("dropInputTable", true);
        inputMap.put("fileDataInput", new String[]{fileName+".shp"});
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        propertyMap.put("progressMonitor", new ProgressMonitor("root"));
        //Execute script
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        //Test the outputs
        Assert.assertEquals("The ShapeFile has been created.",outputMap.get("literalDataOutput"));
        File file = new File(fileName+".shp");
        Assert.assertTrue(file.exists());
        file = new File(fileName+".shx");
        Assert.assertTrue(file.exists());
    }


    @Test
    public void testExportTSVFileScript() throws Exception {
        String fileName = "target/test-classes/tsvfile.tsv";
        String tableName = "TSVEXPORT";
        //Prepare database
        st.execute("DROP TABLE IF EXISTS "+tableName+";");
        st.execute("CREATE TABLE "+tableName+" (ID INT, THE_GEOM POINT);");
        st.execute("INSERT INTO "+tableName+" VALUES (0, ST_GeomFromText('POINT (0 0)'))");
        st.execute("INSERT INTO "+tableName+" VALUES (5, ST_GeomFromText('POINT (15 15)'))");
        st.execute("INSERT INTO "+tableName+" VALUES (80, ST_GeomFromText('POINT (5680 5680)'))");
        //Prepare script execution
        String scriptPath = WPSScriptExecute.class.getResource("scripts/Export/exportTSVFile.groovy").getPath();
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("inputJDBCTable", tableName);
        inputMap.put("dropInputTable", true);
        inputMap.put("fileDataInput", new String[]{fileName});
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("sql", sql);
        propertyMap.put("progressMonitor", new ProgressMonitor("root"));
        //Execute script
        Map<String, Object> outputMap = WPSScriptExecute.run(groovyClassLoader, scriptPath, propertyMap, inputMap);
        //Test the outputs
        Assert.assertEquals("The TSV file has been created.",outputMap.get("literalDataOutput"));
        File file = new File(fileName);
        Assert.assertTrue(file.exists());
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        Assert.assertEquals("\tID\t\t\tTHE_GEOM\t", reader.readLine());
        Assert.assertEquals("\t0\t\t\tPOINT (0 0)\t", reader.readLine());
        Assert.assertEquals("\t5\t\t\tPOINT (15 15)\t", reader.readLine());
        Assert.assertEquals("\t80\t\t\tPOINT (5680 5680)\t", reader.readLine());
    }
}
