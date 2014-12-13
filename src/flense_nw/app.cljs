(ns flense-nw.app
  (:require [cljs.core.async :as async :refer [<!]]
            [cljs.reader :as rdr]
            [flense.actions.text :as text]
            [flense.editor :as flense]
            [flense.model :as model]
            [flense-nw.cli :refer [cli-view]]
            [flense-nw.error :refer [error-bar-view]]
            [flense-nw.keymap :refer [keymap]]
            [fs.core :as fs]
            [om.core :as om]
            [om.dom :as dom]
            [phalanges.core :as phalanges])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; top-level state setup and management
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ->tab [name forms]
  {:name name :document (model/forms->document forms)})

(def ^:private app-state
  (atom {:selected-tab 0
         :tabs [(->tab "scratch"
                  '[(defn greet [name] (str "Hello, " name "!"))])]}))

(def ^:private error-chan (async/chan))

(defn raise!
  "Display error message `mparts` to the user in the popover error bar."
  [& mparts]
  (async/put! error-chan (apply str mparts)))

(defn- string->forms [string]
  (let [pbr (rdr/push-back-reader string)
        eof (js/Object.)]
    (loop [forms []]
      (let [form (rdr/read pbr false eof false)]
        (if (= form eof)
          forms
          (recur (conj forms form)))))))

(defn open!
  "Load the source file at `fpath` and open the loaded document in a new tab."
  [fpath]
  (let [forms (string->forms (fs/slurp fpath))
        tabs (conj (:tabs @app-state) (->tab fpath forms))]
    (reset! app-state {:selected-tab (dec (count tabs)) :tabs tabs})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; text commands
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle-command (fn [command & _] command))

(defmethod handle-command :default [command & _]
  (raise! "Invalid command \"" command \"))

(defmethod handle-command "open" [_ & args]
  (if-let [fpath (first args)]
    (open! fpath)
    (raise! "Must specify a filepath to open")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; keybinds
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn perform! [action]
  (swap! app-state update-in [:tabs (:selected-tab @app-state) :document]
         (flense/perform action)))

(defn- handle-keydown [ev]
  (let [keyset (phalanges/key-set ev)]
    (if (= keyset #{:ctrl :x})
      (do (.preventDefault ev)
          (.. js/document (getElementById "cli") focus))
      (when-let [action (keymap keyset)]
        (.preventDefault ev)
        (perform! action)))))

(def legal-char?
  (let [uppers (map (comp js/String.fromCharCode (partial + 65)) (range 26))
        lowers (map (comp js/String.fromCharCode (partial + 97)) (range 26))
        digits (map str (range 10))
        puncts [\. \! \? \$ \% \& \+ \- \* \/ \= \< \> \_ \: \' \\ \|]]
    (set (concat uppers lowers digits puncts))))

(defn- handle-keypress [ev]
  (let [c (phalanges/key-char ev)]
    (when (legal-char? c)
      (.preventDefault ev)
      (perform! (partial text/insert-char c)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; application setup and wiring
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn tabs [data owner opts]
  (reify om/IRender
    (render [_]
      (let [{:keys [selected-tab tabs]} data]
        (dom/div #js {:className "tabs"}
          (apply dom/div #js {:className "tab-bar"}
            (for [i (range (count tabs))]
              (dom/div #js {
                :className (str "tab" (when (= i selected-tab) " selected"))
                :onClick #(om/update! data :selected-tab i)}
                (:name (nth tabs i)))))
          (dom/div #js {:className "tab-content"}
            (let [{:keys [document]} (nth tabs selected-tab)]
              (om/build flense/editor document {:opts opts}))))))))

(defn init []
  (let [command-chan (async/chan)]
    (om/root tabs app-state
             {:target (.getElementById js/document "editor-parent")})
    (om/root cli-view nil
             {:target (.getElementById js/document "cli-parent")
              :shared {:command-chan command-chan}})
    (om/root error-bar-view nil
             {:target (.getElementById js/document "error-bar-parent")
              :shared {:error-chan error-chan}})
    (go-loop []
      (let [[command & args] (<! command-chan)]
        (apply handle-command command args))
      (recur))
    (.addEventListener js/window "keydown" handle-keydown)
    (.addEventListener js/window "keypress" handle-keypress)))

(init)
