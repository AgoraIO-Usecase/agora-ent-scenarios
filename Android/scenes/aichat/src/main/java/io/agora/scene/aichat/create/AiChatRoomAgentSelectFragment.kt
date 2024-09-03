package io.agora.scene.aichat.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.aichat.R
import io.agora.scene.aichat.create.event.AiChatRoomCreateViewModel
import io.agora.scene.aichat.create.event.ContactItem
import io.agora.scene.aichat.databinding.AichatFragmentAiAgentSelectBinding
import io.agora.scene.aichat.databinding.AichatItemContactPageContainerBinding
import io.agora.scene.aichat.databinding.AichatItemContactSelectBinding
import io.agora.scene.base.component.BaseViewBindingFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class AiChatRoomAgentSelectFragment :
    BaseViewBindingFragment<AichatFragmentAiAgentSelectBinding>() {

    private val mTabs by lazy {
        listOf(
            getString(R.string.aichat_all),
            getString(R.string.aichat_public_agent),
            getString(R.string.aichat_private_agent)
        )
    }
    private val vm by activityViewModels<AiChatRoomCreateViewModel>()
    private val rvDatas by lazy { mutableListOf<ContactItem>() }
    private val vpDatas by lazy { mutableListOf<ContactItem>() }
    private val rvAdapter by lazy {
        object : QuickAdapter<AichatItemContactSelectBinding, ContactItem>(
            AichatItemContactSelectBinding::inflate,
            rvDatas
        ) {
            override fun onBind(
                binding: AichatItemContactSelectBinding,
                datas: List<ContactItem>,
                position: Int
            ) {
                val item = datas[position]
                binding.tvName.text = "page${item.pageIndex} ${datas[position].name}"
                if (item.isCheck) {
                    binding.ivCheck.setImageResource(R.drawable.checked_on)
                } else {
                    binding.ivCheck.setImageResource(R.drawable.checked_off)
                }
            }
        }
    }
    private val vpAdapter by lazy {
        object : QuickAdapter<AichatItemContactPageContainerBinding, ContactItem>(
            AichatItemContactPageContainerBinding::inflate,
            vpDatas
        ) {
            override fun onBind(
                binding: AichatItemContactPageContainerBinding,
                datas: List<ContactItem>,
                position: Int
            ) {
                binding.rv.adapter = rvAdapter
            }

            override fun getItemCount(): Int {
//                return vpDatas.groupBy { it.pageIndex }.size
                return mTabs.size
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AichatFragmentAiAgentSelectBinding {
        return AichatFragmentAiAgentSelectBinding.inflate(inflater, container, false)
    }

    override fun initListener() {
        binding.tvConfirmSelect.setOnClickListener {
            findNavController().navigateUp()
        }
        rvAdapter.onItemClickListener = { datas, position ->
            val item = datas[position]
            vm.updateContactByKey(item.key, !item.isCheck)
            bindRvList(datas[position].pageIndex)
        }
        //初始化vp
        binding.vp.let {
            it.adapter = vpAdapter
            it.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (vpDatas.count { it.pageIndex == position } > 0) {
                        binding.llEmpty.visibility = View.GONE
                    } else {
                        binding.llEmpty.visibility = View.VISIBLE
                    }
                    bindRvList(position)
                }
            })
            // 将 TabLayout 与 ViewPager2 关联
            TabLayoutMediator(binding.tabLayout, binding.vp) { tab, position ->
                tab.setCustomView(R.layout.aichat_contact_select_tabitem)
                val tvTabTitle: TextView =
                    tab.customView?.findViewById(R.id.tvTabTitle) ?: return@TabLayoutMediator
                tvTabTitle.text = mTabs[position].toString()
            }.attach()
            binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tab.customView?.let {
                        it.findViewById<View>(R.id.viewIndicator).visibility = View.VISIBLE
                        it.findViewById<TextView>(R.id.tvTabTitle)
                            .setTextAppearance(R.style.aichat_TabLayoutTextSelected)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    tab.customView?.let {
                        it.findViewById<View>(R.id.viewIndicator).visibility = View.INVISIBLE
                        it.findViewById<TextView>(R.id.tvTabTitle)
                            .setTextAppearance(R.style.aichat_TabLayoutTextUnSelected)
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    tab.customView?.let {
                        it.findViewById<View>(R.id.viewIndicator).visibility = View.VISIBLE
                        it.findViewById<TextView>(R.id.tvTabTitle)
                            .setTextAppearance(R.style.aichat_TabLayoutTextSelected)
                    }
                }

            })
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
        }
        //刷新数据
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.contacts.collectLatest {
                    vpDatas.clear()
                    vpDatas.addAll(it)
                    vpAdapter.notifyDataSetChanged()
                }
            }
        }
        //监听添加按钮的数据驱动
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.selectDatas.collectLatest {
                    binding.tvConfirmSelect.apply {
                        val selectCount = it.count { item -> item.key >= 0 }
                        if (selectCount <= 0) {
                            isEnabled = false
                            setTextColor(ContextCompat.getColor(context, R.color.def_text_grey_979))
                        } else {
                            isEnabled = true
                            setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.aichat_text_blue_00
                                )
                            )
                        }
                        text = getString(R.string.aichat_add) + "(${selectCount})"
                    }
                }
            }
        }
    }

    private fun bindRvList(pageIndex: Int) {
        rvDatas.clear()
        val list = vpDatas.filter { it.pageIndex == pageIndex }
        rvDatas.addAll(list)
        rvAdapter.notifyDataSetChanged()

    }
}