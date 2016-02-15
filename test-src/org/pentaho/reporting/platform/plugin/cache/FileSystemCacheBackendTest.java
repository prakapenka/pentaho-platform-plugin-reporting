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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FileSystemCacheBackendTest extends AbstractCacheTest {


  private static final String directoryKey = "id344324";
  private static final String key = "id344324" + "/file1.html";
  private static final String value = "SerializableObject";

  @Test
  public void testWriteRead() throws Exception {
    assertTrue( fileSystemCacheBackend.write( key, value ) );
    assertEquals( fileSystemCacheBackend.read( key ), value );
  }

  @Test
  public void testPurge() throws Exception {
    assertTrue( fileSystemCacheBackend.write( key, value ) );
    assertEquals( fileSystemCacheBackend.read( key ), value );
    assertTrue( fileSystemCacheBackend.purge( key ) );
    assertNull( fileSystemCacheBackend.read( key ) );
  }

  @Test
  public void testPurgeDir() throws Exception {
    assertTrue( fileSystemCacheBackend.write( key, value ) );
    assertEquals( fileSystemCacheBackend.read( key ), value );
    assertTrue( fileSystemCacheBackend.purge( directoryKey ) );
    assertNull( fileSystemCacheBackend.read( key ) );
  }

  @Test
  public void testlistKeys() throws Exception {
    final Set<String> resultSet = new HashSet<String>();

    for ( int i = 0; i < 10; i++ ) {
      final String filename = "file" + i + ".html";
      fileSystemCacheBackend.write( directoryKey + fileSystemCacheBackend.getSeparator() + filename, value );
      resultSet.add( filename );
    }

    assertEquals( resultSet, fileSystemCacheBackend.listKeys( directoryKey ) );
  }

  @Test
  public void testGetSeparator() throws Exception {
    assertEquals( File.separator, fileSystemCacheBackend.getSeparator() );
  }
}