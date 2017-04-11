package org.server.frontend.remote;

import org.server.core.remote.RMIServiceInterface;
import org.server.core.remote.ShutdownServiceInterface;

/**
 * 后台服务与管理服务交互接口
 */
public interface FrontendMasterInterface extends RMIServiceInterface, ShutdownServiceInterface {

}
