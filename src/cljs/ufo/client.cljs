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
  (ident [this {:keys [id]}]
         [:fperson/by-id id])
  static om/IQuery
  (query [this]
         '[:id :fname :lname])
  Object
  (render [this]
          #_(println "Render Person" (-> this om/props :fname))
          (let [{:keys [id fname lname] :as props} (om/props this)
                style {:style {:border "1px" :borderStyle "solid"}}]
            (html
             [:tr
              [:td style id]
              [:td style fname]
              [:td style lname]]))))

(def threep (om/factory ThreeP {:keyfn :id}))


(defui TCols
  static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  static om/IQuery (query [this] '[:id :fname :lname]))

(defui Th
  "1. {:keyfn ...} can only use keys specified by (om/props this)
2. Values stored under these keys can't be keywords"
  Object
  (render
   [this]
   (let [{:keys [val]} (om/props this)]
     (html [:th val]))))
(def th (om/factory Th {:keyfn :val}))

(defui Td
  "1. {:keyfn ...} can only use keys specified by (om/props this)
2. Values stored under these keys can't be keywords"
  Object
  (render
   [this]
   (let [{:keys [react-key val]} (om/props this)]
     (html [:td (str val)]))))
(def td (om/factory Td {:keyfn :react-key}))

(defui THeadRow
  Object
  (render
   [this]
   (let [{:keys [] :as prm} (om/props this)]
     (html
      [:tr (map (fn [val] (th {:val (str val)}))
                (om/get-query TCols))]))))
(def thead-row (om/factory THeadRow))

(defui TBodyRow
  static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  static om/IQuery (query [this] '[:id :fname :lname])
  Object
  (render
   [this]
   (let [{:keys [id] :as row} (om/props this)]
     (html
      [:tr (map (fn [kw] (td {:react-key (str id "-" (name kw)) :val (kw row)}))
                (om/get-query TCols))]))))
(def tbody-row (om/factory TBodyRow))

(defui Table
  ;; static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  ;; static om/IQuery
  ;; (query [this] (let [qtr (om/get-query TBodyRow)]
  ;;                 `[:id {:list/rows ~qtr}]))
  Object
  (render
   [this]
   (let [rows (om/props this)]
     (html
      [:div
       [:table
        [:thead (thead-row)]
        [:tbody (map tbody-row rows)]]]))))
(def table (om/factory Table))













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
             [:table #_{:style {:border "1px" :borderStyle "solid"}}
              #_[:thead ]
              [:tbody
               (map threep list)]]))))

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


(defn add-person! [widget {:keys [id fname lname] :as prm}]
  (println "prm" prm)
  (let [hm {:kws [:fperson/by-id id]}]
    (om/transact! widget `[(fperson/by-id
                            ;; ~ means evaluate the sexp before passing
                            ~(assoc hm :v {:id id :fname fname :lname lname}))
                           (list/three ~hm)])))

(defui RootView
  static om/IQuery
  (query
   [this]
   (let [subquery (om/get-query Person)
         qthreep (om/get-query ThreeP)
         ;; qtable (om/get-query TCols)
         ]
     `[{:list/one ~subquery} {:list/two ~subquery}
       {:list/three ~qthreep}
       ;; {:list/table ~qtable}
       ]))

  Object
  (render
   [this]
   #_(println "Render RootView")
   (let [{:keys [list/one list/two list/three
                 ;; list/table
                 ]} (om/props this)]
     (html
      [:div
       ;; [:h2 "List A"]
       ;; (list-view one)
       ;; [:h2 "List B"]
       ;; (list-view two)
       [:h2 "Table ThreeP"]
       ;; TODO transact from 'outside'
       (threep-list-view three)
       [:h2 "Table"]
       ;; (table table)
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
