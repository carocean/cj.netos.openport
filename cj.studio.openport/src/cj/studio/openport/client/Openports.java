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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代表远程的任意开放口<br>
 * 供客户端调用
 */
public class Openports {
    static IServiceSite _site;
    static Map<String, IOutputer> outputers;//一个目标一个outputer
    static IOutputSelector selector;
    static Map<Class<?>, Object> openportMap;

    private Openports() {
    }

    Openports(IServiceSite site) {
        this._site = site;
        outputers = new ConcurrentHashMap<>();
        openportMap = new ConcurrentHashMap<>();

    }

    public static boolean checkPortsInstanceIsClosed(Object ports) {
        if (!(ports instanceof IClosed)) {
            return false;
        }
        IClosed closed = (IClosed) ports;
        return closed.__$_is_$_closed_$_outputer___();
    }

    public static boolean checkPortsInterfaceIsClosed(Class<?> portsClazz) {
        Object ports = openportMap.get(portsClazz);
        if (ports == null) return true;
        if (!(ports instanceof IClosed)) {
            return false;
        }
        IClosed closed = (IClosed) ports;
        return closed.__$_is_$_closed_$_outputer___();
    }

    public static <T> T ports(Class<T> openportInterface) {
        if (checkPortsInterfaceIsClosed(openportInterface)) {
            openportMap.remove(openportInterface);
        }
        return (T) openportMap.get(openportInterface);
    }

    /**
     * 检查接口是否已打开
     *
     * @param openportInterface
     * @return
     */
    public boolean isOpened(Class<?> openportInterface) {
        if (checkPortsInterfaceIsClosed(openportInterface)) {
            openportMap.remove(openportInterface);
        }
        return openportMap.containsKey(openportInterface);
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
        if (!openportInterface.isInterface()) {
            throw new CircuitException("406", "参数openportInterface为不是接口");
        }
        if (openportMap.containsKey(openportInterface)) {
            if (checkPortsInterfaceIsClosed(openportInterface)) {
                openportMap.remove(openportInterface);
            } else {
                return (T) openportMap.get(openportInterface);
            }
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
        IOutputer outputer = outputers.get(selectdest);
        if (outputer == null) {
            outputer = selector.select(selectdest);
            outputers.put(selectdest, outputer);
        }else{
            if(outputer.isDisposed()){
                outputers.remove(selectdest);
                outputer = selector.select(selectdest);
                outputers.put(selectdest, outputer);
            }
        }

        IAdaptingAspect aspect = selectAsepct(openportInterface);
        aspect.init(outputer, openportInterface, portsUrl, token);

        IResource resource = (IResource) _site.getService(IResource.class.getName());
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(resource.getClassLoader());
        enhancer.setCallback(aspect);
        enhancer.setInterfaces(new Class<?>[]{openportInterface, IClosed.class});
        T obj = (T) enhancer.create();
        if (!(obj instanceof IRequestAdapter)) {//不是万能接口则缓冲，万能接口可以匹配无穷远程目标，因此需要特别的缓冲区，干脆留给应用层开发者实现
            openportMap.put(openportInterface, obj);
        }
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

    /**
     * 关闭所有。
     *
     * @throws CircuitException
     */
    public static void close() throws CircuitException {
        for(Map.Entry<String,IOutputer> entry:outputers.entrySet()){
            entry.getValue().releasePipeline();
        }
        outputers.clear();
        removeClosedPorts();
    }

    /**
     * 清除缓冲区中的已关闭的对象
     */
    public static void removeClosedPorts() {
        Class<?>[] keys = openportMap.keySet().toArray(new Class<?>[0]);
        for (Class<?> key : keys) {
            Object v = openportMap.get(key);
            IClosed closed = (IClosed) v;
            if (closed.__$_is_$_closed_$_outputer___()) {
                openportMap.remove(key);
            }
        }
    }

    /**
     * 清空缓冲区中的口对象
     */
    public static void emptyCachePorts() {
        openportMap.clear();
    }


}
