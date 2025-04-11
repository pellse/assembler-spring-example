# Real-Time Patient Monitoring with Spring and AI

This repository showcases the standalone usage of [Assembler](https://github.com/pellse/assembler) + an example of integration with different Spring related technologies like [Spring AI](https://spring.io/projects/spring-ai),  [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web-reactive.html) and [Spring GraphQL](https://spring.io/projects/spring-graphql) for real-time data composition.

## Tech Stack
- [Assembler](https://github.com/pellse/assembler)
- [Spring AI](https://spring.io/projects/spring-ai)
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web-reactive.html)
- [Spring GraphQL](https://spring.io/projects/spring-graphql)
- Spring Data
  - [Reactive PostgreSQL (R2DBC)](https://spring.io/projects/spring-data-r2dbc)
  - [Reactive MongoDB](https://spring.io/projects/spring-data-r2dbc)
- Spring Cloud
  - [Spring Cloud Common](https://spring.io/projects/spring-cloud-commons)
    - [Client-Side Load-Balancing with Spring Cloud LoadBalancer](https://spring.io/guides/gs/spring-cloud-loadbalancer)
  - [Spring Cloud Function](https://spring.io/projects/spring-cloud-function)
  - [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream)
    - Reactive Kafka
- [Testcontainers](https://www.testcontainers.org/)

## Scenario
This example showcases a basic healthcare application designed to simulate monitoring patient data. Four services are implemented:
- The *Patient Service* retrieves patient demographics from PostgreSQL.
- The *Blood Pressure Service* fetches a patient's systolic and diastolic blood pressure readings from MongoDB.
  - Coming soon: Support for Change Data Capture (CDC) for real-time synchronization of the *Blood Pressure Service*'s data cache.
- The *Heart Rate Streaming Service* provides real-time heart rate monitoring from an ECG device via Kafka.
- The ***Diagnosis AI Service*** analyzes heart rate and blood pressure data to produce diagnostic insights.

A GraphQL Controller implemented in `PatientObservationController` aggregates data from these services. A GraphQL Subscription and REST SSE (Server-Sent Events) endpoint are also implemented in `VitalsMonitoringStreamController` for real-time data aggregation from a stream of heart rate records.

Check out this brief presentation for a walkthrough of the Assembler API for the real-time streaming example (please note that the newly added AI/LLM integration is not covered in this presentation):

https://github.com/user-attachments/assets/5d9efa18-521f-4bcc-b6ec-5bb0d9ca3a59

You can also view the presentation [here](https://snappify.com/view/a113a410-7957-4e39-898e-38bff1ec7982) and go through each slide at your own speed.

## Assembler with Spring WebFlux/GraphQL for API Composition and solving the N+1 Query Problem

### Batch Mapping (Data Querying)
The new `BatchRule` API from Assembler seamlessly integrates with the Spring GraphQL [@BatchMapping](https://docs.spring.io/spring-graphql/docs/current/reference/html/#controllers.batch-mapping) mechanism, as shown in the usage example found in `PatientObservationController`. Additionally, this example showcases additional features of Assembler, including:
- caching of service invocations using the [cached()](https://github.com/pellse/assembler#reactive-caching) function
- caching of real-time data streams with the [streamTable()](https://github.com/pellse/assembler#stream-table) function.

![image](https://github.com/user-attachments/assets/d54fa70e-c0a5-4d94-be33-936926aa8b27)

### Subscription Mapping (Data Streaming)
Assembler excels in complex data aggregation within a data streaming scenario. This example, via `VitalsMonitoringStreamController`, demonstrates its usage in standalone mode in conjunction with Spring WebFlux for REST Server-Sent Events, and Spring GraphQL using [@SubscriptionMapping](https://docs.spring.io/spring-graphql/docs/current/reference/html/#controllers.schema-mapping). By combining streaming and batching, Assembler enables seamless data stream augmentation for clients connected via persistent HTTP connections or WebSockets.

![image](https://github.com/user-attachments/assets/8574f4aa-de03-4327-8f9d-c39597fd25a1)

## How to Run the Application
- Configure [application.yml](https://github.com/pellse/assembler-spring-example/blob/main/src/main/resources/application.yml) with your OpenAI compatible API Key (Google Gemini is used in this example):
  ```yaml
  spring:
    ai:
      openai:
        api-key: ${GEMINI_API_KEY}
        chat:
          base-url: https://generativelanguage.googleapis.com
          completions-path: /v1beta/openai/chat/completions
          options:
            model: gemini-2.0-flash
  ```
- Make sure Docker is installed
- Run the `main` method in *src\test\java\io\github\pellse\example\PatientMonitoringApplicationTest.java*
  - Or execute the *bootTestRun* Gradle Task

This repository takes advantage of the new [Spring Boot 3.1.0 Testcontainers support](https://www.atomicjar.com/2023/05/spring-boot-3-1-0-testcontainers-for-testing-and-local-development/) for local development.

## How to Use the Application (GraphQL)
Open a browser at http://localhost:8080/graphiql?path=/graphql

#### For Batch Mapping (Data Querying)
- Run the following GraphQL Query:
```graphql
query PatientQuery {
  patients {
    id
    name
    healthCardNumber
    bloodPressures {
      systolic
      diastolic
      time
    }
    heartRate {
      heartRateValue
      time
    }
  }
}
```
Periodically rerun the query, the number of `HR` (heart rate) values for each patient should increase:

![PatientObservationController](https://github.com/user-attachments/assets/57bc660c-e092-45ec-b16a-748df2aa9a02)

#### For Subscription Mapping (Data Streaming)
- Run the following GraphQL Query:
```graphql
subscription VitalsMonitoringStream {
  vitals {
    patient {
      id
      name
      healthCardNumber
    }
    bloodPressures {
      systolic
      diastolic
      time
    }
    heartRate {
      heartRateValue
      time
    }
    diagnosis
  }
}
```
You should see the following:

https://github.com/user-attachments/assets/8b3b3880-9d58-4653-9373-0c7ed524a664

## How to Use the Application (REST Server-Sent Events)
Open a browser or a an HTTP Client (e.g. Postman) at http://localhost:8080/vitals/stream

You should see the following:

https://github.com/user-attachments/assets/1a3ebee5-1a04-4d58-9ede-220add5e1e1e
