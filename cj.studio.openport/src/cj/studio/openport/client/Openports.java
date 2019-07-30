package cj.studio.openport.client;

import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.resource.IResource;
import cj.studio.gateway.socket.pipeline.IOutputSelector;
import cj.studio.gateway.socket.pipeline.IOutputer;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.annotations.CjOpenports;
import cj.ultimate.net.sf.cglib.proxy.Enhancer;
import cj.ultimate.util.StringUtil;

import java.util.Map;

/**
 * 代表远程的任意开放口<br>
 * 供客户端调用
 */
public class Openports {
    static IServiceSite _site;
    static IOutputSelector selector;
    /*
     * output在系统中不关闭生成的管道不释放会导致问题，所以又必须释放。
     * 而放给上层开发者释放，由于他们不知何时要释放管道，所以在使用已释放管道的ports对象导致网络关闭异常
     * 而如果每次都释放时，如果当ports方法调用port方法这样一层层调用下去的时候，会导致一次根方法的调用生成多个output
     * 因而，必须使用本地线程，使用它可以在一次执行序共享管道
     */
    static ThreadLocal<Map<String, IOutputer>> local;

    private Openports() {
    }

    Openports(IServiceSite site) {
        this._site = site;
        local = new ThreadLocal<>();
    }

    /**
     * 在出线程时关闭<br>
     *     如果忘记关闭也没关系，最后内核会调用该方法<br>
     *     注意：如果主动调用了该方法关闭也没问题，只是在关闭后又使用了先前创建的口对象，则口对象会重新建立管道，最后由内核回收。
     * @throws CircuitException
     */
    public static synchronized void close() throws CircuitException {
        if(local==null){
            return;
        }
        Map<String, IOutputer> map = local.get();
        if (map == null||map.isEmpty()) {
            return;
        }
        String[] keys = map.keySet().toArray(new String[0]);
        for (String key : keys) {
            IOutputer outputer = map.get(key);
            if (outputer == null || outputer.isDisposed()) {
                continue;
            }
            outputer.releasePipeline();
        }
        map.clear();
        local.remove();
    }

    /**
     * 打开一个接口<br>
     *
     * @param openportInterface  支持开放口接口和IRequestAdapter接口。注：IRequestAdapter接口对象不会被缓冲
     * @param remoteOpenportsUrl 远程地址，格式：<br>
     *                           ports://逻辑主机/开放口服务相对路径或http://逻辑主机/开放口服务相对路径，其中的逻辑主机名为任意串，在cluster中若重复则报异常
     * @param token              令牌，如果有
     * @param <T>
     * @return 返回的任何对象均可强制转换为IClosed接口以判断对象是否关闭了远程访问
     * @throws CircuitException
     */
    public synchronized static <T> T open(Class<T> openportInterface, String remoteOpenportsUrl, String token) throws CircuitException {
        if (openportInterface == null) {
            throw new CircuitException("406", "参数openportInterface为空");
        }
        if (StringUtil.isEmpty(remoteOpenportsUrl)) {
            throw new CircuitException("406", "参数remoteOpenportsUrl为空");
        }
        if (_site == null) {
            throw new CircuitException("406", "服务站不存在，缺少配置。请在Assembly.json的serviceContainer中配置：monitor: 'cj.studio.openport.client.DefaultOpenportsServicesMonitor'");
        }
        if (!openportInterface.isInterface()) {
            throw new CircuitException("406", "参数openportInterface为不是接口");
        }
        if (selector == null) {
            selector = (IOutputSelector) _site.getService("$.output.selector");
        }

        int pos = remoteOpenportsUrl.indexOf("?");
        String remoteOpenportsPath = "";
        if (pos > 0) {
            remoteOpenportsPath = remoteOpenportsUrl.substring(0, pos);
        } else {
            remoteOpenportsPath = remoteOpenportsUrl;
        }
        if (!remoteOpenportsPath.endsWith("/")) {//一定要检查结尾，如果没有扩展名且没以/结束，发给服务器会被返回302重置而不能正确的传输内容，导致服务端内容参数为空
            pos = remoteOpenportsPath.lastIndexOf("/");
            String tmp = remoteOpenportsPath.substring(pos + 1, remoteOpenportsPath.length());
            if (tmp.lastIndexOf(".") < 0) {//没有文件扩展名
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

        IAdaptingAspect aspect = selectAsepct(openportInterface);
        aspect.init(local, selector, openportInterface, selectdest, portsUrl, token);

        IResource resource = (IResource) _site.getService(IResource.class.getName());
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(resource.getClassLoader());
        enhancer.setCallback(aspect);
        enhancer.setInterfaces(new Class<?>[]{openportInterface});
        T obj = (T) enhancer.create();
        return obj;
    }

    private static <T> IAdaptingAspect selectAsepct(Class<T> openportInterface) throws CircuitException {
        if (IOpenportService.class.isAssignableFrom(openportInterface)) {
            CjOpenports cjOpenports = openportInterface.getAnnotation(CjOpenports.class);
            if (cjOpenports == null) {
                throw new CircuitException("405", "接口没有CjOpenport注解。在：" + openportInterface);
            }
            return new OpenportAdaptingAspect();
        }
        if (IRequestAdapter.class.isAssignableFrom(openportInterface)) {
            return new RequestAdapterAspect();
        }
        throw new CircuitException("405", "没有可用的适配方面");
    }

    /**
     * 获取当前服务站
     *
     * @return
     */
    public IServiceSite site() {
        return _site;
    }


}
