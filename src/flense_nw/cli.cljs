(ns flense-nw.cli
  (:require [cljs.core.async :as async]
            [clojure.string :as string]
            [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom]
            [phalanges.core :as phalanges]))

(defn- handle-key [command-chan ev]
  (case (phalanges/key-set ev)
    #{:enter}
      (let [input (.-target ev)]
        (async/put! command-chan (string/split (.-value input) #"\s+"))
        (set! (.-value input) "")
        (.blur input))
    #{:esc}
      (.. ev -target blur)
    ;else
      nil)
  (.stopPropagation ev))

(defcomponent cli-view [_ owner]
  (render [_]
    (dom/input
      {:id "cli"
       :on-key-down #(handle-key (om/get-shared owner :command-chan) %)
       :on-key-press #(.stopPropagation %)})))
