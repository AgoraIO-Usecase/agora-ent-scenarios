package io.agora.scene.show.service;

/**
 * ---------------------------------------------------------------------------------------------
 * 功能描述:
 * ---------------------------------------------------------------------------------------------
 * 时　　间: 2023/2/7
 * ---------------------------------------------------------------------------------------------
 * 代码创建: Leo
 * ---------------------------------------------------------------------------------------------
 * 代码备注:
 * ---------------------------------------------------------------------------------------------
 **/
public class RoomException extends RuntimeException{

    private String currRoomNo;

    public RoomException(String message, String currRoomNo) {
        super(message);
        this.currRoomNo = currRoomNo;
    }

    public String getCurrRoomNo() {
        return currRoomNo;
    }

    public RoomException setCurrRoomNo(String currRoomNo) {
        this.currRoomNo = currRoomNo;
        return this;
    }
}
