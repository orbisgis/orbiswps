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
package org.orbisgis.orbiswps.scripts;

import org.junit.Assert;
import org.junit.Test;
import org.orbisgis.orbiswps.serviceapi.process.ProcessMetadata.INTERNAL_METADATA;
import org.osgi.framework.BundleException;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Class to test all the basic WPS script plugin methods.
 *
 * @author Sylvain PALOMINOS
 */
public class WpsScriptPluginTest {

    /**
     * Test the acyivation and deactivation of the plugin.
     */
    @Test
    public void bundleLifeCycleTest(){
        WpsScriptPlugin plugin = new WpsScriptPlugin();
        plugin.activate();
        plugin.deactivate();
    }

    /**
     * Test the {@link WpsScriptPlugin#getScriptsList()} method.
     */
    @Test
    public void getGroovyPropertiesTest(){
        WpsScriptPlugin plugin = new WpsScriptPlugin();
        plugin.activate();
        Map<String, Object> propertyMap = plugin.getGroovyProperties();
        Assert.assertEquals("The plugin property map should contain 1 element", 1, propertyMap.size());
        Assert.assertTrue("The plugin property map should a property call i18n", propertyMap.containsKey("i18n"));
    }

    /**
     * Test the {@link WpsScriptPlugin#getScriptsList()} method.
     */
    @Test
    public void getScriptListTest() throws BundleException {
        WpsScriptPlugin plugin = new WpsScriptPlugin();
        plugin.activate();
        List<URL> scriptList = plugin.getScriptsList();
        for(URL url : scriptList){
            if(!url.getFile().endsWith(".groovy")){
                Assert.fail("Only the groovy files should be returned.");
            }
        }
    }

    @Test
    public void getScriptMetadataTest() {
        WpsScriptPlugin plugin = new WpsScriptPlugin();
        plugin.activate();
        List<URL> urlList = plugin.getScriptsList();
        for(URL u : urlList){
            Map<INTERNAL_METADATA, Object> map = plugin.getScriptMetadata(u);
            Assert.assertFalse("The script metadata should contain 'ICON_ARRAY'",
                    !map.containsKey(INTERNAL_METADATA.ICON_ARRAY));
            Assert.assertFalse("The script metadata should contain 'IS_REMOVABLE'",
                    !map.containsKey(INTERNAL_METADATA.IS_REMOVABLE));
            Assert.assertFalse("The script metadata should contain 'NODE_PATH'",
                    !map.containsKey(INTERNAL_METADATA.NODE_PATH));
        }
    }
}
