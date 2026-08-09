package com.allenljf.weatherforecast.core.common.result

/**
 * Represents the outcome of an operation that can fail with a domain-level [AppError].
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    /** No connectivity or the request could not reach the server. */
    data object Network : AppError

    /** The server responded with an error status. */
    data class Server(val code: Int) : AppError

    /** Anything unexpected. */
    data class Unknown(val message: String? = null) : AppError
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
}
