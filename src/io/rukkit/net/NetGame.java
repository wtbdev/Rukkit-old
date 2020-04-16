package io.rukkit.net;

import io.rukkit.command.*;
import java.io.*;
import java.util.*;
import io.rukkit.util.*;
import java.net.*;

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
		catch (IOException e)
		{
			log.e("Can't start Service");
			e.printStackTrace();
		}
	}

}
