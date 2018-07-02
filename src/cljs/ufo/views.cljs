(ns ufo.views
  (:require [re-frame.core :as re-frame]))

(enable-console-print!)

(defn thead [cols]
  (fn []
    [:tr
     (map-indexed (fn [i v]
                    [:th {:key i} (str v)]) cols)]))

(defn td [i v loading?]
  [:td (conj {:key (str "tr-" i "-" v)}
             {:style {:border "1px" :borderStyle "solid"
                      :backgroundColor
                      (if @(re-frame/subscribe [:active v]) "red")
                      }}
             {:on-click #(when-not loading?
                           (re-frame/dispatch [:toggle v]))})
   v])

(defn tbody []
  (fn []
    (let [loading? (re-frame/subscribe [:loading?])
          emps (re-frame/subscribe [:emps])]
      [:tbody
       (doall
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
              (doall
               (map #(td i % @loading?)
                    (remove nil? [id salary abbrev]))))])
         @emps))])))

(defn table [id]
  (let [table-def (re-frame/subscribe [:tables])]
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
