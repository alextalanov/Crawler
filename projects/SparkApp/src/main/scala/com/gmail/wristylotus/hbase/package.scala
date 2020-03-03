package com.gmail.wristylotus

import java.net.URL

import com.gmail.wristylotus.hbase.model.Row
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}

package object hbase {

  import collection.JavaConverters._

  def buildConfiguration(resource: URL) = {
    val config = HBaseConfiguration.create()
    config.addResource(resource)
    config
  }

  def createTable(admin: Admin, table: TableName, families: ColumnFamilyDescriptor*): Unit = {
    val tableDesc = TableDescriptorBuilder.newBuilder(table)
      .setColumnFamilies(families.asJava)
      .build()

    admin.createTable(tableDesc)
  }

  def buildPut(row: Row): Put = {
    val request = new Put(row.key.value.getBytes())
    row.columns.foreach { column =>
      request.addColumn(
        column.family.name.getBytes(),
        column.qualifier.name.getBytes(),
        column.value
      )
    }
    request
  }

  def createRow(table: Table, row: Row): Unit = table.put(buildPut(row))

}
