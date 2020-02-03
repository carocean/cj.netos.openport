package cj.studio.openport;

import cj.studio.ecm.EcmException;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.IContentReciever;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportAppSecurity;
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
    IOpenportBeforeInvoker beforeInvoker;
    IOpenportAfterInvoker afterInvoker;
    Object openportService;
    String openportPath;
    Class<?> openportInterface;//openportService实现的开放接口
    Method method;
    List<MethodParameter> parameters;
    ICheckAppSignStrategy checkAppSignStrategy;
    ICheckAccessTokenStrategy checkAccessTokenStrategy;
    Class<?> applyReturnType;

    public OpenportCommand(Object openportService, String servicepath, Class<?> face, Method method,
                           ICheckAppSignStrategy checkAppSignStrategy, ICheckAccessTokenStrategy checkAccessTokenStrategy) {
        this.openportPath = servicepath;
        this.openportInterface = face;
        this.method = method;
        this.checkAppSignStrategy = checkAppSignStrategy;
        this.checkAccessTokenStrategy = checkAccessTokenStrategy;
        this.openportService = openportService;
        try {
            instanceInvokers();
        } catch (IllegalAccessException e) {
            throw new EcmException(e);
        } catch (InstantiationException e) {
            throw new EcmException(e);
        }
        parseApplyReturnType();
        parseMethodParameters();
    }

    private void instanceInvokers() throws IllegalAccessException, InstantiationException {
        CjOpenport openport = method.getAnnotation(CjOpenport.class);
        if (openport.beforeInvoker() != NullOpenportInvoker.class) {
            this.beforeInvoker = openport.beforeInvoker().newInstance();
        }
        if (openport.afterInvoker() != NullOpenportInvoker.class) {
            this.afterInvoker = openport.afterInvoker().newInstance();
        }
    }

    private void parseApplyReturnType() {
        CjOpenport openport = method.getAnnotation(CjOpenport.class);
        Class<?> applyType = openport.type();
        if (applyType == null || applyType.equals(Void.class)) {
            applyType = method.getReturnType();
        }
        this.applyReturnType = applyType;
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
        this.checkAppSignStrategy = null;
        this.checkAccessTokenStrategy = null;
    }


    private void parseMethodParameters() {
        CjOpenport port = method.getAnnotation(CjOpenport.class);
        this.parameters = new ArrayList<>();
        Parameter[] params = method.getParameters();
        for (Parameter p : params) {
            CjOpenportParameter pp = p.getAnnotation(CjOpenportParameter.class);
            if (ISecuritySession.class.isAssignableFrom(p.getType())) {
                if (pp != null) {
                    throw new EcmException(String.format("声明的方法参数:%s.%s 是%s类型，不需要CjOpenportParameter注解！", method.getName(), pp.name(), DefaultSecuritySession.class.getName()));
                }
                Class<?> runType = p.getType();
                String position = String.format("%s.%s->%s", openportInterface.getName(), method.getName(), p.getName());
                MethodParameter mp = new MethodParameter(position, p, runType);
                this.parameters.add(mp);
                continue;
            }
            if (pp == null) {
                continue;
            }
            if (pp.in() == PKeyInRequest.content && (!port.command().toLowerCase().equals("post"))) {
                throw new EcmException(String.format("声明的方法参数要求放入请求内容，但方法却未声明为post，参数：%s 在: %s", pp.name(), method));
            }
            Class<?> runType = p.getType();
            String position = String.format("%s.%s->%s", openportInterface.getName(), method.getName(), pp.name());
            MethodParameter mp = new MethodParameter(position, p, runType);
            this.parameters.add(mp);
        }
    }

    public void doCommand(Frame frame, Circuit circuit) throws CircuitException {
        CjOpenportAppSecurity cjOpenportAppSecurity = this.method.getAnnotation(CjOpenportAppSecurity.class);
        if (cjOpenportAppSecurity == null) {
            doAccessTokenCommand(frame, circuit);
        } else {
            doAppSignCommand(frame, circuit, cjOpenportAppSecurity);
        }
    }

    public void doAppSignCommand(Frame frame, Circuit circuit, CjOpenportAppSecurity cjOpenportAppSecurity) throws CircuitException {
        ISecuritySession iSecuritySession = null;
        IContentReciever reciever = null;
        String portsurl = frame.relativePath();
        String methodName = method.getName();
        String appId = null;
        switch (cjOpenportAppSecurity.appIDIn()) {
            case header:
                appId = frame.head(cjOpenportAppSecurity.appIDName());
                break;
            case parameter:
                appId = frame.parameter(cjOpenportAppSecurity.appIDName());
                break;
        }
        if (StringUtil.isEmpty(appId)) {
            throw new CircuitException("804", "拒绝访问,缺少appid");
        }
        String appKey = null;
        switch (cjOpenportAppSecurity.appKeyIn()) {
            case header:
                appKey = frame.head(cjOpenportAppSecurity.appKeyName());
                break;
            case parameter:
                appKey = frame.parameter(cjOpenportAppSecurity.appKeyName());
                break;
        }
        if (StringUtil.isEmpty(appKey)) {
            throw new CircuitException("804", "拒绝访问,缺少appKey");
        }
        String nonce = null;
        switch (cjOpenportAppSecurity.nonceIn()) {
            case header:
                nonce = frame.head(cjOpenportAppSecurity.nonceName());
                break;
            case parameter:
                nonce = frame.parameter(cjOpenportAppSecurity.nonceName());
                break;
        }
        if (StringUtil.isEmpty(nonce)) {
            throw new CircuitException("804", "拒绝访问,缺少nonce");
        }
        String sign = null;
        switch (cjOpenportAppSecurity.signIn()) {
            case header:
                sign = frame.head(cjOpenportAppSecurity.signName());
                break;
            case parameter:
                sign = frame.parameter(cjOpenportAppSecurity.signName());
                break;
        }
        if (StringUtil.isEmpty(sign)) {
            throw new CircuitException("804", "拒绝访问,缺少nonce");
        }
        try {
            this.checkAppSignStrategy.checkAppSign(portsurl, methodName, appId, appKey, nonce, sign);
            if (this.beforeInvoker != null) {
                this.beforeInvoker.doBefore(true, iSecuritySession, frame, circuit);
            }
        } catch (Throwable e) {
            ExceptionPrinter printer = new ExceptionPrinter();
            printer.printException(e, circuit);
            return;
        }
        reciever = createContentReciever(iSecuritySession, frame, circuit);
        frame.content().accept(reciever);
    }

    public void doAccessTokenCommand(Frame frame, Circuit circuit) throws CircuitException {
        CjOpenport cjOpenport = this.method.getAnnotation(CjOpenport.class);
        if (cjOpenport == null) {
            throw new CircuitException("801", "拒绝访问");
        }
        String token = "";
        ISecuritySession iSecuritySession = null;
        IContentReciever reciever = null;
        String portsurl = frame.relativePath();
        String methodName = method.getName();
        switch (cjOpenport.tokenIn()) {
            case headersOfRequest:
                token = frame.head(cjOpenport.checkTokenName());
                break;
            case parametersOfRequest:
                token = frame.parameter(cjOpenport.checkTokenName());
                break;
            case nope:
                break;
        }
        try {
            if (cjOpenport.tokenIn() != AccessTokenIn.nope) {
                iSecuritySession = this.checkAccessTokenStrategy.checkAccessToken(portsurl, methodName, token);
            }
            if (this.beforeInvoker != null) {
                this.beforeInvoker.doBefore(cjOpenport.tokenIn() == AccessTokenIn.nope, iSecuritySession, frame, circuit);
            }
        } catch (Throwable e) {
            ExceptionPrinter printer = new ExceptionPrinter();
            printer.printException(e, circuit);
            return;
        }
        reciever = createContentReciever(iSecuritySession, frame, circuit);
        frame.content().accept(reciever);
    }

    protected IContentReciever createContentReciever(ISecuritySession iSecuritySession, Frame frame, Circuit circuit) throws CircuitException {
        CjOpenport cot = method.getAnnotation(CjOpenport.class);
        Class<? extends IOpenportContentReciever> clazz = cot.reciever();
        if (clazz == null) {
            clazz = DefaultOpenportContentReciever.class;
        }
        try {
            IOpenportContentReciever target = clazz.newInstance();
            IContentReciever reciever = new OpenportContentRecieverAdapter(target, iSecuritySession, this, circuit);
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
        if (ot.tokenIn() == AccessTokenIn.nope) {
            String tokenInfo = String.format("%s(<b style='color:red;'>注：<b/>nope表示该方法不需要访问令牌)", ot.tokenIn());
            e.select(".port-usage .desc .tokenin").html(tokenInfo);
            e.select(".port-usage .desc .tokenname").html(ot.checkTokenName() + "");
            e.select(".request-token").attr("style", "display:none;");
            e.select(".port-usage .desc .tokenname").html("无");
        } else {
            e.select(".port-usage .desc .tokenin").html(ot.tokenIn() + "");
            e.select(".port-usage .desc .tokenname").html(ot.checkTokenName() + "");
        }
        StringBuffer sb2 = new StringBuffer();
        for (String st : ot.responseStatus()) {
            sb2.append(st + "; ");
        }
        e.select(".port-usage .desc .restates").html(sb2.toString());
        e.select(".headline .cmd").html(ot.command() + "");
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
        //打印应用签名验证参数
        Element signul = e.select(".port-appsign-port-params").first();
        Element signli = signul.select(">li").first().clone();
        signul.empty();
        CjOpenportAppSecurity cjOpenportAppSecurity = this.method.getAnnotation(CjOpenportAppSecurity.class);
        if (cjOpenportAppSecurity != null) {
            printAppSignParams(signul, signli, cjOpenportAppSecurity);
            e.select(".port-appsign > .port-appsign-usage span").html(cjOpenportAppSecurity.usage()+"");
        } else {
            e.select(".port-appsign").remove();
        }
        //打印参数
        Element ul = e.select(".port-params").first();
        Element li = ul.select(">li").first().clone();
        ul.empty();
        for (MethodParameter mp : this.parameters) {
            if (mp.parameterAnnotation == null) {
                continue;
            }
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

    private void printAppSignParams(Element signul, Element signli, CjOpenportAppSecurity cjOpenportAppSecurity) {
        Element cli = signli.clone();
        cli.attr("paramter-name", cjOpenportAppSecurity.appIDName() + "");
        cli.attr("inrequest", cjOpenportAppSecurity.appIDIn() + "");
        cli.select(">span").html(cjOpenportAppSecurity.appIDName() + "");
        cli.select(".port-appsign-desc").html("平台颁发给您的应用标识");
        cli.select(".port-appsign-notic .in").html(cjOpenportAppSecurity.appIDIn() + "");
        cli.select(".port-appsign-argument").attr("placeholder", "按类型" + String.class.getName() + "输入...");
        cli.select("span.port-appsign-type").html(String.class.getName() + "");
        signul.appendChild(cli);
        cli = signli.clone();
        cli.attr("paramter-name", cjOpenportAppSecurity.appKeyName() + "");
        cli.attr("inrequest", cjOpenportAppSecurity.appKeyIn() + "");
        cli.select(">span").html(cjOpenportAppSecurity.appKeyName() + "");
        cli.select(".port-appsign-desc").html("平台颁发给您的应用标识");
        cli.select(".port-appsign-notic .in").html(cjOpenportAppSecurity.appKeyIn() + "");
        cli.select(".port-appsign-argument").attr("placeholder", "按类型" + String.class.getName() + "输入...");
        cli.select("span.port-appsign-type").html(String.class.getName() + "");
        signul.appendChild(cli);
        cli = signli.clone();
        cli.attr("paramter-name", cjOpenportAppSecurity.nonceName() + "");
        cli.attr("inrequest", cjOpenportAppSecurity.nonceIn() + "");
        cli.select(">span").html(cjOpenportAppSecurity.nonceName() + "");
        cli.select(".port-appsign-desc").html("平台颁发给您的应用标识");
        cli.select(".port-appsign-notic .in").html(cjOpenportAppSecurity.nonceIn() + "");
        cli.select(".port-appsign-argument").attr("placeholder", "按类型" + String.class.getName() + "输入...");
        cli.select("span.port-appsign-type").html(String.class.getName() + "");
        signul.appendChild(cli);
        cli = signli.clone();
        cli.attr("paramter-name", cjOpenportAppSecurity.signName() + "");
        cli.attr("inrequest", cjOpenportAppSecurity.signIn() + "");
        cli.select(">span").html(cjOpenportAppSecurity.signName() + "");
        cli.select(".port-appsign-desc").html("平台颁发给您的应用标识");
        cli.select(".port-appsign-notic .in").html(cjOpenportAppSecurity.signIn() + "");
        cli.select(".port-appsign-argument").attr("placeholder", "按类型" + String.class.getName() + "输入...");
        cli.select("span.port-appsign-type").html(String.class.getName() + "");
        signul.appendChild(cli);
    }
}



