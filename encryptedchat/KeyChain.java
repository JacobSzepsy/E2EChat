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

	public String encrypt(String in) throws Exception{
		cipher.init(Cipher.ENCRYPT_MODE, publickey);
		byte[] input = in.getBytes();
		cipher.update(input);
		byte[] cipherText = cipher.doFinal();
		return new String(cipherText, "UTF8");
	}

	public String decrypt(String in) throws Exception{
		byte[] cipherText = in.getBytes();
		cipher.init(Cipher.DECRYPT_MODE, privatekey);
		byte[] decipheredText = cipher.doFinal(cipherText);
		return new String(decipheredText, "UTF9");
	}

	public String getPublic() throws Exception{
		String s = new String(publickey.getEncoded());
		return Base64.getEncoder().encodeToString(s.getBytes());
	}

	public void setPublic(String key) throws Exception{
		try{
		byte[] bytes = Base64.getDecoder().decode(key);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		publickey = factory.generatePublic(keySpec);
		}catch(Exception e){
			System.out.println(e);
		}
	}
}
