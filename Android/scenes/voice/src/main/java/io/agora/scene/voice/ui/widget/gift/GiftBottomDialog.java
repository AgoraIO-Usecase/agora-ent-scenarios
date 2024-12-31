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

import io.agora.scene.base.component.BaseBottomSheetDialogFragment;
import io.agora.scene.base.utils.KtExtendKt;
import io.agora.scene.voice.R;
import io.agora.scene.voice.VoiceLogger;
import io.agora.scene.voice.databinding.VoiceDialogGiftLayoutBinding;
import io.agora.scene.voice.databinding.VoicePopGiftLayoutBinding;
import io.agora.scene.voice.model.GiftBean;
import io.agora.scene.voice.ui.widget.CommonPopupWindow;

public class GiftBottomDialog extends BaseBottomSheetDialogFragment<VoiceDialogGiftLayoutBinding> implements View.OnClickListener {
    private int currentIndex = 0;
    private GiftFragmentAdapter adapter;
    private OnSendClickListener listener;
    private List<GiftBean> list;
    private int GiftNum = 1;
    private GiftBean giftBean;
    private VoiceDialogGiftLayoutBinding binding;


    public static GiftBottomDialog getNewInstance() {
        return new GiftBottomDialog();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mBinding != null)
            binding = mBinding;
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
                binding.pagerDots.getChildAt(position).setEnabled(false);
                binding.pagerDots.getChildAt(currentIndex).setEnabled(true);
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
     * 
     *
     * @param llGuideGroup
     * @param count        
     */
    public void addViewPagerDots(LinearLayoutCompat llGuideGroup, int count) {
        VoiceLogger.d("addViewPagerDots", "count: " + count);
        if (llGuideGroup == null || count < 1 || getContext() == null) return;
        LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(
                (int) KtExtendKt.getDp(5),  (int) KtExtendKt.getDp(5));
        lp.leftMargin =  (int) KtExtendKt.getDp(5);
        lp.rightMargin =  (int) KtExtendKt.getDp(5);
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(llGuideGroup.getContext());
            imageView.setLayoutParams(lp);
            imageView.setEnabled(true);
            imageView.setOnClickListener(this);
            imageView.setTag(i);
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
                    .width((int) KtExtendKt.getDp( 120))
                    .height((int) KtExtendKt.getDp(186))
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
                                if (mBinding != null)
                                    mBinding.totalCount.setText(getString(R.string.voice_dialog_gift_total_count, String.valueOf(total)));
                                if (giftBean != null && GiftNum >= 1) {
                                    giftBean.setNum(GiftNum);
                                    mBinding.count.setText(num);
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
                            isShowPop(false);
                        }
                    })
                    .build(getContext())
                    .showAtLocation(itemView, Gravity.NO_GRAVITY,
                            location[0] - (int) KtExtendKt.getDp(60) / 3,
                            location[1] - (int) KtExtendKt.getDp(186));
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