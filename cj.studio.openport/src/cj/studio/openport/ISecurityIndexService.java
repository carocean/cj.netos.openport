package cj.studio.openport;

import cj.studio.openport.annotations.CjPermission;

@CjPermission(acl = "allow *",usage = "默认的安全服务命令")
public interface ISecurityIndexService extends ISecurityService {
	@CjPermission(usage = "默认索引命令")
	ResponseClient<IAPIPrinter> index();
}
