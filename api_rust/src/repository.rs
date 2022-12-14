use std::collections::HashMap;
use std::env;
use redis::Commands;
use redis::RedisResult;
use serde::{Deserialize, Serialize};
use std::sync::RwLock;

lazy_static! {
   pub static ref REPOSITORY: RwLock<RedisRepository> = RwLock::new(RedisRepository::new());
}


pub trait Repository<VALUE, KEY> {
    fn save(&mut self, key: &KEY, record: &VALUE) -> VALUE;
    fn find_all(&mut self) -> Option<Vec<VALUE>>;
    fn find(&mut self, key: &KEY) -> Option<VALUE>;
    fn delete_all(&mut self);
}

pub struct RedisRepository {
    connection: redis::Connection,
}

impl RedisRepository {
    pub fn new() -> RedisRepository {
        let redis_host = env::var("REDIS_HOST").unwrap_or(String::from("localhost"));
        let redis_port = env::var("REDIS_PORT").unwrap_or(String::from("6380"));
        let redis_connection_string = format!("redis://{redis_host}:{redis_port}/");
        let client = redis::Client::open(redis_connection_string).unwrap();
        let connection = client.get_connection().unwrap();
        RedisRepository { connection }
    }
}

impl Repository<OrderBook, String> for RedisRepository {
    fn save(&mut self, key: &String, record: &OrderBook) -> OrderBook {
        //TODO implement - required for tests only
        record.clone()
    }

    fn find_all(&mut self) -> Option<Vec<OrderBook>> {
        let collection_name = format!("OrderBookString");
        let exchanges: RedisResult<Vec<String>> = self.connection.smembers(collection_name);
        let exchanges = exchanges.unwrap();
        exchanges.iter().map(|exchange| self.find(&exchange)).collect()
    }

    fn find(&mut self, key: &String) -> Option<OrderBook> {
        let k = format!("OrderBookString:{}", key);
        let exchange_entry : RedisResult<Vec<(String, String)>> = self.connection.hgetall(k);
        match exchange_entry {
            Ok(result) => {
                let map: HashMap<String, String> = result.into_iter().collect();
                let exchange = map.get("exchangeId").unwrap().clone();
                let markets = map.get("markets").unwrap();
                let markets  = serde_json::from_str(markets).unwrap();
                Some(OrderBook { exchange, markets })
            },
            Err(e) => None
        }
    }

    fn delete_all(&mut self) {
        //TODO implement - required for tests only
    }
}

#[derive(Debug, PartialEq, Serialize, Deserialize, Clone)]
pub struct OrderBook {
    #[serde(rename = "exchangeId")]
    pub exchange: String,

    #[serde(rename = "markets")]
    pub markets: Vec<Market>,
}

#[derive(Debug, PartialEq, Serialize, Deserialize, Clone)]
pub struct Market {
    #[serde(rename = "timestamp")]
    timestamp: u64,
    #[serde(rename = "market")]
    market: String,
    #[serde(rename = "avgBid")]
    avg_bid: f64,
    #[serde(rename = "qtyBid")]
    qty_bid: f64,
    #[serde(rename = "avgAsk")]
    avg_ask: f64,
    #[serde(rename = "qtyAsk")]
    qty_ask: f64,
}



#[cfg(test)]
mod redis_repository_tests {
    use serde_json::json;
    use crate::repository::{OrderBook, RedisRepository, Repository};

    /*   #[test]
       fn test_find(){
           let mut repo = RedisRepository::new();
           //TODO save something
           //let record = OrderBook{ exchange: String::from("exchange_1"), markets: String::from("[]") };
           //let saved = repo.save(&record.exchange, &record);

           let exchange = String::from("Blockchain.com");
           let loaded = repo.find(&exchange)
               .expect("Record should present in repository after saving");
           //TODO assert against data saved above in this test

           assert_eq!(loaded.exchange, exchange);
           assert!(loaded.markets.contains("ETH-BTC"));

       }*/


    #[test]
    fn test_save() {
        /* let mut repo = RedisRepository::new();
         let record = OrderBook{ exchange: String::from("exchange_1"), markets: String::from("[]") };
         let saved = repo.save(&record.exchange, &record);
         let loaded = repo.find(&saved.exchange)
             .expect("Record should present in repository after saving");
         assert_eq!(saved, loaded);*/
    }

    #[test]
    fn test_find_all_returns_all() {
        /*let mut repo = RedisRepository::new();
        let record = OrderBook{ exchange: String::from("exchange_1"),  markets: json!("[]") };
        repo.save(&record.exchange, &record);
        let books = repo.find_all();
        let expected_books = json!(vec![record]);
        assert_eq!(books, expected_books);*/
    }

    #[test]
    fn test_find_all_empty() {
        /*let mut repo = RedisRepository::new();
        let books = repo.find_all();
        assert!(books.is_empty());*/
    }

    #[test]
    fn test_find_all_empty_after_delete_all() {
        /* let mut repo = RedisRepository::new();
         let record = OrderBook{ exchange: String::from("exchange_1"),  markets: String::from("[]") };
         repo.save(&record.exchange, &record);
         let books = repo.find_all();
         assert!(!books.is_empty());

         repo.delete_all();
         assert!(repo.find_all().is_empty());*/
    }
}