(ns ufo.server
  (:require
   [ring.util.response :refer [file-response]]
   [ring.adapter.jetty :refer [run-jetty]]

   ;; compojure: routing lib for Ring; dispatching of GET, PUT, etc.
   [compojure.core :refer [defroutes GET PUT]]
   [compojure.route :as route]
   [compojure.handler :as handler]

   [clojure.edn :as edn]
   [ufo
    [db :as db]
    [sql :as sql]]
   [clj-time-ext.core :as etime]))

(defn indexhtml [req #_{:keys [params edn-body] :as prm}]
  (file-response "public/html/index.html" {:root "resources"}))

(defn response [data & [status]]
  {:status (or status 200) ;; Status code: 200 'OK (The request was fulfilled)'
   :headers {"Content-Type" "application/edn; charset=UTF-8"}
   :body (pr-str data)})

(def fmap
  {:users {:db db/users}
   :salaries {:db db/salaries}})

(defn doreq [{:keys [params edn-body] :as prm}]
  (println "doreq" "prm" prm)
  (let [fnkw (:f edn-body)
        dbfn (or (get-in fmap [fnkw :db])
                 (println "ERROR" fnkw "does not exist in the fmap"))
        dbfnprm (into edn-body (get-in fmap [fnkw :prm]))]
    (println "dbfn" dbfn "dbfnprm" dbfnprm)
    (let [data (dbfn dbfnprm)]
      (response {:sql (:sql data)
                 :rows (for [row (:rows data)]
                         (assoc row
                                :ago
                                (etime/tstp-modified-ago
                                 (:tstp row)
                                 {:verbose false :desc-length :short})))}))))
#_
(defroutes myapp
  (GET "/" [] "Show something")
  (POST "/" [] "Create something")
  (PUT "/" [] "Replace something")
  (PATCH "/" [] "Modify Something")
  (DELETE "/" [] "Annihilate something")
  (OPTIONS "/" [] "Appease something") ; beschwichtigen, stillen besaenftigen
  (HEAD "/" [] "Preview something"))

;; Restart figwheel to apply changes
(defroutes routes
  (GET "/"    req (indexhtml req))
  (PUT "/req" req (do
                    (println "req" req)
                    (doreq req)))
  (route/files "/" {:root "resources/public"}))

(defn read-inputstream-edn [input]
  (edn/read {:eof nil}
            (java.io.PushbackReader.
             (java.io.InputStreamReader. input "UTF-8"))))

(defn parse-edn-body [handler]
  (fn [request]
    (handler (if-let [body (:body request)]
               (assoc request
                 :edn-body (read-inputstream-edn body))
               request))))

(def handler (parse-edn-body routes))
