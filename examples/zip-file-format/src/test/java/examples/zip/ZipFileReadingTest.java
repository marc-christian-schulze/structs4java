package examples.zip;

import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ZipFileReadingTest {

    @Test
    public void testUsingExampleZipFile() throws IOException {
        try(RandomAccessFile raf = new RandomAccessFile("src/test/resources/example.zip", "r")) {
            ByteBuffer buffer = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length());

            // ZIP file specification requires little endian encoding
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            // first, we need to search backwards in the ZIP file for the end of central directory
            assertTrue("No valid central directory found", seekToEndOfCentralDirectory(buffer));

            // read from the buffer using the generated Java classes from the zip.structs file
            EndOfCentralDirectoryRecord eocd = EndOfCentralDirectoryRecord.read(buffer);

            assertEquals("We expect 3 entries in the ZIP file", 3, eocd.getNumberOfCentralDirectoryRecordsOnThisDisk());
            assertEquals("We expect 3 entries in the ZIP file", 3, eocd.getTotalNumberOfCentralDirectoryRecords());

            // seek to first entry of the central directory
            buffer.position((int) eocd.getOffsetOfStartOfCentralDirectory());

            // let's retrieve the file names from the directory
            CentralDirectoryFileHeader cdfhDirectory = CentralDirectoryFileHeader.read(buffer);
            assertEquals(cdfhDirectory.getFileName(), "zip-content/");

            CentralDirectoryFileHeader cdfhFile2 = CentralDirectoryFileHeader.read(buffer);
            assertEquals(cdfhFile2.getFileName(), "zip-content/file2.txt");

            CentralDirectoryFileHeader cdfhFile1 = CentralDirectoryFileHeader.read(buffer);
            assertEquals(cdfhFile1.getFileName(), "zip-content/file1.txt");

            // finally, let's read the file contents
            // first, we need to seek to the location of the local file header
            buffer.position((int) cdfhFile1.getRelativeOffsetOfLocalFileHeader());
            // next, we read the local file header using our generated Java class
            LocalFileHeader fhFile1 = LocalFileHeader.read(buffer);
            // right after the header, the actual file content is placed
            byte[] contentFile1 = new byte[(int) fhFile1.getCompressedSizeInBytes()];
            buffer.get(contentFile1);
            assertEquals("content file 1\n", new String(contentFile1));

            // same for file 2
            buffer.position((int) cdfhFile2.getRelativeOffsetOfLocalFileHeader());
            LocalFileHeader fhFile2 = LocalFileHeader.read(buffer);
            byte[] contentFile2 = new byte[(int) fhFile2.getCompressedSizeInBytes()];
            buffer.get(contentFile2);
            assertEquals("content file 2\n", new String(contentFile2));
        }
    }

    private static boolean seekToEndOfCentralDirectory(ByteBuffer buffer) throws IOException {
        int value;

        // we start scanning from the end of the file
        buffer.position(buffer.limit());

        // first search for 0x06
        value = readPreviousByteFromBuffer(buffer);
        while(value != 0x06) {
            if(buffer.position() < 4) {
                return false;
            }
            value = readPreviousByteFromBuffer(buffer);
        }

        // now we search for 0x05
        value = readPreviousByteFromBuffer(buffer);
        while(value != 0x05) {
            if(buffer.position() < 3) {
                return false;
            }
            value = readPreviousByteFromBuffer(buffer);
        }

        // now we search for 0x4b
        value = readPreviousByteFromBuffer(buffer);
        while(value != 0x4b) {
            if(buffer.position() < 2) {
                return false;
            }
            value = readPreviousByteFromBuffer(buffer);
        }

        // finally we search for 0x50
        value = readPreviousByteFromBuffer(buffer);
        while(value != 0x50) {
            if(buffer.position() < 1) {
                return false;
            }
            value = readPreviousByteFromBuffer(buffer);
        }

        return true;
    }

    private static int readPreviousByteFromBuffer(ByteBuffer buffer) throws IOException {
        buffer.position(buffer.position() - 1);
        int value = buffer.get() & 0xFF;
        buffer.position(buffer.position() - 1);
        return value;
    }
}
