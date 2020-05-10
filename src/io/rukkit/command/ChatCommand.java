package io.rukkit.command;
import io.rukkit.*;
import io.rukkit.entity.*;
import io.rukkit.map.*;
import io.rukkit.net.*;
import io.rukkit.util.*;
import java.io.*;
import java.util.*;

public class ChatCommand
{
	private static Logger log = new Logger("Command");
	private static AfkTask afkTask;
	private static class AfkTask extends TimerTask
	{
		int afkTime = 0;
		PlayerThread targetThread;
		
		public AfkTask(PlayerThread thread){
			this.targetThread = thread;
		}
		
		@Override
		public void run()
		{
			// TODO: Implement this method
			afkTime+=1;
			if(afkTime == 15){
				Rukkit.thread.sendSystemBoardcast("15 秒后转移管理员...");
			}
			if(afkTime == 30){
				Rukkit.thread.sendSystemBoardcast("管理员已转移！");
				Rukkit.thread.player.setAdmin(targetThread.threadIndex, true);
				cancel();
			}
		}
		
	}
	public static void executeCommand(String command, PlayerThread thread){
		log.d("Executed:" + command);
		String cmd[] = command.split(" ");
		try{
			switch(cmd[0]){
				case "qc":
					executeCommand(command.substring(4), thread);
					break;
				case "self_move":
					if(Rukkit.thread.isGaming){
						thread.sendSystemMessage("游戏已经启动！");
						return;
					}
					if(thread.threadIndex > ServerProperties.maxPlayer - 1){
						thread.sendSystemMessage("观战中不允许移动位置！");
						return;
					}
					log.d("Player Moved");
					try{
						Player player = Rukkit.thread.player.fetchPlayer(thread.threadIndex);
						if(player.movePlayer(Integer.parseInt(cmd[1]) - 1)){
							thread.threadIndex = Integer.parseInt(cmd[1]) - 1;
							thread.updateTeamList();
						}else{
							thread.sendSystemMessage("移动失败：有人在那个位置上或者你正在观战");
						}
					}catch(Exception e){
						e.printStackTrace();
					}
					break;
				case "self_team":
					if(Rukkit.thread.isGaming){
						thread.sendSystemMessage("游戏已经启动！");
						return;
					}
					if(thread.threadIndex > ServerProperties.maxPlayer - 1){
						thread.sendSystemMessage("观战中不允许移动队伍！");
						return;
					}
					log.i("Player team Moved");
					try{
						Player player = Rukkit.thread.player.fetchPlayer(thread.threadIndex);
						if(!player.moveTeam(Integer.parseInt(cmd[1]) - 1)){
							thread.sendSystemMessage("操作失败：队伍太大或太小或你正在观战");
						}else{
							thread.updateTeamList();
						}
					}catch(Exception e){
						e.printStackTrace();
					}
					break;
				case "t":
					for(PlayerThread s: Rukkit.thread.clients){
						Player player = Rukkit.thread.player.fetchPlayer(s.threadIndex);
						if(player.playerTeam == Rukkit.thread.player.fetchPlayer(thread.threadIndex).playerTeam){
							s.sendChatMessage("[团队消息] " + cmd[1], Rukkit.thread.player.fetchPlayer(thread.threadIndex).playerName, thread.threadIndex);
						}
					}
					break;
				case "start":
					if(Rukkit.thread.isGaming){
						thread.sendSystemMessage("游戏已经启动！");
						return;
					}
					if(Rukkit.thread.player.fetchPlayer(thread.threadIndex).isAdmin){
						Rukkit.thread.startGame();
					}else{
						thread.sendSystemMessage("你不是管理员！");
					}
					break;
				case "afk":
					afkTask = new AfkTask(thread);
					new Timer().schedule(afkTask, 0, 1000);
					Rukkit.thread.sendSystemBoardcast("AFK 倒数已开启...");
					break;
				case "break":
					afkTask.cancel();
					Rukkit.thread.sendSystemBoardcast("AFK 倒计时结束...");
					break;
				case "give":
					if(Rukkit.thread.player.fetchPlayer(thread.threadIndex).giveAdmin(Integer.parseInt(cmd[1]) - 1)){
						Rukkit.thread.sendSystemBoardcast("玩家 " + Rukkit.thread.player.fetchPlayer(thread.threadIndex).playerName +
												 "把权限给了玩家 " + Rukkit.thread.player.fetchPlayer(Integer.parseInt(cmd[1]) - 1).playerName);
					}else{
						thread.sendSystemMessage("玩家不存在或者你根本不是管理！");
					}
					break;
				case "maps":
					for(int i=OfficalMap.maps.length - 1;i>=0;i--){
						Rukkit.thread.sendSystemBoardcast(String.format("[%d] %s", i, OfficalMap.maps[i]));
					}
					Rukkit.thread.sendSystemBoardcast("======== 地图列表 =======");
					Rukkit.thread.sendSystemBoardcast("管理员发送 .map 地图序号 即可换图！");
					Rukkit.thread.sendBroadcast("-map", Rukkit.thread.player.fetchPlayer(thread.threadIndex).playerName, thread.threadIndex);
					break;
				case "map":
					if(Rukkit.thread.isGaming){
						thread.sendSystemMessage("游戏已经启动！");
						return;
					}
					if(Rukkit.thread.player.fetchPlayer(thread.threadIndex).isAdmin){
						ServerProperties.mapName = OfficalMap.maps[Integer.parseInt(cmd[1])];
						Rukkit.thread.updateServerInfo();
					}else{
						thread.sendSystemMessage("你不是管理员！");
					}
					break;
				case "income":
					if(Rukkit.thread.isGaming){
						thread.sendSystemMessage("游戏已经启动！");
						return;
					}
					if(Rukkit.thread.player.fetchPlayer(thread.threadIndex).isAdmin){
						ServerProperties.income = Float.parseFloat(cmd[1]);
						Rukkit.thread.updateServerInfo();
					}else{
						thread.sendSystemMessage("你不是管理员！");
					}
					break;
				case "auto_team":
					if(Rukkit.thread.isGaming){
						thread.sendSystemMessage("游戏已经启动！");
						return;
					}
					if(Rukkit.thread.player.fetchPlayer(thread.threadIndex).isAdmin){
						for(int i =0;i<ServerProperties.maxPlayer;i++){
							Player player = Rukkit.thread.player.fetchPlayer(i);
							if(player == null)continue;
							if(player.playerIndex % 2 == 0){
								player.playerTeam = 0;
							}else{
								player.playerTeam = 1;
							}
						}
					}else{
						thread.sendSystemMessage("你不是管理员！");
					}
					break;
				case "stop":
					if(!Rukkit.thread.isReadying){
						thread.sendSystemMessage("游戏已经启动或者不在准备中！");
						return;
					}
					Rukkit.thread.sendBroadcast("-stop", Rukkit.thread.player.fetchPlayer(thread.threadIndex).playerName, thread.threadIndex);
					Rukkit.thread.isGaming = false;
					break;
				case "help":
					thread.sendSystemMessage("\n===== 帮助 =====\n" +
											 "[管理员指令]\n" +
											 ".start 开始游戏\n" +
											 ".maps 显示地图列表\n" +
											 ".map 地图序号 切换地图\n" +
											 ".income 倍数 切换倍数\n" +
											 ".auto_team 自动分队\n" +
											 ".give 玩家位 把自己的管理给位置上的玩家\n" +
											 ".kick 玩家位 把某号玩家踢出\n" +
											 "[玩家指令]\n" +
											 ".stop 在游戏准备开始时停止游戏\n" +
											 ".stat 查看目前是否开始游戏\n" +
											 ".watch 玩家位 观战该玩家\n" +
											 ".unwatch 取消观战模式并回到玩家位\n" +
											 ".afk 获得管理\n" +
											 ".break 打破管理获得计时");
					break;
				case "stat":
					if(!Rukkit.thread.isGaming){
						thread.sendSystemMessage("游戏未开始！");
					}
					thread.sendSystemMessage("当前正在的游戏玩家人数：" + Rukkit.thread.player.totalPlayers());
					break;
				case "watch":
					if(Rukkit.thread.isGaming){
						thread.sendSystemMessage("游戏已经启动！");
						return;
					}
					if(thread.threadIndex > (ServerProperties.maxPlayer - 1)){
						if(Rukkit.thread.player.fetchPlayer(Integer.parseInt(cmd[1])-1) != null && thread.threadIndex != Integer.parseInt(cmd[1])-1){
							Rukkit.thread.player.fetchPlayer(thread.threadIndex).playerIndex = Integer.parseInt(cmd[1])-1;
							Rukkit.thread.player.fetchPlayer(thread.threadIndex).playerTeam = Rukkit.thread.player.fetchPlayer(Integer.parseInt(cmd[1])-1).playerTeam;
							thread.sendSystemMessage("您正在观战 " + Rukkit.thread.player.fetchPlayer(Integer.parseInt(cmd[1])-1).playerName + "！");
						}else{
							thread.sendSystemMessage("您想要观战的玩家不存在或者是你自己！");
						}
					}else{
						if(Rukkit.thread.player.totalWatchers() >= ServerProperties.maxWatcher){
							thread.sendSystemMessage("观战者已满！");
							return;
						}
						Player player = Rukkit.thread.player.fetchPlayer(thread.threadIndex);
						Rukkit.thread.player.deletePlayer(thread.threadIndex);
						thread.threadIndex =  Rukkit.thread.player.addWatcher(player.playerName);
						thread.sendSystemMessage("您已被切换至观战位！");
						if(Rukkit.thread.player.fetchPlayer(Integer.parseInt(cmd[1])-1) != null&& thread.threadIndex != Integer.parseInt(cmd[1])-1){
							Rukkit.thread.player.fetchPlayer(thread.threadIndex).playerIndex = Integer.parseInt(cmd[1])-1;
							Rukkit.thread.player.fetchPlayer(thread.threadIndex).playerTeam = Rukkit.thread.player.fetchPlayer(Integer.parseInt(cmd[1])-1).playerTeam;
							thread.sendSystemMessage("您正在观战 " + Rukkit.thread.player.fetchPlayer(Integer.parseInt(cmd[1])-1).playerName + "！");
						}else{
							thread.sendSystemMessage("您想要观战的玩家不存在或者是你自己！");
						}
					}
					break;
				case "unwatch":
					if(Rukkit.thread.isGaming){
						thread.sendSystemMessage("游戏已经启动！");
						return;
					}
					if(thread.threadIndex < ServerProperties.maxPlayer - 1){
						thread.sendSystemMessage("您已是非观战状态！");
						return;
					}
					Player player = Rukkit.thread.player.fetchPlayer(thread.threadIndex);
					int threadIndex =  Rukkit.thread.player.addPlayer(player.playerName);
					if(threadIndex == -1){
						thread.sendSystemMessage("玩家人数已满！");
						return;
					}
					Rukkit.thread.player.deletePlayer(thread.threadIndex);
					thread.threadIndex = threadIndex;
					thread.sendSystemMessage("取消观战成功！");
					break;
				case "autofix":
					Rukkit.thread.sendSystemBoardcast("有人启动了自检模式！");
					Rukkit.thread.sendSystemBoardcast("检查玩家存活状态...");
					for(PlayerThread s : Rukkit.thread.clients){
						try{
							s.ping();
						}catch(IOException e){
							s.disconnect();
						}
					}
					Rukkit.thread.sendSystemBoardcast("检查完成！");
					break;
				case "kick":
					if(Rukkit.thread.isGaming){
						thread.sendSystemMessage("游戏已经启动！");
						return;
					}
					if(Rukkit.thread.player.fetchPlayer(thread.threadIndex).isAdmin){
						Rukkit.thread.clients.get(Integer.parseInt(cmd[1])).sendKick("被房主踢出!");
					}
			}
		}catch(Exception e){
			thread.sendSystemMessage("参数有误！");
		}
	}
}
