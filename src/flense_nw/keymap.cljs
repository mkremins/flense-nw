(ns flense-nw.keymap
  (:require [flense.actions.clipboard :as clipboard]
            [flense.actions.clojure :as clojure]
            [flense.actions.completions :as completions]
            [flense.actions.history :as history]
            [flense.actions.paredit :as paredit]
            [flense.actions.text :as text]
            [flense.model :as model]
            [xyzzy.core :as z]))

(defn maybe-insert [ch else-fn]
  #(if (model/editing? %)
     (text/insert ch %)
     (else-fn %)))

(def keymap
  {#{:down} (some-fn text/begin-editing completions/next-completion z/down)
   #{:left} (some-fn text/move-caret-left z/left-or-wrap)
   #{:right} (some-fn text/move-caret-right z/right-or-wrap)
   #{:up} (some-fn text/cease-editing completions/prev-completion z/up)
   #{:alt :left} text/move-caret-left-by-word
   #{:alt :right} text/move-caret-right-by-word
   #{:shift :left} (some-fn text/adjust-range-left z/prev)
   #{:shift :right} (some-fn text/adjust-range-right z/next)
   #{:alt :shift :left} text/adjust-range-left-by-word
   #{:alt :shift :right} text/adjust-range-right-by-word
   #{:meta :shift :k} model/prev-placeholder
   #{:meta :shift :l} model/next-placeholder

   #{:backspace} (some-fn text/delete paredit/delete)
   #{:shift :backspace} paredit/delete
   #{:shift :space} paredit/insert-left
   #{:space} (maybe-insert \space paredit/insert-right)

   #{:ctrl :shift :open-square-bracket} paredit/make-map
   #{:ctrl :shift :nine} paredit/make-seq
   #{:ctrl :open-square-bracket} paredit/make-vec

   #{:shift :open-square-bracket} paredit/wrap-map
   #{:shift :nine} paredit/wrap-seq
   #{:shift :single-quote} text/wrap-string
   #{:open-square-bracket} paredit/wrap-vec

   #{:meta :shift :left} paredit/grow-left
   #{:meta :shift :right} paredit/grow-right
   #{:meta :ctrl :a} paredit/join-left
   #{:meta :ctrl :s} paredit/join-right
   #{:meta :shift :up} paredit/raise
   #{:meta :ctrl :left} paredit/shrink-left
   #{:meta :ctrl :right} paredit/shrink-right
   #{:meta :ctrl :up} paredit/splice
   #{:meta :ctrl :nine} paredit/split-left
   #{:meta :ctrl :zero} paredit/split-right
   #{:meta :ctrl :k} paredit/swap-left
   #{:meta :ctrl :l} paredit/swap-right

   #{:meta :c} clipboard/copy
   #{:meta :x} clipboard/cut
   #{:meta :v} clipboard/paste
   
   #{:tab} completions/complete
   #{:meta :shift :d} clojure/find-introduction
   #{:shift :three} clojure/toggle-dispatch

   #{:meta :z} history/undo
   #{:meta :y} history/redo})
