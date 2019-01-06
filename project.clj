(defproject com.besenczy/gnucash "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [[org.clojure/clojure "1.10.0"]
   [org.clojure/data.xml "0.2.0-alpha6"]
   [org.clojure/data.zip "0.1.2"]
   [clj-time "0.15.1"]]
  :main com.besenczy.gnucash.core
  :profiles
  {:dev
   {:dependencies
    [[org.clojure/test.check "0.9.0"]]}})
