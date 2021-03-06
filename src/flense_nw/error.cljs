(ns flense-nw.error
  (:require [cljs.core.async :as async :refer [alts!]]
            [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def ^:private TIMEOUT_MILLIS 4000)

(defcomponent error-bar-view [_ owner]
  (init-state [_]
    {:message "" :visible? false})
  (will-mount [_]
    (let [error-chan (om/get-shared owner :error-chan)]
      (go-loop [timeout (async/chan)]
        (let [[msg ch] (alts! [error-chan timeout])]
          (condp = ch
            error-chan
            (do (om/set-state! owner :message msg)
                (om/set-state! owner :visible? true)
                (recur (async/timeout TIMEOUT_MILLIS)))
            timeout
            (do (om/set-state! owner :visible? false)
                (recur (async/chan))))))))
  (render-state [_ state]
    (dom/div {:class (if (:visible? state) "visible" "hidden")
              :id "error-bar"}
      (:message state))))
