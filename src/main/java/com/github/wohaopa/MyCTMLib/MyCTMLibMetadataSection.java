package com.github.wohaopa.MyCTMLib;

import net.minecraft.client.resources.data.IMetadataSection;

public class MyCTMLibMetadataSection implements IMetadataSection {

    private final String connection;

    public MyCTMLibMetadataSection(String connection) {
        this.connection = connection;
    }

    public String getConnection() {
        return connection;
    }
}
