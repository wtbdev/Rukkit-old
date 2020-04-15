package io.rukkit.net;

import java.io.*;

public class GameOutputStream
{
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	DataOutputStream stream = new DataOutputStream(buffer);
	public Packet createPacket(int type) {
        try {
            this.stream.flush();
            this.buffer.flush();
            Packet packet = new Packet(type);
            packet.bytes = this.buffer.toByteArray();
            return packet;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeByte(int val) throws IOException {
        this.stream.writeByte(val);
    }

    public void writeBoolean(boolean val) throws IOException {
        this.stream.writeBoolean(val);
    }

    public void writeInt(int val) throws IOException {
        this.stream.writeInt(val);
    }

    public void writeFloat(float val) throws IOException {
        this.stream.writeFloat(val);
    }

    public void writeLong(long val) throws IOException {
        this.stream.writeLong(val);
    }

    public void writeString(String val) throws IOException {
        this.stream.writeUTF(val);
    }

	public GzipEncoder getEncodeStream(String key) throws IOException{
		GzipEncoder enc = new GzipEncoder();
		enc.str = key;
		return enc;
	}

	public void flushEncodeData(GzipEncoder enc) throws IOException{
		this.writeString(enc.str);
		this.writeInt(enc.buffer.size());
		enc.buffer.writeTo((OutputStream)this.stream);
		//stream.flush();
	}
	}
}
