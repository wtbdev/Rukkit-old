package io.rukkit.net;

import io.rukkit.command.*;
import io.rukkit.net.*;
import io.rukkit.util.*;
import java.io.*;
import java.util.*;

//The base game thread
public class GameThread
{

	public boolean isGaming = false;
	public boolean isReadying = false;
	public ArrayList<PlayerThread> clients = new ArrayList<PlayerThread>();
	public LinkedList<GameCommand> commandQuere = new LinkedList<GameCommand>();
	private final Logger log = new Logger("GameThread");

	public void disconnectAll(){
		for(PlayerThread s : clients){
			s.disconnect();
		}
	}

	public void kickAll(String reason){
		for(PlayerThread s : clients){
			try
			{
				s.sendKick(reason);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void updateServerInfo(){
		for(PlayerThread s : clients){
			try
			{
				s.sendServerInfo();
			}
			catch (IOException e)
			{
				log.e("updateServerInfo: 无法更新");
			}
		}
	}

	/* Uncompleted
	 public void startGame(){
	 if(!isGaming){
	 new Thread(new StartTask()).start();
	 }
	 }*/

	public void sendBroadcast(String msg, String sendBy, int team){
		for(PlayerThread s : clients){
			s.sendChatMessage(msg, sendBy, team);
		}
	}

	public void sendPlayerBroadcast(String msg, String sendBy){
		for(PlayerThread s : clients){
			s.sendChatMessage(msg, sendBy, s.threadIndex);
		}
	}

	public void sendSystemBoardcast(String msg){
		sendBroadcast(msg, "SERVER", 5);
	}
}
