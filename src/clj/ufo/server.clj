(ns ufo.server
  (:require
   [ring.util.response :refer [file-response]]

   ;; compojure: routing lib for Ring; dispatching of GET, PUT, etc.
   [compojure.core :refer [defroutes GET PUT]]
   [compojure.route :as route]
   [compojure.handler :as handler]
   [clojure.edn :as edn]
   [clojure.data.json :as json]
   [ufo
    [db :as db]
    [sql :as sql]]
   [clj-time-ext.core :as etime]))

(defn end-response [data & [status]]
  {:status (or status 200) ;; Status code: 200 'OK (The request was fulfilled)'
   :headers {"Content-Type" "application/edn; charset=UTF-8"}
   :body (pr-str data)})

(def fmap
  {:users    {:db db/users    :prm {}}
   :salaries {:db db/salaries :prm {}}})

(defn doreq [{:keys [params edn-body] :as prm}]
  (end-response {:data (slurp "resources/public/notes/logics.org")})
  #_(let [fnkw (:f edn-body)
        dbfn (or (get-in fmap [fnkw :db])
                 (println "ERROR" fnkw "does not exist in the fmap"))
        dbfnprm (into edn-body (get-in fmap [fnkw :prm]))]
    (let [{sql :sql rows :rows} (dbfn dbfnprm)]
      (end-response {:sql sql :rows rows}))))
#_
(defroutes myapp
  (GET "/"     [] "Show something")
  (POST "/"    [] "Create something")
  (PUT "/"     [] "Replace something")
  (PATCH "/"   [] "Modify Something")
  (DELETE "/"  [] "Annihilate something")
  (OPTIONS "/" [] "Appease something") ; beschwichtigen, stillen, besaenftigen
  (HEAD "/"    [] "Preview something"))

;; (reset-autobuild) should be enough for figwheel to see the changes
(defroutes routes
  (GET "/"    req (file-response "public/html/index.html" {:root "resources"}))
  (PUT "/req" req (doreq req))
  (route/files "/" {:root "resources/public"})
  (route/not-found "<h1>Page not found</h1>"))

(defn handler [request]
  (routes (if-let [body (:body request)]
            (assoc request :edn-body
                   (edn/read {:eof nil}
                             (java.io.PushbackReader.
                              (java.io.InputStreamReader. body "UTF-8"))))
            request)))
