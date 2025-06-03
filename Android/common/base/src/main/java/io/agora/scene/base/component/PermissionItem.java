package io.agora.scene.base.component;

public class PermissionItem {
    public String permissionName;           ///< Permission name
    public boolean granted = false;         ///< Whether permission is granted
    public int requestId;                   ///< Request ID

        PermissionItem(String name, int reqId) {
            permissionName = name;
            requestId = reqId;
        }
    }