package com.news.domain.models

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Serializable
data class News(
    val Id : Int,
    val Title : String,
    val Image : String? = "",
    val Slug: String? = "",
    val ShortDes : String? = "",
    val Date : String? = "",
    val Source : String = "",
    val Link:String? = "",
    val Is_Video: Boolean,
    val Is_Album: Boolean,
    val Html: String? = "",
    val SubCat:String? = "",
    val App_Category_Name:String? = "",
    val App_Category_Icon:String? = "",
    val App_Category_Slug:String? = "",
    val App_Category_TextColor:String? = "",
    val App_Category_BackgroundColor:String? = "",

    val App_Site_Slug:String? = "",
    val App_Site_Name:String? = "",
    val Icon:String? = "",
    val App_Site_TextColor:String? = "",
    val App_Site_BackgroundColor:String? = "",
    val TextColor:String? = "",
    val BackgroundColor:String? = "",
    val BackgroundColor2:String? = "",
    val Kind:String?,
    val NewsItemChilds:ArrayList<NewsChildItem>,
    ) : Parcelable

@Parcelize
@Serializable
data class NewsChildItem(
    val NewsId : Int,
    val Seq : Int,
    val MediaUrl : String,
    val Content : String,
    val TextColor:String?,
    val BackgroundColor:String?,
    val BackgroundColor2:String?,
    val Kind:String?,

    val Title : String?,
    //val Published : Date?,
    val LinkSource:String?,
    val SubTitle: String?
) : Parcelable

