package com.c22ps099.relasiahelperapp.ui.missions

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.c22ps099.relasiahelperapp.data.MissionRepository
import com.c22ps099.relasiahelperapp.network.responses.MissionDataItem

class MissionsViewModel(pref: MissionRepository, volunteerId: String) :
    ViewModel() {

    val missionsStatus: LiveData<PagingData<MissionDataItem>> =
        pref.getMissionsStatusPages(volunteerId).cachedIn(viewModelScope)

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val pref: MissionRepository,
        private val volunteerId: String
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MissionsViewModel(pref, volunteerId) as T
        }
    }
}