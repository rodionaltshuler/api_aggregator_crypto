## API service
##### api_rust

1) Serves order book data aggregated from cryptocurrency exchange via REST API
2) Data is served from the local cache
3) Local cache updates itself with a specified frequency from ReadModel (Redis).

Motivation for this design: Redis load from single api instance is constant regardless requests count API receives.

### Configuration

1. `CacheUpdater` class is responsible for scheduling local cache update, currently if you want to change frequency - configure it in main function (`main.rs`).
   Please note updating more often than request frequency to exchanges by `fetch` might not have sense.


### Usage

1. Generate TLS certificate:

```
openssl req -new -newkey rsa:2048 -days 365 -nodes -x509 -keyout cert/server.key -out cert/server.crt
```

2. Place `server.crt` and `server.key` files generated in step #1 to api_rust/cert folder. Name and location of certificates are customizable in main function (`main.rs`) 

3. Run app standalone, not inside Docker container (Rust tools required: https://www.rust-lang.org/tools/install) 

`cargo run`