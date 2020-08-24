package org.structs4java.example.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import org.structs4java.example3.ListOfIntegers;
import org.structs4java.example3.NullTerminatedString;
import org.structs4java.example3.OptionalPart;
import org.structs4java.example3.OptionalPart2;
import org.structs4java.example4.NonGreedy;
import org.structs4java.example4.SelfSizedGreedy;
import org.structs4java.example4.SimpleGreedy;
import org.structs4java.bugs.SignessBug;
import org.structs4java.bugs.CountOfBug;
import org.structs4java.bugs.CountOfBug2;
import org.structs4java.bugs.FixedSizeByteBuffer;
import org.structs4java.example.test.DynamicString;
import org.structs4java.example.test.PaddedWithCustomByte;
import org.structs4java.example.test.FixSizedStringWithCustomFiller;


public class RegressionTest extends AbstractTest {

	@Test
	public void testSimpleStructure() throws IOException {
		SimpleStructure expected = createSimpleStruct();
		expected.write(buffer);
		buffer.flip();

		SimpleStructure actual = SimpleStructure.read(buffer);
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testCountOfBug() throws IOException {
		byte[] testData = new byte[]{ 2, 0, 1, 0, 2};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		CountOfBug bug = CountOfBug.read(buffer);
		assertEquals(2, bug.getNum());
		assertEquals(2, bug.getElements().size());
		assertEquals(1, bug.getElements().get(0).getValue());
		assertEquals(2, bug.getElements().get(1).getValue());
	}
	
	@Test
	public void testCountOfBug2() throws IOException {
		byte[] testData = new byte[]{ 2, 0, 1, 'A', 0, 0, 2, 'B', 0};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		CountOfBug2 bug = CountOfBug2.read(buffer);
		assertEquals(2, bug.getNum());
		assertEquals(2, bug.getElements().size());
		assertEquals(1, bug.getElements().get(0).getValue());
		assertEquals("A", bug.getElements().get(0).getName());
		assertEquals(2, bug.getElements().get(1).getValue());
		assertEquals("B", bug.getElements().get(1).getName());
	}

	@Test
	public void testEnum() throws IOException {
		SimpleEnum.SecondItem.write(buffer);
		buffer.flip();

		SimpleEnum actual = SimpleEnum.read(buffer);

		Assert.assertEquals(SimpleEnum.SecondItem, actual);
	}

	@Test
	public void testAdvancedStructure() throws IOException {
		AdvancedStructure expected = createAdvancedStruct();

		expected.write(buffer);
		buffer.flip();

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
		buffer.flip();

		AdvancedStructure actual = AdvancedStructure.read(buffer);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testLittleEndian() throws IOException {
		AdvancedStructure expected = createAdvancedStruct();

		buffer.order(ByteOrder.LITTLE_ENDIAN);
		expected.write(buffer);
		buffer.flip();

		AdvancedStructure actual = AdvancedStructure.read(buffer);
		Assert.assertEquals(expected, actual);
	}

	@Test(expected = IOException.class)
	public void testMixedEndian() throws IOException {
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		SimpleEnum.SecondItem.write(buffer);
		buffer.flip();
		buffer.order(ByteOrder.BIG_ENDIAN);

		SimpleEnum.read(buffer);
	}
	
	@Test
	public void testBString() throws IOException {
		BString expected = new BString();
		expected.setValue("A string of theoretically variable length!");
		
		expected.write(buffer);
		buffer.flip();

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
		buffer.flip();
		
		DynamicStruct actual = DynamicStruct.read(buffer);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testDynamicStructureWithoutContent() throws IOException {
		
		DynamicStruct expected = new DynamicStruct();
		
		expected.write(buffer);
		buffer.flip();
		
		DynamicStruct actual = DynamicStruct.read(buffer);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testNullTerminatedString() throws IOException {
		NullTerminatedString expected = new NullTerminatedString();
		expected.setValue("A string of theoretically variable length!");
		
		expected.write(buffer);
		buffer.flip();

		NullTerminatedString actual = NullTerminatedString.read(buffer);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testPartialReadOfNullTerminatedString() throws IOException {
		NullTerminatedString expected = new NullTerminatedString();
		expected.setValue("A string of theoretically variable length!");
		
		expected.write(buffer);
		buffer.flip();
		// truncate terminating zero
		buffer.limit(buffer.limit() - 1);

		NullTerminatedString actual = NullTerminatedString.read(buffer, true);
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testDynamicStructHavingPartialNestedObjectWithPartialContent() throws IOException {
		OptionalPart2 optionalFields = new OptionalPart2();
		optionalFields.setMandatory(13);
		
		DynamicStructHavingPartialNestedObject expected = new DynamicStructHavingPartialNestedObject();
		expected.setOptionalFields(optionalFields);
		
		expected.write(buffer);
		buffer.flip();
		
		DynamicStructHavingPartialNestedObject actual = DynamicStructHavingPartialNestedObject.read(buffer);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testListOfIntegers() throws IOException {
		ListOfIntegers expected = new ListOfIntegers();
		ArrayList<Long> list = new ArrayList<Long>();
		for(long i = 0; i < 10; ++i) {
			list.add(10 - i);
		}
		expected.setArray(list);
		
		expected.write(buffer);
		buffer.flip();
		
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
	
	@Test
	public void testNullTerminatedStringBug() throws IOException {
		byte[] testData = new byte[]{ 5, 0, 0, 0, 'T', 'e', 's', 't', 0};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		BString str = BString.read(buffer);
		
		Assert.assertEquals("Test", str.getValue());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		str.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}

	@Test
	public void testVarStringNotNullTerminated() throws IOException {
		byte[] testData = new byte[]{ 4, 0, 0, 0, 'T', 'e', 's', 't'};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		DynamicString str = DynamicString.read(buffer);
		
		Assert.assertEquals("Test", str.getValue());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		str.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}

	@Test
	public void testPaddingWithCustomByte() throws IOException {
		byte[] testData = new byte[]{ 
			1, (byte)0xFF, (byte)0xFF, (byte)0xFF,                      // uint8_t value1 padding(4, using = 0xFF);
			1, 0, (byte)0xFF, (byte)0xFF,                               // uint16_t value2 padding(4, using = 0xFF);
			1, 0, 0, 0, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, // uint32_t value3 padding(8, using = 0xFF);
			1, 0, 0, 0, 0, 0, 0, 0, (byte)0xFF, (byte)0xFF,             // uint64_t value4 padding(10, using = 0xFF);
			'T', 'e', 's', 't', (byte)0xFF,                             // char value5[4] padding(5, using = 0xFF);
			0, 0, (byte) 0x8c, (byte) 0xc1, (byte)0xFF,                 // float value6 padding(5, using = 0xFF);
			0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte)0xFF                // double value7 padding(9, using = 0xFF);
		};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		PaddedWithCustomByte struct = PaddedWithCustomByte.read(buffer);
		
		Assert.assertEquals(1, struct.getValue1());
		Assert.assertEquals(1, struct.getValue2());
		Assert.assertEquals(1, struct.getValue3());
		Assert.assertEquals(1, struct.getValue4());
		Assert.assertEquals("Test", struct.getValue5());
		Assert.assertEquals(-17.5f, struct.getValue6(), 0.01f);
		Assert.assertEquals(-2d, struct.getValue7(), 0.01f);
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		struct.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}

	@Test
	public void testFixSizedStringWithCustomFiller() throws IOException {
		byte[] testData = new byte[]{'T', 'e', 's', ' '};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		FixSizedStringWithCustomFiller str = FixSizedStringWithCustomFiller.read(buffer);
		
		Assert.assertEquals("Tes ", str.getValue());
		str.setValue("Tes"); // ensure we test the custom filler byte
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		str.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testStructWithInterfaces() throws IOException {
		byte[] testData = new byte[]{ 1, 0, 0, 0, 2, 0, 0, 0};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		BothInterfaces obj = BothInterfaces.read(buffer);
		
		Assert.assertEquals(1, obj.getA());
		Assert.assertEquals(2, obj.getB());
		
		InterfaceA a = (InterfaceA)obj;
		Assert.assertEquals(1, a.getA());
		
		InterfaceB b = (InterfaceB)obj;
		Assert.assertEquals(2, b.getB());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		obj.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testStructWithBitfieldsLE() throws IOException {
		byte[] testData = new byte[]{ (byte) 0x92, 7,5,0,3};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		StructWithBitfields obj = StructWithBitfields.read(buffer);
		
//		bitfield uint8_t {
//			uint8_t number  : 3;
//			boolean flag1   : 1;
//			boolean flag2   : 1;
//			int32_t number2 : 3;
//		}
		assertEquals(4, obj.getNumber());
		assertTrue(obj.getFlag1());
		assertFalse(obj.getFlag2());
		assertEquals(2, obj.getNumber2());
		
//		bitfield int32_t {
//			uint8_t highByte    : 8;
//			int16_t middleBytes : 16;
//			MyEnum  lowByte     : 8;
//		}
		assertEquals(3, obj.getHighByte());
		assertEquals(5, obj.getMiddleBytes());
		assertEquals(MyEnum.B, obj.getLowByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		obj.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	@Test
	public void testSignessBug() throws IOException {
		byte[] testData = new byte[]{ // 
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, //
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, //
				(byte) 0xFF, (byte) 0xFF, // 
				(byte) 0xFF, (byte) 0xFF, //
				(byte) 0xFF, // 
				(byte) 0xFF};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		SignessBug bug = SignessBug.read(buffer);
		
		assertEquals(-1L, bug.getSigned32());
		assertEquals(0xFFFFFFFFL, bug.getUnsigned32());
		assertEquals(-1L, bug.getSigned16());
		assertEquals(0xFFFFL, bug.getUnsigned16());
		assertEquals(-1L, bug.getSigned8());
		assertEquals(0xFFL, bug.getUnsigned8());
	}
	
	@Test
	public void testStructWithBitfieldsBE() throws IOException {
		byte[] testData = new byte[]{ (byte) 0x92, 3,0,5,7};
		ByteBuffer buffer = ByteBuffer.wrap(testData);
		buffer.order(ByteOrder.BIG_ENDIAN);
		StructWithBitfields obj = StructWithBitfields.read(buffer);
		
//		bitfield uint8_t {
//			uint8_t number  : 3;
//			boolean flag1   : 1;
//			boolean flag2   : 1;
//			int32_t number2 : 3;
//		}
		assertEquals(4, obj.getNumber());
		assertTrue(obj.getFlag1());
		assertFalse(obj.getFlag2());
		assertEquals(2, obj.getNumber2());
		
//		bitfield int32_t {
//			uint8_t highByte    : 8;
//			int16_t middleBytes : 16;
//			MyEnum  lowByte     : 8;
//		}
		assertEquals(3, obj.getHighByte());
		assertEquals(5, obj.getMiddleBytes());
		assertEquals(MyEnum.B, obj.getLowByte());
		
		ByteBuffer outBuffer = ByteBuffer.allocate(testData.length);
		outBuffer.order(buffer.order());
		obj.write(outBuffer);
		
		assertEqualBuffers(buffer, outBuffer);
	}
	
	
	
	@Test
	public void testCopyConstructorEmptyObject() {
		CopyConstructorCases obj = new CopyConstructorCases();
		assertEquals(obj, new CopyConstructorCases(obj));
	}
	
	@Test
	public void testCopyConstructorFilledObject() {
		CopyConstructorCases obj = createFilledObject();
		
		assertEquals(obj, new CopyConstructorCases(obj));
	}
	
	@Test
	public void testCloneMethodEmptyObject() {
		CopyConstructorCases obj = new CopyConstructorCases();
		assertEquals(obj, obj.clone());
	}
	
	@Test
	public void testCloneMethodFilledObject() {
		CopyConstructorCases obj = createFilledObject();
		assertEquals(obj, obj.clone());
	}
	
	@Test
	public void testFixedSizeByteBufferBug_WithSmallerBufferSize() throws IOException {
		ByteBuffer outBuffer = ByteBuffer.allocate((int) FixedSizeByteBuffer.getSizeOf());
		FixedSizeByteBuffer obj = new FixedSizeByteBuffer();
		obj.setFixed_size_buffer(ByteBuffer.wrap(new byte[]{'1', '2', '3'}));
		obj.setTest("ABCD");
		
		obj.write(outBuffer);
		
		assertEqualBuffers(ByteBuffer.wrap(new byte[]{ '1', '2', '3', 0, 0, 0, 0, 0, 'A', 'B', 'C', 'D'}), outBuffer);
	}
	
	@Test
	public void testFixedSizeByteBufferBug_WithLargerBufferSize() throws IOException {
		ByteBuffer outBuffer = ByteBuffer.allocate((int) FixedSizeByteBuffer.getSizeOf());
		FixedSizeByteBuffer obj = new FixedSizeByteBuffer();
		obj.setFixed_size_buffer(ByteBuffer.wrap(new byte[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '1'}));
		obj.setTest("ABCD");
		
		obj.write(outBuffer);
		
		assertEqualBuffers(ByteBuffer.wrap(new byte[]{ '1', '2', '3', '4', '5', '6', '7', '8', 'A', 'B', 'C', 'D'}), outBuffer);
	}

	@Test
	public void testFixedSizeByteBufferBug_WithExactBufferSize() throws IOException {
		ByteBuffer outBuffer = ByteBuffer.allocate((int) FixedSizeByteBuffer.getSizeOf());
		FixedSizeByteBuffer obj = new FixedSizeByteBuffer();
		obj.setFixed_size_buffer(ByteBuffer.wrap(new byte[]{'1','2','3','4','5','6','7','8'}));
		obj.setTest("ABCD");
		
		obj.write(outBuffer);
		
		assertEqualBuffers(ByteBuffer.wrap(new byte[]{ '1', '2', '3', '4', '5', '6', '7', '8', 'A', 'B', 'C', 'D'}), outBuffer);
	}

	private CopyConstructorCases createFilledObject() {
		CopyConstructorCases obj = new CopyConstructorCases();
		obj.setE(AnyEnum.B);
		obj.setF(5.4);
		obj.setD(3.14d);
		obj.setSigned16(7);
		obj.setSigned32(8);
		obj.setSigned64(9);
		obj.setUnsigned8(1);
		obj.setUnsigned16(2);
		obj.setUnsigned32(3);
		obj.setUnsigned64(4);
		obj.setDArray(new ArrayList<Double>());
		obj.getDArray().add(8d);
		obj.setEArray(new ArrayList<AnyEnum>());
		obj.getEArray().add(AnyEnum.A);
		obj.setFArray(new ArrayList<Double>());
		obj.getFArray().add(7d);
		obj.setSArray(new ArrayList<AnyStruct>());
		obj.getSArray().add(new AnyStruct());
		obj.setSigned16Array(new ArrayList<Long>());
		obj.getSigned16Array().add(1l);
		obj.setSigned32Array(new ArrayList<Long>());
		obj.getSigned32Array().add(5l);
		obj.setSigned64Array(new ArrayList<Long>());
		obj.getSigned64Array().add(6l);
		obj.setSigned8Array(ByteBuffer.allocate(15));
		obj.setStr("what");
		obj.setS(new AnyStruct());
		return obj;
	}
}
