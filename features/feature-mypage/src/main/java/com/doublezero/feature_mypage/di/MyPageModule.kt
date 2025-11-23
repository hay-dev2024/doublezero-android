package com.doublezero.feature_mypage.di

import android.content.Context
import com.doublezero.feature_mypage.BuildConfig // 생성된 BuildConfig import
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class) // ViewModel의 수명주기와 맞춤
object MyPageModule {

    @Provides
    @ActivityRetainedScoped
    fun provideGoogleSignInOptions(): GoogleSignInOptions {
        // BuildConfig에서 안전하게 키를 가져옵니다.
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }

    @Provides
    @ActivityRetainedScoped
    fun provideGoogleSignInClient(
        @ApplicationContext context: Context,
        options: GoogleSignInOptions
    ): GoogleSignInClient {
        return GoogleSignIn.getClient(context, options)
    }
}