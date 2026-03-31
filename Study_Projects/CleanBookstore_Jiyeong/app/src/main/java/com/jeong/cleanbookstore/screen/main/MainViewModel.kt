package com.jeong.cleanbookstore.screen.main

import androidx.lifecycle.ViewModel
import com.jeong.cleanbookstore.navigation.BottomNavItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor() : ViewModel() {
        private val _currentTabIndex = MutableStateFlow(0)
        val currentTabIndex: StateFlow<Int> = _currentTabIndex.asStateFlow()

        val bottomNavItems =
            listOf(
                BottomNavItem.New,
                BottomNavItem.Search,
                BottomNavItem.Bookmark,
            )

        fun onTabSelected(index: Int) {
            _currentTabIndex.value = index
        }
    }
