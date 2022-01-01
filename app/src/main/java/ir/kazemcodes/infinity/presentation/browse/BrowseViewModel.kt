package ir.kazemcodes.infinity.presentation.browse

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.data.network.models.BooksPage
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.use_cases.datastore.DataStoreUseCase
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.util.Resource
import ir.kazemcodes.infinity.util.merge
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class BrowseViewModel(
    private val localUseCase: LocalUseCase,
    private val remoteUseCase: RemoteUseCase,
    private val dataStoreUseCase: DataStoreUseCase,
    private val source: Source,
    private val isLatestUpdateMode : Boolean = true
) : ScopedServices.Registered {

    private val _state = mutableStateOf<BrowseScreenState>(BrowseScreenState())

    val state: State<BrowseScreenState> = _state


    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onServiceRegistered() {
        _state.value = state.value.copy(isLatestUpdateMode = isLatestUpdateMode)
        readLayoutType()
        if (state.value.isLatestUpdateMode) {
            getLatestUpdateBooks(source = source)
        } else {
            getMostPopularBooks(source = source)
        }

    }

    fun getSource(): Source {

        return source
    }

    fun onEvent(event: BrowseScreenEvents) {
        when (event) {
            is BrowseScreenEvents.UpdatePage -> {
                updatePage(event.page)
            }
            is BrowseScreenEvents.UpdateLayoutType -> {
                updateLayoutType(event.layoutType)
            }
            is BrowseScreenEvents.ToggleMenuDropDown -> {
                toggleMenuDropDown(isShown = event.isShown)
            }
            is BrowseScreenEvents.GetBooks -> {
                getLatestUpdateBooks(event.source)
            }
            is BrowseScreenEvents.ToggleSearchMode -> {
                toggleSearchMode(event.inSearchMode)
            }
            is BrowseScreenEvents.UpdateSearchInput -> {
                updateSearchInput(event.query)
            }
            is BrowseScreenEvents.SearchBooks -> {
                searchBook(event.query)
            }
        }
    }

    private fun updatePage(page: Int) {
        if (!state.value.isSearchModeEnable) {
            _state.value = state.value.copy(page = page)
        } else {
            _state.value = state.value.copy(searchPage = page)
        }
    }

    private fun updateSearchInput(query: String) {
        _state.value = state.value.copy(searchQuery = query)
    }

    private fun toggleSearchMode(inSearchMode: Boolean? = null) {
        _state.value =
            state.value.copy(isSearchModeEnable = inSearchMode ?: !state.value.isSearchModeEnable)
        if (inSearchMode == false) {
            exitSearchedMode()
        }
    }

    private fun exitSearchedMode() {
        _state.value = state.value.copy(searchedBook = BooksPage(),
            searchQuery = "",
            page = 1,
            isLoading = false,
            error = "")
    }

    private fun updateLayoutType(layoutType: DisplayMode) {
        _state.value = state.value.copy(layout = layoutType.layout)
        coroutineScope.launch(Dispatchers.IO) {
            dataStoreUseCase.saveBrowseLayoutUseCase(layoutType.layoutIndex)
        }
    }

    private fun readLayoutType() {
        coroutineScope.launch {
            dataStoreUseCase.readBrowseLayoutUseCase().collectLatest { result ->
                if (result.data != null) {
                    _state.value = state.value.copy(layout = result.data.layout)
                }
            }
        }

    }

    private fun toggleMenuDropDown(isShown: Boolean) {
        _state.value = state.value.copy(isMenuDropDownShown = isShown)
    }


    private fun getMostPopularBooks(source: Source) {
        remoteUseCase.getRemoteMostPopularBooksUseCase(page = state.value.page, source = source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = state.value.copy(
                            books = merge(state.value.books, result.data ?: emptyList()),
                            isLoading = false,
                            error = ""
                        )
                        onEvent(BrowseScreenEvents.UpdatePage(state.value.page + 1))
                    }
                    is Resource.Error -> {
                        _state.value =
                            state.value.copy(error = result.message ?: "An Unknown Error Occurred",
                                isLoading = false)
                    }
                    is Resource.Loading -> {
                        _state.value = state.value.copy(isLoading = true, error = "")
                    }
                }
            }.launchIn(coroutineScope)
    }

    private fun searchBook(query: String) {
        remoteUseCase.getSearchedBooksUseCase(page = state.value.page, query, source = source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _state.value = state.value.copy(
                                searchedBook = result.data,
                                isLoading = false,
                                error = ""
                            )
                        }
                        onEvent(BrowseScreenEvents.UpdatePage(state.value.searchPage + 1))
                    }
                    is Resource.Error -> {
                        _state.value =
                            state.value.copy(error = result.message ?: "An Unknown Error Occurred",
                                isLoading = false)
                    }
                    is Resource.Loading -> {
                        _state.value = state.value.copy(isLoading = true, error = "")
                    }
                }
            }.launchIn(coroutineScope)
    }

    private fun getLatestUpdateBooks(source: Source) {
        remoteUseCase.getRemoteLatestUpdateLatestBooksUseCase(page = state.value.page, source = source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = state.value.copy(
                            books = merge(state.value.books, result.data ?: emptyList()),
                            isLoading = false,
                            error = ""
                        )
                        onEvent(BrowseScreenEvents.UpdatePage(state.value.page + 1))
                    }
                    is Resource.Error -> {
                        _state.value =
                            state.value.copy(error = result.message ?: "An Unknown Error Occurred",
                                isLoading = false)
                    }
                    is Resource.Loading -> {
                        _state.value = state.value.copy(isLoading = true, error = "")
                    }
                }
            }.launchIn(coroutineScope)
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }

}