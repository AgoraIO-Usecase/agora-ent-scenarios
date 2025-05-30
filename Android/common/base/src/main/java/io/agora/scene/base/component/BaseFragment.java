package io.agora.scene.base.component;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {
    private boolean isInit = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isInit) {
            initView();
            initListener();
            requestData();
            isInit = true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isInit = false;
    }

    public void initView() {
    }

    public void initListener() {
    }

    public void requestData() {
    }

    protected void setOnApplyWindowInsetsListener(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPaddingRelative(inset.left, inset.top, inset.right, inset.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    protected void hideKeyboard(EditText editText) {
        editText.clearFocus();
        Activity context = getActivity();
        if (context == null) return;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    protected void showKeyboard(EditText editText) {
        Activity context = getActivity();
        if (context == null) return;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }

}
