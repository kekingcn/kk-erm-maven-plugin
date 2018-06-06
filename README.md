# kk-erm-maven-plugin
将er关系描述文件生成JPA实体Entity的maven插件，模型中的说明会以注释的形式添加到Entity中

# erm是什么？
erm全称ermaster，是个基于eclipse插件建模的工具，支持从数据库导入关系生成ER图，导出设计图，导出DDL数据定义语句等功能。支持主流的数据库（mysql，Oracle，db2）建模。更多内容请参考官网介绍：[http://ermaster.sourceforge.net/](http://ermaster.sourceforge.net/)


![输入图片说明](https://gitee.com/uploads/images/2018/0606/155059_2d6357d5_492218.png "屏幕截图.png")

# 为什么开发开源这个插件
er关系建模在我读书的时候非常流行，虽然现在的开发模式慢慢的淡化了er建模在工程化项目中的作用。特别互联网项目，因为soa服务化，微服务等架构兴起后，项目模块被划分到各个独立的项目，新的单体项目维护100~200张表关系的项目基本没有了（我没见到）。但是有些场景，如内部管理系统，后台管理型，面向企业内部使用的系统，不需要做服务拆分，单体项目维护表也在30张以上了。使用er模型可以很好的管理实体关系。这种情况下通过使用这个插件后，你只需要维护er模型图就可以了，不需要自己创建数据库表对应Entity实体了。kk-erm-maven-plugin统统帮你搞定，生成的Entity文件如：

![输入图片说明](https://gitee.com/uploads/images/2018/0606/165444_0a97112a_492218.png "屏幕截图.png")

# 快速开始
> 拉代码，构建插件项目
本项目jar包没有上传到中央仓库，需要你拉下代码后，通过 mvn install自己打包

> 引入maven plugin
在项目pom.xml插件模块引入插件

```
            <plugin>
                <groupId>com.keking.plugin</groupId>
                <artifactId>erm-entity-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <configuration>
                    <designs>
                        <design>database.erm</design>
                    </designs>
                    <targetModule>com.kl</targetModule>
                    <!--<outputDirectory>${project.build.directory}/kl-entities</outputDirectory>-->
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>entity</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```
#配置说明

- designs->design:配置erm模型文件路径，必填的
- targetModule：目标模块包层次结构，必填的
- outputDirectory：entity实体输出路径，选填的，默认输出路径为：${project.build.directory}/kl-entities
