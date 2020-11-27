(ns sparkofreason.zipped-test
  (:require [clojure.test :refer :all]
            [sparkofreason.zipped :refer :all]))

(require '[flow-storm.api :as fsa])
(fsa/connect)

(def reader-dispatch (handle-keypress empty-expr "#"))

(handle-keypress empty-expr \()
(handle-keypress empty-expr \[)
(handle-keypress empty-expr \{)
(handle-keypress empty-expr \")
(handle-keypress empty-expr \a)

(handle-keypress empty-expr \:)
(-> empty-expr
  (handle-keypress \:)
  (handle-keypress \a))

(-> empty-expr
  (handle-keypress \:)
  (handle-keypress \a)
  (handle-keypress \/))
(-> empty-expr
  (handle-keypress \:)
  (handle-keypress \a)
  (handle-keypress \/)
  (handle-keypress \b))
(-> empty-expr
  (handle-keypress \:)
  (handle-keypress \a)
  (handle-keypress \/)
  (handle-keypress \b)
  (handle-keypress \/))  

(-> empty-expr
  (handle-keypress \a)
  (handle-keypress \b)
  (handle-keypress \())

(-> empty-expr
  (handle-keypress \a)
  (handle-keypress \,))

(-> empty-expr 
    (handle-keypress \@)
    (handle-keypress \a))

(-> empty-expr 
    (handle-keypress \@)
    (handle-keypress \a))

#trace
(-> empty-expr 
    (handle-keypress \@)
    (handle-keypress \[)
    (handle-keypress \a)
    (.toString))


(handle-keypress empty-expr \])
(handle-keypress reader-dispatch \()
(handle-keypress reader-dispatch \[)
(handle-keypress reader-dispatch \{)
(handle-keypress reader-dispatch \")
