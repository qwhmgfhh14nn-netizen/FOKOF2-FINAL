import sys, os, urllib.request
R='app/src/main/java/com/example/'
def sub(path, old, new):
    s=open(R+path).read()
    if old not in s: sys.exit('NO MATCH '+path+' :: '+old[:70])
    open(R+path,'w').write(s.replace(old,new,1))

# 1. remote auth client from the user's own old branch
url='https://raw.githubusercontent.com/qwhmgfhh14nn-netizen/FOKOF2/fokof2-auth-integration/app/src/main/java/com/example/data/remote/Fokof2AuthApi.kt'
src=urllib.request.urlopen(url,timeout=30).read().decode('utf-8')
if not src.lstrip().startswith('package com.example.data.remote'): sys.exit('bad api file')
src=src.replace('KeyGenParameterSpec.PURPOSE_ENCRYPT','KeyProperties.PURPOSE_ENCRYPT').replace('KeyGenParameterSpec.PURPOSE_DECRYPT','KeyProperties.PURPOSE_DECRYPT')
os.makedirs(R+'data/remote',exist_ok=True)
open(R+'data/remote/Fokof2AuthApi.kt','w').write(src)

# 2. dependency
s=open('app/build.gradle.kts').read()
a='  implementation(libs.coil.compose)\n'
if a not in s: sys.exit('NO MATCH gradle')
open('app/build.gradle.kts','w').write(s.replace(a,a+'  implementation(libs.okhttp)\n',1))

V='ui/BarayaViewModel.kt'
sub(V,'import com.example.data.ai.GeminiAiService\n','import com.example.data.ai.GeminiAiService\nimport com.example.data.remote.Fokof2AuthApi\n')
sub(V,'    private val aiService = GeminiAiService()\n','    private val aiService = GeminiAiService()\n    private val authApi = Fokof2AuthApi(application)\n')

# login: local first (works offline), then the online account on the server
sub(V,'''            val result = repository.loginUser(email, password)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                showToast(if (_currentLanguage.value == AppLanguage.ARABIC) "مرحباً بك مجدداً ${user.name}!" else "Welcome back, ${user.name}!")
                onComplete(true, "")
            } else {
                val err = result.exceptionOrNull()?.message ?: "فشل تسجيل الدخول"
                onComplete(false, err)
            }''','''            var result = repository.loginUser(email, password)
            var remoteError: String? = null
            if (result.isFailure) {
                // Not found / wrong password on this device: try the online account (cloud login).
                try {
                    val remote = authApi.login(email, password)
                    val mirrored = repository.registerUser(remote.name, remote.email, password, remote.phone)
                    result = if (mirrored.isSuccess) mirrored else repository.loginUser(email, password)
                } catch (e: java.io.IOException) {
                    remoteError = if (_currentLanguage.value == AppLanguage.ARABIC) "تعذر الاتصال بالخادم. تحقق من الإنترنت." else "Could not reach the server. Check your internet."
                } catch (e: Exception) {
                    remoteError = e.message
                }
            }
            if (result.isSuccess) {
                val user = result.getOrThrow()
                showToast(if (_currentLanguage.value == AppLanguage.ARABIC) "مرحباً بك مجدداً ${user.name}!" else "Welcome back, ${user.name}!")
                onComplete(true, "")
            } else {
                val err = remoteError ?: result.exceptionOrNull()?.message ?: "فشل تسجيل الدخول"
                onComplete(false, err)
            }''')

# register: create the online account first when the server is reachable, then the local copy
sub(V,'''            val result = repository.registerUser(name, email, password, phone, bio)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                showToast(if (_currentLanguage.value == AppLanguage.ARABIC) "تم إنشاء الحساب بنجاح!''','''            var remoteFailure: String? = null
            try {
                authApi.register(name, email, password, phone)
            } catch (e: java.io.IOException) {
                // offline: keep going with a device-only account
            } catch (e: Exception) {
                remoteFailure = e.message ?: "تعذر إنشاء الحساب على الخادم"
            }
            if (remoteFailure != null) {
                onComplete(false, remoteFailure)
                return@launch
            }
            val result = repository.registerUser(name, email, password, phone, bio)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                showToast(if (_currentLanguage.value == AppLanguage.ARABIC) "تم إنشاء الحساب بنجاح!''')

# logout also closes the online session
sub(V,'''    fun logout() {
        _isOwnerAuthenticated.value = false
        repository.lockOwnerControl()
        repository.logout()''','''    fun logout() {
        _isOwnerAuthenticated.value = false
        repository.lockOwnerControl()
        viewModelScope.launch { try { authApi.logout() } catch (_: Exception) { } }
        repository.logout()''')
