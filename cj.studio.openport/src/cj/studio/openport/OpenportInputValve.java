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

    @Override
    public void onActive(String inputName, IIPipeline pipeline) throws CircuitException {
        if (container == null) {
            container = (IOpenportServiceContainer) site.getService("$.security.container");
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
        if (container.matchesAPI(frame)) {//注意实现的api时默认的index方法，即当访问一个安全服务的根时打印该服务api，另外返回值样本能让开发者通过注解关联json数据文件
            printOpenportApi(frame, circuit);
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
        IOpenportResource resource = new OpenportResource(site);
        String path = frame.relativePath();
        switch (path) {
            case "/cjstudio/openport/global.css":
                resource.flush("cj/studio/openport/api/global.css", circuit.content());
                break;
            case "/cjstudio/openport/index.css":
                resource.flush("cj/studio/openport/api/index.css", circuit.content());
                break;
            case "/cjstudio/openport/jquery.js":
                resource.flush("cj/studio/openport/api/jquery-2.1.4.js", circuit.content());
                break;
            case "/cjstudio/openport/workspace.js":
                resource.flush("cj/studio/openport/api/workspace.js", circuit.content());
                break;
            default:
                Document canvas = resource.html("cj/studio/openport/api/index.html");
                Elements cssSet = canvas.select("head>link");
                for (Element css : cssSet) {
                    css.attr("href",String.format("/%s/%s", frame.rootName(), css.attr("href")));
                }
                Elements jsSet = canvas.select("head>script");
                for (Element js : jsSet) {
                    js.attr("src",String.format("/%s/%s", frame.rootName(), js.attr("src")));
                }
                OpenportContext portContext = new OpenportContext(canvas);
                container.printPort(portContext);
                circuit.content().writeBytes(canvas.toString().getBytes());
                break;
        }

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
