package nankai.oram.pir;

import java.math.BigInteger;
import java.util.Random;

import nankai.oram.util.CommInfo;
import nankai.oram.util.Util;

/**
 * We just provide a implementation of decimal string
 * 
 * To the real implementation, it should be HEX string which can 
 * 
 * @author Dell 
 */

public class PIRImpl {

	Paillier paillier;
	int plainBlockLen = 0;//length of input plaintext
	int cipherBlockLen = 0;//length of output ciphertext
	
	public PIRImpl(Paillier p)
	{
		paillier = p;
		plainBlockLen = p.getPlaintextLen();
		cipherBlockLen = p.getCiphertextLength();
	}
	
	public int getCipherBlockLen()
	{
		return this.cipherBlockLen;
	}

	public PIRImpl()
	{
		paillier = new Paillier();
		plainBlockLen = paillier.getPlaintextLen();
		cipherBlockLen = paillier.getCiphertextLength();
	}
	
	public int getCipherTextLen(int plainLen)
	{
		int ret = plainLen / this.plainBlockLen;
		if (plainLen%this.plainBlockLen >0)
			ret++;
		return ret*this.cipherBlockLen;
	}
	
//	//cipher_sub  跟它明文域上的逆元作cipher_add就行
//	public byte[] cipherSUB(byte[] oldVaule, byte[] newValue)
//	{
//		BigInteger oldV = new BigInteger(oldVaule);
//		BigInteger newV = new BigInteger(newValue);
//		BigInteger ret = paillier.cipher_add(newV, oldV.modInverse(paillier.n));
//		return ret.toByteArray();
//	}
//
//	public byte[] cipherADD(byte[] v1, byte[] v2)
//	{ 
//		return paillier.cipher_add(new BigInteger(v1), new BigInteger(v2)).toByteArray();
//	}
	/**
	 * The inputs are the plaintexts
	 * divide them into each block to encrypt
	 * 
	 * @param oldVaule
	 * @param newValue
	 * @return
	 */
	public byte[] changeValue(byte[] oldVaule, byte[] newValue)
	{
		/****
		 * 
		 * If old and new are zeros, or oldvalue is zero, we can special treat it???
		 * 
		 */
		
		if (newValue.length<CommInfo.blockSize)
		{
			byte[] newV = new byte[CommInfo.blockSize];
			System.arraycopy(newValue, 0, newV, CommInfo.blockSize-newValue.length, newValue.length);
			newValue=newV;
		}
		
		if (oldVaule.length<CommInfo.blockSize)
		{
			byte[] oldV = new byte[CommInfo.blockSize];
			System.arraycopy(oldVaule, 0, oldV, CommInfo.blockSize-oldVaule.length, oldVaule.length);
			oldVaule=oldV;
		}
		
		//byte[] bOne = this.generateOne();
		
		int srcLen = oldVaule.length;
		int encCount = srcLen/plainBlockLen;
		int i=0;
		int len = encCount * cipherBlockLen;
		boolean bHasLeft = srcLen % plainBlockLen >0;
		if (bHasLeft) 
			len += cipherBlockLen;

		byte[] cipher = new byte[ len ];
		byte[] bIn=new byte[plainBlockLen];
		byte[] bIn_0=new byte[plainBlockLen+1];

		BigInteger oldV = new BigInteger("0");
		BigInteger newV = new BigInteger("0");
		for (i=0;i<encCount;i++)
		{
			if ( oldVaule[i*plainBlockLen] <0 )
			{
				bIn_0[0]=0;
				System.arraycopy(oldVaule, i*plainBlockLen, bIn_0, 1, plainBlockLen);
				oldV = new BigInteger(bIn_0);
			}else{
				System.arraycopy(oldVaule, i*plainBlockLen, bIn, 0, plainBlockLen);
				oldV = new BigInteger(bIn);
			} 
			if ( newValue[i*plainBlockLen] <0 )
			{
				bIn_0[0]=0;
				System.arraycopy(newValue, i*plainBlockLen, bIn_0, 1, plainBlockLen);
				newV = new BigInteger(bIn_0);
			}else{
				System.arraycopy(newValue, i*plainBlockLen, bIn, 0, plainBlockLen);
				newV = new BigInteger(bIn);
			}
			BigInteger changeV = newV.subtract(oldV).mod(paillier.n);
			
			changeV = paillier.Encryption(changeV);
			//System.arraycopy(changeV.toByteArray(), 0, cipher, i*cipherBlockLen, cipherBlockLen);
			Util.copy(changeV.toByteArray(), cipher, i*cipherBlockLen, cipherBlockLen);
			
		}
		if (bHasLeft){ 
			int leftLen = srcLen-i*plainBlockLen;
			if ( oldVaule[i*plainBlockLen] <0 )
			{ 
				byte[] bIn_1 = new byte[leftLen+1];
				bIn_1[0]=0;
				System.arraycopy(oldVaule, i*plainBlockLen, bIn_1, 1, leftLen);
				oldV = new BigInteger(bIn_1);
			}else{
				byte[] bIn_1 = new byte[leftLen];
				System.arraycopy(oldVaule, i*plainBlockLen, bIn_1, 0, leftLen);
				oldV = new BigInteger(bIn_1);
			} 
			if ( newValue[i*plainBlockLen] <0 )
			{
				byte[] bIn_1 = new byte[leftLen+1];
				bIn_1[0]=0;
				System.arraycopy(newValue, i*plainBlockLen, bIn_1, 1, leftLen);
				newV = new BigInteger(bIn_1);
			}else{
				byte[] bIn_1 = new byte[leftLen];
				System.arraycopy(newValue, i*plainBlockLen, bIn_1, 0, leftLen);
				newV = new BigInteger(bIn_1);
			}
			BigInteger changeV = newV.subtract(oldV).mod(paillier.n);
			
			changeV = paillier.Encryption(changeV);
			//System.arraycopy(changeV.toByteArray(), 0, cipher, i*cipherBlockLen, cipherBlockLen);
			Util.copy(changeV.toByteArray(), cipher, i*cipherBlockLen, cipherBlockLen);
		}
		 
		return cipher; 
	}
	public byte[] PIRWriteBlock(byte[] oldVaule, byte[] changeCipher)
	{
		//divide into each block and cipher add

		if (changeCipher.length!=oldVaule.length)
			return null;

		byte[] b1=new byte[cipherBlockLen];
		byte[] b2=new byte[cipherBlockLen];
		
		int len = changeCipher.length;
		byte[] bRet = new byte[len];
		int blocksLen = len/this.cipherBlockLen;
		/**************
		 * CipherAdd the new cipher into the result
		 */
		for (int j=0;j<blocksLen;j++)
		{
			int pos = j*cipherBlockLen;
			System.arraycopy(oldVaule, pos, b1, 0, cipherBlockLen);
			System.arraycopy(changeCipher, pos, b2, 0, cipherBlockLen);
			byte[] byteArray = paillier.cipher_add( new BigInteger(b1) , new BigInteger(b2) ).toByteArray();
			//System.arraycopy( byteArray, 0, bRet, pos, cipherBlockLen); 
			Util.copy(byteArray, bRet, pos, cipherBlockLen); 
		}   
		
		return bRet; 
	}
	/**
	 * once 
	 * @param constant
	 * @param cipher
	 * @return
	 */ 

    public BigInteger c_mul_cipher(BigInteger c, BigInteger cipher) {
    	int lBit=c.bitLength();
    	byte[] bDatas = c.toByteArray(); 
    	int lBytes= bDatas.length;
    	
    	BigInteger count = cipher;  
		
        BigInteger ret =  new BigInteger("1");  
        //BigInteger ret =  paillier.Encryption( new BigInteger("0") );  //or encryption of t0
    	 
        for (int i=0;i<lBit;i++)
        {
        	//if (c.mod(t2).equals(t1)){
        	int posByte = lBytes - i/8 -1;
        	int posBit = i%8;
        	boolean bOdd =  ((bDatas[posByte] >> posBit) &0x01)== 0x01 ;
        	if ( bOdd )
        	{
        		ret = paillier.cipher_add(ret, count);
        	}
        	
        	//c = c.shiftRight(1);
        	count = paillier.cipher_add(count, count); 
        }

        return ret;    
    }
    
    /**
     * cipher as the constant, cipher * vector
     * 
     * Notice that, our vector is one cipher block
     * @param cipher - real stored ciphertext, with more than one blocks by dividing
     * @param vector - one cipher block
     * @return
     */
	public byte[] cMulCipher(byte[] cipher, byte[] vector)
	{ 
		/*int srcLen = cipher.length;
		int encCount = srcLen/cipherBlockLen;
		int i=0;
		int len = encCount * cipherBlockLen;
		boolean bHasLeft = srcLen % cipherBlockLen >0;
		if (bHasLeft) 
			len += cipherBlockLen;
		
		 
        BigInteger vec = new BigInteger(vector); 

		byte[] ret = new byte[ len ];
		byte[] bIn=new byte[cipherBlockLen]; 
		
		for (i=0;i<encCount;i++)
		{  
				System.arraycopy(cipher, i*cipherBlockLen, bIn, 0, cipherBlockLen);
				BigInteger cm = c_mul_cipher( new BigInteger(bIn), vec);
				byte[] bs = cm.toByteArray();
				//System.arraycopy(cm.toByteArray(), 0, ret, i*cipherBlockLen, cipherBlockLen); 
				Util.copy(bs,ret, i*cipherBlockLen, cipherBlockLen); 
		}
		if (bHasLeft){ 
				int leftLen = srcLen-i*cipherBlockLen;
				byte[] bIn_1 = new byte[leftLen];
				System.arraycopy(cipher, i*cipherBlockLen, bIn_1, 0, leftLen);
				BigInteger cm = c_mul_cipher( new BigInteger(bIn_1), vec);
				//System.arraycopy(cm.toByteArray(), 0, ret, i*cipherBlockLen, cipherBlockLen); 
				byte[] bs = cm.toByteArray();
				Util.copy(bs,ret, i*cipherBlockLen, cipherBlockLen); 
		}
		
    	return ret;*/
    	
		int srcLen = cipher.length;
		int encCount = srcLen/plainBlockLen;
		int i=0;
		int len = encCount * cipherBlockLen;
		boolean bHasLeft = srcLen % plainBlockLen >0;
		if (bHasLeft) 
			len += cipherBlockLen;
		
		 
        BigInteger vec = new BigInteger(vector); 

		byte[] ret = new byte[ len ];
		byte[] bIn=new byte[plainBlockLen];
		byte[] bIn_0=new byte[plainBlockLen+1];
		
		for (i=0;i<encCount;i++)
		{ 
			if (cipher[i*plainBlockLen]<0)
			{
				bIn_0[0]=0;
				System.arraycopy(cipher, i*plainBlockLen, bIn_0, 1, plainBlockLen);
				BigInteger cm = c_mul_cipher( new BigInteger(bIn_0), vec);
				//System.arraycopy(cm.toByteArray(), 0, ret, i*cipherBlockLen, cipherBlockLen);
				byte[] bs = cm.toByteArray();
				Util.copy(bs,ret, i*cipherBlockLen, cipherBlockLen); 
			}else{
				System.arraycopy(cipher, i*plainBlockLen, bIn, 0, plainBlockLen);
				BigInteger cm = c_mul_cipher( new BigInteger(bIn), vec);
				//System.arraycopy(cm.toByteArray(), 0, ret, i*cipherBlockLen, cipherBlockLen);
				byte[] bs = cm.toByteArray();
				Util.copy(bs,ret, i*cipherBlockLen, cipherBlockLen); 
			}
		}
		if (bHasLeft){
			if (cipher[i*plainBlockLen]<0)
			{
				int leftLen = srcLen-i*plainBlockLen;
				byte[] bIn_1 = new byte[leftLen+1];
				bIn_1[0]=0;
				System.arraycopy(cipher, i*plainBlockLen, bIn_1, 1, leftLen);
				BigInteger cm = c_mul_cipher( new BigInteger(bIn_1), vec);
				//System.arraycopy(cm.toByteArray(), 0, ret, i*cipherBlockLen, cipherBlockLen);
				byte[] bs = cm.toByteArray();
				Util.copy(bs,ret, i*cipherBlockLen, cipherBlockLen); 
			}else{
				int leftLen = srcLen-i*plainBlockLen;
				byte[] bIn_1 = new byte[leftLen];
				System.arraycopy(cipher, i*plainBlockLen, bIn_1, 0, leftLen);
				BigInteger cm = c_mul_cipher( new BigInteger(bIn_1), vec);
				//System.arraycopy(cm.toByteArray(), 0, ret, i*cipherBlockLen, cipherBlockLen);
				byte[] bs = cm.toByteArray();
				Util.copy(bs,ret, i*cipherBlockLen, cipherBlockLen); 
			}
		}
		
    	return ret;
	}

	public byte[] generateZero()
	{ 
		BigInteger b = new BigInteger("0");
		Random rnd = new Random();
		int r = rnd.nextInt(10000000);
	
		return paillier.Encryption(b, new BigInteger(String.valueOf(r))).toByteArray();
	}
	public byte[] generateOne()
	{ 
		BigInteger b = new BigInteger("1");
		Random rnd = new Random();
		int r = rnd.nextInt(10000000);
	
		return paillier.Encryption(b, new BigInteger(String.valueOf(r))).toByteArray();
	}
	public byte[] generateOne(int len)
	{ 
		byte[] ret = new byte[len];
		for (int i=0;i <len/this.cipherBlockLen; i++)
		{
			BigInteger b = new BigInteger("1");
			Random rnd = new Random();
			int r = rnd.nextInt(10000000);
			byte[] bdata=paillier.Encryption(b, new BigInteger(String.valueOf(r))).toByteArray();
			//System.arraycopy(bdata, 0, ret, i*cipherBlockLen, cipherBlockLen);
			Util.copy(bdata, ret, i*cipherBlockLen, cipherBlockLen);
		}
	
		return ret;
	} 
	public byte[] generateZero(int len)
	{ 
		byte[] ret = new byte[len];
		for (int i=0;i <len/this.cipherBlockLen; i++)
		{
			BigInteger b = new BigInteger("0");
			Random rnd = new Random();
			int r = rnd.nextInt(10000000);
			byte[] bdata=paillier.Encryption(b, new BigInteger(String.valueOf(r))).toByteArray();
			Util.copy(bdata, ret, i*cipherBlockLen, cipherBlockLen);
			//System.arraycopy(bdata, 0, ret, i*cipherBlockLen, cipherBlockLen);
		}
	
		return ret;
	} 
	/**
	 * pir reading from a group of ciphertexts
	 * to product a ciphertext
	 * @return
	 */
	public byte[] PIRRead(byte[][] ciphers, byte[][] vectors)
	{ 
		
		if (ciphers.length!=vectors.length)
			return null;

		byte[] b1=new byte[cipherBlockLen];
		byte[] b2=new byte[cipherBlockLen];
		
		int len = ciphers.length;
		byte[] bRet = cMulCipher(ciphers[0], vectors[0]);
		int cipherLen = bRet.length;
		int blocksLen = cipherLen/this.cipherBlockLen;
		/*************
		 * Generate the only one cipher and return bakc
		 */
		for (int i=1;i<len; i++)
		{
			byte[] bRetTemp = cMulCipher(ciphers[i], vectors[i]);
			
			/**************
			 * CipherAdd the new cipher into the result
			 */
			for (int j=0;j<blocksLen;j++)
			{
				int pos = j*cipherBlockLen;
				System.arraycopy(bRetTemp, pos, b1, 0, cipherBlockLen);
				System.arraycopy(bRet, pos, b2, 0, cipherBlockLen);
				byte[] byteArray = paillier.cipher_add( new BigInteger(b1) , new BigInteger(b2) ).toByteArray();
				//System.arraycopy( byteArray, 0, bRet, pos, cipherBlockLen); 
				Util.copy(byteArray, bRet, pos, cipherBlockLen); 
			} 
		}
		
		return bRet;
	}
	/**
	 * Two decryption for block resulted by the PIR Read 
	 * @param ciphers
	 * @param vectors
	 * @return
	 */
	public byte[] PIRReadDecode( byte[] ret )
	{ 
		return decrypt(decrypt(ret)); 
		
//		int len = ret.length/cipherBlockLen * plainBlockLen; 
//		
//		byte[] bTemp = decrypt(ret);
//		boolean bZero = true;
//		for (int i=0;i<bTemp.length; i++)
//			if (bTemp[i]!=0)
//			{
//				bZero=false;
//				break;
//			}
//		
//		if (bZero)
//			return new byte[len]; //default value is 0
//		else{
//			//further decrypt
//			byte[] retV = decrypt(bTemp);
//			bZero = true;
//			for (int i=0;i<retV.length; i++)
//				if (retV[i]!=0)
//				{
//					bZero=false;
//					break;
//				}
//			if (bZero)
//				return new byte[len]; //default value is 0
//			else
//				return retV; 
//		}
	}

	/**
	 * encrypt data with a long length
	 * @param bPlain
	 * @return
	 */
	public byte[] encrypt(byte[] bPlain)
	{ 
		int srcLen = bPlain.length;
		int encCount = srcLen/plainBlockLen;
		int i=0;
		int len = encCount * cipherBlockLen;
		boolean bHasLeft = srcLen % plainBlockLen >0;
		if (bHasLeft) 
			len += cipherBlockLen;

		byte[] cipher = new byte[ len ];
		byte[] bIn=new byte[plainBlockLen];
		byte[] bIn_0=new byte[plainBlockLen+1];
		
		for (i=0;i<encCount;i++)
		{ 
			if ( bPlain[i*plainBlockLen] <0 )
			{
				bIn_0[0]=0;
				System.arraycopy(bPlain, i*plainBlockLen, bIn_0, 1, plainBlockLen);
				byte[] bRet=enc( bIn_0 );
				//System.arraycopy(bRet, 0, cipher, i*cipherBlockLen, cipherBlockLen); 
				Util.copy(bRet,cipher, i*cipherBlockLen, cipherBlockLen); 
			}else{
				System.arraycopy(bPlain, i*plainBlockLen, bIn, 0, plainBlockLen);
			    byte[] bRet=enc( bIn );
			    //System.arraycopy(bRet, 0, cipher, i*cipherBlockLen, cipherBlockLen);
				Util.copy(bRet,cipher, i*cipherBlockLen, cipherBlockLen); 
			}
		}
		if (bHasLeft){ 
			if ( bPlain[i*plainBlockLen] <0 )
			{ 
				int leftLen = srcLen-i*plainBlockLen;
				byte[] bIn_1 = new byte[leftLen+1];
				bIn_1[0]=0;
				System.arraycopy(bPlain, i*plainBlockLen, bIn_1, 1, leftLen);
				byte[] bRet=enc( bIn_1 );
				//System.arraycopy(bRet, 0, cipher, i*cipherBlockLen, cipherBlockLen); 
				Util.copy(bRet,cipher, i*cipherBlockLen, cipherBlockLen); 
			}else{
				int leftLen = srcLen-i*plainBlockLen;
				byte[] bIn_1 = new byte[leftLen];
				System.arraycopy(bPlain, i*plainBlockLen, bIn_1, 0, leftLen);
				byte[] bRet=enc( bIn_1 );
				//System.arraycopy(bRet, 0, cipher, i*cipherBlockLen, cipherBlockLen); 
				Util.copy(bRet,cipher, i*cipherBlockLen, cipherBlockLen); 
			}
		}
		
    	return cipher;
	}
	
	
	/**
	 * decrypt data with a long length
	 * @param bCipherText
	 * @return
	 */

	public byte[] decrypt(byte[] bCipherText)
	{ 
		//may be zero is zero
		if (bCipherText.length < cipherBlockLen && bCipherText[0]==0)
			return bCipherText;
		
		int srcLen = bCipherText.length;
		int encCount = srcLen/cipherBlockLen;
		int i=0;
		int len = encCount * plainBlockLen; 

		byte[] plain = new byte[ len ];
		byte[] bIn=new byte[cipherBlockLen];
		
		int realLen = 0;
		for (i=0;i<encCount;i++)
		{ 
			System.arraycopy(bCipherText, i*cipherBlockLen, bIn, 0, cipherBlockLen);
			byte[] bRet=dec( bIn );
			
			if (bRet[0]==0)
			{ 
				System.arraycopy(bRet, 1, plain, i*plainBlockLen, bRet.length-1);
				realLen+=bRet.length; 
			}else{ 
				System.arraycopy(bRet, 0, plain, i*plainBlockLen, bRet.length);
				realLen+=bRet.length;
			}
		}

		byte[] ret = new byte[ realLen ];
		System.arraycopy(plain, 0, ret, 0, realLen);
    	return ret;
	}

	/**
	 * decrypt one block
	 * @param b
	 * @return
	 */
	private byte[] dec(byte[] b)
	{ 
    	BigInteger big1=new BigInteger(b);
		big1 = paillier.Decryption(big1); 
		return big1.toByteArray();
	}
	/**
	 * encrypt one block
	 * @param b
	 * @return
	 */
	private byte[] enc(byte[] b)
	{ 
    	BigInteger big1=new BigInteger(b);
		big1 = paillier.Encryption(big1); 
		return big1.toByteArray();
	}
	
	
	
	 
}
