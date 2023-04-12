package com.knoldus.futureproject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success, Try}

class ClassOfStudent {

  def calculateGrades(path: String): Future[Double] = {
    val parsed = parseCsv(path)
    val studentAverages = calculateStudentAverages(parsed)
    val classAverage = calculateClassAverage(studentAverages)
    classAverage
  }

  //to parse csv file into Future-of-List-0f-Map
  private def parseCsv(filePath: String): Future[List[Map[String, String]]] = Future {
    val source = Try(Source.fromFile(filePath)) match {
      case Failure(_) => throw new IllegalArgumentException("Cannot read file at path: " + filePath)
      case Success(source) => source
    }

    val lines = source.getLines().toList
    val keys: List[String] = lines.headOption match {
      case Some(line) => line.split(",").map(_.trim).toList
      case None => List.empty
    }
    val listOfAllValues = lines.drop(1).map(line => line.split(",").map(_.trim).toList)
    val report = listOfAllValues.map(values => (keys zip values).toMap)
    report
  }

  private def calculateStudentAverages(data: Future[List[Map[String, String]]]): Future[List[(String, Double)]] = {
    data.flatMap { studentReports =>
      val studentAverages = studentReports.map { studentReport =>
        val id = studentReport("StudentID")
        val grades = List(studentReport("English").toDouble, studentReport("Physics").toDouble, studentReport("Chemistry").toDouble, studentReport("Maths").toDouble)
        val average = grades.sum / grades.length
        (id, average)
      }
      Future(studentAverages)

    }.recoverWith { case exception => Future.failed(exception)
    }
  }

  private def calculateClassAverage(data: Future[List[(String, Double)]]): Future[Double] = {
    data.flatMap { listOfStudentAverages =>
      val averageOfStudent = listOfStudentAverages.map(_._2)
      val classAverage = averageOfStudent.sum / averageOfStudent.length
      Future(classAverage)
    }.recoverWith { case exception => Future.failed(exception) }
  }
}