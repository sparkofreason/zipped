(ns sparkofreason.zipped-test
  (:require [rewrite-cljc.node :as n]
            [rewrite-cljc.zip :as z]
            [sparkofreason.transpounder :refer [check!]]
            [sparkofreason.zipped :refer :all]))

(comment
 (require '[flow-storm.api :as fsa])
 (fsa/connect)
 
 (require '[vlaaad.reveal :as reveal])
 (add-tap (reveal/ui))
 )



(check!
 #{'(ab xyz)}
 (-> empty-zipper
     (init-edit nil)
     (edit-loc \()
     (edit-loc \a)
     (edit-loc \b)
     (edit-loc \space)
     (edit-loc \x)
     (edit-loc \y)
     :edit-ptr
     :loc
     z/root
     n/sexpr))


