(ns ufo.css
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px]])
  (:import garden.types.CSSFunction
           garden.types.CSSAtRule))

;; Change defstylesheet to defstyles.
(defstyles screen
  [:body {:background-image (CSSFunction. :url "../pic/alien-face.png")
          :background-size "contain"
          :background-repeat "no-repeat"
          :background-position "center"
          :font-family "monospace"
          :font-size "small"}]

  [:table {:width "100%"
           :margin-bottom (px 20)
           :border-collapse "collapse"
           :font-size "small"}]

  ;; Define the hover highlight color for the table row
  [:.data:hover {:background-color "WhiteSmoke"}]

  [:table.data.tr:hover
   {:cursor "pointer"
    :text-decoration "underline"
    :font-weight "bold"}]

  [:tr.active {:border (px 1)
               :border-style "solid"}]

  [:table.data.td
   :table.data.th
   {:border (px 1)}]

  [:td :th {:vertical-align "top"
            :white-space "pre"}]
  [:th {:text-align "left"}]

  [:span.active {:margin-bottom (px 5)
                 :margin-right (px 5)
                 :font-weight "bold"}]

  [:span.line {:display "block"}]

  [:span.stats {:float "right"
                :font-weight "normal"}]

  [:span.sql {:margin-left (px 5)}]

  [:.tablehover {:background-color "whitesmoke"}]

  [:.thead
   :.tbody {:display "table"
            :width "60%"}]

  [:.tr :.th {:display "table-row"}]
  [:.td {:display "table-cell"}])


