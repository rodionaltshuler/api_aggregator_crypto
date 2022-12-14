FROM rust:1.65 as build

WORKDIR /workspace/app

COPY ./Cargo.toml ./Cargo.toml
COPY ./Cargo.lock ./Cargo.lock
COPY ./src ./src

RUN cargo build --release

FROM rust:1.60

ENV REDIS_HOST "redis"
ENV REDIS_PORT "6379"
ENV SERVER_HOST "0.0.0.0"

COPY --from=build /workspace/app/target/release/api-rust .

CMD ["./api-rust"]