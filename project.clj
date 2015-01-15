(defproject mkremins/flense-nw "0.0-SNAPSHOT"
  :dependencies
  [[org.clojure/clojure "1.6.0"]
   [org.clojure/clojurescript "0.0-2665"]
   [org.clojure/core.async "0.1.346.0-17112a-alpha"]
   [com.facebook/react "0.12.2.1"]
   [org.om/om "0.8.0"]
   [spellhouse/phalanges "0.1.4"]
   [mkremins/flense "0.0-SNAPSHOT"]
   [mkremins/fs "0.3.0"]]

  :plugins
  [[lein-cljsbuild "1.0.4"]]

  :source-paths ["src"]

  :cljsbuild
  {:builds
   [{:source-paths ["src"]
     :compiler {:preamble ["react/react.js"]
                :output-to "target/flense.js"
                :source-map "target/flense.js.map"
                :optimizations :whitespace
                :pretty-print true}}]})
