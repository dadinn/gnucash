(ns com.besenczy.gnucash.protocols.book)

(defprotocol Book
  (slots [this])
  (counters [this])
  (prices [this])
  (accounts [this])
  (transactions [this]))

