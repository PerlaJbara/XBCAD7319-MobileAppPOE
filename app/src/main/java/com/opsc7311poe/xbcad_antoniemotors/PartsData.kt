package com.opsc7311poe.xbcad_antoniemotors

data class PartsData(
    val id: String? = null,
    val partName: String? = null,
    val partDescription: String? = null,
    val stockCount: Int = 0,
    val minStock: Int = 0,
    val costPrice: Double? = null
) {
    constructor() : this(null,null, null, 0, 0, null)
}