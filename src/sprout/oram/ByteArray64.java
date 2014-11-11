// Byte array that can have long type length
// Revised and extended by Boyang Wei
// based on code from William Deans (william.deans@gmail.com)
// original code: http://stackoverflow.com/questions/878309/java-array-with-more-than-4gb-elements

package sprout.oram;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ByteArray64 {

    private final int CHUNK_SIZE = 1024*1024*1024; // 1GB
	//private final int CHUNK_SIZE = 10;

    long size;
    byte [][] data;

    public ByteArray64( long size ) {
        this.size = size;
        init();
    }
    
    public ByteArray64(String filename) throws IOException {
    	File file = new File(filename);
    	size = FileUtils.sizeOf(file);
    	init();
    	readFromFile(file);
    }
    
    private void init() {
        if( size < 0 ) {
            data = null;
        } else {
            int chunks = (int)(size / CHUNK_SIZE);
            int remainder = (int)(size % CHUNK_SIZE);
            data = new byte[chunks+(remainder==0?0:1)][];
            for(int i=0; i<chunks; i++) {
                data[i] = new byte[CHUNK_SIZE];
            }
            if( remainder != 0 ) {
                data[chunks] = new byte[remainder];
            }
        }
    }
    
    public long size() {
        return size;
    }
    
    public byte getByte( long index ) {
        if( index<0 || index>=size ) {
            throw new IndexOutOfBoundsException("Error attempting to access data element "+index+".  Array is "+size+" elements long.");
        }
        int chunk = (int)(index / CHUNK_SIZE);
        int offset = (int)(index % CHUNK_SIZE);
        return data[chunk][offset];
    }
    
    public void setByte( long index, byte b ) {
        if( index<0 || index>=size ) {
            throw new IndexOutOfBoundsException("Error attempting to access data element "+index+".  Array is "+size+" elements long.");
        }
        int chunk = (int)(index / CHUNK_SIZE);
        int offset = (int)(index % CHUNK_SIZE);
        data[chunk][offset] = b;
    }
    
    public byte[] getBytes(long start_index, int length) {
    	if (length <= 0)
    		return null;
    	
    	long end_index = start_index + length;
    	if( start_index<0 || start_index>=size || end_index>size) {
            throw new IndexOutOfBoundsException("Error attempting to access data elements from " + start_index + " to "
            		+ end_index + ".  Array is " + size + " elements long.");
        }
    	
    	int start_chunk = (int)(start_index / CHUNK_SIZE);
        int start_offset = (int)(start_index % CHUNK_SIZE);
        int end_chunk = (int)(end_index / CHUNK_SIZE);
        byte[] output = new byte[length];
        
        if (end_chunk == start_chunk) {
        	System.arraycopy(data[start_chunk], start_offset, output, 0, length);
        	return output;
        }

        int end_offset = (int)(end_index % CHUNK_SIZE);
        int middle_chunks = Math.max(end_chunk - start_chunk - 1, 0);
        int copy_offset = 0;
        
        System.arraycopy(data[start_chunk], start_offset, output, copy_offset, CHUNK_SIZE-start_offset);
        copy_offset += CHUNK_SIZE-start_offset;
        
        for (int i=0; i<middle_chunks; i++) {
        	System.arraycopy(data[start_chunk+i+1], 0, output, copy_offset, CHUNK_SIZE);
        	copy_offset += CHUNK_SIZE;
        }
        
        if (end_offset > 0)
        	System.arraycopy(data[end_chunk], 0, output, copy_offset, end_offset);
        
        return output;
    }
    
    public void setBytes(long start_index, byte[] newData) {
    	int length = newData.length;
    	if (length == 0)
    		return;
    	
    	long end_index = start_index + length;
    	if( start_index<0 || start_index>=size || end_index>size) {
            throw new IndexOutOfBoundsException("Error attempting to set data elements from " + start_index + " to "
            		+ end_index + ".  Array is " + size + " elements long.");
        }
    	
    	int start_chunk = (int)(start_index / CHUNK_SIZE);
        int start_offset = (int)(start_index % CHUNK_SIZE);
        int end_chunk = (int)(end_index / CHUNK_SIZE);
        
        if (start_chunk == end_chunk) {
        	System.arraycopy(newData, 0, data[start_chunk], start_offset, length);
        	return;
        }
        
        int end_offset = (int)(end_index % CHUNK_SIZE);
        int middle_chunks = Math.max(end_chunk - start_chunk - 1, 0);
        int copy_offset = 0;
        
        System.arraycopy(newData, copy_offset, data[start_chunk], start_offset, CHUNK_SIZE-start_offset);
        copy_offset += CHUNK_SIZE-start_offset;
        
        for (int i=0; i<middle_chunks; i++) {
        	System.arraycopy(newData, copy_offset, data[start_chunk+i+1], 0, CHUNK_SIZE);
        	copy_offset += CHUNK_SIZE;
        }
        
        if (end_offset > 0)
        	System.arraycopy(newData, copy_offset, data[end_chunk], 0, end_offset);
    }
    
    public void readFromFile(File file) throws IOException {
    	if (size < 0) {
    		return;
    	}
    	FileInputStream fileInputStream = FileUtils.openInputStream(file);
    	for(int i=0; i<data.length; i++) {
            if( fileInputStream.read( data[i] ) != data[i].length ) {
                throw new IOException("short read");
            }
        }
    	fileInputStream.close();
    }
    
    public void writeToFile(String filename) throws IOException {
    	if (size < 0)
    		return;
    	
    	File file = new File(filename);
    	FileUtils.deleteQuietly(file);
    	for (int i=0; i<data.length; i++)
    		FileUtils.writeByteArrayToFile(file, data[i], true);
    }
    
    // testing
    public static void main(String args[]) throws IOException {
    	ByteArray64 t1 = new ByteArray64(1536);
    	byte one = 1;
    	System.out.println(one);
    	t1.setByte(1112, one);
    	t1.writeToFile("files/bytearray64-test");
    	
    	ByteArray64 t2 = new ByteArray64("files/bytearray64-test");
    	one = t2.getByte(1112);
    	System.out.println(one);
    	
    	ByteArray64 t3 = new ByteArray64(30);
    	byte[] input = new byte[20];
    	for (int i=0; i<20; i++) 
    		input[i] = (byte) (i+1);
    	t3.setBytes(5, input);
    	byte[] output = t3.getBytes(2, 26);
    	byte[] output2 = t3.getBytes(2, 26);
    	for (int i=0; i<output.length; i++)
    		System.out.println(output[i] + " " + output2[i]);
    	t3.writeToFile("files/bytearray64-test");
    	ByteArray64 t4 = new ByteArray64("files/bytearray64-test");
    	output = t4.getBytes(0, 30);
    	for (int i=0; i<output.length; i++)
    		System.out.println(output[i]);
    }
    
}
