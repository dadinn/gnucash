(ns com.besenczy.gnucash.test.export
  (:require
   [com.besenczy.gnucash.export :as export]
   [clojure.data.xml :as x]
   [clojure.spec.alpha :as spec]
   [clojure.spec.test.alpha :as test]
   [clojure.test :refer :all]))

(deftest filter-nonempty-contents
  (let [empty-data
        [(x/element :foo {})
         (x/element :bar {:x 13 :y 42})]
        nonempty-data
        (x/element :baz {} "Some" "Content" "Here")
        test-data (conj empty-data nonempty-data)]
    (testing "filtering a single list of xml elements"
      (is (= [nonempty-data]
            (export/filter-nonempty-contents test-data))))
    (testing "filtering multiple lists of xml elements"
      (is (= (repeat 3 nonempty-data)
            (export/filter-nonempty-contents test-data
              test-data test-data))))))
