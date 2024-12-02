package art.galushko.todo_tests.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties
@Configuration
class PropertiesConfig {
    @Value("\${base.url.rest}")
    lateinit var baseUrlRest: String

    @Value("\${base.url.ws}")
    lateinit var baseUrlWs: String

    @Value("\${auth.admin.username}")
    lateinit var adminUsername: String

    @Value("\${auth.admin.password}")
    lateinit var adminPassword: String
}