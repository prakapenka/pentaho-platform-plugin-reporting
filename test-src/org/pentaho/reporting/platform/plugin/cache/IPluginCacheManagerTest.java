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

import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;

import java.io.Serializable;

import static org.junit.Assert.assertNotNull;

/**
 * Simple test for cache manager
 * Created by Yahor_Zhuk on 2/12/2016.
 */
public class IPluginCacheManagerTest {

  private class TestCache implements IPluginCache {

    @Override public boolean put( final String key, final Serializable value ) {
      return false;
    }

    @Override public Object get( final String key ) {
      return null;
    }
  }

  @Test
  public void testGetCache() throws Exception {
    final IPluginCacheManager iPluginCacheManager = new PluginCacheManagerImpl( new FileSystemCacheBackend(), strategy);
    assertNotNull( iPluginCacheManager.getCache( PluginSessionCache.class ) );
    assertNotNull( iPluginCacheManager.getCache( PluginTimeoutCache.class ) );
    assertNotNull( iPluginCacheManager.getCache( PluginFirstSeeCache.class ) );
  }

  @Test
  public void testAddCache() throws Exception {
    final IPluginCacheManager iPluginCacheManager = new PluginCacheManagerImpl( new FileSystemCacheBackend(), strategy);
    iPluginCacheManager.addCache( new TestCache() );
    assertNotNull( iPluginCacheManager.getCache( TestCache.class ) );
  }
}