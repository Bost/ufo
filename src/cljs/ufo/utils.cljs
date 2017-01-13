(ns ^:figwheel-always ufo.utils
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.reader :as reader]
   [goog.events :as events]
   ;; [ufo [...]] ; is not supported by clojurescript
   [ufo.regexps :as re :refer [dbg dbi id]]
   [cljs-time.core :as time]
   #_[om.core :as om :include-macros true]
   [om-tools.core :as otc :refer-macros [defcomponent]]
   [om-tools.dom :as otd :include-macros true]
   [cljs.core.async :as async :refer [<! >! put! chan]])
  (:import
   [goog.net XhrIo]
   [goog.net.EventType]
   [goog.events EventType]
   [goog Uri]
   ;; Jsonp creates a new cross domain channel that sends data to the specified host URL
   [goog.net Jsonp]))

(enable-console-print!)

(defn ednxhr
  "Send an asynchronous HTTP request using XhrIo's send() Instance Method"
  [{:keys [reqprm on-complete on-error] :as prm}]
  (let [kw (:f reqprm)
        rowlim (or (kw {:users 1 :salaries 3})
                   (let [v 10]
                     (println (str "WARN: rowlim undefined for '" kw "'."
                                   " Using default val " v))
                     v))]
    (if (and (integer? rowlim)
             (pos? rowlim))
      ;; instantiate basic class for handling XMLHttpRequests.
      (let [xhr (XhrIo.)]
        #_
        (events/listen xhr goog.net.EventType.COMPLETE
                       (fn [e]
                         (oncomplete (reader/read-string
                                      (.getResponseText xhr)))))
        (events/listen xhr goog.net.EventType.SUCCESS
                       (fn [e]
                         (on-complete (reader/read-string
                                       (.getResponseText xhr)))))
        (events/listen xhr goog.net.EventType.ERROR
                       (fn [e]
                         (on-error {:error
                                    (.getResponseText xhr)})))
        (println "Sending request" reqprm "...")
        (let [url "req"
              opt_method "PUT" ; defaults to "GET"
              opt_content (when prm (pr-str (conj reqprm {:rowlim rowlim})))
              opt_headers {"Content-Type" "application/edn; charset=UTF-8"
                           "Accept" "application/edn"}]
          (.send xhr url opt_method opt_content opt_headers))))))

;; TODO :tbeg :tend must be inside :resp

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

(def uri "http://localhost:3449/")

(def base-url
  #_"http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search="
  (str uri #_"jsonreq/search=" "users/ids="))

(defn search-loop [c]
  (go
    (loop [[query cb remote] (<! c)]
      (if-not (empty? query)
        (let [ret (<! (jsonp (str base-url query)))
              hm (js->clj ret :keywordize-keys true)
              ;; [_ results] ret
              results (->> hm first :rows first)]
          #_(println "search-loop" "results" results)
          (cb {:search/results results} query remote))
        (cb {:search/results []} query remote))
      (recur (<! c)))))

(defn send-to-chan [c]
  (fn [{:keys [search]} cb]
    (when search
      (let [{[search] :children} (om.next/query->ast search)
            query (get-in search [:params :query])]
        (put! c [query cb :search])))))

(def send-chan (chan))
