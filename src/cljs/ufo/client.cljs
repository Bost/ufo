;; TODO https://untangled-web.github.io/untangled/
(ns ^:figwheel-always ufo.client
  (:require
   [ufo.regexps :as re :refer [dbg dbi t f]]
   [ufo.utils :as utils]
   [ufo.meth :as meth]
   [ufo.state :as state]
   [om.next :as om :refer-macros [defui]]
   [sablono.core :refer-macros [html]]

   [goog.dom :as gdom]
   [om.dom :as dom]))

(enable-console-print!)

(defui ThreeP
  static om/Ident
  (ident [this {:keys [fname]}]
         [:fperson/by-fname fname])
  static om/IQuery
  (query [this]
         '[:fname :lname])
  Object
  (render [this]
          (println "Render Person" (-> this om/props :fname))
          (let [{:keys [fname lname] :as props} (om/props this)]
            (dom/li nil
                    (dom/label nil (str fname " " lname))))))

(def threep (om/factory ThreeP {:keyfn :fname}))

(defui Person
  static om/Ident
  (ident [this {:keys [name]}]
    [:person/by-name name])
  static om/IQuery
  (query [this]
    '[:name :points :age])
  Object
  (render [this]
    (println "Render Person" (-> this om/props :name))
    (let [{:keys [points name] :as props} (om/props this)]
      (dom/li nil
        (dom/label nil (str name ", points: " points))
        (dom/button
          #js {:onClick
               (fn [e]
                 (om/transact! this
                   `[(points/increment ~props)]))}
          "+")
        (dom/button
          #js {:onClick
               (fn [e]
                 (om/transact! this
                   `[(points/decrement ~props)]))}
          "-")))))

(def person (om/factory Person {:keyfn :name}))

(defui ThreePListView
  Object
  (render [this]
          (println "Render ThreePListView" (-> this om/path first))
          (let [list (om/props this)]
            (apply dom/ul nil
                   (map threep list)))))

(def threep-list-view (om/factory ThreePListView))

(defui ListView
  Object
  (render [this]
    (println "Render ListView" (-> this om/path first))
    (let [list (om/props this)]
      (apply dom/ul nil
        (map person list)))))

(def list-view (om/factory ListView))

(defui RootView
  static om/IQuery
  (query [this]
         (let [subquery (om/get-query Person)
               qthreep (om/get-query ThreeP)]
      `[{:list/one ~subquery} {:list/two ~subquery} {:list/three ~qthreep}]))
  Object
  (render [this]
    (println "Render RootView")
    (let [{:keys [list/one list/two list/three]} (om/props this)]
      (apply dom/div nil
        [(dom/h2 nil "List A")
         (list-view one)
         (dom/h2 nil "List B")
         (list-view two)
         (dom/h2 nil "List ThreeP")
         ;; TODO transact from 'outside'
         #_(dom/button
          #js {:onClick
               (fn [e]
                 (om/transact! this
                               `[(points/increment ~props)]))}
          "+")
         (threep-list-view three)]))))

(def reconciler
  (om/reconciler
    {:state  state/init-data
     :parser (om/parser {:read state/read :mutate state/mutate})}))
