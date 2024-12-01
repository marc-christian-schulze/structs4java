package examples.zip;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileWritingTest {

    @Test
    public void testWritingZipFile() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // our file content to store
        byte[] fileData = "example file content".getBytes("UTF-8");

        // compute the CRC32 checksum of the file content
        Checksum checksum = new CRC32();
        checksum.update(fileData, 0, fileData.length);
        long crc32 = checksum.getValue();

        // first we create and write the local file header
        long positionOfLocalFileHeader = buffer.position();
        LocalFileHeader fhFile = new LocalFileHeader();
        fhFile.setFileName("example.txt");
        fhFile.setCompressedSizeInBytes(fileData.length);
        fhFile.setUncompressedSizeInBytes(fileData.length);
        fhFile.setCrc32OfUncompressedData(crc32);
        fhFile.write(buffer);
        // right after the local file header, the actual file content needs to be stored
        buffer.put(fileData);

        // now we need to create the central directory header
        long positionOfFirstCentralDirectoryHeader = buffer.position();
        CentralDirectoryFileHeader cdfh = new CentralDirectoryFileHeader();
        cdfh.setFileName("example.txt");
        cdfh.setCrc32OfUncompressedData(crc32);
        cdfh.setCompressedSizeInBytes(fileData.length);
        cdfh.setUncompressedSizeInBytes(fileData.length);
        cdfh.setRelativeOffsetOfLocalFileHeader(positionOfLocalFileHeader);
        cdfh.write(buffer);

        // finally, we need to write the closing end of central directory header
        EndOfCentralDirectoryRecord eocdr = new EndOfCentralDirectoryRecord();
        eocdr.setOffsetOfStartOfCentralDirectory(positionOfFirstCentralDirectoryHeader);
        eocdr.setNumberOfCentralDirectoryRecordsOnThisDisk(1);
        eocdr.setTotalNumberOfCentralDirectoryRecords(1);
        eocdr.write(buffer);

        // now we read the ZIP file using Java on-board classes and verify
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(buffer.array()));
        ZipEntry fileEntry = zis.getNextEntry();
        assertEquals("example.txt", fileEntry.getName());
        byte[] fileContentRead = zis.readAllBytes();
        assertEquals("example file content", new String(fileContentRead, "UTF-8"));
    }
}
