/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this
 *  program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  or from the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved. *
 */
package org.pentaho.reporting.platform.plugin.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Eviction strategy with clean up after first access
 */
public class PluginFirstSeeCache extends PluginTimeoutCache {

  private static final Log logger = LogFactory.getLog( PluginFirstSeeCache.class );

  private static final String SEGMENT = "first_see";

  public PluginFirstSeeCache( final ICacheBackend backend ) {
    super( backend );
  }

  /**
   * Cleanup is performed on first time user sees the content otherwise it works the same way as timeout cache
   *
   * @param key key
   * @return value
   */
  @Override public Object get( final String key ) {
    final Object o = super.get( key );
    if ( o != null ) {
      logger.debug( "Cleaning first see cache: " + key );
      getBackend().purge( getKey( key ) );
      getBackend().purge( getKey( key + TIMESTAMP ) );
    }
    return o;
  }

  @Override protected String getSegment() {
    return SEGMENT;
  }
}
