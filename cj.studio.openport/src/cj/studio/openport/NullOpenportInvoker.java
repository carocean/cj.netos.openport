package cj.studio.openport;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.Frame;
import cj.studio.openport.annotations.CjOpenport;

/**
 * 空类，表示在Openport注解时为空.因此并不被执行
 */
public class NullOpenportInvoker implements IOpenportBeforeInvoker,IOpenportAfterInvoker{
    @Override
    public void doAfter(String methodName, CjOpenport openportAnnotation, Frame frame, Circuit circuit) {

    }

    @Override
    public void doBefore(String methodName, CjOpenport openport, TokenInfo tokenInfo, Frame frame, Circuit circuit) {

    }
}
