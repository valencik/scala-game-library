package com.regblanc.sgl.test
package core

import sgl._
import sgl.util._

trait AbstractApp extends MainScreenComponent {
  this: GameApp with InputHelpersComponent with GameLoopStatisticsComponent =>

  override def startingScreen: GameScreen = LoadingScreen

}
