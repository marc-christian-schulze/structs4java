package org.structs4java.example.tests;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.Before;
import org.structs4java.example1.SimpleEnum;
import org.structs4java.example1.SimpleStructure;
import org.structs4java.example2.AdvancedStructure;

public class AbstractTest {

	protected ByteBuffer buffer;

	@Before
	public void before() {
		buffer = ByteBuffer.allocate(4096);
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
}
