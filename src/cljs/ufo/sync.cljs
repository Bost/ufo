(ns ufo.sync
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]
            [sablono.core :refer-macros [html]]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom])
  (:import [goog Uri]
           ;; Jsonp creates a new cross domain channel that sends data to the specified host URL
           [goog.net Jsonp]))

(enable-console-print!)

(def base-url
  "http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")

(defn jsonp
  ([uri] (jsonp (chan) uri))
  ([c uri]
   (let [gjsonp (Jsonp. (Uri. uri))]
     ;; put! - Asynchronously puts a val into port
     (.send gjsonp nil (fn [val]
                         (println "val" val)
                         (put! c val)))
     c)))

(defmulti read om/dispatch)

(defmethod read :search/results
  [{:keys [state ast] :as env} k {:keys [query]}]
  (merge
    {:value (get @state k [])}
    (when-not (or (string/blank? query)
                  (< (count query) 3))
      {:search ast})))

(defn result-list [results]
  [:ul {:key "result-list"}
   (map (fn [result] [:li result]) results)])

(defn search-field [auto-complete query]
  (html
   [:input
    {:key "search-field"
     :value query
     :onChange
     (fn [e]
       (om/set-query! auto-complete
                      {:params {:query
                                ;; javascript interop: e[target][value]
                                (.. e -target -value)}}))}]))

(defui AutoCompleter
  static om/IQueryParams (params [_] {:query ""})
  static om/IQuery (query [_] '[(:search/results {:query ?query})])
  Object
  (render
   [this]
   (let [{:keys [search/results]} (om/props this)]
     (html
      [:div
       [:h2 "Autocompleter"]
       (cond->
           [(search-field this (:query (om/get-params this)))]
         (not (empty? results)) (conj (result-list results)))]))))

(defn search-loop [c]
  (go
    ;; callback is provided by om.next itself
    (loop [[query callback] (<! c)] ;; <! takes val from a port
      (let [[_ results] (<! (jsonp (str base-url query)))]
        (callback {:search/results results}))
      (recur (<! c)))))

(defn send-to-chan [c]
  (fn [{:keys [search]} callback]
    (when search
      (let [{[search] :children} (om/query->ast search)
            query (get-in search [:params :query])]
        (put! c [query callback])))))

(def send-chan (chan))

(def reconciler
  (om/reconciler
    {:state   {:search/results []}
     :parser  (om/parser {:read read})
     :send    (send-to-chan send-chan)
     :remotes [:search]}))

(search-loop send-chan)

(om/add-root! reconciler AutoCompleter (gdom/getElement "app"))
