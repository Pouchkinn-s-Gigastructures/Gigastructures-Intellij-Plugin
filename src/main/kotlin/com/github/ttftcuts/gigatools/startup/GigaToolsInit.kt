package com.github.ttftcuts.gigatools.startup

import com.github.ttftcuts.gigatools.main.data.ToolData
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class GigaToolsInit : ProjectActivity {

    override suspend fun execute(project: Project) {
        ToolData.loadDataFile(project)
    }
}