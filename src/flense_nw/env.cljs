(ns flense-nw.env)

; note: depends on resources/scripts/env.js

(defn nwjs-present? []
  (js/isNodePresent))

(defn browser-present? []
  (not (nwjs-present?)))

(defn init! []
  (if (browser-present?)
    (println "Browser present - some node functionatily could be broken.")
    (println "NW.JS present.")))