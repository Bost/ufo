(ns ^:figwheel-always ufo.utils
  (:require
   [cljs.reader :as reader]
   [goog.events :as events]
   ;; [ufo [...]] ; is not supported by clojurescript
   [ufo.regexps :as re :refer [dbg dbi id]]
   [cljs-time.core :as time]
   [om.core :as om :include-macros true]
   [om-tools.core :as otc :refer-macros [defcomponent]]
   [om-tools.dom :as otd :include-macros true])
  (:import [goog.net XhrIo]
           [goog.net.EventType]
           [goog.events EventType]))

(enable-console-print!)

(defn ednxhr [{:keys [reqprm on-complete on-error] :as prm}]
  (let [kw (:f reqprm)
        rowlim (or (kw {:users 1 :salaries 10})
                   (let [v 10]
                     (println
                      (str "WARN: rowlim undefined for '" kw "'. Using default val " v))
                     v))]
    (if (and (integer? rowlim)
             (pos? rowlim))
      (let [xhr (XhrIo.)] ;; instantiate basic class for handling XMLHttpRequests.
        (println "Searching in DB for" reqprm "...")
        #_(events/listen
         xhr goog.net.EventType.COMPLETE
         (fn [e]
           (oncomplete (reader/read-string (.getResponseText xhr)))))
        (events/listen xhr goog.net.EventType.SUCCESS
                       (fn [e]
                         (on-complete (reader/read-string (.getResponseText xhr)))))
        (events/listen xhr goog.net.EventType.ERROR
                       (fn [e]
                         (on-error {:error (.getResponseText xhr)})))
        (.send xhr "req" "PUT"
               (when prm (pr-str (conj reqprm {:rowlim rowlim})))
               {"Content-Type" "application/edn; charset=UTF-8"
                "Accept" "application/edn"})))))

;; TODO :tbeg :tend must be inside :resp
