(ns ufo.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(enable-console-print!)

(defn thead [cols]
  (fn []
    [:tr
     (map-indexed (fn [i v]
                    [:th {:key i} (str v)]) cols)]))

(defn tbody []
  (fn []
    (let [loading? (re-frame/subscribe [:loading?])
          emps (re-frame/subscribe [:emps])]
      [:tbody
       (map-indexed
        (fn [i [id-val hm]]
          [:tr {:key (str "tr-" i)}
           (let [id (name id-val)
                 salary-val (:salary hm)
                 salary (if salary-val salary-val ;; auto-onclick
                            (re-frame/dispatch [:id id]))
                 abbrev-val (:abbrev hm)
                 abbrev (if abbrev-val abbrev-val ;; auto-onclick
                            (re-frame/dispatch [:id id]))]
             (for [v (remove nil? [id salary abbrev])]
               [:td (conj {:key (str "tr-" i "-" v)}
                          {:style {:border "1px" :borderStyle "solid"}}
                          {:on-click #(when-not @loading?
                                        (println "on-click" v))})
                v]))])
        @emps)])))

(defn table [id]
  (let [
        table-def (re-frame/subscribe [:tables])
        ;; sqlfn ""
        ;; cols
        ]
    (fn []
      [:div
       [:div (->> @table-def id :name)]
       [:table
        [:thead [thead (->> @table-def id :cols)]]
        [tbody]
        ]])))

(defn loading-throbber
  []
  (let [loading? (re-frame/subscribe [:loading?])]
    (when @loading?
      (do
        #_(println "Loading...")
        [:div.loading
         [:div.three-quarters-loader "Loading..."]]))))

(defn main-panel []
  (fn []
    [:div
     [loading-throbber]
     [table :salaries]]))
