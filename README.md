# flense-nw

flense-nw is a Clojure code editor app written using [nw.js](http://nwjs.io/) and [Flense](https://github.com/mkremins/flense). Essentially, flense-nw wraps an instance of the baseline Flense editor component in an imitation of the traditional text editor interface, providing functionality like file I/O, configurable keybinds, and a way to enter text commands.

## Building

flense-nw runs on [nw.js](http://nwjs.io/). You'll also need [npm](https://www.npmjs.org/) to install some of the dependencies and [Leiningen](http://leiningen.org/) to compile the ClojureScript source.

For the time being, flense-nw builds against the latest snapshot version of Flense. It's recommended that you check out the [Flense repo](https://github.com/mkremins/flense) and `lein install` it in your local repository before attempting to build flense-nw.

```bash
cd path/to/flense
lein cljsbuild once
npm install
path/to/nwjs .
```

This will build flense-nw from source and launch it as a standalone GUI app.

### Development tips

#### Running flense editor directly from a browser

This can be handy for quick tests or when using some bleeding-edge developer tools not available under NW.JS. 
Just keep in mind that some non-essential editor functionality can be broken in this environment. For example opening/saving 
files from local filesystem depends on node.js libraries embedded by NW.JS.

I personally run simple HTTP server this way:

```bash
cd path/to/flense-nw
python -m SimpleHTTPServer
```

#### ClojureScript REPL

```bash
lein repl
```

Next, require the `repl` namespace and boot the Clojurescript repl:

```clojure
(require '[flense-nw.repl :as repl])
(repl/repl!)
```

This will start a Websocket repl using [Weasel](https://github.com/tomjakubowski/weasel). When you reload flense-nw application, it should automatically connect to Weasel and anything you type at the repl will start evaluating.

```clojure
(in-ns 'flense-nw.app)
app-state
```

Should print current app-state:

    #<Atom: {:selected-tab 0, :tabs [{:name "scratch", :document {:path [0], :tree {:children [{:type :seq, :children [{:type :symbol, :text "defn", :path [0 0]} {:type :symbol, :text "greet", :path [0 1]} {:type :vec, :children [{:type :symbol, :text "name", :path [0 2 0]}], :path [0 2]} {:type :seq, :children [{:type :symbol, :text "str", :path [0 3 0]} {:type :string, :text "Hello, ", :path [0 3 1]} {:type :symbol, :text "name", :path [0 3 2]} {:type :string, :text "!", :path [0 3 3]}], :path [0 3]}], :path [0]}]}}}]}>

#### Using React Development Tools with NW.JS

Flense uses Facebook's [React.js](https://github.com/facebook/react) library (via David Nolen's [Om](https://github.com/swannodette/om)). The React team offers a useful [React Developer Tools](https://github.com/facebook/react-devtools) (RDT) Chrome extension for inspecting and debugging React components (it integrates into Chrome's dev tools). However, flense-nw runs in [nw.js](https://github.com/nwjs/nw.js), and RDT cannot be easily installed into nw.js itself.

A solution is to use standalone devtools (frontend) in a standalone Chrome with RDT extension installed and instruct devtools to connect to our remote backend (our nw.js context running inside Flense-nw). It is easily doable, but it requires a special setup:

##### Preparation

  * launch `/Applications/Google\ Chrome\ Canary.app/Contents/MacOS/Google\ Chrome\ Canary --no-first-run --user-data-dir=~/temp/chrome-dev-profile`
  * install RDT
  
##### Development workflow

1. run nw.js instance with remote debugging enabled:

        cd path/to/flense-nw
        path/to/nwjs --remote-debugging-port=9222 .
        
2. launch `/Applications/Google\ Chrome\ Canary.app/Contents/MacOS/Google\ Chrome\ Canary --no-first-run --user-data-dir=~/temp/chrome-dev-profile`
3. in Chrome navigate to [http://localhost:9222/json](http://localhost:9222/json)
    => you should see a websocket url for remote context running in your nw.js from step #1 (note: sometimes you have to do a second refresh to see devtoolsFrontendUrl):

        [ {
           "description": "",
           "devtoolsFrontendUrl": "/devtools/devtools.html?ws=localhost:9222/devtools/page/BDFB0179-D7E4-6A27-6AD4-D7039548FDCB",
           "id": "BDFB0179-D7E4-6A27-6AD4-D7039548FDCB",
           "title": "index.html",
           "type": "page",
           "url": "file:///Users/darwin/code/flense-dev/flense-nw/index.html",
           "webSocketDebuggerUrl": "ws://localhost:9222/devtools/page/BDFB0179-D7E4-6A27-6AD4-D7039548FDCB"
        } ]
        
4. in Chrome navigate to devtoolsFrontendUrl where you replace `/devtools/devtools.html` with `chrome-devtools://devtools/bundled/devtools.html` (kudos to Paul Irish for the solution)
    
    example: `chrome-devtools://devtools/bundled/devtools.html?ws=localhost:9222/devtools/page/BDFB0179-D7E4-6A27-6AD4-D7039548FDCB`

Voila! Now you should have a debug session estabilished between your devtools in Chrome (devtools frontend) and your Flense-nw application (devtools backend).

Last tested with Chrome Canary 42.0.2283.5 and RDT 0.12.1.

## License

[MIT License](http://opensource.org/licenses/MIT). Hack away.
