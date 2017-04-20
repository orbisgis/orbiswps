package org.orbiswps.server.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Utilities for the WPS script bundles.
 *
 * @author Sylvain PALOMINOS
 */
public class WpsScriptUtils {

    private static final I18n I18N = I18nFactory.getI18n(WpsScriptUtils.class);
    protected static final Logger LOGGER = LoggerFactory.getLogger(WpsScriptUtils.class);

    /**
     * Copy the given resource into the given folder.
     *
     * @param resourceFileUrl Url of the resource file to copy.
     * @param folder Folder where the file should be copied
     * @return The Copied File object.
     */
    public static File copyResourceFile(URL resourceFileUrl, File folder) {
        if (resourceFileUrl == null) {
            LOGGER.error(I18N.tr("Unable to get the URL of the process {0}", resourceFileUrl));
            return null;
        }
        final File tempFile = new File(folder.getAbsolutePath(), new File(resourceFileUrl.getFile()).getName());
        if (!tempFile.exists()) {
            try {
                if (!tempFile.createNewFile()) {
                    LOGGER.error(I18N.tr("Unable to create the script file."));
                    return null;
                }
            } catch (IOException e) {
                LOGGER.error(I18N.tr("Unable to create the icon file.\n Error : {0}", e.getMessage()));
            }
        }
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(resourceFileUrl.openStream(), out);
        } catch (Exception e) {
            LOGGER.error(I18N.tr("Unable to copy the content of the script to the temporary file."));
            return null;
        }
        return tempFile;
    }
}
