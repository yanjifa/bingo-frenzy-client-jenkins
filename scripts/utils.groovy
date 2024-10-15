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
    // 定义所有需要同步的文件或目录
    def rasyncItems = [
        '@types',
        'assets',
        'build-templates',
        'configs',
        'local',
        'node_modules',
        'packages',
        'package.json',
        'settings',
        'tools',
        'project.json',
        'template-banner.png',
        'template.json',
        'tsconfig.cocos.json',
        'tsconfig.json',
        'jsconfig.json'
    ]
    // 使用 Jenkins 的 WORKSPACE 环境变量
    def workspaceDir = "${env.WORKSPACE}/bingo-frenzy-client"
    // 同步工程文件到工作区, 保持工作区与源完全一致
    rasyncItems.each { item ->
        sh "rsync -av --delete ${workspaceDir}/${item}/ ${item}/"
    }
}

def buildCocos(String buildArg) {
    sh "${env.COCOS} --path . --build '${buildArg}'"
}

return this;