package org.orbisgis.orbiswps.service.process;

import org.junit.BeforeClass;
import org.junit.Test;
import org.orbisgis.orbiswps.service.WpsServiceImpl;
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
    private static WpsServiceImpl wpsServerImpl;
    private static ProcessManagerImpl processManagerImpl;
    private static DataSource dataSource;

    @BeforeClass
    public static void init() throws JAXBException, SQLException {
        unmarshaller = JaxbContainer.JAXBCONTEXT.createUnmarshaller();
        dataSource = org.h2gis.functions.factory.H2GISDBFactory.createDataSource(ModelWorkerTest.class.getSimpleName(), true);
        wpsServerImpl = new WpsServiceImpl(dataSource, Executors.newSingleThreadExecutor());
        processManagerImpl = new ProcessManagerImpl(wpsServerImpl, dataSource);
    }

    @Test
    public void testModel() throws JAXBException {
        WpsModel model = (WpsModel)unmarshaller.unmarshal(this.getClass().getResourceAsStream("model_simple.xml"));
        ModelWorker worker = new ModelWorker(model,wpsServerImpl, processManagerImpl);
        Map<Integer, List<String>> map = worker.getExecutionTree();
        assertEquals(3, map.size());
        assertEquals(1, map.get(0).size());
        assertEquals("B", map.get(0).get(0));
        assertEquals("A", map.get(1).get(0));
        assertEquals("C", map.get(2).get(0));
    }
}
