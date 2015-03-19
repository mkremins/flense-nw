(defproject mkremins/flense-nw "0.0-SNAPSHOT"
  :dependencies
  [[org.clojure/clojure "1.6.0"]
   [org.clojure/clojurescript "0.0-3126"]
   [org.clojure/core.async "0.1.346.0-17112a-alpha"]
   [org.omcljs/om "0.8.8"]
   [prismatic/om-tools "0.3.11"]
   [spellhouse/phalanges "0.1.6"]
   [mkremins/decodn "0.0-SNAPSHOT"]
   [mkremins/flense "0.0-SNAPSHOT"]
   [mkremins/fs "0.3.0"]
   [com.binaryage/devtools "0.0-SNAPSHOT"]
   [weasel "0.6.0"]
   [com.cemerick/piggieback "0.1.5"]]

  :plugins
  [[lein-cljsbuild "1.0.4"]]

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :source-paths ["src", "src-dev"] ; note: "src" must be included to prevent https://github.com/mfikes/weasel-src-paths

  :cljsbuild
  {:builds
   [{:source-paths ["src"]
     :compiler {:main flense-nw.app
                :output-to "target/flense.js"
                :output-dir "target"
                :source-map true
                :optimizations :none}}]})
