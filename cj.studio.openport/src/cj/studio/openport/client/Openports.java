package cj.studio.openport.client;

import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.resource.IResource;
import cj.studio.gateway.socket.pipeline.IOutputSelector;
import cj.studio.gateway.socket.pipeline.IOutputer;
import cj.studio.openport.annotations.CjOpenports;
import cj.ultimate.net.sf.cglib.proxy.Enhancer;
import cj.ultimate.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 代表远程的任意开放口<br>
 * 供客户端调用
 */
public class Openports {
    static IServiceSite _site;
    static ThreadLocal<IOutputer> localOutputers;
   static IOutputSelector selector;
   static Map<Class<?>, Object> openportMap;

    private Openports() {
    }

    Openports(IServiceSite site) {
        this._site = site;
        localOutputers = new ThreadLocal<>();
        openportMap = new HashMap<>();
    }
    public static Openports aspect(Class<?> clazz){//获取一个方面接口，方面有支持传统stub的调用，openports的调用，及原生调用。open方法默认的是openports方面
        return null;
    }

    public static  <T> T ports(Class<T> openportInterface) {
        return (T) openportMap.get(openportInterface);
    }

    public boolean isOpened(Class<?> openportInterface) {
        return openportMap.containsKey(openportInterface);
    }

    //
    public static <T> T open(Class<T> openportInterface, String remoteOpenportsUrl, String token) throws CircuitException {
        if (openportInterface == null) {
            throw new CircuitException("406", "参数openportInterface为空");
        }
        if (StringUtil.isEmpty(remoteOpenportsUrl)) {
            throw new CircuitException("406", "参数remoteOpenportsUrl为空");
        }
        if (_site == null) {
            throw new CircuitException("406", "服务站不存在，缺少配置。请在Assembly.json的serviceContainer中配置：monitor: 'cj.studio.openport.client.DefaultOpenportsServicesMonitor'");
        }
        if(openportMap.containsKey(openportInterface)){
//            throw new CircuitException("406", "开放接口已打开，可调用ports方法直接获取使用");
            return (T)openportMap.get(openportInterface);
        }

        if (selector == null) {
            selector = (IOutputSelector) _site.getService("$.output.selector");
        }
        CjOpenports cjOpenports = openportInterface.getAnnotation(CjOpenports.class);
        if (cjOpenports == null) {
            throw new CircuitException("405", "接口没有CjOpenport注解。在：" + openportInterface);
        }
        int pos=remoteOpenportsUrl.indexOf("?");
        String remoteOpenportsPath="";
        if(pos>0){
            remoteOpenportsPath=remoteOpenportsUrl.substring(0,pos);
        }else{
            remoteOpenportsPath=remoteOpenportsUrl;
        }
        if(!remoteOpenportsPath.endsWith("/")){//一定要检查结尾，如果没有扩展名且没以/结束，发给服务器会被返回302重置而不能正确的传输内容，导致服务端内容参数为空
            pos=remoteOpenportsPath.lastIndexOf("/");
            String tmp=remoteOpenportsPath.substring(pos+1,remoteOpenportsPath.length());
            if(tmp.lastIndexOf(".")<0) {//没有文件扩展名
                remoteOpenportsPath = String.format("%s/", remoteOpenportsPath);
            }
        }
        pos = remoteOpenportsPath.indexOf("://");
        if (pos < 0) {
            throw new CircuitException("405", "remoteOpenportsUrl格式不正确(ports://逻辑主机/开放口服务相对路径或http://逻辑主机/开放口服务相对路径，其中的逻辑主机名为任意串，在cluster中若重复则报异常）。remoteOpenportsUrl=" + remoteOpenportsUrl);
        }
        String logicprotocol = remoteOpenportsPath.substring(0, pos);
        String remaining = remoteOpenportsPath.substring(pos + "://".length(), remoteOpenportsPath.length());
        while (remaining.startsWith("/")) {
            remaining = remaining.substring(1, remaining.length());
        }
        pos = remaining.indexOf("/");
        if (pos < 0) {
            throw new CircuitException("405", "缺少上下文。在：" + remoteOpenportsPath);
        }
        String logichost = remaining.substring(0, pos);
        String selectdest = String.format("%s://%s", logicprotocol, logichost);
        String portsUrl = remaining.substring(pos, remaining.length());
        IOutputer outputer = localOutputers.get();
        if (outputer == null) {
            outputer = selector.select(selectdest);
            localOutputers.set(outputer);
        }
        IResource resource = (IResource) _site.getService(IResource.class.getName());
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(resource.getClassLoader());
        enhancer.setCallback(new OpenportAdapterImpl(outputer, openportInterface, portsUrl, token));
        enhancer.setInterfaces(new Class<?>[]{openportInterface});
        T obj = (T) enhancer.create();
        openportMap.put(openportInterface, obj);
        return  obj;
    }

    public IServiceSite site() {
        return _site;
    }

    public static void close() throws CircuitException {
        IOutputer outputer = localOutputers.get();
        if (outputer == null) {
            return;
        }
        outputer.releasePipeline();
    }

    public static void emptyCaches() {
        openportMap.clear();
    }


}
