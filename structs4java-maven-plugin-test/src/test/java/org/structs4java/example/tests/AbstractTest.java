package org.structs4java.example.tests;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import org.structs4java.example1.SimpleEnum;
import org.structs4java.example1.SimpleStructure;
import org.structs4java.example2.AdvancedStructure;

public class AbstractTest {

	private static final int BUFFER_SIZE = 256;
	protected ByteBuffer buffer;

	@Before
	public void before() {
		buffer = ByteBuffer.allocate(BUFFER_SIZE);
		for(int i = 0; i < BUFFER_SIZE; ++i) {
			buffer.put((byte)0xFF);
		}
		buffer.position(0);
		
		System.out.println("------------------------- NEW TESTCASE ------------------------------");
		System.out.println("Buffer before:");
		dumpBuffer();
	}
	
	@After
	public void after() {
		System.out.println("Buffer after:");
		dumpBuffer();
	}
	
	public void dumpBuffer() {
		dumpBuffer(buffer);
	}
	
	private void dumpBuffer(ByteBuffer buf) {
		for(int i = 0; i < buf.limit(); ++i) {
			System.out.print(String.format("%02X ", buf.get(i)));
			if(i % 32 == 31) {
				System.out.println();
			}
		}
		System.out.println();
	}
	
	public SimpleStructure createSimpleStruct() {
		SimpleStructure struct = new SimpleStructure();
		struct.setWord1(-478);
		struct.setWord2(0x6699);
		struct.setDword3(0x11223344);
		struct.setEnumValue(SimpleEnum.SecondItem);
		struct.setSomeString("foobar");
		return struct;
	}
	
	public AdvancedStructure createAdvancedStruct() {
		ArrayList<SimpleStructure> structs = new ArrayList<SimpleStructure>();
		for(int i = 0; i < 5; ++i) {
			structs.add(createSimpleStruct());
		}
		AdvancedStructure expected = new AdvancedStructure();
		expected.setSomeFloat_(1.234f);
		expected.setAnArray(structs);
		return expected;
	}
	
	protected void assertEqualBuffers(ByteBuffer a, ByteBuffer b) {
		a.position(0);
		b.position(0);
		if(!a.equals(b)) {
			System.out.println("Expected:");
			dumpBuffer(a);
			System.out.println("Actual:");
			dumpBuffer(b);
			Assert.fail("ByteBuffers are not equal!");
		}
	}
}
