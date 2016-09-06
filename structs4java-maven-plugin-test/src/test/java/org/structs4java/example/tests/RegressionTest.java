package org.structs4java.example.tests;

import java.io.IOException;
import java.nio.ByteOrder;

import org.junit.Assert;
import org.junit.Test;
import org.structs4java.example1.SimpleEnum;
import org.structs4java.example1.SimpleStructure;
import org.structs4java.example2.AdvancedStructure;
import org.structs4java.example3.BString;
import org.structs4java.example3.DynamicStruct;
import org.structs4java.example3.OptionalPart;
import org.structs4java.example3.NullTerminatedString;
import org.structs4java.example3.OptionalPart2;
import org.structs4java.example3.DynamicStructHavingPartialNestedObject;

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
	public void testFixedSizeStructure() throws IOException {
		AdvancedStructure struct = createAdvancedStruct();
		struct.write(buffer);

		Assert.assertEquals(AdvancedStructure.getSizeOf(), buffer.position());
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
	
	@Test
	public void testBString() throws IOException {
		BString expected = new BString();
		expected.setValue("A string of theoretically variable length!");
		
		expected.write(buffer);
		buffer.position(0);

		BString actual = BString.read(buffer);
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testDynamicStructureWithContent() throws IOException {
		OptionalPart optionalFields = new OptionalPart();
		optionalFields.setX(45);
		optionalFields.setY(13);
		optionalFields.setZ(78);
		
		DynamicStruct expected = new DynamicStruct();
		expected.setOptionalWhatever(12345);
		expected.setOptionalFields(optionalFields);
		
		expected.write(buffer);
		buffer.position(0);
		
		DynamicStruct actual = DynamicStruct.read(buffer);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testDynamicStructureWithoutContent() throws IOException {
		
		DynamicStruct expected = new DynamicStruct();
		
		expected.write(buffer);
		buffer.position(0);
		
		DynamicStruct actual = DynamicStruct.read(buffer);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testNullTerminatedString() throws IOException {
		NullTerminatedString expected = new NullTerminatedString();
		expected.setValue("A string of theoretically variable length!");
		
		expected.write(buffer);
		buffer.position(0);

		NullTerminatedString actual = NullTerminatedString.read(buffer);
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testDynamicStructHavingPartialNestedObjectWithPartialContent() throws IOException {
		OptionalPart2 optionalFields = new OptionalPart2();
		optionalFields.setMandatory(13);
		
		DynamicStructHavingPartialNestedObject expected = new DynamicStructHavingPartialNestedObject();
		expected.setOptionalFields(optionalFields);
		
		expected.write(buffer);
		buffer.position(0);
		
		DynamicStructHavingPartialNestedObject actual = DynamicStructHavingPartialNestedObject.read(buffer);
		
		Assert.assertEquals(expected, actual);
	}
}
