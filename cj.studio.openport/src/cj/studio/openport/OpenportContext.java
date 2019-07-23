package cj.studio.openport;

import org.jsoup.nodes.Document;

public class OpenportContext {

    public OpenportContext(Document canvas) {
        this.canvas = canvas;
    }

    private Document canvas;

    public Document canvas() {
        return canvas;
    }
}
