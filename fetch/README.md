## Fetch service
##### com.aggregator.fetch

1) Fetches fresh market data from cryptocurrency exchange,
2) transforms it to common format of OrderBook 
3) saves to ReadModel used later by Web API service.

Current implementation uses Redis as a ReadModel storage.

### Configuration

1. Single fetch services implementation supposed to be used for specific Crypto exchange,
implementing ExchangeFetchService according to particular exchange API.

2. Exchange ID we use in our system to identify exchanges is configured in `application.yml`.

4. `com.aggregator.fetch.fetch.UpdateScheduler` class is responsible for scheduling fetches, currently if you want to change frequency - configure it there.

### Usage

Service is started together with other ones with `docker-compose up -d --build` from the root of the project, see README.md in root for details.

### Development

* JDK 17 or higher is required

1. To stop dockerized instance of this service (to avoid port conflicts in step 3):

`docker stop fetch1`

3. Run non-dockerized `fetch` service from IDE or command line:

`./mvnw spring-boot:run`

4. Run tests (unit and integration ones; uses Redis from testcontainers so won't affect the one you run with docker-compose)

`./mvnw test`