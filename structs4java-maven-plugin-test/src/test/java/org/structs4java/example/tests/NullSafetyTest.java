package org.structs4java.example.tests;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.structs4java.example.tests.nullsafety.StructWithByteBuffer;
import org.structs4java.example.tests.nullsafety.StructWithInt32Array;
import org.structs4java.example.tests.nullsafety.StructWithStructArray;

public class NullSafetyTest {

    @Test
    public void testWritingDefaultStructWithByteBuffer() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        StructWithByteBuffer struct = new StructWithByteBuffer();
        struct.write(buffer);
    }

    @Test
    public void testWritingDefaultStructWithInt32Array() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(40);
        StructWithInt32Array struct = new StructWithInt32Array();
        struct.write(buffer);
    }

    @Test
    public void testWritingDefaultStructWithStructArray() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(400);
        StructWithStructArray struct = new StructWithStructArray();
        struct.write(buffer);
    }

    @Test
    public void testDefaultStructWithInt32ArrayReturnsNotNull() {
        StructWithInt32Array struct = new StructWithInt32Array();
        assertNotNull(struct.getArray());
    }

    @Test
    public void testDefaultStructWithStructArrayReturnsNotNull() {
        StructWithStructArray struct = new StructWithStructArray();
        assertNotNull(struct.getArray());
    }

    @Test
    public void testDefaultStructWithByteBufferReturnsNotNull() {
        StructWithByteBuffer struct = new StructWithByteBuffer();
        assertNotNull(struct.getBuffer());
    }

}
