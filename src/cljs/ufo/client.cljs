;; TODO https://github.com/compassus/compassus
;; TODO https://untangled-web.github.io/untangled/
(ns ^:figwheel-always ufo.client
  (:require
   ;; [ufo [...]] ; is not supported by clojurescript
   [ufo.regexps :as re :refer [t f dbg dbi]]
   [ufo.utils :as utils]
   [om.next :as om :refer-macros [defui]]
   [sablono.core :refer-macros [html]]
   [cljs-time.core :as time]))

;; TODO hiccup (html [:div#foo.bar.baz "bang"])
;;      <div id='foo' class='bar baz'>bang</div>
;; TODO hicckup [:div.stuff "bla stuff"]

;; TODO deduplicated signal graph

;; TODO re-frame - single web page apps; re-frisk
;; TODO reagent is probably better than om-next

(enable-console-print!)

(defn add-row! [widget {:keys [id] :as vals}]
  (om/transact! widget `[(rows/by-id
                          ;; ~ means evaluate the sexp before passing
                          ~{:id id :v vals})
                         (list/trows ~{:kws [:rows/by-id id]})]))

(defui ColsSalaries
  static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  static om/IQuery (query [this] '[:id :salary :abrev]))

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
  static om/IQuery (query [this] '[
                                   #_:search/results
                                   (:search/results {:query ?query})])
  Object
  (render
   [this]
   (let [{:keys [search/results val] :as props} (om/props this)
         params (om/get-params this)
         style {:style {:border "1px" :borderStyle "solid"}}
         qval (or val
                  (first
                   (:query params)))]
     #_(println "TdAbrev" "render" "props" props "params" params)
     (let [{fname :fname lname :lname} (get-in results [qval])
           set-query-fn!
           ;; {:params {:query <val>}} - <val> must be vector otherwise I get:
           ;;    10010 is not ISeqable(...)
           (fn [] (om/set-query! this {:params {:query [qval]}}))]
       ;; auto-resolve of :abrev doesn't work
       (om/set-query! this {:params {:query [qval]}})
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
   (let [{:keys [val] :as props} (om/props this)
         style {:style {:border "1px" :borderStyle "solid"}}]
     (html [:td style (str val)]))))
#_(def td (om/factory Td {:keyfn :react-key}))

(defui THeadRow
  Object
  (render
   [this]
   (let [{:keys [react-key cols] :as props} (om/props this)]
     (html [:tr (map (fn [v] (th {:val (str v)})) cols)]))))
(def thead-row (om/factory THeadRow))

(defn td [props row id kw]
  (let [td-fn (or (kw {:abrev (om/factory TdAbrev {:keyfn :react-key})})
                  (om/factory Td {:keyfn :react-key}))]
    (td-fn (merge props {:id id :react-key (str id "-" (name kw)) :val (kw row)}))))

(defui TBodyRow
  ;; static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  ;; static om/IQuery (query [this] '[:id :fname :lname])
  Object
  (render
   [this]
   (let [{:keys [row cols] :as props} (om/props this)]
     (html [:tr (map (fn [kw] (td (merge {} props) row (:id row) kw)) cols)]))))
(def tbody-row (om/factory TBodyRow))

(defui TBodySalaries
  static om/IQuery (query [this] `[{:list/trows ~(om/get-query ColsSalaries)}])
  Object
  (componentWillMount
   [this]
   (let [{:keys [cols sqlfn] :as props} (om/props this)]
     #_(println "willmount" "props" props)
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
   (let [{:keys [list/trows] :as props} (om/props this)]
     #_(println "TBodySalaries" "render" "props" props)
     (html
      [:tbody (map-indexed
               (fn [idx row]
                 (tbody-row {:react-key (str :row "-" idx)
                             :row row :cols (om/get-query ColsSalaries)}))
               trows)]))))

(defui Table
  static om/Ident (ident [this {:keys [id]}] [:tables/by-id id])
  static om/IQuery (query [this] '[:id :sqlfn :tname])
  Object
  (render
   [this]
   (let [{:keys [id tname sqlfn cols] :as props} (om/props this)]
     #_(println "Table" "render" "props" props)
     (html
      [:div
       [:div tname]
       [:table
        [:thead (thead-row props)]
        (let [hm {:keyfn :sqlfn}
              tbody-fn (or (id {:salaries (om/factory TBodySalaries hm)})
                           (fn [_] (str "ERROR: Unknown id: '" id "'."
                                        " tbody-fn undefined.")))]
          #_(println
             "=" (= props {:id id :tname tname :cols cols :sqlfn sqlfn})
             "(type props)" (type props)
             "(type {:id id :tname tname :cols cols :sqlfn sqlfn})"
             (type {:id id :tname tname :cols cols :sqlfn sqlfn}))
          ;; merge must be done - can't use 'props' ???
          (tbody-fn (merge {} props)))]]))))
(def table (om/factory Table {:keyfn :id}))

(defui RootView
  ;; the query-tree is static
  static om/IQuery (query [this] `[{:list/tables ~(om/get-query Table)}
                                   :all-results])
  Object
  #_(componentWillReceiveProps
     [this next-props]            (println "RootView" "WillReceiveProps"))
  #_(componentWillUpdate
     [this next-props next-state] (println "RootView" "WillUpdate"))
  #_(componentDidUpdate
     [this prev-props prev-state] (println "RootView" "DidUpdate"))
  #_(componentWillMount
     [this]                       (println "RootView" "WillMount"))
  #_(componentDidMount
     [this]                       (println "RootView" "DidMount"))
  #_(componentWillUnmount
     [this]                       (println "RootView" "WillUnmount"))
  (render
   [this]
   (let [{:keys [list/tables all-results] :as props} (om/props this)]
     (println "all-results" all-results)
     (html [:div (map table tables)]))))

