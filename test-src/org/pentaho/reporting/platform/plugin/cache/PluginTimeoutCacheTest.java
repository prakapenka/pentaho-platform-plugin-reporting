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

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test timeout cache
 */
public class PluginTimeoutCacheTest extends AbstractCacheTest {

  @Test
  public void testPutGet() throws Exception {
    final IPluginCache cache = iPluginCacheManager.getCache( PluginFirstSeeCache.class );
    cache.put( SOME_KEY, SOME_VALUE );
    assertNotNull( cache.get( SOME_KEY + PluginFirstSeeCache.TIMESTAMP ) );
    assertEquals( cache.get( SOME_KEY ), SOME_VALUE );
  }

  @Test
  public void testEviction() throws Exception {
    final IPluginCache cache = new PluginTimeoutCache( fileSystemCacheBackend, 1, 1, TimeUnit.SECONDS );
    cache.put( SOME_KEY, SOME_VALUE );
    assertEquals( cache.get( SOME_KEY ), SOME_VALUE );
    Thread.sleep( 3000 );
    assertNull( cache.get( SOME_KEY ) );
  }

}