package com.opsc7311poe.xbcad_antoniemotors

data class VehicleData(
var VehicleOwner: String = "",
var vehicleId: String = "",
var customerID: String = "",
var VehicleNumPlate: String = "",
var VehicleModel: String = "",
var VinNumber: String = "",
var VehicleKms: String = "",
var images: Map<String, String> = emptyMap()

)
