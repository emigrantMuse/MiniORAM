package nankai.oram.pir;

import java.math.BigInteger;

public class PIRTest {
	

    public static void main(String[] str) { 
    	
    	//TestEnc();
    	
        //testPIR();
    	
        testPIRRead();
        
        //testPIRWriteBigInteger();
    	
    	//testPIRWrite();
    	

        //TestEncTime();
    	
    	//testCPlusCipherTime();
    	
//        BigInteger S1 = new BigInteger(bCipher);
//        System.out.println(S1.longValue());
    	  
    }
    

	private static void testPIRWrite() {
		Paillier p =  new Paillier();
    	
    	PIRImpl pir=new PIRImpl(p);
 
    	
    	byte[] oldData = new byte[222];
		oldData[0]= 0x04;
    	for (int i=1;i<222;i++)
    		oldData[i]= (byte) (i % 100); 
    	

    	byte[] newData = new byte[222];
		newData[0]= 0x03;
    	for (int i=1;i<222;i++)
    		newData[i]= (byte) ( (i+1) % 100); 
    	
    	 
    	byte[] oldC = pir.encrypt(oldData);
    	byte[] changeV = pir.changeValue(oldData, newData);
    	byte[] ret = pir.PIRWriteBlock(oldC, changeV);
    	
    	byte[] bPlain = pir.decrypt(ret); 
    	System.out.println(bPlain[0]+"  "+bPlain[1]+"  "+bPlain[2] +" "+bPlain.length); 
    	
	}

	private static void testPIRWriteBigInteger() {
		Paillier p =  new Paillier();
    	
    	//PIRImpl pir=new PIRImpl(p);
 
    	
    	byte[] oldData = new byte[111];
		oldData[0]= 0x04;
    	for (int i=1;i<111;i++)
    		oldData[i]= (byte) (i % 100); 
    	

    	byte[] newData = new byte[111];
		newData[0]= 0x03;
    	for (int i=1;i<111;i++)
    		newData[i]= (byte) ( (i+1) % 100); 
    	
    	 

		BigInteger oldV = new BigInteger(oldData);
		BigInteger newV = new BigInteger(newData);
		BigInteger changeV = newV.subtract(oldV);//.mod(p.n) ; 
		
		changeV = p.Encryption( changeV );

    	
		BigInteger oldC = p.Encryption(oldV);

		//BigInteger changeV = p.cipher_add(oldC, changeV);
		BigInteger ret = p.cipher_add(oldC, changeV);
    	

		ret = p.Decryption(ret);
		
    	byte[] bPlain = ret.toByteArray(); 
    	System.out.println(bPlain[0]+"  "+bPlain[1]+"  "+bPlain[2]);
    	
    	
    	
    	
    	
    	
    	
    	
    	
//    	byte[] bCipher_old=pir.encrypt(oldData); 
//    	
//
//    	
//    	byte[] changeCipher = pir.cipherSUB(oldData, newData);
//    	
//    	byte[] ret = pir.cipherADD(bCipher_old, changeCipher); 
//    	
//    	BigInteger r1 = p.Decryption(new BigInteger(ret));
//    	//r1 = p.Decryption( r1 );
//    	byte[] bPlain = r1.toByteArray();
//  
//    	//byte[] bPlain=pir.decrypt(ret); 
//    	System.out.println(bPlain[0]+"  "+bPlain[1]+"  "+bPlain[2]);
	}
    

	private static void testPIRRead() {
		Paillier p =  new Paillier();
    	
    	PIRImpl pir=new PIRImpl(p);

		BigInteger t0 = new BigInteger("0");
		BigInteger t1 = new BigInteger("1"); 
        BigInteger et0 = p.Encryption(t0);
        BigInteger et1 = p.Encryption(t1);
        
    	
    	int plainLen = p.getPlaintextLen();
    	int cipherLen = p.getCiphertextLength();
    	
    	byte[] plain1 = new byte[111];
		plain1[0]= 0x01;
    	for (int i=1;i<111;i++)
    		plain1[i]= (byte) (i % 100); 
    	
    	byte[] bCipher_1=pir.encrypt(plain1); 
    	

    	byte[] plain2 = new byte[111];
		plain2[0]= 0x02;
    	for (int i=1;i<111;i++)
    		plain2[i]= (byte) (i % 100); 
    	
    	byte[] bCipher_2=pir.encrypt(plain2); 

    	byte[] bC0 = pir.generateZero();
    	byte[] bC1 = pir.generateOne();
    	
    	//cipher as constant
//    	byte[] bCipher1 = pir.cMulCipher( bCipher_1, bC0);
//    	byte[] bCipher2 = pir.cMulCipher( bCipher_2, bC1);
    	
    	byte[][] ciphers = {bCipher_1, bCipher_2};
    	byte[][] vectors = {bC1, bC0};
    	
    	byte[] ret=pir.PIRRead(ciphers, vectors);
  
    	byte[] bPlain=pir.PIRReadDecode(ret); 
    	System.out.println(bPlain[0]+"  "+bPlain[1]+"  "+bPlain[2]);
	}

	private static void testPIR() {
		Paillier p =  new Paillier();
    	
    	PIRImpl pir=new PIRImpl(p);

		BigInteger t0 = new BigInteger("0");
		BigInteger t1 = new BigInteger("1"); 
        BigInteger et0 = p.Encryption(t0);
        BigInteger et1 = p.Encryption(t1);
        
    	
    	int plainLen = p.getPlaintextLen();
    	int cipherLen = p.getCiphertextLength();
    	
    	byte[] s = new byte[222];
		s[0]= 0x01;
    	for (int i=1;i<222;i++)
    		s[i]= (byte) (i % 100);
    	

//		BigInteger s_num = new BigInteger("111");
//        BigInteger S1 =  pir.c_mul_cipher(s_num, et1);
//
//        System.out.println("decrypted sum: "+p.Decryption(S1).toString());
    	
    	byte[] bCipher=pir.encrypt(s); 
        		
    	BigInteger s1=new BigInteger("1");
    	byte[] bC1=p.Encryption(s1).toByteArray();
    	
    	//cipher as constant
    	byte[] bCipher1 = pir.cMulCipher( bCipher, bC1);
    	
    	byte[] bPlain=pir.decrypt(bCipher1);

        System.out.println("len:"+bCipher.length); 
        for (int i=0;i<bCipher.length; i++)
        	 System.out.print( i+":"+bCipher[i]+" " );
        System.out.println();
        
    	System.out.println("len:"+bPlain.length); 
        for (int i=0;i<bPlain.length; i++)
       	 System.out.print( i+":"+bPlain[i]+" " );
        System.out.println();
    	
    	bPlain=pir.decrypt(bPlain);
    	System.out.println(bPlain[0]+"  "+bPlain[1]+"  "+bPlain[2]);
	}

	private static void TestEnc() {
		Paillier p =  new Paillier();
 
    	
    	PIRImpl pir=new PIRImpl(p);
    	
    	int plainLen = p.getPlaintextLen();
    	int cipherLen = p.getCiphertextLength();
    	
    	int n=1001;
    	byte[] s = new byte[n];
		s[0]= (byte) 0xa0;
		if (s[0]<0)
			System.out.println("000000000000000");
    	for (int i=1;i<n;i++)
    	{
    		if (i%2 ==0)
    			s[i]=(byte) 0xaF;
    		else
    		    s[i]= (byte)0xa9;
    	}
    	
        for (int i=0;i<n; i++)
          	 System.out.print( i+":"+s[i]+" " );
           System.out.println();
   		
    	
    	byte[] bCipher=pir.encrypt(s);

        for (int i=0;i<n; i++)
       	 System.out.print( i+":"+bCipher[i]+" " );
        System.out.println();
        
    	
    	byte[] bPlain=pir.decrypt(bCipher); 

        for (int i=0;i<n; i++)
       	 System.out.print( i+":"+bPlain[i]+" " );
        System.out.println();
	}
	

	private static void testCPlusCipherTime() {
    	long testTime = -1;
    	long testDoneTime = -1;
    	
    	int n = 1024;
		Paillier p =  new Paillier(n,64);
    	
    	PIRImpl pir=new PIRImpl(p);

		BigInteger t0 = new BigInteger("0");
		BigInteger t1 = new BigInteger("1"); 
        BigInteger et0 = p.Encryption(t0);
        BigInteger et1 = p.Encryption(t1);
        
    	
    	int plainLen = p.getPlaintextLen();
    	int cipherLen = p.getCiphertextLength();
    	
    	byte[] s = new byte[n/8 -20];
		s[0]= 0x01;
    	for (int i=1;i<n/8 -20;i++)
    		s[i]= (byte) (i % 100);
    	
    	BigInteger c1 = new BigInteger(s);
    	BigInteger c = p.Encryption(c1);
        		 
    	
    	//cipher as constant
        testTime = System.currentTimeMillis(); //ms
		BigInteger cm = pir.c_mul_cipher( c, et1); 
        testDoneTime = System.currentTimeMillis();  
        double totalElapsedTime = (testDoneTime - testTime);// / 1000.0;
        System.out.println("totalElapsedTime:"+totalElapsedTime);  
 
	}
	private static void TestEncTime() {

    	long testTime = -1;
    	long testDoneTime = -1;
    	
    	int n = 1024;
		Paillier p =  new Paillier(n,64);
 
    	  
    	byte[] s = new byte[n/8];
		s[0]= (byte) 0; 
    	for (int i=1;i<n/8;i++)
    	{
    		if (i%2 ==0)
    			s[i]=(byte) 0xaF;
    		else
    		    s[i]= (byte)0xa9;
    	}
    	BigInteger m = new BigInteger(s);
    	
        testTime = System.currentTimeMillis(); //ms
        BigInteger c = p.Encryption(m);

        testDoneTime = System.currentTimeMillis();  
        double totalElapsedTime = (testDoneTime - testTime);// / 1000.0;
        System.out.println("totalElapsedTime:"+totalElapsedTime);  

        testTime = System.currentTimeMillis(); //ms
        BigInteger m1 =p.Decryption(c); 
        testDoneTime = System.currentTimeMillis();  
        totalElapsedTime = (testDoneTime - testTime);// / 1000.0;
        System.out.println("totalElapsedTime:"+totalElapsedTime);  

        byte[] bPlain = m1.toByteArray();
        for (int i=0;i<n/8; i++)
       	 System.out.print( i+":"+bPlain[i]+" " );
        System.out.println();
	}
}
