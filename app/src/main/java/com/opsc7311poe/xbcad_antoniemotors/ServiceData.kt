package com.opsc7311poe.xbcad_antoniemotors

data class ServiceData(

    var name: String?,
    var custID: String?,
    var status: String?,
    var dateReceived: String?,
    var dateReturned: String?,
    var parts: List<Part>? ,
    var labourCost: Double?
){
    // No-argument constructor (required by Firebase)
    constructor() : this(null, null , null, null, null, null, null)
}

data class Part(
    var name: String?,
    var cost: Double?
)

