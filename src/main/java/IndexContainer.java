public interface IndexContainer<Key ,Value> {
        public Value get(Key key) ;
        public void put(Key key, Value val);
    }
