def testApp() {
    echo "testing the application..."
    sh "mvn test"
    echo "executing pipeline for branch $BRANCH_NAME"
}



def deployApp() {
    echo "deploying the application"
}

// def setupMavenImageName(String imageRepo = 'salzaidy')
def setupMavenImageName() {
    echo 'Incrementing Maven build number...'

    // 1) Bump version using Maven versions plugin
    sh '''
        mvn -B \
          build-helper:parse-version versions:set \
          -DnewVersion=\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.nextIncrementalVersion} \
          versions:commit
    '''

    // 2) Get effective project.version
    def version = sh(
            script: "mvn -q -Dexpression=project.version -DforceStdout help:evaluate",
            returnStdout: true
    ).trim()
    echo "Raw Maven project.version is: ${version}"

    def clearVersion = version.replace('-SNAPSHOT', '')
    echo "Clear version (for image tag) is: ${clearVersion}"

    // 3) Get artifactId (project name)
    def artifactId = sh(
            script: "mvn -q -Dexpression=project.artifactId -DforceStdout help:evaluate",
            returnStdout: true
    ).trim()
    echo "Maven artifactId is: ${artifactId}"

    // 4) Build IMAGE_NAME
//    def imageName = "${imageRepo}/${artifactId}:${clearVersion}-${env.BUILD_NUMBER}"
    def imageName = "salzaidy/${artifactId}:${clearVersion}-${env.BUILD_NUMBER}"
    env.IMAGE_NAME = imageName
    echo "IMAGE_NAME will be: ${env.IMAGE_NAME}"
}

return this