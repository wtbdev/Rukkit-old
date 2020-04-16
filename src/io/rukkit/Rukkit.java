package io.rukkit;
import io.rukkit.util.*;
import io.rukkit.net.*;

public class Rukkit
{
	public static NetGame thread;
	
	private static final Logger log = new Logger("Main");
	public static void main(String args[]){
		log.i("Welcome to use Rukkit Server!");
		ServerProperties.unitPath = System.getProperty("java.class.path") + "/unitmeta.conf";
		log.i(ServerProperties.unitPath);
		thread = new NetGame(5123);
		thread.run();
	}
}
