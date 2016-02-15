package org.pentaho.reporting.platform.plugin.cache;

import java.io.Serializable;

public class IReportContent implements Serializable {

    public IReportContent(int pageCount, byte[] data) {
        this.pageCount = pageCount;
        this.data = data;
    }

    private final int pageCount;

    private final byte[] data;

    public int getPageCount() {
        return pageCount;
    }

    public byte[] getData() {
        return data;
    }
}
