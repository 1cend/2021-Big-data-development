import java.util.{Properties, UUID}

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010

import scala.util.parsing.json.JSON

object Main {
  def main(args: Array[String]): Unit = {

    val accessKey = "FB3D24B0CA8E499819B0"
    val secretKey = "W0U2NzZBOUE5MEY4MzdFNEFEQkVEMzVGNjhGNjIw"
    //s3地址
    val endpoint = "http://scut.depts.bingosoft.net:29997"
    //上传到的桶
    val bucket = "wuxuyu"
    //上传文件的路径前缀
    val keyPrefix = "upload/"
    //上传数据间隔 单位毫秒
    val period = 5000
    //输入的kafka主题名称
    val inputTopic = "mn_buy_ticket_wuxuyu"
    //kafka地址
    val bootstrapServers = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037"

    KafkaProducer.kafkaproducer()

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
    def regJson(json:Option[Any])=json match{
      case Some(map:Map[String,Any])=>map
    }
    inputKafkaStream.map(x => {
      println(x)
      val temp=JSON.parseFull(x)
      val name =regJson(temp).get("username").toString.replace("Some(","").replace(")","")
      println(name)
      (x,name)
    }).keyBy(1)
    inputKafkaStream.writeUsingOutputFormat(new S3Writer(accessKey, secretKey, endpoint, bucket, keyPrefix, period))
    env.execute()
  }
}