package com.github.lkaleda.textfieldflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.lkaleda.textfieldflow.ui.theme.TextFieldFlowTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext

class MainActivity : ComponentActivity() {
    private val viewModel: TextFieldViewModel by viewModels {
        TextFieldViewModelFactory(AndroidUiDispatcher.Main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextFieldFlowTheme {
                val state by viewModel.viewState.collectAsState()

                Screen(
                    state = state,
                    onInput = { viewModel.onUserInput(it) }
                )
            }
        }
    }
}

@Composable
fun Screen(state: ViewState, onInput: (UserInput) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(1f)
            .padding(16.dp),
        maxLines = 1,
        value = state.username,
        onValueChange = { onInput(UserInput.UsernameChanged(it)) }
    )
}

class TextFieldViewModel(
    coroutineContext: CoroutineContext
) : ViewModel() {
    private val username = MutableStateFlow("")

    val viewState = MutableStateFlow(ViewState())

    init {
        (viewModelScope + coroutineContext).launch {
            username.collect {
                viewState.value = ViewState(it)
            }
        }
    }

    fun onUserInput(input: UserInput) {
        when (input) {
            is UserInput.UsernameChanged -> {
                username.value = input.text
            }
        }
    }
}

data class ViewState(
    val username: String = ""
)

sealed class UserInput {
    class UsernameChanged(val text: String) : UserInput()
}

class TextFieldViewModelFactory(
    private val coroutineContext: CoroutineContext
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TextFieldViewModel(coroutineContext) as T
    }
}