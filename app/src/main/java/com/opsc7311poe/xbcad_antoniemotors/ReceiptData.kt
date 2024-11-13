package com.opsc7311poe.xbcad_antoniemotors
data class ReceiptData(
    val id: String? = null,  // Optional ID field
    val customerName: String? = null,
    val serviceType: String? = null,
    val parts: List<Map<String, Any>> = listOf(),
    val labourCost: String? = null,
    val totalCost: String? = null,
    val dateCreated: String? = null
) {
    constructor() : this(null, null, null, listOf(), null, null, null)
}
