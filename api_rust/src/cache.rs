use std::collections::HashMap;
use std::sync::RwLock;
use clokwerk::{Interval, Job, ScheduleHandle, Scheduler, TimeUnits};
use crate::repository::{OrderBook, RedisRepository, Repository, REPOSITORY};

lazy_static! {
   pub static ref CACHE: RwLock<Cache> = RwLock::new(Cache::new());
}

pub struct Cache {
    cache: HashMap<String, OrderBook>,
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

pub struct CacheUpdater {
    pub scheduler: Scheduler,
}

impl CacheUpdater {
    pub fn new() -> CacheUpdater {
        CacheUpdater {
            scheduler: Scheduler::new()
        }
    }

    pub fn update_cache_now(&mut self) -> &mut CacheUpdater {
        CacheUpdater::update_cache();
        self
    }

    pub fn schedule_cache_updates(&mut self, interval: Interval) -> &mut CacheUpdater {
        self.scheduler.every(interval).run(|| {
            CacheUpdater::update_cache()
        });
        self
    }

    pub fn update_cache() {
        println!("Updating cache");
        let exchanges = REPOSITORY.write().unwrap().find_all().unwrap();
        println!("Got {} exchanges data from repo", exchanges.len());
        exchanges.into_iter().for_each(|b| CACHE.write().unwrap().put(b));

    }
}


#[cfg(test)]
mod cache_tests {

    //TODO
}