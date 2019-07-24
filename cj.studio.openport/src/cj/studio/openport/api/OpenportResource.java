package cj.studio.openport.api;

import cj.studio.ecm.IServiceProvider;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.ICircuitContent;
import cj.studio.ecm.resource.IResource;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OpenportResource implements  IOpenportResource {
    IServiceProvider site;
    public OpenportResource(IServiceProvider site) {
        this.site=site;
    }

    @Override
    public Document html(String resourcePath) throws CircuitException{
        ClassLoader cl = (ClassLoader) site.getService(IResource.class.getName());
        InputStream apiFile = cl.getResourceAsStream(resourcePath);
        DataInputStream in = new DataInputStream(apiFile);
        ByteBuf bb= Unpooled.buffer();
        byte[] buf=new byte[8192];
        int readlen=0;
        try {
            while ((readlen = in.read(buf, 0, buf.length)) > -1) {
                bb.writeBytes(buf,0,readlen);
            }
        }catch (IOException e){
            throw new CircuitException("404",e);
        }finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        byte[] bytes=new byte[bb.readableBytes()];
        bb.readBytes(bytes,0,bytes.length);
        String html = new String(bytes);
        Document canvas = Jsoup.parse(html);
        return canvas;
    }

    @Override
    public void flush(String resourcePath, ICircuitContent content) throws CircuitException {
        ClassLoader cl = (ClassLoader) site.getService(IResource.class.getName());
        InputStream apiFile = cl.getResourceAsStream(resourcePath);
        DataInputStream in = new DataInputStream(apiFile);
        byte[] buf=new byte[8192];
        int readlen=0;
        try {
            while ((readlen = in.read(buf, 0, buf.length)) > -1) {
                content.writeBytes(buf,0,readlen);
            }
        }catch (IOException e){
            throw new CircuitException("404",e);
        }finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }
}
