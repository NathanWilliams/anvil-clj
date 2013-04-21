(ns anvil-clj.world-test
  (:use clojure.test
        anvil-clj.world))

(deftest file-loading
  ;A simple test, but I need to practice writing them!
  (testing "Ensure loading a binary file works"
    (let [data (load-binary-file "sample_files/r.0.0.mca")]
      (is (= java.nio.HeapByteBuffer (type data)))
      (is (= 548864 (.limit data))))))
