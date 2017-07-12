package nankai.oram.interfaces;

public interface ORAM {

	public void write(String idStr, byte[] value);
	public byte[] read(String idStr);
	public void write(int id, byte[] value);
	public byte[] read(int id);

}
