package cj.netos.openport.program.portface;

import cj.netos.microapp.args.MicroPortal;
import cj.netos.microapp.args.Microapp;
import cj.netos.microapp.args.MicroappVersion;
import cj.netos.microapp.args.UpdateCommand;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.InRequest;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

import java.util.List;

@CjOpenports(usage = "查询管理器，供移动端维持微应用的安装")
public interface IGberaSearcherFrontendPorts extends IOpenportService {
    @CjOpenport(usage = "检查微应用的版本，并返回更新指令，版本为最新的微应用则忽略。返回的是更新命令集合", command = "post", elementType = UpdateCommand.class)
    List<UpdateCommand> checkMicroAppVersions(
            @CjOpenportParameter(name = "versions", in = InRequest.content, usage = "移动端缓冲的版本集合", elementType = MicroappVersion.class) List<MicroappVersion> versions)
            throws CircuitException;

    @CjOpenport(usage = "获取一个微应用",simpleModelFile = "/models/microapp.json")
    Microapp getMicroAppInfo(
            @CjOpenportParameter(name = "microapp", usage = "微应用名。如果没有指定版本默认取应用配置版本，如果指定版本号则取应用指定版。格式为：myappname/1.1") String microappname)
            throws CircuitException;

    @CjOpenport(usage = "列出一个应用的所有版本")
    List<String> listMicroAppVersion(
            @CjOpenportParameter(name = "appnameWithoutVersion", usage = "微应用名,不带版本号", defaultValue = "gbera") String appnameWithoutVersion) throws CircuitException;

    @CjOpenport(usage = "获取一个微框架",simpleModelFile = "/models/microportal.json")
    MicroPortal getMicroPortalInfo(
            @CjOpenportParameter(name = "microportal", defaultValue = "gbera/1.0", usage = "微框架名。格式为：myportal/1.1@mystyle 如果没有指定版本则报错,风格可以不写，不写的话则取框架默认风格") String microportal)
            throws CircuitException;
}
