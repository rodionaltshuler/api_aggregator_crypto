use serde::{Deserialize, Serialize};

pub trait Repository<VALUE, KEY> {
    fn save(&self, key: &KEY, record: &VALUE) -> VALUE;
    fn find_all(&self) -> Vec<VALUE>;
    fn find(&self, key: &KEY) -> Option<VALUE>;
    fn delete_all(&self);
}

pub struct RedisRepository {

}

impl Repository<OrderBook, String> for RedisRepository {

    fn save(&self, key: &String, record: &OrderBook) -> OrderBook {
        OrderBook { exchange: record.exchange.clone() }
    }

    fn find_all(&self) -> Vec<OrderBook> {
        vec![]
    }

    fn find(&self, key: &String) -> Option<OrderBook> {
        None
    }

    fn delete_all(&self) {

    }
}

#[derive(Debug, PartialEq, Serialize, Deserialize, Clone)]
pub struct OrderBook {
    pub exchange: String
}


#[cfg(test)]
mod redis_repository_tests{
    use crate::repository::{OrderBook, RedisRepository, Repository};

    #[test]
    fn test_save(){
        let repo = RedisRepository{};
        let record = OrderBook{ exchange: String::from("exchange_1") };
        let saved = repo.save(&record.exchange, &record);
        let loaded = repo.find(&saved.exchange)
            .expect("Record should present in repository after saving");
        assert_eq!(saved, loaded);
    }

    #[test]
    fn test_find_all_returns_all(){
        let repo = RedisRepository{};
        let record = OrderBook{ exchange: String::from("exchange_1") };
        repo.save(&record.exchange, &record);
        let books = repo.find_all();
        let expected_books = vec![record];
        assert_eq!(books, expected_books);
    }

    #[test]
    fn test_find_all_empty(){
        let repo = RedisRepository{};
        let books = repo.find_all();
        assert!(books.is_empty());
    }

    #[test]
    fn test_find_all_empty_after_delete_all(){
        let repo = RedisRepository{};
        let record = OrderBook{ exchange: String::from("exchange_1") };
        repo.save(&record.exchange, &record);
        let books = repo.find_all();
        assert!(!books.is_empty());

        repo.delete_all();
        assert!(repo.find_all().is_empty());
    }

}