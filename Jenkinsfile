pipeline {
    agent any
    environment {
        COCOS = '/Applications/Cocos/Creator/2.4.13/CocosCreator.app/Contents/MacOS/CocosCreator'
        REMOTE_URL = 'git@github.com:joycastle/bingo-frenzy-client.git'
        PROJECT_PATH = 'bingo-frenzy-client'
        WORK_DIR_NAME = 'workspace'
        CONFIG_DIR_NAME = 'configs'
    }
    stages {
        // 检出代码
        stage('Check Out Code') {
            steps {
                dir("${env.PROJECT_PATH}") {
                    // 检出代码, 重试 3 次
                    script {
                        echo "Start check out code..."
                        def err = null
                        retry(3) {
                            try {
                                checkout scm: [
                                    $class: 'GitSCM',
                                    branches: [[name: params.TARGET_COMMIT_ID]],
                                    userRemoteConfigs: [[url: env.REMOTE_URL]],
                                    extensions: [
                                        [$class: 'CloneOption', timeout: 120],
                                        [$class: 'CleanCheckout']                                    ]
                                ]
                            } catch (Exception e) {
                                if(e instanceof InterruptedException) {
                                    // 中断异常，可能手动取消, 不重试
                                    echo "Checkout was aborted by user. Error: ${e.getMessage()}"
                                    err = e
                                } else {
                                    // 如果检出失败，等待 120 秒后再次尝试
                                    echo "Checkout failed, retrying in 120 seconds. Error: ${e.getMessage()}"
                                    sleep time: 120, unit: 'SECONDS'
                                    throw e // 重新抛出异常以确保可以被 retry 捕获
                                }
                            }
                        }
                        if(err != null) {
                            throw err
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
        // 安装依赖 & 自定义引擎
        stage ('Setup') {
            steps {
                dir("${env.PROJECT_PATH}") {
                    script {
                        // 自定义引擎
                        echo "Customizing engine..."
                        def jsEnginePath = "${env.WORKSPACE}/${env.PROJECT_PATH}/engine"
                        def cppEnginePath = "${env.WORKSPACE}/${env.PROJECT_PATH}/build/jsb-default/frameworks/cocos2d-x"
                        sh """
                        cat local/settings_template.json \\
                        | sed "s|.*js-engine-path.*|  \\"js-engine-path\\": \\"${jsEnginePath}\\",|" \\
                        | sed "s|.*cpp-engine-path.*|  \\"cpp-engine-path\\": \\"${cppEnginePath}\\",|" \\
                        > local/settings.json
                        """
                        // 安装依赖
                        echo "Installing dependencies..."
                        sh 'yarn'
                    }
                }
            }
        }
        // 下载配置文件
        stage('Download Configs') {
            steps {
                // 下载配置文件, 后面 Link Bundles 阶段会用到
                echo "Downloading configs..."
                script {
                    def err = null
                    retry(5) {
                        try {
                            // 定义变量
                            def uuid = sh(script: 'uuidgen', returnStdout: true).trim()
                            def downloadPath = "download/${uuid}"
                            def toPath = "${env.WORK_DIR_NAME}/${env.CONFIG_DIR_NAME}"
                            def env = params.ENVIRONMENT == 'Prod' ? 'production' : 'develop'
                            def sheetId = '1XkorKsp8XLiXubD9ffpFsvS6gSXTtbMqk1fe-EDv3ss'
                            def target = "ubuntu@release.bingo-testing.elitescastle.com"

                            // 打印环境和目标信息
                            echo "env=$env"
                            echo "sheet_id=$sheetId"
                            echo "target=$target"

                            // 远程执行 SSH 命令
                            sh """
                            ssh ${target} '
                                cd /home/ubuntu/ext/bingobe/download/
                                find . -maxdepth 1 -type d -mtime +7 -exec rm -rf {} \\;
                                cd /home/ubuntu/ext/bingobe
                                ./bingotool.linux gds export --indent --out "${downloadPath}" --env "${env}" --sheet "${sheetId}"
                            '
                            """
                            echo "Download done"

                            // 清理和准备本地目录
                            sh "rm -rf ${toPath}"
                            sh "mkdir -p ${toPath}"

                            // 使用 rsync 同步数据
                            sh """
                                rsync --del -avrz --progress -h -e 'ssh' ${target}:~/ext/bingobe/${downloadPath}/client ${toPath}
                            """
                            echo "Rsync done!!!"
                        } catch (Exception e) {
                            if(e instanceof InterruptedException) {
                                // 中断异常，可能手动取消, 不重试
                                echo "Download configs was aborted by user. Error: ${e.getMessage()}"
                                err = e
                            } else {
                                // TODO: 发送飞书通知
                                // 如果下载失败，等待 60 秒后再次尝试
                                echo "Download configs failed, retrying in 60 seconds. Error: ${e.getMessage()}"
                                sleep time: 60, unit: 'SECONDS'
                                throw e // 重新抛出异常以确保可以被 retry 捕获
                            }
                        }
                    }
                    if(err != null) {
                        throw err
                    }
                }
            }
        }
        // 压缩纹理
        stage('Compress Texture') {
            steps {
                echo "Compressing texture..."
                // dir("${env.PROJECT_PATH}/tools/images_compress") {
                //     def err = null
                //     retry(3) {
                //         try {
                //             sh 'yarn'
                //             sh 'yarn tsc -p .'
                //             sh 'node dist/index.js'
                //         } catch (Exception e) {
                //             if(e instanceof InterruptedException) {
                //                 // 中断异常，可能手动取消, 不重试
                //                 echo "Compress texture was aborted by user. Error: ${e.getMessage()}"
                //                 err = e
                //             } else {
                //                 // 如果压缩失败，等待 30 秒后再次尝试
                //                 echo "Compress texture failed, retrying in 30 seconds. Error: ${e.getMessage()}"
                //                 sleep time: 30, unit: 'SECONDS'
                //                 throw e // 重新抛出异常以确保可以被 retry 捕获
                //             }
                //         }
                //     }
                //     if(err != null) {
                //         throw err
                //     }
                // }
            }
        }
        // 链接 Bundle
        stage('Link Bundles') {
            steps {
                // 根据配置决定哪些 Bundle 需要链接
                echo "Linking bundles..."
            }
        }
        // 并行构建 Cocos
        stage('Build Cocos') {
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
        // 并行构建 Native 包
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