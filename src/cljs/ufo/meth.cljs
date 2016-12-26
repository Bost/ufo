(ns ^:figwheel-always ufo.meth
  (:require
   ;; [ufo [...]] ; is not supported by clojurescript
   [ufo.state :as state]
   [ufo.utils :as utils]
   [ufo.regexps :as re :refer [dbg dbi id in?]]
   [om.next :as om]
   [cljs-time.core :as time]))

(enable-console-print!)

(defn get-val [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defn set-vals [state kw kws]
  (let [old-list (kw @state)]
    (println "kw" kw "kws" kws)
    (if (in? old-list kws)
      (println "WARN: mutate " kw " (in? old-list kws); :kws" kws)
      (swap! state assoc (conj old-list kws)))))

(defmethod state/read :list/trows
  [{:keys [state] :as env} key params]
  {:value (get-val state key)})

(defmethod state/mutate 'rows/by-id
  [{:keys [state]} _ {:keys [kws v]}]
  {:action (fn [] (swap! state update-in kws (fn [] v)))})

(defmethod state/mutate 'list/trows
  [{:keys [state]} _ {:keys [kws]}]
  {:action (fn [] (set-vals state :list/trows kws))})

;;;;;;;;;;;;;;;;;

;; "List of tables to display on the web page"
(defmethod state/read :list/tables
  [{:keys [state] :as env} key params]
  {:value (get-val state key)})

(defmethod state/mutate 'tables/by-id
  [{:keys [state]} _ {:keys [kws v]}]
  {:action (fn [] (swap! state update-in kws (fn [] v)))})

(defmethod state/mutate 'list/tables
  [{:keys [state]} _ {:keys [kws]}]
  {:action (fn [] (set-vals state :list/tables kws))})

;;;;;;;;;;;;;;;;;

(defmethod state/read :list/cols
  [{:keys [state] :as env} key params]
  {:value (get-val state key)})

(defmethod state/mutate 'cols/by-id
  [{:keys [state]} _ {:keys [kws v]}]
  {:action (fn [] (swap! state update-in kws (fn [] v)))})

(defmethod state/mutate 'list/cols
  [{:keys [state]} _ {:keys [kws]}]
  {:action (fn [] (set-vals state :list/cols kws))})
