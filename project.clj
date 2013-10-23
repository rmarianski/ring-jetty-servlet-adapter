(defproject rmarianski/ring-jetty-servlet-adapter "0.0.4"
  :description "ring jetty adapter that exposes servlet related information in the request map"
  :url "http://github.com/rmarianski/ring-jetty-servlet-adapter"
  :global-vars {*warn-on-reflection* true}
  :min-lein-version "2.0.0"
  :dependencies [[ring/ring-servlet "1.2.0"]
                 [org.eclipse.jetty/jetty-server "7.6.8.v20121106"]
                 [org.eclipse.jetty/jetty-servlet "7.6.8.v20121106"]]
  :profiles {:dev {:dependencies [[clj-http "0.7.7"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}})
