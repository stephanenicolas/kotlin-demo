package com.github.stephanenicolas.kstock.network

sealed class NetworkState<T> {
    class Progress<T> : NetworkState<T>()
    data class Success<T>(val data: T) : NetworkState<T>()
    data class NetworkError<T>(val code: Int) : NetworkState<T>()
    class InvalidData<T> : NetworkState<T>()
}
