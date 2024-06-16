package com.bangkit.recyclopedia.view.login

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bangkit.recyclopedia.api.ApiConfig.getApiService
import com.bangkit.recyclopedia.api.response.LoginAppResponse
import com.bangkit.recyclopedia.data.model.UserLoginModel
import com.bangkit.recyclopedia.data.model.UserModel
import com.bangkit.recyclopedia.data.pref.UserPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val preference: UserPreference)  : ViewModel() {

    private val _finishingActivity = MutableLiveData<Boolean>()
    val finishActivity: LiveData<Boolean> = _finishingActivity

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading
    fun loginUser(user: UserLoginModel, context: Context) {
        _isLoading.value = true
        val client = getApiService().loginUser(user)
        client.enqueue(object : Callback<LoginAppResponse> {

            override fun onResponse(call: Call<LoginAppResponse>, response: Response<LoginAppResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        responseBody.loginResult?.let { login(it.token) }
                        showAlertDialog("Login Berhasil!", "Kamu bisa login sekarang dan menikmati aplikasi story bersama orang lain!", "Ayo!", context)

                    }
                } else {
                    var posButtonClicked = false
                    AlertDialog.Builder(context).apply {
                        setTitle("Login Gagal")
                        setMessage(response.message())
                        setPositiveButton("Oke") { _, _ ->
                            if (posButtonClicked){
                                _finishingActivity.value = true
                            }
                        }
                        create()
                        show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginAppResponse>, t: Throwable) {
                _isLoading.value = false
                showAlertDialog("Login Gagal", t.message, "Oke", context)
            }
        })
    }

    private fun showAlertDialog(title: String, message: String?, posButton: String, context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(posButton) { _, _ ->
                _finishingActivity.value = true
            }
            setCancelable(false)
            create()
            show()
        }

    }

    fun getUser(): LiveData<UserModel> {
        return preference.getUser().asLiveData()
    }

    fun login(token: String) {
        viewModelScope.launch {
            preference.login(token)
        }
    }
}