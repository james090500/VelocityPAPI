package com.james090500.VelocityPAPI.shared;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class VPObject {

    @Getter private String messageKey;
    @Getter private UUID uuid;
    @Getter private String message;
    @Getter @Setter private String result;
    @Getter private long createdAt;

    /**
     * Creates a new object
     * @param messageKey The secret key
     * @param uuid The players uuid
     * @param message The message to convert
     */
    public VPObject(String messageKey, UUID uuid, String message) {
        this.messageKey = messageKey;
        this.uuid = uuid;
        this.message = message;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Writes pending conversions in the correct order to a byte array
     * @return The byte array
     */
    public byte[] writePending() {
        ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF(messageKey);
        buf.writeUTF(uuid.toString());
        buf.writeUTF(message);
        return buf.toByteArray();
    }

    /**
     * Reads a received pending conversion to a VPObject
     * @param data The data to read
     * @return Returns a VPObject
     */
    public static VPObject readPending(byte[] data) {
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        return new VPObject(
                in.readUTF(),
                UUID.fromString(in.readUTF()),
                in.readUTF()
        );
    }

    /**
     * Writes completed conversion in the correct order to a byte array
     * @return The byte array
     */
    public byte[] writeComplete() {
        ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF(messageKey);
        buf.writeUTF(result);
        return buf.toByteArray();
    }

    /**
     * Reads a received completed conversion to a VPObject
     * @param data The data to read
     * @return Returns a VPObject
     */
    public static VPObject readComplete(byte[] data) {
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        return new VPObject(
                in.readUTF(),
                UUID.fromString(in.readUTF()),
                in.readUTF()
        );
    }

    @Override
    public String toString() {
        return "VPObject{" +
                "messageKey='" + messageKey + '\'' +
                ", uuid=" + uuid +
                ", message='" + message + '\'' +
                ", result='" + result + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
