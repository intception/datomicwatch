(defproject datomicwatch "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [metrics-clojure "2.7.0" :exclusions [org.clojure/clojure]]
                 [metrics-clojure-jvm "2.7.0" :exclusions [org.clojure/clojure]]
                 [metrics-clojure-graphite "2.7.0" :exclusions [org.clojure/clojure]]])
