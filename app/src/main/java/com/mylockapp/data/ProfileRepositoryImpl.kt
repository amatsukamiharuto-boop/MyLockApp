package com.mylockapp.data

import com.mylockapp.domain.model.FaceSignature
import com.mylockapp.domain.model.UserProfile
import com.mylockapp.domain.repository.ProfileRepository

class ProfileRepositoryImpl(private val store: SecureStore) : ProfileRepository {
    private companion object {
        const val PIN_HASH = "pin_hash"
        const val PIN_SALT = "pin_salt"
        const val FACE = "face_signature"
        const val LOCKED = "locked_packages"
        const val PROFILE = "user_profile"
        const val PHOTO = "face_photo"
    }

    @Volatile private var lockedCache: Set<String>? = null

    override fun hasPin() = store.getString(PIN_HASH) != null

    override fun savePin(pin: String) {
        val salt = PinHasher.newSalt()
        store.putString(PIN_SALT, PinHasher.encode(salt))
        store.putString(PIN_HASH, PinHasher.encode(PinHasher.hash(pin, salt)))
    }

    override fun verifyPin(pin: String): Boolean {
        val salt = store.getString(PIN_SALT)?.let(PinHasher::decode) ?: return false
        val stored = store.getString(PIN_HASH)?.let(PinHasher::decode) ?: return false
        return PinHasher.constantTimeEquals(stored, PinHasher.hash(pin, salt))
    }

    override fun saveFace(signature: FaceSignature) = store.putString(FACE, signature.serialize())
    override fun loadFace(): FaceSignature? = store.getString(FACE)?.let(FaceSignature::parse)

    override fun lockedPackages(): Set<String> =
        lockedCache ?: store.getStringSet(LOCKED).also { lockedCache = it }

    override fun setLockedPackages(packages: Set<String>) {
        lockedCache = packages
        store.putStringSet(LOCKED, packages)
    }

    override fun saveUserProfile(profile: UserProfile) = store.putString(PROFILE, profile.toJson())
    override fun loadUserProfile(): UserProfile? = store.getString(PROFILE)?.let(UserProfile::fromJson)

    override fun saveFacePhoto(jpeg: ByteArray) = store.putString(PHOTO, PinHasher.encode(jpeg))
    override fun loadFacePhoto(): ByteArray? = store.getString(PHOTO)?.let(PinHasher::decode)
}
