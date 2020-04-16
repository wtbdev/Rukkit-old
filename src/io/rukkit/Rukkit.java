package io.rukkit;
import io.rukkit.util.*;
import io.rukkit.net.*;

public class Rukkit
{
	public static NetGame thread;
	
	private static final Logger log = new Logger("Main");
	public static void main(String args[]){
		log.i("Welcome to use Rukkit Server!");
		//In Android;
		//ServerProperties.unitPath = "/sdcard/unitmeta.conf";
		//In JAR
		ServerProperties.unitPath = System.getProperty("java.class.path") + "/unitmeta.conf"
		log.d("Unit config path = " + ServerProperties.unitPath);
		thread = new NetGame(5123);
		thread.run();
	}
}
