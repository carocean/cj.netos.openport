package cj.studio.openport;

import cj.studio.ecm.IChip;
import cj.studio.ecm.IChipInfo;
import cj.studio.ecm.IServiceProvider;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.openport.api.IOpenportResource;
import cj.studio.openport.api.OpenportResource;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class DefaultOpenportAPIController implements IOpenportAPIController {
    IOpenportServiceContainer container;
    IServiceProvider site;
    String publicAPIPath;
    Map<String,String > resources;
    public DefaultOpenportAPIController(IServiceProvider site) {
        this.site = site;
        container = (IOpenportServiceContainer) site.getService("$.security.container");
        publicAPIPath = (String) site.getService("$.cj.studio.openport.publicAPIPath");
        initResources();

    }

    protected void initResources() {
        this.resources = new HashMap<>();
        String apipath=publicAPIPath;
        if(!apipath.endsWith("/")){
            apipath=apipath+"/";
        }
        publicAPIPath=apipath;
        String global_css=String.format("%sglobal.css",apipath);
        String index_css=String.format("%sindex.css",apipath);
        String json_css=String.format("%sjson.css",apipath);
        String jquery_js=String.format("%sjquery.js",apipath);
        String openport_js=String.format("%sopenport.js",apipath);
        resources.put(global_css, "cj/studio/openport/api/resource/global.css");
        resources.put(index_css, "cj/studio/openport/api/resource/index.css");
        resources.put(json_css, "cj/studio/openport/api/resource/json.css");
        resources.put(jquery_js, "cj/studio/openport/api/resource/jquery-2.1.4.js");
        resources.put(openport_js, "cj/studio/openport/api/resource/openport.js");
    }

    @Override
    public void flow(Frame frame, Circuit circuit) throws CircuitException {
        IOpenportResource resource = new OpenportResource(site);
        String path = frame.relativePath();
        if(resources.containsKey(path)){
            String resourcepath=resources.get(path);
            resource.flush(resourcepath, circuit.content());
            return;
        }
        Document canvas = resource.html("cj/studio/openport/api/resource/index.html");
        Elements cssSet = canvas.select("head>link");
        for (Element css : cssSet) {
            String href=String.format("/%s%s%s",frame.rootName(), publicAPIPath, css.attr("href"));
            css.attr("href", href);
        }
        Elements jsSet = canvas.select("head>script");
        for (Element js : jsSet) {
            String src=String.format("/%s%s%s",frame.rootName(), publicAPIPath, js.attr("src"));
            js.attr("src", src);
        }
        OpenportContext portContext = new OpenportContext(canvas, frame.rootPath());
        new PrintChipInfo().printPort(portContext);
        container.printPort(portContext);
        circuit.content().writeBytes(canvas.toString().getBytes());
    }

    @Override
    public boolean matchesAPI(Frame frame) {
        return frame.relativePath().startsWith(publicAPIPath);
    }

    class PrintChipInfo implements IOpenportPrinter {

        @Override
        public void printPort(OpenportContext context) {
            IChip chip = (IChip) site.getService(IChip.class.getName());
            IChipInfo info = chip.info();
            Element canvas = context.canvas();
            Element welcomeE = canvas.select(".portlet.welcome-let").first();
            welcomeE.select(".chipname").html(info.getName() + "");
            welcomeE.select(".guid").html(info.getId() + "");
            welcomeE.select(".version").html(info.getVersion() + "");
            welcomeE.select(".product").html(info.getProduct() + "");
            welcomeE.select(".company").html(info.getCompany() + "");
            welcomeE.select(".copyright").html(info.getCopyright() + "");
            welcomeE.select(".desc").html(info.getDescription() + "");
        }
    }
}
