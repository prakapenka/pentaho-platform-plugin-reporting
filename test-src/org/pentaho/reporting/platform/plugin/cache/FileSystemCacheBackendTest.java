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

import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class FileSystemCacheBackendTest {
  private static FileSystemCacheBackend defaultICacheBackend;
  private static PentahoSystem pentahoSystem;

  private static final String directoryKey = "id344324";
  private static final String key = "id344324" + "/file1.html";
  private static final String value = "SerializableObject";

  @BeforeClass
  public static void setUp() throws Exception {
    pentahoSystem = mock( PentahoSystem.class );
    defaultICacheBackend = new FileSystemCacheBackend();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    Assert.assertTrue( defaultICacheBackend.purge( "" ) );
  }

  @Test
  public void testWriteRead() throws Exception {
    assertTrue( defaultICacheBackend.write( key, value ) );
    assertEquals( defaultICacheBackend.read( key ), value );
  }

  @Test
  public void testPurge() throws Exception {
    assertTrue( defaultICacheBackend.write( key, value ) );
    assertEquals( defaultICacheBackend.read( key ), value );
    assertTrue( defaultICacheBackend.purge( key ) );
    assertNull( defaultICacheBackend.read( key ) );
  }

  @Test
  public void testPurgeDir() throws Exception {
    assertTrue( defaultICacheBackend.write( key, value ) );
    assertEquals( defaultICacheBackend.read( key ), value );
    assertTrue( defaultICacheBackend.purge( directoryKey ) );
    assertNull( defaultICacheBackend.read( key ) );
  }

  @Test
  public void testlistKeys() throws Exception {
    Set<String> resultSet = new HashSet<String>();

    for ( int i = 0; i < 10; i++ ) {
      String e = "file" + i + ".html";
      defaultICacheBackend.write( directoryKey + defaultICacheBackend.getSeparator() + e, value );
      resultSet.add( e );
    }

    assertEquals( resultSet, defaultICacheBackend.listKeys( directoryKey ) );
  }

  @Test
  public void testGetSeparator() throws Exception {
    assertEquals( File.separator, defaultICacheBackend.getSeparator() );
  }
}