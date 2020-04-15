package io.rukkit.net;

import io.rukkit.command.*;
import java.util.*;

public class GameThread implements Runnable
{

	public static boolean isGaming = false;
	public static boolean isReadying = false;
	public static ArrayList<GameThread> clients = new ArrayList<GameThread>();
	public static LinkedList<GameCommand> commandQuere = new LinkedList<GameCommand>();
	
	@Override
	public void run()
	{
		// TODO: Implement this method
	}
	
}
