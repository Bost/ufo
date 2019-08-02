(ns ufo.server
  (:require
   [ring.util.response :refer [file-response]]

   ;; compojure: routing lib for Ring; dispatching of GET, PUT, etc.
   [compojure.core :refer [defroutes GET PUT]]
   [compojure.route :as route]
   [compojure.handler :as handler]
   [clojure.edn :as edn]
   [clojure.data.json :as json]
   [clj-time-ext.core :as etime]
   [ufo.parser :as p]))

(defn end-response [data & [status]]
  {:status (or status 200) ;; Status code: 200 'OK (The request was fulfilled)'
   :headers {"Content-Type" "application/edn; charset=UTF-8"}
   :body (pr-str data)})

(defn doreq [{:keys [params edn-body] :as prm}]
  #_"resources/public/notes/category-theory.org"
  (as-> "resources/public/notes/cat.org" $
    (slurp $)
    ;; #spy/p
    ;; (p/exp-transformer (p/parse $ p/exp-parser))
    #spy/p
    (p/title-content-transformer (p/parse $ p/title-content-parser))
    {:data $}
    (end-response $)))

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
