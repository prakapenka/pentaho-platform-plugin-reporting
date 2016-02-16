package org.pentaho.reporting.platform.plugin.async;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlTableModule;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.platform.plugin.ExecuteReportContentHandler;
import org.pentaho.reporting.platform.plugin.SimpleReportingComponent;
import org.pentaho.reporting.platform.plugin.staging.AsyncJobFileStagingHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dima.prokopenko@gmail.com on 2/2/2016.
 */
public class PentahoAsyncReportExecution implements AsyncReportExecution<InputStream> {

  private ILogger logger;

  private SimpleReportingComponent reportComponent;
  private AsyncJobFileStagingHandler handler;
  private String url;

  private String userSession = "";
  private String userName = "";
  private String insnaceId = "";

  private AsyncReportStatusListener listener;

  public PentahoAsyncReportExecution( String url, SimpleReportingComponent reportComponent, AsyncJobFileStagingHandler handler, ILogger logger )
      throws ResourceException, IOException {
    this.reportComponent = reportComponent;
    this.handler = handler;
    this.url = url;
  }

  public void forSession( String userSession ) {
    this.userSession = userSession;
  }

  public void forUser ( String userName ) {
    this.userName = userName;
  }

  public void forInstanceId ( String insnaceId ) {
    this.insnaceId = insnaceId;
  }

  public void withLogger( ILogger logger ) {
    this.logger = logger;
  }

  @Override
  public InputStream call() throws Exception {
    final long start = System.currentTimeMillis();
    AuditHelper.audit( userSession, userSession, url, getClass().getName(), getClass()
        .getName(), MessageTypes.INSTANCE_START, insnaceId, "", 0, logger );

    // let's do it!
    listener.setStatus( AsyncExecutionStatus.WORKING );

    final MasterReport report = reportComponent.getReport();

    //async is always fully buffered
    report.getReportConfiguration().setConfigProperty( ExecuteReportContentHandler.FORCED_BUFFERED_WRITING, "false" );

    long end = 0;
    if ( reportComponent.execute() ) {
      listener.setStatus( AsyncExecutionStatus.FINISHED );

      end = System.currentTimeMillis();
      AuditHelper.audit( userSession, userSession, url, getClass().getName(), getClass()
          .getName(), MessageTypes.FAILED, insnaceId, "", ( (float) ( end - start ) / 1000 ), logger );


      return handler.getStagingContent();
    }

    listener.setStatus( AsyncExecutionStatus.FAILED );

    AuditHelper.audit( userSession, userSession, url, getClass().getName(), getClass()
        .getName(), MessageTypes.FAILED, insnaceId, "", 0, logger );
    return new NullInputStream( 0 );
  }

  @Override
  public AsyncReportState getState() {
    return listener.clone();
  }

  @Override
  public String toString() {
    return this.listener.toString();
  }

  @Override
  public void setListener( AsyncReportStatusListener listener ) {
    this.listener = listener;
  }

  public AsyncReportStatusListener getListener() {
    return this.listener;
  }

  public String getMimeType() {
    return reportComponent.getMimeType();
  }

  public String getReportPath() {
    return this.url;
  }
}
