pipeline {
    agent any

    environment {
        DOCKERHUB = credentials('dockerhub-credentials')
        DOCKERHUB_USERNAME = "change"
        COMMIT = "${env.GIT_COMMIT.take(7)}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changed Services') {
            steps {
                script {
                    // Récupère les fichiers modifiés
                    def changes = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim().split('\n')

                    // Liste des services
                    def services = [
                        "user-service",
                        "product-catalog-service",
                        "shopping-cart-service",
                        "order-service",
                        "payment-service",
                        "notification-service"
                    ]

                    // Filtre ceux modifiés
                    CHANGED_SERVICES = services.findAll { svc ->
                        changes.any { it.startsWith("services/${svc}/") }
                    }

                    if (CHANGED_SERVICES.isEmpty()) {
                        echo "No service changed. Nothing to build."
                    } else {
                        echo "Changed services: ${CHANGED_SERVICES}"
                    }
                }
            }
        }

        stage('Build & Test Modified Services') {
            when {
                expression { CHANGED_SERVICES && CHANGED_SERVICES.size() > 0 }
            }
            steps {
                script {
                    def stepsMap = [:]

                    CHANGED_SERVICES.each { svc ->
                        stepsMap[svc] = {
                            dir("services/${svc}") {
                                sh 'mvn -B clean test'
                            }
                        }
                    }

                    parallel stepsMap
                }
            }
        }

        stage('Build Docker Images (Modified Only)') {
            when {
                expression { CHANGED_SERVICES && CHANGED_SERVICES.size() > 0 }
            }
            steps {
                script {
                    def stepsMap = [:]

                    CHANGED_SERVICES.each { svc ->
                        stepsMap[svc] = {
                            sh "docker build -t ecommerce/${svc}:${COMMIT} services/${svc}"
                        }
                    }

                    parallel stepsMap
                }
            }
        }

        stage('Push Images (Modified Only)') {
            when {
                expression { CHANGED_SERVICES && CHANGED_SERVICES.size() > 0 }
            }
            steps {
                sh 'echo $DOCKERHUB_PSW | docker login -u $DOCKERHUB_USERNAME --password-stdin'
                script {
                    CHANGED_SERVICES.each { svc ->
                        def local = "ecommerce/${svc}:${COMMIT}"
                        def remoteVersion = "${DOCKERHUB_USERNAME}/ecommerce-${svc}:${COMMIT}"
                        def remoteLatest = "${DOCKERHUB_USERNAME}/ecommerce-${svc}:latest"

                        sh "docker tag ${local} ${remoteVersion}"
                        sh "docker push ${remoteVersion}"

                        sh "docker tag ${local} ${remoteLatest}"
                        sh "docker push ${remoteLatest}"
                    }
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
    }
}
