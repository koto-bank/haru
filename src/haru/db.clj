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
"]]
    (map exec-raw schema)))

(defentity posts
  (pk :id))

(defn save-post [& {:keys [title author text image parent-id]
                       :or {title ""
                            author "名無しさん"
                            text ""}}]
  (if (and (empty? text)
           (nil? image))
    (throw (java.lang.RuntimeException.
            "Post must have either image or text")))
  (insert posts
          (values
           {:title title
            :author author
            :text text
            :date (java.sql.Timestamp. (.getTime (java.util.Date.)))
            :image image
            :parentid parent-id})))
