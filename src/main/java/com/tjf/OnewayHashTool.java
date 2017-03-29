/*
 * Uses message digest to create a oneway hash
 * can use MD2, MD5, SHA-1, SHA-256, SHA-384 and SHA-512 algorithms
 */
package com.tjf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class OnewayHashTool {
	private String data;
	private MessageDigest messageDigest;
	
	public static String SHA1 = "SHA-1";
	public static String SHA256 = "SHA-256";
	public static String SHA384 = "SHA-384";
	public static String SHA512 = "SHA-512";
	public static String MD5 = "MD5";
	
	public OnewayHashTool(String dataToHash) {
		this.data = dataToHash;
		this.messageDigest = null;
	}
	
	public String owHash(String inAlgo) {
		String retVal = null;
		String myAlgo = inAlgo;
        StringBuffer stringBuf= null;
        byte[] messageDigestAsArray;
        
		if(null == myAlgo) myAlgo = OnewayHashTool.SHA256;
		
		//begin hashing
        try {
            stringBuf = new StringBuffer();
            this.messageDigest = MessageDigest.getInstance(myAlgo);
            
            this.messageDigest.update(this.data.getBytes());
            messageDigestAsArray = messageDigest.digest();

            for (byte bytes : messageDigestAsArray) {
                stringBuf.append(String.format("%02x", bytes & 0xff));
            }
            
            System.out.println("Digest Contents: " + this.messageDigest);
            System.out.println("OnewayHashTool: " + this.toString());
            
            retVal = stringBuf.toString();
            System.out.println("digested" + myAlgo + "(hex):" + retVal);
        } 
        catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
            retVal = null;
        }		
		
		return retVal;
	}
	
	public String toString() {
		String nl = "\n";
		String tab = "\t";
		StringBuffer strBuf = new StringBuffer();
		
		strBuf.append(nl + "Message:" + tab + this.data + nl);
		strBuf.append("Digest Contents:" + tab + this.messageDigest + nl);
		strBuf.append("Algorithm:" + tab + this.messageDigest.getAlgorithm() + nl);
		strBuf.append("Length:" + tab + this.messageDigest.getDigestLength() + nl);
		strBuf.append("Provider:" + tab + this.messageDigest.getProvider() + nl);

		return strBuf.toString();
	}
}
