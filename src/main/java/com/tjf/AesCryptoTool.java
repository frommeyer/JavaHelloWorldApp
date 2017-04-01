package com.tjf;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
//import java.util.Base64;
 


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesCryptoTool {
	private SecretKeySpec secretKey;
	private byte[] key;
	private String keyAsString;
	public static String SHA1 = "SHA-1";
	public static String SHA256 = "SHA-256";
	public static String SHA384 = "SHA-384";
	public static String SHA512 = "SHA-512";
	
	public AesCryptoTool(String keyStr) {
		this.keyAsString = keyStr;
		this.setKey(keyAsString);
	}
	
	public void setKey(String myKey) 
	{
		MessageDigest sha = null;
		
		try {
			this.key = myKey.getBytes("UTF-8");
			sha = MessageDigest.getInstance(AesCryptoTool.SHA1);
			this.key = sha.digest(key);
			key = Arrays.copyOf(key, 16); 
			this.secretKey = new SecretKeySpec(key, "AES");
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return;
	}
 
	public String encrypt(String strToEncrypt)//, String secret) 
	{
		String retVal = null;
		
		try
		{
			//this.setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			retVal = Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		} 
		catch (Exception e) 
		{
			System.out.println("Error while encrypting: " + e.toString());
			retVal = null;
		}
		
		return retVal;
	}
 
	public String decrypt(String strToDecrypt) 
	{
		String retVal = null;
		
		try
		{
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			retVal = new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} 
		catch (Exception e) 
		{
			System.out.println("Error while decrypting: " + e.toString());
			retVal = null;
		}
		
		return retVal;
	}
	
	public String toString() {
		String nl = "\n";
		String tab = "\t";
		StringBuffer strBuf = new StringBuffer();

		strBuf.append("Key:" + tab + this.keyAsString + nl);
		strBuf.append("Algorithm:" + tab + this.secretKey.getAlgorithm() + nl);
		strBuf.append("Encoded:" + tab + this.secretKey.getEncoded() + nl);
		strBuf.append("Format:" + tab + this.secretKey.getEncoded() + nl);

		return strBuf.toString();
	}
}
