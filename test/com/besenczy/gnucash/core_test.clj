(ns com.besenczy.gnucash.core-test
  (:require
   [com.besenczy.gnucash.core :as core]
   [clojure.java.io :as jio]
   [clojure.test :refer :all]))

(deftest make-hashmap
  (testing "test hashmap maker"
    (is (= {} (core/make-hashmap)))
    (is (= {} (core/make-hashmap :a "" :b []  :c () :d nil)))
    (is (= {:e "foo" :f [1 2 3] :g '(1 2 3) :h 123}
          (core/make-hashmap
            :a ""
            :b []
            :c ()
            :d nil
            :e "foo"
            :f [1 2 3]
            :g '(1 2 3)
            :h 123)))))
