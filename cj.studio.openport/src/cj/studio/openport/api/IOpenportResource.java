package cj.studio.openport.api;

import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.ICircuitContent;
import org.jsoup.nodes.Document;

public interface IOpenportResource {
    Document html(String resourcePath) throws CircuitException;

    void flush(String resourcePath, ICircuitContent content)throws CircuitException;
}
