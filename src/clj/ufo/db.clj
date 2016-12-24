(ns ufo.db
  (:require [ufo
             [sql :as sql]
             [dbcon :as dbcon]]))

(defn
  ^{:doc {:last-change "" ; obtained by (clj-time.core/now)
          :runs-since-last-change 0}
    :test (fn [])
    :comment ""}
  users [prm] (dbcon/sdbquery (sql/users prm)))


(defn salaries [prm] (dbcon/sdbquery (sql/salaries prm)))
