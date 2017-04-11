/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.server.core.io.output;

import java.awt.Point;

import org.server.core.io.tools.LittleEndianAccessorEncoding;

/**
 * Provides a generic writer of a little-endian sequence of bytes.
 *
 * @author Frz
 * @version 1.0
 * @since Revision 323
 */
public class GenericLittleEndianWriter implements LittleEndianWriter{

	private ByteOutputStream bos;
	
	protected GenericLittleEndianWriter() {
    }
	
	/**
     * 获得字节输出流
     *
     * @return
     */
    public ByteOutputStream getByteOutputStream() {
        return bos;
    }
    
    /**
     * Sets the byte-output stream for this instance of the object.
     *
     * @param bos The new output stream to set.
     */
    protected void setByteOutputStream(ByteOutputStream bos) {
        this.bos = bos;
    }
    
    /**
     * Class constructor - only this one can be used.
     *
     * @param bos The stream to wrap this objecr around.
     */
    public GenericLittleEndianWriter(ByteOutputStream bos) {
        this.bos = bos;
    }
    
    /**
     * Write an array of bytes to the stream.
     *
     * @param b The bytes to write.
     */
	@Override
	public synchronized void write(byte[] b) {
		bos.write(b);
	}
	
	/**
     * Write a byte to the stream.
     *
     * @param b The byte to write.
     */
	@Override
	public synchronized void write(byte b) {
		bos.writeByte(b);
	}

	@Override
	public synchronized void write(boolean val) {
		bos.writeByte((byte) (val ? 1 : 0));
	}
	
	/**
     * Write a byte in integer form to the stream.
     *
     * @param b The byte as an <code>Integer</code> to write.
     */
	@Override
	public void write(int b) {
		bos.writeByte((byte) b);
		
	}

	/**
     * Writes an integer to the stream.
     *
     * @param i The integer to write.
     */
	@Override
	public synchronized void writeInt(int i) {
		bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
        bos.writeByte((byte) ((i >>> 16) & 0xFF));
        bos.writeByte((byte) ((i >>> 24) & 0xFF));
	}

	/**
     * Write a short integer to the stream.
     *
     * @param i The short integer to write.
     */
	@Override
	public synchronized void writeShort(int i) {
		bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
		
	}

	/**
     * Write a long integer to the stream.
     *
     * @param l The long integer to write.
     */
	@Override
	public synchronized void writeLong(long l) {
		bos.writeByte((byte) (l & 0xFF));
        bos.writeByte((byte) ((l >>> 8) & 0xFF));
        bos.writeByte((byte) ((l >>> 16) & 0xFF));
        bos.writeByte((byte) ((l >>> 24) & 0xFF));
        bos.writeByte((byte) ((l >>> 32) & 0xFF));
        bos.writeByte((byte) ((l >>> 40) & 0xFF));
        bos.writeByte((byte) ((l >>> 48) & 0xFF));
        bos.writeByte((byte) ((l >>> 56) & 0xFF));
		
	}

	/**
     * Writes an ASCII string the the stream.
     *
     * @param s The ASCII string to write.
     */
	@Override
	public synchronized void writeAsciiString(String s) {
		 write(s.getBytes(LittleEndianAccessorEncoding.DEFAULT));
		
	}

    /**
     * Writes a null-terminated ASCII string to the stream.
     *
     * @param s The ASCII string to write.
     */
	@Override
	public void writeNullTerminatedAsciiString(String s) {
		writeAsciiString(s);
        write(0);
	}

    /**
     * Writes a maple-convention ASCII string to the stream.
     *
     * @param s The ASCII string to use maple-convention to write.
     */
	@Override
	public void writeMapleAsciiString(String s) {
		byte[] data = s.getBytes(LittleEndianAccessorEncoding.DEFAULT);
		writeShort((short) data.length);
		write(data);
	}
	
	public synchronized void writeOfMaxByteCountString(String s, int maxbytecount) {
		byte[] bytes;
		if (s == null) {
			bytes = new byte[maxbytecount];
		}else{
			bytes = s.getBytes(LittleEndianAccessorEncoding.DEFAULT);
		}
		for (int i = 1; i <= maxbytecount; i++) {
            if (bytes.length >= i) {
                write(bytes[i - 1]);
            } else {
                write(0);
            }
        }
	}
	
	 public synchronized void writeMapleNameString(String s) {
	        writeOfMaxByteCountString(s, 13);
	        /* if (s.getBytes(ASCII).length > 12) {
	         s = s.substring(0, 12);
	         }
	         writeAsciiString(s);
	         for (int x = s.getBytes().length; x < 13; x++) {
	         write(0);
	         }*/
	    }

	@Override
	public void writePos(Point s) {
		writeShort(s.x);
        writeShort(s.y);
	}

	@Override
	public synchronized void writeZeroBytes(int times) {
		for (int i = 0; i < times; i++) {
            this.bos.writeByte((byte) 0);
        }
		
	}
}
