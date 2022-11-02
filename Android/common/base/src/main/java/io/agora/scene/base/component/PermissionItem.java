package io.agora.scene.base.component;

public class PermissionItem {
        public String permissionName;           ///< 权限名
        public boolean granted = false;         ///< 是否有权限
        public int requestId;                   ///< 请求Id

        PermissionItem(String name, int reqId) {
            permissionName = name;
            requestId = reqId;
        }
    }