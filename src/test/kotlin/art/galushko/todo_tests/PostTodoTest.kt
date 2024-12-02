package art.galushko.todo_tests

import art.galushko.todo_tests.client.Todo
import art.galushko.todo_tests.client.TodoApiClient
import art.galushko.todo_tests.client.generateTodo
import art.galushko.todo_tests.test_utils.checking
import art.galushko.todo_tests.test_utils.checkingFailedRequest
import io.qameta.allure.Epic
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatusCode

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic("Tests for POST endpoint of TODO app")
@DisplayName("Tests for POST endpoint of TODO app")
class PostTodoTest @Autowired constructor(
    private val todoClient: TodoApiClient,
) {

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should create a new TODO with valid data`(completed: Boolean) {
        val newTodo = generateTodo(completed = completed)
        val response = todoClient.post(newTodo)

        checking("check creation of the new item") {
            assertThat(response.statusCode).isEqualTo(HttpStatusCode.valueOf(201))
            todoClient.get()?.find { it.id == newTodo.id }?.let {
                assertThat(it.text).isEqualTo(newTodo.text)
                assertThat(it.completed).isEqualTo(newTodo.completed)
            } ?: fail("Created item not found")
        }
    }

    @ParameterizedTest
    @ArgumentsSource(BadBodyProvider::class)
    fun `should return 400 for missing required fields`(item: Todo, expectedError: String) {
        checkingFailedRequest("Bad request is expected", {
            todoClient.post(item)
        }) {
            assertThat(it.statusCode).isEqualTo(HttpStatusCode.valueOf(400))
            assertThat(it.responseBodyAsString).contains(expectedError)
        }
    }

    @Test
    fun `should return 400 for item with exists id`() {
        checkingFailedRequest("Bad request is expected", {
            val item = generateTodo(completed = false)
            todoClient.post(item)
            todoClient.post(Todo(id = item.id, text = "test", completed = false))
        }) {
            assertThat(it.statusCode).isEqualTo(HttpStatusCode.valueOf(400))
            assertThat(it.responseBodyAsString).isEmpty()
        }
    }
}