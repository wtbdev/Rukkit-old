package io.rukkit.util;

public class PacketType
{
	//Server Commands
	public static final int PACKET_REGISTER_CONNECTION = 161;
	public static final int PACKET_TEAM_LIST = 0;
	public static final int PACKET_HEART_BEAT = 0;
	public static final int PACKET_SEND_CHAT = 0;
	public static final int PACKET_SERVER_INFO = 0;
	
	//Client Commands
	public static final int PACKET_PREREGISTER_CONNECTION = 160;
	public static final int PACKET_HEART_BEAT_RESPONSE = 0;
	public static final int PACKET_ADD_CHAT = 0;
	public static final int PACKET_PLAYER_INFO = 0;
	
	//Game Commands
	public static final int PACKET_ADD_GAMECOMMAND = 20;
	public static final int PACKET_TICK = 10;
	public static final int PACKET_SYNC_CHECKSUM = 35;
}
