package com.gmail.wristylotus.search

import java.util.concurrent.ThreadLocalRandom

trait GoogleSearch extends SearchEngine {

  override val url: String = "https://www.google.com/com.gmail.wristylotus.search?q="

  override def parse(content: Content): Links = List.fill(20)(buildUrl(ThreadLocalRandom.current().nextInt().toString)).toSet //TODO

}
