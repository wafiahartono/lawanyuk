package com.lawanyuk.home.report.create.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawanyuk.home.report.model.Report
import com.lawanyuk.repository.Repository
import com.lawanyuk.util.lifecycle.Event
import com.lawanyuk.util.logw
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val _message = MutableLiveData<Event<Message>>()
    val message: LiveData<Event<Message>> = _message

    fun createReport(report: Report, fileNameList: List<String>) = viewModelScope.launch {
        _message.value = Event(Message.LOADING)
        try {
            Repository.createReport(report, fileNameList)
            _message.value = Event(Message.SUCCESS)
        } catch (e: Exception) {
            logw("createReport failed", e)
            _message.value = Event(Message.FAILURE)
        }
    }

    enum class Message {
        FAILURE, LOADING, SUCCESS
    }
}
