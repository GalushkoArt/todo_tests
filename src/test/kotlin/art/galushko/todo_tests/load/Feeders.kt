package art.galushko.todo_tests.load

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.util.Base64
import java.util.concurrent.atomic.AtomicLong

val config: Config = ConfigFactory.parseResources("gatling.conf").getConfig("gatling")

private val idCounter = AtomicLong(1)

val todoFeeder = generateSequence {
    val id = idCounter.getAndIncrement()
    mapOf(
        "todoId" to id,
        "text" to "TODO #$id",
        "completed" to false
    )
}.iterator()

val authFeeder = generateSequence {
    val authConfig = config.getConfig("rest").getConfig("auth")
    var auth = "Basic ${Base64.getEncoder().encodeToString("${authConfig.getString("username")}:${authConfig.getString("password")}".toByteArray())}"
    mapOf(
        "auth" to auth,
    )
}.iterator()