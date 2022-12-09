package io.agora.scene.ktv.create;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.Random;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.ActivityRoomCreateBinding;
import io.agora.scene.ktv.live.RoomLivingActivity;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;

/**
 * 创建房间
 */
@Route(path = PagePathConstant.pageRoomCreate)
public class RoomCreateActivity extends BaseViewBindingActivity<ActivityRoomCreateBinding> {
    /**
     * 当前选中的是第几个输入框
     */
    private int currentPosition = 0;

    private String bgOption;

    private RoomCreateViewModel roomCreateViewModel;

    @Override
    protected ActivityRoomCreateBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return ActivityRoomCreateBinding.inflate(inflater);
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        roomCreateViewModel = new ViewModelProvider(this).get(RoomCreateViewModel.class);
        setRandomRoomTitleAndCover();
    }

    private int positionCover = 1;

    private void setCover() {
        if (positionCover > 9) {
            positionCover = 1;
        }
        bgOption = String.valueOf(positionCover++);
        GlideApp.with(getBinding().ivRoomCover.getContext()).load(getCoverRes(bgOption))
                .transform(new CenterCropRoundCornerTransform(40))
                .into(getBinding().ivRoomCover);

    }

    public int getCoverRes(String cover) {
        if ("1".equals(cover)) {
            return io.agora.scene.base.R.mipmap.icon_room_cover1;
        } else if ("2".equals(cover)) {
            return io.agora.scene.base.R.mipmap.icon_room_cover2;
        } else if ("3".equals(cover)) {
            return io.agora.scene.base.R.mipmap.icon_room_cover3;
        } else if ("4".equals(cover)) {
            return io.agora.scene.base.R.mipmap.icon_room_cover4;
        } else if ("5".equals(cover)) {
            return io.agora.scene.base.R.mipmap.icon_room_cover5;
        } else if ("6".equals(cover)) {
            return io.agora.scene.base.R.mipmap.icon_room_cover6;
        } else if ("7".equals(cover)) {
            return io.agora.scene.base.R.mipmap.icon_room_cover7;
        } else if ("8".equals(cover)) {
            return io.agora.scene.base.R.mipmap.icon_room_cover8;
        } else if ("9".equals(cover)) {
            return io.agora.scene.base.R.mipmap.icon_room_cover9;
        }
        return io.agora.scene.base.R.mipmap.icon_room_cover1;
    }

    @Override
    public void initListener() {
        getBinding().ivRoomCover.setOnClickListener(view -> {
            setCover();
        });
        getBinding().superLayout.setOnClickListener(view -> {
            hideInput();
        });
        roomCreateViewModel.joinRoomResult.observe(this, out -> {
            if(out != null){
                RoomLivingActivity.launch(RoomCreateActivity.this, out);
                finish();
                hideLoadingView();
            }else{
                hideLoadingView();
            }
        });
        roomCreateViewModel.createRoomResult.observe(this, out -> {
            if(out != null){
                roomCreateViewModel.joinRoom(out.getRoomNo(), out.getPassword());
            }else{
                hideLoadingView();
            }
        });
        getBinding().cbOpen.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                getBinding().cbUnOpen.setChecked(false);
                getBinding().etCode1.setVisibility(View.GONE);
                getBinding().etCode2.setVisibility(View.GONE);
                getBinding().etCode3.setVisibility(View.GONE);
                getBinding().etCode4.setVisibility(View.GONE);
                getBinding().tvSet.setVisibility(View.GONE);
            }
        });
        getBinding().cbUnOpen.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                getBinding().cbOpen.setChecked(false);
                getBinding().etCode1.setVisibility(View.VISIBLE);
                getBinding().etCode2.setVisibility(View.VISIBLE);
                getBinding().etCode3.setVisibility(View.VISIBLE);
                getBinding().etCode4.setVisibility(View.VISIBLE);
                getBinding().tvSet.setVisibility(View.VISIBLE);
            }
        });
        getBinding().etCode1.setOnFocusChangeListener(editFocusListener);
        getBinding().etCode2.setOnFocusChangeListener(editFocusListener);
        getBinding().etCode3.setOnFocusChangeListener(editFocusListener);
        getBinding().etCode4.setOnFocusChangeListener(editFocusListener);

        getBinding().etCode1.setOnKeyListener(onKeyListener);
        getBinding().etCode2.setOnKeyListener(onKeyListener);
        getBinding().etCode3.setOnKeyListener(onKeyListener);
        getBinding().etCode4.setOnKeyListener(onKeyListener);

        getBinding().etCode1.addTextChangedListener(textWatcher);
        getBinding().etCode2.addTextChangedListener(textWatcher);
        getBinding().etCode3.addTextChangedListener(textWatcher);
        getBinding().etCode4.addTextChangedListener(textWatcher);

        getBinding().btnCreate.setOnClickListener(view -> {
            if (getBinding().cbUnOpen.isChecked() && !isAllInput()) {
                ToastUtils.showToast(getString(R.string.please_input_4_pwd));
            } else {
                String code =
                        getBinding().etCode1.getText().toString()
                                + getBinding().etCode2.getText()
                                + getBinding().etCode3.getText()
                                + getBinding().etCode4.getText();
                if (TextUtils.isEmpty(getBinding().etRoomName.getText())) {
                    ToastUtils.showToast(getString(R.string.please_input_room_name));
                } else if (code.length() > 4) {
                    ToastUtils.showToast(getString(R.string.please_input_4_pwd));
                } else {
                    createRoom(code);
                }
            }
        });
        getBinding().btnRandom.setOnClickListener(view -> {
            setRandomRoomTitleAndCover();
        });
    }

    private void setRandomRoomTitleAndCover() {
        getBinding().etRoomName.setText(getResources().getStringArray(R.array.ktv_roomName)[new Random().nextInt(21)]);
        setCover();
    }

    /**
     * 创建房间流程
     *
     * @param password 密码
     */
    private void createRoom(String password) {
        showLoadingView();
        String roomName = getBinding().etRoomName.getText().toString();
        if (TextUtils.isEmpty(roomName)) {
            ToastUtils.showToast(R.string.please_input_room_name);
            return;
        }
        String userNo = UserManager.getInstance().getUser().userNo;
        int isPrivate;
        if (TextUtils.isEmpty(password)) {
            isPrivate = 0;
        } else {
            isPrivate = 1;
        }
        roomCreateViewModel.createRoom(isPrivate, roomName, password, userNo, bgOption);
    }

    /**
     * 记录当前焦点位置
     */
    @SuppressLint("NonConstantResourceId")
    private final View.OnFocusChangeListener editFocusListener = (view, hasFocus) -> {
        if (hasFocus) {
            int id = view.getId();
            if (id == R.id.etCode1) {
                currentPosition = 0;
            } else if (id == R.id.etCode2) {
                currentPosition = 1;
            } else if (id == R.id.etCode3) {
                currentPosition = 2;
            } else if (id == R.id.etCode4) {
                currentPosition = 3;
            }
        }
    };
    /**
     * 删除前一个输入内
     */
    private final View.OnKeyListener onKeyListener = (v, keyCode, event) -> {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (v instanceof AppCompatEditText) {
                    if (((AppCompatEditText) v).getText().length() == 0) {
                        findNextFocus(false);
                    }
                    ((AppCompatEditText) v).setText("");
                }

                return true;
            }
        }
        return false;
    };

    /**
     * 寻找下一个需要获取焦点的控件
     *
     * @param isNext true 下一个  false 上一个
     */
    private void findNextFocus(boolean isNext) {
        if (isNext) {
            switch (currentPosition) {
                case 0:
                    getBinding().etCode2.setEnabled(true);
                    getBinding().etCode2.requestFocus();
                    break;
                case 1:
                    getBinding().etCode3.setEnabled(true);
                    getBinding().etCode3.requestFocus();
                    break;
                case 2:
                    getBinding().etCode4.setEnabled(true);
                    getBinding().etCode4.requestFocus();
                    break;
                case 3: {
                    //全部填完
                    hideInput();
                    break;
                }
            }
        } else {
            switch (currentPosition) {
                case 1:
                    getBinding().etCode1.requestFocus();
                    break;
                case 2:
                    getBinding().etCode2.requestFocus();
                    break;
                case 3:
                    getBinding().etCode3.requestFocus();
                    break;
            }
        }
    }

    /**
     * 输入历史记录
     */
    private String oldInput = "";

    /**
     * 监听每一个输入框输状态
     */
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            oldInput = charSequence.toString();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() > 0) {
                if (editable.length() > 1) {
                    //检查是否要替换当前输入内容
                    String newInput;
                    if (String.valueOf(editable.charAt(0)).equals(oldInput)) {
                        newInput = String.valueOf(editable.charAt(1));
                    } else {
                        newInput = String.valueOf(editable.charAt(0));
                    }
                    switch (currentPosition) {
                        case 0:
                            getBinding().etCode1.setText(newInput);
                            break;
                        case 1:
                            getBinding().etCode2.setText(newInput);
                            break;
                        case 2:
                            getBinding().etCode3.setText(newInput);
                            break;
                        case 3:
                            getBinding().etCode4.setText(newInput);
                            break;
                    }
                } else {
                    //寻焦
                    findNextFocus(true);
                }
            }
        }
    };

    /**
     * 检查是否已输入完毕
     */
    private boolean isAllInput() {
        if (TextUtils.isEmpty(getBinding().etCode1.getText())) {
            return false;
        }
        if (TextUtils.isEmpty(getBinding().etCode2.getText())) {
            return false;
        }
        if (TextUtils.isEmpty(getBinding().etCode3.getText())) {
            return false;
        }
        return !TextUtils.isEmpty(getBinding().etCode4.getText());
    }
}
