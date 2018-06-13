package org.orbisgis.orbiswps.service.process;

import org.junit.BeforeClass;
import org.junit.Test;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.service.model.JaxbContainer;
import org.orbisgis.orbiswps.service.model.wpsmodel.WpsModel;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModelWorkerTest {

    private static Unmarshaller unmarshaller;
    private static WpsServerImpl wpsServerImpl;
    private static ProcessManager processManager;
    private static DataSource dataSource;

    @BeforeClass
    public static void init() throws JAXBException, SQLException, ClassNotFoundException {
        unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
        dataSource = org.h2gis.functions.factory.H2GISDBFactory.createDataSource(ModelWorkerTest.class.getSimpleName(), true);
        wpsServerImpl = new WpsServerImpl(dataSource, Executors.newSingleThreadExecutor());
        processManager = new ProcessManager(dataSource, wpsServerImpl);
    }

    @Test
    public void testModel() throws JAXBException {
        WpsModel model = (WpsModel)unmarshaller.unmarshal(this.getClass().getResourceAsStream("model_simple.xml"));
        ModelWorker worker = new ModelWorker(model,wpsServerImpl, processManager);
        Map<Integer, List<String>> map = worker.getExecutionTree();
        assertEquals(3, map.size());
        assertEquals(1, map.get(0).size());
        assertEquals("B", map.get(0).get(0));
        assertEquals("A", map.get(1).get(0));
        assertEquals("C", map.get(2).get(0));
    }
}
