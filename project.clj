(defproject mkremins/flense-nw "0.0-SNAPSHOT"
  :dependencies
  [[org.clojure/clojure "1.6.0"]
   [org.clojure/clojurescript "0.0-2727"]
   [org.clojure/core.async "0.1.346.0-17112a-alpha"]
   [org.omcljs/om "0.8.4"]
   [spellhouse/phalanges "0.1.4"]
   [mkremins/flense "0.0-SNAPSHOT"]
   [mkremins/fs "0.3.0"]]

  :plugins
  [[lein-cljsbuild "1.0.4"]]

  :cljsbuild
  {:builds
   [{:source-paths ["src"]
     :compiler {:main flense-nw.app
                :output-to "target/flense.js"
                :source-map "target/flense.js.map"
                :optimizations :none}}]})
