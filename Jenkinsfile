pipeline {
    agent any
    environment {
        COCOS = '/Applications/Cocos/Creator/2.4.13/CocosCreator.app/Contents/MacOS/CocosCreator'
        PROJECT_PATH = 'bingo-frenzy-client'
    }
    stages {
        // 清理工作区, 打印参数
        stage('Clean Up') {
            steps {
                script {
                    echo 'Clean workspace...'
                    sh "rm -rf ${env.WORKSPACE}/workspace"

                    echo "All Parameters:"
                    params.each { key, value ->
                        echo "${key}: ${value}"
                    }
                }
            }
        }
        // 检出代码
        stage('Check Out Code') {
            steps {
                dir("${env.PROJECT_PATH}") {
                    // 检出代码, 重试 3 次
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
                            } catch (Exception e) {
                                // 如果检出失败，等待120秒后再次尝试
                                echo "Checkout failed, retrying in 120 seconds. Error: ${e.getMessage()}"
                                sleep time: 120, unit: 'SECONDS'
                                throw e // 重新抛出异常以确保可以被 retry 捕获
                            }
                        }
                    }
                    // 更新子模块, 首次需要手动编译引擎
                    script {
                        echo "Update Submodule..."
                        script {
                            def submodules = [
                                "engine",
                                "build/jsb-default/frameworks/cocos2d-x"
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
        // 安装依赖
        stage ('Dependencies') {
            steps {
                dir("${env.PROJECT_PATH}") {
                    // 自定义引擎
                    script {
                        echo "Customizing engine..."
                        def jsEnginePath = "${env.WORKSPACE}/${env.PROJECT_PATH}/engine"
                        def cppEnginePath = "${env.WORKSPACE}/${env.PROJECT_PATH}/build/jsb-default/frameworks/cocos2d-x"
                        sh """
                        cat local/settings_template.json \\
                        | sed "s|.*js-engine-path.*|  \\"js-engine-path\\": \\"${jsEnginePath}\\",|" \\
                        | sed "s|.*cpp-engine-path.*|  \\"cpp-engine-path\\": \\"${cppEnginePath}\\",|" \\
                        > local/settings.json
                        """
                    }
                    // 安装依赖
                    echo "Installing dependencies..."
                    sh 'yarn'
                }
            }
        }
        // 压缩纹理
        stage('Compress Texture') {
            steps {
                echo "Compressing texture..."
            }
        }
        // 下载配置文件
        stage('Download Configs') {
            steps {
                // 下载配置文件, 后面 Link Bundles 阶段会用到
                echo "Downloading configs..."
            }
        }
        // 链接 Bundle
        stage('Link Bundles') {
            steps {
                // 根据配置决定哪些 Bundle 需要链接
                echo "Linking bundles..."
            }
        }
        // 动态并行构建
        stage('Dynamic Parallel Build') {
            parallel {
                stage('Build web-mobile') {
                    when {
                        expression {
                            return params.BUILD_PLATFORMS.tokenize(',').contains('web-mobile')
                        }
                    }
                    steps {
                        echo "Building web-mobile..."
                        script {
                            def buildScript = load "scripts/build-web-mobile.groovy"
                            buildScript.build('web-mobile', params)
                        }
                    }
                }

                stage('Build ios') {
                    when {
                        expression {
                            return params.BUILD_PLATFORMS.tokenize(',').contains('ios')
                        }
                    }
                    steps {
                        echo "Building ios..."
                        script {
                            def buildScript = load "scripts/build-ios.groovy"
                            buildScript.build('ios', params)
                        }
                    }
                }

                stage('Build android') {
                    when {
                        expression {
                            return params.BUILD_PLATFORMS.tokenize(',').contains('android')
                        }
                    }
                    steps {
                        echo "Building android..."
                        script {
                            def buildScript = load "scripts/build-android.groovy"
                            buildScript.build('android', params)
                        }
                    }
                }

                stage('Build fb-instant-games') {
                    when {
                        expression {
                            return params.BUILD_PLATFORMS.tokenize(',').contains('fb-instant-games')
                        }
                    }
                    steps {
                        echo "Building fb-instant-games..."
                        script {
                            def buildScript = load "scripts/build-fb-instant-games.groovy"
                            buildScript.build('fb-instant-games', params)
                        }
                    }
                }
            }
        }
        // 构建 Native 包
        stage('Build Native') {
            parallel {
                stage('Build ios Native') {
                    when {
                        expression {
                            return params.BUILD_NATIVE_APP && params.BUILD_PLATFORMS.tokenize(',').contains('ios')
                        }
                    }
                    steps {
                        echo "Building ios native..."
                        script {
                            def buildScript = load "scripts/build-ios.groovy"
                            buildScript.buildNative('ios', params)
                        }
                    }
                }

                stage('Build android Native') {
                    when {
                        expression {
                            return params.BUILD_NATIVE_APP && params.BUILD_PLATFORMS.tokenize(',').contains('android')
                        }
                    }
                    steps {
                        echo "Building android native..."
                        script {
                            def buildScript = load "scripts/build-android.groovy"
                            buildScript.buildNative('android', params)
                        }
                    }
                }
            }
        }
    }
}