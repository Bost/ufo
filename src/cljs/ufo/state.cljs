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
  {
   :rows/by-id {10001 {:id 10001 :fname "Georgi" :lname "Facello"}
                10002 {:id 10002 :fname "Bezalel" :lname "Simmel"}}

   :list/rows [{:id 10001}
               {:id 10002}]})

(defonce reconciler
  (om/reconciler {:state  app-state
                  :parser (om/parser {:read read :mutate mutate})}))

(def init-data
  {:list/one [{:name "JohnY" :points 0}
              {:name "Mary" :points 0}
              {:name "Bob"  :points 0}]
   :list/two [{:name "Mary" :points 0 :age 27}
              {:name "Gwen" :points 0}
              {:name "Jeff" :points 0}]
   :list/three
   [{:fname "Georgi"  :lname "Facello"}
    {:fname "Bezalel" :lname "Simmel"}
    ]})
