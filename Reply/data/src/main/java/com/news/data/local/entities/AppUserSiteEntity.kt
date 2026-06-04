package com.news.data.local.entities

import androidx.room.Entity
import com.news.domain.models.AppUserSite

@Entity(tableName = "app_user_sites", primaryKeys = ["Id", "GROUP", "Kind"])
data class AppUserSiteEntity(
    val Id: Int,
    val Key: String,
    val Name: String,
    val TextColor: String?,
    val BackgroundColor: String?,
    val Url: String,
    val IsActive: Boolean,
    val Kind: String,
    val GROUP: String,
    val Stt: Int,
    val LimitEdit: Boolean,
    val AllowEdit: Boolean,
    val OtherCanSee: Boolean,
    val IdentityId: String,
    val AppIdEncrypt: String
)

fun AppUserSiteEntity.toDomain() = AppUserSite(
    Id = Id,
    Key = Key,
    Name = Name,
    TextColor = TextColor,
    BackgroundColor = BackgroundColor,
    Url = Url,
    IsActive = IsActive,
    Kind = Kind,
    GROUP = GROUP,
    Stt = Stt,
    LimitEdit = LimitEdit,
    AllowEdit = AllowEdit,
    OtherCanSee = OtherCanSee,
    IdentityId = IdentityId,
    AppIdEncrypt = AppIdEncrypt
)

fun AppUserSite.toEntity() = AppUserSiteEntity(
    Id = Id,
    Key = Key,
    Name = Name,
    TextColor = TextColor,
    BackgroundColor = BackgroundColor,
    Url = Url,
    IsActive = IsActive,
    Kind = Kind,
    GROUP = GROUP,
    Stt = Stt,
    LimitEdit = LimitEdit,
    AllowEdit = AllowEdit,
    OtherCanSee = OtherCanSee,
    IdentityId = IdentityId,
    AppIdEncrypt = AppIdEncrypt
)
