import java.util.Properties
import java.sql.{Connection, DriverManager, ResultSet, Statement}

import scala.collection.mutable.ArrayBuffer

object Llogin {
  def table(resultSet: ResultSet):Unit = {
    var table=new Array[String](3);
    var i:Int =0;
    while (resultSet.next) {
      val tableName = resultSet.getString(1)
      table(i)=tableName;
      //输出所有表名
      //println("tableName：" + tableName)
      i = i + 1
    }
    new ShowTablename(table);
  }

  def structure(table: String):Unit={
    val url = "jdbc:hive2://bigdata118.depts.bingosoft.net:22118/user30_db"
    val properties = new Properties()
    properties.setProperty("driverClassName", "org.apache.hive.jdbc.HiveDriver")
    properties.setProperty("user", "user30")
    properties.setProperty("password", "pass@bingo30")

    val connection = DriverManager.getConnection(url, properties)

    val statement = connection.createStatement
    //查询表结构
    var rs = statement.executeQuery("desc formatted "+table)
    var row:Int=0
    while(rs.next){
      row+=1;
    }
    //println("row:"+row);
    var resultSet = statement.executeQuery("desc formatted "+table)
    var str=Array.ofDim[String](row,3);
    var i:Int=0
    while (resultSet.next) {
      val columnName = resultSet.getString(1)
      val columnType = resultSet.getString(2)
      val comment = resultSet.getString(3)
      str(i)(0)=columnName
      str(i)(1)=columnType
      str(i)(2)=comment
      i+=1
      //输出表结构
      //println(s"columnName：$columnName     columnType:$columnType     comment:$comment")
    }
    new ShowDim(str)
    resultSet.close()
  }

  def sqlquery(sql:String):Unit={
    val url = "jdbc:hive2://bigdata118.depts.bingosoft.net:22118/user30_db"
    val properties = new Properties()
    properties.setProperty("driverClassName", "org.apache.hive.jdbc.HiveDriver")
    properties.setProperty("user", "user30")
    properties.setProperty("password", "pass@bingo30")

    val connection = DriverManager.getConnection(url, properties)

    val statement = connection.createStatement
    var rs = statement.executeQuery(sql)
    var row:Int=0
    while(rs.next){
      row+=1;
    }
    var column=rs.getMetaData().getColumnCount()
    //println("row,column"+row+" "+column)
    var columntitle=new Array[String](column)

    var resultSet = statement.executeQuery(sql)

    var str=Array.ofDim[String](row,column);
    var i:Int=0
    var j:Int=0
    while(j<column){
      columntitle(j)=rs.getMetaData().getColumnName(j+1);
      //println(columntitle(j));
      j+=1;
    }
    while (resultSet.next) {
      j=0;
      while(j<column){
        val s=resultSet.getString(j+1);
        str(i)(j)=s;
        j+=1;
      }
      i+=1
    }

    new ShowResult(str,columntitle,column)
    resultSet.close()
  }

  def llogin(username:String,password:String): Unit = {
    val url = "jdbc:hive2://bigdata118.depts.bingosoft.net:22118/user30_db"
    val properties = new Properties()
    properties.setProperty("driverClassName", "org.apache.hive.jdbc.HiveDriver")
    properties.setProperty("user", username)
    properties.setProperty("password", password)

    val connection = DriverManager.getConnection(url, properties)

    val statement = connection.createStatement
    var resultSet = statement.executeQuery("show tables")
    try {
      table(resultSet)
      resultSet.close()
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
  def excute(sql:String): Unit = {

  }
}

