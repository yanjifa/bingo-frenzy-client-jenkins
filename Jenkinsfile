pipeline {
    agent any
    environment {
        COCOS = '/Applications/Cocos/Creator/2.4.13/CocosCreator.app/Contents/MacOS/CocosCreator'
    }
    stages {
        stage('Print Parameters') {
            steps {
                script {
                    echo "All Parameters:"
                    params.each { key, value ->
                        echo "${key}: ${value}"
                    }
                }
            }
        }
        stage('Check Out Code') {
            steps {
                dir('bingo-frenzy-client') {
                    script {
                        echo "Start check out code..."
                        retry(3) {
                            try {
                                    checkout scm: [
                                        $class: 'GitSCM',
                                        branches: [[name: params.TARGET_COMMIT_ID]],
                                        userRemoteConfigs: [[url: 'git@github.com:joycastle/bingo-frenzy-client.git']],
                                        extensions: [
                                            [$class: 'CloneOption', timeout: 120],
                                            [$class: 'CleanCheckout']                                    ]
                                    ]
                                }
                            } catch (Exception e) {
                                // 如果检出失败，等待120秒后再次尝试
                                echo "Checkout failed, retrying in 120 seconds. Error: ${e.getMessage()}"
                                sleep time: 120, unit: 'SECONDS'
                                throw e // 重新抛出异常以确保可以被 retry 捕获
                            }
                    }
                    script {
                        echo "Update Submodule..."
                        script {
                            def submodules = [
                                "engine"
                                // "engine-native"
                            ]
                            submodules.each {
                                sh "git submodule update --init --recursive ${it}"
                            }
                        }
                    }
                }
            }
        }
        stage('Compress Texture') {
            steps {
                echo "Compressing texture..."
            }
        }
        stage('Download Configs') {
            steps {
                // 下载配置文件, 后面 Link Bundles 阶段会用到
                echo "Downloading configs..."
            }
        }
        stage('Link Bundles') {
            steps {
                // 根据配置决定哪些 Bundle 需要链接
                echo "Linking bundles..."
            }
        }
        stage('Dynamic Parallel Build') {
            steps {
                script {
                    def platforms = params.BUILD_PLATFORMS.tokenize(',')
                    def buildStages = [:]

                    // 遍历每个平台并添加到并行构建阶段
                    for (platform in platforms) {
                        def currentPlatform = platform // 创建新的局部变量以避免闭包问题
                        buildStages["Build ${currentPlatform}"] = {
                            def script = load "scripts/build-${currentPlatform}.groovy"
                            script.build(currentPlatform, params)
                        }
                    }
                    // 执行并行阶段
                    parallel buildStages
                }
            }
        }
    }
}