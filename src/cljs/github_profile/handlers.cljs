(ns github-profile.handlers
  (:require
   [cljs.reader :as reader]
   [goog.events :as events]
   #_[ajax.core :refer [GET]]
   [re-frame.core :as re-frame]
   [github-profile.db :as db])
  (:import
   [goog.net XhrIo]
   [goog.net.EventType]
   #_[goog.events EventType]))

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
        (println "xhr" xhr)
        (let [url
              "http://10.90.20.167:3450/req"
              #_"req"
              opt_method "PUT" ; defaults to "GET"
              opt_content (when prm (pr-str (conj reqprm {:rowlim rowlim})))
              opt_headers {"Content-Type" "application/edn; charset=UTF-8"
                           "Accept" "application/edn"}]
          (let [send-ret (.send xhr url opt_method opt_content opt_headers)]
            (println "send-ret" send-ret)
            send-ret))))))

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
   (re-frame/dispatch [:fetch-gh-user-details github-id])
   (assoc-in db [:user :github-id] github-id)))

(re-frame/register-handler
 :fetch-gh-user-details
 (fn [db [_ github-id]]
   #_(ajax.core/GET
       (str "https://api.github.com/users/" github-id)
       {:handler       #(re-frame/dispatch [:process-user-response %1])
        :error-handler #(re-frame/dispatch [:bad-response %1])})
   #_(ajax.core/GET
       (str "https://api.github.com/users/" github-id "/repos?sort=updated")
       {:handler       #(re-frame/dispatch [:process-repo-response %1])
        :error-handler #(re-frame/dispatch [:bad-response %1])})
   #_(ednxhr
    {:reqprm {:f :users :ids [10010] :log true :nocache true}
     :on-complete (fn [resp] (println ":on-complete" resp))
     :on-error (fn [resp] (println ":on-error" resp))})
   #_(ednxhr
    {:reqprm {:f :users :ids [10011] :log true :nocache true}
     :on-complete (fn [resp] (println ":on-complete" resp))
     :on-error (fn [resp] (println ":on-error" resp))})
   (-> db
       (assoc :loading? true)
       (assoc :error false))))

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
