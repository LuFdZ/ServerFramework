package org.server.core.io.input;

import java.awt.Point;

import org.server.core.io.tools.LittleEndianAccessorEncoding;

/**
 * Provides a generic interface to a Little Endian stream of bytes.
 *
 * @version 1.0
 * @author Frz
 * @since Revision 323
 */
public class GenericLittleEndianAccessor implements LittleEndianAccessor {
	
	private final ByteInputStream bs;
    private final long[] cache = new long[8];
    
    /**
     * Class constructor - Wraps the accessor around a stream of bytes.
     *
     * @param bs The byte stream to wrap the accessor around.
     */
    public GenericLittleEndianAccessor(ByteInputStream bs) {
        this.bs = bs;
    }
    
    /**
     * Read a single byte from the stream.
     *
     * @return The byte read.
     * @see net.sf.odinms.tools.data.input.ByteInputStream#readByte
     */
    @Override
    public byte readByte() {
        return (byte) bs.readByte();
    }
    
    @Override
    public boolean readBoolean() {
        return bs.readByte() == 1;
    }
    
    /**
     * Reads an integer from the stream.
     *
     * @return The integer read.
     */
    @Override
    public int readInt() {
        /*   int byte1, byte2, byte3, byte4;
        
         byte1 = bs.readByte();
         byte2 = bs.readByte();
         byte3 = bs.readByte();
         byte4 = bs.readByte();*/
        cache[0] = bs.readByte();
        cache[1] = bs.readByte();
        cache[2] = bs.readByte();
        cache[3] = bs.readByte();
        return (int) ((cache[3] << 24) | (cache[2] << 16) | (cache[1] << 8) | cache[0]);
        // return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }
    
    /**
     * Reads a short integer from the stream.
     *
     * @return The short read.
     */
    @Override
    public short readShort() {
        /*     int byte1, byte2;
         byte1 = bs.readByte();
         byte2 = bs.readByte();
         return (short) ((byte2 << 8) + byte1);*/
        cache[0] = bs.readByte();
        cache[1] = bs.readByte();
        return (short) ((cache[1] << 8) + cache[0]);
    }

    /**
     * Reads a single character from the stream.
     *
     * @return The character read.
     */
    @Override
    public char readChar() {
        return (char) readShort();
    }
    
    /**
     * Reads a long integer from the stream.
     *
     * @return The long integer read.
     */
    @Override
    public long readLong() {
        /*  long byte1 = bs.readByte();
         long byte2 = bs.readByte();
         long byte3 = bs.readByte();
         long byte4 = bs.readByte();
         long byte5 = bs.readByte();
         long byte6 = bs.readByte();
         long byte7 = bs.readByte();
         long byte8 = bs.readByte();
         return (byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) + (byte4 << 24) + (byte3 << 16)
         + (byte2 << 8) + byte1;*/
        cache[0] = bs.readByte();
        cache[1] = bs.readByte();
        cache[2] = bs.readByte();
        cache[3] = bs.readByte();
        cache[4] = bs.readByte();
        cache[5] = bs.readByte();
        cache[6] = bs.readByte();
        cache[7] = bs.readByte();

        return (cache[7] << 56) + (cache[6] << 48) + (cache[5] << 40) + (cache[4] << 32) + (cache[3] << 24) + (cache[2] << 16)
                + (cache[1] << 8) + cache[0];
    }
    
    /**
     * Reads a floating point integer from the stream.
     *
     * @return The float-type integer read.
     */
    @Override
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }
    
    /**
     * Reads a double-precision integer from the stream.
     *
     * @return The double-type integer read.
     */
    @Override
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }
    
    /**
     * Reads an ASCII string from the stream with length <code>n</code>.
     *
     * @param n Number of characters to read.
     * @return The string read.
     */
    @Override
    public final String readAsciiString(int n) {
        byte ret[] = new byte[n];
        for (int x = 0; x < n; x++) {
            ret[x] = (byte) readByte();
        }
        return new String(ret, LittleEndianAccessorEncoding.DEFAULT);
    }
    
    /**
     * Reads a MapleStory convention lengthed ASCII string. This consists of a
     * short integer telling the length of the string, then the string itself.
     *
     * @return The string read.
     */
    @Override
    public String readMapleAsciiString() {
        return readAsciiString(readShort());
    }
    
    /**
     * Reads <code>num</code> bytes off the stream.
     *
     * @param num The number of bytes to read.
     * @return An array of bytes with the length of <code>num</code>
     */
    @Override
    public byte[] read(int num) {
        byte[] ret = new byte[num];
        for (int x = 0; x < num; x++) {
            ret[x] = (byte) readByte();
        }
        return ret;
    }
    
    /**
     * Skips the current position of the stream <code>num</code> bytes ahead.
     *
     * @param num Number of bytes to skip.
     */
    @Override
    public void skip(int num) {
        for (int x = 0; x < num; x++) {
            readByte();
        }
    }
    
    /**
     * @see net.sf.odinms.tools.data.input.ByteInputStream#available
     */
    @Override
    public long available() {
        return bs.available();
    }

    /**
     * @see java.lang.Object#toString
     */
    @Override
    public String toString() {
        return bs.toString();
    }

    @Override
    public final Point readPos() {
        int x = readShort();
        int y = readShort();
        return new Point(x, y);
    }

	@Override
	public String readNullTerminatedAsciiString() {
		// TODO Auto-generated method stub
		return null;
	}
}
