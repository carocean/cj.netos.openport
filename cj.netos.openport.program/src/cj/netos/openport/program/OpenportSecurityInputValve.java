package cj.netos.openport.program;

import cj.studio.ecm.Scope;
import cj.studio.ecm.annotation.CjService;
import cj.studio.openport.SecurityInputValve;
@CjService(name="openportSecurityInputValve",scope = Scope.multiton)
public class OpenportSecurityInputValve extends SecurityInputValve{

}
