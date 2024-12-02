package art.galushko.todo_tests.load

import io.gatling.javaapi.core.CoreDsl.rampUsersPerSec
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation

class PostLoadTestSimulation : Simulation() {

    val scn = scenario("Post TODO endpoint scenario")
        .feed(todoFeeder)
        .exec(createTodoRequest)

    init {
        setUp(
            scn.injectOpen(
                rampUsersPerSec(20.0).to(500.0).during(60),
            )
        ).protocols(httpProtocol).maxDuration(70)
    }
}
