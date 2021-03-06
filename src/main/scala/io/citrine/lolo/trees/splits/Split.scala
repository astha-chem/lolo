package io.citrine.lolo.trees.splits

import scala.collection.BitSet

/**
  * Splits are used by decision trees to partition the input space
  */
abstract trait Split extends Serializable {
  /**
    * Take the left branch in the binary split?
    *
    * @param input vector of any type
    * @return true if input takes the left split
    */
  def turnLeft(input: Vector[AnyVal]): Boolean

  /**
    * Get the index of the input vector that is used to pick this split
    *
    * @return index of the input vector used by this split
    */
  def getIndex(): Int
}

/**
  * If no split was found
  */
class NoSplit extends Split {
  /**
    * Take the left branch in the binary split?
    *
    * @param input vector of any type
    * @return true if input takes the left split
    */
  override def turnLeft(input: Vector[AnyVal]): Boolean = false

  /**
    * Get the index of the input vector that is used to pick this split
    *
    * @return index of the input vector used by this split
    */
  override def getIndex(): Int = -1
}

/**
  * Split based on a real value in the index position
  *
  * @param index position of the real value to inspect
  * @param pivot value at or below which to take the left split
  */
class RealSplit(index: Int, pivot: Double) extends Split {

  /**
    * If the value is at or less than the pivot, turn left
    *
    * @param input vector of any type
    * @return true if input takes the left split
    */
  override def turnLeft(input: Vector[AnyVal]): Boolean = {
    input(index).asInstanceOf[Double] <= pivot
  }

  /**
    * ${inherit_doc}
    *
    * @return index of the input vector used by this split
    */
  override def getIndex: Int = index

  /**
    * Pretty print
    *
    * @return debug string
    */
  override def toString: String = s"Split index ${index} @ ${pivot}"
}

/**
  * Split based on inclusion in a set
  *
  * @param index      of the categorical feature
  * @param includeSet set of values that turn left
  */
class CategoricalSplit(index: Int, includeSet: BitSet) extends Split {

  /**
    * If the value at the index position is in the set, turn left
    *
    * @param input vector of any type
    * @return true if input takes the left split
    */
  override def turnLeft(input: Vector[AnyVal]): Boolean = {
    includeSet.contains(input(index).asInstanceOf[Char].toInt)
  }

  /**
    * ${inherit_doc}
    *
    * @return index of the input vector used by this split
    */
  override def getIndex: Int = index
}
