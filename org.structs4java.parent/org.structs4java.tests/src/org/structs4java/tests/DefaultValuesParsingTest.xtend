package org.structs4java.tests

import com.google.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith
import org.structs4java.structs4JavaDsl.IntegerMember
import org.structs4java.structs4JavaDsl.FloatMember

import static org.junit.Assert.assertEquals

@RunWith(XtextRunner)
@InjectWith(Structs4JavaDslInjectorProvider)
class DefaultValuesParsingTest {

	@Inject
	ParseHelper<org.structs4java.structs4JavaDsl.StructsFile> parseHelper

	@Test
	def void structWithValidPrimitivesWithDefaultValues() {
		val pkg = parseHelper.parse('''
			struct S {
			    int8_t   a = 1;
			    uint8_t  b = 2;
			    int16_t  c = 3;
			    uint16_t d = 4;
			    int32_t  e = 5;
			    uint32_t f = 6;
			    int64_t  g = 7;
			    uint64_t h = 8;
			    float    i = 1.0;
			    double   j = 2.0;
			}
		''')
		val struct = pkg.structs.get(0)

		assertEquals(1L, (struct.members.get(0) as IntegerMember).defaultValue)
		assertEquals(2L, (struct.members.get(1) as IntegerMember).defaultValue)
		assertEquals(3L, (struct.members.get(2) as IntegerMember).defaultValue)
		assertEquals(4L, (struct.members.get(3) as IntegerMember).defaultValue)
		assertEquals(5L, (struct.members.get(4) as IntegerMember).defaultValue)
		assertEquals(6L, (struct.members.get(5) as IntegerMember).defaultValue)
		assertEquals(7L, (struct.members.get(6) as IntegerMember).defaultValue)
		assertEquals(8L, (struct.members.get(7) as IntegerMember).defaultValue)
		assertEquals(1.0f, (struct.members.get(8) as FloatMember).defaultValue, 0.001f)
		assertEquals(2.0f, (struct.members.get(9) as FloatMember).defaultValue, 0.001f)
	}

}
