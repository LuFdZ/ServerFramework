package org.server.core.io.input;

import java.io.IOException;
import java.io.InputStream;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * IoBuffer 输入流包装
 *
 * @author Hxms
 */
public class IoBufferByteInputStream implements ByteInputStream {

	private final InputStream _InputStream;
	
	/**
     * 构造一个新实例
     *
     * @param _buffer
     */
    public IoBufferByteInputStream(IoBuffer _buffer) {
        this._InputStream = _buffer.asInputStream();
    }

	@Override
	public int readByte() {
		try {
            return _InputStream.read();
        } catch (IOException ex) {
            return  -1;
        }
	}

	@Override
	public long available() {
		try {
            return _InputStream.available();
        } catch (IOException ex) {
            return 0;
        }
	}
}
