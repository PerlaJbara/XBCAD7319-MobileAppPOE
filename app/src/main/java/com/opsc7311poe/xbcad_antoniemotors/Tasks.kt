package com.opsc7311poe.xbcad_antoniemotors

import android.os.Parcel
import android.os.Parcelable

data class Tasks(
    val taskID: String?,
    val taskDescription: String?,
    val vehicleNumberPlate: String?,
    val creationDate: Long?,      // New property for task creation date
    val completedDate: Long?      // New property for task completion date
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),           // Read creation date from parcel
        parcel.readLong()            // Read completed date from parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(taskID)
        parcel.writeString(taskDescription)
        parcel.writeString(vehicleNumberPlate)
        parcel.writeLong(creationDate ?: -1)  // Write creation date to parcel
        parcel.writeLong(completedDate ?: -1) // Write completed date to parcel
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Tasks> {
        override fun createFromParcel(parcel: Parcel): Tasks {
            return Tasks(parcel)
        }

        override fun newArray(size: Int): Array<Tasks?> {
            return arrayOfNulls(size)
        }
    }
}
