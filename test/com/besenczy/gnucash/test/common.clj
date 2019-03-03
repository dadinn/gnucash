(ns com.besenczy.gnucash.test.common
  (:require [clojure.test :refer :all]))

(defmacro is= [x y] `(is (= ~x ~y)))

(defmacro isnot [body] `(is (not ~body)))
