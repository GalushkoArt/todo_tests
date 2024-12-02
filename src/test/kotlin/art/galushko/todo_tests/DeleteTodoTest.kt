package art.galushko.todo_tests

import art.galushko.todo_tests.client.Todo
import art.galushko.todo_tests.client.TodoApiClient
import art.galushko.todo_tests.test_utils.checking
import art.galushko.todo_tests.test_utils.checkingFailedRequest
import art.galushko.todo_tests.utlis.getBasicAuth
import io.qameta.allure.Epic
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic("Tests for DELETE endpoint of TODO app")
@DisplayName("Tests for DELETE endpoint of TODO app")
class DeleteTodoTest @Autowired constructor(
    private val todoClient: TodoApiClient,
) {

    @Test
    fun `should delete an existing TODO`() {
        val item = Todo(id = System.nanoTime(), "Some todo " + System.nanoTime(), completed = true)
        todoClient.post(item)
        val response = todoClient.delete(item.id)
        checking("check deletion of the item") {
            assertThat(response.statusCode).isEqualTo(HttpStatusCode.valueOf(204))
            todoClient.get()?.find { it.id == item.id }?.let {
                fail("The item is not deleted")
            }
        }
    }

    @Test
    fun `should return 404 for non-existent TODO`() {
        checkingFailedRequest("Not found is expected", {
            todoClient.delete(System.nanoTime())
        }) {
            assertThat(it.statusCode).isEqualTo(HttpStatusCode.valueOf(404))
            assertThat(it.responseBodyAsString).isEmpty()
        }
    }

    @Test
    fun `should return 401 for missing Authorization header`() {
        checkingFailedRequest("Unauthorized is expected", {
            todoClient.delete(System.nanoTime()) { }
        }) {
            assertThat(it.statusCode).isEqualTo(HttpStatusCode.valueOf(401))
            assertThat(it.responseBodyAsString).isEmpty()
        }
    }

    @Test
    fun `should return 401 for incorrect credentials`() {
        checkingFailedRequest("Unauthorized is expected", {
            todoClient.delete(System.nanoTime()) { header(HttpHeaders.AUTHORIZATION, getBasicAuth("username", "pass")) }
        }) {
            assertThat(it.statusCode).isEqualTo(HttpStatusCode.valueOf(401))
            assertThat(it.responseBodyAsString).isEmpty()
        }
    }
}