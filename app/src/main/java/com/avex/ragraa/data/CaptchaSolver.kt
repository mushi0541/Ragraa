package com.avex.ragraa.data

import com.avex.ragraa.sharedPreferences
import com.twocaptcha.TwoCaptcha
import com.avex.ragraa.network.RagraaApi
import com.twocaptcha.captcha.Turnstile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object CaptchaSolver {
    val solver = TwoCaptcha("")
    var key = ""

    init {
        setCKey(Datasource.captchaKey)
    }

    fun setCKey(key: String) {
        solver.setApiKey(key)
        this.key = key
        sharedPreferences.edit().putString("captchaKey", key).apply()
    }

    suspend fun solveCaptcha(): Result<String> {
        if (key.isEmpty())
            return Result.failure(Exception("Key is empty"))

        return withContext(Dispatchers.IO) {
            val captcha = Turnstile().apply {
                setSiteKey(RagraaApi.TURNSTILE_SITE_KEY)
                setUrl("https://flexstudent.nu.edu.pk/Login")
            }

            try {
                // This blocking call now happens off the Main thread
                solver.solve(captcha)
                Result.success(captcha.getCode())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}