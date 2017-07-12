package nankai.oram.interfaces;

import nankai.oram.util.CommInfo;
 

public class SlotObject{
	public int id;
	public byte[] value;
	public SlotObject()
	{
		id=0;
		value=new byte[CommInfo.blockSize];
	}
	public SlotObject(int _id, byte[] _value)
	{
		id=_id;
		value=new byte[CommInfo.blockSize];
		System.arraycopy(_value, 0, value, 0, CommInfo.blockSize);
	}
}