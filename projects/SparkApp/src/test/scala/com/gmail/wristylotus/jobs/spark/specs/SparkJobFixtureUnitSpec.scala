package com.gmail.wristylotus.jobs.spark.specs

import org.scalatest.Outcome

trait SparkJobFixtureUnitSpec extends SparkUnitSpec {

  type JobType

  def withJob: JobType

  override type FixtureParam = JobType

  override def withFixture(test: OneArgTest): Outcome = super.withFixture {
    test.toNoArgTest(withJob)
  }

}
