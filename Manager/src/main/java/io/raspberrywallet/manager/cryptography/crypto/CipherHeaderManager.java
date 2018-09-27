package io.raspberrywallet.manager.cryptography.crypto;

import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class is needed for reading/writing header to stream with encrypted data.
 * Header contains details about used key, key length, block mode etc. and more
 * data can be added to it, since it's serialized/deserialized on write/read to file.
 *
 * How this works:
 * The header is built with:
 * first byte   -> 1 or 0 value, 1 = big endian, 0 = little endian
 * next 4 bytes -> int value, which is the total size of header
 * other data   -> serialized object, which is real header
 *
 * You can use any object as header and this class is going to handle read/write operations.
 */

class CipherHeaderManager {
    
    // in bytes
    private final static int HEADER_METADATA_SIZE = 5;
    private final static int INT_SIZE = 4;
    
    static <E extends Serializable> void writeCipherData(E cipherData, OutputStream outputStream) throws IOException {
        byte[] cipherDataSerialized = SerializationUtils.serialize(cipherData);
        byte[] serializedDataLength = convertIntToByteArray(cipherDataSerialized.length);
        
        outputStream.write(getEndianByte());
        outputStream.write(serializedDataLength);
        outputStream.write(cipherDataSerialized);
    }
    
    static <E extends Serializable> E readCipherData(InputStream inputStream) throws IOException {
        int cipherDataLength = getHeaderSize(inputStream);
        
        byte[] cipherDataBytes = new byte[cipherDataLength];
        inputStream.read(cipherDataBytes, 0, cipherDataLength);
        
        return (E)SerializationUtils.deserialize(cipherDataBytes);
    }
    
    static int getTotalHeaderSize(InputStream inputStream) throws IOException {
        byte isBigEndian = (byte) inputStream.read();
        byte[] bytesWithCipherDataLength = new byte[INT_SIZE];
        inputStream.read(bytesWithCipherDataLength, 0, INT_SIZE);
        int cipherDataLength = getCipherDataLength(bytesWithCipherDataLength, isBigEndian);
        return cipherDataLength + HEADER_METADATA_SIZE;
    }
    
    private static int getHeaderSize(InputStream inputStream) throws IOException {
        return getTotalHeaderSize(inputStream) - HEADER_METADATA_SIZE;
    }
    
    private static int getCipherDataLength(byte[] bytesWithCipherDataLength, byte isBigEndian) {
        return isBigEndian == 1 ? convertByteArrayToInt(bytesWithCipherDataLength, true)
                : convertByteArrayToInt(bytesWithCipherDataLength, false);
    }
    
    private static byte[] convertIntToByteArray(int integer) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(INT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.put(ByteBuffer.allocate(INT_SIZE).order(ByteOrder.nativeOrder()).putInt(integer).array());
        return byteBuffer.array();
    }
    
    private static int convertByteArrayToInt(byte[] byteArray, boolean isBigEndian) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        if (isBigEndian)
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
        else
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        return byteBuffer.getInt();
    }
    
    private static byte getEndianByte() {
        if (isBigEndian())
            return 1;
        else
            return 0;
    }
    
    private static boolean isBigEndian() {
        return ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);
    }
    
}
