# Structs4Java
[![master status](https://img.shields.io/travis/marc-christian-schulze/structs4java/master.svg?maxAge=3600)](https://travis-ci.org/marc-christian-schulze/structs4java)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.marc-christian-schulze.structs4java/structs4java-maven-plugin.svg?maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.marc-christian-schulze.structs4java%22)
[![license](https://img.shields.io/github/license/marc-christian-schulze/structs4java.svg?maxAge=3600)](https://github.com/marc-christian-schulze/structs4java/blob/master/LICENSE)

Structs4Java is a code generator that is based on structure definitions very similiar to C/C++ but with some subtle differences. Unlike in C/C++, 
* structs have a defined memory layout (no automatic alignment/packing), 
* structs can have a dynamic size (we support dynamic arrays) 
* but we do not support unions.  

Its purpose is to provide an easy and portable way to read/write legacy file formats that are typically described as C/C++ structures. For each `struct` and `enum` declaration the code generator will produce a corresponding Java class with a `read` and `write` method accepting a `java.nio.ByteBuffer`.

## Getting Started
Add the plugin to your maven build:
```Maven
<plugin>
  <groupId>com.github.marc-christian-schulze.structs4java</groupId>
  <artifactId>structs4java-maven-plugin</artifactId>
  <version>1.0.31</version>
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

Create some Structs in a file `FileFormat.structs` under `src/main/structs`:
```C++
package com.mycompany.projectx;

struct FileHeader {
  uint8_t     magic[4];
  uint16_t    numberSections countof(sections);
  FileSection sections[];
}

struct FileSection {
  SectionType type;
  char        name[32];
  uint32_t    length sizeof(sectionContent);
  uint8_t     content[];
}

enum SectionType : uint32_t {
  TypeA = 0xCAFEBABE,
  TypeB = 0xDEADBEAF,
  TypeC = 0815
}

struct ContentA {
  ...
}
```

Reading structs:
```Java
java.nio.ByteBuffer buffer = ...
FileHeader fileHeader = FileHeader.read(buffer);

for(FileSection section : fileHeader.getSections()) {
  switch(section.getType()) {
    case TypeA:
      ContentA content = ContentA.read(section.getContent());
      ...
    case TypeB:
      ...
  }
}
```

Writing structs:
```Java
FileHeader fileHeader = ...
fileHeader.getSections().add(new FileSection());

java.nio.ByteBuffer buffer = ...
fileHeader.write(buffer);
```

## Getting Started with the Eclipse Plugin
Install the plugin from our [Update Site](https://dl.bintray.com/marc-christian-schulze/Structs4JavaUpdateSite/updates/):
```
https://dl.bintray.com/marc-christian-schulze/Structs4JavaUpdateSite/updates/
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
  * variable lengths
  * padding

## Unsupported
* Unions
* Bit-Fields
* Pointer
* 64bit enums (partial support for 63 bit)

# User Guide

## Primitive Data Types
| S4J Typename  | Java Mapping           | Description                            |
| ------------- | ---------------------- | -------------------------------------- |
| uint8_t       | long                   | Fixed-size 8bit unsigned integer       |
| int8_t        | long                   | Fixed-size 8bit signed integer         |
| uint16_t      | long                   | Fixed-size 16bit unsigned integer      |
| int16_t       | long                   | Fixed-size 16bit signed integer        |
| uint32_t      | long                   | Fixed-size 32bit unsigned integer      |
| int32_t       | long                   | Fixed-size 32bit signed integer        |
| uint64_t      | long                   | Fixed-size 64bit unsigned integer      |
| int64_t       | long                   | Fixed-size 64bit signed integer        |
| float         | double                 | Fixed-size 32bit floating point number |
| double        | double                 | Fixed-size 64bit floating point number |
| char          | -                      | unsupported                            |
| char[]        | String                 | String of characters (max size 2^31)   |
| uint8_t[]     | java.nio.ByteBuffer    | Raw ByteBuffer                         |
| int8_t[]      | java.nio.ByteBuffer    | Raw ByteBuffer                         |

## Struct Files
Struct files have the file extension `*.structs` and can contain multiple struct or enum definitions. Different to Java very struct and enum declared in a Struct file is public.

## Structs
Structs are simple Java POJOs that provide a read and write method accepting a `java.nio.ByteBuffer`. A struct can have multiple 
```C++
struct Coordinate {
  int32_t x;
  int32_t y;
  int32_t z;
}
```

## Packages
Similar to Java, you can group structures in packages. 
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

## Dynamic size (in bytes) of members or the struct itself
The sizeof attribute defines the size of a struct member or the entire struct in bytes.

For example, useful for header with optional fields at the end:
```C++
struct LegacyFileHeader {
  uint32_t headerLength sizeof(this);
  // ... (optional) header fields
}
```

Or defining the size of an array:
```C++
struct BString {
	uint32_t length sizeof(str);
	char     str[];
}
```

## Dynamic count of array elements
The countof attribute defines the number of elments of an array (not the size!).
```C++
struct Entry {
  ...
}

struct Directory {
  uint16_t numberOfEntries countof(entries);
  Entry entries[];
}
```

## Padding
Depending on the memory layout of the format you need to read/write you sometimes have to align fields of the structure introducing some padding bytes.
```C++
struct StructWithPadding {
	// this field uses 2 bytes for its value but is padded with 2 null-bytes
	// offset: 0
	uint16_t valueWithPadding padding(4);
	// this field will be at offset 4 instead of 2 due to the padding
	// offset: 4
	double anotherField;
}
```
Padding can not only be applied to primitive fields but also for Strings, Structures and (dynamic) Arrays, e.g.
```C++
struct DynamicStructWithPadding {
	uint16_t length sizeof(content);
	// This array has a dynamic length but always a multiple of 4 due to the padding
	uint8_t content[] padding(4);
}
```

# Comparison to Javolution
If you do not want to rely on code generation you should have a look at [Javolution](http://javolution.org/) which is a plain Java implementation.  

Javolution (example taken from official documentation):
```Java
 public enum Gender { MALE, FEMALE };
 public static class Date extends Struct {
     public final Unsigned16 year  = new Unsigned16();
     public final Unsigned8  month = new Unsigned8();
     public final Unsigned8  day   = new Unsigned8();
 }
 public static class Student extends Struct {
     public final Enum32<Gender>       gender = new Enum32<Gender>(Gender.values());
     public final UTF8String           name   = new UTF8String(64);
     public final Date                 birth  = inner(new Date());
     public final Float32[]            grades = array(new Float32[10]);
     public final Reference32<Student> next   = new Reference32<Student>();
 }
```

Structs4Java equivalent:
```C++
enum Gender : uint32_t {
  MALE   = 0, 
  FEMALE = 1
}

struct Date {
  uint16_t year;
  uint8_t  month;
  uint8_t  day;
}

struct Student {
  Gender   gender;
  char     name[64];     // default charset is UTF-8
  Date     birth;
  float    grades[10];
  // Student*    next;  // Pointers are not supported by Structs4Java
}
```

# Developing Structs4Java

**Requirements:**
- Git
- Docker

In order to get started simply clone the repo to your local drive and start the compilation:
```
$ ./build.sh
```
First, a docker container is built containing the required build tools (JDK, Maven, etc.). Afterwards the sources are compiled inside of the container. During the compilation maven will create a dedicated M2-Repo in your workspace.

## Creating a new Release
todo