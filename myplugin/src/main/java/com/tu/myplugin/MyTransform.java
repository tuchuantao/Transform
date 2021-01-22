package com.tu.myplugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Created by tuchuantao on 2021/1/22
 */
public class MyTransform extends Transform {

  @Override
  public String getName() {
    return "MyTransform";
  }

  @Override
  public Set<QualifiedContent.ContentType> getInputTypes() {
    return TransformManager.CONTENT_CLASS;
  }

  @Override
  public Set<? super QualifiedContent.Scope> getScopes() {
    return TransformManager.SCOPE_FULL_PROJECT;
  }

  @Override
  public boolean isIncremental() {
    return false;
  }

  @Override
  public void transform(TransformInvocation invocation) {
    System.out.println(">>>>>>MyTransform transform()");
    for (TransformInput input : invocation.getInputs()) {
      //遍历jar文件 对jar不操作，但是要输出到out路径
      input.getJarInputs().parallelStream().forEach(jarInput -> {
        File src = jarInput.getFile();
        System.out.println(">>>>>>transform()  input.getJarInputs fielName:" + src.getName());
        File dst = invocation.getOutputProvider().getContentLocation(
            jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(),
            Format.JAR);
        try {
          FileUtils.copyFile(src, dst);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
      //遍历文件，在遍历过程中
      input.getDirectoryInputs().parallelStream().forEach(directoryInput -> {
        File src = directoryInput.getFile();
        System.out.println(">>>>>getDirectoryInputs()  fielName:" + src.getName() + "  directoryInput=" + directoryInput);
        File dst = invocation.getOutputProvider().getContentLocation(
            directoryInput.getName(), directoryInput.getContentTypes(),
            directoryInput.getScopes(), Format.DIRECTORY);
        try {
          scanFilesAndInsertCode(src.getAbsolutePath());
          System.out.println(">>>>>getDirectoryInputs() dst=" + dst);
          FileUtils.copyDirectory(src, dst);
//          FileUtils.deleteDirectory(src);
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      });
    }
  }

  private void scanFilesAndInsertCode(String path) throws Exception {
    System.out.println(">>>>>scanFilesAndInsertCode（） file path=" + path);
    ClassPool classPool = ClassPool.getDefault();
    classPool.appendClassPath(path);//将当前路径加入类池,不然找不到这个类
    CtClass ctClass = classPool.getCtClass("com.tu.transform.PluginTestClass");
    if (ctClass == null) {
      return;
    }

    if (ctClass.isFrozen()) {
      ctClass.defrost();
    }
    CtMethod ctMethod = ctClass.getDeclaredMethod("init");

    String insetStr = "System.out.println(\"我是插入的代码\");";
    ctMethod.insertAfter(insetStr);//在方法末尾插入代码
    ctClass.writeFile(path);
    ctClass.detach();//释放
  }
}
