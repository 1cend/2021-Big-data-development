/*假设 kafka 有多个分区，
对于 kafka 分区与 flink 并行度一一对应的情况，
在 kafka source 处 assign watermark，
那么乱序只与数据源本身的乱序有关，
如果不是一一对应的，
比如只有一个消费者消费所有的分区的情况下，
由于无法保证数据全局顺序，
所以即便原始数据是顺序到达的，
但是消费的时候可能产生乱序，
这个乱序与数据源产生的乱序无关.
 */
import java.util.{Properties, UUID}

import org.apache.flink.api.common.functions.{FlatMapFunction, MapFunction}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010

import scala.collection.immutable.HashMap
import scala.util.parsing.json.JSON

object Main {
  /**
   * 输入的主题名称
   */
  val inputTopic = "mn_buy_ticket_1"
  /**
   * kafka地址
   */
  val bootstrapServers = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037"


  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val kafkaProperties = new Properties()
    kafkaProperties.put("bootstrap.servers", bootstrapServers)
    kafkaProperties.put("group.id", UUID.randomUUID().toString)
    kafkaProperties.put("auto.offset.reset", "earliest")
    kafkaProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    kafkaProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    val kafkaConsumer = new FlinkKafkaConsumer010[String](inputTopic,
      new SimpleStringSchema, kafkaProperties)
    kafkaConsumer.setCommitOffsetsOnCheckpoints(true)
    val inputKafkaStream = env.addSource(kafkaConsumer)
    var map = new HashMap[String,Int]
    inputKafkaStream.map(x=>{
      val citys=x.split(",")(3)
      val city=citys.substring(15,citys.length-1)
      //println(city)
      if(map.exists(_._1==city)){
        val t=map(city)+1
        map-=city
        map+=(city->t)
      }
      else {
        map+=(city->1)
      }
      println("前5：")
      map.toList.sortBy(-_._2).take(5).foreach{
        case (key, value) => println(key + value)
      }
    })
    env.execute()
  }
}
