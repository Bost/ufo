(ns ^:figwheel-always ufo.meth
  (:require
   ;; [ufo [...]] ; is not supported by clojurescript
   [ufo.state :as state]
   [ufo.utils :as utils]
   [ufo.regexps :as re :refer [dbg dbi id in?]]
   [om.next :as om]
   [cljs-time.core :as time]))

(enable-console-print!)

(defn get-people [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmethod state/read :list/tvals
  [{:keys [state] :as env} key params]
  {:value (get-people state key)})


(defmethod state/mutate 'rows/by-id
  [{:keys [state]} _ {:keys [kws v]}]
  {:action (fn [] (swap! state update-in kws (fn [] v)))})

(defmethod state/mutate 'list/tvals
  [{:keys [state]} _ {:keys [kws person]}]
  {:action
   (fn []
     (let [old-list (:list/tvals @state)]
       (if (in? old-list kws)
         (println "WARN: mutate list/tvals (in? old-list kws); :kws" kws)
         (swap! state assoc :list/tvals (conj old-list kws)))))})


;;;;;;;;;;;;;;;;;

;; "List of tables to display on the web page"
(defmethod state/read :list/tables
  [{:keys [state] :as env} key params]
  {:value (get-people state key)})

(defmethod state/mutate 'tables/by-id
  [{:keys [state]} _ {:keys [kws v]}]
  {:action (fn [] (swap! state update-in kws (fn [] v)))})

(defmethod state/mutate 'list/tables
  [{:keys [state]} _ {:keys [kws person]}]
  {:action
   (fn []
     (let [old-list (:list/tables @state)]
       (if (in? old-list kws)
         (println "WARN: mutate list/tables (in? old-list kws); :kws" kws)
         (swap! state assoc :list/tables (conj old-list kws)))))})

