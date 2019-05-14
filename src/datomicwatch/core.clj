(ns datomicwatch.core
  (:require
    [clojure.edn]
    [metrics.jvm.core]
    [metrics.reporters.graphite :as graphite-reporter]
    [metrics.reporters :as rmng]
    [metrics.core]
    [metrics.histograms :as histograms]))

(def state (atom {}))
(def url (or (System/getProperty "datomicwatch.configFile")
             "/etc/datomic.watch.edn"))

(def config (try (-> (or (System/getProperty "datomicwatch.configFile")
                         "/etc/datomic.watch.edn")
                     slurp
                     clojure.edn/read-string)
                 (catch Exception e)))

(def registry (metrics.core/new-registry))

(when (System/getProperty "instrument-jvm")
  (metrics.jvm.core/instrument-jvm registry))

(def reporter (when config
                (rmng/start
                  (graphite-reporter/reporter registry config)
                  10)))

(defn report
  [title value]
  (when-not (get @state title)
    (->> ["datomic" "watch" title]
         (filterv identity)
         (histograms/histogram registry)
         (swap! state assoc title)))
  (histograms/update! (get @state title) value))

(defn callback
  [X]
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
