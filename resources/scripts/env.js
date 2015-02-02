function isNodePresent() {
  return (typeof process == "object");
}

if (!isNodePresent()) {
  // mute require calls when running under browser
  window.require = function(name) {
    if (name!="React")
      console.info("A require call to '" + name + "' is not available under raw browser environment.");
    return {};
  }
}