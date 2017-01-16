(ns ^:figwheel-always ufo.utils
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.reader :as reader]
   [goog.events :as events]
   ;; [ufo [...]] ; is not supported by clojurescript
   [ufo.regexps :as re :refer [dbg dbi id]]
   [cljs-time.core :as time]
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

(def uri "http://localhost:3449/")

(def base-url
  #_"http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search="
  (str uri #_"jsonreq/search=" "users/ids="))

(defn send-to-remote
  "prm is a map of remotes and the pending message to be sent"
  [{:keys [search] :as prm} cb]
  (println "search" search)
  (let [{[search-ast] :children} (om.next/query->ast search)
        query (get-in search-ast [:params :query])
        key :search/results
        remote :search]
    (println "prm" prm)
    (println "(om.next/query->ast search-ast)" (om.next/query->ast search))
    (if (empty? query)
      (cb {key []} query remote)
      (ednxhr
       {:reqprm {:f :users :ids query :log true :nocache true}
        :on-complete
        (fn [resp]
          ;; map returs a lazy sequence therefore doseq must be used
          ;; (map #(add-row! widget %) (:rows resp))
          (doseq [row (:rows resp)]
            (let [result {(:id row) row}]
              (cb {key result} query remote))))
        :on-error (fn [resp] (println resp))}))))
