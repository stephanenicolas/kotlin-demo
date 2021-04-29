package com.github.stephanenicolas.kstock.ui

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

fun Context.hideKeyboard(view: View) {
  val inputMethodManager =
    getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
  inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}
