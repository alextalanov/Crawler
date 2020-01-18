package com.gmail.wristylotus.search

trait YandexSearch extends SearchEngine {

  override val url: String = "https://yandex.ru/com.gmail.wristylotus.search/?text="

  override def parse(content: Content): Links = Set() //TODO

}
