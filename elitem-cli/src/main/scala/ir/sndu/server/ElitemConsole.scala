package ir.sndu.server

object ElitemConsole {
  def withColor(color: String)(f: => Unit): Unit = {
    print(color)
    f
    print(Console.WHITE)
  }

  def withError(f: => Unit): Unit = withColor(Console.RED)(f)

  def withWarning(f: => Unit): Unit = withColor(Console.YELLOW)(f)

  def withOutput(f: => Unit): Unit = withColor(Console.GREEN)(f)

}

