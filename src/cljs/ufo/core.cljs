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
                 ;; map returs a lazy sequence therefore doseq must be used
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
  (merge {:value (get @state k [])}
         (when query
           {:search ast})))

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
    :send utils/send-to-remote
    :remotes [:remote :search] ;; remote targets - represent remote services
    }))

(om/add-root! reconciler cli/RootView (gdom/getElement "app"))
