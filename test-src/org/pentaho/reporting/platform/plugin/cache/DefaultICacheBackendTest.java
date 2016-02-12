/*!
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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.platform.plugin.cache;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class DefaultICacheBackendTest extends TestCase {
  DefaultICacheBackend defaultICacheBackend;
  OutputStream outputStream;
  PentahoSystem pentahoSystem;
  IApplicationContext iApplicationContext;
  String separator = File.separator;

  String path = "system/tmp/";
  String key = "id344324/file1.html";
  String directoryKey = "id344324/";
  String value = "SerializableObject";


  @Before
  public void setUp() throws Exception {
    outputStream = mock( OutputStream.class );
    pentahoSystem = mock( PentahoSystem.class );
    iApplicationContext = mock( IApplicationContext.class );
    pentahoSystem.setApplicationContext( iApplicationContext );
    defaultICacheBackend = new DefaultICacheBackend();
  }

  @Test
  public void testWrite() throws Exception {
    doReturn( path ).when( iApplicationContext ).getSolutionPath( any( String.class ) );
    File resultFile = new File( path + key );
    assertTrue( defaultICacheBackend.write( key, "SerializableObject" ) );
    assertTrue( resultFile.exists() );
  }

  @Test
  public void testRead() throws Exception {
    doReturn( path ).when( iApplicationContext ).getSolutionPath( any( String.class ) );
    File resultFile = new File( path + key );
    if ( !resultFile.exists() ) {
      resultFile.createNewFile();
    }

    FileOutputStream fout = new FileOutputStream( resultFile );
    ObjectOutputStream oos = new ObjectOutputStream( fout );
    oos.writeObject( value );
    oos.close();

    assertEquals( value, defaultICacheBackend.read( key ) );
  }

  @Test
  public void testPurge() throws Exception {
    doReturn( path ).when( iApplicationContext ).getSolutionPath( any( String.class ) );
    File resultFile = new File( path + key);
    if (!resultFile.exists()) {
      resultFile.createNewFile();
    }
    assertTrue( resultFile.exists() );
    assertTrue( defaultICacheBackend.purge( key ) );
    assertFalse( resultFile.exists() );
  }

  @Test
  public void testlistKeys() throws Exception {
    Set<String> resultSet = new HashSet<String>();

    for (int i=0; i<5 ; i++) {
      resultSet.add( "file" + i + ".html" );
      File resultFile = new File(path + directoryKey + "file" + i + ".html");
      if (!resultFile.exists()) {
        resultFile.createNewFile();
      }
    }

    assertEquals( resultSet, defaultICacheBackend.listKeys( directoryKey ) );
  }

  @Test
  public void testGetSeparator() throws Exception {
    assertEquals( File.separator, defaultICacheBackend.getSeparator() );
  }
}