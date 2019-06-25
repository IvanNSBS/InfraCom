import java.util.UUID;

public class ClientData implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String cur_message;
	public boolean disconnected;
	
	public boolean ACK;
	String msgID = UUID.randomUUID().toString();
	
	String targetID;
	
	ClientData(String msg) {
		cur_message = msg;
	}
	
	ClientData(boolean dc) {
		disconnected = dc;
	}
	
	ClientData(String msg, boolean dc) {
		cur_message = msg;
		disconnected = dc;
	}
	
	ClientData(boolean ACK, String tgId){
		this.ACK = ACK;
		this.targetID = tgId;
	}
	
	String getMsg() {
		return cur_message;
	}
	
	void set_msg(String msg){
		cur_message = msg;
	}
}
