package cj.studio.openport;

import java.util.ArrayList;
import java.util.List;

import cj.studio.ecm.EcmException;
import cj.ultimate.util.StringUtil;

public class Acl {
	private List<Ace> allows;
	private List<Ace> denys;
	private List<Ace> invisibles;

	public Acl() {
		this.allows = new ArrayList<>();
		this.denys = new ArrayList<>();
		this.invisibles = new ArrayList<>();
	}
	public boolean isEmpty() {
		return allows.isEmpty()&&denys.isEmpty()&&invisibles.isEmpty();
	}
	public void addAce(String aceText) {
		if(StringUtil.isEmpty(aceText)) {
			throw new EcmException("ace声明为空。");
		}
		int pos = aceText.indexOf(" ");
		if(pos<0) {
			throw new EcmException("ace声明错误，格式应为：allow whois，当前格式只有一个字："+aceText);
		}
		String rightText = aceText.substring(0, pos);

		Rights rights = Rights.valueOf(rightText);
		
		String remaining = aceText.substring(pos + 1, aceText.length());
		while (remaining.startsWith(" ")) {
			remaining = remaining.substring(1, remaining.length());
		}
		while (remaining.endsWith(" ")) {
			remaining = remaining.substring(0, remaining.length() - 1);
		}
		if(remaining.indexOf(" ")>-1) {
			throw new EcmException("ace声明错误，格式应为：allow whois，当前格式具有多个字："+aceText);
		}
		Ace e = new Ace(remaining, rights);
		switch (rights) {
		case allow:
			allows.add(e);
			break;
		case deny:
			denys.add(e);
			break;
		case invisible:
			invisibles.add(e);
			break;
		}
		
	}

	public void removeAce(String aceText) {

	}

	public int allowCount() {
		return allows.size();
	}

	public Ace allow(int index) {
		return allows.get(index);
	}

	public int denyCount() {
		return denys.size();
	}

	public Ace deny(int index) {
		return denys.get(index);
	}

	public int invisibleCount() {
		return invisibles.size();
	}

	public Ace invisible(int index) {
		return invisibles.get(index);
	}
	public void empty() {
		this.allows.clear();
		this.denys.clear();
		this.invisibles.clear();
	}
	public boolean hasUserOnInvisibles(String user) {
		for(Ace e:invisibles) {
			String[] whoisArr=e.whoisDetails();
			if(!"user".equals(whoisArr[1])&&!"*".equals(whoisArr[1])) {
				continue;
			}
			if("*".equals(whoisArr[1])||"*".equals(whoisArr[0])) {
				return true;
			}
			if(user.equals("*")) {
				return true;
			}
			if(user.equals(whoisArr[0])) {
				return true;
			}
		}
		return false;
	}
	public boolean hasRoleOnInvisibles(List<String> roles) {
		for(Ace e:invisibles) {
			String[] whoisArr=e.whoisDetails();
			if(!"role".equals(whoisArr[1])&&!"*".equals(whoisArr[1])) {
				continue;
			}
			if("*".equals(whoisArr[1])||"*".equals(whoisArr[0])) {
				return true;
			}
			if(roles.contains("*")) {
				return true;
			}
			if(roles.contains(whoisArr[0])) {
				return true;
			}
		}
		return false;
	}
	public boolean hasUserOnDenys(String user) {
		for(Ace e:denys) {
			String[] whoisArr=e.whoisDetails();
			if(!"user".equals(whoisArr[1])&&!"*".equals(whoisArr[1])) {
				continue;
			}
			if("*".equals(whoisArr[1])||"*".equals(whoisArr[0])) {
				return true;
			}
			if(user.equals("*")) {
				return true;
			}
			if(user.equals(whoisArr[0])) {
				return true;
			}
		}
		return false;
	}
	public boolean hasRoleOnDenys(List<String> roles) {
		for(Ace e:denys) {
			String[] whoisArr=e.whoisDetails();
			if(!"role".equals(whoisArr[1])&&!"*".equals(whoisArr[1])) {
				continue;
			}
			if("*".equals(whoisArr[1])||"*".equals(whoisArr[0])) {
				return true;
			}
			if(roles.contains("*")) {
				return true;
			}
			if(roles.contains(whoisArr[0])) {
				return true;
			}
		}
		return false;
	}
	public boolean hasUserOnAllows(String user) {
		for(Ace e:allows) {
			String[] whoisArr=e.whoisDetails();
			if(!"user".equals(whoisArr[1])&&!"*".equals(whoisArr[1])) {
				continue;
			}
			if("*".equals(whoisArr[1])||"*".equals(whoisArr[0])) {
				return true;
			}
			if(user.equals("*")) {
				return true;
			}
			if(user.equals(whoisArr[0])) {
				return true;
			}
		}
		return false;
	}
	public boolean hasRoleOnAllows(List<String> roles) {
		for(Ace e:allows) {
			String[] whoisArr=e.whoisDetails();
			if(!"role".equals(whoisArr[1])&&!"*".equals(whoisArr[1])) {
				continue;
			}
			if("*".equals(whoisArr[1])||"*".equals(whoisArr[0])) {
				return true;
			}
			if(roles.contains("*")) {
				return true;
			}
			if(roles.contains(whoisArr[0])) {
				return true;
			}
		}
		return false;
	}
}
