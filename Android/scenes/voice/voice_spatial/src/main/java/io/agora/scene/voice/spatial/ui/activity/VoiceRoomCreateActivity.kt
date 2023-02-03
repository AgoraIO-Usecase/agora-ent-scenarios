package io.agora.scene.voice.spatial.ui.activity

import android.graphics.Typeface
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialActivityCreateRoomLayoutBinding
import io.agora.scene.voice.spatial.model.PageBean
import io.agora.scene.voice.spatial.model.VoiceRoomModel
import io.agora.scene.voice.spatial.service.VoiceServiceProtocol
import io.agora.scene.voice.spatial.viewmodel.VoiceCreateViewModel
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.net.Resource
import io.agora.voice.common.ui.BaseUiActivity
import io.agora.voice.common.utils.DeviceTools
import io.agora.voice.common.utils.FastClickTools.isFastClick
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.StatusBarCompat
import io.agora.voice.common.utils.ThreadManager
import io.agora.voice.common.utils.ToastTools
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.roundToInt

class VoiceRoomCreateActivity : BaseUiActivity<VoiceSpatialActivityCreateRoomLayoutBinding>() {

    private var isPublic = true
    private val pageData: List<PageBean> by lazy {
        mutableListOf<PageBean>().apply {
            val bean = PageBean()
            bean.room_type = 1
            bean.tab_title = getString(R.string.voice_tab_layout_chat_room)
            bean.room_name = getString(R.string.voice_room_create_chat_room)
            bean.room_desc = getString(R.string.voice_room_create_chat_room_desc)
            add(bean)
        }
    }
    private var roomType = 0
    private var encryption: String = ""
    private var roomName: String = ""
    private lateinit var voiceRoomViewModel: VoiceCreateViewModel
    private var curVoiceRoomModel: VoiceRoomModel? = null

    override fun getViewBinding(inflater: LayoutInflater): VoiceSpatialActivityCreateRoomLayoutBinding {
        return VoiceSpatialActivityCreateRoomLayoutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarCompat.setLightStatusBar(this, true)
        super.onCreate(savedInstanceState)
        voiceRoomViewModel = ViewModelProvider(this)[VoiceCreateViewModel::class.java]
        initUi()
//        binding.edRoomName.filters = arrayOf<InputFilter>(EmojiInputFilter(32))
        binding.titleBar.title.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        initListener()
        voiceRoomObservable()
        setupWithViewPager()
    }

    private fun initListener() {
        binding.edPwd.setOnTextChangeListener {
            if (it.length >= 4) hideKeyboard()
        }
        binding.radioGroupGender.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.radioButton_private) {
                isPublic = false
            } else if (checkedId == R.id.radioButton_public) {
                isPublic = true
            }
            initUi()
        }
        binding.titleBar.setOnBackPressListener {
            onBackPressed()
        }
        binding.bottomNext.setOnClickListener {
            if (roomType == 1) {
                binding.bottomNext.isEnabled = false
                showLoading(false)
            }
            encryption = binding.edPwd.text.toString().trim { it <= ' ' }
            roomName = binding.edRoomName.text.toString().trim { it <= ' ' }
            if (it?.let { isFastClick(it, 1000) } == false) checkPrivate()
        }
        binding.randomLayout.setOnClickListener {
            binding.edRoomName.setText(randomName())
        }
        binding.agoraTabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.customView?.let {
                    val title = it.findViewById<TextView>(R.id.tab_item_title)
                    val layoutParams = title.layoutParams
                    layoutParams.height = DeviceTools.dp2px(this@VoiceRoomCreateActivity, 26f)
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    title.gravity = Gravity.CENTER
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.customView?.let {
                    val title = it.findViewById<TextView>(R.id.tab_item_title)
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        binding.vpFragment.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                roomType = pageData[position].room_type
                if (roomType == 0) {
                    binding.bottomNext.text = getString(R.string.voice_room_create_next)
                } else {
                    binding.bottomNext.text = getString(R.string.voice_room_create_go_live)
                }
                binding.edRoomName.setText(randomName())
            }
        })
        binding.baseLayout.setOnTouchListener { v, event ->
            hideKeyboard()
            false
        }
    }

    private fun voiceRoomObservable() {
        voiceRoomViewModel.createRoomObservable().observe(this) { response: Resource<VoiceRoomModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceRoomModel>() {
                override fun onSuccess(voiceRoomModel: VoiceRoomModel?) {
                    curVoiceRoomModel = voiceRoomModel
                    voiceRoomModel?.let {
                        voiceRoomViewModel.joinRoom(it.roomId)
                    }
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    binding.bottomNext.isEnabled = true
                    dismissLoading()
                    when (code) {
                        VoiceServiceProtocol.ERR_LOGIN_ERROR -> {
                            ToastTools.show(this@VoiceRoomCreateActivity, getString(R.string.voice_room_login_exception))
                        }
                        VoiceServiceProtocol.ERR_ROOM_NAME_INCORRECT -> {
                            ToastTools.show(this@VoiceRoomCreateActivity, getString(R.string.voice_room_name_rule))
                        }
                        else -> {
                            ToastTools.show(this@VoiceRoomCreateActivity, getString(R.string.voice_room_create_error))
                        }
                    }
                }
            })
        }
        voiceRoomViewModel.joinRoomObservable().observe(this) { response: Resource<VoiceRoomModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceRoomModel>() {
                override fun onSuccess(result: VoiceRoomModel?) {
                    curVoiceRoomModel = result ?: return
                    ThreadManager.getInstance().runOnMainThread {
                        goVoiceRoom()
                    }
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    binding.bottomNext.isEnabled = true
                    dismissLoading()
                    "VoiceRoomCreateActivity syncJoinRoom fail:$code $message".logD()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.edPwd.rest()
    }

    private fun setupWithViewPager() {
        binding.vpFragment.offscreenPageLimit = 1
        val recyclerView = binding.vpFragment.getChildAt(0)
        if (recyclerView is RecyclerView) {
            recyclerView.setPadding(DeviceTools.dp2px(this, 30f), 0, DeviceTools.dp2px(this, 30f), 0)
            recyclerView.clipToPadding = false
        }
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(DeviceTools.dp2px(this, 16f)))
        binding.vpFragment.setPageTransformer(compositePageTransformer)
        // set adapter
        binding.vpFragment.adapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(this@VoiceRoomCreateActivity)
                    .inflate(R.layout.voice_spatial_create_page_item_layout, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                if (pageData[position].room_type == 0) {
                    holder.mLayout.setBackgroundResource(R.drawable.voice_icon_create_chat_room)
                    holder.mTitle.text = getString(R.string.voice_room_create_chat_room)
                    holder.mContent.text = getString(R.string.voice_room_create_chat_room_desc)
                } else if (pageData[position].room_type == 1) {
                    holder.mLayout.setBackgroundResource(R.drawable.voice_icon_create_3d_room)
                    holder.mTitle.text = getString(R.string.voice_room_create_3d_room)
                    holder.mContent.text = getString(R.string.voice_room_create_3d_room_desc)
                }
            }

            override fun getItemCount(): Int {
                return pageData.size
            }
        }

        // set TabLayoutMediator
        val mediator = TabLayoutMediator(binding.agoraTabLayout, binding.vpFragment) { tab, position ->
            tab.setCustomView(R.layout.voice_spatial_create_tab_item_layout)
            tab.customView?.findViewById<TextView>(R.id.tab_item_title)?.text = pageData[position].tab_title
        }
        // setup with viewpager2
        mediator.attach()
    }

    fun goVoiceRoom() {
        curVoiceRoomModel?.let {
            binding.bottomNext.isEnabled = true
            dismissLoading()
            "voice room create joinChatRoom onSuccess".logD()
            ChatroomLiveActivity.startActivity(this, it)
            finish()
        }
    }

    private fun initUi() {
        if (isPublic) {
            binding.edPwd.visibility = View.GONE
            binding.baseLayout.requestFocus()
            hideKeyboard()
            binding.inputTip.visibility = View.GONE
            binding.tipsLayout.visibility = View.VISIBLE
        } else {
            binding.edPwd.visibility = View.VISIBLE
            binding.edPwd.isFocusable = true
            binding.edPwd.isFocusableInTouchMode = true
            binding.edPwd.requestFocus()
            showKeyboard(binding.edPwd)
            binding.tipsLayout.visibility = View.GONE
        }
    }

    private fun checkPrivate() {
        if (TextUtils.isEmpty(roomName)) {
            ToastTools.show(this, getString(R.string.voice_room_create_empty_name), Toast.LENGTH_LONG)
            binding.bottomNext.isEnabled = true
            dismissLoading()
            return
        }
        if (!isPublic && encryption.length != 4) {
            binding.inputTip.visibility = View.VISIBLE
            ToastTools.show(this, getString(R.string.voice_room_create_tips), Toast.LENGTH_LONG)
            binding.bottomNext.isEnabled = true
            dismissLoading()
            return
        }
        binding.inputTip.visibility = View.GONE
        if (roomType == 0) {
            VoiceRoomSoundSelectionActivity.startActivity(this, roomName, isPublic, encryption, roomType)
        } else if (roomType == 1) {
            createSpatialRoom()
        }
    }

    private fun createSpatialRoom() {
        voiceRoomViewModel.createSpatialRoom(roomName, 0, encryption)
    }

    private fun randomName(): String {
        var roomName = ""
        val date = Date()
        val month = SimpleDateFormat("MM").format(date) //获取月份
        val day = SimpleDateFormat("dd").format(date) //获取分钟
        roomName = if (roomType == 0) {
            getString(R.string.voice_room_create_chat_room) + "-" + month + day + "-" + (Math.random() * 999 + 1).roundToInt()
        } else {
            getString(R.string.voice_room_create_chat_3d_room) + "-" + month + day + "-" + (Math.random() * 999 + 1).roundToInt()
        }
        return roomName
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mLayout: ConstraintLayout
        val mTitle: TextView
        val mContent: TextView

        init {
            mLayout = itemView.findViewById(R.id.item_layout)
            mTitle = itemView.findViewById(R.id.item_title)
            mContent = itemView.findViewById(R.id.item_text)
        }
    }

    class EmojiInputFilter(max: Int) : InputFilter.LengthFilter(max) {
        private var emoji = Pattern.compile(
            "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
            Pattern.UNICODE_CASE or Pattern.CASE_INSENSITIVE
        )

        override fun filter(
            source: CharSequence,//即将要输入的字符串
            start: Int,//source的start
            end: Int,//source的end
            dest: Spanned,//输入框中原来的内容
            dstart: Int,//光标所在位置
            dend: Int//光标终止位置
        ): CharSequence {
            var resultInput = source.toString()
            val emojiMatcher: Matcher = emoji.matcher(source)
            if (emojiMatcher.find()) {
                return ""
            }
            return resultInput
        }
    }
}