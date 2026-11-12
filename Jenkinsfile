pipeline {
    agent any
    environment {
        APP_NAME = 'ifs25011-p5-pbo2026-springboot-cashflow'
    }
    stages {
        stage('Build & Test') {
            steps { sh './mvnw -B clean test' }
        }
        stage('Jacoco 100%') {
            steps { sh './mvnw -B verify -Pcoverage' }
        }
        stage('Trivy Scan') {
            steps { sh 'trivy fs --exit-code 0 --severity HIGH,CRITICAL .' }
        }
        stage('Package') {
            steps { sh './mvnw -B clean package -Pproduction -DskipTests' }
        }
        stage('Publish') {
            steps { sh 'docker build -t ${APP_NAME}:${BUILD_NUMBER} .' }
        }
        stage('Deploy') {
            steps { echo 'Isi langkah deploy sesuai server (docker run / compose / ssh).' }
        }
    }
}
