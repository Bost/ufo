;; TODO https://untangled-web.github.io/untangled/
(ns ^:figwheel-always ufo.client
  (:require
   ;; [ufo [...]] ; is not supported by clojurescript
   [ufo.regexps :as re :refer [dbg dbi t f]]
   [ufo.utils :as utils]
   [ufo.meth :as meth]
   [ufo.state :as state]
   [om.next :as om :refer-macros [defui]]
   [sablono.core :refer-macros [html]]
   [cljs-time.core :as time]))

(enable-console-print!)

(def colsmap
  {:table-id0   [:id :fname :lname]
   :table-id1   [:fname :lname :id]})

;; TODO use this for stats
#_(otd/span
 {:class "line"}
 (let [tbeg (get-in app [id :tbeg])]
   (if-not (nil? tbeg)
     (do
       (otd/span
        {:class "stats"}
        (otd/span
         {:class "cnt"} (str (count (get-in app [id :resp :rows])) "/"
                             (time/in-millis (time/interval tbeg tend))))
        #_(otd/img {:alt "refresh" :src "pic/refresh.png"})
        (otd/span
         {:class "sql"
          :onClick
          (fn []
            (println "TODO println sql command in the browser console"))}
         "sql"))))))

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

(defui RootView
  static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  static om/IQuery
  (query [this] (let [qtr (om/get-query TBodyRow)]
                  `[{:list/rows ~qtr}]))
  Object
  #_(componentWillMount [this] (println "componentWillMount"))
  #_(componentWillUpdate [this nextprops nextstate] (println "componentWillUpdate"))
  #_(componentDidMount [this] (.log js/console "componentDidMount"))
  (render
   [this]
   (let [{:keys [list/rows] :as m} (om/props this)]
     (html
      [:div
       [:div (table rows)]
       #_[:button
        {:onClick
         (fn [e] (println "TODO" "(clojure.core.m/emoize/memo-clear! f args)"))}
        "(clojure.core.memoize/memo-clear! f args)"]
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
                 ;; TODO transact the hash-map to the app-state
                 {:resp (str resp) :tbeg tbeg :tend (time/now)})
               :on-error (fn [resp] (println resp))
               })))}
        "fetch data"]]))))
