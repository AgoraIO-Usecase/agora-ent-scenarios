package io.agora.scene.ktv.widget;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import io.agora.scene.base.component.BaseBottomSheetDialogFragment;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.ktv.databinding.KtvDialogMoreBinding;
import io.agora.scene.ktv.live.RoomLivingActivity;
import io.agora.scene.ktv.live.fragment.dialog.ProfileFragment;

public class MoreDialog extends BaseBottomSheetDialogFragment<KtvDialogMoreBinding> {
    public static final String TAG = "MoreDialog";

    private final MusicSettingBean mSetting;
    private final Boolean isRoomOwner;

    public MoreDialog(MusicSettingBean mSetting, boolean isRoomOwner) {
        this.mSetting = mSetting;
        this.isRoomOwner = isRoomOwner;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(requireDialog().getWindow().getDecorView(), (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            mBinding.getRoot().setPadding(inset.left, 0, inset.right, inset.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        initView();
    }

    private void initView() {
        mBinding.iBtnProfile.setOnClickListener(this::showProfilePage);
    }

    private void showProfilePage(View v) {
        mBinding.getRoot().removeAllViews();
        BaseViewBindingFragment<?> voiceFragment = new ProfileFragment(mSetting, isRoomOwner);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.add(mBinding.getRoot().getId(), voiceFragment, ProfileFragment.TAG);
        ft.commit();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        ((RoomLivingActivity) requireActivity()).setDarkStatusIcon(false);
    }
}
