package io.rukkit.net;

import io.rukkit.command.*;
import java.io.*;
import java.util.*;
import io.rukkit.util.*;
import java.net.*;
import io.rukkit.*;

public class NetGame extends GameThread
{
	private final Logger log = new Logger("NetGameThread");
	private int networkPort;

	public NetGame(int port)
	{
		this.networkPort = port;
	}

	public void run()
	{
		
		new Thread(new Runnable(){
				public void run(){
					try
					{
						log.i("Staring publisher service...");
						Demo.main(networkPort);
					}
					catch (Exception e)
					{}
				}
			}).start();
		log.i("Thread starting on port " + networkPort + "...");
		try
		{
			ServerSocket server = new ServerSocket(this.networkPort);
			while (true)
			{
				Socket sock = server.accept();
				PlayerThread thread = new PlayerThread(sock);
				clients.add(thread);
				new Thread(thread).start();
			}
		}
		catch (Exception e)
		{
			log.e("Can't start Service");
			e.printStackTrace();
		}
	}

}
