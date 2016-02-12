/* !
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
 *  Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 *
 */

package org.pentaho.reporting.platform.plugin.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.libraries.base.config.ExtendedConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Default interface for cache backend
 */
public class DefaultICacheBackend implements ICacheBackend {
  private static final Log logger = LogFactory.getLog( PluginTimeoutCache.class );
  private static String DEFAULT_CACHE_PATH = "system/tmp";
  private String cachePath = "";

  public DefaultICacheBackend() {
    this.cachePath = getCachePath();
  }

  @Override
  public boolean write( String key, final Serializable value ) {
    final IApplicationContext appCtx = PentahoSystem.getApplicationContext();
    final File file = new File( new StringBuilder( appCtx.getSolutionPath( cachePath ) ).append( key ).toString() );

    ObjectOutputStream oos = null;
    FileOutputStream fout = null;
    try {
      file.getParentFile().mkdirs();
      if ( !file.exists() ) {
        file.createNewFile();
      }

      fout = new FileOutputStream( file );
      oos = new ObjectOutputStream( fout );
      oos.writeObject( value );
      oos.close();
      fout.close();
    } catch ( IOException e ) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  @Override
  public Object read( String key ) {
    ObjectInputStream objectinputstream = null;
    Object result = null;

    final IApplicationContext appCtx = PentahoSystem.getApplicationContext();
    try {
      FileInputStream fis =
        new FileInputStream( new StringBuilder( appCtx.getSolutionPath( cachePath ) ).append( key ).toString() );
      objectinputstream = new ObjectInputStream( fis );
      result = objectinputstream.readObject();
      objectinputstream.close();
      fis.close();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public boolean purge( String key ) {
    try {
      final IApplicationContext appCtx = PentahoSystem.getApplicationContext();
      final File file = new File( new StringBuilder( appCtx.getSolutionPath( cachePath ) ).append( key ).toString() );
      return file.delete();
    } catch ( Exception e ) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public Set<String> listKeys( String directoryName ) {
    Set<String> resultSet = new HashSet<String>();
    File directory =
      new File( new StringBuilder( cachePath ).append( getSeparator() ).append( directoryName ).toString() );

    // get all the files from a directory
    File[] fList = directory.listFiles();
    for ( File file : fList ) {
      if ( file.isFile() ) {
        resultSet.add( file.getName() );
      } else if ( file.isDirectory() ) {
        listKeys( file.getAbsolutePath() );
      }
    }
    return resultSet;
  }

  @Override
  public String getSeparator() {
    return File.separator;
  }

  private String getCachePath() {
    final ExtendedConfiguration config = ClassicEngineBoot.getInstance().getExtendedConfig();
    String cachePath =
      config.getConfigProperty( "org.pentaho.reporting.platform.plugin.cache.ICacheBackendPath" );
    if ( StringUtil.isEmpty( cachePath ) ) {
      cachePath = DEFAULT_CACHE_PATH;
    }
    return cachePath;
  }

}
