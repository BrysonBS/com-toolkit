CREATE TABLE IF NOT EXISTS sys_database_config (
   id INTEGER PRIMARY KEY,
   name TEXT NOT NULL,
   jdbc_url TEXT NOT NULL,
   username TEXT NOT NULL,
   password TEXT NOT NULL,
   is_default INTEGER DEFAULT 0,
   create_time INTEGER DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')),
   update_time INTEGER
);