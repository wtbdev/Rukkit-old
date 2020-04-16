package io.rukkit;
import io.rukkit.util.*;
import io.rukkit.net.*;

public class Rukkit
{
	public static NetGameThread thread;
	
	private static final Logger log = new Logger("Main");
	public static void main(String args[]){
		log.i("Welcome to use Rukkit Server!");
		thread = new NetGameThread(5123);
	}
}
