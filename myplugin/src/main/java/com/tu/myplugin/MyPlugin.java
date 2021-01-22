package com.tu.myplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import com.android.build.gradle.BaseExtension;

/**
 * Created by tuchuantao on 2021/1/22
 */
public class MyPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    System.out.println(">>>>registerTransform()<<<<");
    project.getExtensions().findByType(BaseExtension.class)
        .registerTransform(new MyTransform());
  }
}
