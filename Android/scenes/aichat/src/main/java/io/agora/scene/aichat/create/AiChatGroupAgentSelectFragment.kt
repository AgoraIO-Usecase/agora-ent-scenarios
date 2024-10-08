package io.agora.scene.aichat.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
import io.agora.scene.aichat.create.logic.AiChatGroupCreateViewModel
import io.agora.scene.aichat.create.logic.ContactItem
import io.agora.scene.aichat.databinding.AichatFragmentAiAgentSelectBinding
import io.agora.scene.aichat.databinding.AichatItemContactPageContainerBinding
import io.agora.scene.aichat.databinding.AichatItemContactSelectBinding
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.base.component.BaseViewBindingFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class AiChatGroupAgentSelectFragment : BaseViewBindingFragment<AichatFragmentAiAgentSelectBinding>() {

    private val mTabs by lazy {
        listOf(
            getString(R.string.aichat_all),
            getString(R.string.aichat_public_agent),
            getString(R.string.aichat_private_agent)
        )
    }
    private val vm by activityViewModels<AiChatGroupCreateViewModel>()
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
                binding.tvName.text = item.name
                binding.ivIcon.loadCircleImage(item.avatar ?: "")
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
        binding.tvBackTitle.setOnClickListener {
            findNavController().navigateUp()
        }
        rvAdapter.onItemClickListener = { datas, position ->
            val item = datas[position]
            vm.updateContactByKey(item.userId, !item.isCheck)
            bindRvList(binding.vp.currentItem)
        }

        //初始化vp
        binding.vp.let {
            it.adapter = vpAdapter
            it.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val dataList = when (position) {
                        1 -> vpDatas.filter { contactItem -> contactItem.isPublic }
                        2 -> vpDatas.filter { contactItem -> !contactItem.isPublic }
                        else -> vpDatas
                    }
                    binding.llEmpty.isVisible = dataList.isEmpty()
                    bindRvList(position)
                }
            })
            // 将 TabLayout 与 ViewPager2 关联
            TabLayoutMediator(binding.tabLayout, binding.vp) { tab, position ->
                tab.setCustomView(R.layout.aichat_tabitem_contact_select)
                val tvTabTitle: TextView =
                    tab.customView?.findViewById(R.id.tvTabTitle) ?: return@TabLayoutMediator
                tvTabTitle.text = mTabs[position]

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
                        val selectCount =
                            it.count { item -> item.userId.isNotEmpty() && item.userId != EaseIM.getCurrentUser().id }
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
        val dataList = when (pageIndex) {
            1 -> vpDatas.filter { contactItem -> contactItem.isPublic }
            2 -> vpDatas.filter { contactItem -> !contactItem.isPublic }
            else -> vpDatas
        }
        rvDatas.clear()
        rvDatas.addAll(dataList)
        rvAdapter.notifyDataSetChanged()
    }
}