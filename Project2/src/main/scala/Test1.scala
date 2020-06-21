import org.apache.spark.{SparkConf, SparkContext}

object Test1 {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName("Data Analysis").setMaster("local")
    val sc: SparkContext = new SparkContext(conf)
    sc.setLogLevel("WARN")
//    val one = sc.textFile("src/main/resources/test.txt")
//
//    val c = one.map(w => (w, 1)).reduceByKey(_+_)
//    c.foreach(println)
//
//    val cc = one.flatMap(x => x.split(" ")).map(w => (w, 1)).reduceByKey(_+_)
//    cc.foreach(println)


    val myOne = List(
      List("cat", "mat", "bat"),
      List("hat", "mat", "rat"),
      List("cat", "mat", "sat"),
      List("cat", "fat", "bat"))
    myOne.foreach(println)
    val myTwo = sc.parallelize(myOne)
    myTwo.foreach(println)

    val myThree = myTwo.map(x => (x(1), x(2)))
    myThree.foreach(println)

    val myFour = myThree.sortBy(_._1, false).sortBy(_._2)
    myFour.foreach(println)

  }


}
