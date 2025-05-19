/*
 * Copyright © Sud.Tech
 * https://sud.tech
 */

package tech.sud.mgp.SudMGPWrapper.utils;

import tech.sud.mgp.core.ISudFSMStateHandle;
import tech.sud.mgp.SudMGPWrapper.state.MGStateResponse;

public class ISudFSMStateHandleUtils {

    /**
     * 回调游戏，成功
     *
     * @param handle
     */
    public static void handleSuccess(ISudFSMStateHandle handle) {
        MGStateResponse response = new MGStateResponse();
        response.ret_code = MGStateResponse.SUCCESS;
        response.ret_msg = "success";
        handle.success(SudJsonUtils.toJson(response));
    }

}
