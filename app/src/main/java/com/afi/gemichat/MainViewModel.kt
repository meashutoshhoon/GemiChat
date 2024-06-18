package com.afi.gemichat

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // Enter your own API key here
    private val apiKey = "YOUR API KEY"

    private val geminiProModel by lazy {
        GenerativeModel(
            modelName = "gemini-pro", apiKey = apiKey
        ).apply {
            startChat()
        }
    }

    private val geminiProVisionModel by lazy {
        GenerativeModel(
            modelName = "gemini-pro-vision", apiKey = apiKey
        ).apply {
            startChat()
        }
    }

    val isGenerating = mutableStateOf(false)
    val conversations = mutableStateListOf<Triple<String, String, List<Bitmap>?>>()


    fun sendText(textPrompt: String, images: SnapshotStateList<Bitmap>) {

        isGenerating.value = true

        conversations.add(Triple("sent", textPrompt, images.toList()))
        conversations.add(Triple("received", "", null))

        val generativeModel = if (images.isNotEmpty()) geminiProVisionModel else geminiProModel

        val inputContent = content {
            images.forEach { imageBitmap ->
                image(imageBitmap)
            }
            text(textPrompt)
        }
        viewModelScope.launch {
            generativeModel.generateContentStream(inputContent).collect { chunk ->
                conversations[conversations.lastIndex] = Triple(
                    "received", conversations.last().second + chunk.text, null
                )
            }
            isGenerating.value = false
        }
    }

}