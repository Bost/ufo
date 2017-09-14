(defproject ufo "1.8.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [
   [org.clojure/clojure "1.9.0-alpha19"]

   ;; webapp - begin
   [re-frame "0.10.1"]
   [secretary "1.2.3"]
   [org.clojure/clojurescript "1.9.908"]
   [ring "1.6.2"]
   ;; Ring routing lib; dispatching of GET, PUT, etc.
   [compojure "1.6.0"]
   [garden "1.3.2"] ; render CSS
   [com.andrewmcveigh/cljs-time "0.5.1"] ;; (time/now) in cljs
   ;; webapp - end

   #_[com.rpl/specter "0.13.1"] ; overcome fear of nested data
   ;; TODO see http://www.clodoc.org/doc/clojure.contrib.def/defn-memo
   [org.clojure/core.memoize "0.5.9"]

   ;; [org.clojure/core.match "0.3.0-alpha4"] ; pattern matching library

   [org.clojure/java.jdbc "0.7.0"]
   [com.mchange/c3p0 "0.9.5.2"] ; db connection pooling
   [mysql/mysql-connector-java "6.0.6"]

   ;; 0.9.0 requires new db2jcc4.jar and {:classname ... :jdbc-url ...}
   [clj-dbcp "0.9.0"] ; JDBC connections pools

   [clj-time-ext "0.13.0"] ;; (time/now) in clj
   [clj-time "0.14.0"]]
  :plugins
  [
   [lein-cljsbuild "1.1.7"]
   [lein-garden "0.3.0"]
   ;; quartzite dependency on slf4j-api should be auto-resolved
   ;; [org.slf4j/slf4j-nop "1.7.13"] ; Simple Logging Facade for Java

   [lein-figwheel "0.5.13" :exclusions [org.clojure/clojure]]
   ]

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]
  :clean-targets ^{:protect false}
  ["resources/public/js/out" "resources/public/js/main.js"]

  ;; figwheel server config
  :figwheel
  {:ring-handler ufo.server/handler
   ;; Access figwheel server from outside of VM:
   ;; the 'Figwheel: Starting server at http://localhost:3448' is misleading
   ;; :server-ip "..."; default is "localhost"; see also :websocket-host
   :server-port 3450 ; default port 3449
   :http-server-root "public" ; css-dirs requires http-server-root specification
   :css-dirs ["resources/public/css"]
   ;; Load CIDER, refactor-nrepl and piggieback middleware
   :nrepl-middleware ["cider.nrepl/cider-middleware"
                      "refactor-nrepl.middleware/wrap-refactor"
                      "cemerick.piggieback/wrap-cljs-repl"]}
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
   :dev {:dependencies [
                        ;; com.cemerick/piggieback "0.2.13-SNAPSHOT" fixes:
                        ;; Unable to resolve var: cemerick.piggieback/wrap-cljs-repl in this context
                        [com.cemerick/piggieback "0.2.2"]
                        [figwheel-sidecar "0.5.13"]
                        [ns-tracker "0.3.1"]
                        [binaryage/devtools "0.9.4"]
                        [org.clojure/tools.nrepl "0.2.13"]]
         :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
         :source-paths ["src/cljs" "src/clj"]}}

  :garden
  {:builds [{:id "screen"
             :source-paths ["src/clj"]
             :stylesheet ufo.css/screen
             :compiler {:output-to "resources/public/css/style.css"
                        :pretty-print? true}}]})
