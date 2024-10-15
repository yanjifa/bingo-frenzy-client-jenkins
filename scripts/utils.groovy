def makeWorkSpace() {
    // 删除不需要的文件
    def delItems = [
        'build',
        'temp',
        'library'
    ]
    delItems.each { item ->
        sh "rm -rf ${item}"
    }
    // 定义所有需要同步的目录
    def rasyncDirs = [
        '@types',
        'assets',
        'ext-bundles',
        'build-templates',
        'configs',
        'local',
        'node_modules',
        'packages',
        'settings',
        'tools'
    ]
    // 定义所有需要同步的文件
    def rsyncFiles = [
        'package.json',
        'project.json',
        'template-banner.png',
        'template.json',
        'tsconfig.cocos.json',
        'tsconfig.json',
        'jsconfig.json'
    ]
    // 使用 Jenkins 的 WORKSPACE 环境变量
    def workspaceDir = "${env.WORKSPACE}/${env.PROJECT_PATH}"
    // 同步工程文件到工作区, 保持工作区与源完全一致
    rasyncDirs.each { item ->
        // 同步目录
        sh "rsync -av --delete ${workspaceDir}/${item}/ ${item}/"
    }
    rsyncFiles.each { item ->
        // 同步文件
        sh "rsync -av ${workspaceDir}/${item} ${item}"
    }
}

def buildCocos(String buildArg) {
    sh "${env.COCOS} --path . --build '${buildArg}'"
}

return this;