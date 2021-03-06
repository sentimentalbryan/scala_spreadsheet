package spreadsheet

import spreadsheet.QueryTermParser.Formula
import scala.collection.immutable.HashMap


/**
 * Model of a spreadsheet containing 8 columns and ten rows.
 * The columns are labeled A-H, and the rows as 1-10
 */
object Model {


  /**
   * A class to represent a spreadsheet cell.
   * It will require methods that allow it to be assigned a numerical value,
   * to be emptied, and to return both a string representation and a numerical representation. Write test code that tests
   * the behaviour of the cell class.
   */
  case class Cell(value: Either[String, Formula]) {

    val OnlyNum = """^-?[0-9]*(\.?[0-9]+)$""".r

    def isNumeric = value match {
      case Left(s) => s match {
        case OnlyNum(_) => true
        case other => false
      }
      case other => false
    }


    def numericalValue()(implicit m: Model): Double = {

      try {

        value match {
          case Left(s) if isNumeric => s.toDouble
          case Left(s) => 0
          case Right(f) => f.evaluate()
        }

      } catch {
        case t: Throwable =>
          val addr = m.getAddress(this)
          System.err.println(s" problem with $addr -> $value  ${ t.getMessage}")
          0
      }
    }

    def displayable(): String = {
      value match {
        case Left(s) => s
        case Right(f) => f.toString
      }
    }

    def printable()(implicit m: Model, settings: Settings): String = {
      val ret: String = value match {
        case Left(s) if isNumeric =>
          s.toDouble match {
            case 0 => ""
            case _ => s
          }
        case Left(s) => s
        case Right(f) => f.evaluate().toString
      }
      ret.substring(0, Math.min(settings.CellWidth, ret.length)).trim
    }
  }

  object Cell {
    def apply(value: String): Cell = {
      new Cell(Left(value))
    }

    def apply(value: Formula): Cell = {
      new Cell(Right(value))
    }
  }

}

class Model {

  import Model._

  def generateDefault() = Range(0, 10).toArray.map(c => Range(0, 8).toArray.map(r => Cell(Left("0"))))

  private val data: Array[Array[Cell]] = generateDefault()

  def getRow(i: Int) = data(i)

  def getRows(): List[List[Cell]] = data.toList.map(_.toList)

  def getCol(i: Int) =
    (for (r <- Range(0, data.length)) yield getRow(r)(i)).toList

  def getRowCol(rc: RCOff): Cell = data(rc.row)(rc.col)

  var addresses = HashMap.empty[Cell, RCOff]

  def setRowCol( rc: RCOff,c: Cell) = synchronized {
    addresses = addresses + (c -> rc)
    data(rc.row).update(rc.col, c)
  }

  def getAddress(c: Cell): Option[RCOff] = {
    addresses.get(c)

  }

}