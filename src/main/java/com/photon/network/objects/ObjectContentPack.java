package com.photon.network.objects;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.photon.network.sql.SQLInteraction.SQLCommandSerializer;

public record ObjectContentPack(double size, int connectionID, String name, boolean isLast, boolean dir, String sha1, byte[] fileContent) implements SQLCommandSerializer<ObjectContentPack> {
    @Override
    public ObjectContentPack objectify(ResultSet resultSet) throws SQLException {
        return new ObjectContentPack(
            resultSet.getDouble("size"),
            resultSet.getInt("connectionID"),
            resultSet.getString("name"),
            resultSet.getBoolean("isLast"),
            resultSet.getBoolean("dir"),
            resultSet.getString("sha1"),
            resultSet.getBytes("fileContent")
        );
    }
}
