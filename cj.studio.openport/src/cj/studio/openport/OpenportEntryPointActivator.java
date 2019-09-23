package cj.studio.openport;

import cj.studio.ecm.*;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.context.IElement;
import cj.studio.ecm.context.IProperty;
import cj.studio.gateway.socket.pipeline.IAnnotationInputValve;
import cj.ultimate.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全服务检查器
 *
 * @author caroceanjofers
 */
public class OpenportEntryPointActivator implements IEntryPointActivator {
    IOpenportServiceContainer container;

    @Override
    public void activate(IServiceSite site, IElement args) {
        IProperty acs = (IProperty) args.getNode("accessControlStrategy");
        IProperty cts = (IProperty) args.getNode("checkTokenStrategy");
        IProperty apipath = (IProperty) args.getNode("publicAPIPath");
        String apipathStr = "";
        if (apipath != null) {
            apipathStr = apipath.getValue().getName();
        }
        if (StringUtil.isEmpty(apipathStr)) {
            apipathStr = "/cjstudio/openport/";//系统默认的开放地址
        }
        if(apipathStr.equals("/")||apipathStr.lastIndexOf(".")>0){
            throw new EcmException("publicAPIPath是开放api地址，它不能是根目录也必须是目录格式，publicAPIPath="+apipathStr);
        }
        CJSystem.logging().info("系统开放api地址是：" + apipathStr);
        String acsclassStr = "";
        if (acs != null) {
            acsclassStr = acs.getValue().getName();
        }
        if (StringUtil.isEmpty(acsclassStr)) {
            acsclassStr = DefaultAccessControlStrategy.class.getName();
        }
        String ctsclassStr = "";
        if (cts != null) {
            ctsclassStr = cts.getValue().getName();
        }
        if (StringUtil.isEmpty(ctsclassStr)) {
            ctsclassStr = EmptyTokenCheckerStrategy.class.getName();
        }

        Map<String, Object> props = new HashMap<>();
        props.put("$.cj.studio.openport.publicAPIPath", apipathStr);

        props.put("$.cj.studio.openport.accessControlStrategy", acsclassStr);
        props.put("$.cj.studio.openport.checkTokenStrategy", ctsclassStr);

        DefaultServiceSite dsite = new DefaultServiceSite(site, props);
        container = new DefaultOpenportServiceContainer(dsite);

        IAnnotationInputValve invalve = new OpenportInputValve(site);
        CjService closedservice = OpenportInputValve.class.getAnnotation(CjService.class);
        site.addService(closedservice.name(), invalve);
    }

    @Override
    public void inactivate(IServiceSite site) {
        container.dispose();

    }

    class DefaultServiceSite implements IServiceSite {
        IServiceSite site;
        Map<String, Object> props;

        public DefaultServiceSite(IServiceSite site, Map<String, Object> props) {
            this.site = site;
            this.props=props;
            if (props == null) {
                this.props = new HashMap<>();
            }
        }

        @Override
        public <T> ServiceCollection<T> getServices(Class<T> serviceClazz) {
            return site.getServices(serviceClazz);
        }

        @Override
        public Object getService(String serviceId) {
            if (props.containsKey(serviceId)) {
                return props.get(serviceId);
            }
            return site.getService(serviceId);
        }

        @Override
        public void addService(Class<?> clazz, Object service) {
            site.addService(clazz, service);
        }

        @Override
        public void removeService(Class<?> clazz) {
            site.removeService(clazz);
        }

        @Override
        public void addService(String serviceName, Object service) {
            site.addService(serviceName, service);
        }

        @Override
        public void removeService(String serviceName) {
            site.removeService(serviceName);
        }

        @Override
        public String getProperty(String key) {
            return site.getProperty(key);
        }

        @Override
        public String[] enumProperty() {
            return site.enumProperty();
        }
    }
}
