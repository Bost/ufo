;; TODO https://untangled-web.github.io/untangled/
(ns ^:figwheel-always ufo.client
  (:require
   [ufo.regexps :as re :refer [dbg dbi t f]]
   [ufo.utils :as utils]
   [ufo.meth :as meth]
   [ufo.state :as state]
   [om.next :as om :refer-macros [defui]]
   [sablono.core :refer-macros [html]]
   [cljs-time.core :as time]

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
          #_(println "Render Person" (-> this om/props :fname))
          (let [{:keys [fname lname] :as props} (om/props this)]
            (html
             [:li
              [:label (str fname " " lname)]]))))

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
          #_(println "Render Person" (-> this om/props :name))
          (let [{:keys [points name] :as props} (om/props this)]
            (html
             [:li
              [:label (str name ", points: " points)]
              [:button {:onClick
                        (fn [e]
                          (om/transact! this
                                        `[(points/increment ~props)]))}
               "+"]
              [:button {:onClick
                        (fn [e]
                          (om/transact! this
                                        `[(points/decrement ~props)]))}
               "-"]]))))

(def person (om/factory Person {:keyfn :name}))

(defui ThreePListView
  Object
  (render [this]
          #_(println "Render ThreePListView" (-> this om/path first))
          (let [list (om/props this)]
            (html
             [:ul
              (map threep list)]))))

(def threep-list-view (om/factory ThreePListView))

(defui ListView
  Object
  (render [this]
          #_(println "Render ListView" (-> this om/path first))
          (let [list (om/props this)]
            (html
             [:ul
              (map person list)]))))

(def list-view (om/factory ListView))


(defn add-person! [widget {:keys [fname lname]}]
  (let [hm {:kws [:fperson/by-fname (keyword fname)]}]
    (om/transact! widget `[(fperson/by-fname
                            ;; ~ means evaluate the sexp before passing
                            ~(assoc hm :v {:fname fname :lname lname}))
                           (list/three ~hm)])))

(defui RootView
  static om/IQuery
  (query
   [this]
   (let [subquery (om/get-query Person)
         qthreep (om/get-query ThreeP)]
     `[{:list/one ~subquery} {:list/two ~subquery} {:list/three ~qthreep}]))

  Object
  (render
   [this]
   #_(println "Render RootView")
   (let [{:keys [list/one list/two list/three]} (om/props this)]
     (html
      [:div
       [:h2 "List A"]
       (list-view one)
       [:h2 "List B"]
       (list-view two)
       [:h2 "List ThreeP"]
       ;; TODO transact from 'outside'
       (threep-list-view three)
       [:button
        {:onClick
         (fn [e]
           (let [fname :users
                 tbeg (time/now)]
             (println "read-key" fname "Searching in DB...")
             (utils/ednxhr
              {:reqprm {:f fname :rowlim 4 :log t :nocache t}
               :on-complete
               (fn [resp]
                 ;; map returs a lazy sequence therefore doseq must be used
                 ;; (map #(add-person! this %) (:rows resp))
                 (doseq [p (:rows resp)]
                   (add-person! this p))
                 (println ":resp"
                            {:resp (str resp) :tbeg tbeg :tend (time/now)}))
               :on-error (fn [resp] (println resp))})))}
        "fetch data"]]))))

(def reconciler
  (om/reconciler
    {:state  state/init-data
     :parser (om/parser {:read state/read :mutate state/mutate})}))
