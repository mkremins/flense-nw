# flense-nw

flense-nw is a Clojure code editor app written using [node-webkit](https://github.com/rogerwang/node-webkit) and [Flense](https://github.com/mkremins/flense). Essentially, flense-nw wraps an instance of the baseline Flense editor component in an imitation of the traditional text editor interface, providing functionality like file I/O, configurable keybinds, and a way to enter text commands.

## Building

flense-nw runs on [node-webkit](https://github.com/rogerwang/node-webkit). You'll also need [npm](https://www.npmjs.org/) to install some of the dependencies and [Leiningen](http://leiningen.org/) to compile the ClojureScript source.

For the time being, flense-nw builds against the latest snapshot version of Flense. It's recommended that you check out the [Flense repo](https://github.com/mkremins/flense) and `lein install` it in your local repository before attempting to build flense-nw.

```bash
cd path/to/flense
lein cljsbuild once
npm install
path/to/node-webkit .
```

This will build flense-nw from source and launch it as a standalone GUI app.

## License

[MIT License](http://opensource.org/licenses/MIT). Hack away.
