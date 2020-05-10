package io.rukkit;

import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.net.*;


public class Demo {

	private static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyz";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36";

	private static String uuid;

	private static String token;

	private static int port;

	// 新版实现 随机40个长度
	public static String generateStr(int len){
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i <len ; i++) {
			sb.append(allChar.charAt(random.nextInt(allChar.length())));
		}
		return sb.toString();
	}
	
	static TimerTask UpdateTask = new TimerTask(){

		@Override
		public void run()
		{
			try
			{
				updateServer(uuid, token);
			}
			catch (Exception e)
			{
			}
			// TODO: Implement this method
		}
	};

	public static void main(int port2) throws Exception{


		// 原版实现-无效
		/*
		Random f385a = new Random();
		char[] k = new char[36];
		for (int i3 = 0; i3 < 10; i3++) {
				k[i3] = (char) (i3 + 48);
		}
		for (int i4 = 10; i4 < 36; i4++) {
			k[i4] = (char) ((i4 + 97) - 10);
		}
	
		StringBuilder sb = new StringBuilder();
		for (int i3 = 0; i3 < 40; i3++) {
			sb.append(f385a.nextInt(k.length));
		}
		System.out.println(sb.toString());
		*/
		System.out.println("Started!");
		token = generateStr(40);
		uuid = UUID.randomUUID().toString();
		port = port2;
		System.out.println(doPost("http://gs4.corrodinggames.net/masterserver/1.3/interface","action=self_info&port="+ port));
		StringBuffer sb = new StringBuffer();
		// 创建ADD
		sb.append("action=add")
		// UUID
		.append("&user_id=u_"+uuid)
		// 用户名前部
		.append("&game_name=unnamed")
		// 版本
		.append("&game_version=136")
		.append("&game_version_string=1.13.3")
		// tokenv
		.append("&private_token="+token)
		// token2
		.append("&private_token_2="+b(b(token)))
		// 验证
		.append("&confirm="+b("a"+b(token)))
		// 是否携带密码
		.append("&password_required=false")
		// 用户名全称
		.append("&created_by=RUKKIT")
		// 内网ip
		.append("&private_ip=192.168.0.100")
		// port
		.append("&port_number=" + port)
		// 地图名
		.append("&game_map=" + ServerProperties.mapName)
		// ?
		.append("&game_mode=skirmishMap")
		// ?
		.append("&game_status=battleroom")
		// 当前玩家数量
		.append("&player_count=" + Rukkit.thread.clients.size())
		// 最大
		.append("&max_player_count=" + (ServerProperties.maxPlayer + ServerProperties.maxWatcher));
		System.out.println(doPost("http://gs4.corrodinggames.net/masterserver/1.3/interface",sb.toString()));
		System.out.println("\n");
		System.out.println(uuid);
		new Timer().schedule(UpdateTask, 0, 15000);
		//1821142013121292023291120018232332629420113211553517171422233183023203412
		//
	}
	
	public static void updateServer(String uuid, String token) throws Exception{
		StringBuffer sb = new StringBuffer();
		String stat = "battleroom";
		if(Rukkit.thread.isGaming){
			stat = "ingame";
		}
		// 创建ADD
		sb.append("action=update")
			.append("&id="+ "u_"+uuid)
			.append("&private_token="+token)
			.append("&check_port=false")
			// UUID
			.append("&user_id=u_"+uuid)
			// 用户名前部
			.append("&game_name=SERVER")
			// 版本
			.append("&game_version=136")
			.append("&game_version_string=1.13.3")
			// token
			.append("&private_token="+token)
			// token2
			.append("&private_token_2="+b(b(token)))
			// 验证
			.append("&confirm="+b("a"+b(token)))
			// 是否携带密码
			.append("&password_required=false")
			// 用户名全称
			.append("&created_by=RUKKIT")
			// 内网ip
			.append("&private_ip=192.168.0.100")
			// port
			.append("&port_number=" + port)
			// 地图名
			.append("&game_map=" + ServerProperties.mapName)
			// ?
			.append("&game_mode=skirmishMap")
			// ?
			.append("&game_status=" + stat)
			// 当前玩家数量
			.append("&player_count=" + Rukkit.thread.clients.size())
			// 最大
			.append("&max_player_count=" + (ServerProperties.maxPlayer + ServerProperties.maxWatcher));
		System.out.println(doPost("http://gs4.corrodinggames.net/masterserver/1.3/interface",sb.toString()));
		System.out.println("\n");
		System.out.println(uuid);
	}

	public static String doPost(String url, String param) throws Exception {
		String result = "";
		URL realUrl = new URL(url);
		//打开和URL之间的连接
		URLConnection conn =  realUrl.openConnection();
		//设置通用的请求属性
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("User-Agent",USER_AGENT);
		//发送POST请求必须设置如下两行
		conn.setDoOutput(true);
		conn.setDoInput(true);
		//获取URLConnection对象对应的输出流
		PrintWriter out = new PrintWriter(conn.getOutputStream());
		//发送请求参数
		out.print(param);
		//flush输出流的缓冲
		out.flush();
		// 定义 BufferedReader输入流来读取URL的响应
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
		String line;
		while ((line = in.readLine()) != null) {
			result += "\n" + line;
		}
		return result;
	}

	public static String b(String str) {
		try {
			byte[] digest = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder(digest.length * 2);
			for (byte b2 : digest) {
				int b3 = b2 & 0xFF;
				if (b3 < 16) {
					sb.append('0');
				}
				sb.append(Integer.toHexString(b3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e2) {
			throw new RuntimeException("MD5 should be supported", e2);
		} catch (UnsupportedEncodingException e3) {
			throw new RuntimeException("UTF-8 should be supported", e3);
		}
	}

}
