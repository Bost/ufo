(ns ^:figwheel-always ufo.meth
  (:require
   ;; [ufo [...]] ; is not supported by clojurescript
   [ufo.state :as state]
   [ufo.utils :as utils]
   [ufo.regexps :as re :refer [dbg dbi id in?]]
   [om.next :as om]
   [cljs-time.core :as time]))

(enable-console-print!)

(defn get-vals [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmethod state/read :list/trows
  [{:keys [state] :as env} key params]
  {:value (get-vals state key)})

(defmethod state/mutate 'rows/by-id
  [{:keys [state]} _ {:keys [id v]}]
  {:action
   (fn []
     (let [ks [:rows/by-id id]
           ;; get-in must be converted into {}
           old-state (into {} (get-in @state ks))]
       (swap! state update-in ks (fn [] (conj old-state v)))))})

(defmethod state/mutate 'list/trows
  [{:keys [state]} _ {:keys [kws]}]
  {:action
   (fn []
     (let [kw :list/trows]
       (let [old-list (kw @state)]
         (if (in? old-list kws)
           (println "WARN: mutate" kw "(in? old-list kws); :kws" kws)
           (swap! state assoc kw (conj old-list kws))))))})

;;;;;;;;;;;;;;;;;

;; "List of tables to display on the web page"
(defmethod state/read :list/tables
  [{:keys [state] :as env} key params]
  {:value (get-vals state key)})

#_(defmethod state/mutate 'tables/by-id
  [{:keys [state]} _ {:keys [kws v]}]
  {:action (fn [] (swap! state update-in kws (fn [] v)))})

#_(defmethod state/mutate 'list/tables
  [{:keys [state]} _ {:keys [kws]}]
  {:action (fn [] (set-vals state :list/tables kws))})
