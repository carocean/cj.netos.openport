package cj.studio.security;

import java.util.Map;

import cj.studio.ecm.IServiceSite;
import cj.studio.security.annotation.Right;

public class DefaultAccessControlStrategy implements IAccessControlStrategy {

	@Override
	public void init(IServiceSite site) {
	}

	@Override
	public boolean isInvisible(String[] acl) {
		if (acl == null || acl.length == 0)
			return true;
		for (String ace : acl) {
			int pos=ace.indexOf(" ");
			String rightText=ace.substring(0,pos);
			
			Right right=Right.valueOf(rightText);
			if(right!=Right.invisible) {
				continue;
			}
			String remaining=ace.substring(pos+1,ace.length());
			while(remaining.startsWith(" ")) {
				remaining=remaining.substring(1,remaining.length());
			}
			while(remaining.endsWith(" ")) {
				remaining=remaining.substring(0,remaining.length()-1);
			}
			if("*".equals(remaining)||"*.user".equals(remaining)||"*.role".equals(remaining)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void checkRight(Map<String, Object> tokenInfo, String[] acl) throws CheckRightException {
		// 检查权限
		System.out.println("检查权限:" + tokenInfo + " acl:" + acl);
	}

}
