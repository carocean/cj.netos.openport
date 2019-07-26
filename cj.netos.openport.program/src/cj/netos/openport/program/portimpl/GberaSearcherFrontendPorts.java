package cj.netos.openport.program.portimpl;

import cj.netos.microapp.args.MicroPortal;
import cj.netos.microapp.args.Microapp;
import cj.netos.microapp.args.MicroappVersion;
import cj.netos.microapp.args.UpdateCommand;
import cj.netos.microapp.ports.IGberaSearcherPorts;
import cj.netos.openport.program.portface.IGberaSearcherFrontendPorts;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;

import java.util.List;
@CjService(name = "/gberaSearcher.ports")
public class GberaSearcherFrontendPorts implements IGberaSearcherFrontendPorts {
    @CjServiceRef(refByName = "$openports.cj.netos.microapp.ports.IGberaSearcherPorts")
    IGberaSearcherPorts gberaSearcherPorts;

    @Override
    public List<UpdateCommand> checkMicroAppVersions(List<MicroappVersion> versions) throws CircuitException {
        return gberaSearcherPorts.checkMicroAppVersions(versions);
    }

    @Override
    public Microapp getMicroAppInfo(String microappname) throws CircuitException {
        return gberaSearcherPorts.getMicroAppInfo(microappname);
    }

    @Override
    public List<String> listMicroAppVersion(String appnameWithoutVersion) throws CircuitException {
        return gberaSearcherPorts.listMicroAppVersion(appnameWithoutVersion);
    }

    @Override
    public MicroPortal getMicroPortalInfo(String microportal) throws CircuitException {
        return gberaSearcherPorts.getMicroPortalInfo(microportal);
    }
}
