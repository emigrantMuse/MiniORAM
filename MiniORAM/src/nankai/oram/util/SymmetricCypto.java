package nankai.oram.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class SymmetricCypto {
	  Cipher cp;
	  int keySize; 
	  static final String CIPHER_ALGORITHM_CBC_NoPadding = "AES/CBC/NoPadding";  
	  
	  byte[] iv;
	  public SymmetricCypto(int keysize)
	  {
	      try {
			cp=Cipher.getInstance(CIPHER_ALGORITHM_CBC_NoPadding);
			keySize=keysize; 
			iv=new byte[16];
			for (int i=0;i<16;i++)
				iv[i]=0;
		} catch (NoSuchAlgorithmException e) { 
			e.printStackTrace();
		} catch (NoSuchPaddingException e) { 
			e.printStackTrace();
		}
	  }
	  
	  
	public void initEnc(SecretKey sk, byte[] IV) {
		try {
			cp.init(Cipher.ENCRYPT_MODE, sk, new IvParameterSpec( (IV==null)?iv:IV ));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	  public void initDec(SecretKey sk, byte[] IV)
	  {
		try {
			cp.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec( (IV==null)?iv:IV ));
		} catch (Exception e) { 
			e.printStackTrace();
		}
	  }
	  
	  public void enc_decData(byte[] bData, int len)
	  {
		  int blockNumber = len/keySize;
		  for (int i=0;i<blockNumber;i++)
		  {
			  try {  
				cp.doFinal(bData, i*keySize, keySize, bData, i*keySize);
			} catch (Exception e) { 
				e.printStackTrace();
			}
		  }
	  } 

}
