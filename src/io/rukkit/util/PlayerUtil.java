package io.rukkit.util;
import io.rukkit.entity.*;
import java.util.*;
import io.rukkit.*;
import io.rukkit.net.*;

public class PlayerUtil
{
	private static Player[] players = new Player[ServerProperties.maxPlayer + ServerProperties.maxWatcher];
	public static Player fetchPlayer(int index)
	{
		try
		{
			players[index].isNull = false;
		}
		catch (Exception e)
		{
			return null;
		}
		return players[index];
	}

	public static void addPlayer(Player p)
	{
		players[p.playerIndex] = p;
	}

	public static int addPlayer(String name)
	{
		for (int i=0;i < ServerProperties.maxPlayer;i++)
		{
			try
			{
				players[i].isNull = false;
			}
			catch (Exception e)
			{
				Player player = new Player();	
				player.isNull = false;
				player.playerIndex = i;
				player.playerName = name;
				player.playerCredits = 4000;
				if (totalPlayers() % 2 == 0)
				{
					player.playerTeam = 0;
				}
				else
				{
					player.playerTeam = 1;
				}
				if (totalPlayers() == 0)
				{
					player.isAdmin = true;
					/*Rukkit.thread.sendSystemBoardcast("玩家 " + player.playerName +
											" 你是管理员了！输入 .start 来开始游戏！");*/
				}
				players[i] = player;
				return i;
			}
		}

		return -1;
	}
	
	public static int addWatcher(String name){
		for (int i=ServerProperties.maxPlayer;i < players.length;i++)
		{
			try
			{
				players[i].isNull = false;
			}
			catch (Exception e)
			{
				Player player = new Player();	
				player.isNull = false;
				player.playerIndex = 0;
				player.playerName = name;
				player.playerTeam = 1;
				players[i] = player;
				return i;
			}
		}
		return -1;
	}

	public static int totalPlayers()
	{
		int count = 0;
		for (int i=0;i < ServerProperties.maxPlayer;i++)
		{
			try
			{
				players[i].isNull = false;
				count ++;
			}
			catch (Exception e)
			{}
		}
		return count;
	}

	public static int totalWatchers()
	{
		int count = 0;
		for (int i=ServerProperties.maxPlayer - 1;i < players.length;i++)
		{
			try
			{
				players[i].isNull = false;
				count ++;
			}
			catch (Exception e)
			{}
		}
		return count;
	}
	
	public static void setPlayer(Player p)
	{
		players[p.playerIndex] = p;
	}

	public static boolean isAllRandy()
	{
		for (int i=0;i < ServerProperties.maxPlayer;i++)
		{
			try
			{
				players[i].isNull = false;
				if (!players[i].isRandy)
				{
					return false;
				}
			}
			catch (Exception e)
			{
				continue;
			}
		}

		return true;
	}

	/* Uncompleted
	public static void disconnectPlayer(int index)
	{
		try{
			if(players[index].isAdmin){
				for(PlayerThread s : Rukkit.thread.clients){
					Player p = fetchPlayer(s.threadIndex);
					p.isAdmin = true;
					Rukkit.thread.sendSystemBoardcast("玩家 " + p.playerName +" 成为了新管理员！");
					players[index].isAdmin = false;
					break;
				}
			}
		}catch(Exception e){}
		players[index] = null;
	}*/
	
	public static void deletePlayer(int index)
	{
		players[index] = null;
	}
}