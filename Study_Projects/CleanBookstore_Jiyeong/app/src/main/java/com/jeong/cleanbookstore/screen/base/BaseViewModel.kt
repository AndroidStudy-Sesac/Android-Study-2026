package com.jeong.cleanbookstore.screen.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Base view model
 *
 * @param S: 하나의 상태 타입
 * @param E: 하나의 이벤트 타입
 *
 * fetchData(): 화면 진입 시 데이터를 가져오는 공통 진입점
 * getInitialState(): 처음에 어떤 상태로 시작하는지를 명시하도록 강제
 * MutableStateFlow: UI는 stateFlow를 구독하고, viewModel은 내부에서 _stateFlow를 바꿈
 * MutableSharedFlow: 일회성 이벤트(Snackbar 표시 / 화면 이동 / Toast 메시지 / 특정 사용자 액션 전달)
 * setState: 새 상태 객체를 만들어 교체
 * withState: 현재 상태를 안전하게 꺼내서 다루기 위한 헬퍼.
 * 그러나 BaseViewModel 자체가 이미 S : State를 알고 있기 때문에, 다시 reified S를 쓰는 구조는 과할 수 있음
 * sendEvent: 일회성 이벤트를 전달하는 메서드
 */
abstract class BaseViewModel<S : State, E : Event> : ViewModel() {
    open fun fetchData(): Job = viewModelScope.launch { }

    abstract fun getInitialState(): S

    private val _stateFlow = MutableStateFlow(getInitialState())
    val stateFlow: StateFlow<S> = _stateFlow

    private val _eventFlow = MutableSharedFlow<E>()
    val eventFlow: SharedFlow<E> = _eventFlow

    fun setState(state: S) {
        _stateFlow.value = state
    }

    inline fun <reified T : State> withState(withState: (T) -> Unit) {
        val currentState = stateFlow.value
        if (currentState is T) {
            withState(currentState)
        }
    }

    fun sendEvent(event: E) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }
}
