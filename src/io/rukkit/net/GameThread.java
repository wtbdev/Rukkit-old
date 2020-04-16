package io.rukkit.net;

import io.rukkit.command.*;
import io.rukkit.entity.PlayerController;
import io.rukkit.net.*;
import io.rukkit.util.*;
import java.io.*;
import java.util.*;
import io.rukkit.net.GameThread.*;

//The base game thread
public class GameThread
{

	public boolean isGaming = false;
	public boolean isReadying = false;
	public ArrayList<PlayerThread> clients = new ArrayList<PlayerThread>();
	public volatile LinkedList<GameCommand> commandQuere = new LinkedList<GameCommand>();
	public PlayerController player = new PlayerController();
	private final Logger log = new Logger("GameThread");

	private class StartTask implements Runnable
	{

		private GameThread.RandyTask randyTask;

		@Override
		public void run()
		{
			if (player.totalPlayers() < 2)
			{
				sendSystemBoardcast("一人无法开始游戏！");
				return;
			}
			isReadying = true;
			isGaming = true;
			for (int i = 5;i >= 0;i--)
			{
				if (!isGaming)
				{
					break;
				}
				sendSystemBoardcast("游戏将在 " + i + "秒后启动...");
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{}
			}
			if (!isGaming)
			{
				sendSystemBoardcast("游戏准备被停止！");
				return;
			}
			for (PlayerThread s : clients)
			{
				try
				{
					s.startGame();
					randyTask = new RandyTask();
					new Timer().schedule(randyTask, 0, 100);
					isReadying = false;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			// TODO: Implement this method
		}


	}

	private class GameTickTask extends TimerTask
	{
		public GameTickTask()
		{
			time = 0;
			resetTime = 0;
		}

		private int resetTime = 0;
		private int time = 0;
		@Override
		public void run()
		{
			time += 10;
			log.i(clients.size());
			log.i(resetTime);
			if (player.totalPlayers() <= 0)
			{
				resetTime = 600;
			}
			if (player.totalPlayers() <= 1)
			{
				if (resetTime >= 600)
				{
					sendSystemBoardcast("游戏结束！");
					disconnectAll();
					isGaming = false;
					cancel();
				}
				if (resetTime == 0)
				{
					sendSystemBoardcast("提示： 服务器只剩你一人，1分钟后服务器将重置。要继续这盘游戏请保存存档到本地！");
				}
				resetTime += 2;
			}
			else
			{
				resetTime = 0;
			}
			if (commandQuere.size() != 0)
			{
				log.d("Command Tick sended");
				final GameCommand cmd = commandQuere.removeLast();
				for (final PlayerThread s : clients)
				{
					new Thread(new Runnable(){@Override public void run(){
					s.sendGameTickCommand(time, cmd);
					}}).start();
				}
			}
			else
			{
				for (final PlayerThread s : clients)
				{
					new Thread(new Runnable(){@Override public void run(){
								s.sendTick(time);
							}}).start();
					//player.fetchPlayer(s.threadIndex).playerCredits += 36;
				}
			}
			// TODO: Implement this method
		}
	}

	private class RandyTask extends TimerTask
	{

		private int loadTime = 0;
		private GameThread.GameTickTask gameTickTask;
		@Override
		public void run()
		{
			if (player.isAllRandy())
			{
				gameTickTask = new GameTickTask();
				new Timer().schedule(gameTickTask, 0, 200);
				sendSystemBoardcast("本服务端基于Rukkit Engine！");
				sendSystemBoardcast("请不要中途断线，将会无法同步！");
				cancel();
			}
			loadTime += 100;
			if (loadTime > 3000)
			{
				gameTickTask = new GameTickTask();
				new Timer().schedule(gameTickTask, 0, 150);
				sendSystemBoardcast("有玩家未准备好！");
				sendSystemBoardcast("本服务端基于Rukkit Engine！");
				sendSystemBoardcast("请不要中途断线，将会无法同步！");
				cancel();
			}
			// TODO: Implement this method
		}
	}


	public void disconnectAll()
	{
		for (PlayerThread s : clients)
		{
			s.disconnect();
		}
	}

	public void kickAll(String reason)
	{
		for (PlayerThread s : clients)
		{
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

	public void updateServerInfo()
	{
		for (PlayerThread s : clients)
		{
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

	public void startGame()
	{
		if (!isGaming)
		{
			new Thread(new StartTask()).start();
		}
	}

	public void sendBroadcast(String msg, String sendBy, int team)
	{
		for (PlayerThread s : clients)
		{
			s.sendChatMessage(msg, sendBy, team);
		}
	}

	public void sendPlayerBroadcast(String msg, String sendBy)
	{
		for (PlayerThread s : clients)
		{
			s.sendChatMessage(msg, sendBy, s.threadIndex);
		}
	}

	public void sendSystemBoardcast(String msg)
	{
		sendBroadcast(msg, "SERVER", 5);
	}
}
