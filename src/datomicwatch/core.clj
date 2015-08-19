(ns datomicwatch.core
  (:require
    [clojure.edn]
    [metrics.reporters.graphite :as graphite-reporter]
    [metrics.reporters :as rmng]
    [metrics.core]
    [metrics.histograms :as histograms]))

(def state (atom {}))
(def config (try (clojure.edn/read-string (slurp "/etc/datomic.watch.edn")) (catch Exception e)))
(def registry (metrics.core/new-registry))
(def reporter (when config
                (rmng/start
                  (graphite-reporter/reporter registry config)
                  10)))

(defn report
  [title value]
  (println "title: " title " value: " value)
  (when-not (get @state title)
    (swap! state assoc title (histograms/histogram registry ["datomic" "watch" title])))
  (histograms/update! (get @state title) value))

(defn callback
  [X]
  (println "callback: " X)
  (when config
    (doall
      (map (fn [[k v]]
             (if (map? v)
               (if (not= 0 (:count v))
                 (report (str (name k)) (quot (:sum v) (:count v)))
                 (doall (map (fn [[i vv]]
                               (report (str (name k) "." (name i))
                                       vv)) v)))
               (report (name k) v)))
           X))))
