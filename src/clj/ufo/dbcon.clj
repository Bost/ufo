(ns ufo.dbcon
  (:require
   [clojure.java.jdbc :as jdbc]
   [clj-dbcp.core :as dbcp]
   [clojure.core.memoize :as memo])
  #_(:use
     [korma.db]
     [korma.core])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

;; TODO DRY dbase connection
(defn dbspec [dbuser]
  {:datasource (dbcp/make-datasource
                {:classname "com.mysql.jdbc.Driver"
                 :jdbc-url
                 (str
                  "jdbc:mysql://localhost:3306/employees"
                  "?useUnicode=true"
                  "&useJDBCCompliantTimezoneShift=true"
                  "&useLegacyDatetimeCode=false"
                  "&serverTimezone=UTC")
                 :user "root" :password "root"
                 :test-query "SELECT 1;"}
                #_{:user "login" :password "password"
                   :database 'DBASE_NAME;; ' is for excatly quoted symbol-name
                   :adapter :db2 :host "127.0.0.1" :port 12345})})

(defn pool [spec]
  {:datasource (doto (ComboPooledDataSource.)
                 (.setDriverClass (:classname spec))
                 (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":"
                                   (:subname spec)))
                 (.setUser (:user spec))
                 (.setPassword (:password spec))
                 ;; expire excess connections after 30 minutes of inactivity:
                 (.setMaxIdleTimeExcessConnections (* 30 60))
                 ;; expire connections after 3 hours of inactivity:
                 (.setMaxIdleTime (* 3 60 60)))})

(def dbconn-default :ufo)
(def pooleddbspec {:ufo (delay (pool (dbspec dbconn-default)) 100)})

(defn dbquery-impl [{:keys [dbconn f sql log] :or {dbconn dbconn-default} :as prm}]
  (if log
    (println (str "-- (-- :" f sql"
-- )")))
  (let [dbx @(dbconn pooleddbspec)]
    (jdbc/with-db-connection [dbx (dbspec dbconn)]
      (jdbc/query dbx [sql]))))

(def dbquery
  dbquery-impl
  #_(memo/memo dbquery-impl))

(defn sdbquery [{:keys [dbconn sql] :or {dbconn dbconn-default} :as prm}]
  #_(println "sdbquery" "sql" sql)
  {
   ;; :sql [sql] ; potentially more sql commands
   :rows (let [rows (dbquery prm)]
           ;; (println "rows" rows)
           rows)})
