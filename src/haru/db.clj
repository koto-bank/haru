(ns haru.db
  (:require [korma.db :refer :all]
            [korma.core :refer :all]))

(defdb db (postgres {:db "haru"
                     :user "haru"
                     :password "12345"}))

(defn init-db []
  (let [schema
        ["
CREATE TABLE IF NOT EXISTS posts(
  id SERIAL PRIMARY KEY,
  title VARCHAR(64),
  author VARCHAR(512),
  text TEXT,
  date TIMESTAMP WITHOUT TIME ZONE,
  image TEXT,
  parentid INTEGER);
"
         "
CREATE TABLE IF NOT EXISTS boards(
  id SERIAL PRIMARY KEY,
  shortname VARCHAR(8) UNIQUE NOT NULL,
  description VARCHAR(128));
"
        "
CREATE TABLE IF NOT EXISTS posts_to_boards_map(
  post_id INTEGER REFERENCES posts,
  board_id INTEGER REFERENCES boards
);
"]]
    (map exec-raw schema)))

(declare boards posts)
(defentity boards
  (pk :id)
  (many-to-many posts :posts_to_boards_map {:lfk :post_id :rfk :board_id}))

(defentity posts
  (pk :id)
  (many-to-many boards :posts_to_boards_map {:lfk :post_id :rfk :board_id}))

(defentity posts-to-boards-map
  (table :posts_to_boards_map))

(defn save-board [& {:keys [short-name description]}]
  (insert boards
          (values {:shortname short-name
                  :description description})))

(defn save-post [& {:keys [title author text image parent-id board-list]
                    :or {title ""
                         author "名無しさん"
                         text ""}}]
  (if (and (empty? text)
           (nil? image))
    (throw (java.lang.RuntimeException.
            "Post must have either image or text")))
  (if (and (nil? parent-id)
           (nil? board-list))
    (throw (java.lang.RuntimeException.
            "Post must be a reply or have a board list")))
  (if (and (not (nil? board-list))
           (nil? image))
    (throw (java.lang.RuntimeException.
            "First post in a thread must have an image")))
  (if (and (not (nil? parent-id)) (not (nil? board-list)))
    (throw (java.lang.RuntimeException.
            "Post can't both be a reply and have a board list")))

  (let [post-id
        (:id
         (insert posts
                 (fields :id)
                 (values
                  {:title title
                   :author author
                   :text text
                   :date (java.sql.Timestamp. (.getTime (java.util.Date.)))
                   :image image
                   :parentid parent-id})))]

    (if-not (nil? board-list)
      (let [board-ids (map :id
                           (select boards
                                   (fields :id)
                                   (where {:shortname [in board-list]})))]
        (if-not (=
                 (count board-ids)
                 (count board-list))
          (throw
           (java.lang.RuntimeException. "FIXME: some boards do not exist")))
        (doseq [board-id board-ids]
          (insert posts-to-boards-map
                  (values {:post_id post-id
                           :board_id board-id})))))))
