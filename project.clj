(defproject ufo "1.8.1"
  ;; TODO look at devcards (figwheel) https://youtu.be/1YqnaUXcSl8?t=34m
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;; :pedantic? :abort
  :exclusions [
               ;; org.clojure/clojure
               org.clojure/tools.nrepl]

  :dependencies
  [
   [org.clojure/clojure "1.9.0"]
   [org.clojure/clojurescript "1.10.439"]
   [org.clojure/core.async  "0.4.474"]
   ;; provides REPL Srv, Cli and some common API for IDEs
   [nrepl "0.4.2"]

   ;; leads to the WARNING: CIDER's version (0.17.0) does not match
   ;; cider-nrepl's version (nil). Things will break!
   ;; [org.clojure/core.typed "0.5.3"
   ;;  ;; :exclusions [org.clojure/tools.nrepl]
   ;;  ]

   [io.aviso/pretty "0.1.35"] ; print things, prettily
   ;; webapp - begin
   [re-frame "0.10.5"]
   [secretary "1.2.3"]
   [ring "1.7.1"]
   ;; Ring routing lib; dispatching of GET, PUT, etc.
   [compojure "1.6.1"]
   [garden "1.3.5"] ; render CSS
   [com.andrewmcveigh/cljs-time "0.5.2"] ;; (time/now) in cljs
   ;; webapp - end

   [dk.ative/docjure "1.13.0"] ;; parse excel files
   #_[com.rpl/specter "0.13.1"] ; overcome fear of nested data
   ;; TODO see http://www.clodoc.org/doc/clojure.contrib.def/defn-memo
   [org.clojure/core.memoize "0.7.1"]

   ;; [org.clojure/core.match "0.3.0-alpha4"] ; pattern matching library

   [com.mchange/c3p0 "0.9.5.2"] ; db connection pooling
   [org.clojure/java.jdbc "0.7.8"]
   [mysql/mysql-connector-java "8.0.13"
    :exclusions [com.google.protobuf/protobuf-java]]

   ;; 0.9.0 requires new db2jcc4.jar and {:classname ... :jdbc-url ...}
   [clj-dbcp "0.9.0"] ; JDBC connections pools

   [clj-time-ext "0.0.0-31-0x6e56"] ;; (time/now) in clj
   [clj-time "0.15.1"]

   ;; A Clojure(Script); debug single- and multi-threaded apps
   [spyscope "0.1.6"]
   ]

  :injections [(require 'spyscope.core)]

  ;; :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
  :plugins
  [
   [lein-figwheel "0.5.17"]
   [lein-cljsbuild "1.1.7"]
   ;; render CSS
   [lein-garden "0.3.0" :exclusions [org.apache.commons/commons-compress]]]

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
   :css-dirs ["resources/public/css"]}

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/cljs" "src/clj"]
     ;; figwheel client config
     :figwheel {
                ;; :websocket-host :js-client-host
                :on-jsload "ufo.core/mount-root"}

     :compiler {:output-to "resources/public/js/main.js"
                :output-dir "resources/public/js/out"
                :main ufo.core
                :asset-path "js/out"
                :source-map-timestamp true
                :preloads             [devtools.preload]
                :external-config      {:devtools/config
                                       {:features-to-install :all}}}}]}
  ;; :main ufo.blogic
  :profiles
  {:uberjar {:aot :all}
   :dev {:dependencies
         [
          [binaryage/devtools "0.9.10"]
          [figwheel-sidecar "0.5.17"]

          ;; nREPL middleware enabling the use of a ClojureScript REPL on top of
          ;; an nREPL session
          [cider/piggieback "0.3.10"]

          ;; keeping track of changes to source files and their associated
          ;; namespaces i.e. to auto-reload modified namespaces in a running
          ;; Clojure application
          [ns-tracker "0.3.1"]]
         :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
         :source-paths ["src/cljs" "src/clj"]}}

  :garden
  {:builds [{:id "screen"
             :source-paths ["src/clj"]
             :stylesheet ufo.css/screen
             :compiler {:output-to "resources/public/css/style.css"
                        :pretty-print? true}}]})
