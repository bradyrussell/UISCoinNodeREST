package com.bradyrussell.uiscoin.data;

public class ExportedKeypairResponse {
    private String exportedKeypair;

    public ExportedKeypairResponse(String exportedKeypair) {
        this.exportedKeypair = exportedKeypair;
    }

    public String getExportedKeypair() {
        return exportedKeypair;
    }

    public void setExportedKeypair(String exportedKeypair) {
        this.exportedKeypair = exportedKeypair;
    }
}
