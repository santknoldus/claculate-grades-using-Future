package com.knoldus.futureproject

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object ClassOfStudentDriver extends App {

  private val classOfStudent = new ClassOfStudent

  val grades = classOfStudent.calculateGrades("resources/studentReport.csv")
  grades.onComplete {
    case Success(value) => println("Grade:" + value)
    case Failure(exception) => println(exception)
  }
}