package cj.studio.openport;

import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.gateway.socket.pipeline.IAnnotationInputValve;
import cj.studio.gateway.socket.pipeline.IIPipeline;
import cj.studio.openport.client.Openports;
import cj.studio.openport.util.ExceptionPrinter;

/**
 * 该类在openport服务器端才用到，用于调度ports请求
 */
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
        if (controller == null) {
            controller = (IOpenportAPIController) container.getService("$.cj.studio.openport.openportAPIController");
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
            try {
                controller.flow(frame, circuit);
            } catch (Throwable e) {
                throw e;
            } finally {
                Openports.close();
            }
            return;
        }
        if (!container.matchesAndSelectKey(frame)) {
            try {
                pipeline.nextFlow(request, response, this);
            } catch (Throwable e) {
                throw e;
            } finally {
                Openports.close();
            }
            return;
        }

        try {
            container.invokeService(frame, circuit);
        } catch (Throwable e) {
            ExceptionPrinter printer = new ExceptionPrinter();
            printer.printException(e, circuit);
        } finally {
            Openports.close();
        }
    }


    @Override
    public void onInactive(String inputName, IIPipeline pipeline) throws CircuitException {
        pipeline.nextOnInactive(inputName, this);
    }

    /**
     * 该vavle顺序永远是1
     * @return
     */
    @Override
    public final int getSort() {
        return 1;
    }


}
