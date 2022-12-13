## API service
##### com.aggregator.api

1) Serves order book data aggregated from cryptocurrency exchange via REST API
2) Data is served from the local cache 
3) Local cache updates itself with a specified frequency from ReadModel (Redis).
   
Motivation for this design: Redis load from single api instance is constant regardless requests count API receives.

### Configuration

1. `com.aggregator.api.cache.CacheUpdateScheduler` class is responsible for scheduling local cache update, currently if you want to change frequency - configure it there. 
  Please note updating more often than request frequency to exchanges by `fetch` might not have sense. 


### Usage

1. Generate TLS certificate:

```
openssl req -new -newkey rsa:2048 -days 365 -nodes -x509 -keyout cert/server.key -out cert/server.crt
```

2. Place `server.crt` and `server.key` files generated in step #1 to api/cert folder. Name and location of certificates are customizable with settings in `application.yml`. 

3. Service is started together with other ones with `docker-compose up -d --build` from the root of the project, see README.md in root for details.

### Development

* JDK 17 or higher is required

1. To stop dockerized instance of this service (to avoid port conflicts in step 3):

`docker stop api1`

2. Run non-dockerized `api` service from IDE or command line:

`./mvnw spring-boot:run`

3. Run tests (mostly WebMVC controller tests; integration tests for RedisRepository are implemented already in `fetch` module)

`./mvnw test`

4. API interactive documentation:

`http://localhost:8080/swagger-ui/index.html`

OpenAPI spec:

`http://localhost:8080/v3/api-docs`


### Future improvements

1. Custom health endpoint which report `healthy` only after successful cache update (so we have a data to serve).

2. Data schema for OrderBookDto is the same in a ReadModel and in controller response, so we can avoid deserelization+serialization cycle in order to lower latency. 

3. Sending updates to clients when new data is available, using Webscokets/SSE/gRPC