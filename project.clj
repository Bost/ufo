(defproject ufo :lein-v
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
   [org.clojure/clojure "1.10.1"]
   [org.clojure/core.rrb-vector "0.0.14"]
   [org.clojure/clojurescript "1.10.520"]
   [org.clojure/core.async "0.4.500"]
   [org.clojure/algo.monads "0.1.6"]

   ;; leads to the WARNING: CIDER's version (0.17.0) does not match
   ;; cider-nrepl's version (nil). Things will break!
   ;; [org.clojure/core.typed "0.5.3"
   ;;  ;; :exclusions [org.clojure/tools.nrepl]
   ;;  ]

   [io.aviso/pretty "0.1.37"] ; print things, prettily
   ;; webapp - begin
   [re-frame "0.10.8"]  ; Reagent Framework For Writing SPAs, in Clojurescript
   [secretary "1.2.3"]  ; client-side router for clojurescript
   [ring "1.7.1"]       ; HTTP server abstraction
   [compojure "1.6.1"]  ; Ring routing lib; dispatching of GET, PUT, etc.
   [garden "1.3.9"]     ; render CSS
   [com.andrewmcveigh/cljs-time "0.5.2"] ; (time/now) in cljs
   ;; webapp - end

   ;; TODO see http://www.clodoc.org/doc/clojure.contrib.def/defn-memo
   [org.clojure/core.memoize "0.7.2"]

   ;; [org.clojure/core.match "0.3.0-alpha4"] ; pattern matching library
   [clj-time-ext "0.0.0-34-0x7939"] ;; (time/now) in clj
   [clj-time "0.15.1"]

   [spyscope "0.1.6"] ; A Clojure(Script); debug single- & multi-threaded apps
   [org.clojars.bost/utils "0.0.0-30-0x4b48"]
   ]

  :injections [(require 'spyscope.core)]

  ;; :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
  :plugins
  [
   ;; Drive leiningen project version from git instead of the other way around
   [com.roomkey/lein-v "7.1.0"]
   [lein-figwheel "0.5.19"]
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
                                       {:features-to-install :all}}

                :install-deps         true
                :npm-deps {
                           :jsonfile    "5.0.0"  ;; might not be needed
                           :react       "16.8.6"
                           :react-dom   "16.8.6"
                           :katex       "0.10.2"
                           :react-katex "2.0.2"
                           }
                }}]}
  ;; :main ufo.blogic
  :profiles
  {:uberjar {:aot :all}
   :dev {:dependencies
         [
          [binaryage/devtools "0.9.10"]
          [figwheel-sidecar "0.5.19"]

          ;; nREPL middleware enabling the use of a ClojureScript REPL on top of
          ;; an nREPL session
          [cider/piggieback "0.4.1"]

          ;; keeping track of changes to source files and their associated
          ;; namespaces i.e. to auto-reload modified namespaces in a running
          ;; Clojure application
          [ns-tracker "0.4.0"]]
         :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
         :source-paths ["src/cljs" "src/clj"]}}

  :garden
  {:builds [{:id "screen"
             :source-paths ["src/clj"]
             :stylesheet ufo.css/screen
             :compiler {:output-to "resources/public/css/style.css"
                        :pretty-print? true}}]})
