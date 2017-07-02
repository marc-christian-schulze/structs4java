package org.structs4java.example.tests;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.structs4java.bugs.endianess.Outer;
import org.structs4java.example1.SimpleEnum;
import org.structs4java.example1.SimpleStructure;
import org.structs4java.example2.AdvancedStructure;
import org.structs4java.example3.BString;
import org.structs4java.example3.DynamicStruct;
import org.structs4java.example3.DynamicStructHavingPartialNestedObject;
import org.structs4java.example3.NullTerminatedString;
import org.structs4java.example3.OptionalPart;
import org.structs4java.example3.OptionalPart2;
import org.structs4java.example3.ListOfIntegers;
import org.structs4java.example4.SimpleGreedy;
import org.structs4java.example4.NonGreedy;
import org.structs4java.example4.SelfSizedGreedy;


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
	
	@Test
	public void testListOfIntegers() throws IOException {
		ListOfIntegers expected = new ListOfIntegers();
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < 10; ++i) {
			list.add(10 - i);
		}
		expected.setArray(list);
		
		expected.write(buffer);
		buffer.position(0);
		
		ListOfIntegers actual = ListOfIntegers.read(buffer);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testReadSimpleGreedy() throws IOException {
		byte[] testData = new byte[]{ 6, 1, 2, 3, 4, 5};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		SimpleGreedy greedy = SimpleGreedy.read(buffer);
		
		Assert.assertEquals(6, greedy.getFirst());
		Assert.assertEquals(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}), greedy.getRest());
	}
	
	@Test
	public void testReadGreedyWithinNonGreedy() throws IOException {
		byte[] testData = new byte[]{ 3, 1, 2, 3, 4, 5};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		NonGreedy nonGreedy = NonGreedy.read(buffer);
		
		Assert.assertEquals(3, nonGreedy.getVeryFirst());
		Assert.assertEquals(1, nonGreedy.getGreedy().getFirst());
		Assert.assertEquals(ByteBuffer.wrap(new byte[]{2, 3}), nonGreedy.getGreedy().getRest());
	}
	
	@Test
	public void testReadSelfSizedGreedy() throws IOException {
		byte[] testData = new byte[]{ 3, 1, 2, 3, 4, 5};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		SelfSizedGreedy selfSizedGreedy = SelfSizedGreedy.read(buffer);
		
		Assert.assertEquals(ByteBuffer.wrap(new byte[]{1, 2}), selfSizedGreedy.getRest());
	}
	
	@Test
	public void testEndianessBugWhenSlicingLE() throws IOException {
		byte[] testData = new byte[]{ 2, 1, 0, 1, 0};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		Outer outer = Outer.read(buffer);
		
		Assert.assertEquals(2, outer.getLength());
		Assert.assertEquals(1, outer.getWord());
		Assert.assertEquals(1, outer.getInner().getWord());
	}
	
	@Test
	public void testEndianessBugWhenSlicingBE() throws IOException {
		byte[] testData = new byte[]{ 2, 0, 1, 0, 1};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.BIG_ENDIAN);
		Outer outer = Outer.read(buffer);
		
		Assert.assertEquals(2, outer.getLength());
		Assert.assertEquals(1, outer.getWord());
		Assert.assertEquals(1, outer.getInner().getWord());
	}
	
	@Test
	public void testPaddedByteMember() throws IOException {
		byte[] testData = new byte[]{ 2, 0, 0, 0, 1};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		PaddedByteMember struct = PaddedByteMember.read(buffer);
		
		Assert.assertEquals(2, struct.getPaddedByte());
		Assert.assertEquals(1, struct.getFollowingByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testPaddedWordMemberLE() throws IOException {
		byte[] testData = new byte[]{ 2, 0, 0, 0, 1};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		PaddedWordMember struct = PaddedWordMember.read(buffer);
		
		Assert.assertEquals(2, struct.getPaddedWord());
		Assert.assertEquals(1, struct.getFollowingByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testPaddedWordMemberBE() throws IOException {
		byte[] testData = new byte[]{ 0, 2, 0, 0, 1};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.BIG_ENDIAN);
		PaddedWordMember struct = PaddedWordMember.read(buffer);
		
		Assert.assertEquals(2, struct.getPaddedWord());
		Assert.assertEquals(1, struct.getFollowingByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testPaddedDWordMemberLE() throws IOException {
		byte[] testData = new byte[]{ 2, 0, 0, 0, 1};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		PaddedDWordMember struct = PaddedDWordMember.read(buffer);
		
		Assert.assertEquals(2, struct.getPaddedDWord());
		Assert.assertEquals(1, struct.getFollowingByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testPaddedDWordMemberBE() throws IOException {
		byte[] testData = new byte[]{ 0, 0, 0, 2, 1};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.BIG_ENDIAN);
		PaddedDWordMember struct = PaddedDWordMember.read(buffer);
		
		Assert.assertEquals(2, struct.getPaddedDWord());
		Assert.assertEquals(1, struct.getFollowingByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);;
	}
	
	@Test
	public void testPaddedDWordMemberAt8BytesLE() throws IOException {
		byte[] testData = new byte[]{ 2, 0, 0, 0, 0, 0, 0, 0, 1};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		PaddedDWordMemberAt8Bytes struct = PaddedDWordMemberAt8Bytes.read(buffer);
		
		Assert.assertEquals(2, struct.getPaddedDWord());
		Assert.assertEquals(1, struct.getFollowingByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testPaddedDWordMemberAt8BytesBE() throws IOException {
		byte[] testData = new byte[]{ 0, 0, 0, 2, 0, 0, 0, 0, 1};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.BIG_ENDIAN);
		PaddedDWordMemberAt8Bytes struct = PaddedDWordMemberAt8Bytes.read(buffer);
		
		Assert.assertEquals(2, struct.getPaddedDWord());
		Assert.assertEquals(1, struct.getFollowingByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testPaddedFloatMemberLE() throws IOException {
		byte[] testData = new byte[]{ 0, 0, (byte) 0x8c, (byte) 0xc1, 0, 0, 0, 0, 1};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		PaddedFloatMember struct = PaddedFloatMember.read(buffer);
		
		Assert.assertEquals(-17.5f, struct.getPaddedFloat(), 0.0f);
		Assert.assertEquals(1, struct.getFollowingByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testPaddedFloatMemberBE() throws IOException {
		byte[] testData = new byte[]{ (byte) 0xc1, (byte) 0x8c, 0, 0, 0, 0, 0, 0, 1};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.BIG_ENDIAN);
		PaddedFloatMember struct = PaddedFloatMember.read(buffer);
		
		Assert.assertEquals(-17.5f, struct.getPaddedFloat(), 0.0f);
		Assert.assertEquals(1, struct.getFollowingByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testPaddedDynamicByteArray() throws IOException {
		byte[] testData = new byte[]{ 2, 3, 4, 0, 0, 1};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		PaddedDynamicByteArray struct = PaddedDynamicByteArray.read(buffer);
		
		Assert.assertEquals(2, struct.getArray().limit());
		Assert.assertEquals(3, struct.getArray().get());
		Assert.assertEquals(4, struct.getArray().get());
		Assert.assertEquals(1, struct.getFollowingByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testNestedGreedyStructs() throws IOException {
		byte[] testData = new byte[]{ 1, 0, 3, 0, 1, 2, 3, 4};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		OuterGreedy struct = OuterGreedy.read(buffer);
		
		Assert.assertEquals(4, struct.getContent().get());
		Assert.assertEquals(1, struct.getInner().getContent().get());
		Assert.assertEquals(2, struct.getInner().getContent().get());
		Assert.assertEquals(3, struct.getInner().getContent().get());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testMultipleGreedy() throws IOException {
		byte[] testData = new byte[]{ 2, 0, 1, 3, 2, 3, 4};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		MultipleGreedy struct = MultipleGreedy.read(buffer);
		
		Assert.assertEquals(0, struct.getFirst().get());
		Assert.assertEquals(1, struct.getFirst().get());
		Assert.assertEquals(2, struct.getSecond().get());
		Assert.assertEquals(3, struct.getSecond().get());
		Assert.assertEquals(4, struct.getSecond().get());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
}
