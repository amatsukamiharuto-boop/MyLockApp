package com.mylockapp.domain.model

import org.json.JSONObject

data class UserProfile(
    val name: String,
    val id: String,
    val role: String,
    val position: String,
    val verified: Boolean
) {
    fun toJson(): String = JSONObject()
        .put("name", name)
        .put("id", id)
        .put("role", role)
        .put("position", position)
        .put("verified", verified)
        .toString()

    companion object {
        fun fromJson(s: String): UserProfile? = runCatching {
            val o = JSONObject(s)
            UserProfile(
                name = o.getString("name"),
                id = o.getString("id"),
                role = o.optString("role"),
                position = o.optString("position"),
                verified = o.optBoolean("verified", false)
            )
        }.getOrNull()
    }
}
