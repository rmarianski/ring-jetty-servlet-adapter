(defproject rmarianski/ring-jetty-servlet-adapter "0.0.3"
  :description "ring jetty adapter that exposes servlet related information in the request map"
  :url "http://github.com/rmarianski/ring-jetty-servlet-adapter"
  :dependencies [[ring/ring-core "1.1.0"]
                 [ring/ring-servlet "1.1.0"]
                 [org.eclipse.jetty/jetty-server  "7.6.3.v20120416"]
                 [org.eclipse.jetty/jetty-servlet "7.6.3.v20120416"]]
  :dev-dependencies [[clj-http "0.3.2"]])
