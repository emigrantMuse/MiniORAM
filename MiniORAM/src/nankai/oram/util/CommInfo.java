package nankai.oram.util;

public interface CommInfo {

	public static int dummyID=-1;
	public static int noiseID=-2;
	public static int keySize=16;//the length of a key   byte
	public static int v=2;//the rate of evict operation 
	public static int blockSize=2048;//the length of a block    byte
	public static double capacity_parameter=4.6;//4.6*N, the parameter of max capacity of partition
	public static double capacity_parameter_mini=2.8;//4.6*N, the parameter of max capacity of partition
}
