```
Compilation started at Thu Jul  6 14:51:00

JAVA_HOME=$(/usr/libexec/java_home -v20) mvn -Dstyle.color=never -Dorg.slf4j.simpleLogger.defaultLogLevel=info -s ~/.m2/settings-no-proxies.xml -f "$(findup pom.xml)" test -o
[INFO] Scanning for projects...
[INFO] Inspecting build with total of 1 modules...
[INFO] Installing Nexus Staging features:
[INFO]   ... total of 1 executions of maven-deploy-plugin replaced with nexus-staging-maven-plugin
[INFO] 
[INFO] --------------------< org.microbean:microbean-bean >--------------------
[INFO] Building microBean™ Bean 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-toolchains-plugin:3.1.0:toolchain (default) @ microbean-bean ---
[INFO] Required toolchain: jdk [ vendor='openjdk' version='20' ]
[INFO] Found matching toolchain for type jdk: JDK[/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home]
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) @ microbean-bean ---
[INFO] skip non existing resourceDirectory /Users/lairdnelson/Projects/github/microbean/microbean-bean/src/main/resources
[INFO] Copying 1 resource from  to target/classes/META-INF
[INFO] 
[INFO] --- maven-compiler-plugin:3.11.0:compile (default-compile) @ microbean-bean ---
[INFO] Toolchain in maven-compiler-plugin: JDK[/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home]
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) @ microbean-bean ---
[INFO] skip non existing resourceDirectory /Users/lairdnelson/Projects/github/microbean/microbean-bean/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.11.0:testCompile (default-testCompile) @ microbean-bean ---
[INFO] Toolchain in maven-compiler-plugin: JDK[/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home]
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-surefire-plugin:3.0.0:test (default-test) @ microbean-bean ---
[INFO] Toolchain in maven-surefire-plugin: JDK[/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home]
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running org.microbean.bean.TestDefaultBeanSet
[INFO] Running org.microbean.bean.TestConstableSemantics
[INFO] Running org.microbean.bean.TestMethodHandlesLookup
[INFO] Running org.microbean.bean.TestReferenceTypeList
[INFO] Running org.microbean.bean.TestFactory
[INFO] Running org.microbean.bean.TestQualifiers
[INFO] Running org.microbean.bean.TestJlm
[INFO] Running org.microbean.bean.TestSelector
[WARNING] Tests run: 1, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 0.022 s - in org.microbean.bean.TestMethodHandlesLookup
[INFO] Running org.microbean.bean.TestConstantDescs
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.025 s - in org.microbean.bean.TestFactory
[INFO] Running org.microbean.bean.TestBean
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.027 s - in org.microbean.bean.TestQualifiers
[INFO] Running org.microbean.bean.TestConcurrentHashMapComputation
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s - in org.microbean.bean.TestConcurrentHashMapComputation
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.007 s - in org.microbean.bean.TestConstantDescs
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.211 s - in org.microbean.bean.TestJlm
*** supertype of T: none
*** supertype of S: java.lang.String
*** types: [java.io.Serializable]
java.lang.Exception: Stack trace
	at java.base/java.lang.Thread.dumpStack(Thread.java:2246)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang.declaredType(Lang.java:1373)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang$ConstableTypeAndElementSource.declaredType(Lang.java:2016)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.TypeAndElementSource.declaredType(TypeAndElementSource.java:56)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.bean(DefaultBeanSet.java:221)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:177)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:137)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:109)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.TestDefaultBeanSet.setup(TestDefaultBeanSet.java:74)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:578)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:727)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptLifecycleMethod(TimeoutExtension.java:128)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptBeforeEachMethod(TimeoutExtension.java:78)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeMethodInExtensionContext(ClassBasedTestDescriptor.java:520)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$synthesizeBeforeEachMethodAdapter$23(ClassBasedTestDescriptor.java:505)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeEachMethods$3(TestMethodTestDescriptor.java:174)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeMethodsOrCallbacksUntilExceptionOccurs$6(TestMethodTestDescriptor.java:202)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(TestMethodTestDescriptor.java:202)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeEachMethods(TestMethodTestDescriptor.java:171)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:134)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool.helpJoin(ForkJoinPool.java:2076)
	at java.base/java.util.concurrent.ForkJoinTask.awaitDone(ForkJoinTask.java:423)
	at java.base/java.util.concurrent.ForkJoinTask.join(ForkJoinTask.java:651)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.joinConcurrentTasksInReverseOrderToEnableWorkStealing(ForkJoinPoolHierarchicalTestExecutorService.java:179)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.invokeAll(ForkJoinPoolHierarchicalTestExecutorService.java:153)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1312)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1843)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1808)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:188)
java.lang.Exception: Stack trace
	at java.base/java.lang.Thread.dumpStack(Thread.java:2246)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang.declaredType(Lang.java:1373)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang$ConstableTypeAndElementSource.declaredType(Lang.java:2016)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.TypeAndElementSource.declaredType(TypeAndElementSource.java:56)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.bean(DefaultBeanSet.java:221)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:177)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:137)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:109)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.TestDefaultBeanSet.setup(TestDefaultBeanSet.java:74)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:578)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:727)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptLifecycleMethod(TimeoutExtension.java:128)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptBeforeEachMethod(TimeoutExtension.java:78)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeMethodInExtensionContext(ClassBasedTestDescriptor.java:520)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$synthesizeBeforeEachMethodAdapter$23(ClassBasedTestDescriptor.java:505)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeEachMethods$3(TestMethodTestDescriptor.java:174)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeMethodsOrCallbacksUntilExceptionOccurs$6(TestMethodTestDescriptor.java:202)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(TestMethodTestDescriptor.java:202)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeEachMethods(TestMethodTestDescriptor.java:171)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:134)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.tryRemoveAndExec(ForkJoinPool.java:1351)
	at java.base/java.util.concurrent.ForkJoinTask.awaitDone(ForkJoinTask.java:422)
	at java.base/java.util.concurrent.ForkJoinTask.join(ForkJoinTask.java:651)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.joinConcurrentTasksInReverseOrderToEnableWorkStealing(ForkJoinPoolHierarchicalTestExecutorService.java:179)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.invokeAll(ForkJoinPoolHierarchicalTestExecutorService.java:153)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1312)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1843)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1808)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:188)
java.lang.Exception: Stack trace
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.228 s - in org.microbean.bean.TestBean
	at java.base/java.lang.Thread.dumpStack(Thread.java:2246)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang.declaredType(Lang.java:1373)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang$ConstableTypeAndElementSource.declaredType(Lang.java:2016)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.TypeAndElementSource.declaredType(TypeAndElementSource.java:56)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.bean(DefaultBeanSet.java:223)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:177)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:137)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:109)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.TestDefaultBeanSet.setup(TestDefaultBeanSet.java:74)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:578)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:727)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptLifecycleMethod(TimeoutExtension.java:128)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptBeforeEachMethod(TimeoutExtension.java:78)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeMethodInExtensionContext(ClassBasedTestDescriptor.java:520)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$synthesizeBeforeEachMethodAdapter$23(ClassBasedTestDescriptor.java:505)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeEachMethods$3(TestMethodTestDescriptor.java:174)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeMethodsOrCallbacksUntilExceptionOccurs$6(TestMethodTestDescriptor.java:202)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(TestMethodTestDescriptor.java:202)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeEachMethods(TestMethodTestDescriptor.java:171)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:134)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool.helpJoin(ForkJoinPool.java:2076)
	at java.base/java.util.concurrent.ForkJoinTask.awaitDone(ForkJoinTask.java:423)
	at java.base/java.util.concurrent.ForkJoinTask.join(ForkJoinTask.java:651)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.joinConcurrentTasksInReverseOrderToEnableWorkStealing(ForkJoinPoolHierarchicalTestExecutorService.java:179)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.invokeAll(ForkJoinPoolHierarchicalTestExecutorService.java:153)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1312)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1843)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1808)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:188)
java.lang.Exception: Stack trace
	at java.base/java.lang.Thread.dumpStack(Thread.java:2246)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang.declaredType(Lang.java:1373)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang$ConstableTypeAndElementSource.declaredType(Lang.java:2016)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.TypeAndElementSource.declaredType(TypeAndElementSource.java:56)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.bean(DefaultBeanSet.java:223)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:177)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:137)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:109)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.TestDefaultBeanSet.setup(TestDefaultBeanSet.java:74)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:578)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:727)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptLifecycleMethod(TimeoutExtension.java:128)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptBeforeEachMethod(TimeoutExtension.java:78)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeMethodInExtensionContext(ClassBasedTestDescriptor.java:520)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$synthesizeBeforeEachMethodAdapter$23(ClassBasedTestDescriptor.java:505)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeEachMethods$3(TestMethodTestDescriptor.java:174)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeMethodsOrCallbacksUntilExceptionOccurs$6(TestMethodTestDescriptor.java:202)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(TestMethodTestDescriptor.java:202)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeEachMethods(TestMethodTestDescriptor.java:171)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:134)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.tryRemoveAndExec(ForkJoinPool.java:1351)
	at java.base/java.util.concurrent.ForkJoinTask.awaitDone(ForkJoinTask.java:422)
	at java.base/java.util.concurrent.ForkJoinTask.join(ForkJoinTask.java:651)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.joinConcurrentTasksInReverseOrderToEnableWorkStealing(ForkJoinPoolHierarchicalTestExecutorService.java:179)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.invokeAll(ForkJoinPoolHierarchicalTestExecutorService.java:153)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1312)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1843)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1808)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:188)
*** rtl types: [T, java.lang.constant.ClassDesc, java.lang.invoke.TypeDescriptor.OfField<java.lang.constant.ClassDesc>, java.lang.constant.ConstantDesc, java.lang.invoke.TypeDescriptor]
[ERROR] Tests run: 4, Failures: 0, Errors: 2, Skipped: 0, Time elapsed: 0.259 s <<< FAILURE! - in org.microbean.bean.TestDefaultBeanSet
[ERROR] org.microbean.bean.TestDefaultBeanSet.testConstableStuff  Time elapsed: 0.028 s  <<< ERROR!
java.lang.NullPointerException: Cannot invoke "javax.lang.model.type.TypeMirror.toString()" because "t" is null
	at jdk.compiler/com.sun.tools.javac.model.JavacTypes.getDeclaredType0(JavacTypes.java:272)
	at jdk.compiler/com.sun.tools.javac.model.JavacTypes.getDeclaredType(JavacTypes.java:241)
	at jdk.compiler/com.sun.tools.javac.model.JavacTypes.getDeclaredType(JavacTypes.java:249)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang.declaredType(Lang.java:1387)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang$ConstableTypeAndElementSource.declaredType(Lang.java:2016)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.bean(DefaultBeanSet.java:223)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:177)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:137)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:109)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.TestDefaultBeanSet.setup(TestDefaultBeanSet.java:74)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:578)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:727)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptLifecycleMethod(TimeoutExtension.java:128)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptBeforeEachMethod(TimeoutExtension.java:78)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeMethodInExtensionContext(ClassBasedTestDescriptor.java:520)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$synthesizeBeforeEachMethodAdapter$23(ClassBasedTestDescriptor.java:505)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeEachMethods$3(TestMethodTestDescriptor.java:174)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeMethodsOrCallbacksUntilExceptionOccurs$6(TestMethodTestDescriptor.java:202)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(TestMethodTestDescriptor.java:202)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeEachMethods(TestMethodTestDescriptor.java:171)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:134)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.tryRemoveAndExec(ForkJoinPool.java:1351)
	at java.base/java.util.concurrent.ForkJoinTask.awaitDone(ForkJoinTask.java:422)
	at java.base/java.util.concurrent.ForkJoinTask.join(ForkJoinTask.java:651)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.joinConcurrentTasksInReverseOrderToEnableWorkStealing(ForkJoinPoolHierarchicalTestExecutorService.java:179)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.invokeAll(ForkJoinPoolHierarchicalTestExecutorService.java:153)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1312)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1843)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1808)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:188)

[ERROR] org.microbean.bean.TestDefaultBeanSet.testBeans  Time elapsed: 0.02 s  <<< ERROR!
java.lang.NullPointerException: Cannot invoke "javax.lang.model.type.TypeMirror.toString()" because "t" is null
	at jdk.compiler/com.sun.tools.javac.model.JavacTypes.getDeclaredType0(JavacTypes.java:272)
	at jdk.compiler/com.sun.tools.javac.model.JavacTypes.getDeclaredType(JavacTypes.java:241)
	at jdk.compiler/com.sun.tools.javac.model.JavacTypes.getDeclaredType(JavacTypes.java:249)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang.declaredType(Lang.java:1387)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.Lang$ConstableTypeAndElementSource.declaredType(Lang.java:2016)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.bean(DefaultBeanSet.java:223)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:177)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:137)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:109)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.TestDefaultBeanSet.setup(TestDefaultBeanSet.java:74)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:578)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:727)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptLifecycleMethod(TimeoutExtension.java:128)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptBeforeEachMethod(TimeoutExtension.java:78)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeMethodInExtensionContext(ClassBasedTestDescriptor.java:520)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$synthesizeBeforeEachMethodAdapter$23(ClassBasedTestDescriptor.java:505)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeEachMethods$3(TestMethodTestDescriptor.java:174)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeMethodsOrCallbacksUntilExceptionOccurs$6(TestMethodTestDescriptor.java:202)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(TestMethodTestDescriptor.java:202)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeEachMethods(TestMethodTestDescriptor.java:171)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:134)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool.helpJoin(ForkJoinPool.java:2076)
	at java.base/java.util.concurrent.ForkJoinTask.awaitDone(ForkJoinTask.java:423)
	at java.base/java.util.concurrent.ForkJoinTask.join(ForkJoinTask.java:651)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.joinConcurrentTasksInReverseOrderToEnableWorkStealing(ForkJoinPoolHierarchicalTestExecutorService.java:179)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.invokeAll(ForkJoinPoolHierarchicalTestExecutorService.java:153)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1312)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1843)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1808)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:188)

*** rtl types: [S, java.lang.String, java.lang.Object, java.io.Serializable, java.lang.CharSequence, java.lang.Comparable<java.lang.String>, java.lang.constant.Constable, java.lang.constant.ConstantDesc]
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.265 s - in org.microbean.bean.TestSelector
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.265 s - in org.microbean.bean.TestConstableSemantics
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.267 s - in org.microbean.bean.TestReferenceTypeList
[INFO] 
[INFO] Results:
[INFO] 
[ERROR] Errors: 
[ERROR]   TestDefaultBeanSet.setup:74 » NullPointer Cannot invoke "javax.lang.model.type.TypeMirror.toString()" because "t" is null
[ERROR]   TestDefaultBeanSet.setup:74 » NullPointer Cannot invoke "javax.lang.model.type.TypeMirror.toString()" because "t" is null
[INFO] 
[ERROR] Tests run: 23, Failures: 0, Errors: 2, Skipped: 1
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.273 s
[INFO] Finished at: 2023-07-06T14:51:05-07:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.0.0:test (default-test) on project microbean-bean: 
[ERROR] 
[ERROR] Please refer to /Users/lairdnelson/Projects/github/microbean/microbean-bean/target/surefire-reports for the individual test results.
[ERROR] Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException

Compilation exited abnormally with code 1 at Thu Jul  6 14:51:05

VERY intermittent:

Compilation started at Mon Jul 24 14:11:53

JAVA_HOME=$(/usr/libexec/java_home -v20) mvn -Dstyle.color=never -Dorg.slf4j.simpleLogger.defaultLogLevel=info -s ~/.m2/settings-no-proxies.xml -f "$(findup pom.xml)" clean install javadoc:javadoc
[INFO] Scanning for projects...
[INFO] Inspecting build with total of 1 modules...
[INFO] Installing Nexus Staging features:
[INFO]   ... total of 1 executions of maven-deploy-plugin replaced with nexus-staging-maven-plugin
[INFO] 
[INFO] --------------------< org.microbean:microbean-bean >--------------------
[INFO] Building microBean™ Bean 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:3.2.0:clean (default-clean) @ microbean-bean ---
[INFO] Deleting /Users/lairdnelson/Projects/github/microbean/microbean-bean/target
[INFO] Deleting /Users/lairdnelson/Projects/github/microbean/microbean-bean (includes = [src/**/*~, *~], excludes = [])
[INFO] 
[INFO] --- maven-toolchains-plugin:3.1.0:toolchain (default) @ microbean-bean ---
[INFO] Required toolchain: jdk [ vendor='openjdk' version='20' ]
[INFO] Found matching toolchain for type jdk: JDK[/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home]
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) @ microbean-bean ---
[INFO] skip non existing resourceDirectory /Users/lairdnelson/Projects/github/microbean/microbean-bean/src/main/resources
[INFO] Copying 1 resource from  to target/classes/META-INF
[INFO] 
[INFO] --- maven-compiler-plugin:3.11.0:compile (default-compile) @ microbean-bean ---
[INFO] Toolchain in maven-compiler-plugin: JDK[/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home]
[INFO] Changes detected - recompiling the module! :source
[INFO] Compiling 71 source files with javac [forked debug deprecation release 20 module-path] to target/classes
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) @ microbean-bean ---
[INFO] skip non existing resourceDirectory /Users/lairdnelson/Projects/github/microbean/microbean-bean/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.11.0:testCompile (default-testCompile) @ microbean-bean ---
[INFO] Toolchain in maven-compiler-plugin: JDK[/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home]
[INFO] Changes detected - recompiling the module! :dependency
[INFO] Compiling 12 source files with javac [forked debug deprecation release 20 module-path] to target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:3.0.0:test (default-test) @ microbean-bean ---
[INFO] Toolchain in maven-surefire-plugin: JDK[/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home]
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running org.microbean.bean.TestConstableSemantics
[INFO] Running org.microbean.bean.TestMethodHandlesLookup
[INFO] Running org.microbean.bean2.TestFactory
[INFO] Running org.microbean.bean.TestDefaultBeanSet
[INFO] Running org.microbean.bean.TestJlm
[INFO] Running org.microbean.bean.TestQualifiers
[INFO] Running org.microbean.bean.TestSelector
[INFO] Running org.microbean.bean.TestFactory
[WARNING] Tests run: 1, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 0.022 s - in org.microbean.bean.TestMethodHandlesLookup
[INFO] Running org.microbean.bean.TestReferenceTypeList
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.026 s - in org.microbean.bean2.TestFactory
[INFO] Running org.microbean.bean.TestConstantDescs
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 s - in org.microbean.bean.TestFactory
[INFO] Running org.microbean.bean.TestBean
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 s - in org.microbean.bean.TestQualifiers
[INFO] Running org.microbean.bean.TestConcurrentHashMapComputation
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s - in org.microbean.bean.TestConcurrentHashMapComputation
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.015 s - in org.microbean.bean.TestConstantDescs
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.246 s - in org.microbean.bean.TestJlm
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.262 s - in org.microbean.bean.TestSelector
*** supertype of T: none
*** types: [java.io.Serializable]
*** supertype of S: java.lang.String
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.259 s - in org.microbean.bean.TestBean
*** rtl types: [T, java.lang.constant.ClassDesc, java.lang.invoke.TypeDescriptor.OfField<java.lang.constant.ClassDesc>, java.lang.constant.ConstantDesc, java.lang.invoke.TypeDescriptor]
*** rtl types: [S, java.lang.String, java.lang.Object, java.io.Serializable, java.lang.CharSequence, java.lang.Comparable<java.lang.String>, java.lang.constant.Constable, java.lang.constant.ConstantDesc]
[ERROR] Tests run: 6, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.279 s <<< FAILURE! - in org.microbean.bean.TestReferenceTypeList
[ERROR] org.microbean.bean.TestDefaultBeanSet.testTypeElementResolution  Time elapsed: 0.004 s  <<< FAILURE!
java.lang.AssertionError: null type element for module org.microbean.bean and org.microbean.bean.Alternate
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.TypeAndElementSource.typeElement(TypeAndElementSource.java:136)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.TypeAndElementSource.declaredType(TypeAndElementSource.java:61)
	at org.microbean.lang@0.0.1-SNAPSHOT/org.microbean.lang.TypeAndElementSource.declaredType(TypeAndElementSource.java:61)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:184)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:137)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.DefaultBeanSet.<init>(DefaultBeanSet.java:109)
	at org.microbean.bean@0.0.1-SNAPSHOT/org.microbean.bean.TestDefaultBeanSet.setup(TestDefaultBeanSet.java:77)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	at java.base/java.lang.reflect.Method.invoke(Method.java:578)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:727)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptLifecycleMethod(TimeoutExtension.java:128)
	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptBeforeEachMethod(TimeoutExtension.java:78)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)
	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.invokeMethodInExtensionContext(ClassBasedTestDescriptor.java:520)
	at org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor.lambda$synthesizeBeforeEachMethodAdapter$23(ClassBasedTestDescriptor.java:505)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeEachMethods$3(TestMethodTestDescriptor.java:174)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeBeforeMethodsOrCallbacksUntilExceptionOccurs$6(TestMethodTestDescriptor.java:202)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(TestMethodTestDescriptor.java:202)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeBeforeEachMethods(TestMethodTestDescriptor.java:171)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:134)
	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
	at org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService$ExclusiveTask.compute(ForkJoinPoolHierarchicalTestExecutorService.java:202)
	at java.base/java.util.concurrent.RecursiveAction.exec(RecursiveAction.java:194)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:387)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1312)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1843)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1808)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:188)

[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.309 s - in org.microbean.bean.TestConstableSemantics
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.316 s - in org.microbean.bean.TestDefaultBeanSet
[INFO] 
[INFO] Results:
[INFO] 
[ERROR] Failures: 
[ERROR]   TestDefaultBeanSet.setup:77 null type element for module org.microbean.bean and org.microbean.bean.Alternate
[INFO] 
[ERROR] Tests run: 25, Failures: 1, Errors: 0, Skipped: 1
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.678 s
[INFO] Finished at: 2023-07-24T14:11:58-07:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.0.0:test (default-test) on project microbean-bean: There are test failures.
[ERROR] 
[ERROR] Please refer to /Users/lairdnelson/Projects/github/microbean/microbean-bean/target/surefire-reports for the individual test results.
[ERROR] Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException

Compilation exited abnormally with code 1 at Mon Jul 24 14:11:58

```
