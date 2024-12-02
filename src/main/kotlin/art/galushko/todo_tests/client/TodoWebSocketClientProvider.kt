package art.galushko.todo_tests.client

import art.galushko.todo_tests.config.PropertiesConfig
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.qameta.allure.Step
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.Exception
import java.net.URI
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Service
class WebSocketClientProvider(
    private val objectMapper: ObjectMapper,
    private val config: PropertiesConfig,
) {
    @Step("Connecting to TODO WebSocket endpoint")
    fun connectToTodoWebSocket(expectedMessage: Todo): Pair<WebSocketContext<Todo>, WsClient<Todo>> {
        val webSocketContext = WebSocketContext<Todo>(config.baseUrlWs + "/ws", expectedMessage = expectedMessage)
        val client = WsClient(webSocketContext, object : TypeReference<WebSocketData<Todo>>() {}, objectMapper)
        client.connectBlocking(3, TimeUnit.SECONDS)
        return Pair(webSocketContext, client)
    }
}

class WsClient<T>(
    private val context: WebSocketContext<T>,
    private val typeReference: TypeReference<WebSocketData<T>>,
    private val objectMapper: ObjectMapper,
) : WebSocketClient(URI(context.url), Draft_6455(), null, context.timeout * 1000) {
    val log = LoggerFactory.getLogger(WsClient::class.java)

    override fun onOpen(handshakedata: ServerHandshake) {
        log.info("Connection with ${context.url} established. ${handshakedata.httpStatus} ${handshakedata.httpStatusMessage}")
    }

    override fun onMessage(message: String) {
        log.info("Received message: $message")
        val todo = objectMapper.readValue(message, typeReference)
        if (todo != null) {
            context.messages.offer(todo)
        }
        if (context.expectedMessage?.equals(todo.data) == true) {
            closeConnection(1000, "Expected message received")
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        log.info("Connection closed with code $code. Reason: $reason. Remote: $remote")
    }

    override fun onError(ex: Exception) {
        log.error("Connection error: ${ex.message}", ex)
    }

    @Step("Closing connection with code {0} and message {1}")
    override fun closeConnection(code: Int, message: String?) {
        super.closeConnection(code, message)
    }
}

data class WebSocketContext<T>(
    val url: String,
    val messages: BlockingQueue<WebSocketData<T>> = LinkedBlockingQueue<WebSocketData<T>>(),
    val expectedMessage: T? = null,
    var statusCode: Int? = null,
    val requestHeaders: Map<String, String> = emptyMap(),
    val timeout: Int = 10,
)

data class WebSocketData<T>(
    val data: T,
    val type: String,
)