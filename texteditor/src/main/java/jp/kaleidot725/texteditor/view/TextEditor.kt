package jp.kaleidot725.texteditor.view

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import jp.kaleidot725.texteditor.state.EditableTextEditorState
import jp.kaleidot725.texteditor.state.TextEditorState

@Composable
fun TextEditor(
    textEditorState: TextEditorState,
    onUpdatedState: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(
            items = textEditorState.toEditable().fields,
            key = { _, item -> item.id }
        ) { index, textFieldState ->
            // workaround: prevent to hide ime when editor delete newline
            val focusManager = LocalFocusManager.current
            val focusRequester by remember { mutableStateOf(FocusRequester()) }

            LaunchedEffect(textFieldState.isSelected) {
                if (textFieldState.isSelected) focusRequester.requestFocus()
            }

            TextField(
                textFieldValue = textFieldState.value,
                onUpdateText = { newText ->
                    textEditorState.toEditable()
                        .updateField(targetIndex = index, textFieldValue = newText)
                    onUpdatedState()
                },
                onAddNewLine = { newText ->
                    textEditorState.toEditable()
                        .splitField(targetIndex = index, textFieldValue = newText)
                    onUpdatedState()
                },
                onDeleteNewLine = {
                    textEditorState.toEditable().deleteField(targetIndex = index)
                    onUpdatedState()

                    // workaround: prevent to hide ime when editor delete newline
                    focusManager.moveFocus(FocusDirection.Up)
                },
                focusRequester = focusRequester,
                onFocus = {
                    if (textEditorState.selectedIndices.contains(index)) return@TextField
                    textEditorState.toEditable().selectField(targetIndex = index)
                    onUpdatedState()
                }
            )
        }
    }
}

internal fun TextEditorState.toEditable(): EditableTextEditorState {
    return this as EditableTextEditorState
}