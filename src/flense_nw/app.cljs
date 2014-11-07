(ns flense-nw.app
  (:require [cljs.core.async :as async :refer [<!]]
            [cljs.reader :as rdr]
            [flense.actions.text :as text]
            [flense.editor :refer [editor-view]]
            [flense.model :as model]
            [flense-nw.cli :refer [cli-view]]
            [flense-nw.error :refer [error-bar-view]]
            [flense-nw.keymap :refer [keymap]]
            [fs.core :as fs]
            [om.core :as om]
            [phalanges.core :as phalanges])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; top-level state setup and management
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private app-state
  (atom (model/forms->document
          '[(defn greet [name] (str "Hello, " name "!"))])))

(def ^:private edit-chan (async/chan))
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
  "Load the source file at `fpath` and open the loaded document, discarding any
   changes made to the previously active document."
  [fpath]
  (reset! app-state
          (->> (fs/slurp fpath) string->forms model/forms->document)))

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

(defn- handle-keydown [ev]
  (when-let [action (keymap (phalanges/key-set ev))]
    (if (contains? (:tags (meta action)) :text-command)
      (.. js/document (getElementById "cli") focus)
      (do (.preventDefault ev)
          (async/put! edit-chan action)))))

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
      (async/put! edit-chan (partial text/insert-char c)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; application setup and wiring
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init []
  (let [command-chan (async/chan)]
    (om/root editor-view app-state
             {:target (.getElementById js/document "editor-parent")
              :opts {:edit-chan edit-chan}})
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
