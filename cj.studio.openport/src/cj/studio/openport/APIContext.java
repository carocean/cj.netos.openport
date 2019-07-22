package cj.studio.openport;

import org.jsoup.nodes.Document;

public class APIContext {

    public APIContext(Document canvas) {
        this.canvas = canvas;
    }

    private Document canvas;

    public Document canvas() {
        return canvas;
    }
}
