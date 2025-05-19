package io.agora.scene.aichat.imkit.model

/**
 * The type of loading data.
 */
enum class EaseLoadDataType {
    /**
     * Load messages from local database.
     * In this mode, the page has the following characteristics:
     * 1、The latest messages are loaded first. The latest message is shown at the bottom of the list.
     * 2、The new receive message will be inserted into the list automatically and be shown at the bottom of the list.
     * 3、The list will scroll to the bottom automatically when the page is opened.
     * 4、The list will scroll to the bottom automatically when the new message is received.
     * 5、The list will scroll to the bottom automatically when the message is sent.
     */
    LOCAL,

    /**
     * Load messages from remote server.
     * It has some limitations. The messages that users can pull are limited by
     * the period of remote data storage enabled by the user.
     * In this mode, the page has the following characteristics:
     * 1、It only can fetch messages from chat server and cannot combine it with local messages.
     * 2、Other characteristics are the same as [LOCAL].
     */
    ROAM,

    /**
     * Load messages from local database with a start message id.
     * In this mode, the page has the following characteristics:
     * 1、The messages are loaded from the start message id.
     * 2、The layout will locate the start message id and scroll to the message automatically.
     * 3、You can load more old messages when the top of the list is reached.
     * 4、You can load more new messages when the bottom of the list is reached.
     * 5、If the view is not the bottom of the list, the list not scroll to the bottom automatically when the new message is received.
     */
    SEARCH,

    /**
     * In this mode, will show chat thread messages.
     * In this mode, the page has the following characteristics:
     * 1、The oldest messages are loaded first. The oldest message is shown at the top of the list.
     * 2、The data is load from chat server.
     * 3、If the view is not the bottom of the list, the list not scroll to the bottom automatically when the new message is received.
     */
    THREAD
}

internal fun EaseLoadDataType.isShouldStackFromEnd(): Boolean {
    return this != EaseLoadDataType.THREAD && this != EaseLoadDataType.SEARCH
}