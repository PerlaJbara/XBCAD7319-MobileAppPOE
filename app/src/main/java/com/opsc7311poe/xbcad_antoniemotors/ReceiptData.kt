package com.opsc7311poe.xbcad_antoniemotors

data class ReceiptData (
    val customerName: String?,
    val serviceTypeName: String?,
    val finalQuote: String?
)
{
    constructor() : this(null, null, null)
}