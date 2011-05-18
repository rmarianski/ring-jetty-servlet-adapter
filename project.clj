(defproject rmarianski/ring-jetty-servlet-adapter "0.0.1"
  :description "ring jetty adapter using a jetty servlet context"
  :url "http://github.com/rmarianski/ring-jetty-servlet-adapter"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.mortbay.jetty/jetty "6.1.26"]
                 [org.mortbay.jetty/jetty-util "6.1.26"]
                 [ring/ring-servlet "0.3.8"]]
  :dev-dependencies [[swank-clojure "1.3.0"]
                     [clj-http "0.1.3"]
                     [lein-clojars "0.6.0"]])
