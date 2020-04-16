package io.rukkit.entity;

import io.rukkit.Rukkit;
import io.rukkit.util.*;
import java.io.*;
import java.util.*;

public class Player
{
	public boolean isAdmin = false;
	public boolean isNull = true;
	public boolean isRandy = false;
	public String playerName = "unnamed";
	public int playerIndex = 0;
	public int playerTeam = 0;
	public int playerCredits = 4000;

	public void writePlayer(DataOutputStream stream) throws IOException{
		stream.writeByte(playerIndex);
		stream.writeInt(playerCredits);
		stream.writeInt(playerTeam);
		stream.writeBoolean(true);
		stream.writeUTF(playerName);
		stream.writeBoolean(true);

		//enc.stream.writeBoolean(true);
		if(isAdmin){
			stream.writeInt(-99);
		}else{
			stream.writeInt(50 + new Random().nextInt(200));
		}
		stream.writeLong(System.currentTimeMillis());

		stream.writeBoolean(false);
		stream.writeInt(0);

		stream.writeInt(playerIndex);
		stream.writeByte(0);
		stream.writeBoolean(false);
		stream.writeBoolean(false);
		stream.writeBoolean(false);
		stream.writeBoolean(false);
		stream.writeInt(-9999);
	}

	public boolean movePlayer(int index){
		Player player = Rukkit.thread.player.fetchPlayer(index);
		if(player != null)return false;
		if(index > 9 && index < 0){
			return false;
		}
		Rukkit.thread.player.deletePlayer(this.playerIndex);
		this.playerIndex = index;
		Rukkit.thread.player.addPlayer(this);
		return true;
	}

	public boolean moveTeam(int team){
		if(team > 9 && team < 0){
			return false;
		} else {
			this.playerTeam = team;
		}
		return true;
	}

	public boolean giveAdmin(int index){
		Player player = Rukkit.thread.player.fetchPlayer(index);
		if(index < 9 && index > 0 && player != null && this.isAdmin){
			player.isAdmin = true;
			this.isAdmin = false;
			return true;
		}
		return false;
	}
}
