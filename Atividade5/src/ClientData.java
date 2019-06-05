
public class ClientData implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String cur_message;
	
	ClientData(String msg) {
		cur_message = msg;
	}
	
	String getMsg() {
		return cur_message;
	}
	
	void set_msg(String msg){
		cur_message = msg;
	}
}
