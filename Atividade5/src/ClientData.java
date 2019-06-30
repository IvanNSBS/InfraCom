public class ClientData implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String cur_message;
	public boolean deleteRequest;
	
	public boolean disconnected;
	
	public String clientID;
	public boolean ACKReceive, ACKVis;
	
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
	
	ClientData(boolean ACKRcv, boolean ACKVis, String clientID, String msg){
		this.ACKReceive = ACKRcv;
		this.ACKVis = ACKVis;
		this.clientID = clientID;
		cur_message = msg;
	}
	
	String getMsg() {
		return cur_message;
	}
	
	void set_msg(String msg){
		cur_message = msg;
	}
}
