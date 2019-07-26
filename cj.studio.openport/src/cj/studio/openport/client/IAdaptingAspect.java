package cj.studio.openport.client;

import cj.studio.ecm.adapter.IAdapterInterrupter;
import cj.studio.gateway.socket.pipeline.IOutputSelector;
import cj.studio.gateway.socket.pipeline.IOutputer;

import java.util.Map;

public interface IAdaptingAspect extends IAdapterInterrupter {
    void init(ThreadLocal<Map<String, IOutputer>> local, IOutputSelector selector, Class<?> openportInterface, String dest, String portsUrl, String token);
}
