package io.rukkit.net;
import io.rukkit.*;
import io.rukkit.entity.*;
import io.rukkit.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class PlayerThread implements Runnable
{

	class heartBeatTask extends TimerTask
	{

		private DataOutputStream out;
		public void setStream(DataOutputStream stream)
		{
			this.out = stream;
		}

		@Override
		public void run()
		{
			// TODO: Implement this method
			GameOutputStream o = new GameOutputStream();
			try
			{
				o.writeLong(new Random().nextLong());
				o.writeByte(0);
				Packet p = o.createPacket(108);
				out.writeInt(p.bytes.length);
				out.writeInt(p.type);
				out.write(p.bytes);
				out.flush();
				//尝试次数加一
				tryTimes += 1;
			}
			catch (IOException e)
			{
				cancel();
			}
		}
	};

	class TeamTask extends TimerTask
	{

		private DataOutputStream out;
		public void setStream(DataOutputStream stream)
		{
			this.out = stream;
		}

		@Override
		public void run()
		{
			// TODO: Implement this method
			GameOutputStream o = new GameOutputStream();
			try
			{
				if (threadIndex > ServerProperties.maxPlayer - 1)
				{
					o.writeInt(PlayerUtil.fetchPlayer(threadIndex).playerIndex);
				}
				else
				{
					o.writeInt(threadIndex); //位置
				}
				o.writeInt(ServerProperties.maxPlayer); //最大玩家
				GzipEncoder enc = o.getEncodeStream("teams");
				for (int i =0;i < ServerProperties.maxPlayer;i++)
				{
					Player player = PlayerUtil.fetchPlayer(i);
					enc.stream.writeBoolean(player != null);
					if (player == null)continue;
					enc.stream.writeInt(0);
					player.writePlayer(enc.stream);
				}
				o.flushEncodeData(enc);

				o.writeInt(2);
				o.writeInt(4);
				o.writeBoolean(true);
				o.writeInt(1);
				o.writeByte(4);
				o.writeInt(250);
				o.writeInt(250);

				o.writeInt(1);
				o.writeFloat(ServerProperties.income);
				o.writeBoolean(false);
				o.writeBoolean(false);
				o.writeBoolean(false);
				o.writeBoolean(false);

				Packet p = o.createPacket(115);

				sendPacket(p);
			}
			catch (IOException e)
			{
				cancel();
			}
		}
	};


	private Socket client;
	private int tryTimes = 0;
	private heartBeatTask heartBeatTask;
	private TeamTask teamTask;
	public Logger log;
	public int threadIndex;

	public PlayerThread(Socket sock)
	{
		this.client = sock;
	}

	@Override
	public void run()
	{
		try{
			while(true){
				DataInputStream in = new DataInputStream(client.getInputStream());
				//Player timed out OR diconnected
				if (tryTimes > 10){
					log.i("Player disconnected. (index=" + threadIndex + ")");
					disconnect();
					break;
				}
				//Receive packet
				int size = in.readInt();
				Packet packet = new Packet(in.readInt());
				packet.bytes = new byte[size];
				log.d("Got Packet, size:" + size);
				log.d("Packet type：" + packet.type);
				int bytesRead = 0;
				while (bytesRead < size)
				{
					int readIn = in.read(packet.bytes, bytesRead, size - bytesRead);
					if (readIn == -1)
					{
						break;
					}
					bytesRead += readIn;
				}
				//Do Actions
				
			}
		}catch(IOException e){
			
		}
	}

	private void doActions(Packet p) throws IOException{
		switch(p.type){
			//PreRegister
			case PacketType.PACKET_PREREGISTER_CONNECTION:
				registerConnection(p);
				break;
			//GetPlayerData
		}
	}
	
	public void sendPacket(Packet p) throws IOException
	{
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		out.writeInt(p.bytes.length);
		out.writeInt(p.type);
		out.write(p.bytes);
		out.flush();
	}


	public void sendChatMessage(String msg, String sendBy, int team)
	{
		try
		{
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			GameOutputStream o = new GameOutputStream();
			o.writeString(msg);
			o.writeByte(3);
			o.writeBoolean(true);
			o.writeString(sendBy);
			o.writeInt(team);
			o.writeInt(team);
			Packet p = o.createPacket(141);
			out.writeInt(p.bytes.length);
			out.writeInt(p.type);
			out.write(p.bytes);
			out.flush();
		}
		catch (IOException e)
		{
		}
	}

	public void sendServerInfo() throws IOException
	{
		// TODO: Implement this method
	}

	public void sendKick(String reason) throws IOException
	{
		// TODO: Implement this method
	}

	public void disconnect()
	{
		// TODO: Implement this method
	}

	private void registerConnection(Packet p) throws IOException
	{
		GameInputStream n = new GameInputStream(p);
		log.i("Client Packagename:" + n.readString());
		n.readInt();
		int version = n.readInt();
		log.i("Client Version" + version);
		n.readInt();
		GameOutputStream o = new GameOutputStream();
		o.writeString("com.corrodinggames.rts");
		o.writeInt(1);
		o.writeInt(version);
		o.writeInt(version);
		o.writeString("com.corrodinggames.rts");
		o.writeString("bad8b8ab-335e-475f-9953-7ac311be7f33");
		o.writeInt(114514);
		sendPacket(o.createPacket(PacketType.PACKET_REGISTER_CONNECTION));
	}
}
