(defproject rmarianski/ring-jetty-servlet-adapter "0.0.2"
  :description "ring jetty adapter that exposes servlet related information in the request map"
  :url "http://github.com/rmarianski/ring-jetty-servlet-adapter"
  :dependencies [[org.mortbay.jetty/jetty "6.1.26"]
                 [org.mortbay.jetty/jetty-util "6.1.26"]
                 [ring/ring-servlet "0.3.11"]]
  :dev-dependencies [[clj-http "0.1.3"]])
