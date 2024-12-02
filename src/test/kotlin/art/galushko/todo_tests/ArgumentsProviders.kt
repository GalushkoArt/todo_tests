package art.galushko.todo_tests

import art.galushko.todo_tests.client.Todo
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class BadBodyProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext) : Stream<Arguments> = Stream.of(
        Arguments.of(Todo(id = System.nanoTime(), text = "test"), "missing field `completed`"),
        Arguments.of(Todo(id = System.nanoTime(), completed = false), "missing field `text`"),
        Arguments.of(Todo(text = "test", completed = false), "missing field `id`"),
        Arguments.of(Todo(text = "test"), "missing field `id`"),
        Arguments.of(Todo(completed = false), "missing field `id`"),
        Arguments.of(Todo(id = System.nanoTime()), "missing field `text`"),
    )
}