import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Client {
	static Socket sfxSocket = null;
	static PrintWriter out = null;
	static BufferedReader in = null;
	static String username, text;
	public class Input extends Thread
	{
		Scanner s = new Scanner(System.in);
		public void run()
		{
			String input;
			while (true)
			{
				if((input = s.nextLine()) != null)
					 text = "[" + String.valueOf(System.currentTimeMillis()) + "]" + " " + username + ": " + input;
					out.println(text);
			}
		}
	}
	public static void main(String[] args) throws Exception
	{
		
		Scanner s = new Scanner(System.in);
		username = s.next();
		String ip = "73.150.68.111", output, input;
		int port = 1337;
		try {
			sfxSocket = new Socket(ip, port);
			out = new PrintWriter(sfxSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sfxSocket.getInputStream()));
		}finally{}
		new Client().new Input().start();
		while(true)
		{
			if(in.ready())
				System.out.println(in.readLine());
		}
		
	}
}
