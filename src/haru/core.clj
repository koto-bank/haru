(ns haru.core
  (:use org.httpkit.server
        hiccup.core)
  (:require
   [garden.core :refer [css]]
   [garden.color :as css-color :refer [rgb]]
   [compojure.handler :refer [site]]
   [compojure.core :refer [defroutes GET context]]

   [korma.db :refer :all]
   [korma.core :refer :all]
   (:gen-class)))

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

(defn db-save-post [& {:keys [title author text image parent-id]
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

(defn comment-css []
  [:div.comment {:background-color (css-color/rgb 214 218 240)
                 :border "1px solid #B7C5D9"
                 :margin-left "1%"
                 :margin-bottom "1%"}])

(defn post-css []
  [:div.post
   {:border "1px solid #B7C5D9"
    :margin "1%"
    :padding "0.5%"}

   [:span {:padding "0.5%"}]
   [:span.author {:color "green"
                  :font-weight "bold"}]
   [:span.name {:color "blue"
                :font-weight "bold"}]
   [:div.post-text {:padding "1%"}]
   (comment-css)])

(defn gen-comment-html [& {:keys [author datetime id text]
                           :or {author "Anon"
                                datetime (java.util.Date.)}}]
  (html [:div.comment
         [:span.author author]
         [:span.datetime datetime]
         [:span.id (str "#" id)]
         [:div.comment-text text]]))

(defn gen-post-html [ & {:keys [name author datetime id text]
                         :or {author "Anon"
                              datetime (java.util.Date.)
                              name ""}}]
  (html [:div.post
         [:span.name name]
         [:span.author author]
         [:span.datetime datetime]
         [:span.id (str "#" id)]
         [:div.post-text text]
         (gen-comment-html :id 1 :text "Test comment")]))

(defn gen-page-html []
  (html [:head [:style (css (post-css))]]
        [:body
         (gen-post-html :name "test" :id 1 :text "Hello /g/")
         (gen-post-html :id 2 :text "Hello /g/ 2")]))

(defn index [req]
  {
   :status 200
   :header {"Content-Type" "text/html"}
   :body (gen-page-html)})

(defroutes routes
  (GET "/" [] index))

(defn -main
  [& args]
  (run-server (site #'routes) {:port 8080}))
