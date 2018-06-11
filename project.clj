(defproject tbaldridge_lessons "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clojure"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 ;; [org.clojure/core.async "0.2.385"]
                 [org.clojure/core.async "0.4.474"]
                 ;; [cheshire "5.6.3"]
                 [cheshire "5.8.0"] ;; JSON library
                 ;; [org.zeromq/jeromq "0.3.5"]
                 [org.zeromq/jeromq "0.4.3"]
                 [http-kit "2.3.0"]
                 [org.clojure/data.xml "0.0.8"]]
  :repl-options {;; :timeout 120000
                 :port 9001})
