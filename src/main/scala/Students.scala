package com.knoldus.futureproject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.{Failure, Success, Try}

class Students {

  def calculateGrades(filePath: String): Future[Double] = {
    val grades = for {
      parsed <- parseCsv(filePath)
      studentAverage <- calculateStudentAverages(Future(parsed))
      classAverage <- calculateClassAverage(Future(studentAverage))
    } yield classAverage
    Await.ready(grades, Duration.Inf)
  }

  //to parse csv file into Future-of-List-0f-Map
  private def parseCsv(filePath: String): Future[List[Map[String, String]]] = Future {
    val source = Try(Source.fromFile(filePath)) match {
      case Failure(exception) => throw new IllegalArgumentException(s"Cannot read file at path: $filePath", exception)
      case Success(source) => source
    }

    val lines = source.getLines().toList
    val keys: List[String] = lines.headOption match {
      case Some(line) => line.split(",").map(_.trim).toList
      case None => List.empty
    }
    val listOfAllValues = lines.drop(1).map(line => line.split(",").map(_.trim).toList)
    listOfAllValues.map(values => (keys zip values).toMap)
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
    }.recoverWith { case exception => Future.failed(exception) }
  }

  private def calculateClassAverage(data: Future[List[(String, Double)]]): Future[Double] = {
    data.flatMap { listOfStudentAverages =>
      val averageOfStudent = listOfStudentAverages.map(_._2)
      val classAverage = averageOfStudent.sum / averageOfStudent.length
      Future(classAverage)
    }.recoverWith { case exception => Future.failed(exception) }
  }
}