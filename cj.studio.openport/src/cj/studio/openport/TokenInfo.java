package cj.studio.openport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenInfo {
	String user;
	List<String> roles;
	Map<String, Object> props;

	public TokenInfo() {
		props = new HashMap<>();
		roles = new ArrayList<>();
	}

	public TokenInfo(String user) {
		this();
		this.user = user;
	}

	public Map<String, Object> getProps() {
		return props;
	}

	public List<String> getRoles() {
		return roles;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
