## 如何使用
## 思路
1. 通过ASM 扫描继承了 [PlugStart.java](start%2FPlugStart.java) 的类
2. 通过自定义类加载器来加载jar，并获取[PlugStart.java](start%2FPlugStart.java)的class ，并运行
3. 关于类加载器
4. isClassExists 方法来判断该类是否能被官方类加载器和自定义加载器加载
5. isSkipLoad 跳过那些引用了外部依赖的类（这些类通常无法被加载，因为插件作者不想让应用加载那些外部依赖），该功能需要用ASM 扫描带有[SkipLoadClass.java](annotation%2FSkipLoadClass.java)注解的类，然后再类加载的时候进行判断
6. [ClassReaderUtil.java](loader%2FClassReaderUtil.java) 通过该类可以获取class的byte数组，这些class可以位于jar或者文件夹
7. 关于多例和单例，因为代码臃肿且不是主要功能就暂且不提
8. searchClass 方法会扫描所有的class然后进行处理，但是因为资源消耗太大，所以选择了引入ASM
9. loadClass 方法有多个重载分别是public和protected的，这是从父类继承的注意区分
10. forName 方法依赖于单例模式，功能同样不值一提暂且不叙述
11. 关于SPI
12. 因为原始的SPI机制，不能加载在Jar中的SPI文件，所以在面对黑箱似的插件中。使用起来局限太大
13. PlugServiceLoader 自实现SPI类(简单实现) 扫描类加载器所在的项目下的SPI文件，与类加载器所要加载的目标Jar下的SPI文件
14. 插件需要在 META-INF/services/ 下建立SPI文件，文件名称为`github.zimoyin.mtool.plug.start.PlugStartSPI` 文件内容为插件的全限定名
15. 注意插件需要实现 `github.zimoyin.mtool.plug.start.PlugStartSPI` 接口
16. PlugLoader 需要为单例模式的时候才能使用SPI