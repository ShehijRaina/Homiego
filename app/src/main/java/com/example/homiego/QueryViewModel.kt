package com.example.homiego

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class QueryViewModel : ViewModel() {

    val capt: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val generativeModel : GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-pro-002",
        apiKey = "YOUR_API_KEY"
    )

    fun sendMessage(question : String) {
        viewModelScope.launch {

            try
            {
                val chat = generativeModel.startChat()

                val response = chat.sendMessage(question)
                MainActivity.Companion.speak(response.text.toString())
                capt.setValue(response.text.toString())

            }
            catch (e : Exception)
            {
                capt.setValue("Error : " + e.message.toString())
            }

        }
    }
}



