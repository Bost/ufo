(ns ufo.views
  (:require
   [katex :as k :refer [render renderMathInElement renderToString]]
   [re-frame.core :as re-frame]))

(enable-console-print!)

(defn ex [txt ktx]
  [:span {:class "m"
          :ktx (k/renderToString ktx #_(clj->js {:strict false}))
          :txt txt} txt])

(def ring-operator-supported "∘")
(def white-bullet-unsupported "◦")
(def increment-unsupported "∆")

(def replacements
  [
   [white-bullet-unsupported "\\circ"]
   [ring-operator-supported "\\circ"]
   #_[increment-unsupported "\\Delta"]
   [increment-unsupported "Δ" #_"\\Delta"]
   #_[white-bullet-unsupported ring-operator-supported]
   ["{" "\\{"]
   ["}" "\\}"]
   ["->" "\\rarr"]
   ["<-" "\\larr"]
   ["|" "\\mid"]
   ["•" "\\bullet"]
   ["~" "\\thicksim"]
   ;; this is a hack
   [" α " "~α~"]
   [" α" "~α"]])

(def replace-all
  (fn [exp replacements]
    (loop [rec-exp exp
           rec-replacements replacements
           acc 0]
      (if (or (> acc 100) ;; do not run forever if something's screwed
              (empty? rec-replacements))
        rec-exp
        (let [[src dst] (first rec-replacements)]
          (recur
           (clojure.string/replace rec-exp src dst)
           (rest rec-replacements)
           (inc acc)))))))

(defn e
  ([txt    ] (ex txt (replace-all txt replacements)))
  ([txt ktx] (ex txt ktx)))

(defn render-math [render? el]
  ;; see k/renderMathInElement, (k/render exp3 el)
  ;; (.log js/console (.getAttribute el "txt"))
  ;; (.log js/console (.getAttribute el "ktx"))
  (set! (.-innerHTML el)
        (if render?
          (.getAttribute el "ktx")
          (.getAttribute el "txt"))))

(defn doall-render-math []
  (let [render-math? @(re-frame/subscribe [:notes/render-math-state])]
    #_(.log js/console "render-math?" render-math?)
    (doall
     (let [html-coll (.getElementsByClassName js/document "m")
           elems (array-seq html-coll)]
       (.log js/console "render-math in" (count elems) "elems")
       (map #(render-math render-math? %) elems)))))

(defn ui [{:keys [id title content]}]
  #_(.log js/console "id" id)
  (let [open? @(re-frame/subscribe [:notes/panel-state id])
        content-id (str "content" id)]
    ;; (.log js/console "open?" open?)
    [:div {:key id}
     [:button
      (conj
       {:class "collapsible"}
       {:on-click (fn [e]
                    (let [elem         (js/document.getElementById content-id)
                          style        (.-style elem)
                          comp-style   (.getComputedStyle js/window elem)
                          comp-display (.-display comp-style)
                          new-display  (if (= "none" comp-display) "block" "none")]
                      (set! (.-display style) new-display)))})
      title]
     [:span (conj {:id content-id}
                  {:class "content"}) content]]))

#_(defn main-panel []
  (fn []
    [:div
     (when @(re-frame/subscribe [:loading?])
       [:div.loading
        [:div.three-quarters-loader "Loading..."]])
     (table :salaries)]))

(re-frame/reg-event-db
 :notes/toggle-render-math
 (fn [db [_ _]]
   (update-in db [:render-math] not)))

(re-frame/reg-event-db
 :notes/toggle-panel
 (fn [db [_ id]]
   (update-in db [:open-panels id] not)))

(re-frame/reg-sub
 :notes/render-math-state
 (fn [db [_ _]]
   ;; (.log js/console "db" (pr-str db))
   (get-in db [:render-math])))

(re-frame/reg-sub
 :notes/panel-state
 (fn [db [_ id]]
   ;; (.log js/console "db" (pr-str db))
   (get-in db [:open-panels id])))

(defn adjust-exp [i vhm]
  (if (= :exp (:type vhm))
    [:span {:key i} [e (str (:val vhm))]]
    (:val vhm)))

(defn main-panel []
  #_[:div {:class "language-klipse"}
     [input-ui]
     [compile-cljs-ui]
     [evaluate-clj-ui]
     [evaluate-js-ui]
     #_"(identity 1)"]
  (re-frame/dispatch [:id :salaries])
  (let [content (re-frame/subscribe [:resp])]
    [:div
     [:button {:on-click (fn []
                           (doall-render-math)
                           (re-frame/dispatch [:notes/toggle-render-math]))}
      "(doall-render-math)"]
     (doall
      (map-indexed
       (fn [i hm] (ui (conj {:id i}
                           (->> hm
                                (map (fn [[k val-hms]]
                                       {k (map-indexed adjust-exp val-hms)}))
                                (reduce conj)))))
       @content))
     #_[display-re-pressed-example]]))
