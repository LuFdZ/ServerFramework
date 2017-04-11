package org.server.backend.remote;

import org.server.core.remote.RMIServiceInterface;
import org.server.core.remote.ShutdownServiceInterface;

/**
 * 后台RMI服务接口
 */
public interface BackendMasterInterface extends RMIServiceInterface, ShutdownServiceInterface {

}
