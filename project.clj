(defproject ufo "1.8.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [[org.clojure/clojure "1.9.0-alpha15"]

   [reagent "0.5.1"]
   [re-frame "0.5.0"]
   [secretary "1.2.3"]

   ;; webapp - begin
   [org.clojure/clojurescript "1.9.518"]
   [ring "1.5.1"]
   [compojure "1.5.2"] ; routing lib for Ring; dispatching of GET, PUT, etc.
   ;; (time/now) in cljs
   [com.andrewmcveigh/cljs-time "0.4.0"]
   [sablono "0.8.0"] ; hiccup style templating for om-next
   ;; webapp - end

   [org.clojure/java.jdbc "0.6.1"]
   [com.mchange/c3p0 "0.9.5.2"] ; db connection pooling
   [mysql/mysql-connector-java "6.0.6"]

   ;; 0.9.0 requires new db2jcc4.jar and {:classname ... :jdbc-url ...}
   [clj-dbcp "0.8.2"] ; JDBC connections pools

   [clj-time-ext "0.13.0"] ;; (time/now) in clj
   [clj-time "0.13.0"]]
  :plugins
  [[lein-cljsbuild "1.1.5"]
   [lein-figwheel "0.5.10" :exclusions [cider/cider-nrepl org.clojure/clojure]]]

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
   :server-port 3450 ; default port 3449
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
     :figwheel {
                ;; :websocket-host :js-client-host
                :on-jsload "ufo.core/mount-root"
                }
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
   :dev {:dependencies [[figwheel-sidecar "0.5.10"
                         :exclusions [org.clojure/tools.analyzer
                                      org.clojure/tools.analyzer.jvm]]
                        [com.cemerick/piggieback "0.2.1"]
                        ;; 0.2.13-SNAPSHOT fixes:
                        ;; Unable to resolve var: cemerick.piggieback/wrap-cljs-repl in this context
                        [org.clojure/tools.nrepl "0.2.13"]]
         :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
         :source-paths ["src/cljs" "src/clj"]}})
