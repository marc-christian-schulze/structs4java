package org.structs4java.example.tests;

import org.junit.Test;
import org.structs4java.example.tests.defaultvalues.*;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DefaultValueTest {
    @Test
    public void testStructWithIntegerDefaultValue() {
        StructWithIntegerDefaultValue struct = new StructWithIntegerDefaultValue();
        assertEquals(1, struct.getInt8());
        assertEquals(2, struct.getInt16());
        assertEquals(3, struct.getInt32());
        assertEquals(4, struct.getInt64());
    }

    @Test
    public void testStructWithFloatDefaultValue() {
        StructWithFloatDefaultValue struct = new StructWithFloatDefaultValue();
        assertEquals(1.0, struct.getF(), 0.001);
        assertEquals(2.0, struct.getD(), 0.001);
    }

    @Test
    public void testStructWithStringDefaultValue() {
        StructWithStringDefaultValue struct = new StructWithStringDefaultValue();
        assertEquals("default", struct.getStr());
    }

    @Test
    public void testStructWithEnumDefaultValue() {
        StructWithEnum struct = new StructWithEnum();
        assertEquals(Int8Enum.B, struct.getInt8Enum());
    }

    @Test
    public void testStructWithArrayDefaultValue() {
        StructWithArrayDefaultValue struct = new StructWithArrayDefaultValue();
        assertBufferEquals(struct.getInt8(), 1, 0x20, 3);
        assertListEquals(struct.getInt16(), 4, 5, 6);
        assertListEquals(struct.getInt32(), 7, 8, 9);
        assertListEquals(struct.getInt64(), 0x10, 0x11, 0x12);
    }

    private void assertBufferEquals(ByteBuffer buffer, int... expected) {
        for(int value : expected) {
            assertEquals(value, buffer.get() & 0xFF);
        }
    }

    private void assertListEquals(List<Long> items, int... expected) {
        for(int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], items.get(i).longValue());
        }
    }
}
