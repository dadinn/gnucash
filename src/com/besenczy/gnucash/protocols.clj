(ns com.besenczy.gnucash.protocols)

(defprotocol Book
  (slots [this])
  (counters [this])
  (prices [this])
  (accounts [this])
  (transactions [this]))

(defprotocol Document
  (book [this]))

