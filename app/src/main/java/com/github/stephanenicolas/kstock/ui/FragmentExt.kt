package com.github.stephanenicolas.kstock.ui

import androidx.fragment.app.Fragment

fun Fragment.hideKeyboard() {
  view?.let {
    activity?.hideKeyboard(it)
  }
}
