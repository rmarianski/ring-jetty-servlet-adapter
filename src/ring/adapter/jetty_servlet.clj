(ns ring.adapter.jetty-servlet
  (:import [org.mortbay.jetty Server Request Response Handler]
           [org.mortbay.jetty.bio SocketConnector]
           [org.mortbay.jetty.security SslSocketConnector]
           [org.mortbay.jetty.servlet Context ServletHolder]
           [javax.servlet.http HttpServletRequest HttpServletResponse])
  (:use [ring.util.servlet :only (servlet)]))

(defn make-jetty-handler
  ([handler] (make-jetty-handler handler Context/SESSIONS))
  ([handler options]
     (doto (Context. options)
       (.setContextPath "/")
       (.addServlet (-> handler
                        servlet
                        (ServletHolder.))
                    "/"))))

(defn add-ssl-connector!
  "Add an SslSocketConnector to a Jetty Server instance."
  [^Server server options]
  (let [ssl-connector (SslSocketConnector.)]
    (doto ssl-connector
      (.setPort        (options :ssl-port 443))
      (.setKeystore    (options :keystore))
      (.setKeyPassword (options :key-password)))
    (when (options :keystore-type)
      (.setKeystoreType ssl-connector (options :keystore-type)))
    (when (options :truststore)
      (.setTruststore ssl-connector (options :truststore)))
    (when (options :trust-password)
      (.setTrustPassword ssl-connector (options :trust-password)))
    (.addConnector server ssl-connector)))

(defn create-server
  "Construct a Jetty Server instance."
  [options]
  (let [connector (doto (SocketConnector.)
                    (.setPort (options :port 8080))
                    (.setHost (options :host)))
        server    (doto (Server.)
                    (.addConnector connector)
                    (.setSendDateHeader true))]
    (when (or (options :ssl?) (options :ssl-port))
      (add-ssl-connector! server options))
    server))

(defn ^Server run-jetty
  "Serve the given handler according to the options.
  Options:
    :configurator        - A function called with the Server instance.
    :port
    :host
    :join?               - Block the caller: defaults to true.
    :make-jetty-handler  - Function used to generate jetty handler
    :ssl?                - Use SSL.
    :ssl-port            - SSL port: defaults to 443, implies :ssl?
    :keystore
    :key-password
    :truststore
    :trust-password"
  [handler options]
  (let [^Server s (create-server (dissoc options :configurator))]
    (when-let [configurator (:configurator options)]
      (configurator s))
    (doto s
      (.setHandler
       (^Handler (:make-jetty-handler options make-jetty-handler)
                 handler))
      (.start))
    (when (:join? options false)
      (.join s))
    s))
