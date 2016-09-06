# Structs4Java
This project brings structs known from C/C++ to the Java language to read/write plain memory. Java code is generated using a XText-based compiler that takes structures definitions (similar to C/C++ struct definitions) as source. The compiler generates for each _struct_ and _enum_ declaration a corresponding class or enum in Java that provides read and write methods that take an _java.nio.ByteBuffer_ as input. The generated classes are no wrapper but plain POJOs (that's the reason why there is no union support). If you're looking for a library that wraps native memory and applies changes to the Java classes immediately to the underlying memory have a look at the Javolution project.

## Example
Let's try to read the header information of an ELF executable.
The header structures for the 64bit ELF file header can be described like this:
```
struct Elf64_Ehdr {
  // ELF identification
  uint8_t e_ident[16];
  // Object file type
  uint16_t e_type; 
  // Machine type
  uint16_t e_machine; 
  // Object file version
  uint32_t e_version;
  // Entry point address 
  uint64_t e_entry;
  // Program header offset 
  uint64_t e_phoff; 
  // Section header offset
  uint64_t e_shoff; 
  // Processor-specific flags
  uint32_t e_flags; 
  // ELF header size
  uint16_t e_ehsize; 
  // Size of program header entry
  uint16_t e_phentsize;
  // Number of program header entries
  uint16_t e_phnum; 
  // Size of section header entry
  uint16_t e_shentsize;
  // Number of section header entries 
  uint16_t e_shnum; 
  // Section name string table index
  uint16_t e_shstrndx; 
}
```
Passing this struct delcaration to the Structs4Java Code Generator will lead to a _Elf64_Ehdr_ class that provides a static _read_ and _write_ method.
Using the _read_ meathod you can parse the file header like this:
```
RandomAccessFile elfFile = new RandomAccessFile("path/to/executable", "r");
MappedByteBuffer buffer = elfFile.getChannel().map(MapMode.READ_ONLY, 0, Elf64_Ehdr.getSizeOf());
buffer.order(ByteOrder.LITTLE_ENDIAN);

Elf64_Ehdr elfHeader = Elf64_Ehdr.read(buffer);
...
```

## Maven Plugin
You can run the Structs4Java code generator (by default in the generate-source phase) like this:
```
<plugin>
  <groupId>org.structs4java</groupId>
  <artifactId>structs4java-maven-plugin</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <executions>
    <execution>
      <id>compile-structs</id>
      <goals>
      	<goal>compile</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

## Features
* Reading / Writing C/C++ Structs from/to Java NIO ByteBuffers
* No additional dependencies
* Support for 
  * structs, 
  * enums (8, 16 and 32 bit), 
  * fixed-size primitives incl. floating point numbers,
    * uint8_t
    * int8_t
    * uint16_t
    * int16_t
    * uint32_t
    * int32_t
    * uint64_t
    * int64_t
    * float
    * double
  * strings incl. different encodings, 
  * arrays and 
  * nested elements

## Unsupported
* Unions
* Bit-Fields
* Pointer (and object-graphs)
* 64bit enums
* Memory Alignment

## Comparison to Javolution
[http://javolution.org/]  
Structs4Java is focussed solely on plain C/C++ struct reading / writing whereas Javolution brings complete JNI interoperability tooling. Therefore Structs4Java does not have any additional dependency despite the Java Runtime Environment (JRE) classes. And finally, it's less verbose...

Javolution (example taken from official documentation):
```
 public enum Gender { MALE, FEMALE };
 public static class Date extends Struct {
     public final Unsigned16 year = new Unsigned16();
     public final Unsigned8 month = new Unsigned8();
     public final Unsigned8 day   = new Unsigned8();
 }
 public static class Student extends Struct {
     public final Enum32<Gender>       gender = new Enum32<Gender>(Gender.values());
     public final UTF8String           name   = new UTF8String(64);
     public final Date                 birth  = inner(new Date());
     public final Float32[]            grades = array(new Float32[10]);
     public final Reference32<Student> next   =  new Reference32<Student>();
 }
```
Structs4Java:
```
enum Gender : uint32_t {
  MALE = 0, 
  FEMALE = 1
}

struct Date {
  uint16_t year;
  uint8_t  month;
  uint8_t  day;
}

struct Student {
  Gender  gender;
  char    name[64];
  Date    birth;
  float   grades[10];
  // Pointers are not supported by Structs4Java
  // Student*    next;
}
```

# Tutorial

## Structs
Structures are declared using the struct keyword:
```
struct Coordinate {
  int32_t x;
  int32_t y;
  int32_t z;
}
```

## Packages
You can group certain structures into a package.
```
package com.structs4java.pkg;

... your struct definitions
```
A document (*.struct) can contain only a single package declaration and if present it must be the first statement before any type declarations. 

## Imports
Once you've defined structures across multiple documents (*.struct) and packages it becomes handy to import a fully qualified name.
Pkg1.structs
```
package com.structs4java.example.pkg1;

struct Address {
  ...
}
```
Pkg2.structs
```
package com.structs4java.example.pkg2;

import com.structs4java.example.pkg1.Address;

struct Person {
  Address address;
  ...
}
```

## Enums
Enumerations are declared using the enum keyword:
```
enum Colors : uint8_t {
  RED = 0xCAFE,
  BLUE = 123,
  GREEN = 42
}
```
Each enum must provide a base type specified after the colon that defines the size of an enum value in serialized form.

## Strings
Fixed strings can be specified as an array of chars:
```
struct Person {
  char name[20];
}
```
By default UTF-8 is choosen as encoding but can be specified explicitily using the encoding attribute
```
struct Person {
  // Windows wide-string
  char name[20] encoding("UTF16-LE");
}
```
Null-terminated strings can be specified as an array of char without a dimension:
```
struct NullTerminatedString {
  char value[];
}
```
For null-terminated strings the number of terminating zeros is determined according to the string encoding so that for example for US-ASCII a single zero indicates the end of the string while 2 zeros are necessary for an UTF16 encoded string.
Variable-length but not null-terminated strings can be represented like the following:
```
struct DynamicString {
  uint32_t length sizeof(value);
  char     value[];
}
```

## SizeOf() Attribute
The sizeof attribute defines the size of a struct member or the entire struct in bytes.

For example, useful for header with optional fields at the end:
```
struct LegacyFileHeader {
  uint32_t headerLength sizeof(this);
  ... (optional) header fields
} 
```

## CountOf() Attribute
The countof attribute defines the number of elments of an array.
```
struct Entry {
  ...
}

struct Directory {
  uint16_t numberOfEntries countof(entries);
  Entry entries[];
}
```


