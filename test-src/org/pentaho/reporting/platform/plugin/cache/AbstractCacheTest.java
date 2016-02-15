/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.platform.plugin.cache;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Setup and teardown for cache tests
 */
public class AbstractCacheTest {
  public static final String SOME_KEY = "some_key";
  public static final String SOME_VALUE = "some_value";
  protected static FileSystemCacheBackend fileSystemCacheBackend;
  protected static IPluginCacheManager iPluginCacheManager;
  private static PentahoSystem pentahoSystem;

  @BeforeClass
  public static void setUp() throws Exception {
    pentahoSystem = mock( PentahoSystem.class );
    fileSystemCacheBackend = new FileSystemCacheBackend();
    iPluginCacheManager = new PluginCacheManagerImpl( fileSystemCacheBackend, strategy);
  }

  @AfterClass
  public static void teardown() {
    assertTrue( fileSystemCacheBackend.purge( "" ) );
  }
}
