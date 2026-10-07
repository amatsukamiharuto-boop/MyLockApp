package com.mylockapp.domain.repository

import com.mylockapp.domain.model.FaceSignature
import com.mylockapp.domain.model.UserProfile

interface ProfileRepository {
    fun hasPin(): Boolean
    fun savePin(pin: String)
    fun verifyPin(pin: String): Boolean
    fun saveFace(signature: FaceSignature)
    fun loadFace(): FaceSignature?
    fun lockedPackages(): Set<String>
    fun setLockedPackages(packages: Set<String>)

    fun saveUserProfile(profile: UserProfile)
    fun loadUserProfile(): UserProfile?
    fun saveFacePhoto(jpeg: ByteArray)
    fun loadFacePhoto(): ByteArray?
}
