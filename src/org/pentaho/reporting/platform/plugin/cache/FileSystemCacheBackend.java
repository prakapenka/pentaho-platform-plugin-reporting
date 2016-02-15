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

import org.apache.commons.io.FileUtils;
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
public class FileSystemCacheBackend implements ICacheBackend {
  private static String DEFAULT_CACHE_PATH = "pentaho-reporting-plugin";
  private String cachePath = "";

  public FileSystemCacheBackend() {
    this.cachePath = getCachePath();
  }

  @Override
  public boolean write( final String key, final Serializable value ) {
    final File file = new File( cachePath + key );

    final ObjectOutputStream oos;
    final FileOutputStream fout;
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
    } catch ( final IOException e ) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  @Override
  public Object read( final String key ) {
    final ObjectInputStream objectinputstream;
    Object result = null;

    try {
      final FileInputStream fis =
        new FileInputStream( cachePath + key );
      objectinputstream = new ObjectInputStream( fis );
      result = objectinputstream.readObject();
      objectinputstream.close();
      fis.close();
    } catch ( final Exception e ) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public boolean purge( final String key ) {
    try {
      final File file = new File( cachePath + key );
      if ( file.isDirectory() ) {
        FileUtils.deleteDirectory( file );
        return !file.exists();
      }
      return file.delete();
    } catch ( final Exception e ) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public Set<String> listKeys( final String directoryName ) {
    final Set<String> resultSet = new HashSet<String>();
    final File directory =
      new File( cachePath + getSeparator() + directoryName );

    // get all the files from a directory
    final File[] fList = directory.listFiles();
    if ( fList != null ) {
      for ( final File file : fList ) {
        if ( file.isFile() ) {
          resultSet.add( file.getName() );
        } else if ( file.isDirectory() ) {
          listKeys( file.getAbsolutePath() );
        }
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
    String s = System.getProperty( "java.io.tmpdir" ); //$NON-NLS-1$
    char c = s.charAt( s.length() - 1 );
    if ( ( c != '/' ) && ( c != '\\' ) ) {
      System.setProperty( "java.io.tmpdir", s + "/" ); //$NON-NLS-1$//$NON-NLS-2$
    }
    return s + cachePath + getSeparator();
  }

}
