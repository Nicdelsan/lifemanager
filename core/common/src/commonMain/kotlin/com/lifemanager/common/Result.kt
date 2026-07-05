package com.lifemanager.common

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val throwable: Throwable, val message: String? = null) : Result<Nothing>
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (Throwable, String?) -> Unit): Result<T> {
    if (this is Result.Error) action(throwable, message)
    return this
}
