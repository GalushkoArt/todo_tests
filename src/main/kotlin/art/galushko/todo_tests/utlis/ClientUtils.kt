package art.galushko.todo_tests.utlis

import io.qameta.allure.Allure
import org.slf4j.Logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayInputStream
import java.util.Base64
import kotlin.collections.toString

class LoggingInterceptor(private val logger: Logger) : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logRequest(request, body)
        val response = execution.execute(request, body)
        val responseBody = response.body.readAllBytes()
        logResponse(request, response, responseBody)
        return BufferingClientHttpResponseWrapper(response, responseBody)
    }

    private fun headersString(headers: HttpHeaders) = headers.headerSet()
            .joinToString(separator = "\n", transform = { " " + it.key + ": " + it.value })

    private fun logRequest(request: HttpRequest, body: ByteArray?) {
        val bodyString = body?.toString(Charsets.UTF_8)?.ifEmpty { null }
        val requestInfo = """
            |Method: ${request.method}
            |URI: ${request.uri}
            |Headers:
            |${headersString(request.headers)}${bodyString?.let { "\nBody:\n$it" } ?: ""}
        """.trimMargin()
        logger.info("\n" + requestInfo)
        Allure.addAttachment("Request", requestInfo)
    }

    private fun logResponse(request: HttpRequest, response: ClientHttpResponse, body: ByteArray) {
        val bodyString = body.toString(Charsets.UTF_8).ifEmpty { null }
        val responseInfo = """
            |Response from ${request.method} ${request.uri}:
            |Status code: ${response.statusCode}
            |Headers:
            |${headersString(response.headers)}${bodyString?.let { "\nBody:\n$it" } ?: ""}
        """.trimMargin()
        logger.info("\n" + responseInfo)
        Allure.addAttachment("Response", responseInfo)
    }
}

class BufferingClientHttpResponseWrapper(val response: ClientHttpResponse, val body: ByteArray) : ClientHttpResponse by response {
    override fun getBody() = ByteArrayInputStream(body)
}

fun getBasicAuth(username: String, password: String) =
    "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"