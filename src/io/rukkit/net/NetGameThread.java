package io.rukkit.net;

import io.rukkit.command.*;
import java.io.*;
import java.util.*;
import io.rukkit.util.*;

public class NetGameThread extends GameThread implements Runnable{
	private final Logger log = new Logger("NetGameThread");
	private int networkPort;

	public NetGameThread(int port){
		this.networkPort = port;
	}
	
	@Override
	public void run()
	{
	}

}
