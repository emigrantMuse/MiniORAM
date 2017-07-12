package nankai.oram;

public class Position{
	/**********************
	 * BLockID is maintained by the client
	 * But id is stored in the server, the record ID of the data u with ID of BLockID
	 * 
	 * The same sequence in the client is denoted as the BLockIDs
	 * but the real sequence in the server will be the different ids
	 * ********************/
	public int partition;
	public int level;
	public int offset;
	public int rndNumber;//random value  
	public Position()
	{
		partition=-1;
		level=-1;
		offset=-1;
		rndNumber=0; 
	}
}