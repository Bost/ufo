;; TODO https://untangled-web.github.io/untangled/
(ns ^:figwheel-always ufo.client
  (:require
   [ufo.regexps :as re :refer [dbg dbi t f]]
   [ufo.utils :as utils]
   [ufo.meth :as meth]
   [ufo.state :as state]
   [om.next :as om :refer-macros [defui]]
   [sablono.core :refer-macros [html]]
   [cljs-time.core :as time]

   [goog.dom :as gdom]
   [om.dom :as dom]))

;; TODO hiccup (html [:div#foo.bar.baz "bang"])
;;      <div id='foo' class='bar baz'>bang</div>
;; TODO hicckup [:div.stuff "bla stuff"]

;; TODO deduplicated signal graph

;; TODO re-frame - single web page apps; re-frisk
;; TODO reagent is probably better than om-next

(enable-console-print!)

(defn add-person! [widget {:keys [id fname lname] :as prm} cols]
  (let [
        hm-rows {:kws [:rows/by-id id]}
        hm-cols {:kws [:cols/by-id id]}
        ]
    (om/transact! widget `[
                           (rows/by-id
                            ;; ~ means evaluate the sexp before passing
                            ~(assoc hm-rows :v {:id id :fname fname :lname lname}))
                           (list/trows ~hm-rows)
                           (cols/by-id
                            ;; ~ means evaluate the sexp before passing
                            ~(assoc hm-cols :v {:id id :cols cols}))
                           (list/cols ~hm-cols)
                           ])))

(defui TCols
  static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  static om/IQuery (query [this] '[:id :fname :lname]))

(defui TTableCols
  static om/Ident (ident [this {:keys [id]}] [:cols/by-id id])
  static om/IQuery (query [this] '[:id :cols]))

(defui Th
  "1. {:keyfn ...} can only use keys specified by (om/props this)
2. Values stored under these keys can't be keywords"
  Object
  (render
   [this]
   (let [{:keys [val]} (om/props this)]
     (html [:th val]))))
(def th (om/factory Th {:keyfn :val}))

(defui Td
  "1. {:keyfn ...} can only use keys specified by (om/props this)
2. Values stored under these keys can't be keywords"
  Object
  (render
   [this]
   (let [{:keys [react-key val]} (om/props this)
         style {:style {:border "1px" :borderStyle "solid"}}]
     (html [:td style (str val)]))))
(def td (om/factory Td {:keyfn :react-key}))

(defui THeadRow
  Object
  (render
   [this]
   (let [{:keys [cols] :as prm} (om/props this)]
     (html
      [:tr (map (fn [val] (th {:val (str val)}))
                cols)]))))
(def thead-row (om/factory THeadRow))

(defui TBodyRow
  ;; static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  ;; static om/IQuery (query [this] '[:id :fname :lname])
  Object
  (render
   [this]
   (let [{:keys [id] :as row} (om/props this)]
     (html
      [:tr (map (fn [kw] (td {:react-key (str id "-" (name kw)) :val (kw row)}))
                (om/get-query TCols))]))))
(def tbody-row (om/factory TBodyRow))

(defui TBody
  static om/IQuery
  ;; TODO use zipmap here
  (query [this] `[{:list/trows ~(om/get-query TCols)}
                  {:list/cols ~(om/get-query TTableCols)}])
  Object
  (componentWillMount
   [this]
   (let [{:keys [cols fname] :as prm} (om/props this)
         tbeg (time/now)]
     (utils/ednxhr
      {:reqprm {:f fname :rowlim 4 :log t :nocache t}
       :on-complete
       (fn [resp]
         ;; map returs a lazy sequence therefore doseq must be used
         ;; (map #(add-person! this %) (:rows resp))
         (doseq [p (:rows resp)]
           (add-person! this p cols))
         ;; TODO transact {:resp (str resp) :tbeg tbeg :tend (time/now)})
         :on-error (fn [resp] (println resp)))})
     ;; TODO Searching DB should be returned by ednxhr and displayed here
     #_(html [:div "Searching DB..."])))
  (render
   [this]
   (let [{:keys [list/trows list/cols] :as prm} (om/props this)]
     (println "render" "cols" cols)
     (html
      [:tbody (map tbody-row trows)]))))
(def tbody (om/factory TBody))

(defui Table
  ;; static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  ;; static om/IQuery (query [this] `[:id {:list/rows ~(om/get-query TBodyRow)}])
  Object
  (render
   [this]
   (let [{:keys [id fname tname cols] :as prm} (om/props this)]
     (html
      [:div
       [:button
        {:onClick
         (fn [e]
           (let [tbeg (time/now)]
             (utils/ednxhr
              {:reqprm {:f fname :rowlim 4 :log t :nocache t}
               :on-complete
               (fn [resp]
                 ;; map returs a lazy sequence therefore doseq must be used
                 ;; (map #(add-person! this %) (:rows resp))
                 (doseq [p (:rows resp)]
                   (add-person! this p cols))
                 #_{:resp (str resp) :tbeg tbeg :tend (time/now)})
               :on-error (fn [resp] (println resp))})))}
        "fetch data"]
       [:div tname]
       [:table
        [:thead (thead-row prm)]
        ;; the map {:cols cols :fname fname} must be reconstructed; can't use 'prm'
        (tbody {:cols cols :fname fname})]]))))
(def table (om/factory Table {:keyfn :fname}))

(defui RootView
  static om/IQuery
  (query [this] `[{:list/tables ~(om/get-query TCols)}])
  Object
  #_(componentWillUpdate [this nextprops nextstate] (println "componentWillUpdate"))
  #_(componentDidMount [this] (.log js/console "componentDidMount"))
  (render
   [this]
   (let [{:keys [list/tables]} (om/props this)]
     (html
      [:div
       (for [table-desc tables]
         (table table-desc))]))))

(def reconciler
  (om/reconciler
    {:state  state/app-state
     :parser (om/parser {:read state/read :mutate state/mutate})}))
