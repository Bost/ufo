(ns ufo.sync
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]
            [sablono.core :refer-macros [html]]
            [om.next :as om :refer-macros [defui]]
            [ufo.regexps :as re :refer [dbg dbi id t f]]
            [ufo.utils :as utils]
            [om.dom :as dom])
  (:import [goog Uri]
           ;; Jsonp creates a new cross domain channel that sends data to the specified host URL
           [goog.net Jsonp]))

(enable-console-print!)

(def uri "http://localhost:3449/")

(def base-url
  #_"http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search="
  (str uri "jsonreq/"))

(defn jsonp
  ([uri] (jsonp (chan) uri))
  ([c uri]
   ;; put! - Asynchronously puts a val into port
   (.send (Jsonp. (Uri. uri))
          nil                    ;; payload
          (fn [val] (put! c val)) ;; reply-callback
          (fn [val] (println "error-callback" "val" val))
          nil                    ;; callback param value
          )
   c))

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

(defn search-field [widget query]
  (html
   [:input
    {:key "search-field"
     :value query
     :onChange
     (fn [e]
       (om/set-query! widget
                      ;; javascript interop: e[target][value]
                      {:params {:query (.. e -target -value)}}))}]))

(defui AutoCompleter
  static om/IQueryParams (params [_] {:query ""})
  static om/IQuery (query [_] '[(:search/results {:query ?query})])
  Object
  (render
   [this]
   (let [{:keys [search/results]} (om/props this)]
     (println "AutoCompleter" "(om/props this)" (om/props this))
     (html
      [:div
       [:h2 (str "base-url: " base-url)]
       (cond->
           [(search-field this (:query (om/get-params this)))]
         (not (empty? results)) (conj (result-list results)))]))))
(def auto-completer (om/factory AutoCompleter #_{:keyfn :tid}))

(defn search-loop [c]
  (go
    ;; callback is provided by om.next itself
    (loop [[query callback] (<! c)] ;; <! takes val from a port
      (let [fetched-vals (<! (jsonp (str base-url query)))
            [_ results] fetched-vals]
        (callback {:search/results results}))
      (recur (<! c)))))

(defn send-to-chan [c]
  (fn [{:keys [search] :as prm} callback]
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

#_(om/add-root! reconciler AutoCompleter (gdom/getElement "app"))
