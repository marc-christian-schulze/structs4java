package org.structs4java.example.tests;

import org.junit.Test;
import org.structs4java.example.tests.defaultvalues.*;

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
}
