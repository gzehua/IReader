package ireader.presentation.ui.core.viewmodel

//actual abstract class BaseViewModel : ScreenModel {
//
//    actual val scope = createICoroutineScope()
//
//    private val activeScope = MutableStateFlow<CoroutineScope?>(null)
//
//    actual open fun onDestroy() {
//    }
//
//    actual fun <T> Preference<T>.asState() = PreferenceMutableState(this, scope)
//
//    actual fun <T> Preference<T>.asState(onChange: (T) -> Unit): PreferenceMutableState<T> {
//        this.changes()
//            .onEach { onChange(it) }
//            .launchIn(scope)
//        return PreferenceMutableState(this, scope)
//    }
//
//    actual fun <T> StateFlow<T>.asState(): State<T> {
//        val state = mutableStateOf(value)
//        scope.launch {
//            collect { state.value = it }
//        }
//        return state
//    }
//
//    actual fun <T> Flow<T>.launchWhileActive(): Job {
//        return activeScope
//            .filterNotNull()
//            .onEach { launchIn(it) }
//            .launchIn(scope)
//    }
//
//    actual fun <T> Flow<T>.asState(
//        initialValue: T,
//        onChange: (T) -> Unit
//    ): State<T> {
//        val state = mutableStateOf(initialValue)
//        scope.launch {
//            collect { state.value = it }
//        }
//        return state
//    }
//
//    private val _eventFlow = MutableSharedFlow<UiEvent>()
//    actual open val eventFlow = _eventFlow.asSharedFlow()
//    actual fun showSnackBar(message: UiText?) {
//    }
//
//}
