package org.structs4java.example.tests;

import java.io.IOException;
import java.nio.ByteOrder;

import org.junit.Assert;
import org.junit.Test;
import org.structs4java.example1.SimpleEnum;
import org.structs4java.example1.SimpleStructure;
import org.structs4java.example2.AdvancedStructure;

public class RegressionTest extends AbstractTest {

	@Test
	public void testSimpleStructure() throws IOException {
		SimpleStructure expected = createSimpleStruct();
		expected.write(buffer);
		buffer.position(0);

		SimpleStructure actual = SimpleStructure.read(buffer);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testEnum() throws IOException {
		SimpleEnum.SecondItem.write(buffer);
		buffer.position(0);

		SimpleEnum actual = SimpleEnum.read(buffer);

		Assert.assertEquals(SimpleEnum.SecondItem, actual);
	}

	@Test
	public void testAdvancedStructure() throws IOException {
		AdvancedStructure expected = createAdvancedStruct();

		expected.write(buffer);
		buffer.position(0);

		AdvancedStructure actual = AdvancedStructure.read(buffer);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testHashCode() throws IOException {
		AdvancedStructure a1 = createAdvancedStruct();
		AdvancedStructure a2 = createAdvancedStruct();

		Assert.assertEquals(a1.hashCode(), a2.hashCode());
	}

	@Test
	public void testToString() throws IOException {
		AdvancedStructure a1 = createAdvancedStruct();
		AdvancedStructure a2 = createAdvancedStruct();

		Assert.assertEquals(a1.toString(), a2.toString());
	}

	@Test
	public void testBigEndian() throws IOException {
		AdvancedStructure expected = createAdvancedStruct();

		buffer.order(ByteOrder.BIG_ENDIAN);
		expected.write(buffer);
		buffer.position(0);

		AdvancedStructure actual = AdvancedStructure.read(buffer);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testLittleEndian() throws IOException {
		AdvancedStructure expected = createAdvancedStruct();

		buffer.order(ByteOrder.LITTLE_ENDIAN);
		expected.write(buffer);
		buffer.position(0);

		AdvancedStructure actual = AdvancedStructure.read(buffer);
		Assert.assertEquals(expected, actual);
	}

	@Test(expected = IOException.class)
	public void testMixedEndian() throws IOException {
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		SimpleEnum.SecondItem.write(buffer);
		buffer.position(0);
		buffer.order(ByteOrder.BIG_ENDIAN);

		SimpleEnum.read(buffer);
	}
}
