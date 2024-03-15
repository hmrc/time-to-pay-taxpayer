import sbt.Keys.parallelExecution
import sbt.{Def, *}
import scoverage.ScoverageKeys

object ScoverageSettings {

  def apply(): Seq[Def.Setting[? >: String & Double & Boolean]] = Seq( // Semicolon-separated list of regexes matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*BuildInfo.*;Reverse.*;app.Routes.*;prod.*;testOnlyDoNotUseInProd.*;manualdihealth.*;forms.*;config.*;",
    ScoverageKeys.coverageExcludedFiles := ".*microserviceGlobal.*;.*microserviceWiring.*;.*ApplicationLoader.*;.*ApplicationConfig.*;.*package.*;.*Routes.*;.*TestOnlyController.*;.*WebService.*",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}