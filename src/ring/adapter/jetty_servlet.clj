(ns ring.adapter.jetty-servlet
  (:import (org.eclipse.jetty.server Server Request)
           (org.eclipse.jetty.server.handler AbstractHandler)
           (org.eclipse.jetty.server.nio SelectChannelConnector)
           (org.eclipse.jetty.server.ssl SslSelectChannelConnector)
           (org.eclipse.jetty.servlet ServletContextHandler ServletHolder)
           (org.eclipse.jetty.util.thread QueuedThreadPool)
           (org.eclipse.jetty.util.ssl SslContextFactory)
           (javax.servlet.http HttpServletRequest HttpServletResponse))
  (:use [ring.util.servlet :only (servlet)]))

(defn make-jetty-handler
  ([handler] (make-jetty-handler handler ServletContextHandler/SESSIONS))
  ([handler options]
     (doto (ServletContextHandler. options)
       (.setContextPath "/")
       (.addServlet (-> handler
                        servlet
                        (ServletHolder.))
                    "/"))))

(defn- ssl-context-factory
  "Creates a new SslContextFactory instance from a map of options."
  [options]
  (let [context (SslContextFactory.)]
    (if (string? (options :keystore))
      (.setKeyStorePath context (options :keystore))
      (.setKeyStore context (options :keystore)))
    (.setKeyStorePassword context (options :key-password))
    (when (options :keystore-type)
      (.setKeystoreType context (options :keystore-type)))
    (when (options :truststore)
      (.setTrustStore context (options :truststore)))
    (when (options :trust-password)
      (.setTrustPassword context (options :trust-password)))
    (case (options :client-auth)
      :need (.setNeedClientAuth context true)
      :want (.setWantClientAuth context true)
      nil)
    context))

(defn- ssl-connector
  "Creates a SslSelectChannelConnector instance."
  [options]
  (doto (SslSelectChannelConnector. (ssl-context-factory options))
    (.setPort (options :ssl-port 443))
    (.setHost (options :host))))

(defn- create-server
  "Construct a Jetty Server instance."
  [options]
  (let [connector (doto (SelectChannelConnector.)
                    (.setPort (options :port 80))
                    (.setHost (options :host)))
        server    (doto (Server.)
                    (.addConnector connector)
                    (.setSendDateHeader true))]
    (when (or (options :ssl?) (options :ssl-port))
      (.addConnector server (ssl-connector options)))
    server))

(defn ^Server run-jetty
  "Start a Jetty webserver to serve the given handler according to the
  supplied options:

  :configurator - a function called with the Jetty Server instance
  :port         - the port to listen on (defaults to 80)
  :host         - the hostname to listen on
  :join?        - blocks the thread until server ends (defaults to true)
  :make-jetty-handler - Function used to generate jetty handler
  :ssl?         - allow connections over HTTPS
  :ssl-port     - the SSL port to listen on (defaults to 443, implies :ssl?)
  :keystore     - the keystore to use for SSL connections
  :keystore-type - the keystore stype to use for SSL connections
  :key-password - the password to the keystore
  :truststore   - a truststore to use for SSL connections
  :trust-password - the password to the truststore
  :max-threads  - the maximum number of threads to use (default 50)
  :client-auth  - SSL client certificate authenticate, may be set to :need,
                  :want or :none (defaults to :none)"
  [handler options]
  (let [^Server s (create-server (dissoc options :configurator))]
    (doto s
      (.setHandler ((options :make-jetty-handler make-jetty-handler)
                    handler))
      (.setThreadPool (QueuedThreadPool. (options :max-threads 50))))
    (when-let [configurator (:configurator options)]
      (configurator s))
    (.start s)
    (when (:join? options false)
      (.join s))
    s))
