package nankai.oram.test;

import nankai.oram.parORAM.PartitionORAM;
import nankai.oram.util.CommInfo;
import nankai.oram.util.Util;
 

public class PartitionORAMTest {

	public static void main(String[] args) {

		long testTime = -1;
		long testTime1 = -1;
    	long testDoneTime = -1;
    	long testDoneTime1 = -1;
		
		
		PartitionORAM oram=new PartitionORAM();
		

		//initialize the client
		oram.init(102400); 
		

		//initalize the server
		oram.openORAM();

		Util.bandwidth = 0;
		Util.bandwidthMini = 0;
		Util.worstShuffle = 0; 
		Util.filleLevels=0;

        testTime = System.currentTimeMillis(); //ms   
		//write some data
		byte[] bData = new byte[CommInfo.blockSize];
		
		for (int h=0; h<2; h++ )
			for (int id = 0; id < 500; id++) { 
				for (int i = 0; i < CommInfo.blockSize; i++)
					bData[i] = (byte) id;
				Util.intToByte(bData, 0, id);
				oram.write(id, bData);
			}

        testDoneTime = System.currentTimeMillis();  
        double totalElapsedTime = (testDoneTime - testTime);// / 1000.0; 
        System.out.println("totalElapsedTime:"+totalElapsedTime);   
        
		System.out.println("-----ready to read the block !-----------");

        testTime = System.currentTimeMillis(); //ms  
        testTime1 = System.nanoTime();//ns 
		bData=oram.read(111); 
		

        testDoneTime = System.currentTimeMillis();  
        totalElapsedTime = (testDoneTime - testTime);// / 1000.0; 
        System.out.println("totalElapsedTime:"+totalElapsedTime);   
		/******************
		 * Should be the byte 0x08
		 * *******************/
		System.out.println(bData[0]+"  "+bData[10]);


		System.out.println( "Util.bandwidth  "+Util.bandwidth);
		System.out.println( "Util.bandwidthMini  "+Util.bandwidthMini);

		System.out.println( "Util.bandwidth_worst  "+Util.bandwidth_worst);
		System.out.println( "Util.bandwidthMini_worst  "+Util.bandwidthMini_worst);
		System.out.println( "Util.worstShuffle  "+Util.worstShuffle); 
		System.out.println( "Util.filleLevels  "+Util.filleLevels);  
		System.out.println( "Util.writeNumber  "+Util.writeNumber);  
		
	}

}
