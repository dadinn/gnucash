(ns com.besenczy.gnucash.test.utils
  (:require
   [com.besenczy.gnucash.utils :as utils]
   [clojure.java.io :as jio]
   [clojure.test :refer :all]))

(deftest into-map
  (testing "test hashmap maker"
    (is (= {} (utils/into-map)))
    (is (= {} (utils/into-map :a "" :b []  :c () :d nil)))
    (is (= {:e "foo" :f [1 2 3] :g '(1 2 3) :h 123}
          (utils/into-map
            :a ""
            :b []
            :c ()
            :d nil
            :e "foo"
            :f [1 2 3]
            :g '(1 2 3)
            :h 123)))))
