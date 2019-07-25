package cj.studio.openport.client;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.IAssemblyContext;
import cj.studio.ecm.IChipPlugin;
import cj.studio.ecm.context.IElement;
import cj.studio.ecm.context.INode;
import cj.studio.ecm.context.IProperty;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供开放口芯片插件<br>
 * 有此插件就可以像用普通服务一样使用openports了，而不必直接使用Openports工具类
 */
public class OpenportChipPlugin implements IChipPlugin {

    Map<String, OpenportsConfig> map;

    @Override
    public void load(IAssemblyContext ctx, IElement args) {
        map = new HashMap<>();
        INode portsNode = args.getNode("ports");
        if (portsNode == null) return;
        IProperty property = (IProperty) portsNode;
        if (property.getValue() == null) {
            return;
        }
        String json = property.getValue().getName();
        if (StringUtil.isEmpty(json)) {
            return;
        }
        CJSystem.logging().info("openports插件发现配置：");
        List<Object> list = new Gson().fromJson(json, ArrayList.class);
        for (Object obj : list) {
            Map<String, String> config = (Map<String, String>) obj;
            String openportInterface = config.get("openportInterface");
            String remoteOpenportsUrl = config.get("remoteOpenportsUrl");
            String token = config.get("token");
            Class<?> clazz = null;
            try {
                clazz = Class.forName(openportInterface, true, ctx.getResource().getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new EcmException(e);
            }
            OpenportsConfig oc = new OpenportsConfig();
            oc.openportsInterface = clazz;
            oc.remoteOpenportsUrl = remoteOpenportsUrl;
            oc.token = token;
            map.put(openportInterface, oc);
            CJSystem.logging().info(String.format("\t\topenportInterface=%s remoteOpenportsUrl=%s token=%s", openportInterface, remoteOpenportsUrl, token));
        }
    }

    @Override
    public void unload() {
        map.clear();
        Openports.removeClosedPorts();
    }

    @Override
    public Object getService(String serviceId) {
        if (StringUtil.isEmpty(serviceId)) {
            return null;
        }
        String openportsInterface = serviceId;
        if (!map.containsKey(openportsInterface)) {
            throw new EcmException("没有Openports的相关配置，请求：" + openportsInterface);
        }
        OpenportsConfig config = map.get(openportsInterface);
        try {
            return Openports.open(config.openportsInterface, config.remoteOpenportsUrl, config.token);
        } catch (CircuitException e) {
            throw new EcmException(e);
        }
    }

    class OpenportsConfig {
        Class<?> openportsInterface;
        String remoteOpenportsUrl;
        String token;

    }
}
