package cj.studio.openport;

import cj.studio.ecm.IChip;
import cj.studio.ecm.IChipInfo;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.openport.api.IOpenportResource;
import cj.studio.openport.api.OpenportResource;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DefaultOpenportAPIController implements IOpenportAPIController {
    IOpenportServiceContainer container;
    IServiceSite site;
    public DefaultOpenportAPIController(IServiceSite site) {
        this.site=site;
        container = (IOpenportServiceContainer) site.getService("$.security.container");
    }
    @Override
    public void flow(Frame frame, Circuit circuit) throws CircuitException {
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
                OpenportContext portContext = new OpenportContext(canvas,frame.rootPath());
                new PrintChipInfo().printPort(portContext);
                container.printPort(portContext);
                circuit.content().writeBytes(canvas.toString().getBytes());
                break;
        }
    }
    @Override
    public boolean matchesAPI(Frame frame) {
        return frame.relativePath().startsWith("/cjstudio/openport/");
    }

    class  PrintChipInfo implements  IOpenportPrinter{

        @Override
        public void printPort(OpenportContext context) {
            IChip chip=(IChip) site.getService(IChip.class.getName());
            IChipInfo info=chip.info();
            Element canvas=context.canvas();
            Element welcomeE=canvas.select(".portlet.welcome-let").first();
            welcomeE.select(".chipname").html(info.getName()+"");
            welcomeE.select(".guid").html(info.getId()+"");
            welcomeE.select(".version").html(info.getVersion()+"");
            welcomeE.select(".product").html(info.getProduct()+"");
            welcomeE.select(".company").html(info.getCompany()+"");
            welcomeE.select(".copyright").html(info.getCopyright()+"");
            welcomeE.select(".desc").html(info.getDescription()+"");
        }
    }
}
