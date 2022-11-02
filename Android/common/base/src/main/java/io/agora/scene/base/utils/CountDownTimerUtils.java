package io.agora.scene.base.utils;

import android.os.Build;
import android.os.CountDownTimer;
import android.text.Html;

import androidx.appcompat.widget.AppCompatTextView;

public class CountDownTimerUtils extends CountDownTimer {
    private AppCompatTextView mTvTime;

    public CountDownTimerUtils(AppCompatTextView mTvTime,
                               int millisInFuture,
                               int countDownInterval) {
        super(millisInFuture, countDownInterval);
        this.mTvTime = mTvTime;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        mTvTime.setClickable(false);
        String openHtmlText =
                "<font color='#F7B500'>重新获取</font><font color='#F7B500'>(" + millisUntilFinished / 1000 +
                        "s)</font>";
        mTvTime.setText(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                        Html.fromHtml(openHtmlText, Html.FROM_HTML_MODE_LEGACY)
                        : Html.fromHtml(openHtmlText));
    }

    @Override
    public void onFinish() {
        mTvTime.setClickable(true);
        mTvTime.setText("重新获取");
    }
}
