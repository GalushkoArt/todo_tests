# TODO app tests

## About

More about the task you can read in [TASK.md](TASK.md)

## Tech stack

- Java 21
- Kotlin 2
- JUnit 5
- Gatling 3.13
- Spring Boot 3.4
- Java-WebSocket
- AssertJ
- Allure

## Prerequisites
- Java 21
- Docker

## How to run test

1. Load docker image with the command:

```shell
docker load < todo-app.tar
```

2. Run docker image:

```shell
docker run -p 8080:4242 todo-app
```

3. Run functional test

```shell
./gradlew clean test
```

4. Generate or serve allure report locally

```shell
./gradlew allureReport
```

```shell
./gradlew allureServe
```

5. Run load test

```shell
./gradlew gatlingRun --simulation art.galushko.todo_tests.load.PostLoadTestSimulation
```

```shell
./gradlew gatlingRun --simulation art.galushko.todo_tests.load.TodoLoadTestSimulation
```
**Caution**: each run of PostLoadTestSimulation will affect on another load run until container restart
