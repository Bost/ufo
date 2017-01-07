(defproject ufo "1.8.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [[org.clojure/clojure "1.9.0-alpha14"]

   ;; webapp - begin
   [org.clojure/clojurescript "1.9.293"]
   [prismatic/om-tools "0.4.0"] ; more convenient dom elements
   [org.omcljs/om "1.0.0-alpha47" :exclusions [commons-codec]]
   [ring "1.5.0"]
   [cheshire "5.6.3"]
   [compojure "1.5.1"] ; routing lib for Ring; dispatching of GET, PUT, etc.
   ;; (time/now) in cljs
   [com.andrewmcveigh/cljs-time "0.4.0"]
   [sablono "0.7.6"] ; hiccup style templating for om-next
   ;; [cljsjs/react "15.2.1-1"]
   ;; [cljsjs/react-dom "15.2.1-1"]
   ;; [binaryage/devtools "0.7.2"] ; TODO look at CLJS DevTools
   ;; webapp - end

   #_[com.rpl/specter "0.13.1"] ; overcome fear of nested data

   ;; TODO see http://www.clodoc.org/doc/clojure.contrib.def/defn-memo
   [org.clojure/core.memoize "0.5.9"]

   ;; [org.clojure/core.match "0.3.0-alpha4"] ; pattern matching library

   [org.clojure/java.jdbc "0.6.1"]
   [com.mchange/c3p0 "0.9.5.2"] ; db connection pooling
   [mysql/mysql-connector-java "6.0.5"]

   ;; 0.9.0 requires new db2jcc4.jar and {:classname ... :jdbc-url ...}
   [clj-dbcp "0.8.2"] ; JDBC connections pools

   ;; quartzite dependency on slf4j-api should be auto-resolved
   ;; [org.slf4j/slf4j-nop "1.7.13"] ; Simple Logging Facade for Java

   ;; (time/now) in clj
   [clj-time-ext "0.7.2"]
   [clj-time "0.12.2"]]
  :plugins
  [[lein-cljsbuild "1.1.5"]
   [lein-figwheel "0.5.8" :exclusions [org.clojure/clojure]]]

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]
  :clean-targets ^{:protect false} ["resources/public/js/out"
                                    "resources/public/js/main.js"]

  ;; figwheel server config
  :figwheel
  {:ring-handler ufo.server/handler
   ;; Access figwheel server from outside of VM:
   ;; the 'Figwheel: Starting server at http://localhost:3448' is misleading
   ;; :server-ip "..."; default is "localhost"; see also :websocket-host
   ;; :server-port ... ; default port 3449
   :http-server-root "public" ; css-dirs requires http-server-root specification
   :css-dirs ["resources/public/css"]
   ;; Load CIDER, refactor-nrepl and piggieback middleware
   :nrepl-middleware ["cider.nrepl/cider-middleware"
                      "refactor-nrepl.middleware/wrap-refactor"
                      "cemerick.piggieback/wrap-cljs-repl"]}
  :cljsbuild
  {:builds
   [{:id "dev-ufo"
     :source-paths ["src/cljs" "src/clj"]
     ;; figwheel client config
     :figwheel {:websocket-host :js-client-host}
     :compiler {:output-to "resources/public/js/main.js"
                :output-dir "resources/public/js/out"
                :main ufo.core
                :asset-path "js/out"
                :optimizations :none
                ;; for debugging ClojureScript directly in the browser
                :source-map true}}]}
  ;; :main ufo.blogic
  :profiles
  {:uberjar {:aot :all}
   :dev {:dependencies [[figwheel-sidecar "0.5.8"
                         :exclusions [org.clojure/tools.analyzer
                                      org.clojure/tools.analyzer.jvm]]
                        #_[lein-figwheel "0.5.4-5"]
                        [com.cemerick/piggieback "0.2.1"]
                        ;; 0.2.13-SNAPSHOT fixes:
                        ;; Unable to resolve var: cemerick.piggieback/wrap-cljs-repl in this context
                        [org.clojure/tools.nrepl "0.2.13-SNAPSHOT"]]
         :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
         :source-paths ["src/cljs" "src/clj"]}})
