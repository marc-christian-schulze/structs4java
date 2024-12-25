/*
 * generated by Xtext 2.10.0
 */
package org.structs4java.generator

import org.structs4java.structs4JavaDsl.EnumDeclaration
import org.structs4java.structs4JavaDsl.StructsFile
import org.structs4java.structs4JavaDsl.Item

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class EnumGenerator {

	def compile(StructsFile structsFile, EnumDeclaration enumDecl) '''
		«packageDeclaration(structsFile)»
		
		«printComments(enumDecl)»
		public enum «enumDecl.name» {
			
			«items(enumDecl)»
			
			«reader(enumDecl)»
			«writer(enumDecl)»
			
			public long getValue() {
				return value;
			}
			
			private «enumDecl.name»(long value) {
				this.value = value;
			}
			
			private long value;
		}
	'''
	
	def printComments(EnumDeclaration typeDecl) '''
	/**
	«FOR comment : typeDecl.comments»
	* «comment.substring(2).trim()»
	«ENDFOR»
	*/
	'''
	
	def printComments(Item item) '''
	/**
	«FOR comment : item.comments»
	* «comment.substring(2).trim()»
	«ENDFOR»
	*/
	'''

	def items(EnumDeclaration enumDecl) '''
		«FOR i : enumDecl.items SEPARATOR "," AFTER ";"»
			«printComments(i)»
			«i.name»(«i.value»L)
		«ENDFOR»
	'''

	def reader(EnumDeclaration enumDecl) '''
		public static «enumDecl.name» read(java.nio.ByteBuffer buf, boolean partialRead) throws java.io.IOException {
			return read(buf);
		}
		
		public static «enumDecl.name» read(java.nio.ByteBuffer buf) throws java.io.IOException {
			«read(enumDecl)»
			try {
				return fromValue(value);
			} catch(IllegalArgumentException e) {
				throw new java.io.IOException(e);
			}
		}
		
		public static «enumDecl.name» fromValue(long value) throws IllegalArgumentException {
			«FOR f : enumDecl.items»
			if(value == «f.value»L) {
				return «f.name»;
			}
			«ENDFOR»
			throw new IllegalArgumentException(String.format("Unknown enum value: 0x%X", value));
		}
	'''

	def read(EnumDeclaration enumDecl) {
		switch (enumDecl.typename) {
			case "int8_t": '''long value = buf.get();'''
			case "uint8_t": '''long value = buf.get() & 0xFFL;'''
			case "int16_t": '''long value = buf.getShort();'''
			case "uint16_t": '''long value = buf.getShort() & 0xFFFFL;'''
			case "int32_t": '''long value = buf.getInt();'''
			case "uint32_t": '''long value = buf.getInt() & 0xFFFFFFFFL;'''
			case "int64_t": '''long value = buf.getLong();'''
			case "uint64_t": '''long value = buf.getLong();'''
		}
	}

	def writer(EnumDeclaration enumDecl) '''
		public void write(java.nio.ByteBuffer buf) throws java.io.IOException {
			«write(enumDecl)»
		}
	'''

	def write(EnumDeclaration enumDecl) {
		switch (enumDecl.typename) {
			case "int8_t": '''buf.put((byte)(this.value));'''
			case "uint8_t": '''buf.put((byte)(this.value & 0xFFL));'''
			case "int16_t": '''buf.putShort((short)(this.value));'''
			case "uint16_t": '''buf.putShort((short)(this.value & 0xFFFFL));'''
			case "int32_t": '''buf.putInt((int)(this.value));'''
			case "uint32_t": '''buf.putInt((int)(this.value & 0xFFFFFFFFL));'''
			case "int64_t": '''buf.putLong(this.value);'''
			case "uint64_t": '''buf.putLong(this.value);'''
		}
	}

	def packageDeclaration(StructsFile structsFile) '''
		«IF structsFile.name !== null && !structsFile.name.empty»
			package «structsFile.name»;
		«ENDIF»
	'''
}
