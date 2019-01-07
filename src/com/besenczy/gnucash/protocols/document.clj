(ns com.besenczy.gnucash.protocols.document)

(defprotocol Document
  (counters [this])
  (book [this]))
