(ns ^:figwheel-always ufo.regexps
  (:require
   [clojure.string :as s]
   #_[clj-time.core :as time]
   #_[clj-time.format :as timef]))

(def t true)
(def f false)
(def id identity)

(defmacro dbi
  "Identity macro for convenience purposes"
  [body]
  `(let [x# ~body]
     x#))

(defmacro dbg [body]
  `(let [x# ~body]
     (println (str "dbg: (def " (quote ~body) " " x#")"))
     x#))

(defn in?
  "true if seq contains elm"
  [seq elm]
  (boolean (some (fn [e] (= elm e)) seq)))

(defn uid? [x] (if (string? x) true #_(re-seq revm x)))

(defn union-re-patterns
  "Union regex patters"
  [patterns]
  (re-pattern (s/join "|" (map str patterns))))

;; TODO inclausestr: improve surrounding by "'"
(defn inclause
  "The content of '... in (...)' must be separated by comma and space"
  [{:keys [elems contract] :as prm}]
  #_(every? contract elems)
  (let [collstr [] ;; list of contracts for strings/varchars
        separator (if (in? collstr contract) "','" ", ")
        ret (clojure.string/join separator elems)]
    (let [s (if (in? collstr contract) (str "'" ret "'") ret)]
      (clojure.string/replace s #"(([\d']+, ){12})" "$1\n"))))

#_(defn tnow []
  (timef/unparse (timef/formatter "HHmmss.SSS") (time/now)))
#_(defn fntime [v]
  (timef/unparse (timef/formatter "HH:mm dd.MM.yy")
                 (time/date-time v)))

(defn sjoin [coll] (s/join " " coll))
(defn sfilter [pred coll] (seq (filter pred coll)))

