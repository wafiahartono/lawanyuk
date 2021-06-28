package com.lawanyuk.home.report.list.fragment

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

    private val _reportList = MutableLiveData<List<Report>>()
    val reportList: LiveData<List<Report>> = _reportList

    fun refreshReportList() = viewModelScope.launch {
        _message.value = Event(Message.LOADING)
        try {
            val reportList = Repository.getReportList()
            _reportList.value = reportList
            _message.value = Event(
                if (reportList.isEmpty()) Message.EMPTY
                else Message.SUCCESS
            )
        } catch (e: Exception) {
            logw("refreshReportList failed", e)
            _message.value = Event(Message.FAILURE)
        }
    }

    fun urlListToStorageReferenceList(urlList: List<String>) = urlList.map {
        Repository.getStorageReference().child(it)
    }

    fun isUserAnonymous(): Boolean {
        return Repository.getAuthState() == Repository.AuthState.ANONYMOUS
    }

    enum class Message {
        EMPTY, FAILURE, LOADING, SUCCESS
    }
}
