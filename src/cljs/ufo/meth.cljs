(ns ^:figwheel-always ufo.meth
  (:require
   ;; [ufo [...]] ; is not supported by clojurescript
   [ufo.state :as state]
   [ufo.utils :as utils]
   [ufo.regexps :as re :refer [dbg dbi id]]
   [om.next :as om]
   [cljs-time.core :as time]))

(enable-console-print!)

(defn get-people [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmethod state/read :list/one
  [{:keys [state] :as env} key params]
  {:value (get-people state key)})

(defmethod state/read :list/two
  [{:keys [state] :as env} key params]
  {:value (get-people state key)})

(defmethod state/read :list/three
  [{:keys [state] :as env} key params]
  {:value (get-people state key)})

(defmethod state/mutate 'points/increment
  [{:keys [state]} _ {:keys [name]}]
  {:action
   (fn []
     (swap! state update-in
            [:person/by-name name :points]
            inc))})

(defmethod state/mutate 'points/decrement
  [{:keys [state]} _ {:keys [name]}]
  {:action
   (fn []
     (swap! state update-in
            [:person/by-name name :points]
            #(let [n (dec %)] (if (neg? n) 0 n))))})


(defmethod state/mutate 'fperson/by-fname
  [{:keys [state]} _ {:keys [kws v]}]
  {:action (fn [] (swap! state update-in kws (fn [] v)))})

(defmethod state/mutate 'list/three
  [{:keys [state]} _ {:keys [kws person]}]
  {:action (fn [] (swap! state assoc :list/three
                        (conj (:list/three @state) kws)))})

