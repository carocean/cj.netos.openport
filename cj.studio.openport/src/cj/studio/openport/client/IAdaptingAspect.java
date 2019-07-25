package cj.studio.openport.client;

import cj.studio.ecm.adapter.IAdapterInterrupter;
import cj.studio.gateway.socket.pipeline.IOutputer;

public interface IAdaptingAspect extends IAdapterInterrupter {
    void init(IOutputer outputer,
              Class<?> face,
              String portsUrl,
              String token);
}
