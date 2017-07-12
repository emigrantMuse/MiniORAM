package nankai.oram.miniORAM;


/*************
 * The data in a level of a partition is encrypted by the key for level
 * 
 * Each data is included by BID+DATA 
 * BID = REALBLOCK? BLOCKID : - OFFSET
 */

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
 




import nankai.oram.Position;
import nankai.oram.pir.PIRImpl;
import nankai.oram.util.CommInfo;
import nankai.oram.util.MongDBUtil;
import nankai.oram.util.SymmetricCypto;
import nankai.oram.util.Util;

public class MiniPartition{ 
	public static int part=0;
	int n_levels;
	
	int n_blocks;
	int n_realBlocks_p;//the real number of blocks in a partition 
	int n_capacity;//the max capacity of a partition -- need the top level
	int top_level_len;//the number of blocks in the top level
	
	int p = 0;
	int blockIDs[];//all the blockIDs. When re-shuffle, the dummyIDs will be re-filled  
	boolean filled[];//filled level flag  
	
 
	Position pos_map[];//position map 

	byte cmdData[] = new byte[13]; 

	public int realDataNumber = 0; 
	

	MongDBUtil dbUtil;
	PIRImpl PIR;

	Random rnd = new Random();
	
	public MiniPartition(int nN, int n_partitions, Position posMap[], MongDBUtil _dbUtil, PIRImpl pir )
	{ 
		PIR=pir;
		dbUtil=_dbUtil; 
		pos_map=posMap; 
		
		n_realBlocks_p =   (int) Math.ceil(((double) nN) / n_partitions);
	    n_blocks = n_realBlocks_p * n_partitions;
	    
	    n_levels = (int) (Math.log((double) n_realBlocks_p) / Math.log(2.0)) ;
	    n_capacity = (int) Math.ceil(CommInfo.capacity_parameter_mini * n_realBlocks_p);
	    top_level_len = n_capacity - (1 << (n_levels-1) ) ;
	    
		p = part++;  
		blockIDs=new int[n_capacity]; 
		filled=new boolean[n_levels];

		for (int i=0;i<n_levels;i++) 
			filled[i]=false;
		
		filled[n_levels-1]=true;

		for (int i=0;i<n_capacity;i++) 
			blockIDs[i]=CommInfo.dummyID; 
		 
	}
	
	 

	public byte[] readPartition(int block_id) {
		
		/****************
		 * Client should first generate the vector and send to the cloud
		 * 
		 * In our simulation, we will store each ciphertext into ciphers
		 * and generate each vector at the same time		 * 
		 */
		int filledLevelsNumber = 0;
		for (int i=0;i<n_levels;i++)
		{
			if (filled[i]) 
				filledLevelsNumber++; 
		}
		int ciphertextLength = PIR.getCipherTextLen(CommInfo.blockSize);
		byte[][] ciphers = new byte[filledLevelsNumber][ciphertextLength];
		byte[][] vectors = new byte[filledLevelsNumber][PIR.getCipherBlockLen()];

		/*******************************
		 * PIR READ
		 * 
		 * From Each filled level, to read a block
		 * if it is in this level, read the only one real block
		 * else, read a dummy block			 * 
		 * *****************************/
		int readNumber = 0;
		byte[] data = new byte[ciphertextLength]; 
		int level=-1;
		if (block_id!=CommInfo.dummyID)
			level = pos_map[block_id].level;
		
		for (int i=0;i<n_levels;i++)
		{
			if (filled[i])
			{
				int begin =  (1 << i) - 1;
				if (level==i)
				{
				    int _id = begin+ pos_map[block_id].offset ;
					// in this level, to read a real block. get the offset
					readBlock(p, _id, data);
					/*****************************
					 * Store into the ciphers
					 * *************************/ 
					System.arraycopy(data, 0, ciphers[readNumber], 0, ciphertextLength); 
					//generate one vector
					vectors[readNumber] = PIR.generateOne();
					
					//if this is the real block, the identify will be set to noise but not the dummy
					this.blockIDs[_id] = CommInfo.noiseID;
					
				}else{
					
					//random select a block whatever dummy or not
				    int _id = begin+ rndOffset(i);

					readBlock(p, _id, data);
					/*****************************
					 * Store into the ciphers
					 * *************************/ 
					System.arraycopy(data, 0, ciphers[readNumber], 0, ciphertextLength); 
					//generate zero vector
					vectors[readNumber] = PIR.generateZero();
				}
				readNumber++;
			}
		}

		byte[] ret = PIR.PIRRead(ciphers, vectors);

		/*******************************
		 *PIR DECODE
		 * *****************************/
		ret = PIR.PIRReadDecode(ret);
		
		return ret; 
	}
	
	public void writePartition(int block_id, byte[] bData)
	{
		/***************************
		 * Step 1
		 * add a dummy
		 * **********************************/
		//store into the database 
		try { 
			int cipherLen = PIR.getCipherTextLen(CommInfo.blockSize);  
			byte[] bBlockDataZero= PIR.generateZero(cipherLen);
			
			writeBlock(p, -1, bBlockDataZero);  
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		

		/***************************
		 * Step 2
		 * write dummy or write real
		 * **********************************/
		//get the filled levels number
		int filledLevelsNumber = 0;
		for (int i=0;i<n_levels;i++)
		{
			if (filled[i]) 
				filledLevelsNumber++; 
		}
		
		//get the first no filled level
		int firstUnfilledLevel=-1;
		for (int i=0;i<this.n_levels;i++)
		{
			if (!filled[i])
			{
				firstUnfilledLevel=i-1;
				break;
			}
		}
		
		boolean bDummy = (block_id <= CommInfo.dummyID);
		if (bDummy)
		{
			writeDummy(block_id, bData, filledLevelsNumber, firstUnfilledLevel); 
		}else{ 
			writeReal(block_id, bData, filledLevelsNumber);
		}
		 

		/************************** 
		 * Step 3
		 * upper moving
		 **************************/ 
		 UpperMoving();
	}



	private void UpperMoving() {
		;
	}



	private void writeReal(int block_id, byte[] bData, int filledLevelsNumber) {
		/****************
		 * Write real block
		 * 
		 * Client should first generate the vector and send to the cloud
		 * 
		 * In our simulation, we will store each ciphertext into ciphers
		 * and generate each vector at the same time		 * 
		 */

		int realOFFSET = 0;
		int realLevel = 0;
		
		//random select a level with noise or dummy blocks   
		int fetchedLevel = rnd.nextInt(filledLevelsNumber);
		//add the chance to write into the dummy layer , i.e., level #
		if (rnd.nextInt(filledLevelsNumber)==0){
			fetchedLevel=-1;
			realLevel= -1;
		}else{
			//ensure that there are enough dummy or noise blocks 
			while (true)
			{
				//get the real level
				int j = 0;
				for (int i=0;i<n_levels;i++)
				{
					if (filled[i]) {
						if (j==fetchedLevel)
						{
							fetchedLevel=i;
							break;
						}
						j++;
					}
				}
				int begin = 1<<fetchedLevel -1;
				int end = (1 << (fetchedLevel+1));
			    if (fetchedLevel == n_levels - 1) 
			    	end = this.top_level_len;
			    
			    boolean bExistDummy = false;
			    for (j=0; j<end; j++ )
				{
					if ( this.blockIDs[begin+j] <= CommInfo.dummyID)
					{
						bExistDummy=true;
						break; 
					}
				}
			    if (bExistDummy)
			    {
			    	realLevel = fetchedLevel;
			    	break;
			    }
			    
			    fetchedLevel = rnd.nextInt(filledLevelsNumber);		
			}
		}
		
		
		int ciphertextLength = PIR.getCipherTextLen(CommInfo.blockSize);
		
		//the dummy layer should be visited
		byte[][] ciphers = new byte[filledLevelsNumber+1][ciphertextLength];
		byte[][] vectors = new byte[filledLevelsNumber+1][PIR.getCipherBlockLen()];
		int[] offSets=new int[filledLevelsNumber+1];
		

		byte[] oldStoredValue = new byte[ciphertextLength];
		/*******************************
		 * 1 PIR READ 
		 * *****************************/
		int readNumber = 0;
		byte[] data = new byte[ciphertextLength];  
		
		//first visit the dummy block
		if (realLevel==-1)
		{  
			realOFFSET = -1; 
			readBlock(p, realOFFSET, data);
			/*****************************
			 * Store into the ciphers
			 * *************************/ 
			System.arraycopy(data, 0, ciphers[readNumber], 0, ciphertextLength); 
			System.arraycopy(data, 0, oldStoredValue, 0, ciphertextLength); 
			
			//generate one vector
			vectors[readNumber] = PIR.generateOne();
			readNumber++;
		}
		
		for (int i=0;i<n_levels;i++)
		{
			if (filled[i])
			{
				int begin = (1 << i) - 1;
				if (realLevel == i)
				{
					//will fetch a real dummy or noise block to be written 
					offSets[readNumber]=rndDummyOrNoise(i);
					realLevel = i;
					realOFFSET = begin + offSets[readNumber]; 
					
					readBlock(p, realOFFSET, data);
					/*****************************
					 * Store into the ciphers
					 * *************************/ 
					System.arraycopy(data, 0, ciphers[readNumber], 0, ciphertextLength); 
					System.arraycopy(data, 0, oldStoredValue, 0, ciphertextLength); 
					//generate one vector
					vectors[readNumber] = PIR.generateOne();
					

				}else{
					//random select a block to read 
					offSets[readNumber] = rndOffset(i);
					int _id = begin + offSets[readNumber]; 
					readBlock(p, _id, data);
					/*****************************
					 * Store into the ciphers
					 * *************************/ 
					System.arraycopy(data, 0, ciphers[readNumber], 0, ciphertextLength); 
					//generate zero vector
					vectors[readNumber] = PIR.generateZero();
				}
				readNumber++; 
			}
		}

		byte[] ret = PIR.PIRRead(ciphers, vectors);
		/*******************************
		 *2 PIR DECODE
		 * *****************************/
		ret = PIR.PIRReadDecode(ret);

		/*******************************
		 *3 compute change value
		 * *****************************/
		byte[] changeV =PIR.changeValue(ret, bData);
		
		/*******************************
		 *4 write back
		 * *****************************/
		byte[] result = PIR.PIRWriteBlock(oldStoredValue, changeV); 

		//store into the database 
		try {
			writeBlock(p, realOFFSET, result); 
			
			byte[] bddd = PIR.decrypt(result);
			

			//save status, update the position map 
		    pos_map[block_id].partition = p;
		    pos_map[block_id].level = realLevel;
		    pos_map[block_id].offset = realOFFSET;

			blockIDs[realOFFSET]=block_id;
			
			//offSets is used to execute write in the server, our simulation is ignored
			
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}



	private void writeDummy(int block_id, byte[] bData, int filledLevelsNumber,
			int firstUnfilledlevel) {
		//first read a real block and then merge it with a dummy to hide its position

		/**********************
		 * Step 1
		 * Read a real block 
		 *************************/

		int ciphertextLength = PIR.getCipherTextLen(CommInfo.blockSize);
		byte[][] ciphers = new byte[filledLevelsNumber][ciphertextLength];
		byte[][] vectors = new byte[filledLevelsNumber][PIR.getCipherBlockLen()];

		int readNumber = 0;
		byte[] data = new byte[ciphertextLength]; 
		 
		
		//select a real block 
		int realOFFSET = this.rndReal(firstUnfilledlevel);
		int realLevel = -1;//(int) Math.log(  realOFFSET ) ;
		//if no real block, will execute the same process, to write into the first dummy position
		if (realOFFSET!=-1)
		{
			realLevel = (int) Math.log(  realOFFSET ) ;
		}
		if (realOFFSET> (1<<(n_levels-1) - 1) )
			realLevel = n_levels-1;
		
		int realBlockID = -1;
		
		for (int i=0;i<n_levels;i++)
		{
			if (filled[i])
			{
				int begin =  (1 << i) - 1;
				if (realLevel==i)
				{
				    int _id = realOFFSET ;
					// in this level, to read a real block. get the offset
					readBlock(p, _id, data);
					/*****************************
					 * Store into the ciphers
					 * *************************/ 
					System.arraycopy(data, 0, ciphers[readNumber], 0, ciphertextLength); 
					//generate one vector
					vectors[readNumber] = PIR.generateOne();
					
					//this would be a noise block
					realBlockID = blockIDs[_id];
					this.blockIDs[_id] = CommInfo.noiseID;
					
				}else{
					//random select a block to read
				    int _id = begin+ rndOffset(i) ;
					readBlock(p, _id, data);
					/*****************************
					 * Store into the ciphers
					 * *************************/ 
					System.arraycopy(data, 0, ciphers[readNumber], 0, ciphertextLength); 
					//generate zero vector
					vectors[readNumber] = PIR.generateZero();
				}
				readNumber++;
			}
		}

		byte[] ret = PIR.PIRRead(ciphers, vectors);

		/*******************************
		 *PIR DECODE
		 * *****************************/
		ret = PIR.PIRReadDecode(ret);

		/**********************
		 * Step2
		 * Merge it with a dummy block 
		 *************************/
		//here, old value is bData, which is the zero
		byte[] changeV =PIR.changeValue( bData, ret);
 
		
		//store into the database 
		try {
			if (realOFFSET>=0)
			{
				//real fetch a real block, should merge it with others
				
				//fetch a dummy block
				int dummy = rndDummy(realLevel);

				//simulate the server operation
				readBlock(p, dummy, data); 
				byte[] result = PIR.PIRWriteBlock(data, changeV);   
				
				writeBlock(p, dummy, result); 

				pos_map[realBlockID].partition = p;
				pos_map[realBlockID].level = realLevel;
				pos_map[realBlockID].offset = dummy - ((1 << realLevel) - 1);
				
			}else{
				//add to the dummy layer, level #
				//This simulation can do not any operation
			}
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	

	private boolean writeBlock(int p, int _id, byte[] blockData ) throws UnsupportedEncodingException
	{
		byte[] bBlockData=new byte[CommInfo.blockSize];
		System.arraycopy(blockData, 0, bBlockData, 0, CommInfo.blockSize);

		MongoCollection<Document> collection = dbUtil.getCollection("part_"+p);  
		
		String str=new String(bBlockData, "ISO-8859-1"); 

        collection.findOneAndReplace(new Document("_id", _id), new Document("_id", _id).append("data", str));
        
		return true;
	}

	 private void psuedo_random_permute(byte[][] sBuffer, int len) { 
			
		 Random rnd=new Random();
		 byte[] bData=new byte[CommInfo.blockSize];
	        for (int i = len- 1; i > 0; --i) {
	            int j = rnd.nextInt(i); 
	            System.arraycopy(sBuffer[i], 0, bData, 0, CommInfo.blockSize);
	            System.arraycopy(sBuffer[j], 0, sBuffer[i], 0, CommInfo.blockSize);
	            System.arraycopy(bData, 0, sBuffer[j], 0, CommInfo.blockSize); 
	        }
	    }

	 

	private void readBlock(int p, int _id, byte[] recData) {  
		 
		//System.out.println("p _id :" + p + "  " + _id );
		MongoCollection<Document> collection = dbUtil
				.getCollection("part_" + p);
		FindIterable<Document> findIterable = collection.find(new Document(
				"_id", _id));
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		if (mongoCursor.hasNext()) {
			Document doc1 = mongoCursor.next(); 
 
			String bData = (String) doc1.get("data");
			
			byte[] bs;
			try {
				bs = bData.getBytes("ISO-8859-1");
				if (recData!=null)
				    System.arraycopy(bs, 0, recData, 0, CommInfo.blockSize); 
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}  
		} 
	}
	

	/**
	 * get a dummy block position, the return value is its offset, the input realLevel will be change to where it is in
	 * @param level
	 * @return
	 */
	public int rndDummy(int realLevel)
	{ 
		
		int dummyNumber = 0;

		for (int i=0;i<n_levels;i++)
		{
			if (filled[i])
			{
				int begin =  (1 << i) - 1;
				int end = (1 << (i+1));
			    if (i == n_levels - 1) 
			    	end = this.top_level_len;
				
				for (int j=0; j<end; j++ )
				{
					if ( this.blockIDs[begin+j] == CommInfo.dummyID)
						dummyNumber++; 
				}
			}
		}
		
		int dummy = rnd.nextInt(dummyNumber);
		int pos = 0;

		for (int i=0;i<n_levels;i++)
		{
			if (filled[i])
			{
				int begin =  (1 << i) - 1;
				int end = (1 << (i+1));
			    if (i == n_levels - 1) 
			    	end = this.top_level_len;
				
				for (int j=0; j<end; j++ )
				{
					if ( this.blockIDs[begin+j] == CommInfo.dummyID)
					{
						if (pos==dummy)
						{
							realLevel = i;
							return j;
						}
						pos++;
					} 
				}
			}
		}

	    return -1;
	}
	
	/**
	 * From a level to fetch a dummy or noise block
	 * 
	 * it must contain such a block
	 * @param currentLevel
	 * @return
	 */
	public int rndDummyOrNoise(int currentLevel)
	{ 
		int dummyNumber = 0;
		int begin =  (1 << currentLevel) - 1;
		int end = (1 << (currentLevel+1));
	    if (currentLevel == n_levels - 1) 
	    	end = this.top_level_len;
		
		for (int j=0; j<end; j++ )
		{
			if ( this.blockIDs[begin+j] <= CommInfo.dummyID)
				dummyNumber++; 
		} 
			
		
		int dummy = rnd.nextInt(dummyNumber);
		int pos = 0;
		for (int j=0; j<end; j++ )
		{
			if ( this.blockIDs[begin+j] <= CommInfo.dummyID)
			{
				if (pos==dummy)
				{ 
					return j;
				}
				pos++;
			} 
		} 

	    return -1;
	}

	/**
	 * the level is the first unfilled level
	 * @param level
	 * @return
	 */
	public int rndReal(int level)
	{ 
		//level may be is null, i.e., there is no consective levels
		if (level<0)
			return -1;
		
		
		int end = 0;
	    if (level != n_levels - 1) {
	        end =   (1 <<  level ) - 1;
	    } else end = this.n_capacity;
		//compute the position in the position map
	    int realBlocksNumber = 0;
	    for (int i=0;i<end;i++)
		    if (blockIDs[i] > CommInfo.dummyID)
		    	realBlocksNumber++;
	    
	    if (realBlocksNumber==0)
	    	return -1;//there is no real block
	    else{
	    	int pos = rnd.nextInt(realBlocksNumber); 
	    	int j = 0;UpperMoving();
		    for (int i=0;i<end;i++)
			    if (blockIDs[i] > CommInfo.dummyID)
			    {
			    	if (j==pos)
			    		return i;
			    	
			    	j++;
			    }
	    }
	    return -1;
	}
	/**
	 * the level which to be read from
	 * @param level
	 * @return
	 */
	public int rndOffset(int level)
	{  
	    int end=0;
	    if (level != n_levels - 1) {
	        end = 1 << level;
	    } else end = top_level_len;
		 
	    return rnd.nextInt(end);
	}
	 
}