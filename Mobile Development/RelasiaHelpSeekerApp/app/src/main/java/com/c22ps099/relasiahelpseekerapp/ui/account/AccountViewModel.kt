package com.c22ps099.relasiahelpseekerapp.ui.account

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.c22ps099.relasiahelpseekerapp.data.api.ApiConfig
import com.c22ps099.relasiahelpseekerapp.data.api.responses.AddNewHelpSeekerResponse

import com.c22ps099.relasiahelpseekerapp.data.api.responses.MissionItem
import com.c22ps099.relasiahelpseekerapp.data.api.responses.MissionsResponse
import com.c22ps099.relasiahelpseekerapp.misc.Event
import com.c22ps099.relasiahelpseekerapp.model.Helpseeker
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AccountViewModel(private val token: String) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error


    fun addNewHelpSeeker(helpSeeker: Helpseeker) {
        _isLoading.value = true

        ApiConfig.getApiService().addHelpseeker(helpSeeker)
            .enqueue(object : Callback<AddNewHelpSeekerResponse> {
                override fun onResponse(
                    call: Call<AddNewHelpSeekerResponse>,
                    response: Response<AddNewHelpSeekerResponse>
                ) {
                    _isLoading.value = false

                   if (response.isSuccessful) {
                        Log.v("ini adalah mission:", "Successs")
                    } else {
                        val errorMessage = Gson().fromJson(
                            response.errorBody()?.charStream(),
                            AddNewHelpSeekerResponse::class.java
                        )
                        _error.value = Event(errorMessage.message!!)
                        Log.e("err", "${_error.value}")
                    }
                }

                override fun onFailure(call: Call<AddNewHelpSeekerResponse>, t: Throwable) {
                    _isLoading.value = false
                    _error.value = Event(t.message.toString())
                    Log.e("err", "$t")
                }
            })
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val token: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountViewModel(token) as T
        }
    }
}