(ns ^:figwheel-always ufo.state
  (:require [om.next :as om]))

(enable-console-print!)

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

;; Queries:
;; Properties (SELECT name,address)
;; Joins (think sub-queries, FROM (SELECT ...))
;; Idents (think foreign keys)
;; Unions (think UNION)
;; Mutations (think INSERT/UPDATE/DELETE)

;; (def app-state { :keyword { id real-information }})

(def app-state
  {:list/vals [{:id 10001 :fname "Jeffry" :lname "Glacons"}]})

(defonce reconciler
  (om/reconciler {:state  app-state
                  :parser (om/parser {:read read :mutate mutate})}))
