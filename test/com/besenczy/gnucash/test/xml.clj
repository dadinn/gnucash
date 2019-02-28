(ns com.besenczy.gnucash.test.xml
  (:require
   [com.besenczy.gnucash.test.common :refer [is=]]
   [clojure.java.io :as jio]
   [clojure.data.xml :as x]
   [clojure.test :refer :all]))

(def xml-str
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><gnc-v2>\n<a:count-data xmlns:a=\"http://www.gnucash.org/XML/gnc\" xmlns:b=\"http://www.gnucash.org/XML/cd\" b:type=\"book\">1</a:count-data>\n<c:book xmlns:c=\"http://www.gnucash.org/XML/gnc\" version=\"2.0.0\">\n<e:id xmlns:e=\"http://www.gnucash.org/XML/book\" type=\"guid\">d4d0d712fbb43eba57a039cc276fe865</e:id>\n<c:count-data xmlns:g=\"http://www.gnucash.org/XML/cd\" g:type=\"commodity\">2</c:count-data>\n<c:count-data xmlns:h=\"http://www.gnucash.org/XML/cd\" h:type=\"account\">75</c:count-data>\n<c:count-data xmlns:i=\"http://www.gnucash.org/XML/cd\" i:type=\"transaction\">1200</c:count-data>\n<c:commodity version=\"2.0.0\">\n<k:space xmlns:k=\"http://www.gnucash.org/XML/cmdty\">ISO4217</k:space>\n<l:id xmlns:l=\"http://www.gnucash.org/XML/cmdty\">GBP</l:id></c:commodity></c:book></gnc-v2>")

(def xml-element
  (x/element :gnc-v2 {}
    "\n"
    (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/count-data
      #:xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcd{:type "book"}
      "1")
    "\n"
    (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/book
      {:version "2.0.0"}
      "\n"
      (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fbook/id
        {:type "guid"}
        "d4d0d712fbb43eba57a039cc276fe865")
      "\n"
      (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/count-data
        #:xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcd{:type "commodity"}
        "2")
      "\n"
      (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/count-data
        #:xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcd{:type "account"}
        "75")
      "\n"
      (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/count-data
        #:xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcd{:type "transaction"}
        "1200")
      "\n"
      (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fgnc/commodity
        {:version "2.0.0"}
        "\n"
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/space {} "ISO4217")
        "\n"
        (x/element :xmlns.http%3A%2F%2Fwww.gnucash.org%2FXML%2Fcmdty/id {} "GBP")))))

(deftest test-parse-emit
  (testing "`clojure.data.xml` library"
    (testing "with parsing namespaced tags"
      (is= (x/parse-str xml-str) xml-element))
    (testing "with emitting namespaced tags"
      (is= (x/emit-str xml-element) xml-str))))



