package cj.studio.openport;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class OpenportContext {

    public OpenportContext(Element canvas,String contextPath) {
        this.canvas = canvas;
        this.contextPath=contextPath;
    }
    String contextPath;
    private Element canvas;

    public Element canvas() {
        return canvas;
    }

    public String contextPath() {
        return contextPath;
    }
}
