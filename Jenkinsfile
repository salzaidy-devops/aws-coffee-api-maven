#!/usr/bin/env groovy

library identifier: 'jenkins-shared-library@main', retriever: modernSCM(
    [$class: 'GitSCMSource',
    remote: 'https://github.com/salzaidy-devops/jenkins-shared-library.git',
    credentialsID: 'github-credentials'
    ]
)

def gv

pipeline {

    agent any

    tools {
        maven 'mvn-3.9'

    }

    // environment {
    //     IMAGE_NAME = 'salzaidy/aws-coffee-api:4.0'
    // }

    stages {
        stage("init") {
            steps {
                script {
                    gv = load "script.groovy"
                    echo "testing trigger for webhooks..."
                }
            }
        }
        stage("test") {
            steps {
                script {
                    echo "testing the application..."
                    // gv.testApp()
                }
            }
        }

        stage("increment build number") {
            steps {
                script {
                    // gv.incrementBuildNumber()
                    echo 'incrementing build number...'
                    sh 'mvn build-helper:parse-version versions:set \
                        -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} \
                        versions:commit'
                    def matcher = readFile('pom.xml') =~ '<version>(.+?)</version>'
                    def version = matcher ? matcher[0][1] : "0.0.1"
                    echo "Raw version is: ${version}"
                    
                    def clearVersion = version.replace('-SNAPSHOT','')
                    echo "Clear version is: ${clearVersion}"

                    env.IMAGE_NAME = "salzaidy/aws-coffee-api:$clearVersion-$BUILD_NUMBER"
                    // def versionWithBuild = "$clearVersion-$BUILD_NUMBER"
                    echo "version With Build will be: ${env.IMAGE_NAME}"
                }
            }
        }

        stage("build jar") {
            steps {
                script {
                    echo 'Building JAR file...'
                    buildMavenJar()
                }
            }
        }

        stage("build docker image") {
            steps {
                script {
                    echo 'Building Docker image...'
                    buildImage(env.IMAGE_NAME)
                    dockerLogin()
                    dockerPush(env.IMAGE_NAME)
                }
            }
        }

        stage("Deploy") {
            steps {
                script {
                    echo 'copying Docker compose file...'
                    // def dockerComposeCMD = "docker-compose -f docker-compose.yaml up --detach"
                    // def dockerComposeCMD = "docker compose --file docker-compose.yaml up --detach"

                    def shellcmd = "bash ./server-cmds.sh ${IMAGE_NAME}"
                    def ec2Instance = "ec2-user@3.17.150.175"

                    sshagent(['ec2-server-key']) {
                        sh "scp server-cmds.sh ${ec2Instance}:/home/ec2-user"
                        sh "scp docker-compose.yaml ${ec2Instance}:/home/ec2-user"
                        sh "ssh -o StrictHostKeyChecking=no ${ec2Instance} ${shellcmd}"
                    }
                }
            }
        }

        stage("commit version update") {
            steps {
                script {
                    echo "testing where these changes land"
                    sshagent(['github-ssh-key']) {
                        // 1) Trust GitHub host key (avoid host key verification error)
                        sh 'mkdir -p ~/.ssh'
                        sh 'ssh-keyscan -H github.com >> ~/.ssh/known_hosts'
                        sh 'chmod 644 ~/.ssh/known_hosts'
                    
                        // 2) Switch remote to SSH (so pushes use the SSH key, not HTTPS)
                        sh 'git remote -v'
                        sh 'git remote set-url origin git@github.com:salzaidy-devops/aws-coffee-api-maven.git'
                        sh 'git remote -v'
                    
                        // 3) Optional sanity check after known_hosts is in place
                        sh 'ssh -T git@github.com || true'
                    
                        // 4) Stage + commit if there are changes
                        sh 'git add .'
                    
                        // 5) Push to target branch via SSH
                        sh 'git push origin HEAD:${BRANCH_NAME}'
                    }

                    // gv.commitVersionUpdate()
                    // withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
                    //     sh 'git config user.email alzaidy@example.com'
                    //     sh 'git config user.name awsCoffeeApi'
                    //     sh 'git status'
                    //     sh 'git branch'
                    //     sh 'git config --list'
                    //     sh 'git remote set-url origin https://${GIT_USER}:${GIT_PASS}@github.com/salzaidy-devops/aws-coffee-api-maven.git'
                    //     sh 'git add .'
                    //     sh 'git commit -m "Version updated to ${IMAGE_NAME}"'
                    //     sh 'git push https://${GIT_USER}:${GIT_PASS}@github.com/salzaidy-devops/aws-coffee-api-maven.git HEAD:${BRANCH_NAME}'
                    // }
                }
            }
        }

    }
}
