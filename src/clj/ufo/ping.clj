(ns ufo.ping
  (:require
   [clojure.java.io :as io]
   [dk.ative.docjure.spreadsheet :as excl]
   [utils.core :as ut :refer [in? not-empty? dbgv]]))

(defn timed-ping
  "Time an .isReachable ping to a given domain. Doesn't work for hosts in DMZ
  unix ping sends ICMP (Internet Control Message Protocol) ECHO_REQUEST"
  [domain timeout]
  (let [addr (java.net.InetAddress/getByName domain)
        start (. System (nanoTime))
        result (.isReachable addr timeout)
        total (/ (double (- (. System (nanoTime)) start)) 1000000.0)]
    {:time total :result result}))

(defn host-up?
  ";; parallel execution FTW!
  (pmap (fn [{:keys [host port] :as prm}]
                        (host-up? (conj prm {:timeout 500})))
  [{:host \"www.google.com\" :port 80}
])"
  [{:keys [host timeout port] :as prm}]
  (conj prm
        (let [sock-addr (java.net.InetSocketAddress. host (Integer. port))
              start (. System (nanoTime))]
          (try
            (with-open [sock (java.net.Socket.)]
              (. sock connect sock-addr timeout)
              {:res true :time (/ (double (- (. System (nanoTime)) start))
                                  1000000.0)})
            (catch java.io.IOException e
              {:res false :time nil})
            (catch java.net.SocketTimeoutException e
              {:res false :time nil})
            (catch java.net.UnknownHostException e
              {:res false :time nil})))))

(defn read-cell [{:keys [file sheet cell] :as prm}]
  (let [v (->> (excl/load-workbook file)
               (excl/select-sheet sheet)
               (excl/select-cell cell)
               #_(.getRawValue)
               #_(.getCellFormula)
               #_(.getCellType)
               #_(.getCachedFormulaResultType))]
    (conj prm
          {:cell-val (->> v excl/read-cell)}
          {:raw-val (->> v .getRawValue)}
          {:formula (->> v .getCellFormula)})))

