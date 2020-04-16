package io.rukkit.net;
import io.rukkit.*;
import io.rukkit.command.*;
import io.rukkit.entity.*;
import io.rukkit.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class PlayerThread implements Runnable
{

	class HeartBeatTask extends TimerTask
	{

		private DataOutputStream out;
		public HeartBeatTask() throws IOException
		{
			this.out = new DataOutputStream(client.getOutputStream());
		}

		@Override
		public void run()
		{
			// TODO: Implement this method
			GameOutputStream o = new GameOutputStream();
			try
			{
				log.d("Send Heartbeat packet(Trytimes = " + tryTimes);
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
				log.w("Cannot send Heartbeat.Client is disconnected.");
				tryTimes += 10;
				cancel();
			}
		}
	};

	class TeamTask extends TimerTask
	{

		private DataOutputStream out;

		public TeamTask() throws IOException
		{
			this.out = new DataOutputStream(client.getOutputStream());
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
					o.writeInt(Rukkit.thread.player.fetchPlayer(threadIndex).playerIndex);
				}
				else
				{
					o.writeInt(threadIndex); //位置
				}
				o.writeInt(ServerProperties.maxPlayer); //最大玩家
				GzipEncoder enc = o.getEncodeStream("teams");
				for (int i =0;i < ServerProperties.maxPlayer;i++)
				{
					Player player = Rukkit.thread.player.fetchPlayer(i);
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

				Packet p = o.createPacket(PacketType.PACKET_TEAM_LIST);

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
	private HeartBeatTask heartBeatTask;
	private TeamTask teamTask;
	public Logger log = new Logger("UnknownPlayer");
	public int threadIndex;

	public PlayerThread(Socket sock) throws IOException
	{
		this.client = sock;
		heartBeatTask = new HeartBeatTask();
		teamTask = new TeamTask();
	}

	@Override
	public void run()
	{
		try
		{
			while (true)
			{
				DataInputStream in = new DataInputStream(client.getInputStream());
				//Player timed out OR diconnected
				if (tryTimes > 10)
				{
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
				doActions(packet);
			}
		}
		catch (IOException e)
		{

		}
	}

	private void doActions(Packet p) throws IOException
	{
		switch (p.type)
		{
				//PreRegister
			case PacketType.PACKET_PREREGISTER_CONNECTION:
				registerConnection(p);
				break;
				//GetPlayerData
			case PacketType.PACKET_PLAYER_INFO:
				if (getPlayerInfo(p))
				{
					new Timer().schedule(heartBeatTask, 0, 2000);
					new Timer().schedule(teamTask, 0, 1000);
				}
				else
				{
					disconnect();
				}
				break;
			case PacketType.PACKET_HEART_BEAT_RESPONSE:
				this.tryTimes = 0;
				//sendSystemMessage("Have a try");
				break;
			case PacketType.PACKET_ADD_CHAT:
				log.d("Chat Received");
				receiveChat(p);
				break;	
			case PacketType.PACKET_DISCONNECT:
				disconnect();
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
			Packet p = o.createPacket(PacketType.PACKET_SEND_CHAT);
			out.writeInt(p.bytes.length);
			out.writeInt(p.type);
			out.write(p.bytes);
			out.flush();
		}
		catch (IOException e)
		{
		}
	}

	public void sendSystemMessage(String msg)
	{
		sendChatMessage(msg, "SERVER", 5);
	}

	public void sendServerInfo() throws IOException
	{
		GameOutputStream o = new GameOutputStream();
		o.writeString("com.corrodinggames.rts");
		o.writeInt(136);
		o.writeInt(0);
		o.writeString(ServerProperties.mapName);
		o.writeInt(0);
		o.writeInt(2);
		o.writeBoolean(true);

		o.writeInt(1);
		o.writeByte(6);
		o.writeBoolean(false);
		o.writeBoolean(false);
		o.writeInt(250);
		o.writeInt(250);

		o.writeInt(1);
		o.writeFloat(ServerProperties.income);
		o.writeBoolean(false);
		o.writeBoolean(false);
		o.writeBoolean(true);

		GzipEncoder out = o.getEncodeStream("customUnits");
		out.stream.writeInt(1);
		out.stream.writeInt(78);
		BufferedReader reader = new BufferedReader(new FileReader(ServerProperties.unitPath));
		String b = null;
		while((b = reader.readLine()) != null){
			String unitdata[] = b.split("%#%");
			out.stream.writeUTF(unitdata[0]);
			out.stream.writeInt(Integer.parseInt(unitdata[1]));
			out.stream.writeBoolean(true);
			out.stream.writeBoolean(false);
			out.stream.writeLong(0);
			out.stream.writeLong(0);
		}

		o.flushEncodeData(out);

		o.writeBoolean(false);
		o.writeBoolean(false);
		o.writeBoolean(false);

		sendPacket(o.createPacket(106));
	}

	public void sendKick(String reason) throws IOException
	{
		// TODO: Implement this method
	}

	public void disconnect()
	{
		log.d("Disconnecting");
		Rukkit.thread.clients.remove(this);
		Rukkit.thread.player.deletePlayer(threadIndex);
		try {
			this.client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void receiveChat(Packet p) throws IOException{
		ByteArrayInputStream by = new ByteArrayInputStream(p.bytes);
		DataInputStream n = new DataInputStream(by);
		String message = n.readUTF();
		/*if(message.startsWith(".") || message.startsWith("-") || message.startsWith("_")){
			CommandUtil.executeCommand(message.substring(1), this);
		}else{
			
		}*/
		Rukkit.thread.sendBroadcast(message, Rukkit.thread.player.fetchPlayer(threadIndex).playerName, threadIndex);
	}

	private void receiveCommand(Packet p) throws IOException{
		GameInputStream in = new GameInputStream(p);
		byte[] tin = in.getDecodeBytes();
		GameCommand cmd = new GameCommand();
		cmd.sendBy = this.threadIndex;
		cmd.arr = tin;
		Rukkit.thread.commandQuere.addLast(cmd);
	}

	public void sendGameTickCommand(int tick, GameCommand cmd) throws IOException{
		GameOutputStream o = new GameOutputStream();
		o.writeInt(tick);
		o.writeInt(1);
		GzipEncoder enc = o.getEncodeStream("c");
		enc.stream.write(cmd.arr);
		//o.stream.write(cmd.arr);
		o.flushEncodeData(enc);
		sendPacket(o.createPacket(10));
	}

	public void sendTick(int tick) throws IOException{
		GameOutputStream o = new GameOutputStream();
		o.writeInt(tick);
		o.writeInt(0);
		sendPacket(o.createPacket(10));
	}

	public void startGame() throws IOException{
		teamTask.run();
		sendServerInfo();
		GameOutputStream o = new GameOutputStream();
		o.writeByte(0);
		o.writeInt(0);
		o.writeString("maps/skirmish/" + ServerProperties.mapName + ".tmx");
		sendPacket(o.createPacket(120));
		teamTask.cancel();
	}

	private boolean getPlayerInfo(Packet p) throws IOException
	{
		ByteArrayInputStream by = new ByteArrayInputStream(p.bytes);
		DataInputStream n = new DataInputStream(by);
		log.d(n.readUTF());
		log.d(n.readInt());
		log.d(n.readInt());
		log.d(n.readInt());
		String name = n.readUTF();
		if (Rukkit.thread.isGaming)
		{
			this.threadIndex = -1;
		}
		else
		{
			this.threadIndex = Rukkit.thread.player.addPlayer(name);
		}
		if (this.threadIndex == -1)
		{
			this.threadIndex = Rukkit.thread.player.addWatcher(name);
			sendSystemMessage("注意：您目前处于观战模式！");
			if (this.threadIndex == -1)
			{
				if (Rukkit.thread.isGaming)
				{
					sendKick("服务器彻底满了且已经开始游戏！" + "剩余玩家：" + Rukkit.thread.player.totalPlayers() + "人！\n(Powered by Rukkit)");
				}
				else
				{
					sendKick("服务器彻底满了！");
				}
				return false;
			}
		}
		if (Rukkit.thread.isGaming)
		{
			sendSystemMessage("游戏已经开始！剩余玩家：" + Rukkit.thread.player.totalPlayers() + "人！\n(Powered by Rukkit)");
		}
		this.log = new Logger("Player(index=" + this.threadIndex + ")");
		log.i(Rukkit.thread.player.fetchPlayer(threadIndex).playerName);
		log.d(n.readByte());
		log.i(n.readUTF());
		//PlayerID
		log.i((n.readUTF()));
		log.d(n.readInt());
		log.d(n.readUTF());
		return true;
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
