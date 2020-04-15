package io.rukkit.net;
import io.rukkit.*;
import io.rukkit.entity.*;
import io.rukkit.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class PlayerThread implements Runnable
{
	class heartBeatTask extends TimerTask{

		private DataOutputStream out;
		public void setStream(DataOutputStream stream){
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

	class TeamTask extends TimerTask{

		private DataOutputStream out;
		public void setStream(DataOutputStream stream){
			this.out = stream;
		}

		@Override
		public void run()
		{
			// TODO: Implement this method
			GameOutputStream o = new GameOutputStream();
			try
			{
				if(threadIndex > ServerProperties.maxPlayer - 1){
					o.writeInt(PlayerUtil.fetchPlayer(threadIndex).playerIndex);
				}else{
					o.writeInt(threadIndex); //位置
				}
				o.writeInt(ServerProperties.maxPlayer); //最大玩家
				GzipEncoder enc = o.getEncodeStream("teams");
				for(int i =0;i<ServerProperties.maxPlayer;i++){
					Player player = PlayerUtil.fetchPlayer(i);
					enc.stream.writeBoolean(player != null);
					if(player == null)continue;
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
	private int threadIndex;
	private int tryTimes = 0;
	private heartBeatTask heartBeatTask;
	private TeamTask teamTask;
	
	public PlayerThread(Socket sock){
		this.client = sock;
	}

	@Override
	public void run()
	{
		// TODO: Implement this method
	}
	
	public void sendPacket(Packet p) throws IOException{
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		out.writeInt(p.bytes.length);
		out.writeInt(p.type);
		out.write(p.bytes);
		out.flush();
	}
	
}
