package nankai.oram.test;

import nankai.oram.miniORAM.MiniPartitionORAM;
import nankai.oram.parORAM.PartitionORAM;
import nankai.oram.util.CommInfo;
import nankai.oram.util.Util;
 

public class MiniORAMTest {

	public static void main(String[] args) {

		long testTime = -1;
		long testTime1 = -1;
    	long testDoneTime = -1;
    	long testDoneTime1 = -1;
		
		
		MiniPartitionORAM oram=new MiniPartitionORAM();
		

		//initialize the client
		oram.init(400); 
		 
		//initalize the server
		oram.initORAM();

		

        testTime = System.currentTimeMillis(); //ms  
        testTime1 = System.nanoTime();//ns 
		//write some data
		byte[] bData = new byte[CommInfo.blockSize];
		for (int id = 0; id < 1; id++) {
			for (int i = 0; i < CommInfo.blockSize; i++)
				bData[i] = (byte) (id + 1);  
			oram.write(id, bData);
		}

        testDoneTime = System.currentTimeMillis(); 
        testDoneTime1 = System.nanoTime();  
        double totalElapsedTime = (testDoneTime - testTime);// / 1000.0;
        double totalElapsedTime1 = (testDoneTime1 - testTime1);// / 1000.0;
        System.out.println("totalElapsedTime:"+totalElapsedTime);  
        System.out.println("totalElapsedTime1:"+totalElapsedTime1); 
        
		System.out.println("-----ready to read the block !-----------");

        testTime = System.currentTimeMillis(); //ms  
        testTime1 = System.nanoTime();//ns 
		bData=oram.read(111); 
		

        testDoneTime = System.currentTimeMillis(); 
        testDoneTime1 = System.nanoTime();  
        totalElapsedTime = (testDoneTime - testTime);// / 1000.0;
        totalElapsedTime1 = (testDoneTime1 - testTime1);// / 1000.0;
        System.out.println("totalElapsedTime:"+totalElapsedTime);  
        System.out.println("totalElapsedTime1:"+totalElapsedTime1); 
		/******************
		 * Should be the byte 0x08
		 * *******************/
		System.out.println(bData[0]+"  "+bData[10]);
		 

	}

}
