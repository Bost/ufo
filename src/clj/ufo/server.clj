(ns ufo.server
  (:require
   [ring.util.response :refer [file-response]]
   [cheshire.core :refer :all]

   ;; compojure: routing lib for Ring; dispatching of GET, PUT, etc.
   [compojure.core :refer [defroutes GET PUT]]
   [compojure.route :as route]
   [compojure.handler :as handler]

   [clojure.edn :as edn]
   [ufo
    [db :as db]
    [sql :as sql]
    #_[json :as ujs]]
   [clj-time-ext.core :as etime]))

(defn response [data & [status]]
  {:status (or status 200) ;; Status code: 200 'OK (The request was fulfilled)'
   :headers {"Content-Type" "application/edn; charset=UTF-8"}
   :body (pr-str data)})

(def fmap
  {:users    {:db db/users    :prm {}}
   :salaries {:db db/salaries :prm {}}})

(defn doreq [{:keys [params edn-body] :as prm}]
  (let [fnkw (:f edn-body)
        dbfn (or (get-in fmap [fnkw :db])
                 (println "ERROR" fnkw "does not exist in the fmap"))
        dbfnprm (into edn-body (get-in fmap [fnkw :prm]))]
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
  (GET "/"     [] "Show something")
  (POST "/"    [] "Create something")
  (PUT "/"     [] "Replace something")
  (PATCH "/"   [] "Modify Something")
  (DELETE "/"  [] "Annihilate something")
  (OPTIONS "/" [] "Appease something") ; beschwichtigen, stillen, besaenftigen
  (HEAD "/"    [] "Preview something"))

;; Restart figwheel to apply changes
(defroutes routes
  (GET "/"    req (file-response "public/html/index.html" {:root "resources"}))
  (PUT "/req" req (doreq req))
  (GET "/jsonreq" req
       (do
         (println "GET /jsonreq")
         (generate-string ["aaa" ["aaa!" "desc0"] [] []])))
  (route/files "/" {:root "resources/public"}))

(def handler
  (fn [request]
    (let [req (if-let [body (:body request)]
                (assoc request
                       :edn-body
                       (edn/read {:eof nil}
                                 (java.io.PushbackReader.
                                  (java.io.InputStreamReader. body "UTF-8"))))
                request)]
      (routes req))))
