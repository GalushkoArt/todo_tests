package art.galushko.todo_tests.load

import io.gatling.javaapi.core.CoreDsl.rampUsersPerSec
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation

class TodoLoadTestSimulation : Simulation() {

    val scn = scenario("Full TODO Workflow")
        .feed(todoFeeder)
        .feed(authFeeder)
        .exec(createTodoRequest)
        .exec(getTodosRequest)
        .exec(updateTodoRequest)
        .exec(getTodosRequest)
        .exec(deleteTodoRequest)

    init {
        setUp(
            scn.injectOpen(
                rampUsersPerSec(50.0).to(500.0).during(60),
            )
        ).protocols(httpProtocol).maxDuration(70)
    }
}
