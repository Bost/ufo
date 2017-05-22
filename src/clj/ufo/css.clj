(ns ufo.css
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px]]))

;; Change defstylesheet to defstyles.
(defstyles screen
  [:body {
          ;; :background-image 'url("../pic/alien-face.png")
          ;; :background-size 'contain
          ;; :background-repeat 'no-repeat
          ;; :background-position 'center
          :font-family "monospace"
          :font-size "small"}]
  [:table {:width "100%"
           :margin-bottom (px 20)
           :border-collapse "collapse"
           :font-size "small"}
   ]
  ;; /* Define the hover highlight color for the table row */
  [
   :.data:hover {
                :background-color "WhiteSmoke"
                }]

  )


