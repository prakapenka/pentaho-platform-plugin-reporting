/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.platform.plugin.staging;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.reporting.engine.classic.core.util.StagingMode;
import org.pentaho.reporting.platform.plugin.TrackingOutputStream;

import java.io.*;

/**
 * Async stage handler.
 * Сдщыу to TEMP file handler but:
 * - do not keep link to passed output streamб вo not write to output stream passed in constructor.
 * - live between requests.
 * - require to re-set output stream for ready-to-fetch-request to fetch ready to use data.
 *
 *
 *
 * Created by dima.prokopenko@gmail.com on 2/10/2016.
 */
public class AsyncJobFileStagingHandler extends AbstractStagingHandler {

  private static String PREFIX = "repasyncstg";
  private static String POSTFIX = ".tmp";

  private static final Log logger = LogFactory.getLog( AsyncJobFileStagingHandler.class );

  private TrackingOutputStream fileTrackingStream;
  File tmpFile;

  public AsyncJobFileStagingHandler( OutputStream outputStream, IPentahoSession userSession ) throws IOException {
    super( outputStream, userSession );
  }

  @Override
  protected void initialize() throws IOException {
    logger.trace( "Staging mode set - TEMP_FILE, async report generation" );

    // do not write to output stream passed in constructor,
    // do not keep link to parent output stream, just forget it.
    //TODO remove output stream from constructor?
    super.outputStream = null;

    // prepare staging handler for async write
    final IApplicationContext appCtx = PentahoSystem.getApplicationContext();
    // Use the deleter framework for safety...
    if ( userSession.getId().length() >= 10 ) {
      tmpFile = appCtx.createTempFile( userSession, PREFIX, POSTFIX, true );
    } else {
      // Workaround bug in appContext.createTempFile ... :-(
      //TODO what this bug number???
      final File parentDir = new File( appCtx.getSolutionPath( "system/tmp" ) ); //$NON-NLS-1$
      final ITempFileDeleter fileDeleter =
          (ITempFileDeleter) userSession.getAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE );
      final String newPrefix = PREFIX + UUIDUtil.getUUIDAsString().substring( 0, 10 ) + "-";
      tmpFile = File.createTempFile( newPrefix, POSTFIX, parentDir );
      if ( fileDeleter != null ) {
        fileDeleter.trackTempFile( tmpFile );
      } else {
        // There is no deleter, so cleanup on VM exit. (old behavior)
        tmpFile.deleteOnExit();
      }
    }
    fileTrackingStream = new TrackingOutputStream( new BufferedOutputStream( new FileOutputStream( tmpFile ) ) );
  }

  @Override
  public StagingMode getStagingMode() {
    return StagingMode.TMPFILE;
  }

  @Override
  public boolean isFullyBuffered() {
    return true;
  }

  @Override
  public boolean canSendHeaders() {
    return true;
  }

  @Override
  public OutputStream getStagingOutputStream() {
    return fileTrackingStream;
  }

  @Override
  public void complete() throws IOException {
    if ( outputStream == null ) {
      throw new IOException( "Please set effective ouputStream" );
    }
    IOUtils.closeQuietly( fileTrackingStream );
    final BufferedInputStream bis = new BufferedInputStream( new FileInputStream( tmpFile ) );
    try {
      IOUtils.copy( bis, outputStream );
    } finally {
      IOUtils.closeQuietly( bis );
    }
  }

  public InputStream getStagingContent() throws FileNotFoundException {
    return new StagingInputStream( new FileInputStream( tmpFile ) );
  }

  @Override
  public void close() {
    this.closeQuietly();
  }

  @Override
  public int getWrittenByteCount() {
    return fileTrackingStream.getTrackingSize();
  }

  private void closeQuietly() {
    IOUtils.closeQuietly( fileTrackingStream );
    if ( tmpFile != null && tmpFile.exists() ) {
      try {
        boolean deleted = tmpFile.delete();
        if ( !deleted ) {
          logger.debug( "Unable to delete temp file for user: " + userSession.getName() );
        }
      } catch ( Exception ignored ) {
        logger.debug( "Exception when try to delete temp file for user: " + userSession.getName() );
      }
    }
  }

  /**
   * Inner class to be able to close staging resources
   * after closing of InputStream.
   *
   * Holding a link to this particular InputStream object will prevent
   * GC to collect staging handler.
   */
  private class StagingInputStream extends BufferedInputStream {

    public StagingInputStream( InputStream in ) {
      super( in );
    }

    public StagingInputStream( InputStream in, int size ) {
      super( in, size );
    }

    @Override
    public void close() throws IOException {
      try {
        closeQuietly();
      } catch ( Exception e ) {
        logger.debug( "Attempt to close quietly is not quietly" );
      } finally {
        super.close();
      }
    }
  }
}
