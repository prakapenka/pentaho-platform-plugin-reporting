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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Eviction strategy that kills cache by specified timeout Periodically it checks all each stored key and removes
 * expired ones
 */
public class PluginTimeoutCache extends AbstractPluginCache {

  private static final Log logger = LogFactory.getLog( PluginTimeoutCache.class );
  private static final String SEGMENT = "long_term";
  public static final String TIMESTAMP = "-timestamp";
  public static final int MILLIS_IN_DAY = 86400000;
  private final ScheduledExecutorService scheduler;
  private final long millisToLive;

  public PluginTimeoutCache( final ICacheBackend backend ) {
    this( backend, 1L, 1L, TimeUnit.HOURS );
  }

  protected PluginTimeoutCache( final ICacheBackend backend, final long initialDelay, final long period,
                                final TimeUnit timeUnit ) {
    super( backend );
    //Schedule eviction
    scheduler = Executors.newScheduledThreadPool( 1 );
    scheduler.scheduleAtFixedRate( new PeriodicalCacheEviction(), initialDelay, period, timeUnit );
    final int daysToLive = ClassicEngineBoot.getInstance().getExtendedConfig().getIntProperty(
      "org.pentaho.reporting.platform.plugin.cache.PentahoDataCache.CachableRowLimit" );
    millisToLive = daysToLive * MILLIS_IN_DAY;
  }

  @Override String getKey( final String key ) {
    return getSegment() + getBackend().getSeparator() + key;
  }

  /**
   * Saves value with timestamp
   *
   * @param key   key
   * @param value value
   * @return success
   */
  @Override public boolean put( final String key, final Serializable value ) {
    if ( super.put( key, value ) ) {
      return super.put( key + TIMESTAMP, System.currentTimeMillis() );
    }
    return false;
  }

  protected String getSegment() {
    return SEGMENT;
  }

  /**
   * Cache eviction runnable
   */
  private class PeriodicalCacheEviction implements Runnable {

    @Override public void run() {
      logger.debug( "Starting periodical cache eviction" );
      final long currentTimeMillis = System.currentTimeMillis();
      final ICacheBackend backend = getBackend();
      //Check all timestamps
      for ( final String key : backend.listKeys( getSegment() ) ) {
        if ( key.matches( ".*" + TIMESTAMP ) ) {
          final String fullPath = getSegment() + backend.getSeparator() + key;
          final Long timestamp = (Long) backend.read( fullPath );
          if ( currentTimeMillis - timestamp > millisToLive ) {
            backend.purge( fullPath.replace( TIMESTAMP, "" ) );
            backend.purge( fullPath );
            logger.debug( "Purged long-term cache: " + fullPath );
          }
        }
      }
      logger.debug( "Finished periodical cache eviction" );
    }
  }
}
