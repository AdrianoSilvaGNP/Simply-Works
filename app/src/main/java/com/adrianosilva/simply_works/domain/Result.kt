package com.adrianosilva.simply_works.domain

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val reason: ErrorReason) : Result<T>()
}

sealed class ErrorReason {
    data object NoData : ErrorReason()
    data object NoConnection : ErrorReason()
    data class NetworkError(val message: String) : ErrorReason()
    data class Unknown(val exception: Throwable) : ErrorReason()
}
