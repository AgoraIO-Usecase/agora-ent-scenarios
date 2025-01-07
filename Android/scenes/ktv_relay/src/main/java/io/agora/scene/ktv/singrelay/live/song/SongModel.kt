package io.agora.scene.ktv.singrelay.live.song

import io.agora.scene.ktv.singrelay.service.ChooseSongInputModel

object SongModel {

    private val song = ChooseSongInputModel(
        "Let Go",
        "1000000",
        "Anonymous Annoyance",
        "https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/661208.jpg",
        listOf(44705, 89415, 132980, 192512, 244558)
    )

    fun getRandomGameSong() : ChooseSongInputModel {
        val songList = listOf(song)
        return songList[0]
    }

    fun getSongPartListWithSongCode(songCode: String): List<Long> {
        listOf(song).forEach {
            if (it.songNo == songCode) {
                return it.relayList
            }
        }
        return listOf()
    }
}