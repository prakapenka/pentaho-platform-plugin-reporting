package org.pentaho.reporting.platform.plugin.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ILogoutListener;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.platform.plugin.staging.AsyncJobFileStagingHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by dima.prokopenko@gmail.com on 2/2/2016.
 */
//TODO system shutdown call?
public class PentahoAsyncExecutor implements ILogoutListener {

  public static final String BEAN_NAME = "reporting-async-thread-pool";

  private static final Log log = LogFactory.getLog( PentahoAsyncExecutor.class );

  private Map<CompositeKey, Future<InputStream>> tasks = new ConcurrentHashMap<>();
  private Map<CompositeKey, AsyncReportStatusListener> listeners = new ConcurrentHashMap<>();

  private ExecutorService executorService;

  /**
   * Spring supports beans with private constructors ))
   *
   * @param capacity
   */
  private PentahoAsyncExecutor( int capacity ) {
    log.info( "Initialized reporting  async execution fixed thread pool with capacity: " + capacity );
    executorService = Executors.newFixedThreadPool( capacity );
    PentahoSystem.addLogoutListener( this );
  }

  // default visibility for testing purpose
  static class CompositeKey {

    private String sessionId;
    private String uuid;

    CompositeKey( IPentahoSession session, UUID id ) {
      this.uuid = id.toString();
      this.sessionId = session.getId();
    }

    private String getSessionId() {
      return sessionId;
    }

    @Override
    public boolean equals( Object o ) {
      if ( this == o )
        return true;
      if ( o == null || getClass() != o.getClass() )
        return false;
      CompositeKey that = (CompositeKey) o;
      return Objects.equals( sessionId, that.sessionId ) && Objects.equals( uuid, that.uuid );
    }

    @Override
    public int hashCode() {
      return Objects.hash( sessionId, uuid );
    }
  }

  public UUID addTask( PentahoAsyncReportExecution task ) {

    final IPentahoSession session = PentahoSessionHolder.getSession();
    UUID id = UUID.randomUUID();
    CompositeKey key = new CompositeKey( session, id );

    AsyncReportStatusListener listener = new AsyncReportStatusListener( task.getReportPath(), id, task.getMimeType() );
    task.setListener( listener );

    log.debug( "register async execution for task: " + task.toString() );

    Future<InputStream> result = executorService.submit( task );
    tasks.put( key, result );
    listeners.put( key, listener );
    return id;
  }

  public Future<InputStream> getFuture( UUID id ) {
    if ( id == null ) {
      throw new NullPointerException( "uuid is null" );
    }
    // get effective session for every call
    final IPentahoSession session = PentahoSessionHolder.getSession();
    return tasks.get( new CompositeKey( session, id ) );
  }

  public AsyncReportState getReportState( UUID id ) {
    if ( id == null ) {
      throw new NullPointerException( "uuid is null" );
    }
    // get effective session for every call
    final IPentahoSession session = PentahoSessionHolder.getSession();
    // link to running task
    AsyncReportStatusListener runningTask = listeners.get( new CompositeKey( session, id ) );

    return runningTask.clone();
  }

  @Override
  public void onLogout( IPentahoSession iPentahoSession ) {
    if ( log.isDebugEnabled() ) {
      // don't expose full session id.
      log.debug( "killing async report execution cache for " + iPentahoSession.getId().substring( 0, 10 ) );
    }
    for ( Map.Entry<CompositeKey, Future<InputStream>> entry : tasks.entrySet() ) {
      if ( entry.getKey().getSessionId().equals( iPentahoSession.getId() ) ) {
        //TODO future cancel impl.
        entry.getValue().cancel( true );
      }
    }

    // do it generic way according to staging handler was used?
    Path stagingSessionDir = AsyncJobFileStagingHandler.getStagingDirPath().resolve( iPentahoSession.getId() );
    File sessionStagingContent = stagingSessionDir.toFile();
    if ( sessionStagingContent.exists() ) {
      try {
        Files.walkFileTree( stagingSessionDir, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
            Files.delete( file );
            return FileVisitResult.CONTINUE;
          }
          @Override
          public FileVisitResult visitFileFailed( Path file, IOException e ) throws IOException {
            Files.delete( file );
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory( Path dir, IOException e ) throws IOException {
            if ( e == null ) {
              Files.delete( dir );
              return FileVisitResult.CONTINUE;
            } else {
              throw e;
            }
          }
        } );
      } catch ( IOException e ) {
        log.debug( "Unable delete temp files on session logout." );
      }
    }
  }
}
