import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import scala.collection.mutable.HashMap

//https://vincentarelbundock.github.io/Rdatasets/csv/Ecdat/Car.csv
//Stated Preferences for Car Choice
//category : car type
//value: price of vehicle divided by the logarithm of income
//data : 4655 records

object CarPriceStats {
  val total = new HashMap[String, (Double, Double)]()
  def main(args: Array[String]): Unit = {

    println("============ Step 2: Mean and Variance ============")
    val config = new SparkConf().setAppName("CarPriceStats").setMaster("local[1]")
    val sc = SparkContext.getOrCreate(config)
    sc.setLogLevel("WARN")
    val textFile = sc.textFile("src/main/resources/Car.csv")
    val lines = textFile.mapPartitionsWithIndex((idx, iter) => if(idx == 0) iter.drop(1) else iter)
    val pairRDD = lines.map(_.split(",")).map(words => (words(5), words(17).toDouble))

    println("============ Step 3: Mean and Variance ============")
    println("category \t mean \t\t variance")
    println("===================================================")
    pairRDD
      .groupByKey()
      .mapValues(calcMeanAndVariance)
      .foreach(v => printf("%10s \t %f \t %f\n", v._1, v._2._1, v._2._2))

    println("\n============ Step 4: 25% Mean and Variance ============\n")
    val sampleWithoutReplacement = getSampleData(false, pairRDD, 0.25)

    println("============ Step 5: 10 Mean and Variance ============")
    println("category \t mean \t\t variance")
    println("===================================================")
    val n = 10
    for(i <- 0 until n){
      getSampleData(true, sampleWithoutReplacement, 1)
        .groupByKey()
        .mapValues(i => calcMeanAndVariance(i))
        .foreach(v => {
          if(total.contains(v._1)) total.put(v._1, addValueToHash(total.get(v._1).get, v._2) )
          else total.put(v._1, v._2)
        })
    }
    total.keys.foreach(k => printf("%10s \t %f \t %f\n", k, total.get(k).get._1 / n, total.get(k).get._2 / n ))
    sc.stop()
  }

  def getSampleData(b: Boolean, data: RDD[(String, Double)], percent: Double): RDD[(String, Double)] = {
    val m1 = data.countByKey().map(v => (v._1, percent)).toMap
    data.sampleByKeyExact(b, m1)
  }

  def calcMeanAndVariance(d: Iterable[Double]) : (Double, Double)= {
    val mean = d.reduce(_ + _) / d.size
    val variance = d.map(i => scala.math.pow(mean - i, 2)).reduce(_ + _) / d.size
    (mean, variance)
  }

//  def calcMean(pairsRDD: RDD[(Int, Double)]) : RDD[(Int, Double)] = {
//    pairsRDD
//      .mapValues(value => (value, 1))
//      .reduceByKey((x, y) => (x._1 + y._1, x._2 + y._2))
//      .mapValues(value => value._1 / value._2)
//  }
//
//  def calcVariance(pairsRDD: RDD[(Int, Double)]) : RDD[(Int, Double)] = {
//    pairsRDD
//      .mapValues(value => (value, scala.math.pow(value, 2), 1))
//      .reduceByKey((x, y) => (x._1 + y._1, x._2 + y._2, x._3 + y._3 ))
//      .mapValues(value => (value._2 / value._3) - scala.math.pow(value._1 / value._3, 2))
//  }
//
//  def getMeanVarianceFromSample(data: RDD[(Int, Double)]) : (Double, Double) = {
//    val mean = data.map(_._2).reduce(_ + _) / data.count()
//    val variance = data.map(value => scala.math.pow((value._2 - mean), 2)).reduce(_ + _)/data.count()
//    (mean, variance)
//  }
//
  def addValueToHash(v1: (Double, Double), v2: (Double, Double)): (Double, Double) = {
    (v1._1 + v2._1, v1._2 + v2._2)
  }
}
