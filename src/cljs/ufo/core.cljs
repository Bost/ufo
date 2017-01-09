(ns ^:figwheel-always ufo.core
  (:require
   [goog.dom :as gdom]
   [om.dom]
   [om.next :as om]
   [cljs-time.core :as time]
   ;; [ufo [...]] ; is not supported by clojurescript
   [ufo.regexps :as re :refer [dbg dbi id in? t f]]
   [ufo.utils :as utils]
   [ufo.client :as cli]
   [ufo.sync :as sync]))

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
  [{:keys [state] :as env} key params]
  {:value (get-vals state key)})

(defmethod mutate 'rows/by-id
  [{:keys [state]} _ {:keys [id v]}]
  {:action
   (fn []
     (let [ks [:rows/by-id id]
           ;; result of get-in must be converted into {}
           old-state (into {} (get-in @state ks))]
       (swap! state update-in ks (fn [] (conj old-state v)))))})

(defmethod read :user
  [{:keys [state] :as env} key {:keys [id] :as params}]
  {:value (get-in @state [:users/by-id id])})

(defmethod read :search/user
  [{:keys [state ast] :as env} key {:keys [query] :as params}]
  (merge
   {:value (get-in @state [key query])}
   (when query ;; a condition on query
     {:search ast})))

(defmethod read :search/results
  [{:keys [state ast] :as env} k {:keys [query]}]
  (merge
   {:value (get @state k [])}
   (when-not (or (clojure.string/blank? query)
                 (< (count query) 3))
     {:search ast})))

(defmethod mutate 'users/by-id
  [{:keys [state]} _ {:keys [id]}]
  {:action
   (fn []
     (let [ks [:users/by-id id]
           ;; get-in must be converted into {}
           old-state (into {} (get-in @state ks))]
       (if (empty? old-state)
         (utils/ednxhr
          {:reqprm {:f :users :id id :log t :nocache t}
           :on-complete
           (fn [resp]
             ;; map returs a lazy sequence therefore doseq must be used
             ;; (map #(add-row! widget %) (:rows resp))
             (doseq [row (:rows resp)]
               (swap! state update-in ks
                      (fn [] (conj old-state row)))))
           ;; TODO transact {:resp (str resp) :tbeg tbeg :tend (time/now)})
           :on-error (fn [resp] (println resp))}))))})

(defmethod mutate 'list/trows
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
  {:search/user []
   :list/tables
   [{:tid   :salaries
     :sqlfn :salaries
     :tname "Salaries"
     :cols [:id :salary :abrev]}
    #_
    {:tid   :users
     :sqlfn :users
     :tname "Users"
     :cols [:id :fname :lname]}]})

;; defonce produces: Encountered two children with the same key, `null`
(def reconciler
  (om/reconciler
   {:state app-state
    :parser (om/parser {:read read :mutate mutate})
    :send (cli/send-to-chan cli/send-chan)
    ;; "I don't know the value of this key, go ask the server"
    :remotes [:search] ;; remote targets - represent remote services
    }))

(om/add-root! reconciler cli/RootView (gdom/getElement "app"))
(om/add-root! sync/reconciler sync/AutoCompleter (gdom/getElement "app-json"))

