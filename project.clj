(defproject com.besenczy/gnucash "0.2.0"
  :description "Clojure library to parse/emit GnuCash XML format"
  :url "https://www.gnucash.org"
  :license
  {:name "LGPL"
   :url "https://www.gnu.org/copyleft/lesser.html"}
  :dependencies
  [[org.clojure/clojure "1.10.0"]
   [org.clojure/data.xml "0.2.0-alpha6"]
   [org.clojure/data.zip "0.1.2"]
   [clojure.java-time "0.3.2"]]
  :profiles
  {:dev
   {:dependencies
    [[org.clojure/test.check "0.9.0"]]}})
