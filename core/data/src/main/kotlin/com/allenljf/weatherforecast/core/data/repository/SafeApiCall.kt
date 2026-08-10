package com.allenljf.weatherforecast.core.data.repository

import com.allenljf.weatherforecast.core.common.result.AppError
import com.allenljf.weatherforecast.core.common.result.AppResult
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

/**
 * Executes [block] and wraps the outcome in an [AppResult], translating known
 * failures to domain-level [AppError]s. [CancellationException] is rethrown so
 * coroutine cancellation keeps propagating.
 */
internal suspend inline fun <T> safeApiCall(block: () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: IOException) {
    AppResult.Error(AppError.Network)
} catch (e: HttpException) {
    AppResult.Error(AppError.Server(e.code()))
} catch (e: Throwable) {
    AppResult.Error(AppError.Unknown(e.message))
}
