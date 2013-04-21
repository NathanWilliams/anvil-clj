(ns anvil-clj.region-file-test
  (:use clojure.test
        anvil-clj.region-file
        anvil-clj.world))

(deftest header-test
  ;Some of these tests are a bit simplistic
  ;but it is all about testing practice for me!
  (testing "Confirm the header is read correctly"
    (let [data (load-binary-file "sample_files/r.0.0.mca")
          header (read-header data)]
      (is (= (set (keys header) #{:locations :timestamps})))
      (is (= 1024 
             (count (:locations header)) 
             (count (:timestamps header))))
      (is (every? #(= #{:offset :size} %)
                  (map #(set (keys %)))
                  (:locations header)))
      (is (= {:offset 69632, :size 4096}    ;Simple regression
             (first (:locations header))))
      (is (= 1364435190 (first (:timestamps header)))))))




