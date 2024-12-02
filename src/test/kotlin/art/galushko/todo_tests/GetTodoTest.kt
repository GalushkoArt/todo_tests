package art.galushko.todo_tests

import art.galushko.todo_tests.client.Todo
import art.galushko.todo_tests.client.TodoApiClient
import art.galushko.todo_tests.test_utils.checking
import io.qameta.allure.Allure.step
import io.qameta.allure.Epic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic("Tests for GET endpoint of TODO app")
@DisplayName("Tests for GET endpoint of TODO app")
class GetTodoTest @Autowired constructor(
    private val todoClient: TodoApiClient,
) {
    private val idOffset = 123456L

    @BeforeAll
    fun setup() {
        step("Initialize todos") { _ ->
            for (id in 1L..20L) {
                todoClient.post(Todo(id = id + idOffset, text = id.toString(), completed = false))
            }
        }
    }

    @AfterAll
    fun teardown() {
        step("Cleanup todos") { _ ->
            for (id in 1L..20L) {
                todoClient.delete(id + idOffset)
            }
        }
    }

    @Test
    fun `should fetch all TODOs`() {
        val response = todoClient.get()
        checking("check get all todos") {
            assertThat(response).hasSizeGreaterThanOrEqualTo(20)
        }
    }

    @Test
    fun `should fetch TODOs with offset and limit`() {
        val original = todoClient.get()
        val response = todoClient.get(offset = 5, limit = 10)
        checking("check get all todos with limit and offset") {
            assertThat(response).hasSize(10)
            assertThat(response).containsAll(original?.subList(5, 15))
        }
    }

    @Test
    fun `should fetch TODOs with offset`() {
        val original = todoClient.get()
        val response = todoClient.get(offset = 5)
        checking("check get all todos with offset") {
            assertThat(response).hasSize(original?.size?.minus(5) ?: 0)
            assertThat(response).containsAll(original?.subList(5, original.size))
        }
    }

    @Test
    fun `should fetch TODOs with limit`() {
        val original = todoClient.get()
        val response = todoClient.get(limit = 10)
        checking("check get todos with limit") {
            assertThat(response).hasSize(10)
            assertThat(response).containsAll(original?.subList(0, 10))
        }
    }
}