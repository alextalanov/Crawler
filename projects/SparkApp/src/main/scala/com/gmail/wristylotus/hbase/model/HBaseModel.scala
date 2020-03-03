package com.gmail.wristylotus.hbase.model

sealed trait HBaseModel

case class Row(key: RowKey, columns: Column*) extends HBaseModel

case class RowKey(value: String) extends HBaseModel

case class Column(family: ColumnFamily, qualifier: ColumnQualifier, value: Array[Byte]) extends HBaseModel

case class ColumnFamily(name: String) extends HBaseModel

case class ColumnQualifier(name: String) extends HBaseModel
