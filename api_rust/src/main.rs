#[macro_use]
extern crate lazy_static;

use std::env;
use std::time::Duration;
use actix_web::App;
use actix_web::get;
use actix_web::HttpResponse;
use actix_web::HttpServer;
use actix_web::web;
use actix_web::http::header::ContentType;
use clokwerk::Interval;
use openssl::ssl::{SslAcceptor, SslFiletype, SslMethod};
use crate::cache::{CACHE, CacheUpdater};

mod repository;
mod cache;


#[actix_web::main]
async fn main() -> std::io::Result<()> {

    let mut cache_updater = CacheUpdater::new();

    cache_updater.
        update_cache_now().
        schedule_cache_updates(Interval::Seconds(5));

    let handle = cache_updater.scheduler.watch_thread(Duration::from_millis(100));

    let mut builder = SslAcceptor::mozilla_intermediate(SslMethod::tls()).unwrap();
    builder
        .set_private_key_file("cert/server.key", SslFiletype::PEM)
        .unwrap();
    builder.set_certificate_chain_file("cert/server.crt").unwrap();

    let host = env::var("SERVER_HOST").unwrap_or(String::from("127.0.0.1"));
    let port = env::var("SERVER_PORT").unwrap_or(String::from("8080"));

    HttpServer::new(|| {
        App::new()
            .service(order_books)
            .service(hello)
    })
        //.bind((host, 8080))? -> no-tls version
        .bind_openssl(String::from(format!("{host}:{port}")), builder)?
        .run()
        .await
}


#[get("/hello/")]
async fn hello() -> HttpResponse {
    HttpResponse::Ok()
        .content_type(ContentType::plaintext())
        .body("Hello")
}
#[get("/exchanges/{exchange}/order-books/")]
async fn order_books(exchange: web::Path<(String)>) -> HttpResponse {
    let exchange = &exchange.into_inner();

    let cache_read_lock = CACHE.read().unwrap();
    let cache_result = cache_read_lock.get(String::from(exchange));

    match cache_result {
        Some(b) => {
            HttpResponse::Ok()
                .content_type(ContentType::json())
                .json(b)
        }
        None => {
            let error_message = format!("Data for exchange {} not found", String::from(exchange));
            HttpResponse::NotFound()
                .content_type(ContentType::json())
                .body(error_message)
        }
    }
}