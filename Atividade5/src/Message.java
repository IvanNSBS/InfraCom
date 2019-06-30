public class Message implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	//string of message to be sent or message to be
	//deleted, rcvACK'd or visACK'd
	public String cur_message = "";
	
	//boolean that tells wether this message is a
	//delete request or not
	public boolean deleteRequest = false;
	//the index of occurrence of the sent message
	//used to know which message to delete on a delete
	//request
	public int occurrence = -1;
	
	//boolean that tells whether this message is a
	//disconnect request or not
	public boolean disconnected = false;
	
	//The ID of the client that sent the message
	public String clientID = "";
	
	//booleans to tell if the message is an ACK
	//to a certain message of some client
	public boolean ACKReceive = false, ACKVis = false;
	
	
	//constructor for simple sent message
	Message(String msg, String clientID) {
		cur_message = msg;
		this.clientID = clientID;
	}
	
	//constructor for disconnect request
	Message(String msg, boolean dc, String clientID) {
		cur_message = msg;
		disconnected = dc;
		this.clientID = clientID;
	}
	
	//constructor for delete request
	Message(boolean deleteRequest, String msg, int ocr ){
		cur_message = msg;
		this.deleteRequest = deleteRequest;
		occurrence = ocr;
	}
	
	//constructor for rcvACK and visACK
	Message(boolean ACKRcv, boolean ACKVis, String clientID, String msg){
		this.ACKReceive = ACKRcv;
		this.ACKVis = ACKVis;
		this.clientID = clientID;
		cur_message = msg;
	}
	
	String getMsg() {
		return cur_message;
	}
}
