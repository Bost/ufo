(ns github-profile.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(enable-console-print!)

(defn thead-row []
  (fn []
    [:tr
     [:th "th-0"]
     [:th "th-1"]]))

(defn tbody-row []
  (fn []
    (let [td-vals ["val-0" "val-1"]]
      [:tr (map-indexed
            (fn [i v]
              [:td
               (conj {:key i}
                     {:style {:border "1px" :borderStyle "solid"}}
                     {:onClick (fn [_] (println "onClick" v))})
               v]) td-vals)])))

(defn table []
  (let [id "id"
        tname "table-name"
        ;; sqlfn ""
        ;; cols
        ]
    (fn []
      [:div
       [:div tname]
       [:table
        [:thead [thead-row]]
        [:tbody
         [tbody-row]
         [tbody-row]]
        ]])))

(defn main-panel []
  (fn []
    [:div
     [table]]))
