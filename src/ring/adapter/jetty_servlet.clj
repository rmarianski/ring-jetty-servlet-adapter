(ns ring.adapter.jetty-servlet
  (:require [ring.util.servlet :refer [servlet]])
  (:import (org.eclipse.jetty.server Server Request)
           (org.eclipse.jetty.server.handler AbstractHandler)
           (org.eclipse.jetty.server.nio SelectChannelConnector)
           (org.eclipse.jetty.server.ssl SslSelectChannelConnector)
           (org.eclipse.jetty.servlet ServletContextHandler ServletHolder)
           (org.eclipse.jetty.util.thread QueuedThreadPool)
           (org.eclipse.jetty.util.ssl SslContextFactory)
           (java.security KeyStore)
           (javax.servlet Servlet)
           (javax.servlet.http HttpServletRequest HttpServletResponse)))

(defn make-jetty-handler
  ([handler] (make-jetty-handler handler ServletContextHandler/SESSIONS))
  ([handler options]
     (doto (ServletContextHandler. options)
       (.setContextPath "/")
       (.addServlet (ServletHolder. ^Servlet (servlet handler))
                    "/"))))

(defn- ssl-context-factory
  "Creates a new SslContextFactory instance from a map of options."
  [options]
  (let [context (SslContextFactory.)]
    (if (string? (options :keystore))
      (.setKeyStorePath context (options :keystore))
      (.setKeyStore context ^KeyStore (options :keystore)))
    (.setKeyStorePassword context (options :key-password))
    (when (options :keystore-type)
      (.setKeyStoreType context ^String (options :keystore-type)))
    (when (options :truststore)
      (.setTrustStore context ^KeyStore (options :truststore)))
    (when (options :trust-password)
      (.setTrustStorePassword context ^String (options :trust-password)))
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
    (.setHost (options :host))
    (.setMaxIdleTime (options :max-idle-time 200000))))

(defn- create-server
  "Construct a Jetty Server instance."
  [options]
  (let [connector (doto (SelectChannelConnector.)
                    (.setPort (options :port 80))
                    (.setHost (options :host))
                    (.setMaxIdleTime (options :max-idle-time 200000)))
        server    (doto (Server.)
                    (.addConnector connector)
                    (.setSendDateHeader true))]
    (when (or (options :ssl?) (options :ssl-port))
      (.addConnector server (ssl-connector options)))
    server))

(defn ^Server run-jetty
  "Start a Jetty webserver to serve the given handler according to the
  supplied options:

  :configurator   - a function called with the Jetty Server instance
  :port           - the port to listen on (defaults to 80)
  :host           - the hostname to listen on
  :join?          - blocks the thread until server ends (defaults to true)
  :daemon?        - use daemon threads (defaults to false)
  :ssl?           - allow connections over HTTPS
  :ssl-port       - the SSL port to listen on (defaults to 443, implies :ssl?)
  :keystore       - the keystore to use for SSL connections
  :key-password   - the password to the keystore
  :truststore     - a truststore to use for SSL connections
  :trust-password - the password to the truststore
  :max-threads    - the maximum number of threads to use (default 50)
  :min-threads    - the minimum number of threads to use (default 8)
  :max-idle-time  - the maximum idle time in milliseconds for a connection (default 200000)
  :client-auth    - SSL client certificate authenticate, may be set to :need,
                    :want or :none (defaults to :none)

  Added to ring.adapter.jetty:
  :make-jetty-handler - fn to generate a jetty handler
  :keystore-type      - the keystore stype to use for SSL connections "
  [handler options]
  (let [^Server s (create-server (dissoc options :configurator))
        ^QueuedThreadPool p (QueuedThreadPool. ^Integer (options :max-threads 50))]
    (.setMinThreads p (options :min-threads 8))
    (when (:daemon? options false)
      (.setDaemon p true))
    (doto s
      (.setHandler ((options :make-jetty-handler make-jetty-handler)
                    handler))
      (.setThreadPool p))
    (when-let [configurator (:configurator options)]
      (configurator s))
    (.start s)
    (when (:join? options false)
      (.join s))
    s))
