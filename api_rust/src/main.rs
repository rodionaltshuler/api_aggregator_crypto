#[macro_use]
extern crate lazy_static;
use actix_web::App;
use actix_web::get;
use actix_web::HttpResponse;
use actix_web::HttpServer;
use actix_web::web;
use actix_web::http::header::ContentType;
use crate::cache::CACHE;

mod repository;
mod cache;


#[actix_web::main]
async fn main() -> std::io::Result<()> {
    HttpServer::new(|| {
        App::new()
            .service(order_books)
    })

        .bind(("127.0.0.1", 8081))?
        .run()
        .await
}


#[get("/exchanges/{exchange}/order-books/")]
async fn order_books(exchange: web::Path<(String)>) -> HttpResponse {

    let exchange = &exchange.into_inner();

    let cache_read_lock = CACHE.read().unwrap();
    let cache_result = cache_read_lock.get(String::from(exchange));

    match cache_result {
        Some(b) => {
            let body = serde_json::to_string(&b).unwrap();
            HttpResponse::Ok()
                .content_type(ContentType::json())
                .body(body) },
        None => {
            let error_message = format!("Data for exchange {} not found", String::from(exchange));
            HttpResponse::NotFound()
                .content_type(ContentType::json())
                .body(error_message)
        }
    }
}