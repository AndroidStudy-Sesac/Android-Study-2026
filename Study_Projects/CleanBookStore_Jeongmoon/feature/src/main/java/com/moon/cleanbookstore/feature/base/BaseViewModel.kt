package com.moon.cleanbookstore.feature.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : State, E : Event> : ViewModel() {

    abstract fun getInitialState(): S

    private val _stateFlow = MutableStateFlow(getInitialState())
    val stateFlow: StateFlow<S> = _stateFlow

    private val _eventFlow = MutableSharedFlow<E>()
    val eventFlow: SharedFlow<E> = _eventFlow

    protected val jobs = mutableListOf<Job>()

    open fun fetchData(): Job = viewModelScope.launch { }

    override fun onCleared() {
        jobs.forEach { it.cancel() }
        super.onCleared()
    }

    protected fun setState(state: S) {
        _stateFlow.value = state
    }

    protected inline fun <reified T : S> withState(action: (T) -> Unit) {
        val currentState = stateFlow.value
        if (currentState is T) {
            action(currentState)
        }
    }

    protected fun sendEvent(event: E) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }
}