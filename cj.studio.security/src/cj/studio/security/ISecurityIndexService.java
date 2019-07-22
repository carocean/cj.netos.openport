package cj.studio.security;

import cj.studio.security.annotation.CjPermission;

@CjPermission(tokenIn = TokenIn.none,who = "*",right = Right.allow,usage = "默认的安全服务命令")
public interface ISecurityIndexService extends ISecurityService {
	@CjPermission(usage = "默认索引命令")
	ResponseClient<SecurityServiceAPI> index();
}
