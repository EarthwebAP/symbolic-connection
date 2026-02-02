package com.glyphos.symbolic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glyphos.symbolic.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EphemeralTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    onTypingChange: (Boolean) -> Unit,
    placeholder: String = "Type message...",
    modifier: Modifier = Modifier
) {
    val ledonovaFont = FontFamily(Font(R.font.ledonova_glyph_regular))
    val scope = rememberCoroutineScope()

    // Track word indices in glyph mode
    val glyphModeWords = remember { mutableStateMapOf<Int, Boolean>() }

    // Timer jobs for each word
    val wordTimerJobs = remember { mutableStateMapOf<Int, Job>() }

    // Long-press reveal state
    var revealedWord by remember { mutableStateOf<String?>(null) }
    var revealWordIndex by remember { mutableIntStateOf(-1) }

    val words = value.split(Regex("\\s+")).filter { it.isNotEmpty() }

    // Build annotated string with mixed fonts
    val displayText = buildAnnotatedString {
        words.forEachIndexed { wordIndex, word ->
            val isGlyph = glyphModeWords[wordIndex] ?: false

            if (isGlyph) {
                withStyle(style = SpanStyle(fontFamily = ledonovaFont)) {
                    append(word)
                }
            } else {
                append(word)
            }

            if (wordIndex < words.size - 1) {
                append(" ")
            }
        }
    }

    // Handle typing and timers
    LaunchedEffect(value) {
        if (value.isNotEmpty()) {
            onTypingChange(true)

            // Cancel any active timers
            wordTimerJobs.forEach { (_, job) -> job.cancel() }
            wordTimerJobs.clear()

            // For each word, start a new timer
            words.indices.forEach { wordIndex ->
                // Reset glyph mode for this word
                glyphModeWords[wordIndex] = false

                // Start timer
                val job = scope.launch {
                    delay(1500) // 1.5 second inactivity threshold
                    glyphModeWords[wordIndex] = true
                }
                wordTimerJobs[wordIndex] = job
            }
        } else {
            onTypingChange(false)
            wordTimerJobs.forEach { (_, job) -> job.cancel() }
            wordTimerJobs.clear()
            glyphModeWords.clear()
            revealedWord = null
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF008B8B)) },
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { offset ->
                            // Find word at tap position
                            val tapX = offset.x.toInt()
                            var wordIndex = -1
                            var currentX = 0

                            words.forEachIndexed { index, word ->
                                val wordWidth = word.length * 8
                                if (tapX in currentX until (currentX + wordWidth)) {
                                    wordIndex = index
                                }
                                currentX += wordWidth + 6 // Space for word and gap
                            }

                            if (wordIndex >= 0) {
                                revealedWord = words[wordIndex]
                                revealWordIndex = wordIndex
                            }
                        }
                    )
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Cyan,
                unfocusedTextColor = Color.Cyan,
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color(0xFF008B8B),
                cursorColor = Color.Cyan
            ),
            singleLine = true
        )

        // Reveal bubble
        if (revealedWord != null) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 60.dp, start = (revealWordIndex * 40).dp)
                    .align(Alignment.TopStart)
                    .background(
                        color = Color.Cyan.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = revealedWord ?: "",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Default
                )
            }
        }
    }

    // Auto-dismiss reveal bubble
    LaunchedEffect(revealedWord) {
        if (revealedWord != null) {
            delay(3000)
            revealedWord = null
        }
    }
}
