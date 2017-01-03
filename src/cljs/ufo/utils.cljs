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

(defn rowlim [kw]
  {:rowlim
   (or (kw {:users 2 :salaries 2})
       (do
         (let [v 10]
           (println
            (str "WARN: rowlim undefined for kw '" kw "'. Using default val " v))
           v)))})

(defn ednxhr [{:keys [reqprm on-complete on-error] :as prm}]
  (let [f (:f reqprm)
        rowlim (rowlim f)
        rowlim-val (:rowlim rowlim)]
    (if (and (integer? rowlim-val)
             (pos? rowlim-val))
      (let [xhr (XhrIo.)] ;; instantiate basic class for handling XMLHttpRequests.
        (println "Searching in DB for" f "...")
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
               (when prm (pr-str (conj reqprm rowlim)))
               {"Content-Type" "application/edn; charset=UTF-8"
                "Accept" "application/edn"})))))

;; TODO :tbeg :tend must be inside :resp
