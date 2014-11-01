(ns flense-nw.app
  (:require [cljs.core.async :as async :refer [<!]]
            [cljs.reader :as rdr]
            [flense.actions :refer [default-actions]]
            [flense.actions.history :as hist]
            [flense.editor :refer [editor-view]]
            [flense.model :as model]
            [flense-nw.cli :refer [cli-view]]
            [flense-nw.error :refer [error-bar-view]]
            [fs.core :as fs]
            [om.core :as om]
            [phalanges.core :as phalanges])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; top-level state setup and management
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private app-state
  (atom {:path [0]
         :tree {:children (mapv model/form->tree
                            '[(defn greet [name] (str "Hello, " name "!"))])}}))

(def ^:private edit-chan (async/chan))
(def ^:private error-chan (async/chan))

(defn raise!
  "Display error message `mparts` to the user in the popover error bar."
  [& mparts]
  (async/put! error-chan (apply str mparts)))

(defn open!
  "Load the source file at `fpath` and open the loaded document, discarding any
   changes made to the previously active document."
  [fpath]
  (reset! app-state
    {:path [0]
     :tree {:children
            (->> (fs/slurp fpath) model/string->forms (mapv model/form->tree))}}))

(def actions
  (assoc default-actions :flense/text-command
         ;; dummy action to trap ctrl+x keybind
         (with-meta identity {:tags #{:text-command}})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; text commands
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle-command (fn [command & _] command))

(defmethod handle-command :default [command & _]
  (raise! "Invalid command \"" command \"))

(defmethod handle-command "exec" [_ & args]
  (if-let [name (first args)]
    (if-let [action (-> name rdr/read-string actions)]
      (async/put! edit-chan action)
      (raise! "Invalid action \"" name \"))
    (raise! "Must specify an action to execute")))

(defmethod handle-command "open" [_ & args]
  (if-let [fpath (first args)]
    (open! fpath)
    (raise! "Must specify a filepath to open")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; keybinds
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic *keymap*)

(defn- bound-action [ev]
  (-> ev phalanges/key-set *keymap* actions))

(defn- handle-keydown [ev]
  (when-let [action (bound-action ev)]
    (if (contains? (:tags (meta action)) :text-command)
      (.. js/document (getElementById "cli") focus)
      (do (.preventDefault ev)
          (async/put! edit-chan action)))))

(defn- handle-keypress [ev]
  (let [c (phalanges/key-char ev)]
    (when-let [action (actions (keyword "text" (str "insert-" c)))]
      (.preventDefault ev)
      (async/put! edit-chan action))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; application setup and wiring
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init []
  (set! *keymap* (rdr/read-string (fs/slurp "resources/config/keymap.edn")))
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
