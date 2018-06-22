package org.orbisgis.orbiswps.serviceapi.process;

import groovy.lang.GroovyObject;
import net.opengis.ows._2.CodeType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Class used to manage process.
 *
 * @author Sylvain PALOMINOS (CNRS 2017, UBS 2018)
 * @author Erwan Bocher (CNRS)
 */
public interface ProcessManager {

    /**
     * Returns the ProcessIdentifier containing the process with the given CodeType.
     * @param identifier CodeType used as identifier of a process.
     * @return The process.
     */
    ProcessIdentifier getProcessIdentifier(CodeType identifier);

    /**
     * Returns all the process identifiers.
     * @return All the process identifiers.
     */
    List<ProcessIdentifier> getAllProcessIdentifier();

    /**
     * Indicates if a process is actually running.
     * @return True if a process is running, false otherwise.
     */
    Future executeNewProcessWorker(ProcessWorker processWorker);

    /**
     * Cancel the job corresponding to the jobID.
     * @param jobId Id of the job to cancel.
     */
    void cancelProcess(UUID jobId);

    /**
     * Execute the given process with the given model.
     * @param jobId UUID of the job to execute.
     * @param processIdentifier ProcessIdentifier of the process to execute.
     * @param dataMap Map containing the model for the process.
     * @param propertiesMap Map containing the properties for the GroovyObject.
     * @param progressMonitor ProgressMonitor associated to the process execution.
     * @return The groovy object on which the 'processing' method will be called.
     */
    GroovyObject executeProcess(UUID jobId, ProcessIdentifier processIdentifier, Map<URI, Object> dataMap,
                                Map<String, Object> propertiesMap, ProgressMonitor progressMonitor);

    /**
     * Schedule the destroying of a generated result at the given date.
     * @param resultUri Uri of the result to destroy.
     * @param date Date when the result should be destroyed.
     */
    void scheduleResultDestroying(URI resultUri, XMLGregorianCalendar date);
}
