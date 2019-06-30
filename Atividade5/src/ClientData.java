public class ClientData implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public String cur_message;
	
	public boolean deleteRequest = false;
	
	public boolean disconnected = false;
	
	public String clientID;
	public boolean ACKReceive = false, ACKVis = false;
	
	public int occurrence = -1;
	
	ClientData(String msg, String clientID) {
		cur_message = msg;
		this.clientID = clientID;
	}
	
	ClientData(boolean dc) {
		disconnected = dc;
	}
	
	ClientData(String msg, boolean dc) {
		cur_message = msg;
		disconnected = dc;
	}
	
	ClientData(boolean deleteRequest, String msg, int ocr ){
		cur_message = msg;
		this.deleteRequest = deleteRequest;
		occurrence = ocr;
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
