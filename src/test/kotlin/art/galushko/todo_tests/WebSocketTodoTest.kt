package art.galushko.todo_tests

import art.galushko.todo_tests.client.TodoApiClient
import art.galushko.todo_tests.client.WebSocketClientProvider
import art.galushko.todo_tests.client.generateTodo
import art.galushko.todo_tests.test_utils.checking
import io.qameta.allure.Epic
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.TimeUnit

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic("Tests for WebSocket endpoint of TODO app")
@DisplayName("Tests for WebSocket endpoint of TODO app")
class WebSocketTodoTest @Autowired constructor(
    private val todoClient: TodoApiClient,
    private val webSocketClientProvider: WebSocketClientProvider,
){
    @Test
    fun `should receive updates when a TODO is created`() {
        val newTodo = generateTodo()
        val context = webSocketClientProvider.connectToTodoWebSocket(newTodo).first

        todoClient.post(newTodo)

        val message = context.messages.poll(2, TimeUnit.SECONDS)
        checking("Checking received update") {
            assertThat(message?.data).isEqualTo(newTodo)
            assertThat(message?.type).isEqualTo("new_todo")
        }
    }

    @Test
    fun `should not receive updates after WebSocket disconnects`() {
        val newTodo = generateTodo()
        val (context, client) = webSocketClientProvider.connectToTodoWebSocket(newTodo)

        client.closeConnection(1000, "Disconnecting")
        todoClient.post(newTodo)

        val message = context.messages.poll(1, TimeUnit.SECONDS)
        checking("Checking that no update was received") {
            assertThat(message).isNull()
        }
    }

    @Test
    fun `should handle multiple WebSocket clients receiving updates`() {
        val newTodo = generateTodo()
        val context1 = webSocketClientProvider.connectToTodoWebSocket(newTodo).first
        val context2 = webSocketClientProvider.connectToTodoWebSocket(newTodo).first

        todoClient.post(newTodo)

        val message1 = context1.messages.poll(2, TimeUnit.SECONDS)
        val message2 = context2.messages.poll(2, TimeUnit.SECONDS)
        checking("Checking received update") {
            listOf(message1, message2).forEach { message ->
                assertThat(message?.data).isEqualTo(newTodo)
                assertThat(message?.type).isEqualTo("new_todo")
            }
        }
    }
}