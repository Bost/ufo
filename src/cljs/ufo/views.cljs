(ns ufo.views
  (:require [re-frame.core :as re-frame]))

(enable-console-print!)

(defn thead [cols]
  [:tr
   (map-indexed (fn [i v]
                  [:th {:key i} (str v)]) cols)])

(defn td [i v loading?]
  [:td (conj {:key (str "tr-" i "-" v)}
             {:style {:border "1px" :borderStyle "solid"
                      :backgroundColor
                      (if @(re-frame/subscribe [:active v]) "red")
                      }}
             {:on-click #(when-not loading?
                           (re-frame/dispatch [:toggle v]))})
   v])

(defn table [id]
  (let [table-def (re-frame/subscribe [:tables])]
    [:div
     [:div (->> @table-def id :name)]
     [:table
      [:thead (thead (->> @table-def id :cols))]
      [:tbody
       (let [loading? (re-frame/subscribe [:loading?])]
         (doall
          (map-indexed
           (fn [i [id-val hm]]
             [:tr {:key (str "tr-" i)}
              (let [id (name id-val)
                    salary-val (:salary hm)
                    salary (or salary-val ;; auto-onclick
                               (re-frame/dispatch [:id id]))
                    abbrev-val (:abbrev hm)
                    abbrev (or abbrev-val ;; auto-onclick
                               (re-frame/dispatch [:id id]))]
                (doall
                 (map #(td i % @loading?)
                      (remove nil? [id salary abbrev]))))])
           @(re-frame/subscribe [:emps]))))]
      ]]))

(defn main-panel []
  (fn []
    [:div
     (when @(re-frame/subscribe [:loading?])
       [:div.loading
        [:div.three-quarters-loader "Loading..."]])
     (table :salaries)]))
