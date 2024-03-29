# Structs4Java
[![master status](https://img.shields.io/travis/marc-christian-schulze/structs4java/master.svg?maxAge=3600)](https://travis-ci.com/marc-christian-schulze/structs4java)
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
  <version>${s4j.version}</version>
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

Define some structures you would like to read/write in a `*.structs` file under `src/main/structs`, e.g. `FileFormat.structs`:
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

## Features
* Reading / Writing C/C++ Structs from/to Java NIO ByteBuffers
* Generated code has no additional dependencies
* Support for 
  * structs 
  * enums 
  * fixed-size primitives incl. floating point numbers
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
  * strings incl. different encodings
  * arrays
  * nested elements
  * variable lengths
  * padding
  * bit fields
  * implement Java interfaces

## Unsupported
* Unions
* Pointer
* 64bit enums (partial support; up to 63 bits)

# User Guide

## Primitive Data Types

The following table shows the built-in data types of Structs4Java. They can be used to compose more advanced types.

| S4J Typename  | Java Mapping           | Size (bytes) | Subject to Endianess | Description                            |
| ------------- | ---------------------- | ------------ | ---------------------| -------------------------------------- |
| uint8_t       | long                   | 1            | no                   | Fixed-size 8bit unsigned integer       |
| int8_t        | long                   | 1            | no                   | Fixed-size 8bit signed integer         |
| uint16_t      | long                   | 2            | yes                  | Fixed-size 16bit unsigned integer      |
| int16_t       | long                   | 2            | yes                  | Fixed-size 16bit signed integer        |
| uint32_t      | long                   | 4            | yes                  | Fixed-size 32bit unsigned integer      |
| int32_t       | long                   | 4            | yes                  | Fixed-size 32bit signed integer        |
| uint64_t      | long                   | 8            | yes                  | Fixed-size 64bit unsigned integer      |
| int64_t       | long                   | 8            | yes                  | Fixed-size 64bit signed integer        |
| float         | double                 | 4            | no                   | Fixed-size 32bit floating point number |
| double        | double                 | 8            | no                   | Fixed-size 64bit floating point number |
| char[]        | String                 | variable     | no                   | String of characters (max size 2^31)   |
| uint8_t[]     | java.nio.ByteBuffer    | variable     | no                   | Raw ByteBuffer                         |
| int8_t[]      | java.nio.ByteBuffer    | variable     | no                   | Raw ByteBuffer                         |

There is no primitive type `char` available. If you need to read a single 1-byte character you can use `char[1]` instead.

## Advanced Data Types (Structs & Enums)

You can define complex data structures using the `struct` keyword. Similar to C++ it allows you to define a fixed size structure composed of one or more fields. In addition Structs4Java also allows specifying variable-sized and greedy structures.

### Fixed-Sized Structures

Fixed-sized structures are `struct`s that only consists of fixed-sized fields. So no array without explicit dimension are allowed in any of the fields.  
Example:
```C++
struct Address { // getSizeOf() = 50
  char     street[20];
  char     city[20];
  char     zipCode[10];
}

struct Person { // getSizeOf() = 102
  char     name[50];
  Address  address;
  int16_t  age;
}
```
Both defined structures in the example are fixed-sized. The generated Java classes will therefore have a static `getSizeOf()` method that returns the exact number of bytes a serialized instance of this structure would require in memory.

### Variable-Sized Structures

Variable-sized structures contain at least one array field which dimension is defined by the value of another field. Look at the following example of a `BString` which is quiet common when working with COM ([Component Object Model](https://docs.microsoft.com/en-us/windows/desktop/com/the-component-object-model)):
```C++
struct BString { // no getSizeOf()
  uint32_t  length   sizeOf(value);
  char      value[]  encoding("UCS-2") null-terminated;
}
```
In the given example no explicit dimension of field `value` is provided. Instead the field `length` is marked with the `sizeOf` keyword indicating that it's value will provide the size of the overall array `value` in bytes. You can also use the `countOf` keyword to provide the count of elements an array will contain, e.g.
```C++
struct Person { // no getSizeOf()
  uint8_t      numberMailAddresses  countOf(mailAddresses);
  MailAddress  mailAddresses[];
}
```
Java classes generated for variable-sized structures do not have the `getSizeOf()` method since it depends on the values of a specific instance.  
It's also possible to let a field indicate the size of the structure itself (including the field containing the size), e.g.
```C++
struct FileHeader {
  uint32_t  headerLength  sizeof(this);
  // ... (optional) header fields
}
```
By using definitions like the above-mentioned you can make structure fields optional.

### Greedy Structures

Greedy structures are a special case of variable-sized structures. They have as last field an array without dimension and no other field that would provide any information about the length of the last field. When those structures are read from a `ByteBuffer` it will consume all bytes available.
```C++
struct WholeBuffer { // no getSizeOf()
  Header            header;
  StandardContent   content;
  uint8_t           extensionContent[];
}
```
Greedy structures can be nested inside variable structures:
```C++
struct GreedyStruct { // no getSizeOf()
  uint8_t   content[];
}

struct VariableSized { // no getSizeOf()
  uint32_t      length  sizeOf(greedy);
  GreedyStruct  greedy;
}
```

### Enums

Enumerations are sets of values that are derived from built-in data types, e.g.
```C++
enum Colors : uint8_t {
  RED = 0xCAFE,
  BLUE = 123,
  GREEN = 42
}
```
Unlike in C++, the size of enums in Structs4Java is not derived of the highest value in the enum but explicitly specified.

## Namespaces

A `*.structs` file can contain multiple `struct` or `enum` definitions that by default will be placed in Java's default package. If you want the code generator to put the generated Java classes into different packages you can use the `package` keyword at the beginning of the file. E.g.
```C++
package com.structs4java.pkg;

... your struct definitions
```

In order to re-use structure definitions contained in another `*.structs` file using a different package you will have to import them like in Java:  
`Pkg1.structs`
```C++
package com.structs4java.example.pkg1;

struct Address {
  ...
}
```
`Pkg2.structs`
```C++
package com.structs4java.example.pkg2;

import com.structs4java.example.pkg1.Address;

struct Person {
  Address address;
  ...
}
```
If the structures you want to reuse are in the same package you can omit the import definition.

## Strings
Fix-sized strings can be specified as an array of chars:
```C++
struct Person { // getSizeOf() = 20
  char name[20];
}
```
By default, a fixed sized string is capped or filled (using 0x0) if the given value is shorter or longer than the char array.
You can change the default filler byte using:
```C++
struct Person { // getSizeOf() = 20
  char name[20] filler(0x20); // blank-filled char array
}
```

By default UTF-8 is choosen as encoding but can be specified explicitily using the encoding attribute
```C++
struct Person { // getSizeOf() = 20
  // Windows wide-string
  char name[20] encoding("UTF16-LE");
}
```
Null-terminated strings can be specified as an array of char without a dimension and are a special case of greedy structures:
```C++
struct NullTerminatedString { // no getSizeOf()
  char value[];
}
```
For null-terminated strings the number of terminating zeros is determined according to the string encoding so that for example for US-ASCII a single zero indicates the end of the string while 2 zeros are necessary for an UTF16 encoded string.
Variable-length but not null-terminated strings can be represented like the following:
```C++
struct DynamicString { // no getSizeOf()
  uint32_t length sizeof(value);
  char     value[];
}
```
And finally, there's a way to create a variable-sized null-terminated string. In this case the length field includes the terminating zeros:
```C++
struct DynamicStringWithNullTermination { // no getSizeOf()
  uint32_t length sizeof(value);
  char     value[] null-terminated;
}
```

## Memory Layout

Fields of a `struct` are layed-out without spacing in the order they appear top-down in the `struct` definition.  
E.g.
```C++
struct Coordinate { // getSizeOf() = 6
  uint16_t  x;
  uint16_t  y;
  uint16_t  z;
}
```
will be represented as the following 6 bytes in memory:
```
| 0 | 1 | 2 | 3 | 4 | 5 |
|   x   |   y   |   z   |
```
But in some cases you want to layout elements different by providing some boundary to which elements shall align. This can be achieved by using the `padding` keyword. Padding will specify the number of bytes a field will allocate, e.g.
```C++
struct Coordinate { // getSizeOf() = 12
  uint16_t  x  padding(4);
  uint16_t  y  padding(4);
  uint16_t  z  padding(4);
}
```
This will introduce 2 filler bytes (zeros) after the structure.
```
| 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 |
|   x   | filler|   x   | filler|   z   | filler  |
```
You can customize the padding byte as well:
```C++
struct Coordinate { // getSizeOf() = 12
  uint16_t  x  padding(4, using = 0xFF);
  uint16_t  y  padding(4);
  uint16_t  z  padding(4);
}
```

Padding can not only be applied to primitive fields but also for Strings, Structures and (dynamic) Arrays, e.g.
```C++
struct DynamicStructWithPadding { // no getSizeOf()
  uint16_t length sizeof(content);
  // This array has a dynamic length 
  // but always a multiple of 4 due to the padding
  uint8_t content[] padding(4);
}
```
Note: The padding in this example does not get applied for each element of the array but the arrray as a whole. If you need to pad each element you need to use a dedicated `struct` to describe the element including the desired padding.

## Bit Fields

A bit field is a fixed-size type where subsets of the underlying bits are interpreted independently as separated fields. Each field of a `bitfield` is treated like a field of the containing `struct`. This is often used to store a set of flags in a memory-efficient representation, e.g. a `uint8_t` can store 8 boolean flags:
```C++
struct BitsetWith8Flags
{
  // any other fields ...
  bitfield uint8_t {
    boolean flag0 : 1; // 2^7 = 128
    boolean flag1 : 1; // 2^6 = 64
    boolean flag2 : 1; // 2^5 = 32
    boolean flag3 : 1; // 2^4 = 16
    boolean flag4 : 1; // 2^3 = 8
    boolean flag5 : 1; // 2^2 = 4
    boolean flag6 : 1; // 2^1 = 2
    boolean flag7 : 1; // 2^0 = 1
  }
  // any other fields ...
}
```
You can also group multiple bits to interpret them as integer or enum values:
```C++
enum SomeEnum : uint16_t {
  ...
}

struct AnotherBitset
{
  // any other fields ...
  bitfield uint8_t {
    int32_t   number : 4; // 2^7, 2^6, 2^5, 2^4; value range 0 .. 16
    boolean   flag   : 1; // 2^3
    SomeEnum  myEnum : 3; // 2^2, 2^1, 2^0
  }
  // any other fields ...
}
```

Bits of a bitfield in Structs4Java are always defined top-down from the highest to the lowest bit, regardless of the memory representation - even if subject to endianess! That way you can re-use the same bitfield for reading bitfields of different endianess represenations.

## Endianess

While Structs4Java does not provide endianess transformation itself by using the features on the `java.nio.ByteBuffer` you can read structures with different endianess encoding by setting the `ByteOrder` before reading / writing.
```Java
java.nio.ByteBuffer buffer = ...
buffer.order(ByteOrder.BIG_ENDIAN);

MyStruct s = MyStruct.read(buffer);
```
This will automatically transform the endianess of all fields having types that are subject to endianess (cf. table of built-in data types).

## Implementing Java Interfaces

Although `struct`s can not form any inheritance relationship you can let them implement interfaces from your Java code, e.g.:
```C++
import org.myproject.MyJavaInterface;

struct SomeStruct implements MyJavaInterface {
  // ...
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
  // Student*    next;  // Pointers are not supported by Structs4Java ...
}

// ... but if they are stored just in a sequence:
struct FileWithStudents {
  Student students[];
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
```
$ docker run -it --rm -v "$(pwd)":/workspace --user "$(id -u):$(id -g)" buildbox bash
```

```
docker-container$ ./perform_release.sh 1.0.51 "Marc-Christian Schulze" "Marc.Christian.Schulze@gmail.com"
```
