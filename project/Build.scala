import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "menjajse-api"
    val appVersion      = "1.0-SNAPSHOT"
  
    val appDependencies = Seq(
       "reactivemongo" %% "reactivemongo" % "0.1-SNAPSHOT",
       "play.modules.reactivemongo" %% "play2-reactivemongo" % "0.1-SNAPSHOT"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
       resolvers += "sgodbillon" at "https://bitbucket.org/sgodbillon/repository/raw/master/snapshots/"
    )
}
