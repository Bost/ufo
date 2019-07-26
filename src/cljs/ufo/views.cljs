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
  (re-frame/dispatch [:id id])
  (let [resp (re-frame/subscribe [:resp])]
    (.log js/console "table" (type @resp))
    [:div @resp]))

(defn main-panel []
  (fn []
    [:div
     (when @(re-frame/subscribe [:loading?])
       [:div.loading
        [:div.three-quarters-loader "Loading..."]])
     (table :salaries)]))
