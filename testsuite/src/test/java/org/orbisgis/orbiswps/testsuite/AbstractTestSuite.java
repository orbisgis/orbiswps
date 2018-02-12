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
package org.orbisgis.orbiswps.testsuite;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.SFSUtilities;
import org.junit.Before;
import org.junit.Test;
import org.orbisgis.orbiswps.scripts.WpsScriptPlugin;
import org.orbisgis.orbiswps.service.WpsServerImpl;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.Executors;

/**
 * Conformance test suite as describe in the OGC WPS 2.0 interface standard
 */
public class AbstractTestSuite {

    private WpsServerImpl wpsServer;

    @Before
    public void init() throws SQLException {
        DataSource dataSource = SFSUtilities.wrapSpatialDataSource(H2GISDBFactory.createDataSource(
                AbstractTestSuite.class.getSimpleName(), false));
        wpsServer = new WpsServerImpl(dataSource, Executors.newSingleThreadExecutor());
        wpsServer.addWpsScriptBundle(new WpsScriptPlugin());
    }

    /**
     * Verify that the server implements the Basic WPS conformance class.
     *
     * Verify that the server implements the Synchronous WPS and/or the Asynchronous WPS conformance class.
     * Verify that the requests and responses to a supported operation are syntactically correct.
     * Verify that the service supports the Synchronous WPS Conformance class, the Asynchronous WPS Conformance class
     * or both.
     * Verify that all process offerings implement the native process model.
     * Verify the following list of conformance tests:
     *
     * - A.4.1, A.4.3
     * - A.2 and/or A.3
     *
     */
    @Test
    public void A1Test(){
        A41Test();
        A43Test();
        A2Test();
        A3Test();
    }

    /**
     * Verify that the server correctly advertises synchronous execution capabilities. Verify that the server correctly
     * implements all operations that are mandatory for synchronous execution.
     *
     * Verify that the GetCapabilities, DescribeProcess and Execute operations appear in the ows:OperationsMetadata
     * element.
     * Verify that the server offers at least one wps:ProcessSummary element whose “jobControlOptions” attribute
     * contains “sync-execute”.
     * Verify that the service supports at least one binding (e.g. HTTP GET/POST) for each supported operation.
     * Verify the following list of conformance tests:
     *
     *  - A.5.1, A.5.2, A.5.3, A.5.4, A.5.5, A.5.6, A.5.7, A.5.8
     *  - A.5.9 and/or A.5.16
     *  - A.5.10 and/or A.5.17
     *  - A.5.11, A.5.13
     */
    public void A2Test(){
        A51Test();
        A52Test();
        A53Test();
        A54Test();
        A55Test();
        A56Test();
        A57Test();
        A58Test();

        A59Test();
        A516Test();

        A510Test();
        A517Test();

        A511Test();
        A513Test();
    }

    /**
     * Verify that the server correctly advertises asynchronous execution capabilities. Verify that the server
     * correctly implements all operations that are mandatory for asynchronous execution.
     *
     * Verify that the GetCapabilities, DescribeProcess, Execute, GetStatus and GetResult operations appear in the
     * ows:OperationsMetadata element.
     * Verify that the server offers at least one wps:ProcessSummary element whose “jobControlOptions” attribute
     * contains “async-execute”.
     * Verify that the service supports at least one binding (e.g. HTTP GET/POST) for each supported operation.
     *
     * Verify the following list of conformance tests:
     *  - A.5.1, A.5.2, A.5.3, A.5.4, A.5.5, A.5.6, A.5.7, A.5.8
     *  - A.5.9 and/or A.5.16
     *  - A.5.10 and/or A.5.17
     *  - A.5.12, A.5.13
     *  - A.5.14 and/or A.5.18
     *  - A.5.15 and / or A.5.19
     */
    public void A3Test(){
        A51Test();
        A52Test();
        A53Test();
        A54Test();
        A55Test();
        A56Test();
        A57Test();
        A58Test();

        A59Test();
        A516Test();

        A510Test();
        A517Test();

        A512Test();
        A513Test();

        A514Test();
        A518Test();

        A515Test();
        A519Test();
    }

    /**
     * Verify that a given process description is in compliance with the Process XML encoding.
     * Verify that the tested document fulfils all requirements listed in req/native-process/xml-encoding/process.
     */
    public void A41Test(){

    }

    /**
     * Verify that a given generic process description is in compliance with the generic process XML encoding.
     * Verify that the tested document fulfils all requirements listed in
     * req/native-process/xml-encoding/generic-process.
     */
    @Test
    public void A42Test(){

    }

    /**
     * Verify that any XML data type description and values that are used in conjunction with the native process model
     * are encoded in compliance with the process model XML encoding.
     *
     * For ComplexData descriptions: Test passes if the tested XML fragment validates against wps:ComplexData.
     * For LiteralData descriptions: Test passes if the tested XML fragment validates against wps:LiteralData.
     * For BoundingBoxData descriptions: Test passes if the tested XML fragment validates against wps:BoundingBoxData.
     * For ComplexData values: No general test available; the correctness of complex data values must be tested against
     * a particular data type specification given by mime type, encoding and schema.
     * For LiteralData values: Test passes if the tested XML fragment validates against wps:LiteralValue.
     * For BoundingBoxData values: Test passes if the tested XML fragment validates against ows:BoundingBox.
     */
    public void A43Test(){

    }

    /**
     * Verify that the correctly handles the service name parameter.
     *
     * For each request type, send valid requests to server under test. Modulate service parameter:
     *      Parameter value equal to what is required. Verify that request succeeds.
     *      Parameter value not equal to what is required. Verify that request fails.
     * Overall test passes if all individual tests pass.
     */
    public void A51Test(){

    }

    /**
     * Verify that the correctly handles the service version parameter.
     *
     * For each request type, send valid requests to server under test. Modulate service parameter:
     *      Parameter value equal to what is required. Verify that request succeeds.
     *      Parameter value not equal to what is required. Verify that request fails.
     * Overall test passes if all individual tests pass.
     */
    public void A52Test(){

    }

    /**
     * Verify that the server correctly handles input data transmission by value.
     *
     * Send Execute requests to the server under test with valid inputs passed by value. Test passed if the execution
     * finishes successfully.
     */
    public void A53Test(){

    }

    /**
     * Verify that the server correctly handles input data transmission by reference.
     *
     * Send Execute requests to the server under test with valid inputs passed by reference. Test passed if the
     * execution finishes successfully.
     */
    public void A54Test(){

    }

    /**
     * Verify that the server correctly handles output data transmission by value.
     *
     * Check the available process offerings for outputs that can be retrieved by value. If there is an output that
     * can be retrieved by value, send an Execute request to the server requesting the output by value. Test passes if
     * a valid Execute response is returned containing the requested output.
     *
     * Skip this test if no output can be retrieved by value.
     */
    public void A55Test(){

    }

    /**
     * Verify that the server correctly handles output data transmission by reference.
     *
     * Check the available process offerings for outputs that can be retrieved by reference. If there is an output that
     * can be retrieved by reference, send an Execute request to the server requesting the output by reference. Test
     * passes if a valid Execute response is returned containing a reference to the requested output.
     *
     * Skip this test if no output can be retrieved by reference.
     */
    public void A56Test(){

    }

    /**
     * Verify that each process the server offers has a unique identifier.
     * Get all available processes from the server under test. Test passes if all processes have a unique identifier.
     */
    public void A57Test(){

    }

    /**
     * Verify that the server creates a unique jobID for each job.
     *
     * Send more than one asynchronous Execute requests to the server under test. Test passes if the retrieved JobIDs
     * differ from each other.
     */
    public void A58Test(){

    }

    /**
     * Verify that the server can handle GetCapabilities requests via POST/XML.
     *
     * Send a valid GetCapabilities request to the server under test. Test passes if a valid document of the type
     * wps:Capabilities is returned.
     */
    public void A59Test(){

    }

    /**
     * Verify that the server can handle DescribeProcess requests via POST/XML.
     *
     * Send a valid DescribeProcess request to the server under test. Test passes if a valid document of the type
     * wps:ProcessOfferings is returned.
     */
    public void A510Test(){

    }

    /**
     * Verify that the server can handle synchronous Execute requests via POST/XML.
     *
     * Send a valid XML Execute request to the server under test, setting the “mode” attribute to “sync”. Modulate the
     * “response” parameter:
     *      Parameter value equal “document”. Verify that a valid Execute wps:Result is returned.
     *      Parameter equal to “raw”. Verify that is returned.
     *
     * Overall test passes if all individual tests pass.
     */
    public void A511Test(){

    }

    /**
     * Verify that the server can handle asynchronous Execute requests via POST/XML.
     *
     * Send a valid XML Execute request to the server under test, setting the “mode” attribute to “async”. Test passes
     * if a valid Execute wps:StatusInfo document is returned.
     */
    public void A512Test(){

    }

    /**
     * Verify that the server can handle the execution mode “auto” requested via POST/XML.
     *
     * Send a valid XML Execute request to the server under test, setting the “mode” attribute to “auto”. Modulate the
     * “response” parameter.
     *      Case 1) If the process offering supports document output set “response” parameter value equal “document”.
     *      Check the execute response according to the following cases:
     *          Case 1a) If the process offering supports “sync-execute” and not “async-execute”: Verify that a valid
     *          Execute wps:Result document is returned.
     *          Case 1b) If the process offering supports “async-execute” and not “sync-execute”: Verify that a valid
     *          Execute wps:StatusInfo document is returned.
     *          Case 1c) If the process offering supports “sync-execute” and “async-execute”: Verify that a valid
     *          Execute wps:Result document or a valid wps:StatusInfo document is returned.
     *      Case 2) If the process offering supports raw output set “response” parameter equal to “raw”. Check the
     *      execute response according to the following cases:
     *          Case 2a) If the process offering supports “sync-execute” and not “async-execute”: Verify that valid
     *          that raw data is returned.
     *          Case 2b) If the process offering supports “async-execute” and not “sync-execute”: Verify that a valid
     *          Execute wps:StatusInfo document is returned.
     *          Case 2c) If the process offering supports “sync-execute” and “async-execute”: Verify that raw data or a
     *          valid wps:StatusInfo document is returned.
     *
     * Overall test passes if all individual tests pass.
     */
    public void A513Test(){

    }

    /**
     * Verify that the server can handle GetStatus requests via POST/XML.
     *
     * Send a valid XML Execute request to the server under test, setting the “mode” attribute to “async”. Verify that
     * a valid wps:StatusInfo document is returned. Extract the wps:JobID.
     *
     * Send a valid XML GetStatus request to the server under test using the extracted JobID. Test passes if a valid
     * wps:StatusInfo document is returned.
     */
    public void A514Test(){

    }

    /**
     * Verify that the server can handle GetResult requests via POST/XML.
     *
     * Send a valid XML Execute request to the server under test, setting the “mode” attribute to “async”. Modulate the
     * “response” parameter. Verify that a valid wps:StatusInfo document is returned. Extract the wps:JobID. Check the
     * status of the job. If the job succeeded, send a valid XML GetResult request to the server under test using the
     * extracted JobID. Depending on the value of the “response” parameter of the above Execute request:
     *      Parameter value equal “document”. Verify that a valid Execute wps:Result document is returned.
     *      Parameter equal to “raw”. Verify that raw is returned.
     *
     *      Overall test passes if all individual tests pass.
     */
    public void A515Test(){

    }

    /**
     * Verify that the server can handle GetCapabilities requests via GET/KVP.
     *
     * Send a valid KVP GetCapabilities request to the server under test, modulating upper and lower case of the
     * parameter names. Test passes if a valid document of the type wps:Capabilities is returned.
     */
    public void A516Test(){

    }

    /**
     * Verify that the server can handle DescribeProcess requests via GET/KVP.
     *
     * Send a valid KVP DescribeProcess request to the server under test, modulating upper and lower case of the
     * parameter names. Test passes if a valid document of the type wps:ProcessOfferings is returned.
     */
    public void A517Test(){

    }

    /**
     * Verify that the server can handle GetStatus requests via GET/KVP.
     *
     * Send a valid XML Execute request to the server under test, setting the “mode” attribute to “async”. Verify that
     * a valid wps:StatusInfo document is returned. Extract the wps:JobID.
     *
     * Send a valid KVP GetStatus request to the server under test, using the extracted JobID and modulating upper and
     * lower case of the parameter names. Test passes if a valid document of the type wps:StatusInfo is returned.
     */
    public void A518Test(){

    }

    /**
     * Verify that the server can handle GetResult requests via GET/KVP.
     *
     * Send a valid XML Execute request to the server under test, setting the “mode” attribute to “async”. Modulate the
     * “response” parameter. Verify that a valid wps:StatusInfo document is returned. Extract the wps:JobID. Check the
     * status of the job. If the job succeeded, send a valid KVP GetResult request to the server under test using the
     * extracted JobID and modulating upper and lower case of the parameter names.
     * Depending on the value of the “response” parameter of the above Execute request:
     *      Parameter value equal “document”. Verify that a valid Execute wps:Result document is returned.
     *      Parameter equal to “raw”. Verify that raw is returned.
     *
     * Overall test passes if all individual tests pass.
     */
    public void A519Test(){

    }

    /**
     * Verify that the server correctly advertises dismiss capabilities. Verify that the server correctly implements
     * the Dismiss operation.
     *
     * Verify that the Dismiss operation appears in the ows:OperationsMetadata element. Verify that the server offers
     * at least one wps:ProcessSummary element whose “jobControlOptions” attribute contains “dismiss”. Verify that the
     * service supports at least one binding (e.g. HTTP GET/POST) for the dismiss operation by verifying A.6.1 and/or
     * A.6.2.
     */
    @Test
    public void A6Test(){
        A611Test();
        A612Test();
    }

    /**
     * Verify that the server can handle Dismiss requests via POST/XML.
     *
     * Precondition: The process offering used for testing must have “dismiss” listed among its job control options.
     *
     * Send a valid XML Execute request to the server under test, setting the “mode” attribute to “async”. Verify that
     * a valid wps:StatusInfo document is returned. Extract the wps:JobID.
     *
     * Send a valid XML Dismiss request to the server under test using the extracted JobID. Test passes if a valid
     * wps:StatusInfo document is returned containing a wps:Status element with value ”Dismissed” (case insensitive).
     */
    public void A611Test(){

    }

    /**
     * Verify that the server can handle Dismiss requests via GET/KVP.
     *
     * Precondition: The process offering used for testing must have “dismiss” listed among its job control options.
     *
     * Send a valid XML Execute request to the server under test, setting the “mode” attribute to “async”. Verify that
     * a valid wps:StatusInfo document is returned. Extract the wps:JobID.
     *
     * Send a valid KVP Dismiss request to the server under test using the extracted JobID and modulating upper and
     * lower case of the parameter names. Test passes if a valid document of the type wps:StatusInfo document is
     * returned containing a wps:Status element with value ”Dismissed” (case insensitive).
     */
    public void A612Test(){

    }
}
