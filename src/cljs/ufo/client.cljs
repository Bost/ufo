;; TODO https://github.com/compassus/compassus
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

(defn add-person! [widget {:keys [id] :as vals}]
  (om/transact! widget `[(rows/by-id
                          ;; ~ means evaluate the sexp before passing
                          ~{:id id :v vals})
                         (list/trows ~{:kws [:rows/by-id id]})]))

(defui ColsUsers
  static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  static om/IQuery (query [this] '[:id :fname :lname]))

(defui ColsSalaries
  static om/Ident (ident [this {:keys [id]}] [:rows/by-id id])
  static om/IQuery (query [this] '[:id :salary]))

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
   (let [{:keys [row cols]} (om/props this)
         id (:id row)]
     (println "(om/props this)" (om/props this))
     (html
      [:tr (map (fn [kw] (td {:react-key (str id "-" (name kw)) :val (kw row)}))
                cols)]))))
(def tbody-row (om/factory TBodyRow))

(defn will-mount [widget]
  (let [{:keys [cols sqlfn] :as prm} (om/props widget)
        tbeg (time/now)]
    (utils/ednxhr
     {:reqprm {:f sqlfn :rowlim 4 :log t :nocache t}
      :on-complete
      (fn [resp]
        ;; map returs a lazy sequence therefore doseq must be used
        ;; (map #(add-person! widget %) (:rows resp))
        (doseq [p (:rows resp)]
          (add-person! widget p))
        ;; TODO transact {:resp (str resp) :tbeg tbeg :tend (time/now)})
        :on-error (fn [resp] (println resp)))})
    ;; TODO Searching DB should be returned by ednxhr and displayed here
    #_(html [:div "Searching DB..."])))

(defui TBodyUsers
  static om/IQuery (query [this] `[{:list/trows ~(om/get-query ColsUsers)}])
  Object (componentWillMount [this] (will-mount this))
  (render
   [this]
   (let [{:keys [list/trows]} (om/props this)]
     (html
      [:tbody (map (fn [row]
                     (tbody-row {:row row :cols (om/get-query ColsUsers)}))
                   trows)]))))
(def tbody-users (om/factory TBodyUsers {:keyfn :sqlfn}))

(defui TBodySalaries
  static om/IQuery (query [this] `[{:list/trows ~(om/get-query ColsSalaries)}])
  Object (componentWillMount [this] (will-mount this))
  (render
   [this]
   (let [{:keys [list/trows]} (om/props this)]
     (html
      [:tbody (map (fn [row]
                     (tbody-row {:row row :cols (om/get-query ColsSalaries)}))
                   trows)]))))
(def tbody-salaries (om/factory TBodySalaries {:keyfn :sqlfn}))

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
        (let [hm {:tname tname :cols cols :sqlfn sqlfn}]
          (cond
            (= tid :users)    (tbody-users    hm)
            (= tid :salaries) (tbody-salaries hm)
            :else (str "Unknown tid: '" tid "'")))
        ]]))))
(def table (om/factory Table {:keyfn :tid}))

(defui RootView
  static om/IQuery
  (query [this] `[{:list/tables ~(om/get-query TTable)}])
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

