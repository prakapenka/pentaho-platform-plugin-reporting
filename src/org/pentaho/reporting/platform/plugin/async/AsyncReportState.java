package org.pentaho.reporting.platform.plugin.async;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by dima.prokopenko@gmail.com on 2/12/2016.
 */
@XmlRootElement
public interface AsyncReportState extends Serializable {
  @XmlElement
  String getPath();
  @XmlElement
  UUID getUuid();
  @XmlElement
  AsyncExecutionStatus getStatus();
  @XmlElement
  int getProgress();
  @XmlElement
  String getMimeType();
}
