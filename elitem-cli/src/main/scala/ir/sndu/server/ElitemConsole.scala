package ir.sndu.server

object ElitemConsole {
  def withColor(color: String)(f: ⇒ Unit): Unit = {
    print(color)
    f
    print(Console.WHITE)
  }

  def withError(f: ⇒ Unit): Unit = withColor(Console.RED)(f)
  def withError(msg: String): Unit = withError(System.err.println(msg))

  def withWarning(f: ⇒ Unit): Unit = withColor(Console.YELLOW)(f)
  def withWarning(msg: String): Unit = withWarning(println(msg))

  def withMessage(f: ⇒ Unit): Unit = withColor(Console.GREEN)(f)
  def withMessage(msg: String): Unit = withMessage(println(msg))

  def withOutput(f: ⇒ Unit): Unit = withColor(Console.BLINK)(f)
  def withOutput(msg: String): Unit = withOutput(println(msg))

}

