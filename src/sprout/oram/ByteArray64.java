// Original author: William Deans, william.deans@gmail.com
// Source from: http://stackoverflow.com/questions/878309/java-array-with-more-than-4gb-elements
// Modified by Boyang Wei

package sprout.oram;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ByteArray64 {

    private final int CHUNK_SIZE = 1024*1024*1024; //1GiB

    long size;
    byte [][] data;

    public ByteArray64( long size ) {
        this.size = size;
        init();
    }
    
    public ByteArray64(String filename) {
    	File file = new File(filename);
    	size = FileUtils.sizeOf(file);
    	init();
    }
    
    private void init() {
        if( size == 0 ) {
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
    
    public byte get( long index ) {
        if( index<0 || index>=size ) {
            throw new IndexOutOfBoundsException("Error attempting to access data element "+index+".  Array is "+size+" elements long.");
        }
        int chunk = (int)(index / CHUNK_SIZE);
        int offset = (int)(index % CHUNK_SIZE);
        return data[chunk][offset];
    }
    
    public void set( long index, byte b ) {
        if( index<0 || index>=size ) {
            throw new IndexOutOfBoundsException("Error attempting to access data element "+index+".  Array is "+size+" elements long.");
        }
        int chunk = (int)(index / CHUNK_SIZE);
        int offset = (int)(index % CHUNK_SIZE);
        data[chunk][offset] = b;
    }
    
    /**
     * Simulates a single read which fills the entire array via several smaller reads.
     * 
     * @param fileInputStream
     * @throws IOException
     */
    public void read( FileInputStream fileInputStream ) throws IOException {
        if( size == 0 ) {
            return;
        }
        for( int idx=0; idx<data.length; idx++ ) {
            if( fileInputStream.read( data[idx] ) != data[idx].length ) {
                throw new IOException("short read");
            }
        }
    }
    
    public long size() {
        return size;
    }
}
