package io.agora.scene.voice.ui.widget.gift;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import io.agora.scene.voice.R;
import io.agora.scene.voice.databinding.VoiceDialogGiftLayoutBinding;
import io.agora.scene.voice.databinding.VoicePopGiftLayoutBinding;
import io.agora.scene.voice.model.GiftBean;
import io.agora.voice.common.ui.dialog.BaseSheetDialog;
import io.agora.voice.common.ui.widget.CommonPopupWindow;
import io.agora.voice.common.utils.DeviceTools;
import io.agora.voice.common.utils.LogTools;

public class GiftBottomDialog extends BaseSheetDialog<VoiceDialogGiftLayoutBinding> implements View.OnClickListener {
    private int currentIndex = 0;//当前页面,默认首页
    private GiftFragmentAdapter adapter;
    private OnSendClickListener listener;
    private List<GiftBean> list;
    private int GiftNum = 1;
    private GiftBean giftBean;
    private VoiceDialogGiftLayoutBinding binding;


    public static GiftBottomDialog getNewInstance() {
        return new GiftBottomDialog();
    }

    @Nullable
    @Override
    protected VoiceDialogGiftLayoutBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return VoiceDialogGiftLayoutBinding.inflate(inflater, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getBinding() != null)
            binding = getBinding();
        setOnApplyWindowInsets(binding.getRoot());
        initView(savedInstanceState);
        initListener();
        initData();
    }

    public void initView(Bundle savedInstanceState) {
        if (getActivity() != null)
            adapter = new GiftFragmentAdapter(getActivity());
        binding.viewPager.setAdapter(adapter);

    }

    public void initData() {
        list = GiftRepository.getDefaultGifts(getContext());
        initPoints();
    }

    public void initListener() {
        binding.viewPager.setOffscreenPageLimit(1);
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                //当前页卡被选择时,position为当前页数
                binding.pagerDots.getChildAt(position).setEnabled(false);//不可点击
                binding.pagerDots.getChildAt(currentIndex).setEnabled(true);//恢复之前页面状态
                currentIndex = position;
                if (currentIndex == 0) {
                    binding.pagerDots.getChildAt(currentIndex).setEnabled(false);
                } else if (currentIndex == 1) {

                }

            }
        });
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (giftBean != null && listener != null && giftBean.isChecked())
                    listener.SendGift(v, giftBean);
            }
        });
        binding.giftCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPop(v);
            }
        });
        adapter.setOnVpFragmentItemListener(new GiftFragmentAdapter.OnVpFragmentItemListener() {
            @Override
            public void onVpFragmentItem(int position, Object bean) {
                giftBean = (GiftBean) bean;
                check(giftBean.getPrice());
                reset();
            }

            @Override
            public void onFirstData(GiftBean bean) {
                giftBean = bean;
                check(bean.getPrice());
                if (getActivity() != null && isAdded())
                    binding.totalCount.setText(getActivity().getString(R.string.voice_dialog_gift_total_count, bean.getPrice()));
                binding.count.setText("1");
            }
        });
    }

    private void isShowPop(boolean isShow) {
        if (isShow) {
            binding.icon.setImageResource(R.drawable.voice_icon_arrow_down);
        } else {
            binding.icon.setImageResource(R.drawable.voice_icon_arrow_up);
        }
    }

    @Override
    public void onClick(View v) {
        binding.viewPager.setCurrentItem((int) v.getTag());
    }

    private void initPoints() {
        addViewPagerDots(binding.pagerDots, Math.round((list.size() / 4) + 0.5f));
    }

    /**
     * 向一个线性布局里添加小圆点
     *
     * @param llGuideGroup
     * @param count        要添加多少个小圆点
     */
    public void addViewPagerDots(LinearLayoutCompat llGuideGroup, int count) {
        LogTools.d("addViewPagerDots", "count: " + count);
        if (llGuideGroup == null || count < 1 || getContext() == null) return;
        LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(
                DeviceTools.dp2px(getContext(), 5), DeviceTools.dp2px(getContext(), 5));
        lp.leftMargin = DeviceTools.dp2px(getContext(), 5);
        lp.rightMargin = DeviceTools.dp2px(getContext(), 5);
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(llGuideGroup.getContext());
            imageView.setLayoutParams(lp);
            imageView.setEnabled(true);//设置当前状态为允许点击（可点，灰色）
            imageView.setOnClickListener(this);//设置点击监听
            //额外设置一个标识符，以便点击小圆点时跳转对应页面
            imageView.setTag(i);//标识符与圆点顺序一致
            imageView.setBackgroundResource(R.drawable.voice_bg_gift_vp_point);
            llGuideGroup.addView(imageView);
        }
    }


    public void setOnConfirmClickListener(OnSendClickListener listener) {
        this.listener = listener;
    }

    private void showPop(View itemView) {
        //Gets the coordinates attached to the view
        int[] location = new int[2];
        itemView.getLocationInWindow(location);
        if (getContext() != null) {
            new CommonPopupWindow.ViewDataBindingBuilder<VoicePopGiftLayoutBinding>()
                    .viewDataBinding(VoicePopGiftLayoutBinding.inflate(LayoutInflater.from(getContext())))
                    .width(DeviceTools.dp2px(getContext(), 120))
                    .height(DeviceTools.dp2px(getContext(), 186))
                    .outsideTouchable(true)
                    .focusable(true)
                    .clippingEnabled(false)
                    .alpha(0.618f)
                    .intercept((popupWindow, view) -> {
                        isShowPop(true);
                        String[] data = {"999", "599", "199", "99", "9", "1"};
                        GiftAdapter adapter = new GiftAdapter(getContext(), 1, data);
                        view.listView.setAdapter(adapter);
                        adapter.setOnItemClickListener(new GiftAdapter.OnItemClickListener() {
                            @Override
                            public void OnItemClick(int position, String num) {
                                GiftNum = Integer.parseInt(num);
                                int total = GiftNum * Integer.parseInt(giftBean.getPrice());
                                if (getBinding() != null)
                                    getBinding().totalCount.setText(getString(R.string.voice_dialog_gift_total_count, String.valueOf(total)));
                                if (giftBean != null && GiftNum >= 1) {
                                    giftBean.setNum(GiftNum);
                                    getBinding().count.setText(num);
                                    //礼物金额大于100的 数量只能选1
                                    if (Integer.parseInt(giftBean.getPrice()) >= 100) {
                                        reset();
                                    }
                                }
                                popupWindow.dismiss();
                            }
                        });
                    })
                    .onDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            /*每次dismiss都会回调*/
                            isShowPop(false);
                        }
                    })
                    .build(getContext())
                    .showAtLocation(itemView, Gravity.NO_GRAVITY,
                            location[0] - DeviceTools.dp2px(getContext(), 60) / 3,
                            location[1] - DeviceTools.dp2px(getContext(), 186));
        }
    }

    public void reset() {
        if (binding != null)
            binding.count.setText("1");
        if (null != giftBean) {
            giftBean.setNum(1);
            binding.totalCount.setText(getString(R.string.voice_dialog_gift_total_count, giftBean.getPrice()));
        }
    }

    public void check(String price) {
        if (Integer.parseInt(price) < 100 && binding != null) {
            binding.icon.setAlpha(1.0f);
            binding.count.setAlpha(1.0f);
            binding.giftCountLayout.setEnabled(true);
        } else {
            if (binding != null) {
                binding.icon.setAlpha(0.2f);
                binding.count.setAlpha(0.2f);
                binding.giftCountLayout.setEnabled(false);
            }
        }
    }

    public void setSendEnable(boolean enable) {
        if (binding != null)
            binding.send.setEnabled(enable);
    }
}