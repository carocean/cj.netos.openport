package cj.netos.openport.program;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.Frame;
import cj.studio.openport.DefaultOpenportContentReciever;
import cj.studio.openport.IOpenportMethod;
import cj.studio.openport.ISecuritySession;

public class MyOpenportContentReciever extends DefaultOpenportContentReciever {

    @Override
    public void oninvoke(IOpenportMethod openportMethod, ISecuritySession iSecuritySession, Frame frame, Circuit circuit) {
        super.oninvoke(openportMethod, iSecuritySession, frame, circuit);
        Class<? extends cj.studio.openport.IOpenportContentReciever> clazz=openportMethod.getOpenportAnnotation().reciever();

        if(clazz!=null&&clazz.equals(this.getClass())) {
            String conter=frame.head("OpenportsTester-Counter");
            if(conter!=null) {
                CJSystem.logging().info("当前请求次数：" + frame.head("OpenportsTester-Counter"));
            }
        }
    }
}
