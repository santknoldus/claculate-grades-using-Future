package com.knoldus.futureproject

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object StudentsDriver extends App {

  private val students = new Students

  val grades = students.calculateGrades("resources/studentReport.csv")
  grades.onComplete {
    case Success(value) => println("Grade:" + value)
    case Failure(exception) => println(exception)
  }
}