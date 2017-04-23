(ns ufo.handlers
  (:require [cljs.reader :as reader]
            [goog.events :as events]
            [re-frame.core :as re-frame]
            [ufo.db :as db])
  (:import goog.net.XhrIo))

(enable-console-print!)

(defn ednxhr
  "Send an asynchronous HTTP request using XhrIo's send() Instance Method"
  [{:keys [reqprm on-complete on-error] :as prm}]
  (let [
        ;; reqprm {:f :users, :ids [10010], :log true, :nocache true}
        kw (:f reqprm)
        rowlim (or (kw {:users 1 :salaries 2})
                   (let [v 10]
                     (println (str "WARN: rowlim undefined for '" kw "'."
                                   " Using default val " v))
                     v))]
    (if (and (integer? rowlim)
             (pos? rowlim))
      ;; instantiate basic class for handling XMLHttpRequests.
      (let [xhr (XhrIo.)]
        (events/listen xhr goog.net.EventType.SUCCESS
                       (fn [e]
                         (on-complete (reader/read-string
                                       (.getResponseText xhr)))))
        (events/listen xhr goog.net.EventType.ERROR
                       (fn [e]
                         (on-error {:error
                                    (.getResponseText xhr)})))
        (println "Sending request" reqprm "...")
        (let [url
              #_"http://10.90.20.167:3450/req"
              "req" ;; for localhost
              opt_method "PUT" ; defaults to "GET"
              opt_content (when prm (pr-str (conj reqprm {:rowlim rowlim})))
              opt_headers {"Content-Type" "application/edn; charset=UTF-8"
                           "Accept" "application/edn"}]
          ;;.send  returns nil
          (.send xhr url opt_method opt_content opt_headers))))))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/register-handler
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/register-handler
 :set-github-id
 (fn [db [_ github-id]]
   (re-frame/dispatch [:fetch-abbrevs github-id])
   (re-frame/dispatch [:fetch-salaries github-id])
   (assoc-in db [:user :github-id] github-id)))

(re-frame/register-handler
 :fetch-salaries
 (fn [db [_ github-id]]
   (let [id github-id]
     (ednxhr
      {:reqprm {:f :salaries :ids [id] :log true :nocache true}
       :on-complete (fn [resp] (re-frame/dispatch [:emp-salaries resp]))
       :on-error (fn [resp] (println ":on-error" resp))}))
   (-> db
       (assoc :loading? true)
       (assoc :error false))))

(re-frame/register-handler
 :fetch-abbrevs
 (fn [db [_ github-id]]
   (let [id github-id]
     (ednxhr
      {:reqprm {:f :users :ids [id] :log true :nocache true}
       :on-complete (fn [resp] (re-frame/dispatch [:emp-abbrevs resp]))
       :on-error (fn [resp] (println ":on-error" resp))}))
   (-> db
       (assoc :loading? true)
       (assoc :error false))))

(defn abbrev [name]
  (if name
    (subs name 0 (min 2 (count name)))))

(re-frame/register-handler
 :emp-salaries
 (fn [db [_ resp]]
   (let [row (->> resp :rows first)]
     (let [id (->> row :id)
           salary (->> row :salary)]
       (-> db
           (assoc :loading? false)
           (assoc-in [:emps (keyword (str id)) :salary]
                     salary))))))

(re-frame/register-handler
 :emp-abbrevs
 (fn [db [_ resp]]
   (let [row (->> resp :rows first)]
     (let [id (->> row :id)
           fname (->> row :fname)
           lname (->> row :lname)]
       (-> db
           (assoc :loading? false)
           (assoc-in [:emps (keyword (str id)) :abbrev]
                     (str (abbrev fname) (abbrev lname))))))))

(re-frame/register-handler
 :process-user-response
 (fn [db [_ response]]
   (-> db
       (assoc :loading? false)
       (assoc-in [:user :profile] (js->clj response)))))

(re-frame/register-handler
 :process-repo-response
 (fn [db [_ response]]
   (-> db
       (assoc :loading? false)
       (assoc-in [:user :repos] (js->clj response)))))

(re-frame/register-handler
 :bad-response
 (fn [db [_ _]]
   (-> db
       (assoc :loading? false)
       (assoc :error true)
       (assoc-in [:user :repos] [])
       (assoc-in [:user :profile] {}))))