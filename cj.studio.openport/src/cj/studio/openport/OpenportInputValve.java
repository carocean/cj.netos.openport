package cj.studio.openport;

import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.ICircuitContent;
import cj.studio.gateway.socket.pipeline.IAnnotationInputValve;
import cj.studio.gateway.socket.pipeline.IIPipeline;
import cj.studio.openport.api.IOpenportResource;
import cj.studio.openport.api.OpenportResource;
import cj.studio.openport.util.ExceptionPrinter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class OpenportInputValve implements IAnnotationInputValve {
    IOpenportServiceContainer container;
    @CjServiceSite
    IServiceSite site;
    IOpenportAPIController controller;
    @Override
    public void onActive(String inputName, IIPipeline pipeline) throws CircuitException {
        if (container == null) {
            container = (IOpenportServiceContainer) site.getService("$.security.container");
        }
        if(controller==null){
            controller=(IOpenportAPIController)container.getService("$.cj.studio.openport.openportAPIController");
        }
        pipeline.nextOnActive(inputName, this);
    }

    @Override
    public void flow(Object request, Object response, IIPipeline pipeline) throws CircuitException {
        if (!(request instanceof Frame)) {
            return;
        }
        Frame frame = (Frame) request;
        Circuit circuit = (Circuit) response;
        if (controller.matchesAPI(frame)) {//注意实现的api时默认的index方法，即当访问一个安全服务的根时打印该服务api，另外返回值样本能让开发者通过注解关联json数据文件
            controller.flow(frame, circuit);
            return;
        }
        if (!container.matchesAndSelectKey(frame)) {
            pipeline.nextFlow(request, response, this);
            return;
        }

        try {
            container.invokeService(frame, circuit);
        } catch (Throwable e) {
            ExceptionPrinter printer = new ExceptionPrinter();
            printer.printException(e, circuit);
        }
    }

    protected void printOpenportApi(Frame frame, Circuit circuit) throws CircuitException {


    }


    @Override
    public void onInactive(String inputName, IIPipeline pipeline) throws CircuitException {
        pipeline.nextOnInactive(inputName, this);
    }

    @Override
    public int getSort() {
        return 0;
    }


}
