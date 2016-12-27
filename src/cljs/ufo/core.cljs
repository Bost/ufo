(ns ^:figwheel-always ufo.core
  (:require
   [goog.dom :as gdom]
   [om.dom]
   [om.next :as om]
   [ufo.state :as state]
   [ufo.client :as cli]))

(om/add-root! state/reconciler cli/RootView (gdom/getElement "app"))
