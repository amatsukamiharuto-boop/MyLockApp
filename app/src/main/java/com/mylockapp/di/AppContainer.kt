package com.mylockapp.di

import android.content.Context
import com.mylockapp.audio.AlarmController
import com.mylockapp.audio.SfxPlayer
import com.mylockapp.data.ProfileRepositoryImpl
import com.mylockapp.data.SecureStore
import com.mylockapp.domain.repository.ProfileRepository
import com.mylockapp.domain.usecase.MatchProfileUseCase
import com.mylockapp.domain.usecase.VerifyPinUseCase
import com.mylockapp.security.DefaultSecurityManager
import com.mylockapp.security.SecurityManager
import com.mylockapp.service.UnlockSessions

/** Manual DI. Swap for Hilt/Koin later without touching UI code. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val profileRepository: ProfileRepository = ProfileRepositoryImpl(SecureStore(appContext))
    val securityManager: SecurityManager = DefaultSecurityManager(appContext) // v2: RaspSecurityManager
    val sfx = SfxPlayer(appContext)
    val alarm = AlarmController(appContext)
    val sessions = UnlockSessions()

    val verifyPin = VerifyPinUseCase(profileRepository)
    val matchProfile = MatchProfileUseCase(profileRepository)
}
