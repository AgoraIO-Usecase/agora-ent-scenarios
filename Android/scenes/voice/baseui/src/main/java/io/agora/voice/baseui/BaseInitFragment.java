package io.agora.voice.baseui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import io.agora.voice.baseui.general.callback.OnResourceParseCallback;
import io.agora.voice.baseui.general.enums.Status;
import io.agora.voice.baseui.general.net.Resource;

import io.agora.voice.baseui.interfaces.IParserSource;


public abstract class BaseInitFragment extends Fragment implements IParserSource {
    private AlertDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = getLayoutId();
        View view = null;
        if(layoutId != 0) {
            view = inflater.inflate(layoutId, container, false);
        }else {
            view = getContentView(inflater,container,savedInstanceState);
        }
        initArgument();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(savedInstanceState);
        initViewModel();
        initListener();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    /**
     * Return the layout ID
     * @return
     */
    protected int getLayoutId(){
        return 0;
    };

    /**
     * Return the layout view
     * @return
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    protected View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    /**
     * Initialize the params
     */
    protected void initArgument() {}

    /**
     * Initialize the views
     * @param savedInstanceState
     */
    protected void initView(Bundle savedInstanceState) {
    }


    /**
     * Initialize the viewmodels
     */
    protected void initViewModel() {}

    /**
     * Initialize the listeners
     */
    protected void initListener() {}

    /**
     * Initialize the data
     */
    protected void initData() {}

    /**
     * hide keyboard
     */
    protected void hideKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(inputManager == null) {
                    return;
                }
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public void showLoading(boolean cancelable){
        if (loadingDialog == null && getActivity() != null) {
            loadingDialog = new AlertDialog.Builder(getActivity()).setView(R.layout.voice_view_base_loading).create();
            if (loadingDialog.getWindow() != null){
                loadingDialog.getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
                DisplayMetrics dm = new DisplayMetrics();
                if (getActivity() != null) {
                    WindowManager windowManager = getActivity().getWindowManager();
                    if (windowManager != null) {
                        windowManager.getDefaultDisplay().getMetrics(dm);
                        WindowManager.LayoutParams params = loadingDialog.getWindow().getAttributes();
                        params.gravity = Gravity.CENTER;
                        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        loadingDialog.getWindow().setAttributes(params);
                    }
                }
            }
            loadingDialog.setCancelable(cancelable);
            loadingDialog.show();
        }
    }

    public void dismissLoading(){
        if (loadingDialog != null)
            loadingDialog.dismiss();
    }

    /**
     * Call it after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param id
     * @param <T>
     * @return
     */
    protected <T extends View> T findViewById(@IdRes int id) {
        return getView().findViewById(id);
    }

    /**
     //     * Parse Resource<T>
     //     * @param response
     //     * @param callback
     //     * @param <T>
     //     */
    public <T> void parseResource(Resource<T> response, @NonNull OnResourceParseCallback<T> callback) {
        if(response == null) {
            return;
        }
        if(response.status == Status.SUCCESS) {
            callback.onHideLoading();
            callback.onSuccess(response.data);
        }else if(response.status == Status.ERROR) {
            callback.onHideLoading();
            if(!callback.hideErrorMsg) {
                Log.e("parseResource ",response.getMessage());
            }
            callback.onError(response.errorCode, response.getMessage());
        }else if(response.status == Status.LOADING) {
            callback.onLoading(response.data);
        }
    }
}
