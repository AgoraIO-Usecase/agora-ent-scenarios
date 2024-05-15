package io.agora.scene.ktv.singrelay.live.song

import io.agora.scene.ktv.singrelay.service.ChooseSongInputModel
import kotlin.random.Random

object SongModel {
    private val song1 = ChooseSongInputModel(
            "美人鱼",
            "6625526604232820",
            "林俊杰",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/661208.jpg",
            listOf(55606, 97357, 150592, 190350, 243041)
    )

    private val song2 = ChooseSongInputModel(
            "一直很安静",
            "6654550232746660",
            "阿桑",
            "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/961853.jpg",
            listOf(57291, 76635, 130940, 148464, 210536)
    )

    fun getRandomGameSong() : ChooseSongInputModel {
        val songList = listOf(song1, song2)
        return songList[(0..1).random()]
    }

    fun getSongPartListWithSongCode(songCode: String): List<Long> {
        listOf(song1, song2).forEach {
            if (it.songNo == songCode) {
                return it.relayList
            }
        }
        return listOf()
    }
}