@file:Suppress("unused")

package com.lawanyuk.util

import android.util.Log
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import coil.api.clear
import coil.api.load
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.lawanyuk.R
import com.lawanyuk.databinding.LayoutStateBinding
import kotlin.random.Random

private const val LOG_TAG = "log_tag"

fun Any.logd(message: String) {
    Log.d(LOG_TAG, "${this::class.simpleName} - $message")
}

fun Any.loge(message: String, throwable: Throwable? = null) {
    Log.e(LOG_TAG, "${this::class.simpleName} - $message", throwable)
}

fun Any.logi(message: String) {
    Log.i(LOG_TAG, "${this::class.simpleName} - $message")
}

fun Any.logw(message: String, throwable: Throwable? = null) {
    Log.w(LOG_TAG, "${this::class.simpleName} - $message", throwable)
}

fun String.prettyClassString(): String =
    trimMargin().replace(Regex(" {4}.*_\\n", RegexOption.MULTILINE), "")

fun <T> List<T>.prettyString(level: Int = 1, indent: String = " ".repeat(4)): String {
    if (isEmpty()) return "[]"
    val defIndent = indent.repeat(level)
    return joinToString(
        prefix = "[\n",
        separator = ",\n",
        postfix = "\n${indent.repeat(level - 1)}]"
    ) { it.toString().replace(Regex("^", RegexOption.MULTILINE), defIndent) }
}

fun TextInputEditText.requireInput(textInputLayout: TextInputLayout): String? {
    val input = text.toString().trim()
    val empty = input.isEmpty()
    if (empty) textInputLayout.error = context.getString(
        R.string.empty_field_message,
        (textInputLayout.hint ?: "").toString()
    )
    else textInputLayout.error = null
    return if (empty) null else input
}

fun LayoutStateBinding.setLayout(@DrawableRes imageResId: Int, @StringRes textResId: Int) {
    root.visibility = View.VISIBLE
    imageViewState.load(imageResId)
    textViewState.setText(textResId)
}

fun LayoutStateBinding.setErrorState() {
    setLayout(R.drawable.svg_undraw_lost_bqr2, R.string.state_error_message)
}

fun LayoutStateBinding.setEmptyState() {
    setLayout(R.drawable.svg_undraw_empty_xct9, R.string.state_empty_message)
}

fun LayoutStateBinding.clearLayout() {
    root.visibility = View.GONE
    imageViewState.clear()
    textViewState.text = null
}

private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun generateRandomString(length: Int) =
    (1..length).map { charPool[Random.nextInt(0, charPool.size)] }.joinToString(separator = "")
