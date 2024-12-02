package art.galushko.todo_tests.load

import io.gatling.javaapi.http.HttpDsl.http


val httpProtocol = http.baseUrl(config.getConfig("rest").getString("url")).header("Content-Type", "application/json")
