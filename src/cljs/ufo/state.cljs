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
  {:list/tables
   [{:tid   :salaries
     :sqlfn :salaries
     :tname "Salaries"
     :cols [:id :salary]}
    #_{:tid   :users
     :sqlfn :users
     :tname "Users"
     :cols [:id :fname :lname]}]})

;; defonce produces: Encountered two children with the same key, `null`
(def reconciler (om/reconciler
                 {:state app-state
                  :parser (om/parser {:read read :mutate mutate})}))
