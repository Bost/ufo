;; TODO https://github.com/compassus/compassus
;; TODO https://untangled-web.github.io/untangled/
(ns ^:figwheel-always ufo.client
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [ufo.regexps :as re :refer [t f dbg dbi]]
   [ufo.utils :as utils]
   [ufo.sync :as sync]
   [cljs.core.async :as async :refer [<! >! put! chan]]
   [om.next :as om :refer-macros [defui]]
   [sablono.core :refer-macros [html]]
   [cljs-time.core :as time]

   [goog.dom :as gdom]
   [om.dom :as dom])
  (:import [goog Uri]
           ;; Jsonp creates a new cross domain channel that sends data to the specified host URL
           [goog.net Jsonp]))

;; TODO hiccup (html [:div#foo.bar.baz "bang"])
;;      <div id='foo' class='bar baz'>bang</div>
;; TODO hicckup [:div.stuff "bla stuff"]

;; TODO deduplicated signal graph

;; TODO re-frame - single web page apps; re-frisk
;; TODO reagent is probably better than om-next

(enable-console-print!)

(defn add-missing! [widget {:keys [id] :as vals}]
  (om/transact! widget `[(users/by-id
                          ;; ~ means evaluate the sexp before passing
                          ~{:id id})]))

(defn add-row! [widget {:keys [id] :as vals}]
  (om/transact! widget `[(rows/by-id
                          ;; ~ means evaluate the sexp before passing
                          ~{:id id :v vals})
                         (list/trows ~{:kws [:rows/by-id id]})]))

(defui ColsSalaries
  static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  static om/IQuery (query [this] '[:id :salary :abrev]))

(defui TTable
  static om/Ident (ident [this {:keys [tid]}] [:tables/by-tid tid])
  static om/IQuery (query [this] '[:tid :sqlfn :tname]))

(defui Th
  "1. {:keyfn ...} can only use keys specified by (om/props this)
2. Values stored under these keys can't be keywords"
  Object
  (render
   [this]
   (let [{:keys [val]} (om/props this)]
     (html [:th val]))))
(def th (om/factory Th {:keyfn :val}))

(defn abbrev [name]
  (if name
    (subs name 0 (min 2 (count name)))))

(defui TdAbrev
  "1. {:keyfn ...} can only use keys specified by (om/props this)
2. Values stored under these keys can't be keywords"
  ;; val is the id
  static om/IQueryParams (params [this] {:query nil})
  static om/IQuery (query [this] '[(:search/results {:query ?query})])
  Object
  (render
   [this]
   (let [{:keys [search/results val] :as prm} (om/props this)
         style {:style {:border "1px" :borderStyle "solid"}}
         qval (or val
                  (first
                   (:query (om/get-params this))))]
     (let [{fname :fname lname :lname} results
           set-query-fn!
           ;; {:params {:query <val>}} - <val> must be vector otherwise I get:
           ;;    10010 is not ISeqable(...)
           (fn [] (om/set-query! this {:params {:query [qval]
                                                }}))]
       (html [:td (conj style
                            {}
                            {:onClick set-query-fn!})
                  (str qval "-" (abbrev fname) (abbrev lname))])))))

(defui Td
  "1. {:keyfn ...} can only use keys specified by (om/props this)
2. Values stored under these keys can't be keywords"
  Object
  (render
   [this]
   (let [{:keys [react-key val]} (om/props this)
         style {:style {:border "1px" :borderStyle "solid"}}]
     (html [:td style (str val)]))))
#_(def td (om/factory Td {:keyfn :react-key}))

(defui THeadRow
  Object
  (render
   [this]
   (let [{:keys [cols] :as prm} (om/props this)]
     (html
      [:tr (map (fn [val] (th {:val (str val)}))
                cols)]))))
(def thead-row (om/factory THeadRow))

(defn td [row id kw]
  (let [td-fn (or (kw {:abrev (om/factory TdAbrev {:keyfn :react-key})})
                  (om/factory Td {:keyfn :react-key}))]
    (td-fn {:react-key (str id "-" (name kw)) :val (kw row)})))

(defui TBodyRow
  ;; static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  ;; static om/IQuery (query [this] '[:id :fname :lname])
  Object
  (render
   [this]
   (let [{:keys [row cols]} (om/props this)
         id (:id row)]
     (html
      [:tr (map (fn [kw] (td row id kw)) cols)]))))
(def tbody-row (om/factory TBodyRow))

(defui TBodySalaries
  static om/IQuery (query [this] `[{:list/trows ~(om/get-query ColsSalaries)}])
  Object
  (componentWillMount
   [this]
   (let [{:keys [cols sqlfn] :as prm} (om/props this)]
     (utils/ednxhr
      {:reqprm {:f sqlfn :log true :nocache true}
       :on-complete
       (fn [resp]
         ;; map returs a lazy sequence therefore doseq must be used
         ;; (map #(add-row! this %) (:rows resp))
         (doseq [row (:rows resp)]
           (add-row! this row)))
       :on-error (fn [resp] (println resp))})))
  (render
   [this]
   (let [{:keys [list/trows]} (om/props this)]
     (html
      [:tbody (map-indexed
               (fn [idx row]
                 (tbody-row {:react-key (str :row "-" idx)
                             :row row :cols (om/get-query ColsSalaries)}))
               trows)]))))

(defui Table
  ;; static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  ;; static om/IQuery (query [this] `[:id {:list/rows ~(om/get-query TBodyRow)}])
  Object
  (render
   [this]
   (let [{:keys [tid tname sqlfn cols] :as prm} (om/props this)]
     (html
      [:div
       #_[:button
        {:onClick
         (fn [e]
           (let [tbeg (time/now)]
             (utils/ednxhr
              {:reqprm {:f fname :log t :nocache t}
               :on-complete
               (fn [resp]
                 ;; map returs a lazy sequence therefore doseq must be used
                 ;; (map #(add-row! this %) (:rows resp))
                 (doseq [p (:rows resp)]
                   (add-row! this p cols))
                 #_{:resp (str resp) :tbeg tbeg :tend (time/now)})
               :on-error (fn [resp] (println resp))})))}
        "fetch data"]
       [:div tname]
       [:table
        [:thead (thead-row prm)]
        (let [hm {:keyfn :sqlfn}
              tbody-fn (or (tid {:salaries (om/factory TBodySalaries hm)})
                           (fn [_] (str "ERROR: Unknown tid: '" tid "'. tbody-fn undefined.")))]
          ;; the map {:cols cols :fname fname} must be reconstructed; can't use 'prm'
          (tbody-fn {:tname tname :cols cols :sqlfn sqlfn}))]]))))
(def table (om/factory Table {:keyfn :tid}))

(defui RootView
  static om/IQuery ;; the query-tree is static
  (query [this] `[{:list/tables ~(om/get-query TTable)}])
  Object
  #_(componentWillReceiveProps [this next-props]            (println "RootView" "WillReceiveProps"))
  #_(componentWillUpdate       [this next-props next-state] (println "RootView" "WillUpdate"))
  #_(componentDidUpdate        [this prev-props prev-state] (println "RootView" "DidUpdate"))
  #_(componentWillMount        [this]                       (println "RootView" "WillMount"))
  #_(componentDidMount         [this]                       (println "RootView" "DidMount"))
  #_(componentWillUnmount      [this]                       (println "RootView" "WillUnmount"))
  (render
   [this]
   (let [{:keys [list/tables]} (om/props this)]
     (html
      [:div
       [:div (for [table-desc tables]
               (table table-desc))]]))))

(defn jsonp
  ([uri] (jsonp (chan) uri))
  ([c uri]
   ;; put! - Asynchronously puts a val into port
   (.send (Jsonp. (Uri. uri))
          nil                    ;; payload
          (fn [val] (put! c val)) ;; reply-callback
          (fn [val] (println "error-callback" "val" val))
          nil                    ;; callback param value
          )
   c))

(def base-url
  #_"http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search="
  (str sync/uri #_"jsonreq/search=" "users/ids="))

(defn search-loop [c]
  (go
    (loop [[query cb remote] (<! c)]
      (if-not (empty? query)
        (let [ret (<! (jsonp (str base-url query)))
              hm (js->clj ret :keywordize-keys true)
              ;; [_ results] ret
              results (->> hm first :rows first)]
          #_(println "search-loop" "results" results)
          (cb {:search/results results} query remote))
        (cb {:search/results []} query remote))
      (recur (<! c)))))

(defn send-to-chan [c]
  (fn [{:keys [search]} cb]
    (when search
      (let [{[search] :children} (om/query->ast search)
            query (get-in search [:params :query])]
        (put! c [query cb :search])))))

(def send-chan (chan))

(search-loop send-chan)
