package com.gmail.wristylotus.search

trait YandexSearch extends SearchEngine {

  override val url: String = "https://yandex.ru/search/?text="

  override def parse(content: Html): Links = ??? //TODO

}
