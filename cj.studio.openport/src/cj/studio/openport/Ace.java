package cj.studio.openport;

public class Ace {
	private String whois;
	private Right right;
	public Ace() {
		// TODO Auto-generated constructor stub
	}
	public Ace(String whois, Right right) {
		super();
		this.whois = whois;
		this.right = right;
	}
	public String whois() {
		return whois;
	}
	public void whois(String whois) {
		this.whois = whois;
	}
	public Right right() {
		return right;
	}
	public void right(Right right) {
		this.right = right;
	}
	/**
	 * 二维数组，将whois的格式解析，如：cj.user，第一个是用户名或者角色，支持通配符，第二个是身份，如user说明第一个是用户名，role说明第一个是角色
	 * @return
	 */
	public String[] whoisDetails() {
		String[] arr=new String[2];
		int pos=whois.lastIndexOf(".");
		if(pos>0) {
			arr[0]=whois.substring(0,pos);
			arr[1]=whois.substring(pos+1,whois.length());
		}else {
			arr[0]=whois;
		}
		return arr;
	}
}
