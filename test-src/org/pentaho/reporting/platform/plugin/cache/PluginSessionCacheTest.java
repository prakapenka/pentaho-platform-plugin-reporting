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
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test Session cache
 */
public class PluginSessionCacheTest extends AbstractCacheTest {


  @Test
  public void testPutGet() throws Exception {
    PentahoSessionHolder.setSession( new StandaloneSession( "test", "100500" ) );
    final IPluginCache cache = iPluginCacheManager.getCache( PluginSessionCache.class );
    cache.put( SOME_KEY, SOME_VALUE );
    assertEquals( cache.get( SOME_KEY ), SOME_VALUE );
  }

  @Test
  public void testEviction() throws Exception {
    final StandaloneSession session = new StandaloneSession( "test", "100500" );
    PentahoSessionHolder.setSession( session );

    final IPluginCache cache = iPluginCacheManager.getCache( PluginSessionCache.class );
    cache.put( SOME_KEY, SOME_VALUE );
    assertEquals( cache.get( SOME_KEY ), SOME_VALUE );

    final StandaloneSession session1 = new StandaloneSession( "test1", "100501" );
    PentahoSessionHolder.setSession( session1 );
    cache.put( SOME_KEY, SOME_VALUE );
    assertEquals( cache.get( SOME_KEY ), SOME_VALUE );
    PentahoSystem.invokeLogoutListeners( session );
    assertEquals( cache.get( SOME_KEY ), SOME_VALUE );
    PentahoSessionHolder.setSession( session );
    assertNull( cache.get( SOME_KEY ) );
  }


}