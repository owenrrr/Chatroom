!/usr/bin/env groovy
node {
    def mvnHome
    def javaHome
    mvnHome = tool 'maven3.6.3'
    javaHome = tool 'jdk1.8'
    env.PATH = "${javaHome}/bin:${mvnHome}/bin;${env.PATH}"

    stage('Git') {
        git credentialsId: 'Owen Liu', url: 'https://github.com/owenrrr/Chatroom.git'
        echo "clone success!"
    }
}
