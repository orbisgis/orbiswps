package org.orbisgis.orbiswps.serviceapi.process;

import java.beans.PropertyChangeListener;
import java.util.UUID;

/**
 * Class executing a process
 *
 * @author Sylvain PALOMINOS (CNRS 2017, UBS 2018)
 * @author Erwan Bocher (CNRS)
 */
public interface ProcessWorker extends Runnable, PropertyChangeListener {


    UUID getJobId();
}
