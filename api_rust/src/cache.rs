use std::collections::HashMap;
use crate::repository::OrderBook;
use std::sync::{RwLock};

lazy_static! {
   pub static ref CACHE: RwLock<Cache> = RwLock::new(Cache::new());
}

pub struct Cache {
    cache: HashMap<String, OrderBook>
}

impl Cache {

    pub fn put(&mut self, book: OrderBook) {
       self.cache.insert(book.exchange.to_string(), book);
    }

    pub fn get(&self, exchange: String) -> Option<&OrderBook> {
        self.cache.get(&exchange)
    }

    pub fn new() -> Cache {
        Cache {
            cache: HashMap::new()
        }
    }
}


#[cfg(test)]
mod cache_tests {

    //TODO
}