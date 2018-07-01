(defproject ufo "1.8.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [
   [org.clojure/clojure "1.9.0"]
   [org.clojure/clojurescript "1.10.339"]
   [org.clojure/core.async  "0.4.474"]
   [nrepl "0.4.1"]

   [io.aviso/pretty "0.1.34"] ; print things, prettily
   ;; webapp - begin
   [re-frame "0.10.5"]
   [secretary "1.2.3"]
   [ring "1.6.3"]
   ;; Ring routing lib; dispatching of GET, PUT, etc.
   [compojure "1.6.1"]
   [garden "1.3.5"] ; render CSS
   [com.andrewmcveigh/cljs-time "0.5.2"] ;; (time/now) in cljs
   ;; webapp - end

   [dk.ative/docjure "1.12.0"] ;; parse excel files
   #_[com.rpl/specter "0.13.1"] ; overcome fear of nested data
   ;; TODO see http://www.clodoc.org/doc/clojure.contrib.def/defn-memo
   [org.clojure/core.memoize "0.7.1"]

   ;; [org.clojure/core.match "0.3.0-alpha4"] ; pattern matching library

   [org.clojure/java.jdbc "0.7.7"]
   [com.mchange/c3p0 "0.9.5.2"] ; db connection pooling
   ;; [mysql/mysql-connector-java "8.0.11"]

   ;; 0.9.0 requires new db2jcc4.jar and {:classname ... :jdbc-url ...}
   [clj-dbcp "0.9.0"] ; JDBC connections pools

   [clj-time-ext "0.13.0"] ;; (time/now) in clj
   [clj-time "0.14.4"]
   ]
  :plugins
  [
   [lein-figwheel "0.5.16" :exclusions [[org.clojure/clojure]]]
   [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
   [lein-garden "0.3.0"]
   ;; quartzite dependency on slf4j-api should be auto-resolved
   ;; [org.slf4j/slf4j-nop "1.7.13"] ; Simple Logging Facade for Java
   ]

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]
  :clean-targets ^{:protect false} ["resources/public/js/out" "resources/public/js/main.js"]

  ;; figwheel server config
  :figwheel
  {:ring-handler ufo.server/handler
   ;; Access figwheel server from outside of VM:
   ;; the 'Figwheel: Starting server at http://localhost:3448' is misleading
   ;; :server-ip "..."; default is "localhost"; see also :websocket-host
   :server-port 3450 ; default port 3449
   :http-server-root "public" ; css-dirs requires http-server-root specification
   :css-dirs ["resources/public/css"]
   }
  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/cljs" "src/clj"]
     ;; figwheel client config
     :figwheel {
                ;; :websocket-host :js-client-host
                :on-jsload "ufo.core/mount-root"
                }
     :compiler {:output-to "resources/public/js/main.js"
                :output-dir "resources/public/js/out"
                :main ufo.core
                :asset-path "js/out"
                :source-map-timestamp true
                :preloads             [devtools.preload]
                :external-config      {:devtools/config {:features-to-install :all}}}}]}
  ;; :main ufo.blogic
  :profiles
  {:uberjar {:aot :all}
   :dev {:dependencies
         [
          [binaryage/devtools "0.9.10"]
          [figwheel-sidecar "0.5.16"]

          ;; nREPL middleware enabling the use of a ClojureScript REPL on top of an nREPL session
          [cider/piggieback "0.3.6"]

          ;; keeping track of changes to source files and their associated namespaces
          ;; i.e. to auto-reload modified namespaces in a running Clojure application
          [ns-tracker "0.3.1"]]
         :plugins
         [
          ;; collection of nREPL middleware designed to enhance CIDER
          [cider/cider-nrepl "0.17.0"]]
         :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
         :source-paths ["src/cljs" "src/clj"]}}

  :garden
  {:builds [{:id "screen"
             :source-paths ["src/clj"]
             :stylesheet ufo.css/screen
             :compiler {:output-to "resources/public/css/style.css"
                        :pretty-print? true}}]})
