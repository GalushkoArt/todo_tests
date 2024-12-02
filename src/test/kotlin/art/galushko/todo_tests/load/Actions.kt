package art.galushko.todo_tests.load

import io.gatling.javaapi.core.CoreDsl.StringBody
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status


val createTodoRequest = http("Create TODO")
    .post("/todos")
    .body(StringBody("""{"id":#{todoId},"text":"#{text}","completed":#{completed}}"""))
    .check(status().`is`(201))


val getTodosRequest = http("Get TODO")
    .get("/todos")
    .check(status().`is`(200))


val updateTodoRequest = http("Update TODO")
    .put("/todos/#{todoId}")
    .body(StringBody("""{"id":#{todoId},"text":"#{text}","completed":true}"""))
    .check(status().`is`(200))

val deleteTodoRequest = http("Delete TODO")
    .delete("/todos/#{todoId}")
    .header("Authorization", "#{auth}")
    .check(status().`is`(204))
