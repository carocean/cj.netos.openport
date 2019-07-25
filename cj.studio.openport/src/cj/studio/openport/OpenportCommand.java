package cj.studio.openport;

import cj.studio.ecm.EcmException;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.IContentReciever;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;
import cj.studio.openport.util.ExceptionPrinter;
import cj.ultimate.IDisposable;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import org.jsoup.nodes.Element;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class OpenportCommand implements IDisposable, IOpenportPrinter {
    Object openportService;
    String openportPath;
    Class<?> openportInterface;//openportService实现的开放接口
    Method method;
    List<MethodParameter> parameters;
    IAccessControlStrategy acsStrategy;
    ICheckTokenStrategy ctstrategy;
    Acl acl;
    Class<?> applyReturnType;

    public OpenportCommand(Object openportService, String servicepath, Class<?> face, Method method,
                           IAccessControlStrategy acsStrategy, ICheckTokenStrategy ctstrategy) {
        this.openportPath = servicepath;
        this.openportInterface = face;
        this.method = method;
        this.acsStrategy = acsStrategy;
        this.ctstrategy = ctstrategy;
        this.openportService = openportService;
        this.acl = new Acl();
        parseApplyReturnType();
        parseMethodAcl();
        parseMethodParameters();
    }

    private void parseApplyReturnType() {
        CjOpenport openport = method.getAnnotation(CjOpenport.class);
        Class<?> applyType = openport.type();
        if (applyType == null||applyType.equals(Void.class)) {
            applyType = method.getReturnType();
        }
        this.applyReturnType=applyType;
    }

    public CjOpenports getOpenportsAnnotation() {
        return openportInterface.getAnnotation(CjOpenports.class);
    }

    public CjOpenport getOpenportAnnotation() {
        return method.getAnnotation(CjOpenport.class);
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

    public String getOpenportPath() {
        return openportPath;
    }

    public Class<?> getOpenportInterface() {
        return openportInterface;
    }

    @Override
    public void dispose() {
        this.openportInterface = null;
        this.openportPath = null;
        this.method = null;
        if (this.parameters != null)
            this.parameters.clear();
        this.acsStrategy = null;
        this.ctstrategy = null;
        if (acl != null)
            this.acl.empty();
    }

    private void parseMethodAcl() {
        CjOpenport mperm = method.getAnnotation(CjOpenport.class);
        if (mperm == null) return;
        String[] aclarr = mperm.acl();
        for (String aceText : aclarr) {
            acl.addAce(aceText);
        }
    }

    private void parseMethodParameters() {
        CjOpenport port = method.getAnnotation(CjOpenport.class);
        this.parameters = new ArrayList<>();
        Parameter[] params = method.getParameters();
        for (Parameter p : params) {
            CjOpenportParameter pp = p.getAnnotation(CjOpenportParameter.class);
            if (pp == null) {
                continue;
            }
            if (pp.in() == InRequest.content && (!port.command().toLowerCase().equals("post"))) {
                throw new EcmException(String.format("声明的方法参数要求放入请求内容，但方法却未声明为post，参数：%s 在: %s", pp.name(), method));
            }
            Class<?> runType = p.getType();
            String position = String.format("%s.%s->%s", openportInterface.getName(), method.getName(), pp.name());
            MethodParameter mp = new MethodParameter(position, p, runType);
            this.parameters.add(mp);
        }
    }

    public void doCommand(Frame frame, Circuit circuit) throws CircuitException {
        CjOpenport mperm = this.method.getAnnotation(CjOpenport.class);
        if (mperm == null) {
            throw new CircuitException("801", "拒绝访问");
        }
        String token = "";
        TokenInfo tokenInfo = null;
        IContentReciever reciever = null;
        switch (mperm.tokenIn()) {
            case headersOfRequest:
                token = frame.head(mperm.checkTokenName());
                try {
                    tokenInfo = this.ctstrategy.checkToken(token);
                    this.acsStrategy.checkRight(tokenInfo, acl);
                } catch (Throwable e) {
                    ExceptionPrinter printer = new ExceptionPrinter();
                    printer.printException(e, circuit);
                    return;
                }
                reciever = createContentReciever(frame, circuit);
                frame.content().accept(reciever);
                break;
            case parametersOfRequest:
                token = frame.parameter(mperm.checkTokenName());
                try {
                    tokenInfo = this.ctstrategy.checkToken(token);
                    this.acsStrategy.checkRight(tokenInfo, acl);
                } catch (Throwable e) {
                    ExceptionPrinter printer = new ExceptionPrinter();
                    printer.printException(e, circuit);
                    return;
                }
                reciever = createContentReciever(frame, circuit);
                frame.content().accept(reciever);
                break;
            case nope:
                reciever = createContentReciever(frame, circuit);
                frame.content().accept(reciever);
                break;
        }

    }

    protected IContentReciever createContentReciever(Frame frame, Circuit circuit) throws CircuitException {
        CjOpenport cot = method.getAnnotation(CjOpenport.class);
        Class<? extends IOpenportContentReciever> clazz = cot.reciever();
        if (clazz == null) {
            clazz = DefaultOpenportContentReciever.class;
        }
        try {
            IOpenportContentReciever target = clazz.newInstance();
            IContentReciever reciever = new OpenportContentRecieverAdapter(target, this, circuit);
            return reciever;
        } catch (InstantiationException e) {
            throw new CircuitException("404", e);
        } catch (IllegalAccessException e) {
            throw new CircuitException("404", e);
        }
    }

    @Override
    public void printPort(OpenportContext context) {
        Element e = context.canvas();
        CjOpenport ot = this.method.getAnnotation(CjOpenport.class);
        e.select(".port-title span").html(method.getName());
        e.select(".port-usage .desc .usage").html(ot.usage() + "");
        e.select(".port-usage .desc .command").html(method.getName());
        e.select(".port-usage .desc .tokenin").html(ot.tokenIn() + "");
        e.select(".port-usage .desc .tokenname").html(ot.checkTokenName() + "");
        StringBuffer sb = new StringBuffer();
        for (String ace : ot.acl()) {
            sb.append(ace + "; ");
        }
        e.select(".port-usage .desc .acl").html(sb.toString());
        StringBuffer sb2 = new StringBuffer();
        for (String st : ot.responseStatus()) {
            sb2.append(st + "; ");
        }
        e.select(".port-usage .desc .restates").html(sb2.toString());
        e.select(".headline .openportCommand").html(ot.command() + "");
        e.select(".headline .url").html(this.openportPath + "");
        e.select(".headline .protocol").html(ot.protocol() + "");

        String simpleRetFileName = ot.simpleModelFile();
        if (StringUtil.isEmpty(simpleRetFileName)) {
            e.select(".port-ret input[action=viewSimple]").remove();
        } else {
            while (simpleRetFileName.startsWith("/")) {
                simpleRetFileName = simpleRetFileName.substring(1, simpleRetFileName.length());
            }
            String cpath = context.contextPath;
            while (cpath.endsWith("/")) {
                cpath = cpath.substring(0, cpath.length() - 1);
            }
            String simpleurl = String.format("%s/%s", cpath, simpleRetFileName);
            e.select(".port-ret input[action=viewSimple]").attr("simpleModelFile", simpleurl);
        }
        ResponseClient rc = new ResponseClient();
        rc.dataElementTypes = null;
        if ("void" != method.getReturnType().getName()) {
            rc.dataText = String.format("这是%s类型的文本数据", method.getReturnType().getName());
        }
        rc.status = 200;
        rc.message = "ok";
        rc.dataType = method.getReturnType().getName();
        String json = new Gson().toJson(rc);
        e.select(".port-ret .type").html(json);
        //打印参数
        Element ul = e.select(".port-params").first();
        Element li = ul.select(">li").first().clone();
        ul.empty();
        for (MethodParameter mp : this.parameters) {
            Element cli = li.clone();
            cli.attr("paramter-name", mp.parameterAnnotation.name() + "");
            cli.attr("inrequest", mp.parameterAnnotation.in() + "");
            cli.select(">span").html(mp.parameterAnnotation.name() + "");
            cli.select(".desc").html(mp.parameterAnnotation.usage() + "");
            cli.select(".notic .in").html(mp.parameterAnnotation.in() + "");
            cli.select(".argument").attr("placeholder", "按类型" + mp.applyType + "输入...");
            if (!StringUtil.isEmpty(mp.parameterAnnotation.defaultValue())) {
                cli.select(".argument").attr("value", mp.parameterAnnotation.defaultValue());
            }
            cli.select("span.type").html(mp.applyType + "");
            simpleRetFileName = mp.parameterAnnotation.simpleModelFile();
            if (StringUtil.isEmpty(simpleRetFileName)) {
                cli.select("input[action=viewSimple]").remove();
            } else {
                while (simpleRetFileName.startsWith("/")) {
                    simpleRetFileName = simpleRetFileName.substring(1, simpleRetFileName.length());
                }
                String cpath = context.contextPath;
                while (cpath.endsWith("/")) {
                    cpath = cpath.substring(0, cpath.length() - 1);
                }
                String simpleurl = String.format("%s/%s", cpath, simpleRetFileName);

                cli.select("input[action=viewSimple]").attr("simpleModelFile", simpleurl);
            }
            ul.appendChild(cli);
        }
    }
}



