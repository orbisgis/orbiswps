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
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
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
package org.orbisgis.orbiswps.service.utils;

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
