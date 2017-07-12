package nankai.oram.test;

import nankai.oram.parORAM.PartitionORAM;
 


/**
 * This is the init oram program
 * Each test should after the initilization of ORAM
 * @author Dell
 *
 */
public class ParORAMInitOram {

	public static void main(String[] args) {   
		
		
		PartitionORAM oram=new PartitionORAM();
		

		//initialize the client
		oram.init(102400); 
		

		long testTime = -1; 
    	long testDoneTime = -1; 
        testTime = System.currentTimeMillis(); //ms  
		//initalize the server
		oram.initORAM(); 

        testDoneTime = System.currentTimeMillis();  
        double totalElapsedTime = (testDoneTime - testTime);// / 1000.0;
        System.out.println("totalElapsedTime:"+totalElapsedTime);  
		 
		
	}
}
