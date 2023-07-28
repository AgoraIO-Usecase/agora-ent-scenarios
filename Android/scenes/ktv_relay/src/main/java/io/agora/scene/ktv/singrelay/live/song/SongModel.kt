package io.agora.scene.ktv.singrelay.live.song

import io.agora.scene.ktv.singrelay.service.ChooseSongInputModel
import kotlin.random.Random

object SongModel {
    private val song1 = ChooseSongInputModel(
            "勇气大爆发",
            "6805795303139450",
            "贝乐虎；土豆王国小乐队；奶糖乐团",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/CASW1078064.jpg",
            listOf(32000, 47000, 81433, 142000, 176000)
    )

    private val song2 = ChooseSongInputModel(
            "美人鱼",
            "6625526604232820",
            "林俊杰",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/661208.jpg",
            listOf(55000, 97000, 150000, 190000, 243000)
    )

    private val song3 = ChooseSongInputModel(
            "天外来物",
            "6388433023669520",
            "薛之谦",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/CJ1420004109.jpg",
            listOf(91000, 129000, 173000, 212000, 251000)
    )

    private val song4 = ChooseSongInputModel(
            "凄美地",
            "6625526611288130",
            "郭顶",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/126936.jpg",
            listOf(44000, 89000, 132000, 192000, 244000)
    )

    private val song5 = ChooseSongInputModel(
            "一直很安静",
            "6654550232746660",
            "阿桑",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/961853.jpg",
            listOf(57000, 76000, 130000, 148000, 210000)
    )

    private val song6 = ChooseSongInputModel(
            "他不懂",
            "6625526604594370",
            "张杰",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/792885.jpg",
            listOf(46000, 81000, 124000, 159000, 207000)
    )

    private val song7 = ChooseSongInputModel(
            "一路向北",
            "6357555536291690",
            "周杰伦",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/961979.jpg",
            listOf(90000, 118000, 194000, 222000, 262000)
    )

    private val song8 = ChooseSongInputModel(
            "天黑黑",
            "6246262727285990",
            "孙燕姿",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/147907.jpg",
            listOf(51000, 85000, 122000, 176000, 223000)
    )

    private val song9 = ChooseSongInputModel(
            "起风了",
            "6625526603305730",
            "买辣椒也用券",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/385062.jpg",
            listOf(63000, 109000, 154000, 194000, 274000)
    )

    private val song10 = ChooseSongInputModel(
            "这世界那么多人",
            "6654550267486590",
            "莫文蔚",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/CJ1420010039.jpg",
            listOf(91000, 147000, 191000, 235000, 295000)
    )

    fun getRandomGameSong() : ChooseSongInputModel {
        val songList = listOf(song1, song2, song3, song4, song5, song6, song7, song8, song9, song10)
        return songList[(0..9).random()]
    }

    fun getSongPartListWithSongCode(songCode: String): List<Long> {
        listOf(song1, song2, song3, song4, song5, song6, song7, song8, song9, song10).forEach {
            if (it.songNo == songCode) {
                return it.relayList
            }
        }
        return listOf()
    }
}