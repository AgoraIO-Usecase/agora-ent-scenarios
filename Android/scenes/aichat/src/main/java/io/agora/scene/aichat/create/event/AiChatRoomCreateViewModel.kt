package io.agora.scene.aichat.create.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.R
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class ContactItem(
    val pageIndex: Int,
    val key: Int,
    val name: String,
    var isCheck: Boolean = false
)

class AiChatRoomCreateViewModel : ViewModel() {

    companion object {
        const val MAX_SELECT_COUNT = 5
    }

    private val selfItem = ContactItem(0, -1, "this")
    private val placeHolder = ContactItem(0, -1, "placeholder")

    private val _selectDatas by lazy { MutableStateFlow(listOf(selfItem, placeHolder)) }
    val selectDatas: MutableStateFlow<List<ContactItem>> = _selectDatas

    private val _contacts by lazy { MutableStateFlow(emptyList<ContactItem>()) }
    val contacts: MutableStateFlow<List<ContactItem>> = _contacts


    init {
        viewModelScope.launch {
            val contactDatas = mutableListOf<ContactItem>()
            repeat(50) { i ->
                contactDatas.add(ContactItem(i % 2, i, "name $i"))
            }
            contacts.value = contactDatas
        }
    }

    fun updateContactByKey(pos: Int,select:Boolean) {
        val list = _contacts.value.toMutableList()
        val item = list.find { it.key == pos }
        if (item != null) {
            if (!item.isCheck) {
                val count = contacts.value.filter { it.isCheck }.count()
                if (count >= MAX_SELECT_COUNT) {
                    ToastUtils.showToast(
                        AgoraApplication.the().getString(R.string.aichat_group_create_desc)
                    )
                    return
                }
            }
            item.isCheck = select
            _contacts.value = list

            //更新选择的用户
            val list = mutableListOf<ContactItem>()
            list.add(selfItem)
            val contactItemList: List<ContactItem> = contacts.value.filter { it.isCheck }
            list.addAll(contactItemList)
            val count = contactItemList.count()
            if (count < MAX_SELECT_COUNT) {
                list.add(placeHolder)
            }
            _selectDatas.value = list
        }
    }
}