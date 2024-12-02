package art.galushko.todo_tests.test_utils

import io.qameta.allure.Allure
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.springframework.web.client.RestClientResponseException

fun checking(name: String, assertions: SoftAssertions.() -> Unit) {
    Allure.step(name) { _ ->
        val assertion = SoftAssertions()
        assertions(assertion)
        assertion.assertAll()
    }
}

fun checkingFailedRequest(expect: String, action: () -> Unit, assertions: SoftAssertions.(RestClientResponseException) -> Unit) {
    try {
        action()
        Assertions.fail(expect)
    } catch (e: RestClientResponseException) {
        Allure.step("Check failed request") { _ ->
            val assertion = SoftAssertions()
            assertions(assertion, e)
            assertion.assertAll()
        }
    } catch (e: Throwable) {
        Assertions.fail(expect, e)
    }
}