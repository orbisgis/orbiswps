package org.orbisgis.orbiswps.service.process;

import org.junit.BeforeClass;
import org.junit.Test;
import org.orbisgis.orbiswps.service.WpsServiceImpl;
import org.orbisgis.orbiswps.service.model.JaxbContainer;
import org.orbisgis.orbiswps.service.model.wpsmodel.WpsModel;
import org.orbisgis.orbiswps.service.operations.WPS_2_0_ServerProperties;
import org.orbisgis.orbiswps.service.utils.WpsServerUtils;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModelWorkerTest {

    private static ProcessManagerImpl processManagerImpl;
    private static WPS_2_0_ServerProperties props;
    private static ProcessIdentifierImpl id;

    @BeforeClass
    public static void init() throws JAXBException, SQLException, URISyntaxException {
        Unmarshaller unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
        DataSource dataSource = org.h2gis.functions.factory.H2GISDBFactory.createDataSource(ModelWorkerTest.class.getSimpleName(), true);
        props = new WPS_2_0_ServerProperties();
        WpsServiceImpl wpsServiceImpl = new WpsServiceImpl(dataSource, Executors.newSingleThreadExecutor());
        processManagerImpl = new ProcessManagerImpl(wpsServiceImpl, dataSource);
        processManagerImpl.addScript(ModelWorkerTest.class.getResource("A.groovy").toURI());
        processManagerImpl.addScript(ModelWorkerTest.class.getResource("B.groovy").toURI());
        processManagerImpl.addScript(ModelWorkerTest.class.getResource("C.groovy").toURI());
        WpsModel model = (WpsModel) unmarshaller.unmarshal(ModelWorkerTest.class.getResourceAsStream("model_simple.xml"));
        id = new ProcessIdentifierImpl(WpsServerUtils.getProcessOfferingFromModel(model, processManagerImpl), "");
        id.setModel(model);
    }

    @Test
    public void testModelExecutionTree() {
        ModelWorker worker = new ModelWorker(props, id, processManagerImpl, new HashMap<URI, Object>());
        Map<Integer, List<String>> map = worker.getExecutionTree();
        assertEquals(3, map.size());
        assertEquals(1, map.get(0).size());
        assertEquals("B", map.get(0).get(0));
        assertEquals("A", map.get(1).get(0));
        assertEquals("C", map.get(2).get(0));
    }

    @Test
    public void testModelProcessExecution() throws ExecutionException, InterruptedException {
        ModelWorker worker = new ModelWorker(props, id, processManagerImpl, new HashMap<URI, Object>());
        Map<URI, Object> map = new HashMap<>();
        map.put(URI.create("A:Ain1"), "t");
        map.put(URI.create("A:Ain2"), "a");
        Future future = worker.executeProcess("A", map);
        future.get();
        assertTrue(map.containsKey(URI.create("A:Aout1")));
        assertEquals("ta", map.get(URI.create("A:Aout1")));
    }

    @Test
    public void testModelDataMap() {
        ModelWorker worker = new ModelWorker(props, id, processManagerImpl, new HashMap<URI, Object>());
        worker.getExecutionTree();
        Map<URI, Object> dataMap = worker.getDataMap();
        assertEquals(4, dataMap.size());
        assertEquals("t", dataMap.get(URI.create("input1")));
        assertEquals("a", dataMap.get(URI.create("input2")));
        assertEquals("t", dataMap.get(URI.create("input3")));
        assertEquals("toto", dataMap.get(URI.create("input4")));
    }

    @Test
    public void testModelRun() {
        ModelWorker worker = new ModelWorker(props, id, processManagerImpl, new HashMap<URI, Object>());
        worker.run();
        Map<URI, Object> dataMap = worker.getDataMap();
        URI uri = URI.create("C:Cout1");
        assertTrue(dataMap.containsKey(uri));
        assertEquals("tatatoto", dataMap.get(uri));
    }
}
