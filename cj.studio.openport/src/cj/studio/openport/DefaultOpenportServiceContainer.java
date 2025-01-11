package cj.studio.openport;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.ServiceCollection;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.resource.IResource;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenports;
import cj.ultimate.util.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DefaultOpenportServiceContainer implements IOpenportServiceContainer, IOpenportPrinter {
    ICheckAppSignStrategy checkAppSignStrategy;
    ICheckAccessTokenStrategy checkAccessTokenStrategy;
    Map<String, OpenportCommand> commands;// key为地址：/myservice.openportService#method1则直接访问到方法,/myservice#method1,因此服务名的索引直接以此作key
    Map<String, CjOpenports> portsMap;//收集服务注解为打印需要
    IServiceSite site;

    public DefaultOpenportServiceContainer(IServiceSite site) {
        portsMap = new HashMap<>();
        commands = new HashMap<>();
        this.site = site;
        String acsStr = (String) site.getService("$.cj.studio.openport.checkAppSignStrategy");
        String ctsStr = (String) site.getService("$.cj.studio.openport.checkAccessTokenStrategy");
        ClassLoader cl=(ClassLoader)site.getService(IResource.class.getName());
        try {
            this.checkAppSignStrategy = (ICheckAppSignStrategy) Class.forName(acsStr,true,cl).newInstance();
            this.checkAccessTokenStrategy = (ICheckAccessTokenStrategy) Class.forName(ctsStr,true,cl).newInstance();
            this.checkAppSignStrategy.init(site);
            this.checkAccessTokenStrategy.init(site);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new EcmException(e);
        }
        IOpenportAPIController controller = new DefaultOpenportAPIController(this);
        site.addService("$.cj.studio.openport.openportAPIController", controller);
        site.addService("$.security.container", this);

        ServiceCollection<?> col = site.getServices(CjOpenports.class);

        for (Object ss : col) {
            CjService cjService = ss.getClass().getAnnotation(CjService.class);
            String sname = cjService.name();
            Class<?>[] faces = ss.getClass().getInterfaces();
            int foundFace = 0;//一个开放服务只能实现一个开放接口
            for (Class<?> c : faces) {
                CjOpenports perm = c.getAnnotation(CjOpenports.class);
                if (perm == null) {
                    CJSystem.logging().warn(getClass(), String.format("缺少注解@CjOpenports，在接口：%s", c.getName()));
                    continue;
                }
                if (foundFace > 0) {
                    throw new EcmException("发现类实现了多个开放口，一个服务只能实现一个开放口。在：" + ss.getClass().getName());
                }
                CJSystem.logging().info(String.format("发现安全服务：%s，类型：%s", sname, ss.getClass().getName()));
                portsMap.put(sname, perm);
                fillCommand(sname, c, ss);
                foundFace++;
            }
        }
    }

    @Override
    public <T> ServiceCollection<T> getServices(Class<T> serviceClazz) {
        return this.site.getServices(serviceClazz);
    }

    @Override
    public Object getService(String serviceId) {
        if ("$.security.container".equals(serviceId)) {
            return this;
        }
        return site.getService(serviceId);
    }

    private void fillCommand(String servicepath, Class<?> face, Object openportService) {
        Method[] methods = face.getMethods();
        for (Method m : methods) {
            CjOpenport openport = m.getAnnotation(CjOpenport.class);
            if (openport == null) {
                continue;
            }

            String key = String.format("%s#%s", servicepath, m.getName());
            CJSystem.logging().info(String.format("\t\t服务命令：%s", m.getName()));
            OpenportCommand cmd = new OpenportCommand(openportService, servicepath, face, m, this.checkAppSignStrategy, this.checkAccessTokenStrategy);
            commands.put(key, cmd);
        }
    }

    @Override
    public void dispose() {
        commands.clear();
    }


    @Override
    public boolean matchesAndSelectKey(Frame frame) throws CircuitException {
        // 根据相对路径要到服务实例，再根据command查找服务实现中的方法。
        // 地址：/myservice.openportService#method1则直接访问到方法,/myservice#method1,因此服务名的索引直接以此作key
        String command = frame.head("Rest-Command");
        if (StringUtil.isEmpty(command)) {
            command = frame.head("rest-command");
        }
        if (StringUtil.isEmpty(command)) {
            command = frame.parameter("Rest-Command");
        }
        if (StringUtil.isEmpty(command)) {
            command = frame.parameter("rest-command");
        }
        if (StringUtil.isEmpty(command)) {
            return false;
        }
        String relpath = frame.relativePath();

        String key = String.format("%s#%s", relpath, command);
        if (this.commands.containsKey(key)) {
            frame.head("Security-SelectedKey", key);
            return true;
        }
        String newrelpath = "";
        if (relpath.endsWith("/")) {
            newrelpath = relpath.substring(0, relpath.length() - 1);
        } else {
            newrelpath = newrelpath + "/";
        }
        key = String.format("%s#%s", newrelpath, command);
        if (this.commands.containsKey(key)) {
            frame.head("Security-SelectedKey", key);
            return true;
        }
        return false;
    }

    @Override
    public void invokeService(Frame frame, Circuit circuit) throws CircuitException {
        if (!frame.containsHead("Security-SelectedKey")) {
            throw new CircuitException("501", "未选择key");
        }
        String key = frame.head("Security-SelectedKey");
        if (StringUtil.isEmpty(key)) {
            return;
        }
        OpenportCommand scmd = this.commands.get(key);
        if (scmd == null) {
            return;
        }
        scmd.doCommand(frame, circuit);
    }

    @Override
    public void printPort(OpenportContext context) {
        Element canvas = context.canvas();
        Element portsUL = canvas.select(".pr-tree>.pr-folders").first();
        Element portsLI = portsUL.select(".pr-folder").first().clone();
        portsUL.empty();
        Map<String,CjOpenports> portsTree=new TreeMap<>();
        portsTree.putAll(portsMap);
        for (String key : portsTree.keySet()) {
            String path = key;
            CjOpenports ports = portsMap.get(key);
            Element cportsli = portsLI.clone();
            cportsli.select(".portsurl").html(path);
            cportsli.attr("portsurl", path);
            cportsli.select(".usage").html(ports.usage() + "");
//            cportsli.select(".simpleHome").html(ports.simpleHome()+"");//这是给开发者自己看的，没必要公开出来
            int count=printPortMethodTree(context, path, cportsli);
            cportsli.select(".folder-count .count").html(count+"个");
            portsUL.appendChild(cportsli);
        }

        Element e = context.canvas().select(".portlet.method-let").first();
        Element portletLI = e.clone();
        Elements letsEs = e.parents().select(".main-column-lets");
        context.canvas().select(".portlet.method-let").remove();
        for (String key:commands.keySet()) {
            String path = key;
            OpenportCommand cmd = commands.get(key);
            Element pli = portletLI.clone();
            pli.attr("porturl", path);
            pli.attr("portname", cmd.method.getName());
            pli.attr("request-url", String.format("%s%s", context.contextPath, cmd.openportPath));
            CjOpenport cport = cmd.method.getAnnotation(CjOpenport.class);
            pli.attr("tokenin", cport.tokenIn() + "");
            pli.attr("checkTokenName", cport.checkTokenName() + "");
            pli.attr("request-command", (cport.command() + "").toLowerCase());
            OpenportContext ctx = new OpenportContext(pli, context.contextPath());
            cmd.printPort(ctx);
            letsEs.append(ctx.canvas().outerHtml());
        }
    }

    private int printPortMethodTree(OpenportContext context, String portPath, Element portli) {
        Element ul = portli.select(".pr-objs").first();
        Element li = ul.select(".pr-obj").first().clone();
        ul.empty();

        int count=0;
        Map<String,OpenportCommand> cmdTree=new TreeMap<>();
        cmdTree.putAll(commands);
        for (Map.Entry<String, OpenportCommand> entry : cmdTree.entrySet()) {
            String path = entry.getKey();
            OpenportCommand cmd = entry.getValue();
            if (!portPath.equals(cmd.openportPath)) {
                continue;
            }
            Element cli = li.clone();
            cli.attr("porturl", path);
            cli.select(".obj-code.portname").html(cmd.method.getName());
            CjOpenport cport = cmd.method.getAnnotation(CjOpenport.class);
            cli.select(".command").html(cport.command());


            ul.appendChild(cli);
            count++;
        }
        return count;
    }
}
