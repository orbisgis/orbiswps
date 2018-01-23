package org.orbisgis.orbiswps.serviceapi;

import org.orbisgis.orbiswps.serviceapi.process.ProcessMetadata;
import org.xnap.commons.i18n.I18n;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Interface for the declaration of a WPS script bundle. The implemented class should be annotated @Component to be
 * recognized by OSGI and loaded inside the WpsService implemented class.
 *
 * @author Sylvain PALOMINOS
 */
public interface WpsScriptBundle {

    /**
     * Returns a map with the groovy properties needed by the scripts contained inside the bundle.
     * @return A map with the groovy property name as Key and the property object as Value
     */
    Map<String, Object> getGroovyProperties();

    /**
     * Return the List of all the URL of the script available in the bundle.
     * @return The List of the URL of the scripts.
     */
    List<URL> getScriptsList();

    /**
     * Return a map with the metadata of a script with the name as key and its value as value.
     * @param scriptUrl URL of the script.
     * @return A map with the metadata of a script with the name as key and its value as value.
     */
    Map<ProcessMetadata.INTERNAL_METADATA, Object> getScriptMetadata(URL scriptUrl);

    I18n getI18n();
}
