package encryptedchat;

import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import java.util.Scanner;
import java.io.*;
import java.io.FileWriter;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.*;
public class KeyChain{
	private PublicKey publickey;
	private PrivateKey privatekey;
	private Cipher cipher;
	
	public KeyChain() throws Exception{
		start();
		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	}
	
	private void start() throws Exception{
		File privkey = new File("./private.key");
		File pubkey = new File("./public.key");
		KeyFactory factory = KeyFactory.getInstance("RSA");
		if(privkey.exists() && pubkey.exists()){
			ObjectInputStream inpriv = new ObjectInputStream(new BufferedInputStream(new FileInputStream("./private.key")));
			ObjectInputStream inpub = new ObjectInputStream(new BufferedInputStream(new FileInputStream("./public.key")));
			
			BigInteger modulus = (BigInteger) inpriv.readObject();
			BigInteger exponent = (BigInteger) inpriv.readObject();
			privatekey = factory.generatePrivate(new RSAPrivateKeySpec(modulus, exponent));
			inpriv.close();
			
			modulus = (BigInteger) inpub.readObject();
			exponent = (BigInteger) inpub.readObject();
			publickey = factory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
			inpub.close();
		}else{
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(2048);
			KeyPair pair = gen.generateKeyPair();
			publickey = pair.getPublic();
			privatekey = pair.getPrivate();
			RSAPublicKeySpec pub = factory.getKeySpec(publickey, RSAPublicKeySpec.class);
			RSAPrivateKeySpec priv = factory.getKeySpec(privatekey, RSAPrivateKeySpec.class);
			
			ObjectOutputStream outpriv = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("./private.key")));
			outpriv.writeObject(priv.getModulus());
			outpriv.writeObject(priv.getPrivateExponent());
			
			ObjectOutputStream outpub = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("./public.key")));
			outpub.writeObject(pub.getModulus());
			outpub.writeObject(pub.getPublicExponent());
			
			outpriv.close();
			outpub.close();
		}
	}

	public ArrayList<byte[]> encrypt(String in) throws Exception{
		int index = 0;
		String encryptedString = "";
		byte[] input = in.getBytes();
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		System.out.println("ENCRYPTION");
		System.out.println("input is " + input.length + "long");
		while(true){
			if(input.length <= 5){
				cipher.init(Cipher.ENCRYPT_MODE, publickey);
				cipher.update(input);
				byte[] cipherText = cipher.doFinal();
				String s = new String(cipherText, "US-ASCII");
				System.out.println("-------------------------");
				System.out.println("size after encrypt" + cipherText.length);
				System.out.println(s);
				System.out.println("-------------------------");
				//encryptedString += s;
				list.add(cipherText);
				break;
			}else{
				
				cipher.init(Cipher.ENCRYPT_MODE, publickey);
				
				byte[] temp = Arrays.copyOfRange(input, 0, 5);
				System.out.println(temp.length);
				System.out.println(new String(temp));
				cipher.update(temp);
				byte[] cipherText = cipher.doFinal();
				System.out.println("-------------------------");
				System.out.println("Size after encrypt" + cipherText.length);
				System.out.println(new String(cipherText));
				System.out.println("-------------------------");
				String s = new String(cipherText);
				//encryptedString += s + "@@@";
				list.add(cipherText);
				input = Arrays.copyOfRange(input, 6, input.length);
				System.out.println("input is " + input.length + "long");
			}

		}
		System.out.println("returning string");
		return list;
	}

	public String decrypt(ArrayList input) throws Exception{
		String decryptedString = "";
		for(Object outerObj : input){
			ArrayList bytes = (ArrayList) outerObj;
			byte[] cipherText = new byte[bytes.size()];
			int i = 0;
			for(Object innerObj : bytes){
				Double d = (Double) innerObj;
				Integer a = new Integer((int) Math.round(d));
				cipherText[i] = a.byteValue();
				i++;
			}
			cipher.init(Cipher.DECRYPT_MODE, privatekey);
			byte[] decipheredText = cipher.doFinal(cipherText);
			decryptedString += new String(decipheredText, "UTF8");
		}
		//System.out.println("decrypt");
		//System.out.println(in);
		//String[] strings = in.split("@@@");
		//String decryptedString = "";
		//System.out.println(strings.length + " many entries to decode");
		//for(String s : strings){
			//s = s.substring(1);
		//	System.out.println("-------------------------------");
		//	System.out.println(s.getBytes().length);
		//	System.out.println(s);
		//	System.out.println("-------------------------------");
		//
		//	byte[] cipherText = s.getBytes();
		//	cipher.init(Cipher.DECRYPT_MODE, privatekey);
		//	byte[] decipheredText = cipher.doFinal(cipherText);
		//	decryptedString += new String(decipheredText, "UTF8");
		//}
		return decryptedString;
	}

	public String getPublic() throws Exception{
		return Base64.getEncoder().encodeToString(publickey.getEncoded());
	}

	public void setPublic(String key) throws Exception{
		try{
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
		KeyFactory factory = KeyFactory.getInstance("RSA");
		publickey = factory.generatePublic(keySpec);
		}catch(Exception e){
			System.out.println(e);
		}
	}
}
