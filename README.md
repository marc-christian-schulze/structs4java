# Structs4Java
[![Build Status](https://travis-ci.org/marc-christian-schulze/structs4java.svg?branch=master)](https://travis-ci.org/marc-christian-schulze/structs4java)
This project brings structs known from C/C++ to the Java language to read/write plain memory. Java code is generated using a XText-based compiler that takes structures definitions (similar to C/C++ struct definitions) as source. The compiler generates for each _struct_ and _enum_ declaration a corresponding class or enum in Java that provides read and write methods that take an _java.nio.ByteBuffer_ as input. The generated classes are no wrapper but plain POJOs (that's the reason why there is no union support). If you're looking for a library that wraps native memory and applies changes to the Java classes immediately to the underlying memory have a look at the Javolution project.

## Getting Started
Since we don't yet publish our artifacts to maven central you need to build it by your own.  
To build everything in one step simply do
```Shell
$ git clone https://github.com/marc-christian-schulze/structs4java.git
$ cd structs4java
$ mvn clean install
```
This builds the Structs4Java Code Generator, Eclipse Plugin and Maven Plugin.

## Using the Structs4Java Maven Plugin
The Structs4Java Maven Plugin compiles any _*.structs_ files below the _src/main/structs_ directory to Java code. To enable the compiler in your maven build add the following plugin description:
```Maven
<plugin>
  <groupId>com.github.marc-christian-schulze.structs4java</groupId>
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

## Describe your Struct
Structs4Java is based on a struct description language that is very close to the original syntax of C/C++.
```C++
package com.mycompany.projectx;

struct FileHeader {
	uint8_t     magic[4];
	uint16_t    numberSections countof(sections);
	FileSection sections[];
}

struct FileSection {
	char     sectionName[32];
	uint32_t sectionLength sizeof(sectionContent);
	uint8_t  sectionContent[];
}
```

## Read and Write your Struct
The compiler generates for each struct a regular Java class providing a read and write method that can be used together with the Java NIO API like this
```Java
// open the file
RandomAccessFile file = new RandomAccessFile("path/to/file", "rw");
// create a memory mapping for efficient access
MappedByteBuffer buffer = file.getChannel().map(MapMode.READ_WRITE, 0, file.length());
// set endianess depending on your CPU architecture
buffer.order(ByteOrder.LITTLE_ENDIAN);

// read the struct
FileHeader fileHeader = FileHeader.read(buffer);

// process your struct as POJO
checkMagic(fileHeader.getMagic());
for(FileSection section : fileHeader.getSections()) {
	processSection(section.getSectionName(), section.getSectionContent());
}

// reset file pointer to the beginning of the file
buffer.position(0);
// and write the struct back to the hard-drive
fileHeader.write(buffer);
```

## Data Types
| S4J Typename  | Java Mapping  | Description |
| ------------- | ------------- | ----------- |
| uint8_t       | int           | Fixed-size 8bit unsigned integer |
| int8_t        | int           | Fixed-size 8bit signed integer |
| uint16_t      | int           | Fixed-size 16bit unsigned integer |
| int16_t       | int           | Fixed-size 16bit signed integer |
| uint32_t      | int           | Fixed-size 32bit unsigned integer |
| int32_t       | int           | Fixed-size 32bit signed integer |
| uint64_t      | long          | Fixed-size 64bit unsigned integer |
| int64_t       | long          | Fixed-size 64bit signed integer |
| float         | float         | Fixed-size 32bit floating point number |
| double        | float         | Fixed-size 64bit floating point number |
| char          | -             | unsupported |
| char[]        | String        | String of characters |
| uint8_t[]     | ByteBuffer    | Raw ByteBuffer |
| int8_t[]      | ByteBuffer    | Raw ByteBuffer |

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
```Java
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
```C++
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
```C++
struct Coordinate {
  int32_t x;
  int32_t y;
  int32_t z;
}
```

## Packages
You can group certain structures into a package.
```C++
package com.structs4java.pkg;

... your struct definitions
```
A document (*.struct) can contain only a single package declaration and if present it must be the first statement before any type declarations. 

## Imports
Once you've defined structures across multiple documents (*.struct) and packages it becomes handy to import a fully qualified name.
Pkg1.structs
```C++
package com.structs4java.example.pkg1;

struct Address {
  ...
}
```
Pkg2.structs
```C++
package com.structs4java.example.pkg2;

import com.structs4java.example.pkg1.Address;

struct Person {
  Address address;
  ...
}
```

## Enums
Enumerations are declared using the enum keyword:
```C++
enum Colors : uint8_t {
  RED = 0xCAFE,
  BLUE = 123,
  GREEN = 42
}
```
Each enum must provide a base type specified after the colon that defines the size of an enum value in serialized form.

## Strings
Fixed strings can be specified as an array of chars:
```C++
struct Person {
  char name[20];
}
```
By default UTF-8 is choosen as encoding but can be specified explicitily using the encoding attribute
```C++
struct Person {
  // Windows wide-string
  char name[20] encoding("UTF16-LE");
}
```
Null-terminated strings can be specified as an array of char without a dimension:
```C++
struct NullTerminatedString {
  char value[];
}
```
For null-terminated strings the number of terminating zeros is determined according to the string encoding so that for example for US-ASCII a single zero indicates the end of the string while 2 zeros are necessary for an UTF16 encoded string.
Variable-length but not null-terminated strings can be represented like the following:
```C++
struct DynamicString {
  uint32_t length sizeof(value);
  char     value[];
}
```

## SizeOf() Attribute
The sizeof attribute defines the size of a struct member or the entire struct in bytes.

For example, useful for header with optional fields at the end:
```C++
struct LegacyFileHeader {
  uint32_t headerLength sizeof(this);
  ... (optional) header fields
}
```

## CountOf() Attribute
The countof attribute defines the number of elments of an array.
```C++
struct Entry {
  ...
}

struct Directory {
  uint16_t numberOfEntries countof(entries);
  Entry entries[];
}
```


