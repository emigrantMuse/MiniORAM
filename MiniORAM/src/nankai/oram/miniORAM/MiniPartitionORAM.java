package nankai.oram.miniORAM;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
 

 




import org.bson.Document;

import com.mongodb.client.MongoCollection;

import nankai.oram.Position;
import nankai.oram.pir.PIRImpl;
import nankai.oram.util.MongDBUtil;
import nankai.oram.interfaces.ORAM;
import nankai.oram.interfaces.SlotObject;
import nankai.oram.util.CommInfo;
import nankai.oram.util.Util;


public class MiniPartitionORAM implements ORAM{
	MongDBUtil dbUtil;
	PIRImpl PIR;
 
	boolean initFlag;
	int N;
	int n_partitions;
	int n_capacity;//the max capacity of a partition -- need the top level
    public int n_levels;	
	int n_blocks;
	int n_realBlocks_p;//the real number of blocks in a partition  
	int counter=0;//for sequneceEvict
	 
	Position pos_map[];//position map
	
	/*************************************************/
	Queue<SlotObject> slots[];
	MiniPartition partions[];

	 
	
	
	/**
	 * Follow this function, openORAM() should be called for the real usage
	 */
	public MiniPartitionORAM()
	{ 
		PIR=new PIRImpl();
		initFlag=false; 
		dbUtil=new MongDBUtil();
	    dbUtil.connect("localhost", 27017); 
	}
	
 
	
	/**
	 * initialize the parameters, storage space and so on
	 * @param nN
	 */
	public boolean init(int nN)
	{
		if (initFlag==true)
		{
			System.out.println("have inited!");
			return false;
		}
		N=nN; 
		n_partitions = (int) Math.ceil(Math.sqrt(nN));

		n_realBlocks_p =   (int) Math.ceil(((double) nN) / n_partitions);
	    n_blocks = n_realBlocks_p * n_partitions;
	    
	    n_levels = (int) (Math.log((double) n_realBlocks_p) / Math.log(2.0));
	    n_capacity = (int) Math.ceil(CommInfo.capacity_parameter_mini * n_realBlocks_p);
		 
	    partions=new MiniPartition[n_partitions];
  
	    pos_map=new Position[n_blocks+100];
	    for (int i=0;i<n_blocks+100; i++)
	    {
	    	pos_map[i]=new Position();
	    }
	    slots=new Queue[n_partitions]; 
	    
	    //randomly generate the keys for each level of each partition 
		for (int i = 0; i < n_partitions; i++)
		{
			slots[i]=new LinkedList<SlotObject>();
			partions[i]=new MiniPartition(N, n_partitions, pos_map, dbUtil, PIR); 
		}
					
	    counter = 0;  

	    initFlag=true;
	    return true;
	}
	
	 
	/**
	 * initialize the oram, run for first time
	 * @return
	 */
	public boolean initORAM()
	{

	    //Create DB and open DB
	    if (!dbUtil.createDB("MiniPartitionORAM"))
	    	return false;
	    //init partitions: create the table/collection for the partitions
	    try {
			initPartitions();
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		    return false;
		}
	    
		
		return true;//cli.responseType!=ResponseType.wrong;
	}
	

	private void initPartitions() throws UnsupportedEncodingException
	{
		int i=0;
		int cipherLen = PIR.getCipherTextLen(CommInfo.blockSize);  

		byte[] bBlockData= PIR.generateZero(cipherLen); 
		String str=new String(bBlockData, "ISO-8859-1"); 
		
		for (i=0;i<n_partitions;i++)
		{
			dbUtil.createCollection("part_"+i);
			
			//insert all the data records into the collection

			MongoCollection<Document> collection = dbUtil.getCollection("part_"+i);
			//Each level, there are max 2^i real blocks, but more than 2^i dummy blocks
			for (int j = -1; j < n_capacity; j++) {
				
				/*collection.insertOne(new Document("_id", j).append("data",
						new String(bBlockData, "ISO-8859-1")));*/
				collection.insertOne(new Document("_id", j).append("data",
						str ));
			} 
 
		}
	}

	/**
	 * Notice the ORAM server to open the database, will use the created database
	 * @return
	 */
	public boolean openORAM()
	{ 
	    if (!dbUtil.openDB("MiniPartitionORAM"))
	    	return false;
	    return true;
	}
	 

	byte[] access(char op, int block_id, byte[] value)
    {
		byte data[] = new byte[CommInfo.blockSize];
		
		int r = 1;
//    	if (Util.debug==false){
//            r = Util.rand_int(n_partitions); 
//        	//not write to the partition with more than the pre-defined real blocks
////        	while ( partions[r].realDataNumber >= 2*this.n_realBlocks_p )
////        		r = Util.rand_int(n_partitions); 
//    	}
    	
        int p = pos_map[block_id].partition; 
        /****************
         * Read data from slots or the server
         * If it is in the slot
         *    readAndDel from the slot
         *    read a dummy block from server
         * Else
         *    read the real block from the server
         * ******************/
		if (p >= 0) {
			boolean isInSlot = false; 
			Iterator itr = slots[p].iterator();
			SlotObject targetObj = null;
			while (itr.hasNext())
			{
				targetObj = (SlotObject)itr.next();
				if (targetObj.id==block_id)
				{
					isInSlot=true;
					break;
				}
			}
			 
			if ( isInSlot ) {
				// in the slot
				System.arraycopy(targetObj.value, 0, data, 0, CommInfo.blockSize); 
				slots[p].remove(targetObj);
				partions[p].readPartition(CommInfo.dummyID);
			} else {
				/**************************
				 * Here, should send a request to the cloud server to get the
				 * data
				 * **************************/ 
				byte[] bReadData=partions[p].readPartition(block_id);
				System.arraycopy(bReadData, 0, data, 0, CommInfo.blockSize); 
			}
		}
       
        pos_map[block_id].partition = r;
        pos_map[block_id].level = -1;
        pos_map[block_id].offset = -1;
        
        if (op == 'w')
        { 
        	data=value;
        }
        SlotObject newObject = new SlotObject(block_id, data);
    	slots[r].add(newObject);

    	//randomEvict(CommInfo.v); 
		evict(r);
        return data;
    }

    void sequentialEvict(int vNumber)
    {   
		for (int i = 0; i < vNumber; i++) {
			counter = (counter + 1) % n_partitions;
			evict(counter);
		}
    }
    void randomEvict(int vNumber)
    {   
    	Random rnd=new Random(); 
		for (int i = 0; i < vNumber; i++) {
			int r = rnd.nextInt(n_partitions); 
			evict(r);
		}
    }
	void evict(int p) {

	    if (slots[p].isEmpty()) { 
	    	partions[p].writePartition(CommInfo.dummyID, new byte[CommInfo.blockSize]);  
	    } else {
	    	//pop a data in slots
	    	SlotObject obj=slots[p].poll();  
	    	partions[p].writePartition( obj.id, obj.value);
	    }
	}
	
	public int getCacheSlotSize()
	{
		int ret = 0;
		for (int i=0; i<slots.length; i++)
		{
			ret += slots[i].size();
		}
		return ret;
	}

	/**
	 * Suggest the client to call this, when it will be closed
	 */
	public void clearSlot()
	{
		for (int i=0; i<slots.length; i++)
		{
			/**************
			 * Here, do not clear directly
			 * Just remove from the slot, but the data should always stored in the database
			 * So, we should update the position map
			 * 
			 */

			while (slots[i].size()>0){
		    	SlotObject obj=slots[i].poll();  
				partions[i].writePartition(obj.id, obj.value);
			}
			slots[i].clear();
		} 
	}

	@Override
	public void write(String idStr, byte[] value) { 
	    access('w', Integer.parseInt(idStr), value); 
	}

	@Override
	public byte[] read(String idStr) { 
	    return access('r', Integer.parseInt(idStr), null); 
	}

	@Override
	public void write(int id, byte[] value) { 
	    access('w', id, value); 
	}

	@Override
	public byte[] read(int id) { 
	    return access('r', id, null);  
	}


}
