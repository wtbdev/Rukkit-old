package io.rukkit.net;

public class Packet
{
	public byte[] bytes;
    public int type;

    public Packet(int type) {
        this.type = type;
    }
}
