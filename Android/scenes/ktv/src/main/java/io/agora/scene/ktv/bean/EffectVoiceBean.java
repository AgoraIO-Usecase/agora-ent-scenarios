package io.agora.scene.ktv.bean;

import androidx.annotation.DrawableRes;

/**
 * ---------------------------------------------------------------------------------------------
 * 功能描述:
 * ---------------------------------------------------------------------------------------------
 * 时　　间: 2023/3/1
 * ---------------------------------------------------------------------------------------------
 * 代码创建: Leo
 * ---------------------------------------------------------------------------------------------
 * 代码备注:
 * ---------------------------------------------------------------------------------------------
 **/
public class EffectVoiceBean {

    private int id;
    private @DrawableRes int resId;
    private String title;

    private boolean select;

    public EffectVoiceBean(int id, int resId, String title) {
        this.id = id;
        this.resId = resId;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public EffectVoiceBean setId(int id) {
        this.id = id;
        return this;
    }

    public int getResId() {
        return resId;
    }

    public EffectVoiceBean setResId(int resId) {
        this.resId = resId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public EffectVoiceBean setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isSelect() {
        return select;
    }

    public EffectVoiceBean setSelect(boolean select) {
        this.select = select;
        return this;
    }
}
