package nankai.oram.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random; 

import javax.crypto.SecretKey;
 


public class Util {
	
	public static boolean debug = false;
	public static int writeNumber = 0; //
	public static int cloudtocloud = 0; //
	public static long bandwidth = 0; //
	public static long bandwidthMini = 0; //
	public static long bandwidth_worst = 0; //
	public static long bandwidthMini_worst = 0; //
	public static long cloudcloudbandwidth = 0; //
	public static long readbandwidth = 0; //
	public static int readNumber = 0; //
	public static int worstShuffle = 0; // 
	public static int[] part_bigger = new int[100]; //

	public static int filleLevels = 0; //  

	public static void copy(byte[] src, byte[] des, int desPos, int len)
	{
		byte[] b = new byte[len]; 
		if (len != src.length)
			System.arraycopy(src, 0, b, len-src.length, src.length);  
		System.arraycopy(src, 0, b, len-src.length, src.length);  

		System.arraycopy(b, 0, des, desPos, len);
	}

	public static int byteToInt(byte[] src, int pos, int len)
	{
		int ret = 0;
		for(int i =0; i< len; i++){
			ret += (src[pos+i]&0xff) << (24-8*i);
		}
		return ret;
	}

	public static void intToByte(byte[] dest, int pos, int value) { 
		dest[pos+3] = (byte) (value & 0xff);
		dest[pos+2] = (byte) (value >> 8 & 0xff);
		dest[pos+1] = (byte) (value >> 16 & 0xff);
		dest[pos+0] = (byte) (value >> 24 & 0xff);
	}
	
	public static int rand_int(int maxNum)
	{
		Random rnd=new Random(); 
		if (maxNum<=0) maxNum=1;
		return Math.abs(rnd.nextInt(maxNum)); 
	}
	
	public static byte[] generateDummyData( int len)
	{
		byte[] bData=new byte[len];
//		Random rnd=new Random();
//		rnd.nextBytes(bData);
		for (int i=0;i<len;i++)
			bData[i]=-1;
		return bData;
	}

	public static byte[] generateSessionData( int len)
	{
		byte[] bData=new byte[len];
		Random rnd=new Random();
		rnd.nextBytes(bData); 
		return bData;
	}

	public static byte[] generateIV(int dummyID) {
		byte[] iv=new byte[16];
		Util.intToByte(iv, 0, dummyID); 
		for (int i=4;i<16;i++)
		{
			iv[i] = 0;
		}
		return iv;
	}
	
	public static int fpeForPermution(int inData, SecretKey sfk, int modular)
	{
		SymmetricCypto scp =new SymmetricCypto(CommInfo.keySize);
		scp.initEnc(sfk, null);	
		byte[] bData = new byte[CommInfo.keySize];
		for (int j=0; j<CommInfo.keySize; j++)
			bData[j]=(byte) inData;
		scp.enc_decData(bData, CommInfo.keySize);
		int ret = Math.abs(  Util.byteToInt(bData, 0, 4) );
		return ret % modular; 
	}
	 
//	public static byte[] hexStringToByte(String hex) {   
//	    int len = (hex.length() / 2);   
//	    byte[] result = new byte[len];   
//	    char[] achar = hex.toCharArray();   
//	    for (int i = 0; i < len; i++) {   
//	     int pos = i * 2;   
//	     result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));   
//	    }   
//	    return result;   
//	}  
//	  
//	private static byte toByte(char c) {   
//	    byte b = (byte) "0123456789ABCDEF".indexOf(c);   
//	    return b;   
//	}  
//	  
//	/** *//**  
//	    * 把字节数组转换成16进制字符串  
//	    * @param bArray  
//	    * @return  
//	    */   
//	public static final String bytesToHexString(byte[] bArray) {   
//	    StringBuffer sb = new StringBuffer(bArray.length);   
//	    String sTemp;   
//	    for (int i = 0; i < bArray.length; i++) {   
//	     sTemp = Integer.toHexString(0xFF & bArray[i]);   
//	     if (sTemp.length() < 2)   
//	      sb.append(0);   
//	      sb.append(sTemp.toUpperCase());   
//	    }   
//	    return sb.toString();   
//	}  
}
