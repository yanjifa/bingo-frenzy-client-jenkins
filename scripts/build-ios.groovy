def build(String platform, Map params) {
    // 加载工具类
    def utils = load 'scripts/utils.groovy'
    dir("${env.WORK_DIR_NAME}/${platform}") {
        // 准备工作区
        utils.setupWorkDir()
        // 延迟
        sleep time: 10, unit: 'SECONDS'
        // 构建 Cocos 项目, 重试 3 次
        def err = null
        retry(3) {
            try {
                def buildArg = "platform=${platform};buildPath=./build;template=default;debug=false;"
                utils.buildCocos(buildArg)
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    // 中断异常，可能手动取消, 不重试
                    echo "Build Cocos was aborted by user. Error: ${e.getMessage()}"
                    err = e
                } else {
                    // 如果构建失败，等待 30 秒后再次尝试
                    echo "Build Cocos failed, retrying in 30 seconds. Error: ${e.getMessage()}"
                    sleep time: 30, unit: 'SECONDS'
                    throw e // 重新抛出异常以确保可以被 retry 捕获
                }
            }
        }
        if(err != null) {
            throw err
        }
    }
    // 传入参数为构建结果的路径, 纯资源处理, 不需要网络, 不需要重试
    dir("${env.PROJECT_PATH}/tools/bundling-assets") {
        sh 'yarn'
        sh 'yarn tsc -p .'
        sh "node dist/index.js ${env.WORKSPACE}/${env.WORK_DIR_NAME}/${platform} ${platform}"
    }
    // remote asset bundle 上传 aws

    // 发布热更新
}

def buildNative(String platform, Map params) {
    dir("${env.WORK_DIR_NAME}/${platform}") {
    }
}

return this;