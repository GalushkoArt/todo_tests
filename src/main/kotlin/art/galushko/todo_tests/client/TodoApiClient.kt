package art.galushko.todo_tests.client

import art.galushko.todo_tests.config.PropertiesConfig
import art.galushko.todo_tests.utlis.LoggingInterceptor
import art.galushko.todo_tests.utlis.getBasicAuth
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.qameta.allure.Step
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

@Service
class TodoApiClient(
    private val config: PropertiesConfig,
) {
    private val log = LoggerFactory.getLogger(TodoApiClient::class.java)
    private val client: RestClient = RestClient.builder()
        .baseUrl(config.baseUrlRest)
        .requestInterceptor(LoggingInterceptor(log))
        .build()

    @Step("Make GET request to /todos")
    fun get(offset: Int? = null, limit: Int? = null) = client.get()
        .uri { builder ->
            builder.path("/todos").apply {
                offset?.let { offset -> queryParam("offset", offset) }
                limit?.let { limit -> queryParam("limit", limit) }
            }.build()
        }.retrieve()
        .body(typeReference<List<Todo>>())

    @Step("Make POST request to /todos")
    fun post(todo: Todo) = client.post()
        .uri("/todos")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(todo)
        .retrieve()
        .toBodilessEntity()

    @Step("Make PUT request to /todos/{0}")
    fun put(id: Long?, todo: Todo, addHeaders: (RestClient.RequestHeadersSpec<*>.() -> Unit)? = null) = client.put()
        .uri("/todos/${id ?: ""}")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .apply { addHeaders?.invoke(this) }
        .body(todo)
        .retrieve()
        .toBodilessEntity()

    @Step("Make DELETE request to /todos/{0}")
    fun delete(id: Long?, overrideAuth: (RestClient.RequestHeadersSpec<*>.() -> Unit)? = null) = client.delete()
        .uri("/todos/${id ?: ""}")
        .apply {
            overrideAuth?.invoke(this) ?: header(
                HttpHeaders.AUTHORIZATION,
                getBasicAuth(config.adminUsername, config.adminPassword)
            )
        }
        .retrieve()
        .toBodilessEntity()
}

@JsonInclude(Include.NON_NULL)
data class Todo(
    val id: Long? = null,
    val text: String? = null,
    val completed: Boolean? = null,
)

fun generateTodo(id: Long? = System.nanoTime(), completed: Boolean? = true) =
    Todo(id = id, "Some todo $id", completed = completed)