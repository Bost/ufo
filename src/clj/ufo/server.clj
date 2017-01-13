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

(defn json-response [callback data & [status]]
  {:status (or status 200) ;; Status code: 200 'OK (The request was fulfilled)'
   :headers {"Content-Type" "application/jsonp; charset=UTF-8"}
   :body
   (do
     (println "callback" callback "data" data)
     (let [json-data (json/write-str data)]
       (if callback
         (str callback "(" json-data ")") ;; encapsulate the return in jsonp
         json-data)))})

(def fmap
  {:users    {:db db/users    :prm {}}
   :salaries {:db db/salaries :prm {}}})

(defn doreq [{:keys [params edn-body] :as prm}]
  (let [fnkw (:f edn-body)
        dbfn (or (get-in fmap [fnkw :db])
                 (println "ERROR" fnkw "does not exist in the fmap"))
        dbfnprm (into edn-body (get-in fmap [fnkw :prm]))]
    (let [{sql :sql rows :rows} (dbfn dbfnprm)]
      (end-response {:sql sql
                     :rows rows}))))

(defn doreq-json [callback {:keys [params edn-body] :as prm}]
  (let [fnkw (:f edn-body)
        dbfn (or (get-in fmap [fnkw :db])
                 (println "ERROR" fnkw "does not exist in the fmap"))
        dbfnprm (into edn-body (get-in fmap [fnkw :prm]))]
    (let [{sql :sql rows :rows} (dbfn dbfnprm)]
      (let [joe (json-response callback ["joj-foj" ["foj" "joj"] [] []])]
        (println "joe" joe)
        (let [res (json-response callback [{:sql sql :rows rows}])]
          (println "res" res)
          res)))))
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
  (GET "/users/ids=:ids" req
       (do
         (println "/users/ids=:ids" req)
         (let [{ids-str :ids} (:params req)]
           (println "(= ids-str \"foo-bar-ba\")" (= ids-str "foo-bar-ba"))
           (let [query-string (:query-string req)
                 callback (subs query-string (count "callback="))]
             (if (= ids-str "foo-bar-ba")
               (json-response callback ["jot-fot" ["fot" "jot"] [] []])
               #_(json-response callback ["jof-fof" ["fof" "jof"] [] []])
               #_(println "(read-string ids-str)" (read-string ids-str))
               (let [ids (read-string ids-str)
                     edn-body (conj {:f :users, :log true, :nocache true, :rowlim 1} {:ids ids})]
                 #_(println "edn-body" edn-body)
                 (doreq-json callback {:edn-body edn-body})))
           ))))
  (GET "/jsonreq/:search" req
       (do
         (println "/jsonreq/:search")
         (let [query-string (:query-string req)
                 callback (subs query-string (count "callback="))]
             (json-response callback ["joe-foo-stuff" ["foo" "joe"] [] []]))))
  (route/files "/" {:root "resources/public"})
  (route/not-found "<h1>Page not foundX</h1>"))

(defn handler [request]
  (routes (if-let [body (:body request)]
            (assoc request :edn-body
                   (edn/read {:eof nil}
                             (java.io.PushbackReader.
                              (java.io.InputStreamReader. body "UTF-8"))))
            request)))
