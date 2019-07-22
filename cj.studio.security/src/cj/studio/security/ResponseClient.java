package cj.studio.security;
/**
 * 许可方法的返回值要么为空，要么必须为该类型
 * @author caroceanjofers
 *
 * @param <T>
 */
public final class ResponseClient<T> {
	int status;
	String message;
	T data;
	public ResponseClient() {
	}
	
	public ResponseClient(int status, String message, T data) {
		super();
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	
}
