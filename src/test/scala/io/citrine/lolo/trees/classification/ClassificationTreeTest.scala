package io.citrine.lolo.trees.classification

import java.io.{File, FileOutputStream, ObjectOutputStream}

import io.citrine.lolo.TestUtils
import io.citrine.lolo.stats.functions.Friedman
import org.junit.Test
import org.scalatest.Assertions._

import scala.util.Random

/**
  * Created by maxhutch on 12/2/16.
  */
@Test
class ClassificationTreeTest {

  /**
    * Trivial models with no splits should have finite feature importance.
    */
  @Test
  def testFeatureImportanceNaN(): Unit = {
    val X = Vector.fill(100) {
      val input = Vector.fill(10)(1.0)
      (input, 2.0)
    }

    val DTLearner = new ClassificationTreeLearner()
    val DTMeta = DTLearner.train(X)
    val DT = DTMeta.getModel()
    assert(DTMeta.getFeatureImportance()
      .get.forall(v => !v.isNaN))
  }

  @Test
  def testBinary(): Unit = {
    val rnd = new Random(seed = 0L)
    assert(rnd.nextLong() == -4962768465676381896L)
    val trainingData = TestUtils.binTrainingData(
      TestUtils.generateTrainingData(2048, 12, noise = 0.1,
        function = Friedman.friedmanSilverman),
      responseBins = Some(2)
    )
    val DTLearner = new ClassificationTreeLearner()
    val DTMeta = DTLearner.train(trainingData)
    val DT = DTMeta.getModel()

    /* We should be able to memorize the inputs */
    val output = DT.transform(trainingData.map(_._1))
    trainingData.zip(output.getExpected()).foreach { case ((x, a), p) =>
      assert(a == p, s"${a} != ${p} for ${x}")
    }
    assert(output.getGradient().isEmpty)
    output.getDepth().foreach(d => assert(d > 0))

    /* The first features should be the most important */
    val importances = DTMeta.getFeatureImportance().get
    assert(importances.slice(0, 5).min > importances.slice(5, importances.size).max)
  }

  /**
    * Test a larger case and time it as a benchmark guideline
    */
  @Test
  def longerTest(): Unit = {
    val rnd = new Random(seed = 0L)
    assert(rnd.nextLong() == -4962768465676381896L)
    val trainingData = TestUtils.binTrainingData(
      TestUtils.generateTrainingData(1024, 12, noise = 0.1, function = Friedman.friedmanSilverman),
      responseBins = Some(16)
    )
    val DTLearner = new ClassificationTreeLearner()
    val N = 100
    val start = System.nanoTime()
    val DTMeta = DTLearner.train(trainingData)
    val DT = DTMeta.getModel()
    (0 until N).map(i => DTLearner.train(trainingData))
    val duration = (System.nanoTime() - start) / 1.0e9

    println(s"Training large case took ${duration / N} s")

    /* We should be able to memorize the inputs */
    val output = DT.transform(trainingData.map(_._1))
    trainingData.zip(output.getExpected()).foreach { case ((x, a), p) =>
      assert(a == p, s"${a} != ${p} for ${x}")
    }
    assert(output.getGradient().isEmpty)
    output.getDepth().foreach(d => assert(d > 4 && d < 17, s"Depth is ${d}"))

    /* The first feature should be the most important */
    val importances = DTMeta.getFeatureImportance().get
    println(importances)
    assert(importances.slice(0, 5).min > importances.slice(5, importances.size).max)

    /* Test serialization */
    val tmpFile: File = File.createTempFile("tmp", ".csv")
    val oos = new ObjectOutputStream(new FileOutputStream(tmpFile))
    oos.writeObject(DT)
  }

  /**
    * Test a larger case and time it as a benchmark guideline
    */
  @Test
  def testCategorical(): Unit = {
    val trainingData = TestUtils.binTrainingData(
      TestUtils.generateTrainingData(1024, 12, noise = 0.1, function = Friedman.friedmanSilverman),
      inputBins = Seq((0, 8)), responseBins = Some(16)
    )
    val DTLearner = new ClassificationTreeLearner()
    val N = 100
    val start = System.nanoTime()
    val DT = DTLearner.train(trainingData).getModel()
    (0 until N).map(i => DTLearner.train(trainingData))
    val duration = (System.nanoTime() - start) / 1.0e9

    println(s"Training large case took ${duration / N} s")

    /* We should be able to memorize the inputs */
    val output = DT.transform(trainingData.map(_._1))
    trainingData.zip(output.getExpected()).foreach { case ((x, a), p) =>
      assert(a == p)
    }
    assert(output.getGradient().isEmpty)
    output.getDepth().foreach(d => assert(d > 3 && d < 18, s"Depth is ${d}"))
  }
}

/** Companion driver */
object ClassificationTreeTest {
  /**
    * Test driver
    *
    * @param argv args
    */
  def main(argv: Array[String]): Unit = {
    new ClassificationTreeTest().testBinary()
    new ClassificationTreeTest().longerTest()
    new ClassificationTreeTest().testCategorical()
  }
}
