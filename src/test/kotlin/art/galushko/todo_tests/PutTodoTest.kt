package art.galushko.todo_tests

import art.galushko.todo_tests.client.Todo
import art.galushko.todo_tests.client.TodoApiClient
import art.galushko.todo_tests.config.PropertiesConfig
import art.galushko.todo_tests.test_utils.checking
import art.galushko.todo_tests.test_utils.checkingFailedRequest
import art.galushko.todo_tests.utlis.getBasicAuth
import io.qameta.allure.Epic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic("Tests for PUT endpoint of TODO app")
@DisplayName("Tests for PUT endpoint of TODO app")
class PutTodoTest @Autowired constructor(
    private val todoClient: TodoApiClient,
    private val config: PropertiesConfig,
) {
    lateinit var existItem: Todo

    @BeforeAll
    fun setup() {
        existItem = Todo(id = System.nanoTime(), "Some todo " + System.nanoTime(), completed = false).also {
            todoClient.post(it)
        }
    }

    @AfterAll
    fun teardown() {
        todoClient.delete(existItem.id)
    }

    @Test
    fun `should update an existing TODO`() {
        val updatedTodo = existItem.copy(text = "Updated TODO Text", completed = true)

        val response = todoClient.put(updatedTodo.id, updatedTodo)
        checking("check updated item") {
            assertThat(response.statusCode).isEqualTo(HttpStatusCode.valueOf(200))
            todoClient.get()?.find { it.id == updatedTodo.id }?.let {
                assertThat(it.text).isEqualTo(updatedTodo.text)
                assertThat(it.completed).isEqualTo(updatedTodo.completed)
            } ?: fail("Updated item not found")
        }
    }

    @Test
    fun `should return 404 for non-existent TODO id`() {
        checkingFailedRequest("Not found is expected", {
            todoClient.put(System.nanoTime(), existItem)
        }) {
            assertThat(it.statusCode).isEqualTo(HttpStatusCode.valueOf(404))
            assertThat(it.responseBodyAsString).isEmpty()
        }
    }

    @ParameterizedTest
    @ArgumentsSource(BadBodyProvider::class)
    fun `should return 401 for invalid TODO update payload without authorization`(item: Todo) {
        checkingFailedRequest("Unauthorized is expected", {
            todoClient.put(existItem.id, item)
        }) {
            assertThat(it.statusCode).isEqualTo(HttpStatusCode.valueOf(401))
            assertThat(it.responseBodyAsString).isEmpty()
        }
    }

    @ParameterizedTest
    @ArgumentsSource(BadBodyProvider::class)
    fun `should return 400 for invalid TODO update payload without authorization`(item: Todo, expectedError: String) {
        checkingFailedRequest("Not found is expected", {
            todoClient.put(existItem.id, item) {
                header(HttpHeaders.AUTHORIZATION, getBasicAuth(config.adminUsername, config.adminPassword))
            }
        }) {
            assertThat(it.statusCode).isEqualTo(HttpStatusCode.valueOf(400))
            assertThat(it.responseBodyAsString).contains(expectedError)
        }
    }
}