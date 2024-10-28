package com.opsc7311poe.xbcad_antoniemotors

data class ChatItem(
    val message: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
