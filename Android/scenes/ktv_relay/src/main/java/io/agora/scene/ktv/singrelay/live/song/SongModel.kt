package io.agora.scene.ktv.singrelay.live.song

import io.agora.scene.ktv.singrelay.service.ChooseSongInputModel
import kotlin.random.Random

object SongModel {
    private val song1 = ChooseSongInputModel(
            "美人鱼",
            "6625526604232820",
            "林俊杰",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/661208.jpg",
            listOf(55076, 97357, 150592, 190350, 243041)
    )

    private val song2 = ChooseSongInputModel(
            "凄美地",
            "6625526611288130",
            "郭顶",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/126936.jpg",
            listOf(44705, 89415, 132980, 192512, 244558)
    )

    private val song3 = ChooseSongInputModel(
            "一直很安静",
            "6654550232746660",
            "阿桑",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/961853.jpg",
            listOf(57291, 76635, 130940, 148464, 210536)
    )

    private val song4 = ChooseSongInputModel(
            "起风了",
            "6625526603305730",
            "买辣椒也用券",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/385062.jpg",
            listOf(63836, 109502, 154446, 194166, 274685)
    )

    fun getRandomGameSong() : ChooseSongInputModel {
        val songList = listOf(song1, song2, song3, song4)
        return songList[(0..3).random()]
    }

    fun getSongPartListWithSongCode(songCode: String): List<Long> {
        listOf(song1, song2, song3, song4).forEach {
            if (it.songNo == songCode) {
                return it.relayList
            }
        }
        return listOf()
    }
}