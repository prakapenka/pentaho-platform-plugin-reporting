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

package org.pentaho.reporting.platform.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.platform.plugin.async.AsyncReportState;
import org.pentaho.reporting.platform.plugin.async.AsyncReportStatusListener;
import org.pentaho.reporting.platform.plugin.async.PentahoAsyncExecutor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Created by dima.prokopenko@gmail.com on 2/8/2016.
 */
@Path("/reporting/api/jobs")
public class JobManager {

  private static final Log logger = LogFactory.getLog( JobManager.class );

  @GET
  public Response getEcho() {
    // I'm a teapot
    return Response.status( 418 ).build();
  }

  @GET
  @Path("{job_id}/content")
  public Response getContent(@PathParam("job_id") String job_id) throws IOException {
    UUID uuid = null;
    try {
      uuid = UUID.fromString( job_id );
    } catch ( Exception e ) {
      logger.error( "Invalid UUID: " + job_id );
      // The 422 (Unprocessable Entity) status code
      return Response.status( 422 ).build();
    }

    // get async bean:
    PentahoAsyncExecutor executor = getExecutor();
    if ( executor == null ) {
      return Response.serverError().build();
    }
    Future<InputStream> cachedReport = executor.getFuture( uuid );
    AsyncReportState state = executor.getReportState( uuid );

    InputStream input = null;
    try {
      input = cachedReport.get();
    } catch ( Exception e ) {
      logger.error( "Error generating report", e );
      return Response.serverError().build();
    }
    MediaType mediaType = null;
    try {
      mediaType = MediaType.valueOf( state.getMimeType() );
    } catch ( Exception e ) {
      logger.error( "can't determine JAX-RS media type for: " + state.getMimeType() );
      // may be this will work?
      return Response.ok( input, state.getMimeType() ).build();
    }
    // Response builder is responsible for closing InputStream
    return Response.ok( input, mediaType).build();
  }

  @GET
  @Path("{job_id}/status")
  @Produces("application/json")
  public Response getStatus(@PathParam("job_id") String job_id) {
    UUID uuid = null;
    try {
      uuid = UUID.fromString( job_id );
    } catch ( Exception e ) {
      logger.error( "Invalid UUID: " + job_id );
      // The 422 (Unprocessable Entity) status code
      return Response.status( 422 ).build();
    }

    PentahoAsyncExecutor executor = getExecutor();
    if ( executor == null ) {
      // where is my bean?
      return Response.serverError().build();
    }

    AsyncReportState responseJson = executor.getReportState( uuid );
    if ( responseJson == null ) {
      return Response.status( 422 ).build();
    }
    ObjectMapper mapper = new ObjectMapper();
    String json = null;
    try {
      json = mapper.writeValueAsString( responseJson );
    } catch ( Exception e ) {
      logger.error( "unable to deserialize to json : " + responseJson.toString() );
      Response.serverError().build();
    }
    return Response.ok( json ).build();
  }

  //TODO since it is singlton, get it only one time?
  private PentahoAsyncExecutor getExecutor() {
    return PentahoSystem.get( PentahoAsyncExecutor.class, PentahoAsyncExecutor.BEAN_NAME, null );
  }
}
