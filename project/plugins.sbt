//resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
//resolvers += "HMRC Releases" at "https://dl.bintray.com/hmrc/releases"
//resolvers += Resolver.typesafeRepo("releases")

resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
//resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.1.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.1.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "2.2.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "2.0.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.23")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.3.7")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-bobby" % "3.4.0")