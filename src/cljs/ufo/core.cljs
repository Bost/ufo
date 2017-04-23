(ns ^:figwheel-always ufo.core
  (:require
   [goog.dom :as gdom]
   [om.next :as om]
   [clojure.string :as string]
   [cljs-time.core :as time]
   ;; [ufo [...]] ; is not supported by clojurescript
   [ufo.regexps :as re :refer [dbg dbi id in? t f]]
   [ufo.utils :as utils]
   [ufo.client :as cli]))

(enable-console-print!)

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

;; Queries:
;; Properties (SELECT name,address)
;; Joins (think sub-queries, FROM (SELECT ...))
;; Idents (think foreign keys)
;; Unions (think UNION)
;; Mutations (think INSERT/UPDATE/DELETE)

;; (def app-state { :keyword { id real-information }})

(defn get-vals [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmethod read :list/trows
  [{:keys [state] :as env} kw params]
  {:value (get-vals state kw)})

(defmethod mutate 'rows/by-id
  [{:keys [state]} kwx {:keys [id v]}]
  {:action
   (fn [] (let [kw (keyword kwx)
               ks [kw id]
               ;; result of get-in must be converted into {}
               old-state (into {} (get-in @state ks))]
           (swap! state update-in ks (fn [] (conj old-state v)))))})

(defmethod read :user
  [{:keys [state] :as env} key {:keys [id] :as params}]
  {:value (get-vals state key)})

(defmethod read :all-results
  [{:keys [state] :as env} key {:keys [id] :as params}]
  #_(println "(get-in @state [:search/results])" (get-in @state [:search/results]))
  {:value (get-in @state [:search/results])})

(defmethod read :search/user
  [{:keys [state ast] :as env} k {:keys [query] :as params}]
  (merge {:value (get @state k [])}
         (when query
           {:search ast})))

(defmethod mutate 'users/by-id
  [{:keys [state]} kwx {:keys [id]}]
  {:action
   (fn [] (let [kw (keyword kwx)
               ks [kw id]
               ;; get-in must be converted into {}
               old-state (get-in @state ks)]
           (if (empty? old-state)
             (utils/ednxhr
              {:reqprm {:f :users :id id :log true :nocache true}
               :on-complete
               (fn [resp]
                 ;; map returns a lazy sequence therefore doseq must be used
                 ;; (map #(add-row! widget %) (:rows resp))
                 (doseq [row (:rows resp)]
                   (swap! state update-in ks (fn [] (conj old-state row)))))
               :on-error (fn [resp] (println resp))}))))})

(defmethod mutate 'list/trows
  [{:keys [state]} kwx {:keys [kws]}]
  {:action
   (fn [] (let [kw (keyword kwx)
               old-list (get @state kw [])]
           (if (in? old-list kws)
             (println "WARN: mutate" kw "(in? old-list kws); :kws" kws)
             (swap! state assoc kw (conj old-list kws)))))})

(defmethod read :search/results
  [{:keys [state ast] :as env} k {:keys [query]}]
  (let [ks [k #_query]
        old-state (get-in @state [:search/results])
        value (into {} (get-in @state ks))]
    #_(swap! state update-in ks (fn [] (conj value h1)))
    (let [h1 {:value (get-in @state ks)}
          h2 {:search ast}
          r (merge h1 h2)]
      r)))

;; "List of tables to display on the web page"
(defmethod read :list/tables
  [{:keys [state] :as env} key params]
  {:value (get-vals state key)})

;; "List of tables to display on the web page"
(defmethod read :list/tables
  [{:keys [state] :as env} key params]
  {:value (get-vals state key)})

#_(defmethod mutate 'tables/by-id
  [{:keys [state]} _ {:keys [kws v]}]
  {:action (fn [] (swap! state update-in kws (fn [] v)))})

#_(defmethod mutate 'list/tables
  [{:keys [state]} _ {:keys [kws]}]
  {:action (fn [] (set-vals state :list/tables kws))})

(def app-state
  {:search/results []
   :search/user []
   :list/trows []
   :list/tables
   [{:id   :salaries
     :sqlfn :salaries
     :tname "Salaries"
     :cols [:id :salary :abrev]}
    #_{:id   :users
       :sqlfn :users
       :tname "Users"
       :cols [:id :fname :lname]}]})

;; defonce produces: Encountered two children with the same key, `null`
(def reconciler
  (om/reconciler
   {:state app-state
    :parser (om/parser {:read read :mutate mutate})
    :send (fn [hm callback]
            #_(println "hm" hm)
            #_(println "callback" callback)
            (utils/send-to-remote hm callback))
    :remotes [:remote :search] ;; remote targets - represent remote services
    :merge (fn [reconciler state novelty query]
             #_(println "reconciler" reconciler)
             #_(println "state" state)
             #_(println "novelty" novelty)
             #_(println "query" query)
             (om/default-merge reconciler state novelty query))
    }))

(om/add-root! reconciler cli/RootView (gdom/getElement "app"))
