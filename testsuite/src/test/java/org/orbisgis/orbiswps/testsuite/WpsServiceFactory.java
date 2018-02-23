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
import org.orbisgis.orbiswps.scripts.WpsScriptPlugin;
import org.orbisgis.orbiswps.service.WpsServerImpl;
import org.orbisgis.orbiswps.serviceapi.WpsServer;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.Executors;

/**
 * @author Sylvain PALOMINOS
 */
public class WpsServiceFactory {

    public static WpsServer getService() throws SQLException {
        DataSource dataSource = SFSUtilities.wrapSpatialDataSource(H2GISDBFactory.createDataSource(
                GetCapabilitiesTest.class.getSimpleName(), false));
        WpsServerImpl service = new WpsServerImpl(dataSource,
                WpsServiceFactory.class.getResource("fullWpsService.properties").getFile(),
                Executors.newSingleThreadExecutor());

        WpsScriptPlugin plugin = new WpsScriptPlugin();
        plugin.activate();
        service.addWpsScriptBundle(plugin);

        return service;
    }
}
