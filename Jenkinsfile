@Library('redis-shared-library') _

pipeline {
    agent any

    environment {
        ENVIRONMENT = 'dev'
        PLAYBOOK    = 'redis-setup.yml'
        INVENTORY   = 'inventory/hosts.ini'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout')      { steps { checkout scm } }
        stage('Syntax Check')  { steps { syntaxCheckRedis(inventory: env.INVENTORY, playbook: env.PLAYBOOK) } }
        stage('Dry Run')       { steps { dryRunRedis(inventory: env.INVENTORY, playbook: env.PLAYBOOK) } }
        stage('Backup Config') { steps { backupRedisConfig(inventory: env.INVENTORY) } }
        stage('Deploy Redis')  { steps { deployRedis(inventory: env.INVENTORY, playbook: env.PLAYBOOK) } }
        stage('Validate')      { steps { validateRedis(inventory: env.INVENTORY) } }
    }

    post {
        failure {
            script {
                rollbackRedis(inventory: env.INVENTORY)
            }
        }
        always {
            node('') {
                cleanWs()
            }
        }
    }
}