package encryptedchat;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.net.*;
import java.io.*;
import java.lang.Math;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.*;
class Client{
	
	//timer task that checks for messages every second
	class GetMessage extends TimerTask{
		private String user;
		private String password;
		private String requested;
		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		private Timestamp timestamp = null;
		private KeyChain keys;

		GetMessage(String user, String password, String requested, KeyChain keys){
			this.user = user;
			this.password = password;
			this.requested = requested;
			this.keys = keys;
		}
		public void run(){
			Timestamp current = new Timestamp(System.currentTimeMillis());
			Message message = new Message();
			message.type = "getMessage";
			message.username = user;
			message.password = password;
			message.requestedUser = requested;
			if(timestamp != null){
				message.timestamp = sdf.format(timestamp);
			}
			try{
				Map map = Client.this.post(message);
				//System.out.println(response.get("messages"));
				ArrayList response = (ArrayList) map.get("messages");
				//System.out.println(ls);
				//response = (ArrayList) response.get(0);
				for(Object msg : response){
					ArrayList list = (ArrayList) msg;
					System.out.println("-------------------------");
					System.out.println(list);
					System.out.println("-------------------------");
					String timestamp = (String) ((ArrayList) list.get(0)).get(0);
					String user = (String) ((ArrayList) list.get(1)).get(0);
					System.out.println(timestamp + user);
					list = (ArrayList) ((ArrayList) list.get(2)).get(0);
					System.out.println(list);
					System.out.println(timestamp + " " + user + ": " + keys.decrypt(list));
				}
		//test = (ArrayList) test.get(0); //this is the actual byte array
		//Integer a = new Integer((int) Math.round((Double) test.get(0)));
		//byte b = a.byteValue();
			}catch(Exception e){
				System.out.println("ERROR: " + e);
			}
			timestamp = current;
		}
	}

	class Message{
		public String type;
		public String username;
		public String password;
		public String key;
		public String requestedUser;
		public String timestamp;
		public ArrayList<byte[]> message;
	}
	
	public static Map post(Message message){
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		Map map = null;
		String jsonString = gson.toJson(message);
		//System.out.println(jsonString);	
		try{
		URL url = new URL("https://web.njit.edu/~as2757/cs656/main.php");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		byte[] input = jsonString.getBytes("utf-8");
	        os.write(input, 0, input.length);           		
		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
		StringBuilder response = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null){
			response.append(line.trim());
		}
		String json = response.toString().substring(0,response.toString().length());
		
		
		map = gson.fromJson(json, Map.class);
		}catch(Exception e){
			System.out.println("error" + e);
		}
		return map;
	}

	public void startTimer(String username, String password, String requested, KeyChain keys){
		Timer timer = new Timer();
		TimerTask task = new GetMessage(username, password, requested, keys);
		timer.schedule(task, 0, 1000);
	}
	public static void main(String[] args){
		try{
			Client cl = new Client();
			cl.launch();
		}catch(Exception e){
			System.out.println(e);
		}
	}
		
	public void launch() throws Exception{
		Message message = new Message();
		KeyChain keys = new KeyChain();
		Scanner s = new Scanner(System.in);
		char c;
		String username, password, publickey;
		while(true){
			System.out.println("[L]ogin or [R]egister?");
			c = Character.toLowerCase(s.nextLine().charAt(0));
			if(c == 'l' || c == 'r'){
				break;
			}else{
				System.out.println("Invalid Input");
			}
		}
		while(true){
			System.out.print("Username: ");
			username = s.nextLine();
			System.out.print("Password: ");
			password = s.nextLine();
			message = new Message();
			Map map;
			if(c == 'l'){
				message.type = "login";
				message.username = username;
				message.password = password;
				map = post(message);
				if(map.get("auth").toString().equals("false")){
					System.out.println("Username or password incorrect");
				}else{
					break;
				}
			}else{
				message.type = "register";
				message.username = username;
				message.password = password;
				message.key = keys.getPublic();
				map = post(message);
				if(map.get("done").toString().equals("false")){
					System.out.println("Username already taken");
				}else{
					break;
				}
			}

		}
		System.out.println("'!message <user>' to start a chat with a user");
		String command;
		String messageTarget = null;
		while(true){
			command = s.nextLine();
			if(command.charAt(0) == '!'){
				String[] params = command.substring(1).split(" ");
				System.out.println(Arrays.toString(params));
				if(params.length == 1 && params[0].equalsIgnoreCase("exit")){
					System.exit(0);
				}else if(params.length == 2 && params[0].equalsIgnoreCase("message")){
					message = new Message();
					message.type = "request";
					message.username = username;
					message.password = password;
					message.requestedUser = params[1];
					Map map = post(message);
					if(map.get("done").toString().equals("false")){
						System.out.println("Error connecting to that user");
					}else{
						try{
						System.out.println("before: " + keys.getPublic());
						keys.setPublic(map.get("pubkey").toString());
						System.out.println("after: " + keys.getPublic());
						messageTarget = params[1];
						Client cl = new Client();
						cl.startTimer(username, password, params[1], keys);
						}catch(Exception e){
							System.out.println(e);
						}
					}
				}
				//break up command into segments
				//check first segment (command)
				//check second segment (paramater) 
			}else{
				if(messageTarget != null){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					message = new Message();
					message.type = "sendMessage";
					message.username = username;
					message.password = password;
					message.requestedUser = messageTarget;
					message.message = keys.encrypt(command);
					message.timestamp = sdf.format(timestamp);
					//System.out.println(message.message);
					//System.out.println(keys.decrypt(message.message));
			
					String response = post(message).get("done").toString();
					if(response.equals("false")){
						System.out.println("Error sending message");
					}
				}else{
					System.out.println("Please specify a recipient before trying to send a message");
				}
			}
		}
	}
}
