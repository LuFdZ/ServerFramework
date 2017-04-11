package org.server.core.io.output;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * IoBuffer 字节输出流
 *
 * @author Hxms
 */
public class IoBufferByteOutputStream implements ByteOutputStream {
	
	private final IoBuffer _buffer;
	
	public IoBufferByteOutputStream() {
        this(16);
    }
	
	public IoBufferByteOutputStream(int count) {
        _buffer = IoBuffer.allocate(count, true);
        _buffer.setAutoExpand(true).setAutoShrink(true);
    }
	
	@Override
    public void writeByte(byte b) {
        _buffer.put(b);
    }

    @Override
    public void write(byte[] data) {
        _buffer.put(data);
    }

    @Override
    public byte[] toArray() {
        byte[] data = new byte[_buffer.flip().remaining()];
        _buffer.get(data);
        return data;
    }

    public IoBuffer getBuffer() {
        return _buffer;
    }
}
