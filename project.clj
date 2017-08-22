(defproject haru "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.3.0-alpha2"]
                 [compojure "1.6.0"]
                 [ring/ring "1.6.2"]

                 [hiccup "1.0.5"]
                 [garden "1.3.2"]

                 [com.novemberain/monger "3.1.0"]]
  :main ^:skip-aot haru.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
