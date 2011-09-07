(ns ring.adapter.jetty-servlet-test
  (:use [ring.adapter.jetty-servlet]
        [clojure.test])
  (:require [clj-http.client :as http]))

(defn- hello-world [request]
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Hello World"})

(deftest jetty-test
  (let [server (run-jetty hello-world {:port 4347, :join? false})]
    (try
      (Thread/sleep 2000)
      (let [response (http/get "http://localhost:4347")]
        (is (= (:status response) 200))
        (is (.startsWith (get-in response [:headers "content-type"])
                         "text/plain"))
        (is (= (:body response) "Hello World")))
      (finally (.stop server)))))

(deftest custom-handler-test
  (with-local-vars [custom-handler-invoked false]
    (let [custom-handler (fn [ring-handler]
                           (var-set custom-handler-invoked true)
                           (make-jetty-handler ring-handler))
          server (run-jetty hello-world {:port 4347, :join? false
                                         :make-jetty-handler custom-handler})]
      (try
        (is (var-get custom-handler-invoked))
        (finally (.stop server))))))
