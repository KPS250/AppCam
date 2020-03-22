package io.kps.appcam.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Img(
    val image: String ,
    val name: String ,
    val timestamp: Date? ,
    val latitude: String ,
    val longitude: String,
    val size: Long
): Parcelable