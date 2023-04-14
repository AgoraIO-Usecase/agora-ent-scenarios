package io.agora.scene.widget.dialog;

import android.Manifest;
import android.content.Context;

import androidx.annotation.NonNull;

import io.agora.scene.base.component.OnButtonClickListener;
import io.agora.scene.widget.R;

public class PermissionLeakDialog extends CommonDialog {

    public PermissionLeakDialog(@NonNull Context context) {
        super(context);
    }

    public void show(String permission, Runnable onCancelClicked, Runnable onSettingClieded) {
        String title;
        String content;
        if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
            title = getContext().getString(R.string.comm_permission_leak_auido_title);
            content = getContext().getString(R.string.comm_permission_leak_auido_content);
        } else if (permission.equals(Manifest.permission.CAMERA)) {
            title = getContext().getString(R.string.comm_permission_leak_camera_title);
            content = getContext().getString(R.string.comm_permission_leak_camera_content);
        } else if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            title = getContext().getString(R.string.comm_permission_leak_sdcard_title);
            content = getContext().getString(R.string.comm_permission_leak_sdcard_content);
        } else {
            title = getContext().getString(R.string.comm_permission_leak_other_title);
            content = getContext().getString(R.string.comm_permission_leak_other_content);
        }
        setDialogTitle(title);
        setDescText(content);
        setDialogBtnText(getContext().getString(R.string.cancel), getContext().getString(R.string.comm_setting));
        setOnButtonClickListener(new OnButtonClickListener() {
            @Override
            public void onLeftButtonClick() {
                if (onCancelClicked != null) {
                    onCancelClicked.run();
                }
            }

            @Override
            public void onRightButtonClick() {
                if (onSettingClieded != null) {
                    onSettingClieded.run();
                }
            }
        });
        setCancelable(false);
        show();
    }


}
